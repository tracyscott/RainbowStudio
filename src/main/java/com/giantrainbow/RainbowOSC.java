package com.giantrainbow;

import heronarts.lx.LX;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * RainbowBridge specific class for OSC interaction.
 */
public class RainbowOSC implements LXOscListener {
  private static final Logger logger = Logger.getLogger(RainbowOSC.class.getName());

  private static final int OSC_BUFFER_SIZE = 1024;
  private static final int OSC_PORT = 7979;
  public LX lx;
  public LXOscEngine.Transmitter rainbowOscTransmitter;

  static public List<String> pendingMessages = new ArrayList<String>();

  public RainbowOSC(LX lx) {
    this.lx = lx;

    try {
      // Register for custom OSC messages on a dedicated port
      LXOscEngine.Receiver r = lx.engine.osc.receiver(OSC_PORT).addListener(this);
      logger.info("OSC Receiver enabled.");
    } catch (java.net.SocketException sx) {
      throw new RuntimeException(sx);
    }
  }

  /**
   * Starts an OSC Transmitter for RainbowStudio control data with the specified destination address
   * and destination port.  Initiated by a client invoking OSC method /rainbow/registerclient with
   * a string argument in the form of hostname:port.  Currently, only one OSC transmitter is supported.
   * Additional calls to /rainbow/registerclient will just overwrite any current transmitter.
   *
   * @param address The hostname to send OSC packets to.
   * @param port The destination port number for the remote OSC receiver.
   * @param bufferSize The size of the packet buffer.
   * @return Returns true if it was successfully created.
   */
  public boolean startOscTransmitter(String address, int port, int bufferSize) {
    try {
      rainbowOscTransmitter = lx.engine.osc.transmitter(InetAddress.getByName(address), port, bufferSize);
      logger.info("OSC Sender enabled, destination: " + address + ":" + port);
      return true;
    } catch (UnknownHostException unhex) {
      logger.severe("UnknownHostException creating OSC Transmitter: " + unhex.getMessage());
      return false;
    } catch (SocketException sex) {
      logger.severe("SocketException creating OSC Transmitter: " + sex.getMessage());
    }
    return false;
  }

  /**
   * Pulls a pending textupdate message out of the list.
   */
  static public String getTextUpdateMessage() {
    synchronized (pendingMessages) {
      if (pendingMessages.size() == 0) {
        return null;
      } else {
        String msg = pendingMessages.remove(0);
        return msg;
      }
    }
  }

  public static String lastString = null;

  /**
   * Handle an OSC message from a client.  Root path of address space is /rainbow.
   *
   * registerclient: String argument of form host:port.  RainbowStudio will start sending control
   * data to the specified destination.  Used for phone/tablet control.
   *
   * @param message OscMessage from a client.
   */
  public void oscMessage(OscMessage message) {

    try {
      String addressPattern = message.getAddressPattern().getValue();
      String[] path = addressPattern.split("/");
      logger.info("Received OSC message at path: " + addressPattern + " = " + message.getString());

      if ("rainbow".equals(path[1])) {
        if (path.length > 2) {
          if ("registerclient".equals(path[2])) {
            if (path.length > 3) {
              // Register an OSC endpoint that we should send updates to
              String destination = message.getString();
              String[] addressPort = destination.split(":");
              startOscTransmitter(addressPort[0], Integer.parseInt(addressPort[1]), OSC_BUFFER_SIZE);
            }
          }
          // If a client requests /rainbow/reloadall we should send all available control parameter values
          // This would typically happen on startup.
          if ("reloadall".equals(path[2])) {

          }
          if ("textupdate".equals(path[2])) {
            logger.info("textupdate=" + message.getString(0));
            if (message.getString(0) != null && message.getString(0).trim().equals(""))
              return;
            // Handle some duplicate sending issue.
            if (message.equals(lastString))
              return;
            lastString = message.getString(0);
            synchronized (pendingMessages) {
              pendingMessages.add(message.getString(0));
            }
          }
          // TODO(tracy): Maybe change this to mobilesensor or something?
          if ("mobile".equals(path[2])) {
            if (path.length > 3) {
              if ("accelx".equals(path[3])) {
                RainbowStudio.oscSensor.accelXKnob.setValue(message.getFloat(0));
              } else if ("accely".equals(path[3])) {
                RainbowStudio.oscSensor.accelYKnob.setValue(message.getFloat(0));
              } else if ("accelz".equals(path[3])) {
                RainbowStudio.oscSensor.accelZKnob.setValue(message.getFloat(0));
              }
            }
          }
        }
      }
    } catch (Exception ex) {
      logger.severe("Error handling OSC message: " + ex.getMessage());
    }
  }

  public void sendOscMessage(OscMessage message) {
    try {
      if (rainbowOscTransmitter != null) rainbowOscTransmitter.send(message);
    } catch (IOException ioex) {
      System.err.println("Error sending OSC message: " + ioex.getMessage());
    }
  }
}

package com.giantrainbow.ui;

import heronarts.lx.LX;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * RainbowBridge specific class for OSC interaction.
 */
public class RainbowOSC implements LXOscListener {

  private static final int OSC_PORT = 7979;
  public LX lx;
  public LXOscEngine.Transmitter rainbowOscTransmitter;

  public RainbowOSC(LX lx) {
    this.lx = lx;

    try {
      // Register for custom OSC messages on a dedicated port
      LXOscEngine.Receiver r = lx.engine.osc.receiver(OSC_PORT).addListener(this);
      System.out.println("OSC Receiver enabled.");
      /*
      try {
        rainbowOscTransmitter = lx.engine.osc.transmitter(InetAddress.getByName("192.168.2.136"), 7980, 1024);
        System.out.println("OSC Sender enabled.");
      } catch (UnknownHostException unhex) {
        System.err.println("UnknownHostException creating OSC Transmitter: " + unhex.getMessage());
      } catch (SocketException sex) {
        System.err.println("SocketException creating OSC Transmitter: " + sex.getMessage());
      }
      */

    } catch (java.net.SocketException sx) {
      throw new RuntimeException(sx);
    }
  }

  public void oscMessage(OscMessage message) {
    String addressPattern = message.getAddressPattern().getValue();
    String[] path = addressPattern.split("/");
    System.out.println("Received OSC message at path: " + addressPattern + " = " + message.getString());
  }

  public void sendOscMessage(OscMessage message) {
    try {
      rainbowOscTransmitter.send(message);
    } catch (IOException ioex) {
      System.err.println("Error sending OSC message: " + ioex.getMessage());
    }
  }
}

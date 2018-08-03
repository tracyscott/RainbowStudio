package com.giantrainbow.model;

import static com.giantrainbow.RainbowStudio.ARTNET_PORT;
import static com.giantrainbow.RainbowStudio.LED_CONTROLLER_IP;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.radians;
import static processing.core.PApplet.sin;

import heronarts.lx.LX;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.LXDatagramOutput;
import java.util.ArrayList;
import java.util.List;

public class RainbowModel3D extends RainbowBaseModel {
  public static final int LED_WIDTH = 420;
  public static final int LED_HEIGHT = 30;

  public RainbowModel3D() {
    this(28);
  }

  public ArrayList<LXPoint> perimeter;

  public RainbowModel3D(int numPanels) {
    super(new Fixture(numPanels));
    float arc = (numPanels * 15.0f - 1) * RainbowBaseModel.rainbowThetaInc;
    this.thetaStart = 90.0f - arc/2.0f;
    this.thetaFinish = 90.0f + arc/2.0f;
    pointsWide = numPanels * 15;
    pointsHigh = 30;

    perimeter = new ArrayList<LXPoint>();

    // Left edge & right perimeters
    double z = 0;
    for (int rowNum = 0; rowNum < LED_HEIGHT; rowNum++) {
	double r = innerRadius + rowNum * radiusInc;
	double rx = r * cos(radians(thetaFinish + RainbowBaseModel.rainbowThetaInc));
	double ry = r * sin(radians(thetaFinish + RainbowBaseModel.rainbowThetaInc));
	double lx = r * cos(radians(thetaStart - RainbowBaseModel.rainbowThetaInc));
	double ly = r * sin(radians(thetaStart - RainbowBaseModel.rainbowThetaInc));
	perimeter.add(new LXPoint(lx, ly, z));
	perimeter.add(new LXPoint(rx, ry, z));
    }

    for (int colNum = 0; colNum < pointsWide; colNum++) {
	float angle = thetaStart + colNum * RainbowBaseModel.rainbowThetaInc;

	double br = innerRadius - radiusInc;
	double tr = innerRadius + (LED_HEIGHT+1) * radiusInc;

	double bx = br * cos(radians(angle));
	double by = br * sin(radians(angle));

	double tx = tr * cos(radians(angle));
	double ty = tr * sin(radians(angle));

	perimeter.add(new LXPoint(bx, by, z));
	perimeter.add(new LXPoint(tx, ty, z));
    }
  }

  public static class Fixture extends LXAbstractFixture {
    Fixture(int numPanels) {
      int columns = numPanels * 15;
      float arc = (numPanels * 15.0f - 1) * RainbowBaseModel.rainbowThetaInc;
      float thetaStart = 90.0f - arc/2.0f;
      float thetaFinish = 90.0f + arc/2.0f;
      float z = 0;
      float r = innerRadius;  // Feet
      int ledCount = 0;
      for (int rowNum = 0; rowNum < LED_HEIGHT; rowNum++) {
	      for (int colNum = columns - 1; colNum >= 0; colNum--) {
	        float angle = thetaStart + colNum * RainbowBaseModel.rainbowThetaInc;
          double x = r * cos(radians(angle));
          double y = r * sin(radians(angle));
          addPoint(new LXPoint(x, y, z));
          ledCount++;
        }
        r += radiusInc;  // Each strip is separated by 2.75 inches.  r is in units of feet.
      }
      outerRadius = r;
      System.out.println("thetaStart: " + thetaStart);
      System.out.println("thetaFinish: " + thetaFinish);
      System.out.println("ledCount: " + ledCount);
      System.out.println("outerRadius: " + outerRadius);
    }
  }

  /*
   * Determine point to LED mapping.  This code is placed inside the model class because
   * it is very tightly coupled to the model generation.  For now, we will just put 170 pixel
   * chunks into each ArtNetDatagram, increasing the universe number each time.
   * TODO(tracy): Fix this for final wiring.  Maybe it will be similar to RainbowPanel?
   * TODO(tracy): It would be nice to be able to configure this in the UI.  We would need to
   * call disable on the LXDatagramOutput, remove it, and then rebuild with the new IP/Port.
   */
  public static void configureOutput(LX lx) {
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    int pointsPerUniverse = 170;
    int numPoints = lx.model.points.length;
    int numUniverses = numPoints / pointsPerUniverse;

    for (int currentUniverse = 0; currentUniverse < numUniverses; currentUniverse++) {
      int pointOffset = currentUniverse * pointsPerUniverse;
      // This will be an array 170 point indices (aka LED id's)
      int[] pIndicesForUniverse = new int[pointsPerUniverse];
      // Here 'i' will take on the range of values of the global point index values.  For example,
      // we want to put point indices (aka LED id's) 170-339 into an array, which lets
      // the Datagram know which points (aka LED's) it is responsible for sending
      for (int i = pointOffset; i < pointOffset + pointsPerUniverse; i++) {
        pIndicesForUniverse[i - pointOffset] = i;
      }
      ArtNetDatagram datagram = new ArtNetDatagram(pIndicesForUniverse, currentUniverse);
      try {
        datagram.setAddress(LED_CONTROLLER_IP).setPort(ARTNET_PORT);
      } catch (java.net.UnknownHostException uhex) {
        System.out.println("ERROR! UnknownHostException while configuring ArtNet: " +
        LED_CONTROLLER_IP);
      }
      datagrams.add(datagram);
    }

    // Now we have datagrams bound to all of our LEDs.  Create a LXDatagramOutput and register
    // all datagrams with it.
    try {
      LXDatagramOutput datagramOutput = new LXDatagramOutput(lx);
      for (ArtNetDatagram datagram : datagrams) {
        datagramOutput.addDatagram(datagram);
      }
      // Finally, register our LXDatagramOutput with the engine.
      lx.engine.output.addChild(datagramOutput);
    } catch (java.net.SocketException sex) {
      // This can happen for example if we run out of file handles because some code is opening
      // files without closing them.
      System.out.println("ERROR! SocketException when initializing DatagramOutput: " + sex.getMessage());
    }
  }
}

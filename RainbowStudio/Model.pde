import java.util.List;

LXModel buildModel(int modelType) {
  // A three-dimensional grid model
  // return new GridModel3D();
  if (modelType == FULL_RAINBOW) {
    return new RainbowModel3D();
  } else if (modelType == SRIKANTH_PANEL) {
    return new SrikanthPanel();
  } else {
    return null;
  }
}

public static class GridModel3D extends LXModel {
  
  public final static int SIZE = 20;
  
  public GridModel3D() {
    super(new Fixture());
  }
  
  public static class Fixture extends LXAbstractFixture {
    Fixture() {
      for (int z = 0; z < SIZE; ++z) {
        for (int y = 0; y < SIZE; ++y) {
          for (int x = 0; x < SIZE; ++x) {
            addPoint(new LXPoint(x, y, z));
          }
        }
      }
    }
  }
}

/*
 * Abstract class for tracking things common to our models.
 * Currently, just the number of leds wide and leds high.  These
 * methods can be used by the Patterns so that a pattern can
 * work on either the full rainbow or some subset of LEDs
 * for test panels.
 * TODO(tracy): Maybe it would be better to use the Metrics
 * class that the 2D GridModel uses.  Also, their standard
 * pixel coordinate system is 0,0 top left and max_x,max_y on
 * the bottom right. It would be better to migrate to that 
 * coordinate system so some of the existing patterns in 
 * the LX library work.  It requires rewriting our existing
 * patterns so it should be done sooner rather than later.
 */
public static abstract class RainbowBaseModel extends LXModel {

  public RainbowBaseModel(LXFixture fixture) {
    super(fixture);
  }
  public int pointsWide;
  public int pointsHigh;
}

public static class SrikanthPanel extends RainbowBaseModel {
  
  public static final int LED_WIDTH = 10;
  public static final int LED_HEIGHT = 5;
  public static final int UNIVERSE = 0;
  
  public SrikanthPanel() {
    super(new Fixture());
    pointsWide = LED_WIDTH;
    pointsHigh = LED_HEIGHT;
  }
  
  public static class Fixture extends LXAbstractFixture {
    Fixture() {
      // Scale the panel so that it fits into 3D space where
      // the rainbow fits.  This will make switching between them
      // easy without a bunch of camera fussing.
      float worldWidth = 10.0;
      float worldHeight = 5.0; 
      int numRows = LED_HEIGHT;
      int numCols = LED_WIDTH;
      float widthPerColumn = worldWidth / numCols;
      float heightPerRow = worldHeight / numRows;
      for (int rowNum = 0; rowNum < numRows; rowNum++) {
        for (int colNum = 0; colNum < numCols; colNum++) {
          // Centered around x=0.0 in 3D space just like the rainbow.
          float x = colNum * widthPerColumn - worldWidth/2.0;
          float y = rowNum * heightPerRow;
          float z = 0.0;
          addPoint(new LXPoint(x, y, z));
        }
      }
    }
  }
  
  /*
   * Based on photo from Srikanth. 0 led on his board is bottom right. 149 led is
   * on top left.  The wiring starts from right to left and then alternates to
   * left to right moving up the rows.  The points in 3D space start with
   * 0 at the bottom left. y=1, x=0 also starts from the left side (direction does not
   * alternate like the wiring)
   */
  public static void configureOutput(LX lx) {

    final String ip = "192.168.2.1";  
    
    int[] ledWiring = new int[LED_WIDTH * LED_HEIGHT];

    // For each Point, we want to figure out which LED on the physical strip
    // corresponds to the 3D point.  We are technically mapping to DMX channels
    // here, but this is simple wiring on one universe.
    for (int rowNum = 0; rowNum < LED_HEIGHT; rowNum++) {
      for (int colNum = 0; colNum < LED_WIDTH; colNum++) {
        // Points are stored in a 1D array.
        int pointIndex = rowNum * LED_WIDTH + colNum;
        // First row, third row, etc. goes from right to left.
        // Second row, fourth row goes from left to right.
        // (i.e. the wiring snakes back and forth).
        // Point(row0, col0) (bottom left in 3d space) should be 
        //     led position 9 in first row (0-based index). i.e. LED_WIDTH - colNum
        // Point(row1, col0) LEDs start going left to right so it is LED 10
        // Point(row1, col1) is LED 11 i.e. LED_WIDTH * rowNum + colNum (generic solution)
        // Point(row2, col0) is LED 29 i.e. LED_WIDTH * (rowNum + 1) - 1
        // Point(row2, col1) is LED 28 i.e. LED_WIDTH * (rowNum + 1) - 2
        //     generic solution for 0,2,4 is LED_WIDTH * (rowNum + 1) - 1 - colNum
        int ledPos = 0;
        if (rowNum % 2 == 0) {
          ledPos = LED_WIDTH * (rowNum + 1) - 1 - colNum;
        } else {
          ledPos = LED_WIDTH * rowNum + colNum;
        }
        ledWiring[ledPos] = pointIndex;
      }
    }

    // All pixels fit in a single universe so we only need to create one datagram here.
    ArtNetDatagram datagram = new ArtNetDatagram(ledWiring, UNIVERSE);
    try {
      datagram.setAddress(LED_CONTROLLER_IP).setPort(ARTNET_PORT);
    } catch (java.net.UnknownHostException uhex) {
      System.out.println("ERROR! UnknownHostException while configuring ArtNet: " + LED_CONTROLLER_IP);
    }
    
    // Now we have datagrams bound to all of our LEDs.  Create a LXDatagramOutput and register
    // all datagrams with it.  
    try {
      LXDatagramOutput datagramOutput = new LXDatagramOutput(lx);
      datagramOutput.addDatagram(datagram);
      // Finally, register our LXDatagramOutput with the engine.
      lx.engine.output.addChild(datagramOutput);
    } catch (java.net.SocketException sex) {
      // This can happen for example if we run out of file handles because some code is opening
      // files without closing them.
      System.out.println("ERROR! SocketException when initializing DatagramOutput: " + sex.getMessage());
    }
  }
}

public static class RainbowModel3D extends RainbowBaseModel {
  
  static public final int LED_WIDTH = 420;
  static public final int LED_HEIGHT = 30;
  
  public RainbowModel3D() {
    super(new Fixture());
    pointsWide = LED_WIDTH;
    pointsHigh = LED_HEIGHT;
  }
  
  public static class Fixture extends LXAbstractFixture {
    Fixture() {
      // add Points based on led position.
      // Starts at 9.3165082 degrees
      // arc is 161.3669836 degrees
      // end point at 170.6834918
      // radius is 36.9879
      
      // For each strip at the given radius, generate 420 points
      float pointsPerRow = LED_WIDTH;
      float z = 0;
      float r = 36.9879;  // Feet
      int ledCount = 0;
      for (int rowNum = 0; rowNum < LED_HEIGHT; rowNum++) {
        r += 2.75 / 12.0;  // Each strip is separated by 2.75 inches.  r is in units of feet.
        //for (float angle = 9.4165082; angle < 170.5834918; angle += 161.3669836 / pointsPerRow) {
        for (float angle = 170.5383491; angle > 9.4165082; angle -= 161.3669836 / pointsPerRow) {
          float x = r * cos(radians(angle));
          float y = r * sin(radians(angle));
          addPoint(new LXPoint(x, y, z));
          ledCount++;
        }
      }
      System.out.println("ledCount: " + ledCount);
    }
  }
  
  /*
   * Determine point to LED mapping.  This code is placed inside the model class because
   * it is very tightly coupled to the model generation.  For now, we will just put 170 pixel
   * chunks into each ArtNetDatagram, increasing the universe number each time.
   * TODO(tracy): It would be nice to be able to configure this in the UI.  We would need to
   * call disable on the LXDatagramOutput, remove it, and then rebuild with the new IP/Port.
   */
  public static void configureOutput(LX lx) {
    final int ARTNET_PORT = 6454;
    final String ip = "192.168.2.1";  
    
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    int currentPointNum = 0;
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
        datagram.setAddress(ip).setPort(ARTNET_PORT);
      } catch (java.net.UnknownHostException uhex) {
        System.out.println("ERROR! UnknownHostException while configuring ArtNet: " + ip);
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

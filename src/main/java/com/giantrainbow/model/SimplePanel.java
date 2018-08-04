package com.giantrainbow.model;

import static com.giantrainbow.RainbowStudio.ARTNET_PORT;
import static com.giantrainbow.RainbowStudio.LEDS_PER_UNIVERSE;
import static com.giantrainbow.RainbowStudio.LED_CONTROLLER_IP;
import static processing.core.PApplet.ceil;

import com.giantrainbow.ArtSyncDatagram;
import com.giantrainbow.RainbowDatagram;
import com.giantrainbow.ui.UIPixliteConfig;
import heronarts.lx.LX;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.LXDatagramOutput;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Generic 2D grid model.  Defaults to dimensions of Srikanth's test panel.  Also,
 * configureOutput is only implemented for Srikanth's specific wiring.  The Animated GIF
 * pattern currently uses a 100x50 animated gif of the game of life.  The Animated GIF
 * pattern will scale images to fit the points, but doesn't make sense for game of
 * life so it will run but not look right if not run on the LARGE_PANEL modelType in
 * buildModel (can be specified at the top of RainbowStudio.pde).
 */
public class SimplePanel extends RainbowBaseModel {
  private static final Logger logger = Logger.getLogger(SimplePanel.class.getName());

  public static final int LED_WIDTH = 10;  // Defaults based on Srikanth's test panel
  public static final int LED_HEIGHT = 5;
  public static final int UNIVERSE = 0;

  public SimplePanel(int width, int height) {
      super(new Fixture(width, height));
      pointsWide = width;
      pointsHigh = height;
  }

  public SimplePanel() {
    super(new Fixture());
    pointsWide = LED_WIDTH;
    pointsHigh = LED_HEIGHT;
  }

  public static class Fixture extends LXAbstractFixture {
    Fixture() {
      this(LED_WIDTH, LED_HEIGHT);
    }

    Fixture(int width, int height) {
      // Determine the size in 3D space with this factor.  1.0 looks a bit
      // cramped, especially on the larger panel.  This should eventually account
      // for the physical led spacing on a given panel.
      float worldScaleFactor = 1.5f;
      float worldWidth = width/worldScaleFactor;
      float worldHeight = height/worldScaleFactor;
      int numRows = height;
      int numCols = width;
      float widthPerColumn = worldWidth / numCols;
      float heightPerRow = worldHeight / numRows;
      for (int rowNum = 0; rowNum < numRows; rowNum++) {
        for (int colNum = 0; colNum < numCols; colNum++) {
          // Centered around x=0.0 in 3D space just like the rainbow.
          float x = colNum * widthPerColumn - worldWidth/2.0f;
          float y = rowNum * heightPerRow;
          float z = 0.0f;
          addPoint(new LXPoint(x, y, z));
        }
      }
    }
  }

  /**
   * Multi-panel output.  Panels should be remappable to account for build time
   * issues/mistakes.  There are 2 Pixlite LongRange MKII's per side.  The first
   * Pixlite drives 16 panels with 450 leds each, 7200 leds.  The second drives 12 panels with
   * 450 leds each, 5400 leds.
   */
  public static void configureOutputMultiPanel(LX lx, UIPixliteConfig pixliteConfig) {
    // Config for panel size, number of panels, number of universes per panel, number of led controllers
    // and number of panels per controller.
    int pointsWidePerPanel = 15;
    int pointsHighPerPanel = 30;
    int pointsPerPanel = pointsWidePerPanel * pointsHighPerPanel;
    int pointsPerUniverse = 170;
    int pointsWide = ((RainbowBaseModel)lx.model).pointsWide;
    int pointsHigh = ((RainbowBaseModel)lx.model).pointsHigh;
    int numPanels = pointsWide / pointsWidePerPanel;
    int numLedControllers = 2;
    // TODO(tracy): Use numPanels here for testing with fewer panels.  Use actual values for installation.
    int panelsPerLedController1 = 16;  // Production values
    int panelsPerLedController2 = 12;
    int universesPerPanel = ceil((float)pointsPerPanel/(float)pointsPerUniverse);

    int maxColNumPerPanel = pointsWidePerPanel - 1;
    int currentLogicalPanel;
    logger.info("numPanels= " + numPanels);
    logger.info("controller 1 panels: " + panelsPerLedController1);
    logger.info("controller 2 panels: " + panelsPerLedController2);

    Map<Integer, List<Integer>> panelMap = new HashMap<Integer, List<Integer>>();
    // First, just build this in a straightforward way.
    for (int i = 0; i < numPanels; i++) {
      List<Integer> universesThisPanel = new ArrayList<Integer>();
      for (int j = 0; j < universesPerPanel; j++) {
        int universeNum = i * universesPerPanel + j;
        // Reset universe numbers for second pixlite
        if (i >= panelsPerLedController1) {
          universeNum = (i - panelsPerLedController1) * universesPerPanel + j;
        }
        universesThisPanel.add(universeNum);
      }
      panelMap.put(i, universesThisPanel);
    }

    // Modify the mapping here
    // panelMap.put(0, (15, 16, 17));
    // panelMap.put(5, (0, 1, 2));


    // This iterates through all our points.  We also need to track our current logical
    // panel (which should just be globalLedPos / pointsPerPanel;
    // We need to iterate through panels and build datagrams that way.
    int globalLedPos = 0;

    // Each controller gets a list of ArtNetDatagrams that it is responsible for.
    List<List<ArtNetDatagram>> ledControllersDatagrams = new ArrayList<List<ArtNetDatagram>>(numLedControllers);
    List<ArtNetDatagram> pixlite1Datagrams = new ArrayList<ArtNetDatagram>();
    ledControllersDatagrams.add(pixlite1Datagrams);
    List<ArtNetDatagram> pixlite2Datagrams = new ArrayList<ArtNetDatagram>();
    ledControllersDatagrams.add(pixlite2Datagrams);

    for (currentLogicalPanel = 0; currentLogicalPanel < numPanels; currentLogicalPanel++) {

      int[] dmxChannelsForUniverse = new int[pointsPerUniverse];

    for (int panelLedPos = 0; panelLedPos < pointsWidePerPanel*pointsHighPerPanel; panelLedPos++) {
      int colNumFromRight = panelLedPos / pointsHighPerPanel;
      int colNumFromLeft = maxColNumPerPanel - colNumFromRight;
      int rowNumFromBottom;
      if (colNumFromRight % 2 == 0)
        rowNumFromBottom = panelLedPos % pointsHighPerPanel;
      else
        rowNumFromBottom = pointsHighPerPanel - panelLedPos % pointsHighPerPanel - 1;


      List<Integer> panelUniverses = panelMap.get(currentLogicalPanel);
      // Which Panel-local universe are we in? 0,1,2?  Depends on our panelLedPos.  Then we also need to
      // check the Panel-Universe map in case something was wired up incorrectly and we need to
      // account for it in software.
      int universeOffset = panelLedPos / pointsPerUniverse;
      int currentPanelUniverse = panelUniverses.get(universeOffset);

      // TODO(tracy): pointsPerUniverse might be configurable in UI.

      // Chunk by 170 for each universe.
      int universeLedPos = panelLedPos % pointsPerUniverse;
      // TODO(tracy): This needs to change from panel-local coordinates to global led coordinates.
      //int pointIndex = rowNumFromBottom * pointsWidePerPanel + colNumFromLeft;
      // Convert from Panel-local coordinates to global point coordinates.  Point 1,2 in Panel 2 is 420 * 2 + 1 + 2*30 = 901
      int globalPointIndex = rowNumFromBottom * pointsWide + colNumFromLeft + currentLogicalPanel * pointsWidePerPanel;
      // logger.info(globalLedPos + " colNum:" +colNumFromLeft + " rowNum:" + rowNumFromBottom + " pointIndex: " + pointIndex);
      dmxChannelsForUniverse[universeLedPos] = globalPointIndex;
      // Either we are on DMX channel 170, or we are at the end of the panel.
      if (universeLedPos == pointsPerUniverse - 1 || panelLedPos == pointsWidePerPanel * pointsHighPerPanel - 1) {
        // Construct with our custom datagram class that has lookup table Gamma corrrection and
        // rainbow background color correction.  Also, the last chunk of LEDs on a panel do not fill up an entire universe,
        // so set the datagram size based on the last universeLedPos.
        if (universeLedPos != pointsPerUniverse -1) {
          // We came up short, we need to resize our dmxChannelsForUniverse array because RainbowDatagram and LXDatagram in general
          // just iterate through the length of the passed in dmxChannelsForUniverse, regardless of the dataLength we have constructed
          // it with.  This causes IndexOutOfBounds exceptions because the data buffer size is determined by dataLength, but the
          // buffer filling loop is determined by dmxChannelsForUniverse.length
          // I added a hack to RainbowDatagram to stop at the buffer length so we can pass in a larger int[] without blowing
          // up.
        }
        ArtNetDatagram datagram = new RainbowDatagram(lx, dmxChannelsForUniverse, (universeLedPos+1)*3, currentPanelUniverse);
        String ledControllerIp = "";
        int ledControllerPort = 0;
        List<ArtNetDatagram> whichLedControllerDatagrams = null;

        try {
          // LED_CONTROLLER_IP needs to be changed to a list, configurable in UI.  See Tenere.
          // Leave ARTNET port hardcoded.
          if (currentLogicalPanel < panelsPerLedController1) {
            // logger.info("Creating datagram for panel: " + currentLogicalPanel);
            ledControllerIp = UIPixliteConfig.pixlite1IpP.getString();
            ledControllerPort = Integer.parseInt(UIPixliteConfig.pixlite1PortP.getString());
            whichLedControllerDatagrams = ledControllersDatagrams.get(0);
          } else {
            ledControllerIp = UIPixliteConfig.pixlite2IpP.getString();
            ledControllerPort = Integer.parseInt(UIPixliteConfig.pixlite2PortP.getString());
            whichLedControllerDatagrams = ledControllersDatagrams.get(1);
          }
          datagram.setAddress(ledControllerIp).setPort(ledControllerPort);
        } catch (UnknownHostException uhex) {
          logger.log(Level.SEVERE, "Configuring ArtNet: " + ledControllerIp, uhex);
        }
        // whichLedControllerDatagrams == null should only happen if UnknownHostException is thrown.
        // Go ahead and crash because it is at startup and error message should help debug.
        if (whichLedControllerDatagrams != null) {
          whichLedControllerDatagrams.add(datagram);
        }
        dmxChannelsForUniverse = new int[pointsPerUniverse];
      }
    }
    }
    // TODO(tracy): Add 2 ArtNet sync packet datagrams at the end, one for each controller.

    // Now we have datagrams bound to all of our LEDs.  Create a LXDatagramOutput and register
    // all datagrams with it.
    try {
      LXDatagramOutput datagramOutput = new LXDatagramOutput(lx);
      for (List<ArtNetDatagram> ledControllerDatagrams : ledControllersDatagrams) {
        for (ArtNetDatagram datagram : ledControllerDatagrams) {
          datagramOutput.addDatagram(datagram);
        }
      }
      try {
        datagramOutput.addDatagram(new ArtSyncDatagram(Integer.parseInt(UIPixliteConfig.pixlite1PortP.getString())).setAddress(UIPixliteConfig.pixlite1IpP.getString()));
        datagramOutput.addDatagram(new ArtSyncDatagram(Integer.parseInt(UIPixliteConfig.pixlite2PortP.getString())).setAddress(UIPixliteConfig.pixlite2IpP.getString()));
      } catch (UnknownHostException uhex) {
        logger.log(Level.SEVERE, "ArtNet Sync", uhex);
      }
      // Finally, register our LXDatagramOutput with the engine.
      lx.engine.output.addChild(datagramOutput);
      //lx.engine.output.framesPerSecond.setValue(GLOBAL_FRAME_RATE + 20);
    } catch (SocketException sex) {
      // This can happen for example if we run out of file handles because some code is opening
      // files without closing them.
      logger.log(Level.SEVERE, "Initializing DatagramOutput", sex);
    }
  }

  // TODO(tracy): Make configureMultiPanelOutput work with one controller and one panel;
  public static void configureOutputRainbowPanel(LX lx) {
    int pointsWide = ((RainbowBaseModel)lx.model).pointsWide;
    int pointsHigh = ((RainbowBaseModel)lx.model).pointsHigh;
    int maxColNum = pointsWide - 1;

    int currentUniverse = 0;
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    int[] dmxChannelsForUniverse = new int[LEDS_PER_UNIVERSE];
    for (int globalLedPos = 0; globalLedPos < pointsWide*pointsHigh; globalLedPos++) {
      int colNumFromRight = globalLedPos / pointsHigh;
      int colNumFromLeft = maxColNum - colNumFromRight;
      int rowNumFromBottom;
      if (colNumFromRight % 2 == 0)
        rowNumFromBottom = globalLedPos % pointsHigh;
      else
        rowNumFromBottom = pointsHigh - globalLedPos % pointsHigh - 1;

      // Chunk by 170 for each universe.
      int universeLedPos = globalLedPos % LEDS_PER_UNIVERSE;
      int pointIndex = rowNumFromBottom * pointsWide + colNumFromLeft;
      // System.out.println(globalLedPos + " colNum:" +colNumFromLeft + " rowNum:" + rowNumFromBottom + " pointIndex: " + pointIndex);
      dmxChannelsForUniverse[universeLedPos] = pointIndex;
      // Either we are on DMX channel 170, or we are at the end of the panel.
      if (universeLedPos == LEDS_PER_UNIVERSE - 1 || globalLedPos == pointsWide * pointsHigh - 1) {
        // Construct with our custom datagram class that has lookup table Gamma corrrection and
        // rainbow background color correction.
        ArtNetDatagram datagram = new RainbowDatagram(lx, dmxChannelsForUniverse, (universeLedPos+1)*3, currentUniverse);
        try {
          datagram.setAddress(LED_CONTROLLER_IP).setPort(ARTNET_PORT);
        } catch (UnknownHostException uhex) {
          logger.log(Level.SEVERE, "Configuring ArtNet: " + LED_CONTROLLER_IP, uhex);
        }
        datagrams.add(datagram);
        currentUniverse++;
        dmxChannelsForUniverse = new int[LEDS_PER_UNIVERSE];
      }
    }
    // Now we have datagrams bound to all of our LEDs.  Create a LXDatagramOutput and register
    // all datagrams with it.
    try {
      LXDatagramOutput datagramOutput = new LXDatagramOutput(lx);
      for (ArtNetDatagram datagram : datagrams) {
        datagramOutput.addDatagram(datagram);
      }
      try {
        datagramOutput.addDatagram(new ArtSyncDatagram(ARTNET_PORT).setAddress(LED_CONTROLLER_IP));
      } catch (UnknownHostException uhex) {
        logger.log(Level.SEVERE, "ARTNET Sync", uhex);
      }
      // Finally, register our LXDatagramOutput with the engine.
      lx.engine.output.addChild(datagramOutput);
      // lx.engine.output.framesPerSecond.setValue(GLOBAL_FRAME_RATE);
    } catch (SocketException sex) {
      // This can happen for example if we run out of file handles because some code is opening
      // files without closing them.
      logger.log(Level.SEVERE, "Initializing DatagramOutput", sex);
    }
  }

  /**
   * Based on photo from Srikanth. 0 led on his board is bottom right. 149 led is
   * on top left.  The wiring starts from right to left and then alternates to
   * left to right moving up the rows.  The points in 3D space start with
   * 0 at the bottom left. y=1, x=0 also starts from the left side (direction does not
   * alternate like the wiring)
   * TODO(tracy): This only works for Srikanth's specific wiring.
   */
  public static void configureOutputSrikanthPanel(LX lx) {
    int pointsWide = ((RainbowBaseModel)lx.model).pointsWide;
    int pointsHigh = ((RainbowBaseModel)lx.model).pointsHigh;
    int[] ledWiring = new int[LEDS_PER_UNIVERSE];
    int currentUniverse = 0;
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();

    // For each Point, we want to figure out which LED on the physical strip
    // corresponds to the 3D point.  We are technically mapping to DMX channels
    // here, but this is simple wiring on one universe.
    for (int rowNum = 0; rowNum < pointsWide; rowNum++) {
      for (int colNum = 0; colNum < pointsHigh; colNum++) {
        // Points are stored in a 1D array.
        int pointIndex = rowNum * pointsWide + colNum;
        // NOTE(tracy): For Srikanth's home wiring diagram;
        // The end of the leds is at (rowNum-1, 0)
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
          ledPos = pointsWide * (rowNum + 1) - 1 - colNum;
        } else {
          ledPos = pointsWide * rowNum + colNum;
        }
        // ledWiring should only by chunks of 170 LEDs.  ledPos above is the global led
        // position. Here we mod by 170 to produce a universe-local led number.  When
        // we see the last LED in a universe chunk we will build a datagram and increase
        // the current universe number.  If it is the last point of the grid, we also
        // build the final datagram (after which the for loop ends).
        ledPos = ledPos % LEDS_PER_UNIVERSE;
        //System.out.println("pointIndex: " + pointIndex + " rowNum " + rowNum + " colNum " + colNum + " ledChannel: " + ledPos +  " univ: " + currentUniverse);
        ledWiring[ledPos] = pointIndex;
        // Either we are on DMX channel 170, or we are at the end of the panel.
        if (ledPos == LEDS_PER_UNIVERSE - 1 || pointIndex == pointsWide * pointsHigh - 1) {
          ArtNetDatagram datagram = new ArtNetDatagram(ledWiring, currentUniverse);
          try {
             datagram.setAddress(LED_CONTROLLER_IP).setPort(ARTNET_PORT);
          } catch (UnknownHostException uhex) {
             logger.log(Level.SEVERE, "Configuring ArtNet: " + LED_CONTROLLER_IP, uhex);
          }
          datagrams.add(datagram);
          currentUniverse++;
          ledWiring = new int[LEDS_PER_UNIVERSE];
        }
      }
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
    } catch (SocketException sex) {
      // This can happen for example if we run out of file handles because some code is opening
      // files without closing them.
      logger.log(Level.SEVERE, "Initializing DatagramOutput", sex);
    }
  }
}

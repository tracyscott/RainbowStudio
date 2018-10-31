package com.giantrainbow;

import com.giantrainbow.model.RainbowBaseModel;
import com.giantrainbow.ui.UIPixliteConfig;
import heronarts.lx.LX;
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

import static processing.core.PApplet.ceil;

/**
 * Configure ArtNet Datagram outputs.
 */
public class Output {
  private static final Logger logger = Logger.getLogger(Output.class.getName());

  public static LXDatagramOutput datagramOutput = null;
  static Map<String, List<Integer>> panelInputsMap = new HashMap<String, List<Integer>>();

  /**
   * Multi-panel output.  Panels should be remappable to account for build time
   * issues/mistakes.  There are 2 Pixlite LongRange MKII's per side.  The first
   * Pixlite drives 16 panels with 450 leds each, 7200 leds.  The second drives 12 panels with
   * 450 leds each, 5400 leds.
   * <p></p>
   * Includes support for start/end panels.  Reference:
   * https://www.notion.so/rainbowbridge/End-panel-LED-routing-and-SW-mapping-848d7fa30de744fa94bcc199ba110a2d
   * <p></p>
   * For a single panel, START_PANEL test, use modelType= RAINBOW_START_PANEL.
   * For a single panel, END_PANEL test, use modelType= RAINBOW_END_PANEL.  Also, if using the
   * @see com.giantrainbow.patterns.PanelWire pattern, enable the 'end' toggle so that it
   * doesn't default to the start panel because their is a single panel in the 3D model.
   *
   * @param lx LX Engine
   * @param startPanel Include special output configuration for a panel type Variant E.
   * @param endPanel Include special output configuration for a panel type Variant H.
   */
  public static void configureOutputMultiPanel(LX lx, boolean startPanel, boolean endPanel) {
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
    // TODO(tracy): Just pass in the underlying ParameterFile for these variables and rename ParameterFile to
    // ConfigParams.
    String pixlite1Ip = RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_IP).getString();
    int pixlite1Port = Integer.parseInt(RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PORT).getString());
    int pixlite1Panels = Integer.parseInt(RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PANELS).getString());
    String pixlite2Ip = RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_2_IP).getString();
    int pixlite2Port = Integer.parseInt(RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_2_PORT).getString());
    int pixlite2Panels = Integer.parseInt(RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_2_PANELS).getString());

    // TODO(tracy): Use numPanels here for testing with fewer panels.  Use actual values for installation.
    // 16 for first controller and 12 for second controller also happens to work for test setup with 2 panels
    // and only a single controller.
    int panelsPerLedController1 = pixlite1Panels;  // Production values
    int panelsPerLedController2 = pixlite2Panels;
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
      // Universes start at 1
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

    // Each controller gets a list of ArtNetDatagrams that it is responsible for.
    List<List<ArtNetDatagram>> ledControllersDatagrams = new ArrayList<List<ArtNetDatagram>>(numLedControllers);
    List<ArtNetDatagram> pixlite1Datagrams = new ArrayList<ArtNetDatagram>();
    ledControllersDatagrams.add(pixlite1Datagrams);
    List<ArtNetDatagram> pixlite2Datagrams = new ArrayList<ArtNetDatagram>();
    ledControllersDatagrams.add(pixlite2Datagrams);

    for (currentLogicalPanel = 0; currentLogicalPanel < numPanels; currentLogicalPanel++) {

      int[] dmxChannelsForUniverse = new int[pointsPerUniverse];

      int pointsPerThisPanel = pointsWidePerPanel * pointsHighPerPanel;
      // Start/End panels have fewer leds.  See Notion page referenced at top of method.
      // First 300 leds are standard.  The 7th string is 50 pixels with the last 2 pixels unused.
      // The 8th string is only 34 leds.

      /*
      if (startPanel && currentLogicalPanel == 0)
        pointsPerThisPanel = 300 + 50 + 34;
      if (endPanel && currentLogicalPanel == numPanels - 1)
        pointsPerThisPanel = 300 + 50 + 34;
      */

      for (int wireLedPos = 0; wireLedPos < pointsPerThisPanel; wireLedPos++) {
        int colNumFromLeft = -1;
        int colNumFromRight = -1;
        int rowNumFromBottom = -1;

        // NOTE(tracy):  This code is duplicated in patterns.PanelWire which implements a pattern
        // that traces out the wiring.  Even better, the start panel and end panel wiring cases
        // below are very similar but mirrored about X.
        // Handle start panel, panel variant E special case.
        if (startPanel && currentLogicalPanel == 0) {
          // First 300 leds (6 strands are wired normal but mirrored in X dimension, start at bottom right on front
          if (wireLedPos < 300) {
            colNumFromRight = wireLedPos / pointsHighPerPanel;
            colNumFromLeft = maxColNumPerPanel - colNumFromRight;
            if (colNumFromRight % 2 == 0)
              rowNumFromBottom = wireLedPos % pointsHighPerPanel;
            else
              rowNumFromBottom = pointsHighPerPanel - wireLedPos % pointsHighPerPanel - 1;
          } else {
            // String #7 starts on normal column, row 4, goes up and back down.  Last 2 leds are on the wire
            // but not used.  Unused leds on a wire require skipping a couple dmx channels.  Normally, that would
            // be bad because we are packing the max pixels per universe, so 2 extra pixels pushes the typical
            // universe boundaries to different locations but overall these panels have less pixels so each
            // panel will still use 3 universes.
            if (wireLedPos >= 300 && wireLedPos < 350) {
              // String #7 wiring
              int string7WirePos = wireLedPos - 300;
              int string7StartRow = 3;  // Mingjing said Row 4, 1-based.
              if (string7WirePos <= 26) {
                colNumFromLeft = 4;
                rowNumFromBottom = string7WirePos + 3;
              } else if (string7WirePos <= 47) {
                colNumFromLeft = 3;
                rowNumFromBottom = pointsHighPerPanel - (string7WirePos - 26);
              } else {  // Leds 49,50 or 0-based position 48,49 are unused LEDS
                colNumFromLeft = -1;
              }
            } else if (wireLedPos >= 350) {
              // String #8 wiring
              int string8WirePos = wireLedPos - 350;
              int string8StartRow = 14;
              int string8StartRow2 = 20;
              int string8StartRow3 = 26;
              // Start on row 15 (14 in 0 base), install 26 Leds. First column is from
              // 29-14 = 15 which is 16 leds from 14-29 inclusive 0-based.  That leaves 10 leds for
              // the next column which is 29-20 inclusive 0-based or 30-21 in 1-based.
              // This segment also includes 4 dead leds serving as a wire extension for a total of 34 leds.
              // That is 16 leds short of a typical 50-led wire.
              if (string8WirePos <= 15) {
                colNumFromLeft = 2;
                rowNumFromBottom = string8StartRow + string8WirePos;
              } else if (string8WirePos >= 16 && string8WirePos < 26) {
                colNumFromLeft = 1;
                rowNumFromBottom = (pointsHighPerPanel-1) - (string8WirePos - 16);
              } else if (string8WirePos >= 26 && string8WirePos < 30) {
                colNumFromLeft = -1; // Sentinel to mark unused leds in the wires.
              } else if (string8WirePos >= 30 && string8WirePos < 34) {
                colNumFromLeft = 0;
                rowNumFromBottom = string8WirePos - 30 + string8StartRow3;
              } else {  // At the end of the wiring.  Just make these dead leds that are skipped.
                colNumFromLeft = -1;
              }
            }
          }
          //logger.info("colFromLeft: " + colNumFromLeft + " colFromRight:" + colNumFromRight);
        } else if (endPanel && currentLogicalPanel <= numPanels - 1) {
          // Handle end panel, panel variant H special case.
          // The first 300 leds are the typical wiring.
          if (wireLedPos < 300) {
            colNumFromLeft = wireLedPos / pointsHighPerPanel;
            colNumFromRight = maxColNumPerPanel - colNumFromLeft;
            if (colNumFromLeft % 2 == 0)
              rowNumFromBottom = wireLedPos % pointsHighPerPanel;
            else
              rowNumFromBottom = pointsHighPerPanel - wireLedPos % pointsHighPerPanel - 1;
          } else {
            // String #7 starts on normal column, row 4, goes up and back down.  Last 2 leds are on the wire
            // but not used.  Unused leds on a wire require skipping a couple dmx channels.  Normally, that would
            // be bad because we are packing the max pixels per universe, so 2 extra pixels pushes the typical
            // universe boundaries to different locations but overall these panels have less pixels so each
            // panel will still use 3 universes.
            if (wireLedPos >= 300 && wireLedPos < 350) {
              //String #7 wiring
              int string7WirePos = wireLedPos - 300;
              int string7StartRow = 3;  // Mingjing said Row 4, 1-based.
              if (string7WirePos <= 26) {
                colNumFromLeft = 10;
                rowNumFromBottom = string7WirePos + 3;
              } else if (string7WirePos <= 47) {
                colNumFromLeft = 11;
                rowNumFromBottom = pointsHighPerPanel - (string7WirePos - 26);
              } else {  // Leds 49,50 or 0-based position 48,49 are unused LEDS
                colNumFromLeft = -1;
              }
            } else if (wireLedPos >= 350) {
              // String #8 wiring
              int string8WirePos = wireLedPos - 350;
              int string8StartRow = 14;
              int string8StartRow2 = 20;
              int string8StartRow3 = 26;
              // Start on row 15 (14 in 0 base), install 26 Leds. First column is from
              // 29-14 = 15 which is 16 leds from 14-29 inclusive 0-based.  That leaves 10 leds for
              // the next column which is 29-20 inclusive 0-based or 30-21 1-based.
              // This segment also includes 4 dead leds serving as a wire extension for a
              // total of 34 leds.  That is 16 short of a normal strand.
              if (string8WirePos <= 15) {
                colNumFromLeft = 12;
                rowNumFromBottom = string8StartRow + string8WirePos;
              } else if (string8WirePos >= 16 && string8WirePos < 26) {
                colNumFromLeft = 13;
                rowNumFromBottom = (pointsHighPerPanel-1) - (string8WirePos - 16);
              } else if (string8WirePos >= 26 && string8WirePos < 30) {
                colNumFromLeft = -1; // Sentinel to mark unused leds in the wires.
              } else if (string8WirePos >= 30 && string8WirePos < 34) {
                colNumFromLeft = 14;
                rowNumFromBottom = string8WirePos - 30 + string8StartRow3;
              } else {  // At the end of the wiring.  Just make these dead leds that are skipped.
                colNumFromLeft = -1;
              }
            }
          }
          // logger.info("colFromLeft: " + colNumFromLeft + " colFromRight:" + colNumFromRight);
        } else {
          // Standard Panels
          colNumFromLeft = wireLedPos / pointsHighPerPanel;
          colNumFromRight = maxColNumPerPanel - colNumFromLeft;
          //logger.info("colFromLeft: " + colNumFromLeft + " colFromRight:" + colNumFromRight);

          if (colNumFromRight % 2 == 0)
            rowNumFromBottom = wireLedPos % pointsHighPerPanel;
          else
            rowNumFromBottom = pointsHighPerPanel - wireLedPos % pointsHighPerPanel - 1;
        }

        // TODO(tracy): Modify this for expanded mode.  Rather than looking up the start universe
        // based on the panel number, we need to look it up based on panel_num.panel_part.
        // i.e. lookup 21.1, 21.2, 22.1, 22.2, etc...
        List<Integer> panelUniverses = panelMap.get(currentLogicalPanel);
        // Which Panel-local universe are we in? 0,1,2?  Depends on our wireLedPos.  Then we also need to
        // check the Panel-Universe map in case something was wired up incorrectly and we need to
        // account for it in software.
        // TODO(tracy): Modify this for expanded mode.  If wireLedPos >= 250, then
        // universeOffset = (wireLedPos - 250) / pointsPerUniverse
        int universeOffset = wireLedPos / pointsPerUniverse;
        int currentPanelUniverse = panelUniverses.get(universeOffset);

        // Chunk by 170 for each universe.
        // TODO(tracy): For expanded mode this becomes much more complicated.
        int universeLedPos = wireLedPos % pointsPerUniverse;
        // Convert from Panel-local wire position coordinates to global point coordinates.
        // Point 1,2 in Panel 2 is 420 * 2 + 1 + 2*30 = 901
        int globalPointIndex = 0;
        // For the start/end panel special cases above, we have some leds in the strand that should not be mapped to
        // actual rainbow grid coords.  For those cases, we set colNumFromLeft=-1 to act as a sentinel value to denote
        // an unused led in the wiring.  For those leds, setting the pointIndex to -1 will cause RainbowDatagram to
        // assign the led a black value.  That black value is still pushed out of the pixlite because it is inline
        // with the rest of the leds.
        if (colNumFromLeft != -1)
          globalPointIndex = rowNumFromBottom * pointsWide + colNumFromLeft + currentLogicalPanel * pointsWidePerPanel;
        else
          globalPointIndex = -1;
        // logger.info(wireLedPos + " colNum:" +colNumFromLeft + " rowNum:" + rowNumFromBottom + " pointIndex: " + globalPointIndex);
        dmxChannelsForUniverse[universeLedPos] = globalPointIndex;
        // Either we are on DMX channel 170, or we are at the end of the panel.
        // TODO(tracy): Currently, if we are at 170 leds, or we are at 450 leds, it is time to configure an ArtNet
        // datagram packet and increment the universe number.  For expanded mode, we also need to increment the universe
        // number at 250 leds.  There are some additional complexities relating to wireLedPos and universes.  Since
        // technically, there are now two wiredLedPos=0 values per panel.  It might be better to just continue with
        // the 450 assumption and then accounting for that when computing the appropriate universe number.
        if (universeLedPos == pointsPerUniverse - 1 || wireLedPos == pointsWidePerPanel * pointsHighPerPanel - 1) {
          // Construct with our custom datagram class that has lookup table Gamma correction and
          // rainbow background color correction.  Also, the last chunk of LEDs on a panel do not fill up an entire universe,
          // so set the datagram size based on the last universeLedPos.

          //if (universeLedPos != pointsPerUniverse -1) {
            // NOTE(tracy): Leaving this comment in here even though this if condition does nothing.  It would need to
            // be addressed if RainbowDatagram was not being used (i.e. some other non-artnet output).
            // We came up short, we need to resize our dmxChannelsForUniverse array because RainbowDatagram and LXDatagram in general
            // just iterate through the length of the passed in dmxChannelsForUniverse, regardless of the dataLength we have constructed
            // it with.  This causes IndexOutOfBounds exceptions because the data buffer size is determined by dataLength, but the
            // buffer filling loop is determined by dmxChannelsForUniverse.length
            // NOTE: I added a safety check to RainbowDatagram to stop at the buffer length so we can pass in a larger int[] than
            // our corresponding dataLength without blowing up.  Otherwise we need to compute the size of dmxChannelsForUniverse
            // appropriately for which chunk we are working on AND whether or not it is a start/end panel.
          //}
          ArtNetDatagram datagram = new RainbowDatagram(lx, dmxChannelsForUniverse, (universeLedPos+1)*3, currentPanelUniverse);
          String ledControllerIp = "";
          int ledControllerPort = 0;
          List<ArtNetDatagram> whichLedControllerDatagrams = null;

          try {
            if (currentLogicalPanel < panelsPerLedController1) {
              // logger.info("Creating datagram for panel: " + currentLogicalPanel);
              ledControllerIp = pixlite1Ip;
              ledControllerPort = pixlite1Port;
              whichLedControllerDatagrams = ledControllersDatagrams.get(0);
            } else {
              ledControllerIp = pixlite2Ip;
              ledControllerPort = pixlite2Port;
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

    // Now we have datagrams bound to all of our LEDs.  Create a LXDatagramOutput and register
    // all datagrams with it.
    try {
      datagramOutput = new LXDatagramOutput(lx);
      for (List<ArtNetDatagram> ledControllerDatagrams : ledControllersDatagrams) {
        for (ArtNetDatagram datagram : ledControllerDatagrams) {
          datagramOutput.addDatagram(datagram);
        }
      }
      try {
        datagramOutput.addDatagram(new ArtSyncDatagram(pixlite1Port).setAddress(pixlite1Ip));
        datagramOutput.addDatagram(new ArtSyncDatagram(pixlite2Port).setAddress(pixlite2Ip));
      } catch (UnknownHostException uhex) {
        logger.log(Level.SEVERE, "ArtNet Sync", uhex);
      }
      // Finally, register our LXDatagramOutput with the engine.
      lx.engine.output.addChild(datagramOutput);
      // NOTE(tracy): This is for testing sending packets at a different frame rate than the UI.  Not advisable,
      // but useful for RainbowReceiver to verify correct ArtNet packet sending and sync'ing.
      //lx.engine.output.framesPerSecond.setValue(GLOBAL_FRAME_RATE + 20);
    } catch (SocketException sex) {
      // This can happen for example if we run out of file handles because some code is opening
      // files without closing them.
      logger.log(Level.SEVERE, "Initializing DatagramOutput", sex);
    }
  }

  /**
   * Multi-panel output for Expanded Mode.  Panels should be remappable to account for build time
   * issues/mistakes.  There are 2 Pixlite LongRange MKII's per side.  One
   * Pixlite drives 16 panels with 450 leds each, 7200 leds.  The other drives 12 panels with
   * 450 leds each, 5400 leds.  Depending on the side of the rainbow, the first controller might
   * either control 16 panels or 12 panels.
   * <p></p>
   * Supports expanded mode wiring where each Panel has 2 inputs.  One input is driving 250 leds and
   * another input is driving 200 leds.
   * <p></p>
   * Includes support for start/end panels.  Reference:
   * https://www.notion.so/rainbowbridge/End-panel-LED-routing-and-SW-mapping-848d7fa30de744fa94bcc199ba110a2d
   * <p></p>
   * For a single panel, START_PANEL test, use modelType= RAINBOW_START_PANEL.
   * For a single panel, END_PANEL test, use modelType= RAINBOW_END_PANEL.  Also, if using the
   * @see com.giantrainbow.patterns.PanelWire pattern, enable the 'end' toggle so that it
   * doesn't default to the start panel because their is a single panel in the 3D model.
   *
   * @param lx LX Engine
   * @param startPanel Include special output configuration for a panel type Variant E.
   * @param endPanel Include special output configuration for a panel type Variant H.
   */
  public static void configureOutputMultiPanelExpanded(LX lx, boolean startPanel, boolean endPanel) {
    // Config for panel size, number of panels, number of universes per panel, number of led controllers
    // and number of panels per controller.
    int pointsWidePerPanel = 15;
    int pointsHighPerPanel = 30;
    int pointsPerPanel = pointsWidePerPanel * pointsHighPerPanel;
    int pointsPerUniverse = 170;
    int pointsPerInput = 250;  // First input manages 250 leds, second input manages 200 leds.
    int pointsWide = ((RainbowBaseModel)lx.model).pointsWide;
    int pointsHigh = ((RainbowBaseModel)lx.model).pointsHigh;
    int numPanels = pointsWide / pointsWidePerPanel;
    int numLedControllers = 2;
    // TODO(tracy): Just pass in the underlying ParameterFile for these variables and rename ParameterFile to
    // ConfigParams.
    String pixlite1Ip = RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_IP).getString();
    int pixlite1Port = Integer.parseInt(RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PORT).getString());
    int pixlite1Panels = Integer.parseInt(RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PANELS).getString());
    String pixlite2Ip = RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_2_IP).getString();
    int pixlite2Port = Integer.parseInt(RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_2_PORT).getString());
    int pixlite2Panels = Integer.parseInt(RainbowStudio.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_2_PANELS).getString());

    // TODO(tracy): Use numPanels here for testing with fewer panels.  Use actual values for installation.
    // 16 for first controller and 12 for second controller also happens to work for test setup with 2 panels
    // and only a single controller.
    int panelsPerLedController1 = pixlite1Panels;  // Production values
    int panelsPerLedController2 = pixlite2Panels;
    int universesPerPanel = ceil((float)pointsPerPanel/(float)pointsPerUniverse);

    // A panel now has 2 inputs.  The first input maps 250 leds and the second input maps 200 leds.
    universesPerPanel = 4;

    int maxColNumPerPanel = pointsWidePerPanel - 1;
    int currentLogicalPanel;
    logger.info("numPanels= " + numPanels);
    logger.info("controller 1 panels: " + panelsPerLedController1);
    logger.info("controller 2 panels: " + panelsPerLedController2);


    // First, just build the map in a straightforward way without remapping.
    int universesPerInput = 2;
    for (int i = 0; i < numPanels; i++) {
      // Universes start at 1
      for (int j = 0; j < universesPerPanel/universesPerInput; j++) {
        int universeNum = i * universesPerPanel + j;
        // Reset universe numbers for second pixlite
        for (int panelInputNum = 0; panelInputNum < 2; panelInputNum++) {
          List<Integer> universesThisPanelInput = new ArrayList<Integer>();
          if (i >= panelsPerLedController1) {
            universeNum = (i - panelsPerLedController1) * universesPerPanel + j*universesPerInput + panelInputNum;
          }
          // Map 2 universes to a given Panel.PanelInput key.
          universesThisPanelInput.add(universeNum);
          panelInputsMap.put(""+ i + "" + panelInputNum, universesThisPanelInput);
        }
      }
    }

    // Modify the mapping here
    // panelInputsMap.put("0.0", (15, 16, 17));
    // panelInputsMap.put("5.1", (0, 1, 2));


    // This iterates through all our points.  We also need to track our current logical
    // panel (which should just be globalLedPos / pointsPerPanel;
    // We need to iterate through panels and build datagrams that way.

    // Each controller gets a list of ArtNetDatagrams that it is responsible for.
    List<List<ArtNetDatagram>> ledControllersDatagrams = new ArrayList<List<ArtNetDatagram>>(numLedControllers);
    List<ArtNetDatagram> pixlite1Datagrams = new ArrayList<ArtNetDatagram>();
    ledControllersDatagrams.add(pixlite1Datagrams);
    List<ArtNetDatagram> pixlite2Datagrams = new ArrayList<ArtNetDatagram>();
    ledControllersDatagrams.add(pixlite2Datagrams);

    for (currentLogicalPanel = 0; currentLogicalPanel < numPanels; currentLogicalPanel++) {

      int[] dmxChannelsForUniverse = new int[pointsPerUniverse];

      int pointsPerThisPanel = pointsWidePerPanel * pointsHighPerPanel;


      // Start/End panels have fewer leds.  See Notion page referenced at top of method.
      // First 300 leds are standard.  The 7th string is 50 pixels with the last 2 pixels unused.
      // The 8th string is only 34 leds.
      /*
      if (startPanel && currentLogicalPanel == 0)
        pointsPerThisPanel = 300 + 50 + 34;
      if (endPanel && currentLogicalPanel == numPanels - 1)
        pointsPerThisPanel = 300 + 50 + 34;
      */

      for (int wireLedPos = 0; wireLedPos < pointsPerThisPanel; wireLedPos++) {
        int colNumFromLeft = -1;
        int colNumFromRight = -1;
        int rowNumFromBottom = -1;

        // NOTE(tracy):  This code is duplicated in patterns.PanelWire which implements a pattern
        // that traces out the wiring.  Even better, the start panel and end panel wiring cases
        // below are very similar but mirrored about X.
        // Handle start panel, panel variant E special case.
        if (startPanel && currentLogicalPanel == 0) {
          // First 300 leds (6 strands are wired normal but mirrored in X dimension, start at bottom right on front
          if (wireLedPos < 300) {
            colNumFromRight = wireLedPos / pointsHighPerPanel;
            colNumFromLeft = maxColNumPerPanel - colNumFromRight;
            if (colNumFromRight % 2 == 0)
              rowNumFromBottom = wireLedPos % pointsHighPerPanel;
            else
              rowNumFromBottom = pointsHighPerPanel - wireLedPos % pointsHighPerPanel - 1;
          } else {
            // String #7 starts on normal column, row 4, goes up and back down.  Last 2 leds are on the wire
            // but not used.  Unused leds on a wire require skipping a couple dmx channels.  Normally, that would
            // be bad because we are packing the max pixels per universe, so 2 extra pixels pushes the typical
            // universe boundaries to different locations but overall these panels have less pixels so each
            // panel will still use 3 universes.
            if (wireLedPos >= 300 && wireLedPos < 350) {
              // String #7 wiring
              int string7WirePos = wireLedPos - 300;
              int string7StartRow = 3;  // Mingjing said Row 4, 1-based.
              if (string7WirePos <= 26) {
                colNumFromLeft = 4;
                rowNumFromBottom = string7WirePos + 3;
              } else if (string7WirePos <= 47) {
                colNumFromLeft = 3;
                rowNumFromBottom = pointsHighPerPanel - (string7WirePos - 26);
              } else {  // Leds 49,50 or 0-based position 48,49 are unused LEDS
                colNumFromLeft = -1;
              }
            } else if (wireLedPos >= 350) {
              // String #8 wiring
              int string8WirePos = wireLedPos - 350;
              int string8StartRow = 14;
              int string8StartRow2 = 20;
              int string8StartRow3 = 26;
              // Start on row 15 (14 in 0 base), install 26 Leds. First column is from
              // 29-14 = 15 which is 16 leds from 14-29 inclusive 0-based.  That leaves 10 leds for
              // the next column which is 29-20 inclusive 0-based or 30-21 in 1-based.
              // This segment also includes 4 dead leds serving as a wire extension for a total of 34 leds.
              // That is 16 leds short of a typical 50-led wire.
              if (string8WirePos <= 15) {
                colNumFromLeft = 2;
                rowNumFromBottom = string8StartRow + string8WirePos;
              } else if (string8WirePos >= 16 && string8WirePos < 26) {
                colNumFromLeft = 1;
                rowNumFromBottom = (pointsHighPerPanel-1) - (string8WirePos - 16);
              } else if (string8WirePos >= 26 && string8WirePos < 30) {
                colNumFromLeft = -1; // Sentinel to mark unused leds in the wires.
              } else if (string8WirePos >= 30 && string8WirePos < 34) {
                colNumFromLeft = 0;
                rowNumFromBottom = string8WirePos - 30 + string8StartRow3;
              } else {  // At the end of the wiring.  Just make these dead leds that are skipped.
                colNumFromLeft = -1;
              }
            }
          }
          //logger.info("colFromLeft: " + colNumFromLeft + " colFromRight:" + colNumFromRight);
        } else if (endPanel && currentLogicalPanel <= numPanels - 1) {
          // Handle end panel, panel variant H special case.
          // The first 300 leds are the typical wiring.
          if (wireLedPos < 300) {
            colNumFromLeft = wireLedPos / pointsHighPerPanel;
            colNumFromRight = maxColNumPerPanel - colNumFromLeft;
            if (colNumFromLeft % 2 == 0)
              rowNumFromBottom = wireLedPos % pointsHighPerPanel;
            else
              rowNumFromBottom = pointsHighPerPanel - wireLedPos % pointsHighPerPanel - 1;
          } else {
            // String #7 starts on normal column, row 4, goes up and back down.  Last 2 leds are on the wire
            // but not used.  Unused leds on a wire require skipping a couple dmx channels.  Normally, that would
            // be bad because we are packing the max pixels per universe, so 2 extra pixels pushes the typical
            // universe boundaries to different locations but overall these panels have less pixels so each
            // panel will still use 3 universes.
            if (wireLedPos >= 300 && wireLedPos < 350) {
              //String #7 wiring
              int string7WirePos = wireLedPos - 300;
              int string7StartRow = 3;  // Mingjing said Row 4, 1-based.
              if (string7WirePos <= 26) {
                colNumFromLeft = 10;
                rowNumFromBottom = string7WirePos + 3;
              } else if (string7WirePos <= 47) {
                colNumFromLeft = 11;
                rowNumFromBottom = pointsHighPerPanel - (string7WirePos - 26);
              } else {  // Leds 49,50 or 0-based position 48,49 are unused LEDS
                colNumFromLeft = -1;
              }
            } else if (wireLedPos >= 350) {
              // String #8 wiring
              int string8WirePos = wireLedPos - 350;
              int string8StartRow = 14;
              int string8StartRow2 = 20;
              int string8StartRow3 = 26;
              // Start on row 15 (14 in 0 base), install 26 Leds. First column is from
              // 29-14 = 15 which is 16 leds from 14-29 inclusive 0-based.  That leaves 10 leds for
              // the next column which is 29-20 inclusive 0-based or 30-21 1-based.
              // This segment also includes 4 dead leds serving as a wire extension for a
              // total of 34 leds.  That is 16 short of a normal strand.
              if (string8WirePos <= 15) {
                colNumFromLeft = 12;
                rowNumFromBottom = string8StartRow + string8WirePos;
              } else if (string8WirePos >= 16 && string8WirePos < 26) {
                colNumFromLeft = 13;
                rowNumFromBottom = (pointsHighPerPanel-1) - (string8WirePos - 16);
              } else if (string8WirePos >= 26 && string8WirePos < 30) {
                colNumFromLeft = -1; // Sentinel to mark unused leds in the wires.
              } else if (string8WirePos >= 30 && string8WirePos < 34) {
                colNumFromLeft = 14;
                rowNumFromBottom = string8WirePos - 30 + string8StartRow3;
              } else {  // At the end of the wiring.  Just make these dead leds that are skipped.
                colNumFromLeft = -1;
              }
            }
          }
          // logger.info("colFromLeft: " + colNumFromLeft + " colFromRight:" + colNumFromRight);
        } else {
          // Standard Panels
          colNumFromLeft = wireLedPos / pointsHighPerPanel;
          colNumFromRight = maxColNumPerPanel - colNumFromLeft;
          //logger.info("colFromLeft: " + colNumFromLeft + " colFromRight:" + colNumFromRight);

          if (colNumFromRight % 2 == 0)
            rowNumFromBottom = wireLedPos % pointsHighPerPanel;
          else
            rowNumFromBottom = pointsHighPerPanel - wireLedPos % pointsHighPerPanel - 1;
        }

        // TODO(tracy): Modify this for expanded mode.  Rather than looking up the start universe
        // based on the panel number, we need to look it up based on panel_num.panel_part.
        // i.e. lookup 21.0, 21.1, 22.0, 22.1, etc...
        int currentPanelInput = wireLedPos / 250;
        List<Integer> panelInputUniverses = panelInputsMap.get("" + currentLogicalPanel + "." + currentPanelInput);
        // Which Panel-local universe are we in? 0,1,2?  Depends on our wireLedPos.  Then we also need to
        // check the Panel-Universe map in case something was wired up incorrectly and we need to
        // account for it in software.
        // TODO(tracy): Modify this for expanded mode.  If wireLedPos >= 250, then
        // universeOffset = (wireLedPos - 250) / pointsPerUniverse
        int universeOffset;
        if (wireLedPos < pointsPerInput)
          universeOffset = wireLedPos / pointsPerUniverse;
        else
          universeOffset = (wireLedPos - pointsPerInput) / pointsPerUniverse;

        int currentPanelInputUniverse = panelInputUniverses.get(universeOffset);

        // Chunk by 170 for each universe.
        // TODO(tracy): For expanded mode this becomes much more complicated.
        int universeLedPos;
        if (wireLedPos < pointsPerInput)
          universeLedPos = wireLedPos % pointsPerUniverse;
        else
          universeLedPos = (wireLedPos - pointsPerInput) % pointsPerUniverse;

        // Convert from Panel-local wire position coordinates to global point coordinates.
        // Point 1,2 in Panel 2 is 420 * 2 + 1 + 2*30 = 901
        int globalPointIndex = 0;
        // For the start/end panel special cases above, we have some leds in the strand that should not be mapped to
        // actual rainbow grid coords.  For those cases, we set colNumFromLeft=-1 to act as a sentinel value to denote
        // an unused led in the wiring.  For those leds, setting the pointIndex to -1 will cause RainbowDatagram to
        // assign the led a black value.  That black value is still pushed out of the pixlite because it is inline
        // with the rest of the leds.
        if (colNumFromLeft != -1)
          globalPointIndex = rowNumFromBottom * pointsWide + colNumFromLeft + currentLogicalPanel * pointsWidePerPanel;
        else
          globalPointIndex = -1;
        // logger.info(wireLedPos + " colNum:" +colNumFromLeft + " rowNum:" + rowNumFromBottom + " pointIndex: " + globalPointIndex);
        dmxChannelsForUniverse[universeLedPos] = globalPointIndex;
        // Either we are on DMX channel 170, or we are at the end of the panel.
        // TODO(tracy): Currently, if we are at 170 leds, or we are at 450 leds, it is time to configure an ArtNet
        // datagram packet and increment the universe number.  For expanded mode, we also need to increment the universe
        // number at 250 leds.  There are some additional complexities relating to wireLedPos and universes.  Since
        // technically, there are now two wiredLedPos=0 values per panel.  It might be better to just continue with
        // the 450 assumption and then accounting for that when computing the appropriate universe number.
        if (universeLedPos == pointsPerUniverse - 1 || wireLedPos == pointsWidePerPanel * pointsHighPerPanel - 1) {
          // Construct with our custom datagram class that has lookup table Gamma correction and
          // rainbow background color correction.  Also, the last chunk of LEDs on a panel do not fill up an entire universe,
          // so set the datagram size based on the last universeLedPos.

          //if (universeLedPos != pointsPerUniverse -1) {
          // NOTE(tracy): Leaving this comment in here even though this if condition does nothing.  It would need to
          // be addressed if RainbowDatagram was not being used (i.e. some other non-artnet output).
          // We came up short, we need to resize our dmxChannelsForUniverse array because RainbowDatagram and LXDatagram in general
          // just iterate through the length of the passed in dmxChannelsForUniverse, regardless of the dataLength we have constructed
          // it with.  This causes IndexOutOfBounds exceptions because the data buffer size is determined by dataLength, but the
          // buffer filling loop is determined by dmxChannelsForUniverse.length
          // NOTE: I added a safety check to RainbowDatagram to stop at the buffer length so we can pass in a larger int[] than
          // our corresponding dataLength without blowing up.  Otherwise we need to compute the size of dmxChannelsForUniverse
          // appropriately for which chunk we are working on AND whether or not it is a start/end panel.
          //}
          ArtNetDatagram datagram = new RainbowDatagram(lx, dmxChannelsForUniverse, (universeLedPos+1)*3, currentPanelUniverse);
          String ledControllerIp = "";
          int ledControllerPort = 0;
          List<ArtNetDatagram> whichLedControllerDatagrams = null;

          try {
            if (currentLogicalPanel < panelsPerLedController1) {
              // logger.info("Creating datagram for panel: " + currentLogicalPanel);
              ledControllerIp = pixlite1Ip;
              ledControllerPort = pixlite1Port;
              whichLedControllerDatagrams = ledControllersDatagrams.get(0);
            } else {
              ledControllerIp = pixlite2Ip;
              ledControllerPort = pixlite2Port;
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

    // Now we have datagrams bound to all of our LEDs.  Create a LXDatagramOutput and register
    // all datagrams with it.
    try {
      datagramOutput = new LXDatagramOutput(lx);
      for (List<ArtNetDatagram> ledControllerDatagrams : ledControllersDatagrams) {
        for (ArtNetDatagram datagram : ledControllerDatagrams) {
          datagramOutput.addDatagram(datagram);
        }
      }
      try {
        datagramOutput.addDatagram(new ArtSyncDatagram(pixlite1Port).setAddress(pixlite1Ip));
        datagramOutput.addDatagram(new ArtSyncDatagram(pixlite2Port).setAddress(pixlite2Ip));
      } catch (UnknownHostException uhex) {
        logger.log(Level.SEVERE, "ArtNet Sync", uhex);
      }
      // Finally, register our LXDatagramOutput with the engine.
      lx.engine.output.addChild(datagramOutput);
      // NOTE(tracy): This is for testing sending packets at a different frame rate than the UI.  Not advisable,
      // but useful for RainbowReceiver to verify correct ArtNet packet sending and sync'ing.
      //lx.engine.output.framesPerSecond.setValue(GLOBAL_FRAME_RATE + 20);
    } catch (SocketException sex) {
      // This can happen for example if we run out of file handles because some code is opening
      // files without closing them.
      logger.log(Level.SEVERE, "Initializing DatagramOutput", sex);
    }
  }

}

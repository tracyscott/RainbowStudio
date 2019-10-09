package com.giantrainbow;

import com.giantrainbow.model.RainbowBaseModel;
import com.giantrainbow.ui.UIPanelConfig;
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

import static com.giantrainbow.RainbowStudio.INTERACTIVE_SIDE;
import static processing.core.PApplet.ceil;

/**
 * Configure ArtNet Datagram outputs.
 */
public class Output {
  private static final Logger logger = Logger.getLogger(Output.class.getName());

  public static LXDatagramOutput datagramOutput = null;
  static Map<String, List<Integer>> panelInputsMap = new HashMap<String, List<Integer>>();

  /**
   * Takes a panel number and assigns a new universe mapping.  Each panel is currently three universes.
   * This will overwrite the appropriate entries in the default panelMap constructed assuming correct
   * wiring.
   * (TODO): Fix this for expanded mode.  Each output will be either 200 or 250 pixels, so always 2
   * universes.  We also need to account for the possibility that the two outputs in expanded mode will
   * be wired incorrectly to a single panel.  So we need the concept of panel "parts".  maybe part A/part B?
   * I suppose it is possible that half of panel 3 will show up on the other half of panel 12 so that will
   * need to be accounted for.  There needs to be a mapping from panel-part to universe.  We process the
   * LXPoints in a "logical panel" order, so for each panel, we also need to track the current panel part #
   * and lookup the remapping accordingly.
   *
   * @param panelMap
   * @param panel
   * @param mapToOutput
   */
  public static void remapPanel(Map<Integer, List<Integer>> panelMap, int panel, int mapToOutput) {
    System.out.println("mapping panel: " + panel + " to " + mapToOutput);
    List<Integer> universes = new ArrayList<Integer>();
    universes.add(mapToOutput * 3);
    universes.add(mapToOutput * 3 + 1);
    universes.add(mapToOutput * 3 + 2);
    panelMap.put(panel, universes);
  }

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
    //if (!RainbowStudio.INTERACTIVE_SIDE) {
        panelsPerLedController1 = 12;
        panelsPerLedController2 = 16;
    //}
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

    /*
    if (INTERACTIVE_SIDE) {
        remapPanel(panelMap, 1, 14);
        remapPanel(panelMap, 15, 1);
        remapPanel(panelMap, 14, 0);
        remapPanel(panelMap, 13, 3);
        remapPanel(panelMap, 12, 2);
        remapPanel(panelMap, 11, 5);
        remapPanel(panelMap, 10, 4);
        remapPanel(panelMap, 9, 7);
        remapPanel(panelMap, 8, 6);
        remapPanel(panelMap, 7, 9);
        remapPanel(panelMap, 6, 8);
        remapPanel(panelMap, 5, 10);
        remapPanel(panelMap, 4, 11);
        remapPanel(panelMap, 3, 12);
        remapPanel(panelMap, 2, 13);
        remapPanel(panelMap, 1, 14);
        remapPanel(panelMap, 0, 15);


        remapPanel(panelMap, 16, 9); // 25
        remapPanel(panelMap, 17, 10); // 26
        remapPanel(panelMap, 18, 8); // 24
        remapPanel(panelMap, 19, 11);  // 27
        remapPanel(panelMap, 20, 0); // 16
        remapPanel(panelMap, 21, 3); // 19
        remapPanel(panelMap, 22, 1); // 17
        remapPanel(panelMap, 23, 2);// 18
        remapPanel(panelMap, 24, 6); // 22
        remapPanel(panelMap, 25, 4); // 20 is at
        remapPanel(panelMap, 26, 5); // 21
        remapPanel(panelMap, 27, 7);
    }
    */

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
      int skippedLeds = 0;
      for (int wireLedPos = 0; wireLedPos < pointsPerThisPanel; wireLedPos++) {
        boolean skippedThisLed = false;
        int colNumFromLeft = -1;
        int colNumFromRight = -1;
        int rowNumFromBottom = -1;

        // NOTE(tracy):  This code is duplicated in patterns.PanelWire which implements a pattern
        // that traces out the wiring.  Even better, the start panel and end panel wiring cases
        // below are very similar but mirrored about X.
        // Handle start panel, panel variant E special case.
        if (startPanel && currentLogicalPanel == 0) {
            //logger.info("Start panel");
          // NOTE(tracy): On playa change.  4 leds are removed from each end panel.  Removal of an LED shifts
          // the universe position.  A pixel that exists in code does not have a dmx universe address.  For a
          // removed pixel, we should not increment the universe number.  We also shouldn't attempt to map the
          // pixel to an LXDatagram.
          if (wireLedPos == 1 || wireLedPos == 2 || wireLedPos == 12 || wireLedPos == 13
                    || wireLedPos == 46 || wireLedPos == 47 || wireLedPos == 57 || wireLedPos == 58) {
              skippedLeds++;
              skippedThisLed = true;
              logger.info("Skipped led: " + wireLedPos);
            }
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
        } else if (endPanel && currentLogicalPanel == numPanels - 1) {
          // Handle end panel, panel variant H special case.
          // The first 300 leds are the typical wiring.
            //logger.info("End panel");
          if (wireLedPos < 300) {
            // NOTE(tracy): On playa change.  4 leds are removed from each end panel.  Removal of an LED shifts
            // the universe position.  A pixel that exists in code does not have a dmx universe address.  For a
            // removed pixel, we should not increment the universe number.  We also shouldn't attempt to map the
            // pixel to an LXDatagram.
            if (wireLedPos == 1 || wireLedPos == 2 || wireLedPos == 12 || wireLedPos == 13
                  || wireLedPos == 46 || wireLedPos == 47 || wireLedPos == 57 || wireLedPos == 58) {
              skippedLeds++;
              skippedThisLed = true;
              // System.out.println("Skipped led: " + wireLedPos);
            }
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
       // Track number of skipped pixels and subtract that number from universeOffset.
        // NOTE(tracy): Technically, we need to reset the skippedLeds counter each time we increment
        // the universe number but the end panels are the only panels with skippedLeds and this
        // works since all the skipped leds are always in the same universe number.
        // TODO(tracy): Make this generally work.
        int universeOffset = (wireLedPos - skippedLeds) / pointsPerUniverse;
        int currentPanelUniverse = panelUniverses.get(universeOffset);

        // Chunk by 170 for each universe.
        int universeLedPos = (wireLedPos - skippedLeds) % pointsPerUniverse;
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
        if (currentLogicalPanel == 0) {
          //logger.info(wireLedPos + " colNum:" + colNumFromLeft + " rowNum:" + rowNumFromBottom + " pointIndex: " + globalPointIndex + " upos: " + universeLedPos);
          //logger.info("skippedLeds: " + skippedLeds);
        }
        if (!skippedThisLed) dmxChannelsForUniverse[universeLedPos] = globalPointIndex;
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
          logger.info("Panel: " + currentLogicalPanel + " Universe: " + currentPanelUniverse);
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
  public static void configureOutputMultiPanelExpanded(LX lx, boolean startPanel, boolean endPanel, UIPanelConfig panel16Config,
                                                       UIPanelConfig panel12Config) {
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

    /* UIPanelConfig's specify panel#.input# to universe mappings.  The mappings can be overridden in the UI.
     * Currently, the front/back of the rainbow consists of 2 Pixlites.  One pixlite controls 16 panels, the
     * other pixlite controls 12 panels.  NOTE: Due to the physical wiring topology, on one side the first
     * pixlite manages 16 panels and the second pixlite manages 12 panels while on the opposite side the first
     * pixlite manages 12 panels and the second pixlite manages 16 panels.  Here we check the number of panels
     * specified in the UI for pixlite1 and then assign the appropriate UIPanelConfigs to their respective
     * pixlite1 or pixlite2.
     */
    UIPanelConfig pixlite1PanelConfig;
    UIPanelConfig pixlite2PanelConfig;
    if (pixlite1Panels == 16) {
      pixlite1PanelConfig = panel16Config;
      pixlite2PanelConfig = panel12Config;
    } else {
      pixlite1PanelConfig = panel12Config;
      pixlite2PanelConfig = panel16Config;
    }

    // A panel now has 2 inputs.  The first input maps 250 leds and the second input maps 200 leds.
    int universesPerPanel = 4;

    int maxColNumPerPanel = pointsWidePerPanel - 1;
    int currentLogicalPanel;
    logger.info("numPanels= " + numPanels);
    logger.info("controller 1 panels: " + panelsPerLedController1);
    logger.info("controller 2 panels: " + panelsPerLedController2);


    // First, just build the map in a straightforward way without remapping.
    int universesPerInput = 2;

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

      int skippedLeds = 0;
      for (int wireLedPos = 0; wireLedPos < pointsPerThisPanel; wireLedPos++) {
        boolean skippedThisLed = false;
        int colNumFromLeft = -1;
        int colNumFromRight = -1;
        int rowNumFromBottom = -1;

        // NOTE(tracy):  This code is duplicated in patterns.PanelWire which implements a pattern
        // that traces out the wiring.  Even better, the start panel and end panel wiring cases
        // below are very similar but mirrored about X.
        // Handle start panel, panel variant E special case.
        if (startPanel && currentLogicalPanel == 0) {
          // NOTE(tracy): On playa change.  4 leds are removed from each end panel.  Removal of an LED shifts
          // the universe position.  A pixel that exists in code does not have a dmx universe address.  For a
          // removed pixel, we should not increment the universe number.  We also shouldn't attempt to map the
          // pixel to an LXDatagram.
          if (wireLedPos == 1 || wireLedPos == 2 || wireLedPos == 12 || wireLedPos == 13
              || wireLedPos == 46 || wireLedPos == 47 || wireLedPos == 57 || wireLedPos == 58) {
            skippedLeds++;
            skippedThisLed = true;
            logger.info("Skipped led: " + wireLedPos);
          }
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
        } else if (endPanel && currentLogicalPanel == numPanels - 1) {
          // Handle end panel, panel variant H special case.
          // The first 300 leds are the typical wiring.
          if (wireLedPos < 300) {
            if (wireLedPos == 1 || wireLedPos == 2 || wireLedPos == 12 || wireLedPos == 13
                || wireLedPos == 46 || wireLedPos == 47 || wireLedPos == 57 || wireLedPos == 58) {
              skippedLeds++;
              skippedThisLed = true;
              logger.info("Skipped led: " + wireLedPos);
            }

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

        // Universe remapping for expanded mode.  Rather than looking up the start universe
        // based on the panel number, we need to look it up based on panel_num.panel_part.
        // i.e. lookup 11.0, 11.1, 12.0, 12.1, etc...
        // A UIPanelConfig object has either 16 or 12 panels.  The panel values either
        // range from 0-15 or 0-11.  'pixliteLocalPanelNum' is relative to the UIPanelConfig, it
        // is not the global currentLogicalPanel (which can be 25 for example).  Since sometimes
        // the 16 panel UIPanelConfig will be the first pixlite or sometimes it will be the second
        // pixlite (depending on the wiring on that side of the bridge) everything just uses
        // pixlite-local panel numbering and we swap configs below based on a comparison of the global
        // currentLogicalPanel number and the number of panels managed by the first pixlite.
        int currentPanelInput = wireLedPos / 250;
        int pixliteLocalPanelNum;
        if (currentLogicalPanel < pixlite1Panels)
          pixliteLocalPanelNum = currentLogicalPanel;
        else
          pixliteLocalPanelNum = currentLogicalPanel - pixlite1Panels;
        String mapAddress = "" + pixliteLocalPanelNum + "." + currentPanelInput;
        // Which Panel-local universe are we in? 0,1,2?  Depends on our wireLedPos.  Then we also need to
        // check the Panel-Universe map in case something was wired up incorrectly and we need to
        // account for it in software.
        int inputUniverseOffset;

        // NOTE(Tracy): Can't really compute universeOffset anymore, we just need to handle
        // every case.  The panel input map only has 2 universes per entry.  Each entry is
        // a panel_num.input_num pair.  So 0.0=>0,1 ; 0.1 => 2,3 ; 1.0 => 4,5 ; 1.1 => 6,7
        if (wireLedPos - skippedLeds < 170) {
          inputUniverseOffset = 0;
        } else if (wireLedPos - skippedLeds < 250) {
          inputUniverseOffset = 1;
        } else if (wireLedPos < 420) {
          inputUniverseOffset = 0;
        } else {
          inputUniverseOffset = 1;
        }
        Map<String, List<Integer>> panelInputToUniverseMap;
        if (currentLogicalPanel < pixlite1Panels)
          panelInputToUniverseMap = pixlite1PanelConfig.getPanelInputsMap();
        else
          panelInputToUniverseMap = pixlite2PanelConfig.getPanelInputsMap();

        int currentPanelInputUniverse = panelInputToUniverseMap.get(mapAddress).get(inputUniverseOffset);

        // Chunk by 170 for each universe and also account for 250/200 pixel outputs.
        int universeLedPos;
        // The second input is never affected by skippedLeds so reset the counter
        if (wireLedPos == 250) {
          skippedLeds = 0;
        }
        if (wireLedPos - skippedLeds < 170) {
          universeLedPos = wireLedPos;
          universeLedPos -= skippedLeds;
        } else if (wireLedPos - skippedLeds < 250) {
          universeLedPos = wireLedPos - 170 - skippedLeds;
        } else if (wireLedPos < 420) {
          universeLedPos = wireLedPos - 250;
        } else {
          universeLedPos = wireLedPos - 420;
        }

        // int universeOffset = (wireLedPos - skippedLeds) / pointsPerUniverse;
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
        // if (universeLedPos < 8) logger.info("panel: " + mapAddress + " univOffset: " + inputUniverseOffset + " univLedPos: " + universeLedPos + " wireLedPos:" + wireLedPos + " colNum:" +colNumFromLeft + " rowNum:" + rowNumFromBottom + " pointIndex: " + globalPointIndex);
        if (!skippedThisLed) dmxChannelsForUniverse[universeLedPos] = globalPointIndex;
        //dmxChannelsForUniverse[universeLedPos] = globalPointIndex;
        // When it is time for a new universe it is time to configure an ArtNet
        // datagram packet and increment the universe number.  For expanded mode, we also need to increment the universe
        // number at 250 leds.
        // Either we 1. Hit our points per universe limit (170), or 2. The wireLedPos is at the last LED on the wire for
        // the entire panel, or 3. The wireLedPos is at the 250 pixel limit per expanded mode output (universeLedPos at
        // this point will be 79, i.e. 249 - 170)
        if (universeLedPos == pointsPerUniverse - 1 || wireLedPos == pointsWidePerPanel * pointsHighPerPanel - 1
            || wireLedPos == pointsPerInput-1) {
          // Construct with our custom datagram class that has lookup table Gamma correction and
          // rainbow background color correction.  Also, the last chunk of LEDs on a panel do not fill up an entire universe,
          // so set the datagram size based on the last universeLedPos.

          //if (universeLedPos != pointsPerUniverse -1) {
          // NOTE(tracy): Leaving this comment in here even though this if condition does nothing.  It would need to
          // be addressed if RainbowDatagram was not being used (i.e. some other non-artnet output).
          // If we came up short of 170 leds, we need to resize our dmxChannelsForUniverse array because RainbowDatagram and LXDatagram in general
          // just iterate through the length of the passed in dmxChannelsForUniverse, regardless of the dataLength we have constructed
          // it with.  This causes IndexOutOfBounds exceptions because the data buffer size is determined by dataLength, but the
          // buffer filling loop is determined by the array length dmxChannelsForUniverse.length
          // NOTE: I added a safety check to RainbowDatagram to stop at the buffer length so we can pass in a larger int[] than
          // our corresponding dataLength without blowing up.  Otherwise we need to compute the size of dmxChannelsForUniverse
          // appropriately for which chunk we are about to start working on AND whether or not it is a start/end panel.
          //}
          logger.info("Building datagram: universeLedPos=" + universeLedPos + " currentPanelInputUniverse=" +
              currentPanelInputUniverse);
          ArtNetDatagram datagram = new RainbowDatagram(lx, dmxChannelsForUniverse, (universeLedPos+1)*3,
              currentPanelInputUniverse);
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
      // lx.engine.output.framesPerSecond.setValue(1);
    } catch (SocketException sex) {
      // This can happen for example if we run out of file handles because some code is opening
      // files without closing them.
      logger.log(Level.SEVERE, "Initializing DatagramOutput", sex);
    }
  }

}

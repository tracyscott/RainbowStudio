package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import java.util.logging.Logger;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;

/**
 * Traces the wire of the actual panels.
 */
@LXCategory(LXCategory.TEST)
public class PanelWire extends LXPattern {
  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", 1.0, GLOBAL_FRAME_RATE)
          .setDescription("Controls the frames per second.");
  public CompoundParameter lengthKnob = new CompoundParameter("length", 5.0, 1.0, 450.0);
  public int currentPanel = 0;
  public int headPos = 0;
  protected double currentFrame = 0.0;
  protected int previousFrame = -1;

  private static final Logger logger = Logger.getLogger(PanelWire.class.getName());

  public PanelWire(LX lx) {
    super(lx);
    addParameter(fpsKnob);
    fpsKnob.setValue(5.0);
    addParameter(lengthKnob);
  }

  public void run(double deltaMs) {
    int pointsWidePerPanel = 15;
    int pointsWide = ((RainbowBaseModel)lx.model).pointsWide;
    int pointsHighPerPanel = ((RainbowBaseModel)lx.model).pointsHigh;
    int maxColNumPerPanel = pointsWidePerPanel;
    int numPanels = ((RainbowBaseModel)lx.model).pointsWide / pointsWidePerPanel;

    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs / 1000.0) * fps;
    if ((int) currentFrame <= previousFrame) {
      return;
    }
    previousFrame = (int) currentFrame;
    // reset frame counter occasionally for long running scenarios
    if (currentFrame > 10000.0) {
      currentFrame = 0.0;
      previousFrame = -1;
    }

    for (int panelNum = 0; panelNum < numPanels; panelNum++) {
      for (int wireLedPos = 0; wireLedPos < pointsWidePerPanel * pointsHighPerPanel; wireLedPos++) {
        int colNumFromLeft = -1;
        int colNumFromRight;
        int rowNumFromBottom = -1;

        // Special wiring instructions:
        // https://www.notion.so/rainbowbridge/End-panel-LED-routing-and-SW-mapping-848d7fa30de744fa94bcc199ba110a2d

        // E-variant panel (start of rainbow) has special wiring due to frame mounting.
        if (panelNum == 0) {
          // First 300 leds (6 strands are wired normal but mirrored in X dimension, start at bottom right on front
          if (wireLedPos < 300) {
            colNumFromRight = wireLedPos / pointsHighPerPanel;
            colNumFromLeft = maxColNumPerPanel - (colNumFromRight + 1);
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
                rowNumFromBottom = string8StartRow2 - 16 + string8WirePos;
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
        }

        // H-variant panel (end of rainbow) has special wiring due to frame mounting.
        if (panelNum == numPanels - 1) {
          // The first 300 leds are the typical wiring.
          if (wireLedPos < 300) {
            colNumFromLeft = wireLedPos / pointsHighPerPanel;
            colNumFromRight = maxColNumPerPanel - colNumFromLeft;
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
                rowNumFromBottom = string8StartRow2 - 16 + string8WirePos;
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
        }

        // Standard panels
        if (panelNum > 0 && panelNum < numPanels - 1) {
          colNumFromLeft = wireLedPos / pointsHighPerPanel;
          colNumFromRight = maxColNumPerPanel - colNumFromLeft;
          if (colNumFromLeft % 2 == 0)
            rowNumFromBottom = wireLedPos % pointsHighPerPanel;
          else
            rowNumFromBottom = pointsHighPerPanel - wireLedPos % pointsHighPerPanel - 1;
        }

        // Handle sentinel for unused leds on special-case end panels.
        if (colNumFromLeft == -1) {
          continue;
        }

        // Convert from Panel-local coordinates to global point coordinates.  Point 1,2 in Panel 2 is 420 * 2 + 1 + 2*30 = 901
        int globalPointIndex = rowNumFromBottom * pointsWide + colNumFromLeft + panelNum * pointsWidePerPanel;
        //logger.info(wireLedPos + " colNum:" +colNumFromLeft + " rowNum:" + rowNumFromBottom + " pointIndex: " + globalPointIndex);
        if ((wireLedPos >= (headPos - lengthKnob.getValue())) && (wireLedPos <= headPos)
            && currentPanel == panelNum) {
          colors[globalPointIndex] = LXColor.gray(100);
        } else {
          colors[globalPointIndex] = LXColor.gray(0);
        }
      }
    }
    headPos++;
    if (headPos >= 450) {
      currentPanel++;
      headPos = 0;
    }
    if (currentPanel >= numPanels) {
      currentPanel = 0;
    }
  }
}

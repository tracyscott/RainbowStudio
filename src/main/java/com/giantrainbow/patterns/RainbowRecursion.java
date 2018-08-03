package com.giantrainbow.patterns;

import static processing.core.PApplet.pow;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.COLOR)
public class RainbowRecursion extends LXPattern {
  public final CompoundParameter depthKnob =
      new CompoundParameter("Depth", 0, 10).setDescription("Max recursion depth.");
  public final CompoundParameter thicknessKnob =
      new CompoundParameter("Thick", 1, 10).setDescription("Thickness");
  public final CompoundParameter hueOffsetKnob =
      new CompoundParameter("hOffset", -360, 360).setDescription("Hue offset");

  private int maxDepth = 9;
  private int currentMaxDepth;
  private int pointsWide;
  private int pointsHigh;
  private int bandHeight = 3;
  private boolean forward = true;
  private double hueOffset;

  public RainbowRecursion(LX lx) {
    super(lx);
    pointsWide = ((RainbowBaseModel)(lx.model)).pointsWide;
    pointsHigh = ((RainbowBaseModel)(lx.model)).pointsHigh;
    addParameter(depthKnob);
    addParameter(thicknessKnob);
    addParameter(hueOffsetKnob);
    depthKnob.setValue(9);
    thicknessKnob.setValue(3);
    hueOffsetKnob.setValue(0);
  }

  public void run(double drawDeltaMs) {
    currentMaxDepth = ((int) depthKnob.getValue()) - 1;
    bandHeight = (int) thicknessKnob.getValue();
    for (LXPoint p : model.points) {
      colors[p.index] = 0xff000000;
    }
    colorRecursive(0, 0);
  }

  private void colorRecursive(int thisDepth, int xOffset) {
    // Draw a band of full hue across points at this level and this chunk. Based on our recursion depth and
    // the xOffset we can compute the batch of points that we need to color.
    if (thisDepth > currentMaxDepth) return;
    int chunkSize = pointsWide / (int)pow(2, thisDepth);
    int startLedRow = xOffset;
    int endLedRow = startLedRow + (int)thicknessKnob.getValue() * pointsWide;

    for (int currentLed = startLedRow; currentLed < endLedRow; currentLed++) {
      // Some combination of depths and band thickness can go past our available # leds
      if (currentLed >= model.points.length) return;

      // If we are at the end of a chunk, jump up to the next row of LEDs
     if (currentLed > startLedRow + chunkSize) {
        startLedRow = startLedRow + pointsWide;
        currentLed = startLedRow - 1;  // account for the for loop doing currentLed++
        continue;
      }
      int xpos = currentLed - startLedRow;
      float hue = 360.0f * (float)xpos/(float)chunkSize;
      hue = (float)hueOffsetKnob.getValue() + hue;
      if (hue > 360.0f) hue = hue - 360.0f;
      if (hue < 0.0) hue = hue + 360.0f;
      LXPoint p = model.points[currentLed];
      colors[p.index] = LXColor.hsb(hue, 100, 100);
    }
    colorRecursive(thisDepth + 1, endLedRow);
    colorRecursive(thisDepth + 1, endLedRow + chunkSize/2);
  }
}

package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.FORM)
public class RainbowScannerPattern extends LXPattern {

  public final CompoundParameter width =
    new CompoundParameter("Width", 0, 45)
    .setDescription("Controls the width of the scanner");

  // In columns per second, 0.2 is 5 columns per second
  public final CompoundParameter speed = new CompoundParameter("Speed", 0.2, 4)
    .setDescription("Controls the speed of the scanner");

  private double currentScannerColumn = 0.0;
  private boolean movingForward = true;

  public RainbowScannerPattern(LX lx) {
    super(lx);
    addParameter(width);
    addParameter(speed);
    movingForward = true;
    width.setValue(2.0);
  }

  @Override
    public void run(double deltaMs) {
    int numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    double columnsPerSecond = speed.getValue();
    double scannerWidth = width.getValue();
    if (movingForward) {
      currentScannerColumn += columnsPerSecond * deltaMs;
    } else {
      currentScannerColumn -= columnsPerSecond * deltaMs;
    }

    if (currentScannerColumn > numPixelsPerRow) {
      movingForward = false;
    }

    if (currentScannerColumn < 0) {
      movingForward = true;
    }

    int pointNumber = 0;
    for (LXPoint p : model.points) {
      int pointColumnNumber = pointNumber % numPixelsPerRow;
      if (pointColumnNumber < (int)currentScannerColumn + scannerWidth
        && pointColumnNumber >= currentScannerColumn - scannerWidth) {
        colors[p.index] = LXColor.gray(100);
      } else {
        // Set to black
        colors[p.index] = 0xff000000;
      }
      ++pointNumber;
    }
  }
}

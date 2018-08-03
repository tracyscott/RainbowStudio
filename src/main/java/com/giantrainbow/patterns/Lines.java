package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.TEXTURE)
public class Lines extends LXPattern {
  public final CompoundParameter posKnob =
    new CompoundParameter("Pos", 0, 0, ((RainbowBaseModel)lx.model).pointsHigh)
    .setDescription("Controls where the line is. e.g. Animate with LFO.");
  public final CompoundParameter widthKnob =
    new CompoundParameter("Width", 0, ((RainbowBaseModel)lx.model).pointsHigh * 2)
    .setDescription("Controls the width of the line+deadspace");
  public final CompoundParameter verticalKnob = new CompoundParameter("vert", 0.0, 0.0, 1.0)
    .setDescription("Vertical or Horizontal lines.");

  public Lines(LX lx) {
    super(lx);
    addParameter(posKnob);
    addParameter(widthKnob);
    addParameter(verticalKnob);
    widthKnob.setValue(2);
  }

  public void run(double deltaMs) {
    double position = this.posKnob.getValue();
    double lineWidth = widthKnob.getValue();

    int numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    int pointNumber = 0;

    for (LXPoint p : model.points) {
      if (verticalKnob.getValue() < 0.5) {
        int rowNumber = pointNumber / numPixelsPerRow; // Ranges 0-29
        // TODO(tracy): Compute a brightness that dims as we move away from the line
        // double brightness = 100.0 - (100.0/lineWidth) * Math.abs((rowNumber+1)%(lineWidth*2));
        double brightness = 0;
        if (((position + rowNumber) % lineWidth*2) < lineWidth)
          brightness = 100;
        else
          brightness = 0;
        colors[p.index] = LXColor.gray(brightness);
      } else {
        int colNumber = pointNumber % numPixelsPerRow;
        double brightness = 0;
        if (((position + colNumber) % lineWidth*2) < lineWidth)
          brightness = 100;
        else
          brightness = 0;
        colors[p.index] = LXColor.gray(brightness);
      }
      pointNumber++;
    }
  }
}

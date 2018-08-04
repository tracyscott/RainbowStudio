package com.giantrainbow.patterns;

import static processing.core.PApplet.round;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.RADIUS;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;

/**
 * A simple radial test.  It attempts to alternate one row of leds on and off.  Since
 * it renders to a larger texture and is then sampled for the final output, it is not
 * expected to be perfect because of aliasing issues.  It provides a visual representation
 * of the aliasing.
 */
@LXCategory(LXCategory.FORM)
public class PGRadiusTest extends PGTexture {

  public final CompoundParameter thicknessKnob =
      new CompoundParameter("thickness", 1.0, 10.0)
          .setDescription("Thickness of each band");

  public PGRadiusTest(LX lx) {
    super(lx, P2D);
    addParameter(thicknessKnob);
    thicknessKnob.setValue(1);
  }

  public void draw(double deltaDrawMs) {
    pg.background(0);
    int xCenterImgSpace = round(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot);
    int yCenterImgSpace = round(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot);
    pg.ellipseMode(RADIUS);
    pg.noSmooth();
    pg.noFill();
    // Adjust this to change the thickness of the bands.
    int strokeWidth = (int)thicknessKnob.getValue();
    if (strokeWidth < 1)
      strokeWidth = 1;
    pg.strokeWeight(strokeWidth);
    for (int i = 0; i < 30; i += strokeWidth) {
      float radius = round((RainbowBaseModel.innerRadius + i * RainbowBaseModel.radiusInc) *
        RainbowBaseModel.pixelsPerFoot);
      if (i % (2 * strokeWidth) == 0) pg.stroke(255);
      else pg.stroke(0);
      pg.ellipse(xCenterImgSpace, yCenterImgSpace, radius, radius);
    }
  }
}

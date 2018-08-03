package com.giantrainbow.patterns;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.P3D;
import static processing.core.PConstants.PI;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.FORM)
public class PG3DSimple extends PGTexture {
  public final CompoundParameter sizeKnob =
    new CompoundParameter("size", 1.0, 30.0)
    .setDescription("Size");

  public PG3DSimple(LX lx) {
    super(lx, P3D);
    fpsKnob.setValue(30);
    sizeKnob.setValue(20);
    addParameter(sizeKnob);
  }

  public void draw(double deltaDrawMs) {
    pg.background(0);
    float radiiThickness = RainbowBaseModel.outerRadius - RainbowBaseModel.innerRadius;
    float middleRadiusInWorldPixels = (RainbowBaseModel.innerRadius + radiiThickness) * RainbowBaseModel.pixelsPerFoot;
    pg.lights();
    pg.rectMode(CENTER);
    pg.fill(190);
    pg.noStroke();
    pg.translate(middleRadiusInWorldPixels, 20, 0);
    pg.rotateY(((int)currentFrame%16) * PI/16.0f);
    pg.box((int)(sizeKnob.getValue()));
  }
}

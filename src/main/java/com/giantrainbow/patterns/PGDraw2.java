package com.giantrainbow.patterns;

import static processing.core.PConstants.P2D;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

/**
 * PGDraw implementation by extending PGTexture.
 */
@LXCategory(LXCategory.FORM)
public class PGDraw2 extends PGTexture {
  float angle = 0.0f;
  public PGDraw2(LX lx) {
    super(lx, P2D);
  }

  @Override
  protected void draw(double deltaDrawMs) {
    angle += 0.03;
    pg.background(0);
    pg.strokeWeight(10.0f);
    pg.stroke(255);
    pg.translate(imageWidth/2.0f, imageHeight/2.0f);
    pg.pushMatrix();
    pg.rotate(angle);
    pg.line(-imageWidth/2.0f + 10, -imageHeight/2.0f + 10, imageWidth/2.0f - 10, imageHeight/2.0f - 10);
    pg.popMatrix();
  }
}

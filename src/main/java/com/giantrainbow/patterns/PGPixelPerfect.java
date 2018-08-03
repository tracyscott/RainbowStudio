package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;

/*
 * Abstract base class for pixel perfect Processing drawings.  Use this
 * class for 1-1 pixel mapping with the rainbow.  The drawing will be
 * a rectangle but in physical space it will be distorted by the bend of
 * the rainbow. Gets FPS knob from PGBase.
 */
abstract class PGPixelPerfect extends PGBase {
  public PGPixelPerfect(LX lx, String drawMode) {
    super(lx, ((RainbowBaseModel)lx.model).pointsWide,
      ((RainbowBaseModel)lx.model).pointsHigh,
      drawMode);
  }

  protected void imageToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(lx, colors, pg);
  }

  // Implement PGGraphics drawing code here.  PGPixelPerfect handles beginDraw()/endDraw();
  abstract protected void draw(double deltaDrawMs);
}

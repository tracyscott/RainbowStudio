package com.giantrainbow.patterns;

import static processing.core.PConstants.P2D;

import com.giantrainbow.model.RainbowModel3D;
import heronarts.lx.LX;

/**
 * CanvasPattern2D supports 2D-drawing into a linear array of pixels, then anti-aliasing into true
 * rainbow pixels.
 */
abstract class CanvasPattern2D extends CanvasPattern {
  public CanvasPattern2D(LX lx) {
    super(lx, ((RainbowModel3D) lx.model).canvas, P2D);
  }

  // Implement drawing code here.
  protected abstract void draw(double deltaDrawMs);
}

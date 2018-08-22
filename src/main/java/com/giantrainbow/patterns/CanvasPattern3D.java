package com.giantrainbow.patterns;

import static processing.core.PConstants.P3D;

import com.giantrainbow.model.RainbowModel3D;
import heronarts.lx.LX;

/**
 * CanvasPattern3D supports 3D-drawing into a linear array of pixels, then anti-aliasing into true
 * rainbow pixels.
 */
abstract class CanvasPattern3D extends CanvasPattern {
  public CanvasPattern3D(LX lx) {
    super(lx, ((RainbowModel3D) lx.model).canvas, P3D);
  }

  // Implement drawing code here.
  protected abstract void draw(double deltaDrawMs);
}

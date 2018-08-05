package com.giantrainbow.patterns;

import static processing.core.PConstants.P3D;

import com.giantrainbow.canvas.Canvas;
import heronarts.lx.LX;

/**
 * CanvasPattern3D supports 3D-drawing into a linear array of pixels, then anti-aliasing into true
 * rainbow pixels. [WIP]
 */
abstract class CanvasPattern3D extends PGBase {
  Canvas canvas;

  public CanvasPattern3D(LX lx, Canvas canvas) {
    super(lx, canvas.width(), canvas.height(), P3D);

    this.canvas = canvas;
  }

  protected void imageToPoints() {
    if (pg.pixels == null) {
      pg.loadPixels();
    }

    // TODO Use the pg image buffer directly instead of copy.
    canvas.buffer.copyIn(pg.pixels);
    canvas.render(colors);
  }

  // Implement drawing code here.
  protected abstract void draw(double deltaDrawMs);
}

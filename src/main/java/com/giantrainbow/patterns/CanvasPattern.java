package com.giantrainbow.patterns;

import com.giantrainbow.canvas.Canvas;
import heronarts.lx.LX;

/**
 * CanvasPattern supports drawing into a linear array of pixels, then anti-aliasing into true
 * rainbow pixels for 2D and 3D patterns
 */
abstract class CanvasPattern extends PGBase {
  Canvas canvas;

  public CanvasPattern(LX lx, Canvas canvas, String type) {
    super(lx, canvas.width(), canvas.height(), type);

    this.canvas = canvas;
  }

  protected void imageToPoints() {
    pg.loadPixels();

    // TODO Use the pg image buffer directly instead of copy.
    canvas.buffer.copyInto(pg.pixels);
    pg.updatePixels();

    // Note: to see the underlying buffer each frame, uncomment.
    // canvas.dumpImage();
    canvas.render(colors);
  }

  // Implement drawing code here.
  protected abstract void draw(double deltaDrawMs);
}

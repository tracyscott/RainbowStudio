package com.giantrainbow.patterns;

import static processing.core.PConstants.P3D;

import com.giantrainbow.canvas.Canvas;
import heronarts.lx.LX;

abstract class CanvasPattern3D extends PGBase {
  Canvas canvas;

  public CanvasPattern3D(LX lx, Canvas canvas) {
    super(lx, canvas.width(), canvas.height(), P3D);

    this.canvas = canvas;
  }

  protected void imageToPoints() {
    // TODO Use the pg image buffer directly instead of copy.
    if (pg.pixels == null) {
      pg.loadPixels();
    }

    canvas.buffer.copy(pg.pixels);
    canvas.dumpImage();
    canvas.render(colors);
  }

  // Implement drawing code here.
  protected abstract void draw(double deltaDrawMs);
}

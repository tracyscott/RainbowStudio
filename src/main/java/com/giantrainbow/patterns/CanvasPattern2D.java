package com.giantrainbow.patterns;

import com.giantrainbow.canvas.Canvas;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;

abstract class CanvasPattern2D extends LXPattern {
  Canvas canvas;

  public CanvasPattern2D(LX lx) {
    super(lx);

    this.canvas = new Canvas(lx.model);
  }

  public void run(double deltaMs) {
    draw(deltaMs);
    canvas.render(colors);
  }

  // Implement drawing code here.
  protected abstract void draw(double deltaDrawMs);
}

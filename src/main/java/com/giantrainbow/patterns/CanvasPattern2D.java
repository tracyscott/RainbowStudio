package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;

import com.giantrainbow.canvas.Canvas;

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
  abstract protected void draw(double deltaDrawMs);
}

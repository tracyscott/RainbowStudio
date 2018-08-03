package com.giantrainbow.patterns;

import heronarts.lx.LX;

import static processing.core.PConstants.P3D;

import com.giantrainbow.canvas.Canvas;

abstract class CanvasPattern3D extends PGBase {
    Canvas canvas;

    public CanvasPattern3D(LX lx, Canvas canvas) {
 	super(lx, canvas.width(), canvas.height(), P3D);

        this.canvas = canvas;
    }

    protected void imageToPoints() {
        // RenderImageUtil.imageToPointsSemiCircle(lx, colors, pg, antialiasKnob.isOn());
	// canvas.render(colors);
    }

    // Implement drawing code here.
    abstract protected void draw(double deltaDrawMs);
}

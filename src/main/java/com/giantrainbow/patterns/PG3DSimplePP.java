package com.giantrainbow.patterns;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.P3D;
import static processing.core.PConstants.PI;

import heronarts.lx.LXCategory;
import heronarts.p3lx.P3LX;

/**
 * Simple Processing 3D example.  Note, due to threading limitations with
 * OpenGL and Processing/Java to run P3D patterns, you MUST NOT enable the
 * separate thread for the Engine in the UI.  Only a single thread may perform
 * OpenGL operations and since the UI is already using OpenGL to render the
 * interface, this drawing code must run in the same thread as the UI.
 */
@LXCategory(LXCategory.FORM)
public class PG3DSimplePP extends PGPixelPerfect {
  public PG3DSimplePP(P3LX lx) {
    super(lx, P3D);
  }

  public void draw(double deltaDrawMs) {
    pg.background(0);
    pg.lights();
    pg.rectMode(CENTER);
    pg.fill(190);
    pg.noStroke();
    pg.translate(100, 10, 0);
    pg.rotateY(((int)currentFrame%16) * PI/16.0f);
    pg.rotateX(-0.3f);
    pg.box(10);
  }
}

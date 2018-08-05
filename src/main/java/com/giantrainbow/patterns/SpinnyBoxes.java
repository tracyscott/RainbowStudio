package com.giantrainbow.patterns;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.PI;

import com.giantrainbow.canvas.Canvas;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.FORM)
public class SpinnyBoxes extends CanvasPattern3D {
  public final CompoundParameter sizeKnob =
      new CompoundParameter("size", 1.0, 30.0).setDescription("Size");

  public SpinnyBoxes(LX lx) {
    super(lx, new Canvas(lx.model));
    fpsKnob.setValue(30);
    sizeKnob.setValue(20);
    addParameter(sizeKnob);
  }

  public void draw(double deltaDrawMs) {
    int size = (int) (3 * sizeKnob.getValue());
    pg.background(0);
    pg.lights();
    pg.rectMode(CENTER);
    pg.fill(255, 255, 0);
    pg.noStroke();
    pg.translate(canvas.width() / 2, canvas.height() - size, 0);
    pg.rotateY(((int) currentFrame % 16) * PI / 16.0f);
    pg.box(size);
  }
}

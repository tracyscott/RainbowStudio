package com.giantrainbow.patterns;

import heronarts.lx.LX;
import processing.core.PImage;
import com.giantrainbow.textures.Positioner;
import com.giantrainbow.textures.Strange;

public abstract class AbstractEpileptic extends PGBase implements Positioner {

  Strange strange;

  AbstractEpileptic(LX lx) {
      super(lx, 420, 30, "");
      this.strange = new Strange(this, this, "Pulse");
  }

  public void draw(double deltaMs) {
    PImage img = strange.update(deltaMs);

    if (img == null) {
      return;
    }
      
    RenderImageUtil.imageToPointsPixelPerfect(colors, img);
  }

  public void imageToPoints() {}

  public abstract int[] getPositions(int period);
}

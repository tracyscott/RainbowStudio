package com.giantrainbow.patterns;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.colors.Colors;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PImage;

public class BackgroundPulse {

  public final float MSHZ = 1 / 10000f;

  public final CompoundParameter rateKnob =
      new CompoundParameter("BG rate", 2000, 0, 10000).setDescription("BG rate");
  public final CompoundParameter levelKnob =
      new CompoundParameter("BG level", 0.25, 0, 1).setDescription("BG level");

  double elapsed;
  int colors[];

  public BackgroundPulse(LXPattern pattern) {
    pattern.addParameter(levelKnob);
    pattern.addParameter(rateKnob);

    PImage bg = RainbowStudio.pApplet.loadImage("images/lab-square-lookup.png");
    bg.loadPixels();

    // This texture is square.
    if (bg.width != bg.height) {
      System.err.println("Should be a square texture " + bg.width + " != " + bg.height);
    }

    int width = bg.width;
    int perimSize = (width - 1) * 4;

    this.colors = new int[perimSize];

    int pos = 0;
    for (int i = 0; i < width; i++, pos++) {
      this.colors[pos] = bg.pixels[i];
    }
    for (int j = 1; j < width; j++, pos++) {
      this.colors[pos] = bg.pixels[j * width + (width - 1)];
    }
    for (int i = width - 2; i >= 0; i--, pos++) {
      this.colors[pos] = bg.pixels[(width - 1) * width + i];
    }
    for (int j = width - 2; j >= 1; j--, pos++) {
      this.colors[pos] = bg.pixels[j * width];
    }
  }

  public int get(double deltaMs) {
    elapsed += (rateKnob.getValue() * deltaMs);

    int bgc = colors[(int) (elapsed * MSHZ) % colors.length];
    float bgl = (float) levelKnob.getValue();

    return Colors.rgb(
        (int) ((float) Colors.red(bgc) * bgl),
        (int) ((float) Colors.green(bgc) * bgl),
        (int) ((float) Colors.blue(bgc) * bgl));
  }
}

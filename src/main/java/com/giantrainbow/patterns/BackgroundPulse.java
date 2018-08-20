package com.giantrainbow.patterns;

import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;

public class BackgroundPulse {

  public final float BACKGROUND_SPEED = 10f;
  public final float BACKGROUND_RATE = 100f;

  public final CompoundParameter bgRateKnob =
      new CompoundParameter("BG rate", 10, 1, 100).setDescription("BG rate");
  public final CompoundParameter bgLevelKnob =
      new CompoundParameter("BG level", 0.1, 0, 1).setDescription("BG level");

  public BackgroundPulse(LXPattern pattern) {
    pattern.addParameter(bgLevelKnob);
    pattern.addParameter(bgRateKnob);
  }

  public int get(double deltaMs) {
    // @@@
  }

  // final int bgLevels = 1;
  // final int bgGradSize = 360;
  // final double brightIncr = 0.05;
  // final double brightOffset = 0.00;
  // int bgcolor[];

  // computeBackground();

  // public void computeBackground() {
  //   bgcolor = new int[bgLevels * bgGradSize];

  //   for (int l = 0; l < bgLevels; l++) {
  //     String file =
  //         String.format("images/lch-disc-level=%.02f-sat=1.00.png", brightOffset + l *
  // brightIncr);
  //     PImage bgTexture = RainbowStudio.pApplet.loadImage(file);
  //     bgTexture.loadPixels();
  //     final int tolerance = 1;
  //     final int center = bgTexture.width / 2;

  //     for (int b = 0; b < bgGradSize; b++) {
  //       final double theta = 2.0 * Math.PI * (double) b / (double) bgGradSize;
  //       final int xpos = center + (int) (Math.cos(theta) * (double) (center - tolerance));
  //       final int ypos = center + (int) (Math.sin(theta) * (double) (center - tolerance));
  //       bgcolor[l * bgGradSize + b] = bgTexture.pixels[ypos * bgTexture.width + xpos];
  //     }
  //   }
  // }

  // int bgc =
  //     bgcolor[(int) (telapsed * BG_RATE * (float) bgRateKnob.getValue() * MSHZ) %
  //   bgGradSize];
  // float bgl = (float) bgLevelKnob.getValue();

  // pg.background(
  //     Colors.rgb(
  //         (int) ((float) Colors.red(bgc) * bgl),
  //         (int) ((float) Colors.green(bgc) * bgl),
  //         (int) ((float) Colors.blue(bgc) * bgl)));
}

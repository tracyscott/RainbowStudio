package com.giantrainbow.patterns;

import static processing.core.PConstants.RGB;

import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import java.util.Random;
import processing.core.PImage;

@LXCategory(LXCategory.FORM)
public class SpinnyWheels extends AbstractSpinnyDiscs {

  PImage textures[];

  BackgroundPulse pulse;

  public SpinnyWheels(LX lx) {
    super(lx);

    this.pulse = new BackgroundPulse(this);
    this.textures = new PImage[BALL_COUNT];

    PImage shape = RainbowStudio.pApplet.loadImage("images/spin-disc-k=1.png");
    PImage palette = RainbowStudio.pApplet.loadImage("images/lab-square-lookup.png");

    Random rnd = new Random();

    countKnob.setValue(500);

    for (int i = 0; i < BALL_COUNT; i++) {
      textures[i] = RainbowStudio.pApplet.createImage(shape.width, shape.width, RGB);

      int x = rnd.nextInt(palette.width);
      int y = rnd.nextInt(palette.width);
      int c = palette.pixels[x * palette.width + y];

      for (int p = 0; p < textures[i].pixels.length; p++) {
        textures[i].pixels[p] = c;
      }
      textures[i].mask(shape);
    }
  }

  PImage getTexture(int number) {
    return this.textures[number % textures.length];
  }

  boolean hasBackground() {
    return true;
  }

  int getBackground(double deltaMs) {
    return pulse.get(deltaMs);
  }
};

package com.giantrainbow.patterns;

import static processing.core.PConstants.RGB;

import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import java.util.Random;
import processing.core.PImage;

public abstract class AbstractSpinnyWheels extends AbstractSpinnyDiscs {

  PImage textures[];

  public AbstractSpinnyWheels(LX lx) {
    super(lx);

    this.textures = new PImage[BALL_COUNT];

    new Thread(
            new Runnable() {
              public void run() {
                loadTextures();
              }
            })
        .start();
  }

  void loadTextures() {
    PImage shape = RainbowStudio.pApplet.loadImage("images/spin-disc-k=1.png");
    PImage shape2 = RainbowStudio.pApplet.createImage(shape.width, shape.width, RGB);
    shape2.loadPixels();

    // Reflection
    for (int x = 0; x < shape.width; x++) {
      for (int y = 0; y < shape.width; y++) {
        shape2.pixels[x + shape.width * y] = shape.pixels[(shape.width - 1 - x) + shape.width * y];
      }
    }
    shape2.updatePixels();

    PImage palette = RainbowStudio.pApplet.loadImage("images/lab-square-lookup.png");
    Random rnd = new Random();

    for (int i = 0; i < BALL_COUNT; i++) {

      PImage img = RainbowStudio.pApplet.createImage(shape.width, shape.width, RGB);

      int x = rnd.nextInt(palette.width);
      int y = rnd.nextInt(palette.width);
      int c = palette.pixels[x * palette.width + y];

      for (int p = 0; p < img.pixels.length; p++) {
        img.pixels[p] = c;
      }
      img.mask(i % 2 == 0 ? shape : shape2);

      synchronized (textures) {
        textures[i] = img;
      }
    }
  }

  PImage getTexture(int number) {
    synchronized (textures) {
      return this.textures[number % textures.length];
    }
  }
};

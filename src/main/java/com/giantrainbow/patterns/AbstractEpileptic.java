package com.giantrainbow.patterns;

import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import processing.core.PImage;

public abstract class AbstractEpileptic extends PGBase {
  PImage textures[];

  String inputs[] = {
    "images/blend-red-blue.png",
    "images/blend-green-yellow.png",
    "images/blend-blue-red.png",
    "images/blend-yellow-green.png",
    "images/blend-red-green.png",
    "images/blend-yellow-blue.png",
    "images/blend-green-red.png",
    "images/blend-blue-yellow.png",
    "images/blend-blue-green.png",
    "images/blend-green-blue.png",

    // Not so useful:
    //
    // "images/blend-red-yellow.png",
    // "images/blend-yellow-red.png",
  };

  public final CompoundParameter rateKnob =
      new CompoundParameter("Rate", 100, 1, 200).setDescription("Rate");
  public final CompoundParameter periodKnob =
      new CompoundParameter("Period", 100, 1, 200).setDescription("Period");

  public AbstractEpileptic(LX lx) {
    super(lx, 420, 30, "");

    addParameter(rateKnob);
    addParameter(periodKnob);

    textures = new PImage[inputs.length];
    rnd = new Random();

    new Thread(
            new Runnable() {
              public void run() {
                loadTextures();
              }
            })
        .start();
  }

  void loadTextures() {
    for (int i = 0; i < inputs.length; i++) {
      PImage img = RainbowStudio.pApplet.loadImage(inputs[i]);
      img.loadPixels();
      synchronized (textures) {
        this.textures[i] = img;
      }
    }
  }

  double elapsed;
  double pelapsed;
  Random rnd;

  final float MSHZ = 1f / 10000f;
  final float PERIOD = 1e4f;

  public void draw(double deltaMs) {
    elapsed += deltaMs * MSHZ * rateKnob.getValue();
    pelapsed += deltaMs * MSHZ * periodKnob.getValue();

    int periodNum = (int) (pelapsed % PERIOD);

    int positions[] = getPositions(periodNum);

    int pattern = positions[((int) (elapsed)) % positions.length];

    PImage img;
    synchronized (textures) {
      img = textures[pattern];
    }

    if (img == null) {
      return;
    }

    RenderImageUtil.imageToPointsPixelPerfect(colors, img);
  }

  public void imageToPoints() {}

  abstract int[] getPositions(int period);
}

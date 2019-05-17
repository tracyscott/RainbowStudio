package com.giantrainbow.textures;

import com.giantrainbow.RainbowStudio;
import heronarts.lx.LXPattern;
import java.util.Random;
import processing.core.PImage;
import heronarts.lx.parameter.CompoundParameter;

// TODO: EpilepticWarning should use this.

public class Strange {
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

    public final CompoundParameter rateKnob;
    public final CompoundParameter periodKnob;

    public Strange(LXPattern pattern, Positioner positioner, String name) {
    this.positioner = positioner;

    this.rateKnob =
      new CompoundParameter(name + "Rate", 100, 1, 200).setDescription(name + " Rate");
    this.periodKnob =
      new CompoundParameter(name + "Period", 100, 1, 200).setDescription(name + " Period");

    pattern.addParameter(rateKnob);
    pattern.addParameter(periodKnob);

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
      synchronized (this) {
        this.textures[i] = img;
      }
    }
  }

  double elapsed;
  double pelapsed;
  final Random rnd;
  final Positioner positioner;

  final float MSHZ = 1f / 10000f;
  final float PERIOD = 1e4f;

  public PImage update(double deltaMs) {
    elapsed += deltaMs * MSHZ * rateKnob.getValue();
    pelapsed += deltaMs * MSHZ * periodKnob.getValue();

    int periodNum = (int) (pelapsed % PERIOD);

    int positions[] = positioner.getPositions(periodNum);

    int pattern = positions[((int) (elapsed)) % positions.length];

    PImage img;
    synchronized (this) {
      img = textures[pattern];
    }
    return img;
  }      
}

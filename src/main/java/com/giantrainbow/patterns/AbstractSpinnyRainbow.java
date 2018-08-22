package com.giantrainbow.patterns;

import static processing.core.PConstants.RGB;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PImage;

public abstract class AbstractSpinnyRainbow extends AbstractSpinnyDiscs {

  public final CompoundParameter brightKnob =
      new CompoundParameter("Bright", 0.5, 0, 1).setDescription("Bright");

  // Texture alpha mask
  int textureA[];

  PImage textureLch; // Uniform
  PImage textureHsv; // Bright
  PImage texture; // Blended from the above

  public AbstractSpinnyRainbow(LX lx) {
    super(lx);

    addParameter(brightKnob);

    brightKnob.addListener(
        lxParameter -> {
          setTexture(lxParameter.getValue());
        });

    new Thread(
            new Runnable() {
              public void run() {
                loadTextures();
              }
            })
        .start();
  }

  void loadTextures() {
    // The texture files are square.
    this.textureLch = RainbowStudio.pApplet.loadImage("images/lch-disc-level=0.60-sat=1.00.png");
    this.textureHsv = RainbowStudio.pApplet.loadImage("images/hsv-disc-level=1.00-sat=1.00.png");

    this.textureLch.loadPixels();
    this.textureHsv.loadPixels();

    int ta[] = new int[this.textureLch.width * this.textureLch.width];
    PImage img =
        RainbowStudio.pApplet.createImage(this.textureLch.width, this.textureLch.width, RGB);

    synchronized (this) {
      this.textureA = ta;
      this.texture = img;
    }

    setTexture(brightKnob.getValue());
  }

  PImage getTexture(int number) {
    synchronized (this) {
      return this.texture;
    }
  }

  void setTexture(double bright) {
    synchronized (this) {
      if (this.textureA == null || this.texture == null) {
        return;
      }
    }
    double dim = 1. - bright;
    for (int i = 0; i < this.textureLch.width; i++) {
      for (int j = 0; j < this.textureLch.width; j++) {
        int idx = i + j * this.textureLch.width;

        int lr = Colors.red(this.textureLch.pixels[idx]);
        int lg = Colors.green(this.textureLch.pixels[idx]);
        int lb = Colors.blue(this.textureLch.pixels[idx]);
        int la = Colors.alpha(this.textureLch.pixels[idx]);

        int hr = Colors.red(this.textureHsv.pixels[idx]);
        int hg = Colors.green(this.textureHsv.pixels[idx]);
        int hb = Colors.blue(this.textureHsv.pixels[idx]);

        this.textureA[idx] = la;
        this.texture.pixels[idx] =
            Colors.rgb(
                (int) (dim * (double) lr + bright * (double) hr),
                (int) (dim * (double) lg + bright * (double) hg),
                (int) (dim * (double) lb + bright * (double) hb));
      }
    }
    this.texture.mask(this.textureA);

    synchronized (this) {
      this.texture.updatePixels();
    }
  }
};

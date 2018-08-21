package com.giantrainbow.patterns;

import static processing.core.PConstants.RGB;

import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import java.util.Random;
import processing.core.PGraphics;
import processing.core.PImage;

@LXCategory(LXCategory.FORM)
public class Flowers extends AbstractSpinnyDiscs {

  PImage textures[];

  String props[] = {
    "images/star-disc-k=2.png",
    "images/star-disc-k=3.png",
    "images/star-disc-k=4.png",
    "images/star-disc-k=5.png",
    "images/star-disc-k=6.png",
    "images/star-disc-k=7.png",
    "images/star-disc-k=8.png",
    "images/star-disc-k=9.png",
    "images/star-disc-k=10.png",
    "images/star-disc-k=11.png",
    "images/star-disc-k=12.png",
  };

  BackgroundPulse pulse;

  public Flowers(LX lx) {
    super(lx);

    PImage shapes[];

    this.pulse = new BackgroundPulse(this);

    shapes = new PImage[props.length];
    textures = new PImage[BALL_COUNT];

    PImage colors[] = new PImage[2];
    colors[0] = RainbowStudio.pApplet.loadImage("images/xyy-square-lookup.png");
    colors[1] = RainbowStudio.pApplet.loadImage("images/xyz-square-lookup.png");

    countKnob.setValue(750);

    for (int i = 0; i < props.length; i++) {
      shapes[i] = RainbowStudio.pApplet.loadImage(props[i]);
    }

    PGraphics graphics = RainbowStudio.pApplet.createGraphics(shapes[0].width, shapes[0].width);
    Random rnd = new Random();

    for (int i = 0; i < BALL_COUNT; i++) {
      PImage shape = shapes[i % shapes.length];
      PImage color = colors[i % colors.length];

      textures[i] = RainbowStudio.pApplet.createImage(shape.width, shape.width, RGB);
      textures[i].loadPixels();

      int d = color.width - shape.width;
      int x = rnd.nextInt(color.width - shape.width);
      int y = rnd.nextInt(color.width - shape.width);
      double theta = rnd.nextFloat() * 2. * Math.PI;

      graphics.beginDraw();
      graphics.pushMatrix();

      graphics.copy(color, 0, 0, color.width, color.width, 0, 0, shape.width, shape.width);
      graphics.translate(x + d / 2, y + d / 2);
      graphics.rotate((float) theta);

      textures[i].copy(graphics, 0, 0, shape.width, shape.width, 0, 0, shape.width, shape.width);
      textures[i].mask(shape);
      graphics.popMatrix();
      graphics.endDraw();
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

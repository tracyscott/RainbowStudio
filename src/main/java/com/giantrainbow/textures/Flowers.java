package com.giantrainbow.textures;

import static processing.core.PConstants.RGB;

import com.giantrainbow.RainbowStudio;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.Random;

public class Flowers {

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

  public Flowers(int count) {
    this.textures = new PImage[count];

    // TODO: this was recommended:
    // ScheduledExecutorService executor = ((RainbowStudio)
    // applet).registry.get(Registry.Key.EXEC);
    new Thread(
            new Runnable() {
              public void run() {
                loadTextures();
              }
            })
        .start();
    }

  public PImage getTexture(int number) {
      synchronized (this) {
	  return textures[number % textures.length];
      }
  }
    
  void loadTextures() {
    PImage shapes[] = new PImage[props.length];
    PImage colors[] = new PImage[2];

    colors[0] = RainbowStudio.pApplet.loadImage("images/xyy-square-lookup.png");
    colors[1] = RainbowStudio.pApplet.loadImage("images/xyz-square-lookup.png");

    colors[0].loadPixels();
    colors[1].loadPixels();

    for (int i = 0; i < props.length; i++) {
      shapes[i] = RainbowStudio.pApplet.loadImage(props[i]);
      shapes[i].loadPixels();
    }

    PGraphics graphics = RainbowStudio.pApplet.createGraphics(shapes[0].width, shapes[0].width);
    Random rnd = new Random();

    for (int i = 0; i < textures.length; i++) {
      PImage shape = shapes[i % shapes.length];
      PImage color = colors[i % colors.length];
      PImage img = RainbowStudio.pApplet.createImage(shape.width, shape.width, RGB);
      img.loadPixels();

      int d = color.width - shape.width;
      int x = rnd.nextInt(color.width - shape.width);
      int y = rnd.nextInt(color.width - shape.width);
      double theta = rnd.nextFloat() * 2. * Math.PI;

      graphics.beginDraw();
      graphics.pushMatrix();

      graphics.copy(color, 0, 0, color.width, color.width, 0, 0, shape.width, shape.width);
      graphics.translate(x + d / 2, y + d / 2);
      graphics.rotate((float) theta);

      img.copy(graphics, 0, 0, shape.width, shape.width, 0, 0, shape.width, shape.width);
      img.mask(shape);
      img.updatePixels();

      graphics.popMatrix();
      graphics.endDraw();

      synchronized (this) {
        textures[i] = img;
      }
    }
  }
};

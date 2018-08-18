package com.giantrainbow.patterns;

import static processing.core.PConstants.CLAMP;
import static processing.core.PConstants.RGB;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import processing.core.PImage;

@LXCategory(LXCategory.FORM)
public class SpinnyDiscs extends CanvasPattern2D {
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 5, 1, 20).setDescription("Speed");
  public final CompoundParameter countKnob =
      new CompoundParameter("Count", 100, 1, 10000).setDescription("Count");

  Ball balls[];
  double elapsed;
  PImage texture;
  float xmax;
  float ymax;
  float xoff;
  float yoff;

  PImage makeTexture() {
    PImage img = RainbowStudio.pApplet.createImage(canvas.width(), canvas.width(), RGB);

    int[] alpha = new int[img.pixels.length];

    final float tolerance = 2;
    float width = canvas.width();
    float center = canvas.width() / 2;
    float r2 = (center - tolerance) * (center - tolerance);

    img.loadPixels();

    for (int xi = 0; xi < width; xi++) {
      float xc = (float) xi + 0.5f;
      float xd = center - xc;
      float xd2 = xd * xd;

      for (int yi = 0; yi < width; yi++) {
        float yc = (float) yi + 0.5f;
        float yd = center - yc;
        float yd2 = yd * yd;

        int idx = canvas.width() * yi + xi;

        if (xd2 + yd2 > r2) {
          img.pixels[idx] = 0;
          alpha[idx] = 0;
          continue;
        }
        alpha[idx] = 255;

        float theta = (float) (Math.atan(yd / xd) + (Math.PI / 2));

        if (xd < 0) {
          theta += Math.PI;
        }

        float hue = (float) (theta / (2 * Math.PI));
        float chroma = 1;
        float level = 1;

        img.pixels[idx] = Colors.hsb(hue, chroma, level);
      }
    }

    img.mask(alpha);
    img.updatePixels();
    return img;
  }

  public final float EXPANSION = 1.25f;
  public final float MAX_SIZE = 100;
  public final int BALL_COUNT = 10000;

  public SpinnyDiscs(LX lx) {
    super(lx);

    this.elapsed = 0;
    this.balls = new Ball[BALL_COUNT];
    this.texture = makeTexture();

    float xsize = canvas.width();
    float ysize = canvas.height();

    this.xmax = xsize * EXPANSION;
    this.ymax = ysize * EXPANSION;
    this.xoff = (xmax - xsize) / 2;
    this.yoff = (ymax - ysize) / 2;

    pg.noLights();
    pg.textureWrap(CLAMP);

    pg.smooth(8);

    Random rnd = new Random();

    for (int i = 0; i < balls.length; i++) {
      balls[i] = new Ball();
      balls[i].X = xmax * rnd.nextFloat();
      balls[i].Y = ymax * rnd.nextFloat();
      balls[i].R = MAX_SIZE * rnd.nextFloat();
      balls[i].S = rnd.nextFloat();
    }

    addParameter(speedKnob);
    addParameter(countKnob);
  }

  public void draw(double deltaMs) {
    double speed = speedKnob.getValue();
    elapsed += deltaMs * speed;

    pg.background(Colors.BLACK);

    for (int i = 0; i < countKnob.getValue(); i++) {
      if (i >= balls.length) {
        break;
      }
      balls[i].draw();
    }
  }

  public class Ball {
    float X;
    float Y;
    float R;
    float S;
    float rotation;

    void draw() {
      pg.pushMatrix();

      pg.translate(xoff + X, yoff + Y);
      pg.rotate((float) elapsed * S / 1000f);

      pg.beginShape();
      pg.noStroke();

      pg.texture(texture);

      float w = canvas.width();

      pg.vertex(-R, -R, 0, 0);
      pg.vertex(R, -R, w, 0);
      pg.vertex(R, R, w, w);
      pg.vertex(-R, R, 0, w);
      pg.endShape();

      pg.popMatrix();
    }
  };
}

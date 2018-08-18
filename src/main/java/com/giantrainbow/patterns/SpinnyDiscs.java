package com.giantrainbow.patterns;

import static processing.core.PConstants.CLAMP;
import static processing.core.PConstants.RGB;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.colors.Colors;
import com.giantrainbow.model.space.Lissajous;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import processing.core.PImage;

// TODO Rotate the whole model on the origin?  Rotate each orbit w.r.t. the origin?
// Swap positions when not overlapping.

/**
 * SpinnyDiscs animates a number of variable sized, rotating color wheels in 2D space. They are
 * implemented as a texture mapped circle w/ alpha surround. The balls are positioned by a
 * https://en.wikipedia.org/wiki/Lissajous_curve.
 */
@LXCategory(LXCategory.FORM)
public class SpinnyDiscs extends CanvasPattern2D {
  public final float EXPANSION = 1.25f;
  public final float MAX_SIZE = 100;
  public final float BACKGROUND_SPEED = 10f;
  public final float BACKGROUND_SAT = 1;
  public final float BACKGROUND_BRIGHT = .15f;
  public final float MOVEMENT_RANGE = 0.5f; // Movement range as a ratio of width/height
  public final float MAX_SPEED = 10f; // Speed range
  public final float MSHZ = 1 / 100000f; // Arbitrary slowdown of the millisecond counter

  public final int BALL_COUNT = 1000;

  // Speed determines the overall speed of the entire pattern.
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 10, 0, 20).setDescription("Speed");
  // Count determines the number of balls that render.  They are
  // animated continuously, so raising and lower the number will
  // show/hide them while they continue moving with the animation.
  public final CompoundParameter countKnob =
      new CompoundParameter("Count", 100, 1, BALL_COUNT).setDescription("Count");

  // The "a" paramter of a Lissajous curve that moves all the balls.
  public final CompoundParameter aKnob = new CompoundParameter("a", 5, -10, 10).setDescription("a");

  // The "b" paramter of a Lissajous curve that moves all the balls.
  public final CompoundParameter bKnob =
      new CompoundParameter("b", 2.5, -10, 10).setDescription("b");

  // The "delta" paramter of a Lissajous curve that moves all the balls.
  public final CompoundParameter deltaKnob =
      new CompoundParameter("delta", 0, -Math.PI / 2, Math.PI / 2).setDescription("delta");

  Ball balls[];
  float elapsed;
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

    pg.smooth(2);

    Random rnd = new Random();

    for (int i = 0; i < balls.length; i++) {
      balls[i] = new Ball();
      balls[i].X = xmax * rnd.nextFloat();
      balls[i].Y = ymax * rnd.nextFloat();
      balls[i].A = xmax * rnd.nextFloat() * MOVEMENT_RANGE;
      balls[i].B = ymax * rnd.nextFloat() * MOVEMENT_RANGE;
      balls[i].R = MAX_SIZE * rnd.nextFloat();
      balls[i].S = MAX_SPEED * 2f * (rnd.nextFloat() - 0.5f);
    }

    removeParameter(fpsKnob);
    addParameter(speedKnob);
    addParameter(countKnob);
    addParameter(aKnob);
    addParameter(bKnob);
    addParameter(deltaKnob);
  }

  public void draw(double deltaMs) {
    double speed = speedKnob.getValue();
    elapsed += (float) (deltaMs * speed);

    pg.background(Colors.hsb(elapsed * BACKGROUND_SPEED * MSHZ, BACKGROUND_SAT, BACKGROUND_BRIGHT));

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
    float A;
    float B;
    float R;
    float S;

    void draw() {
      pg.pushMatrix();

      float a = (float) aKnob.getValue();
      float b = (float) bKnob.getValue();
      float delta = (float) deltaKnob.getValue();
      float position = (float) elapsed * S * MSHZ;
      float x = Lissajous.locationX(A, a, delta, position);
      float y = Lissajous.locationY(B, b, position);

      pg.translate(-xoff + X + x, -yoff + Y + y);
      pg.rotate(position);

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

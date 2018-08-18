package com.giantrainbow.patterns;

import static processing.core.PConstants.CLAMP;
import static processing.core.PConstants.RGB;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.colors.Colors;
import com.giantrainbow.model.space.Lissajous;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import org.joml.sampling.Callback2d;
import org.joml.sampling.PoissonSampling.Disk;
import processing.core.PImage;

/**
 * SpinnyDiscs animates a number of variable sized, rotating color wheels in 2D space. They are
 * implemented as a texture mapped circle w/ alpha surround. The balls are positioned by a
 * https://en.wikipedia.org/wiki/Lissajous_curve.
 */
@LXCategory(LXCategory.FORM)
public class SpinnyDiscs extends CanvasPattern2D {
  public final float MAX_SIZE = 10;
  public final float BACKGROUND_SPEED = 10f;
  public final float BACKGROUND_SAT = 1;
  public final float BACKGROUND_BRIGHT = .1f;
  public final float MOVEMENT_RANGE = .5f; // Movement range as a ratio of width/height
  public final float MAX_ROTATE_SPEED = 100f; // Speed range
  public final float MAX_TRANSLATE_SPEED = 10f; // Speed range
  public final float MSHZ = 1 / 100000f; // Arbitrary slowdown of the millisecond counter

  public final int BALL_COUNT = 1000;

  // Speed determines the overall speed of the entire pattern.
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 5, 0, 10).setDescription("Speed");
  public final CompoundParameter sizeKnob =
      new CompoundParameter("Size", 5, 1, 10).setDescription("Size");
  public final CompoundParameter rangeKnob =
      new CompoundParameter("Range", .5, 0, 1).setDescription("Range");

  // Count determines the number of balls that render.  They are
  // animated continuously, so raising and lower the number will
  // show/hide them while they continue moving with the animation.
  public final CompoundParameter countKnob =
      new CompoundParameter("Count", 100, 1, BALL_COUNT).setDescription("Count");

  // The "a" paramter of a Lissajous curve that moves all the balls.
  public final CompoundParameter aKnob = new CompoundParameter("a", 5, -10, 10).setDescription("a");

  // The "b" paramter of a Lissajous curve that moves all the balls.
  public final CompoundParameter bKnob = new CompoundParameter("b", 4, -10, 10).setDescription("b");

  // The "delta" paramter of a Lissajous curve that moves all the balls.
  public final CompoundParameter deltaKnob =
      new CompoundParameter("delta", 0, -Math.PI / 2, Math.PI / 2).setDescription("delta");

  // The "rotate" paramter determines how fast the whole thing spins.
  public final CompoundParameter rotateKnob =
      new CompoundParameter("rotate", 0, -10, 10).setDescription("rotate");

  Ball balls[];
  float telapsed;
  float relapsed;
  PImage texture;

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

  float distance(float x0, float y0, float x1, float y1) {
    return (float) Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));
  }

  public SpinnyDiscs(LX lx) {
    super(lx);

    Random rnd = new Random();

    this.telapsed = 0;
    this.relapsed = 0;
    this.balls = new Ball[BALL_COUNT];
    this.texture = makeTexture();

    float radius = distance(0, 0, canvas.width() / 2, canvas.height());

    pg.textureWrap(CLAMP);

    int minDist = 20;
    for (; ; ) {
      ArrayList<Ball> list = new ArrayList<Ball>();
      new Disk(
          rnd.nextLong(),
          radius,
          minDist,
          25,
          new Callback2d() {
            public void onNewSample(float x, float y) {
              Ball ball = new Ball();
              ball.X = x;
              ball.Y = y;
              ball.A = 2 * radius * rnd.nextFloat() * MOVEMENT_RANGE;
              ball.B = 2 * radius * rnd.nextFloat() * MOVEMENT_RANGE;
              ball.R = MAX_SIZE * rnd.nextFloat();
              ball.ST = MAX_TRANSLATE_SPEED * 2f * (rnd.nextFloat() - 0.5f);
              ball.SR = MAX_ROTATE_SPEED * 2f * (rnd.nextFloat() - 0.5f);
              ball.Angle = (float) rnd.nextFloat() * (float) Math.PI;
              list.add(ball);
            }
          });

      if (list.size() < BALL_COUNT) {
        minDist /= 2;
        continue;
      }

      if (list.size() > BALL_COUNT * 1.25) {
        minDist *= 1.25;
        continue;
      }

      Collections.shuffle(list, rnd);

      balls = list.toArray(balls);
      break;
    }

    removeParameter(fpsKnob);
    addParameter(speedKnob);
    addParameter(countKnob);
    addParameter(aKnob);
    addParameter(bKnob);
    addParameter(deltaKnob);
    addParameter(rotateKnob);
    addParameter(sizeKnob);
    addParameter(rangeKnob);
  }

  public void draw(double deltaMs) {
    double speed = speedKnob.getValue();
    telapsed += (float) (deltaMs * speed);
    double rotate = rotateKnob.getValue();
    relapsed += (float) (deltaMs * rotate);

    pg.background(
        Colors.hsb(relapsed * BACKGROUND_SPEED * MSHZ, BACKGROUND_SAT, BACKGROUND_BRIGHT));

    pg.translate(canvas.width() / 2, 0);
    pg.rotate(relapsed * (float) rotateKnob.getValue() * MSHZ);

    for (int i = 0; i < countKnob.getValue(); i++) {
      if (i >= balls.length) {
        break;
      }
      balls[i].draw();
    }
  }

  float a() {
    return (float) aKnob.getValue();
  }

  float b() {
    return (float) bKnob.getValue();
  }

  public class Ball {
    float X; // Y location
    float Y; // X location
    float A; // Lissajous B
    float B; // Lissajous B
    float R; // Radius
    float ST; // Translation speed
    float SR; // Rotation speed
    float Angle; // Angle offset

    void draw() {
      pg.pushMatrix();

      float delta = (float) deltaKnob.getValue();
      float position = (float) telapsed * ST * MSHZ;
      float rotation = (float) (relapsed + telapsed) * SR * MSHZ;
      float range = (float) rangeKnob.getValue();
      float x = Lissajous.locationX(range * A, a(), delta, position);
      float y = Lissajous.locationY(range * B, b(), position);

      pg.rotate(Angle);
      pg.translate(x, y);
      pg.rotate(-Angle);
      pg.translate(X, Y);
      pg.rotate(rotation);

      pg.beginShape();
      pg.noStroke();

      pg.texture(texture);

      float w = canvas.width();
      float r = R * (float) sizeKnob.getValue();

      pg.vertex(-r, -r, 0, 0);
      pg.vertex(r, -r, w, 0);
      pg.vertex(r, r, w, w);
      pg.vertex(-r, r, 0, w);
      pg.endShape();

      pg.popMatrix();
    }
  };
}

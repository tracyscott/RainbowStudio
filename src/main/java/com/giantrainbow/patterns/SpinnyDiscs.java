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

// TODO Rotate each orbit w.r.t. the origin?
// TODO Swap positions when not overlapping.
// TODO Reject samples inside outer radius and inside inner radius?

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
  public final float BACKGROUND_BRIGHT = .15f;
  public final float MOVEMENT_RANGE = 0; // Movement range as a ratio of width/height
  public final float MAX_ROTATE_SPEED = 0f; // Speed range 20
  public final float MAX_TRANSLATE_SPEED = 0f; // Speed range 10
  public final float MSHZ = 1 / 100000f; // Arbitrary slowdown of the millisecond counter

  public final int BALL_COUNT = 1000;

  // Speed determines the overall speed of the entire pattern.
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 5, 0, 10).setDescription("Speed");
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
      new CompoundParameter("rotate", 0, -100, 100).setDescription("rotate");

  Ball balls[];
  float elapsed;
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

    this.elapsed = 0;
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
  }

  public void draw(double deltaMs) {
    double speed = speedKnob.getValue();
    elapsed += (float) (deltaMs * speed);

    pg.background(Colors.hsb(elapsed * BACKGROUND_SPEED * MSHZ, BACKGROUND_SAT, BACKGROUND_BRIGHT));

    pg.translate(canvas.width() / 2, 0);
    pg.rotate(elapsed * (float) rotateKnob.getValue() * MSHZ);

    for (int i = 0; i < countKnob.getValue(); i++) {
      if (i >= balls.length) {
        break;
      }
      balls[i].draw();
    }
  }

  float a() {
    return (float) (int) (aKnob.getValue() + 0.5);
  }

  float b() {
    return (float) (int) (bKnob.getValue() + 0.5);
  }

  public class Ball {
    float X;
    float Y;
    float A;
    float B;
    float R;
    float ST; // Translation speed
    float SR; // Rotation speed

    void draw() {
      pg.pushMatrix();

      float delta = (float) deltaKnob.getValue();
      float position = (float) elapsed * ST * MSHZ;
      float rotation = (float) elapsed * SR * MSHZ;
      float x = Lissajous.locationX(A, a(), delta, position);
      float y = Lissajous.locationY(B, b(), position);

      pg.translate(X + x, Y + y);
      pg.rotate(rotation);

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

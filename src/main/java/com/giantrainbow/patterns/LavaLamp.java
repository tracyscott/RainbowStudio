/*
 * Created by shawn on 8/4/18 10:54 AM.
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.RainbowStudio.pApplet;
import static com.giantrainbow.colors.Colors.BLACK;
import static com.giantrainbow.colors.Colors.WHITE;
import static processing.core.PApplet.constrain;
import static processing.core.PApplet.pow;
import static processing.core.PConstants.ARGB;
import static processing.core.PConstants.BLUR;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.RGB;
import static processing.core.PConstants.THRESHOLD;

import com.giantrainbow.colors.ColorRainbow;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BooleanParameter;
import processing.core.PImage;

/**
 * Based on: <a href="http://openprocessing.org/sketch/4675">Rorschach Generator</a>
 */
@LXCategory(LXCategory.FORM)
public class LavaLamp extends PGPixelPerfect {
  private static final float COLOR_CHANGE_TIME = 10.0f;
  private static final float DEFAULT_FPS = GLOBAL_FRAME_RATE;

  private static final int SEGMENT_W = 60;

  private static final int N_BALLS = 42;
  private static final float BOUNDARY_THRESHOLD = 0.1f;
  private static final float V_MAX = 3;

  private float[][] balls;  // i: x, y, vx, vy

  private int radius;
  private float speedScale;
  private PImage ballImage;

  // Color interpolation
  private ColorRainbow rainbow =
      new ColorRainbow(new ColorRainbow.NextRandomColor(6, COLOR_CHANGE_TIME, BLACK));

  private final BooleanParameter blackOnlyToggle =
      new BooleanParameter("B & W", false)
          .setDescription("Toggles black-and-white-only mode");

  public LavaLamp(LX lx) {
    super(lx, P2D);

    fpsKnob.addListener(lxParameter -> {
          if (lxParameter.getValue() > 0.0) {
            speedScale = 3.0f / fpsKnob.getValuef();
          }
        });

    addParameter(blackOnlyToggle);
  }

  @Override
  public void setup() {
    fpsKnob.setValue(DEFAULT_FPS);

    // Parameters
    radius = SEGMENT_W / 4;//(int) (pg.width / 10 * 1.2);
    speedScale = 3.0f / DEFAULT_FPS;

    balls = new float[N_BALLS][4];

    generateCircleImage();
    generateBalls();

    rainbow.reset(DEFAULT_FPS);
  }

  @Override
  public void tearDown() {
//    ballImage = null;
    balls = null;
  }

  /**
   * Returns the complement of a color.
   */
  private int complementColor(int c) {
    return (c & 0xff000000)
        | (0xff0000 - (c & 0xff0000))
        | (0xff00 - (c & 0xff00))
        | (0xff - (c & 0xff));
  }

  @Override
  protected void draw(double deltaDrawMs) {
    pg.colorMode(RGB, 255);
    int bgColor = blackOnlyToggle.getValueb()
        ? BLACK
        : rainbow.get(pg, fpsKnob.getValuef());
    int ballColor = complementColor(bgColor);

    moveBalls();

    pg.background(WHITE);
    pg.loadPixels();
    for (float[] ball : balls) {
      pg.image(ballImage, ball[0] - radius, ball[1] - radius);
    }

    pg.filter(THRESHOLD, BOUNDARY_THRESHOLD);

    //apply color changes
    pg.loadPixels();
    for (int i = pg.pixels.length; --i >= 0; ) {
      if (pg.pixels[i] == WHITE) {
        pg.pixels[i] = bgColor;
      } else {
        pg.pixels[i] = ballColor;
      }
    }
    pg.updatePixels();
    pg.filter(BLUR);
  }

  private void moveBalls() {
    for(float[] ball : balls) {
      if (ball[0] <= 0 || ball[0] >= pg.width) {
        ball[2] = -ball[2];
      }

      if (ball[1] <= 0 || ball[1] >= pg.height) {
        ball[3] = -ball[3];
      }

      ball[2] += pApplet.random(-0.1f, 0.1f);
      ball[3] += pApplet.random(-0.1f, 0.1f);
      ball[2] = constrain(ball[2], -V_MAX, V_MAX);
      ball[3] = constrain(ball[3], -V_MAX, V_MAX);

      ball[0] += ball[2] * speedScale;
      ball[1] += ball[3] * speedScale;
    }
  }

  private void generateCircleImage() {
    ballImage = pApplet.createImage(radius * 2, radius * 2, ARGB);
    for(int x = 0; x <= radius; x++) {
      for (int y = 0; y <= radius; y++) {
        float r2 = pow(x - radius, 2) + pow(y - radius, 2);
        if (r2 < radius * radius) {
          int c = pg.color(
              0, 0, 0,
              255 * (1 - r2/(radius*radius)));
          ballImage.set(x, y, c);
          ballImage.set(2*radius - x, y, c);
          ballImage.set(2*radius - x, 2 * radius - y, c);
          ballImage.set(x, 2*radius - y, c);
        } else {
          ballImage.set(x, y, pg.color(0, 0, 0, 0));
        }
      }
    }
  }

  private void generateBalls() {
    for (float[] ball : balls) {
      ball[0] = pApplet.random(radius, pg.width - radius);
      ball[1] = pApplet.random(radius, pg.height - radius);
      ball[2] = pApplet.random(-V_MAX, V_MAX);
      ball[3] = pApplet.random(-V_MAX, V_MAX);
    }
  }
}

/*
 * Created by shawn on 8/6/18 12:23 AM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.colors.Colors.BLACK;
import static com.giantrainbow.colors.Colors.WHITE;
import static processing.core.PApplet.round;
import static processing.core.PConstants.HSB;
import static processing.core.PConstants.P2D;

import com.giantrainbow.input.LowPassFilter;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;

/**
 * Draws the raw sound input.
 *
 * @author Shawn Silverman
 */
@LXCategory(LXCategory.FORM)
public class LineDance extends P3PixelPerfectBase {
  private static final float SENSITIVITY_SCALE = 20.0f;
  private static final float SENSITIVITY_VALUE = 0.7f;

  private static final long TINT_DELAY = 60;  // In ms

  private float[] sample;
  private float sensitivity;
  private boolean auto;

  private long lastTime; // For calculating dt
  private LowPassFilter autoSensitivityFilter = new LowPassFilter(AUTO_SENSITIVITY_TAU, SENSITIVITY_VALUE);
  private static final float AUTO_SENSITIVITY_TAU = 0.2f;
  private static final float MAX_AUTO_FILL = 0.9f;

  private final CompoundParameter blurKnob =
      new CompoundParameter("Blur", 0.75f)
          .setDescription("Blurs the wave");

  private long lastTintDelta;

  public LineDance(LX lx) {
    super(lx, P2D);

    addParameter(blurKnob);
  }

  @Override
  public void setup() {
    // Reset
    sensitivity = SENSITIVITY_VALUE;
    auto = true;
    autoSensitivityFilter.force(sensitivity);
    lastTintDelta = 0;

    // Setup
    sample = new float[inputManager().getAudioSampleSize()];

    pg.colorMode(HSB, 255);
    pg.background(BLACK);
  }

  @Override
  protected void draw(double deltaDrawMs) {
    // Experiments with blurring
//    pg.noStroke();
//    pg.fill(BLACK, (1.0f - blurKnob.getValuef())*255);
//    pg.rect(0, 0, pg.width, pg.height);
    if (blurKnob.getValuef() > 0.0f) {
      if (lastTintDelta >= TINT_DELAY) {
        pg.tint(pg.colorModeX * blurKnob.getValuef());
        pg.image(pg, -2, -2, pg.width + 2, pg.height + 2);
        pg.noTint();
        lastTintDelta = 0L;
      } else {
        lastTintDelta += deltaDrawMs;
      }
    } else {
      lastTintDelta = 0L;
      pg.background(BLACK);
    }
//    pg.tint(WHITE, 255);
//    pg.image(pg, -2, -2, pg.width, pg.height);
//    pg.noTint();
//    PImage img = pg.copy();
//    img.resize(pg.width/2, pg.height/2);
//    img.resize(pg.width - 2, pg.height - 2);
//    pg.tint(245, 250, 255);
//    pg.image(img, 0, 0);

    sample = inputManager().getAudioSample(sample);
    int n = sample.length;
    boolean silent = true;
    for (int i = n; --i >= 0; ) {
      if (sample[i] != 0) {
        silent = false;
        break;
      }
    }

    // Draw the line over top

    pg.stroke(WHITE);
    int yOff = (int) ((0.65f - 0.5f)*pg.height);
    float s = sample[0];
    float max = Math.abs(s);
    float y1 = pg.height*(1.0f - s*sensitivity*SENSITIVITY_SCALE)/2;
    for (int i = 1; i < pg.width; i++) {
      int index = round((float) (n - 1) * (float) i / (float) (pg.width - 1));
      s = sample[index];
      max = Math.max(max, Math.abs(s));
      float y2 = pg.height*(1.0f - s*sensitivity*SENSITIVITY_SCALE)/2;
      pg.line(i - 1, y1 + yOff, i, y2 + yOff);
      y1 = y2;
    }

    if (auto) {
      long time = System.currentTimeMillis();
      float dt = (time - lastTime)/1000.0f;
      lastTime = time;
      s = Math.min(MAX_AUTO_FILL / (max * SENSITIVITY_SCALE), 1.0f);
      sensitivity = autoSensitivityFilter.next(s, dt);
    }
  }
}

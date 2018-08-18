/*
 * Created by shawn on 8/4/18 12:02 AM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.colors.Colors.BLACK;
import static processing.core.PConstants.P2D;

import com.giantrainbow.colors.ColorRainbow;
import com.giantrainbow.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import processing.core.PVector;

/**
 * Randomly moving rainbow strands.
 *
 * @author Shawn Silverman
 */
@LXCategory(LXCategory.FORM)
public class SillyString extends P3PixelPerfectBase {
  private static final Logger logger = Logger.getLogger(SillyString.class.getName());

  private static final int NUM_PARTICLES = 35;
  private static final float MAX_V = 1000.0f;  // In pixels/second

  private static final float COLOR_CHANGE_TIME = 5.0f;
  private static final float PARTICLE_WEIGHT = 2.0f;

  private static final float NOISE_INC = 0.01f;

  private List<Particle> particles = new ArrayList<>();

  // Have the noise drift per frame
  private float noiseOffset;
  private float noiseOffsetInc;

  public SillyString(LX lx) {
    super(lx, P2D);

    fpsKnob.addListener(lxParameter -> setParams(lxParameter.getValuef()));
  }

  private void initNoise() {
    applet.noiseSeed(random.nextLong());
    noiseOffset = 0.0f;
  }

  private void setParams(float frameRate) {
    noiseOffsetInc = NOISE_INC * GLOBAL_FRAME_RATE / frameRate;

    for (Particle p : particles) {
      p.rainbow.reset(frameRate);
    }
  }

  @Override
  public void setup() {
    particles.clear();
    for (int i = 0; i < NUM_PARTICLES; i++) {
      particles.add(new Particle());
    }

    initNoise();
    setParams(fpsKnob.getValuef());
  }

  @Override
  public void tearDown() {
    particles.clear();
  }

  @Override
  protected void draw(double deltaMs) {
    if (noiseOffset == 0) {
      pg.background(BLACK);
    }

    pg.noStroke();
    pg.fill(BLACK, 10);
    pg.rect(0, 0, pg.width, pg.height);

    pg.strokeWeight(PARTICLE_WEIGHT);
    for (Particle p : particles) {
      float dt = (float) (deltaMs / 1000.0);
      p.update(dt);
      p.draw(dt);
    }

    // Increment the noise until we can't anymore
    // At this point, start anew
    float orig = noiseOffset;
    noiseOffset += noiseOffsetInc;
    if (noiseOffset == orig) {
      logger.info("Re-initializing noise");
      initNoise();
    }
  }

  private final class Particle {
    private PVector p;
    private PVector v;

    ColorRainbow rainbow =
          new ColorRainbow(new ColorRainbow.NextArrayColor(
              Colors.RAINBOW_PALETTE, COLOR_CHANGE_TIME, true));

    Particle() {
      p = new PVector(random.nextInt(pg.width), random.nextInt(pg.height));
      v = new PVector(0.0f, 0.0f);
    }

    private float field(float x) {
      return 2.0f*(applet.noise(x*NOISE_INC + noiseOffset) - 0.5f);
    }

    void update(float dt) {
      // Scale the velocity so the speed stays constant

      v.x = MAX_V * speedKnob.getValuef() * field(p.y);
      v.y = MAX_V * speedKnob.getValuef() * field(p.x);
      p.add(PVector.mult(v, dt));

      // Wrapping
      if (p.x < 0.0f) {
        p.x += pg.width;
      }
      if (p.x >= pg.width) {
        p.x -= pg.width;
      }
      if (p.y < 0.0f) {
        p.y += pg.height;
      }
      if (p.y >= pg.height) {
        p.y -= pg.height;
      }
    }

    /**
     * Draws the particle. This sets the stroke color.
     *
     * @param dt the time delta
     */
    void draw(float dt) {
      int stroke = rainbow.get(pg, fpsKnob.getValuef());
      pg.stroke(stroke);
      if (v.x == 0.0f && v.y == 0.0f) {
        // This is necessary for when the particles are stopped
        // Lines of zero length apparently don't draw just one pixel
        pg.point(p.x, p.y);
      } else {
        pg.line(p.x, p.y, p.x - v.x*dt, p.y - v.y*dt);
      }
    }
  }
}

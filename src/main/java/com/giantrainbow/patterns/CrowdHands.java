/*
 * Created by shawn on 8/6/18 12:02 AM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.inputManager;
import static com.giantrainbow.RainbowStudio.pApplet;
import static processing.core.PApplet.lerp;
import static processing.core.PApplet.max;
import static processing.core.PApplet.round;
import static processing.core.PConstants.HSB;
import static processing.core.PConstants.P2D;

import com.giantrainbow.input.InputManager;
import com.giantrainbow.input.LowPassFilter;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Hands bopping to the music.
 *
 * @author Shawn Silverman
 */
@LXCategory(LXCategory.FORM)
public class CrowdHands extends PGPixelPerfect {
  // Usable screen areas

  private static final float START_X = 0.15f;
  private static final float END_X   = 0.85f;
  private static final float START_Y = 0.2f;
  private static final float END_Y   = 1.0f;

  private PImageWrapper handsImage;

  private float tau;
  private long lastTime;
  private Queue<Hands> handsQueue = new LinkedList<>();

  // Use parameters to make it look good on the display
  private float startX;
  private float binSize;

  private InputManager.Beats beats;

  /**
   * Wraps a {@link PImage} so that it can be scaled properly when it's loaded.
   */
  private final class PImageWrapper {
    PImage img;
    boolean done;

    PImageWrapper(PImage img) {
      this.img = img;
    }

    /**
     * Initializes the image and returns whether the initialization was successful
     * and the image can be used. This returns {@code true} if the image is already
     * initialized.
     */
    boolean init() {
      if (done) {
        return true;
      }
      if (img.width <= 0) {
        return false;
      }

      float imageW = round(pg.width / 14.0f);
      float scale = imageW / img.width;
      float imageH = scale * img.height;
      img.resize(round(imageW), round(imageH));

      done = true;
      return true;
    }
  }

  /**
   * Represents one pair of hands.
   */
  private final class Hands {
    private LowPassFilter filter;

    /** Which of the beat levels this representing, high, mid, or low, for example. */
    private int beatIndex;

    /** A random X-position, in the range 0-1. */
    private float xPos;

    private float tintHue;
    private int tintColor;

    Hands(float tau, int beatIndex, float level) {
      filter = new LowPassFilter(tau, level);
      this.beatIndex = beatIndex;
      this.xPos = random.nextFloat();
      this.tintHue = random.nextFloat();
    }

    void draw(PGraphics pg, float level) {
      if (!handsImage.init()) {
        return;
      }
      tintColor = pg.color(tintHue, 1.0f, 1.0f);
      pg.tint(tintColor);
      pg.image(
          handsImage.img,
          startX + (beatIndex + xPos)*binSize - handsImage.img.width/2.0f,
          pg.height * lerp(END_Y, START_Y, level),
          handsImage.img.width, handsImage.img.height);
      pg.noTint();
    }
  }

  public CrowdHands(LX lx) {
    super(lx, P2D);
  }

  @Override
  public void onActive() {
    fpsKnob.setValue(60);

    pg.beginDraw();
    pg.colorMode(HSB, 1.0f);
    pg.endDraw();

    tau = inputManager.getAudioSampleSize() / inputManager.getAudioSampleRate() * 30;
    lastTime = 0L;
    handsQueue.clear();

    // Image from:
    // http://www.ghostride.com/body-parts/cartoon-hands.html
    // http://www.ghostride.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/b/p/bp_hands-cartoon.jpg
    handsImage = new PImageWrapper(pApplet.requestImage("img/bp_hands-cartoon_transparent.png"));

    startX = START_X * pg.width;
    binSize = (END_X - START_X)*pg.width/3.0f;

    beats = inputManager.getBeats();
  }

  @Override
  protected void draw(double deltaDrawMs) {
    long time = System.currentTimeMillis();

    pg.background(0);

    // Handle the first-time case
    if (lastTime == 0) {
      lastTime = time;
      return;
    }

    // Get all the scaled beat levels
    beats = inputManager.getBeats(beats);

    // dt
    float dt = (time - lastTime)/1000.0f;
    lastTime = time;

    // Draw all the current hands and collect the max. beat levels
    float[] maxLevels = new float[InputManager.Beats.beatsCount()];
    for (Iterator<Hands> iter = handsQueue.iterator(); iter.hasNext(); ) {
      Hands hands = iter.next();

      int beatIndex = hands.beatIndex;
      float level = hands.filter.next(beats.isBeat(beatIndex) ? 1.0f : 0.0f, dt);
      maxLevels[beatIndex] = max(maxLevels[beatIndex], level);

      // The level practically won't ever reach zero, so use 1 pixel height as the threshold
      // and solve for the linear interpolation
      if (level >= (END_Y - (float) (pg.height - 1)/(float) pg.height)/(END_Y - START_Y)) {
        hands.draw(pg, level);
      } else {
        iter.remove();
      }
    }

    for (int i = 0; i < InputManager.Beats.beatsCount(); i++) {
      if (beats.isBeat(i)) {
        Hands hands = new Hands(tau, i, 1.0f);
        hands.draw(pg, 1.0f);
        handsQueue.add(hands);
      }
    }
  }
}

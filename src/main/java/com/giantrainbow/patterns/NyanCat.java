/*
 * Created by shawn on 8/7/18 10:44 PM.
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.colors.Colors.WHITE;
import static processing.core.PApplet.map;
import static processing.core.PApplet.max;
import static processing.core.PConstants.P2D;

import com.giantrainbow.PathUtils;
import com.giantrainbow.RainbowStudio;
import com.giantrainbow.UtilsForLX;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import processing.core.PImage;

// Useful links:
// * http://blog.boreal-kiss.net/2012/09/25/scalable-nyan-cat-animation/
// * https://youtu.be/QH2-TGUlwu4

// Star states:
//
//                      *         *         *
//             *        *       *   *
//      *      *
// *   * *   ** **   ** * **   *     *   *     *
//      *      *
//             *        *       *   *
//                      *         *         *

/**
 * Nyan Cat.
 *
 * @author Shawn Silverman
 */
@LXCategory(LXCategory.FORM)
public class NyanCat extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(NyanCat.class.getName());

  private static final int BACKGROUND_COLOR = 0xff0f4d8f;

  private static final float FPS = 6;
  private static final long MS_PER_FRAME = (long) (1000 / FPS);
  private static final float CAT_SPEED = 30;

  private static final int RAINBOW_SEGMENT_COUNT = 18;
  private static final int RAINBOW_SEGMENT_WIDTH = 8;
  private static final int RAINBOW_BAND_HEIGHT = 3;
  private static final int RAINBOW_CAT_INTERSECT = 8;

  // Rainbow colors
  private static final int[] RAINBOW_COLORS = {
      0xffff0000,  // RED
      0xffff9900,  // ORANGE
      0xffffff00,  // YELLOW
      0xff33ff00,  // GREEN
      0xff0099ff,  // BLUE
      0xff6633ff,  // PURPLE
  };

  private static final String SOUND_FILE = "sounds/nyancat.wav";
  private static final String SPRITE_FILE = "images/nyancat.gif";

  private PImage[] catFrames;
  private int maxImgWidth;
  private List<Star> stars = new ArrayList<>();
  private Cat cat = new Cat();
  private Rainbow rainbow = new Rainbow();

  private float catX;
  private float catY;

  private long lastUpdateDelta;

  private File audioFile;

  public NyanCat(LX lx) {
    super(lx, P2D);

    catFrames = PathUtils.loadSprite(RainbowStudio.pApplet, SPRITE_FILE);
    maxImgWidth = 0;
    int maxImgHeight = 0;
    for (PImage img : catFrames) {
      img.resize(img.width/8, img.height/8);
      maxImgWidth = max(maxImgWidth, img.width);
      maxImgHeight = max(maxImgHeight, img.height);
    }

    catX = -maxImgWidth;
    catY = (pg.height - maxImgHeight)/2;

    // Write the audio to a temp file because LX's audio engine only works with File
    audioFile = UtilsForLX.copyAudioForOutput(RainbowStudio.pApplet, SOUND_FILE, lx.engine.audio.output);
  }

  @Override
  public void setup() {
    fpsKnob.setValue(GLOBAL_FRAME_RATE);

    // Create fresh stars
    stars.clear();
    int x = 0;
    while (x < pg.width) {
      x += random.nextInt(maxImgWidth*2/3) + maxImgWidth/3;
      int y = random.nextInt(pg.height);
      stars.add(new Star(x, y, random.nextFloat() < 0.1f));
    }

    // Reset the delta timer
    lastUpdateDelta = 0L;

    // Start the audio
    if (audioFile != null) {
      logger.info("Starting audio: " + audioFile);
      lx.engine.audio.output.file.setValue(audioFile.getName());
      lx.engine.audio.output.looping.setValue(true);
      lx.engine.audio.output.play.setValue(true);
    }
  }

  @Override
  public void tearDown() {
    stars.clear();

    // Stop the audio
    if (audioFile != null) {
      logger.info("Stopping audio: " + audioFile);
      lx.engine.audio.output.play.setValue(false);
      lx.engine.audio.output.file.setValue("");
      lx.engine.audio.output.looping.setValue(false);
    }
  }

  @Override
  protected void draw(double deltaDrawMs) {
    lastUpdateDelta += deltaDrawMs;

    pg.background(BACKGROUND_COLOR);
    if (lastUpdateDelta >= MS_PER_FRAME) {
      stars.forEach(Star::update);
      rainbow.update();
      cat.update();
      lastUpdateDelta = 0;
    }

    // Draw all the parts
    stars.forEach(Star::draw);
    rainbow.draw();
    cat.draw(catX, catY);

    // Update the cat position
    catX += CAT_SPEED / fpsKnob.getValuef();
    if (catX - RAINBOW_SEGMENT_COUNT*RAINBOW_SEGMENT_WIDTH + RAINBOW_CAT_INTERSECT >= pg.width) {
      catX = -maxImgWidth;
    }
  }


  /**
   * Handles cat state.
   */
  private final class Cat {
    private int state;

    void draw(float x, float y) {
      if (state < 0 || catFrames.length <= state) {
        return;
      }
      pg.image(catFrames[state], x, y);
    }

    void update() {
      state = (state + 1)%catFrames.length;
    }
  }

  private final class Rainbow {
    private int state;

    void draw() {
      boolean down = (state/2 % 2 == 0);
      float x = catX + RAINBOW_CAT_INTERSECT;
      pg.noStroke();
      for (int i = 0; i < RAINBOW_SEGMENT_COUNT; i++) {
        x -= RAINBOW_SEGMENT_WIDTH;
        if (x + RAINBOW_SEGMENT_WIDTH <= 0) {
          // Break early if we're not drawing this segment
          break;
        }

        float y = catY;
        if (down) {
          y++;
        }
        float alpha = 255.0f;
        if (i > RAINBOW_SEGMENT_COUNT/2) {
          alpha = map(i, RAINBOW_SEGMENT_COUNT/2, RAINBOW_SEGMENT_COUNT - 1, 255.0f, 25.0f);
        }
        for (int j = 0; j < RAINBOW_COLORS.length; j++) {
          pg.fill(RAINBOW_COLORS[j], alpha);
          pg.rect(x, y + j*RAINBOW_BAND_HEIGHT, RAINBOW_SEGMENT_WIDTH, RAINBOW_BAND_HEIGHT);
        }
        down = !down;
      }
    }

    void update() {
      state = (state + 1)%catFrames.length;
    }
  }

  /**
   * Handles the state for one star.
   */
  private final class Star {
    private static final int STATE_COUNT = 6;

    private final int[][][] LOCATIONS = {
        { { 0, 0 }, },
        { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 }, },
        { { 1, 0 }, { 2, 0 }, { 0, 1 }, { 0, 2 }, { -1, 0 }, { -2, 0 }, { 0, -1 }, { 0, -2 }, },
        {
            { 0, 0 },
            { 2, 0 }, { 3, 0 },
            { 0, 2 }, { 0, 3 },
            { -2, 0 }, { -3, 0 },
            { 0, -2 }, { 0, -3 },
        },
        { { 3, 0 }, { 2, 2 }, { 0, 3 }, { -2, 2 }, { -3, 0 }, { -2, -2 }, { 0, -3 }, { 2, -2 }, },
        { { 3, 0 }, { 0, 3 }, { -3, 0 }, { 0, -3 }, },
    };

    private final int x;
    private final int y;
    private final boolean backwards;  // Direction
    private int state;

    Star(int x, int y, boolean backwards) {
      this.x = x;
      this.y = y;
      this.backwards = backwards;

      // Start in a random state
      state = random.nextInt(STATE_COUNT);
    }

    void draw() {
      pg.stroke(WHITE);
      int[][] points = LOCATIONS[state];
      for (int[] p : points) {
        pg.point(x + p[0], y + p[1]);
      }
    }

    void update() {
      if (backwards) {
        state--;
        if (state < 0) {
          state = STATE_COUNT - 1;
        }
      } else {
        state = (state + 1)%STATE_COUNT;
      }
    }
  }
}

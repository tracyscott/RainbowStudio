/*
 * Created by shawn on 8/4/18 12:50 PM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.RainbowStudio.inputManager;
import static com.giantrainbow.RainbowStudio.pApplet;
import static processing.core.PApplet.floor;
import static processing.core.PApplet.max;
import static processing.core.PApplet.min;
import static processing.core.PApplet.round;
import static processing.core.PConstants.P2D;

import com.giantrainbow.input.InputManager;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import java.util.logging.Logger;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * A moving checkerboard.
 *
 * @author Shawn Silverman
 */
@LXCategory(LXCategory.FORM)
public class CheckerMove extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(CheckerMove.class.getName());

  private static final int INDIAN_RED = 0xffcd5c5c;
  private static final int LIGHT_SKY_BLUE = 0xff87cefa;

  private static final int COLOR_1 = INDIAN_RED;
  private static final int COLOR_2 = LIGHT_SKY_BLUE;

  private static final int CHOOSE_STATE = 0;
  private static final int MOVE_STATE = 1;

  private static final int SQUARE_SIZE_DIVISOR = 420 / 6;

  /** The move time, in ms. */
  private static final int MOVE_TIME = 200;//750 * 2 / 7;

  /** How long before tapping randomly. */
  private static final int BEATS_INPUT_TIMEOUT = 5000;

  private boolean reset;
  private int state;
  private PGraphics screen;
  private PImage moveImage;
  private float moveTime;  // In seconds

  private int squareSize;
  private int squaresW;
  private int squaresH;

  // The current move state
  private int edge;  // 0-3, counterclockwise starting from the right edge
  private int barStart;  // In squares
  private int barWidth;  // In squares
  private int movePixels;
  private int movePixelsTotal;
  private int pixelsPerFrame;

  private InputManager.Beats beats;
  private long lastBeatsTime;
  private boolean beatsNotRandom;  // Indicates whether we are responding to beats

  public CheckerMove(LX lx) {
    super(lx, P2D);
  }

  @Override
  public void onActive() {
    fpsKnob.setValue(GLOBAL_FRAME_RATE);

    pg.beginDraw();
    pg.noSmooth();
    pg.endDraw();

    squareSize = (pg.width + SQUARE_SIZE_DIVISOR - 1)/SQUARE_SIZE_DIVISOR;
    squaresW = (pg.width + squareSize - 1)/squareSize;
    squaresH = (pg.height + squareSize - 1)/squareSize;
    logger.info("squareSize=" + squareSize + " squares=(" + squaresW + ", " + squaresH + ")");

    screen = pApplet.createGraphics(squaresW*squareSize, squaresH*squareSize, P2D);
    screen.beginDraw();
    screen.noStroke();
    screen.noSmooth();
    screen.endDraw();

    // Note that using an image makes the alpha get weird when copying between the graphics
    // context and the image; thus using a Graphics context
    moveImage = pApplet.createGraphics(screen.width, screen.height, P2D);
    moveTime = MOVE_TIME;

    // Reset
    reset = true;
    state = CHOOSE_STATE;

    beats = inputManager.getBeats();
    lastBeatsTime = 0L;
    beatsNotRandom = false;
  }

  @Override
  public void onInactive() {
    screen.dispose();
    ((PGraphics) moveImage).dispose();
  }

  @Override
  protected void draw(double deltaDrawMs) {
    if (reset) {
      reset = false;
      int halfWidth = round(pg.width/(2.0f*squareSize))*squareSize;
      screen.beginDraw();
      screen.fill(COLOR_1);
      screen.rect(0, 0, halfWidth, screen.height);
      screen.fill(COLOR_2);
      screen.rect(halfWidth, 0, screen.width - halfWidth, screen.height);
      screen.endDraw();
      moveImage.copy(screen, 0, 0, screen.width, screen.height, 0, 0, screen.width, screen.height);
    }

    switch (state) {
      case CHOOSE_STATE:
        // Pick an edge, width, and direction

        barWidth = -1;
        long time = System.currentTimeMillis();
        if (time - lastBeatsTime >= BEATS_INPUT_TIMEOUT) {
          if (beatsNotRandom) {
            beatsNotRandom = false;
            logger.info("No beats detected");
          }
          if (random.nextFloat() < 0.1f) {
            float f = random.nextFloat();
            if (f < 0.5f) {
              barWidth = 3;
            } else if (f < 0.8) {
              barWidth = 2;
            } else {
              barWidth = 1;
            }
          }
        }
        for (int i = 0; i < 3; i++) {
          if (beats.isBeat(i)) {
            lastBeatsTime = time;
            beatsNotRandom = true;
            barWidth = 3 - i;
            break;
          }
        }

        if (barWidth < 0) {
          break;
        }

        edge = random.nextInt(4);
        int moveCount;
        if (edge % 2 == 0) {
          int n = max(1, squaresH - barWidth + 1);
          barStart = random.nextInt(n);
          moveCount = (int) (moveTime * fpsKnob.getValuef() / 1000.0f / squareSize);//squaresW / 14;
        } else {
          int n = max(1, squaresW - barWidth + 1);
          barStart = random.nextInt(n);
          moveCount = (int) (moveTime * fpsKnob.getValuef() / 1000.0f / squareSize);//squaresH / 4;
        }

        barStart *= squareSize;
        barWidth *= squareSize;
        movePixelsTotal = moveCount * squareSize;

        // Pixels per frame = pixels-per-s * s-per-frame
        if (moveTime > 0) {
          pixelsPerFrame = max(1, floor((float) movePixelsTotal / moveTime / fpsKnob.getValuef()));
        } else {
          pixelsPerFrame = 1;
        }
        movePixels = 0;

        moveImage.copy(screen, 0, 0, screen.width, screen.height, 0, 0, screen.width, screen.height);
        state = MOVE_STATE;
        break;

      case MOVE_STATE:
        movePixels = min(movePixelsTotal, movePixels + pixelsPerFrame);
        int n = movePixels;
        switch (edge) {
          case 0:  // Move left
            screen.copy(moveImage,
                n, barStart, screen.width - n, barWidth,
                0, barStart, screen.width - n, barWidth);
            screen.copy(moveImage,
                0,                barStart, n, barWidth,
                screen.width - n, barStart, n, barWidth);
            break;
          case 1:  // Move down
            screen.copy(moveImage,
                barStart, 0, barWidth, screen.height - n,
                barStart, n, barWidth, screen.height - n);
            screen.copy(moveImage,
                barStart, screen.height - n, barWidth, n,
                barStart, 0,                 barWidth, n);
            break;
          case 2:  // Move right
            screen.copy(moveImage,
                0, barStart, screen.width - n, barWidth,
                n, barStart, screen.width - n, barWidth);
            screen.copy(moveImage,
                screen.width - n, barStart, n, barWidth,
                0,                barStart, n, barWidth);
            break;
          case 3:  // Move up
            screen.copy(moveImage,
                barStart, n, barWidth, screen.height - n,
                barStart, 0, barWidth, screen.height - n);
            screen.copy(moveImage,
                barStart, 0,                 barWidth, n,
                barStart, screen.height - n, barWidth, n);
            break;
        }

        if (movePixels >= movePixelsTotal) {
          state = CHOOSE_STATE;
          break;
        }
        break;
    }

    pg.copy(screen,
        screen.width - pg.width/2, 0, pg.width/2, pg.height,
        0, 0, pg.width/2, pg.height);
    pg.copy(screen,
        0, 0, pg.width/2, pg.height,
        pg.width/2, 0, pg.width/2, pg.height);
  }
}

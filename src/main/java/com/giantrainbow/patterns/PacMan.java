package com.giantrainbow.patterns;

import static com.giantrainbow.colors.Colors.BLACK;
import static com.giantrainbow.colors.Colors.BLUE;
import static com.giantrainbow.colors.Colors.RED;
import static com.giantrainbow.colors.Colors.WHITE;
import static processing.core.PApplet.radians;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PConstants;

/**
 * PacMan is just minding his own business trying to gobble up some balls and
 * along comes a fucking ghost trying to kill his ass!  Will our hero survive?
 * Tune in and find out...
 */
@LXCategory(LXCategory.FORM)
public class PacMan extends PGPixelPerfect {

  private static final int B = BLACK;
  private static final int U = BLUE;
  private static final int W = WHITE;
  private static final int R = RED;

  private static final int[][] GHOST = new int[][] {
      { B, B, B, B, B, B, B, B, B, B, B, B, B, B, B, B },
      { B, B, B, B, B, B, R, R, R, R, B, B, B, B, B, B },
      { B, B, B, B, R, R, R, R, R, R, R, R, B, B, B, B },
      { B, B, B, R, R, R, R, R, R, R, R, R, R, B, B, B },
      { B, B, R, W, W, R, R, R, R, W, W, R, R, R, B, B },
      { B, B, W, W, W, W, R, R, W, W, W, W, R, R, B, B },
      { B, B, U, U, W, W, R, R, U, U, W, W, R, R, B, B },
      { B, R, U, U, W, W, R, R, U, U, W, W, R, R, R, B },
      { B, R, R, W, W, R, R, R, R, W, W, R, R, R, R, B },
      { B, R, R, R, R, R, R, R, R, R, R, R, R, R, R, B },
      { B, R, R, R, R, R, R, R, R, R, R, R, R, R, R, B },
      { B, R, R, R, R, R, R, R, R, R, R, R, R, R, R, B },
      { B, R, R, R, R, R, R, R, R, R, R, R, R, R, R, B },
      { B, R, R, B, R, R, R, B, B, R, R, R, B, R, R, B },
      { B, R, B, B, B, R, R, B, B, R, R, B, B, B, R, B },
      { B, B, B, B, B, B, B, B, B, B, B, B, B, B, B, B },
  };

  private static final int BITE_FRAMES = 6;
  private static final int BITE_DEGREES_PER_FRAME = 90 / BITE_FRAMES;
  private static final int PAC_RADIUS = 16;
  private static final int BALL_RADIUS = 4;
  private static final int BALL_SPACING = PAC_RADIUS;

  private final int numBalls = pg.width / BALL_SPACING;

  private int x = 0, b = 0;
  private boolean closing = true;

  public PacMan(LX lx) {
    super(lx, null);
  }

  @Override
  protected void draw(double deltaDrawMs) {
    pg.background(0);
    pg.colorMode(PConstants.HSB, 1000);
    drawPac();
    drawWallz();
    drawBallz();
    drawGhost();
  }

  private void drawPac() {
    pg.stroke(166, 1000, 1000);
    pg.fill(166, 1000, 1000);
    int bite = b * BITE_DEGREES_PER_FRAME;
    if (b == BITE_FRAMES) {
      closing = false;
    } else if (b < 0) {
      closing = true;
    }
    b += closing ? 1 : -1;
    pg.arc(x++, PAC_RADIUS, PAC_RADIUS, PAC_RADIUS, radians(45 - bite), radians(315 + bite));
    if (x > pg.width + PAC_RADIUS * 5) x = -PAC_RADIUS;
  }

  private void drawWallz() {
    pg.stroke(BLUE);
    pg.line(0, 0, pg.width, 0);
    pg.line(0, 2, pg.width, 2);
    pg.line(0, pg.height - 3, pg.width, pg.height - 3);
    pg.line(0, pg.height - 1, pg.width, pg.height - 1);
  }

  private void drawBallz() {
    pg.stroke(WHITE);
    pg.fill(WHITE);
    for (int i = 0; i <= numBalls; i++) {
      int ballX = i * BALL_SPACING;
      if (ballX > x) {
        pg.arc(ballX, PAC_RADIUS, BALL_RADIUS, BALL_RADIUS, radians(0), radians(360));
      }
    }
  }

  private void drawGhost() {
    for (int i = 0; i < GHOST.length; i++) {
      for (int j = 0; j < GHOST[i].length; j++) {
        int ghostDistance = pg.width - x + PAC_RADIUS * 3;
        pg.stroke(GHOST[i][j]);
        pg.point(j + x - ghostDistance, i + PAC_RADIUS / 2);
      }
    }
  }
}

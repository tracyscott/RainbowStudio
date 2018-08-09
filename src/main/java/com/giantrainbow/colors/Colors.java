/*
 * Created by shawn on 8/4/18 12:15 AM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.colors;

import java.awt.Color;
import java.util.Random;

/**
 * Utility methods for colors.
 *
 * @author Shawn Silverman
 */
public class Colors {
  static final Random rand = new Random();

  // Useful links:
  // https://www.instructables.com/id/How-to-Make-Proper-Rainbow-and-Random-Colors-With-/

  // Rainbow colors plus some auxiliaries
  public static final int BLACK = 0xff000000;
  public static final int RED = 0xffff0000;
  public static final int ORANGE = 0xffff7f00;
  public static final int YELLOW = 0xffffff00;
  public static final int GREEN = 0xff00ff00;
  public static final int BLUE = 0xff0000ff;
  public static final int VIOLET = 0xff8b00ff;
  public static final int WHITE = 0xffffffff;

  // https://simple.wikipedia.org/wiki/Rainbow
  public static final int[] RAINBOW_PALETTE = {
    RED, ORANGE, YELLOW, GREEN, BLUE, VIOLET,
  };

  // https://www.webnots.com/vibgyor-rainbow-color-codes/
  public static final int[] RAINBOW2_PALETTE = {
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    0xff4b0082, // Indigo
    0xff9400d3, // Violet
  };

  // http://www.color-hex.com/color-palette/7094
  public static final int[] RAINBOW3_PALETTE = {
    0xffff1a00, // Red
    0xffff8d00, // Orange
    0xffe3ff00, // Yellow
    0xff00ff04, // Green
    0xff0051ff, // Blue
  };

  private Colors() {}

  /**
   * Chooses a random n-bit color. If {@code bits} is not in the range 1-8 then 8 will be assumed.
   *
   * @param bits the top number of random bits in an 8-bit color
   */
  public static int randomColor(int bits) {
    if (bits < 1 || 8 < bits) {
      bits = 8;
    }

    // Choose from an n-bit set of random colors
    int c = 0xff;
    for (int i = 0; i < 3; i++) {
      c = (c << 8) | (rand.nextInt(1 << bits) << (8 - bits));
    }
    return c;
  }

  /** Returns the red part of a 32-bit RGBA color. */
  public static int red(int color) {
    return (color >> 16) & 0xff;
  }

  /** Returns the green part of a 32-bit RGBA color. */
  public static int green(int color) {
    return (color >> 8) & 0xff;
  }

  /** Returns the blue part of a 32-bit RGBA color. */
  public static int blue(int color) {
    return color & 0xff;
  }

  /** Returns the alpha part of a 32-bit RGBA color. */
  public static int alpha(int color) {
    return (color >> 24) & 0xff;
  }

  /** Returns a color constructed from the three components. The alpha component is set to 255. */
  public static int rgb(int r, int g, int b) {
    return 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
  }

  /** Returns a color constructed from the three components. The alpha component is set to 255. */
  public static int hsb(float h, float s, float b) {
    return Color.HSBtoRGB(h, s, b);
  }
}

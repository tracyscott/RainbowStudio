/*
 * Created by shawn on 8/4/18 12:15 AM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.colors;

import java.util.Random;

/**
 * Utility methods for colors.
 *
 * @author Shawn Silverman
 */
public class Colors {
  private static final Random rand = new Random();

  private Colors() {
  }

  /**
   * Chooses a random n-bit color. If {@code bits} is not in the range 1-8 then 8 will
   * be assumed.
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
}

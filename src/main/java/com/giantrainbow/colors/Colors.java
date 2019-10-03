/*
 * Created by shawn on 8/4/18 12:15 AM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.colors;

import heronarts.lx.color.LXColor;

import java.awt.Color;
import java.util.Random;

/**
 * Utility methods for colors.
 *
 * @author Shawn Silverman
 */
public final class Colors {
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
      RED,
      ORANGE,
      YELLOW,
      GREEN,
      BLUE,
      VIOLET,
  };

  // https://www.webnots.com/vibgyor-rainbow-color-codes/
  public static final int[] RAINBOW2_PALETTE = {
      RED,
      ORANGE,
      YELLOW,
      GREEN,
      BLUE,
      0xff4b0082,  // Indigo
      0xff9400d3,  // Violet
  };

  // http://www.color-hex.com/color-palette/7094
  public static final int[] RAINBOW3_PALETTE = {
      0xffff1a00,  // Red
      0xffff8d00,  // Orange
      0xffe3ff00,  // Yellow
      0xff00ff04,  // Green
      0xff0051ff,  // Blue
  };

  public static final int[] PALETTE_STARRY_NIGHT = {
      0xff263c8b,
      0xff4e74a6,
      0xff8dbf78,
      0xffbfa524,
      0xff2e231f,
  };

  public static final int[] PALETTE_MONA_LISA = {
      0xff0f0b26,
      0xff522421,
      0xff8c5a2e,
      0xffbf8641,
      0xffb3b372,
  };

  public static final int[] PALETTE_SCREAM = {
      0xff4d7186,
      0xff284253,
      0xffe0542e,
      0xfff4a720,
      0xffef8c12,
  };

  public static final int[] PALETTE_LAST_SUPPER = {
      0xff3c535e,
      0xff252d2a,
      0xfff9d882,
      0xff3f422e,
      0xff261901,
  };

  public static final int[] PALETTE_SEURAT_SUNDAY_AFTERNOON = {
      0xff514264,
      0xff527e8e,
      0xff8db0a7,
      0xff989a55,
      0xff255c3f,
  };

  public static final int[] PALETTE_ROCKWELL_OPTOMETRIST = {
      0xff415e79,
      0xff637771,
      0xffe0c797,
      0xffb78e5d,
      0xff684f2c,
  };

  public static final int[] PALETTE_KANAGA_WAVE = {
      0xff011640,
      0xff2d5873,
      0xff7ba696,
      0xffbfba9f,
      0xffbf9663,
  };

  public static final int[] PALETTE_KLIMT_KISS = {
      0xff593202,
      0xff47421d,
      0xff346c36,
      0xffa1700f,
      0xfff2c641,
  };

  public static final int [] PALETTE_DALI_MEMORY = {
      0xff3698bf,
      0xffd9d3b4,
      0xffd97c2b,
      0xffa63921,
      0xff441d0d,
  };

  public static final int[] PALETTE_MONET_WATER_LILIES = {
      0xff345573,
      0xff6085a6,
      0xff4c6f73,
      0xff6f8c51,
      0xfff2dc6d,
  };

  //
  // Blink Festival color palette.
  //    The first two palettes are for LOGO treatments.
  //    The secondary palette supports gradients.  The
  //    gradient should always be from one color to the
  //    next color, not to the previous color, nor between
  //    two distant colors.
  //

  // The magenta for this treatment should come from PALETTE_BLINK_LOGO_2_COLOR[1]
  // The yellow for the Iris should come from PALETTE_BLINK_SECONDARY[4]
  public static final int[] PALETTE_BLINK_LOGO_FULL = {
      0xffee3524,  // red arts wave logo
      0xff512873,  // purple for magenta to purple eye horizontal gradient
      0xffa1a1a6,  // cool gray 8 for Illuminated by?
      0xffbec0c2,  // cool gray 3
  };

  public static final int[] PALETTE_BLINK_LOGO_2_COLOR = {
      0xff0f0708,  // black text white background or black background with white text
      0xffd20f8c,  // magenta eye with black or white iris
  };

  public static final int[] PALETTE_BLINK_PRIMARY = {
      0xffffffff,  // white
      0xff0f0708,  // rich black
  };

  public static final int[] PALETTE_BLINK_SECONDARY = {
      0xffd20f8c,  // magenta
      0xff5a3795,  // purple
      0xff00b4eb,  // blue
      0xff80c342,  // green
      0xffeee809,  // yellow
      0xffee3124,  // red
  };

  public static final int[] PALETTE_BLINK_TERTIARY = {
      0xffdcddde,   // grey
  };



  public static final int[][] ART_PALETTES ={
      PALETTE_DALI_MEMORY,
      PALETTE_KANAGA_WAVE,
      PALETTE_KLIMT_KISS,
      PALETTE_LAST_SUPPER,
      PALETTE_MONA_LISA,
      PALETTE_MONET_WATER_LILIES,
      PALETTE_ROCKWELL_OPTOMETRIST,
      PALETTE_SCREAM,
      PALETTE_SEURAT_SUNDAY_AFTERNOON,
      PALETTE_STARRY_NIGHT,
  };

  public static final int[][] ALL_PALETTES = {
      RAINBOW_PALETTE,
      PALETTE_DALI_MEMORY,
      PALETTE_KANAGA_WAVE,
      PALETTE_KLIMT_KISS,
      PALETTE_LAST_SUPPER,
      PALETTE_MONA_LISA,
      PALETTE_MONET_WATER_LILIES,
      PALETTE_ROCKWELL_OPTOMETRIST,
      PALETTE_SCREAM,
      PALETTE_SEURAT_SUNDAY_AFTERNOON,
      PALETTE_STARRY_NIGHT,
      PALETTE_BLINK_LOGO_FULL,
      PALETTE_BLINK_LOGO_2_COLOR,
      PALETTE_BLINK_SECONDARY,
  };

  private Colors() {
  }

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

  /**
   * Returns the red part of a 32-bit RGBA color.
   */
  public static int red(int color) {
    return (color >> 16) & 0xff;
  }

  /**
   * Returns the green part of a 32-bit RGBA color.
   */
  public static int green(int color) {
    return (color >> 8) & 0xff;
  }

  /**
   * Returns the blue part of a 32-bit RGBA color.
   */
  public static int blue(int color) {
    return color & 0xff;
  }

  /**
   * Returns the alpha part of a 32-bit RGBA color.
   */
  public static int alpha(int color) {
    return (color >> 24) & 0xff;
  }

  /**
   * Returns a color constructed from the three components. The alpha component is set to 255.
   */
  public static int rgb(int r, int g, int b) {
    return 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
  }

  /**
   * Returns a color constructed from the three components. The alpha component is set to 255.
   */
  public static int hsb(float h, float s, float b) {
    return Color.HSBtoRGB(h, s, b);
  }

  public static float[] RGBtoHSB(int rgb, float[] hsb) {
    int r = (rgb & LXColor.R_MASK) >> LXColor.R_SHIFT;
    int g = (rgb & LXColor.G_MASK) >> LXColor.G_SHIFT;
    int b = rgb & LXColor.B_MASK;
    return Color.RGBtoHSB(r, g, b, hsb);
  }

  public static int HSBtoRGB(float[] hsb) {
    return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
  }
}

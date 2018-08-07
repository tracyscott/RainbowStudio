package com.giantrainbow.colors;

import heronarts.lx.color.LXColor;

public class ColorHelpers {
  public static int b2i(byte b) {
    return (int) b & 0xFF;
  }

  public static int red(int color) {
    return b2i(LXColor.red(color));
  }

  public static int green(int color) {
    return b2i(LXColor.green(color));
  }

  public static int blue(int color) {
    return b2i(LXColor.blue(color));
  }

  public static int rgb(int r, int g, int b) {
    return LXColor.rgb(r, g, b);
  }
}

package com.giantrainbow.colors;

public class ColorHelpers {
  public static int red(int color) {
    return (color >> 16) & 0xff;
  }

  public static int green(int color) {
    return (color >> 8) & 0xff;
  }

  public static int blue(int color) {
    return color & 0xff;
  }

  public static int rgb(int r, int g, int b) {
    return 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
  }
}

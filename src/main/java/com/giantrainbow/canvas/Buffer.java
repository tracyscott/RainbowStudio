package com.giantrainbow.canvas;

import heronarts.lx.color.LXColor;
import java.awt.Color;

public class Buffer {
  int[] buffer;

  Buffer(int size) {
    buffer = new int[size];
  }

  public int get(int idx) {
    return buffer[idx];
  }

  public void setHSB(int idx, float h, float s, float b) {
    buffer[idx] = Color.HSBtoRGB(h, s, b);
  }

  public void setRGB(int idx, int r, int g, int b) {
    buffer[idx] = LXColor.rgb(r, g, b);
  }

  public void copy(int[] pixels) {
    System.arraycopy(pixels, 0, buffer, 0, pixels.length);
  }
}

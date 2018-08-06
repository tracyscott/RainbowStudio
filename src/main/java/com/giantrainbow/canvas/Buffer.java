package com.giantrainbow.canvas;

import heronarts.lx.color.LXColor;
import java.awt.Color;

/** Buffer stores the current values of the sub-sampled pixels of a Canvas. */
public class Buffer {
  int[] buffer;

  Buffer(int size) {
    buffer = new int[size];
  }

  /** get returns the color of the indexed pixel. */
  public int get(int idx) {
    return buffer[idx];
  }

  /** setHSB takes h, s, b in (0, 1]. */
  public void setHSB(int idx, float h, float s, float b) {
    buffer[idx] = Color.HSBtoRGB(h, s, b);
  }

  /** setRGB takes r, g, b in [0, 255]. */
  public void setRGB(int idx, int r, int g, int b) {
    buffer[idx] = LXColor.rgb(r, g, b);
  }

  /** copy copies the buffer into `pixels` */
  public void copyInto(int[] pixels) {
    System.arraycopy(pixels, 0, buffer, 0, pixels.length);
  }
}

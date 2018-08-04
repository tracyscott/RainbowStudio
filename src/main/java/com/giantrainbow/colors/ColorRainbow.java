/*
 * Created by shawn on 8/4/18 12:12 AM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.colors;

import static processing.core.PApplet.max;

import java.util.Objects;
import processing.core.PGraphics;

/**
 * ColorRainbow manages shifting between colors at a specific interval.
 * This interpolates between each successive color at a specified rate.
 *
 * @author Shawn Silverman
 */
public final class ColorRainbow {
  private int color1;
  private int color2;
  private int tickCount;  // Remaining ticks for the current color
  private int totalTicks;  // Ticks for the current color

  private final NextColor n;

  /**
   * Represents one color transition.
   */
  public static final class ColorTransition {
    int c;  // Color
    float changeTime;  // Change time, in seconds

    public ColorTransition(int c, float changeTime) {
      this.c = c;
      this.changeTime = changeTime;
    }

    /**
     * Gets the associated number of change ticks, using the frame rate
     * as a reference.
     */
    final int changeTicks(float frameRate) {
      return max((int) (changeTime * frameRate), 1);
    }
  }

  /**
   * Callback for providing the next color.
   */
  public static abstract class NextColor {
    /** Last chosen color, {@code null} if there was no last color. */
    private Integer lastColor;

    /**
     * Performs any reset functions. The first {@link #get()} call after
     * this should return the first color.
     */
    protected void reset() {
    }

    /**
     * Returns whether the last color matches the given color. Note that
     * this will return {@code false} if there was no last color.
     */
    protected final boolean lastColorMatches(int c) {
      return lastColor != null && c == lastColor;
    }

    /**
     * Gets the next color. Implementors do not have to manage
     * {@link #lastColor}. This uses the default change time set
     * in the {@link ColorRainbow#ColorRainbow} constructor.
     * <p>
     * This should return the first color after {@link #reset()}
     * is called.</p>
     */
    protected abstract ColorTransition get();
  }

  /**
   * Creates a new color rainbow using the given color callback.
   * Note that this does not call {@link #reset(float)}.
   *
   * @param n NextColor callback for retrieving the next color
   */
  public ColorRainbow(NextColor n) {
    Objects.requireNonNull(n, "n");
    this.n = n;
  }

  /**
   * Resets the rainbow using a new color from the NextColor callback.
   * This sets the last color to {@code null} before getting getting
   * from {@link NextColor}.
   *
   * @param frameRate the frame rate, used to calculate ticks
   */
  public void reset(float frameRate) {
    tickCount = 0;
    n.reset();
    n.lastColor = null;
    ColorTransition t = n.get();
    totalTicks = t.changeTicks(frameRate);
    color2 = t.c;
    n.lastColor = color2;
  }

  /**
   * Returns an interpolated color. This counts one "tick" each time this
   * is called.
   *
   * @param pg the PGraphics used to calculate the interpolation
   * @param frameRate the frame rate, used to calculate ticks
   */
  public int get(PGraphics pg, float frameRate) {
    if (tickCount == 0) {
      color1 = color2;
      ColorTransition t = n.get();
      totalTicks = t.changeTicks(frameRate);
      tickCount = totalTicks;
      color2 = t.c;
      n.lastColor = color2;
    }
    tickCount--;
    if (tickCount == 0) {
      return color2;
    }
    if (tickCount == totalTicks - 1) {
      return color1;
    }
    float t = (float) tickCount / (float) (totalTicks - 1);
    return pg.lerpColor(color1, color2, 1.0f - t);
  }
}

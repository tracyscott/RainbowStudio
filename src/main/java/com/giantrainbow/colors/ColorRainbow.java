/*
 * Created by shawn on 8/4/18 12:12 AM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.colors;

import static com.giantrainbow.colors.Colors.BLACK;
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
    final int c;  // Color
    final float changeTime;  // Change time, in seconds

    public ColorTransition(int c, float changeTime) {
      this.c = c;
      this.changeTime = changeTime;
    }

    /**
     * Gets the transition color.
     */
    public int getColor() {
      return c;
    }

    /**
     * Gets the associated number of change ticks, using the frame rate
     * as a reference.
     */
    int changeTicks(float frameRate) {
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
   * A {@link NextColor} implementation that chooses random colors having a
   * specific precision, in bits.
   */
  public static class NextRandomColor extends NextColor {
    private final int bits;
    private final float changeTime;

    private final Integer startColor;
    private Integer nextColor;

    /**
     * Creates a new instance. The number of bits specified in {@code bits} can
     * range from 1-8. It defaults to 8 if it's outside this range.
     *
     * @param bits the number of bits of precision
     * @param changeTime the change time
     * @param startColor an optional start color, {@code null} for none
     */
    public NextRandomColor(int bits, float changeTime, Integer startColor) {
      this.bits = bits;
      this.changeTime = changeTime;
      this.startColor = startColor;
    }

    @Override
    protected void reset() {
      nextColor = startColor;
    }

    @Override
    protected ColorTransition get() {
      if (nextColor != null) {
        int c = nextColor;
        nextColor = null;
        return new ColorTransition(c, changeTime);
      }
      while (true) {
        int c = Colors.randomColor(bits);
        if (!lastColorMatches(c)) {
          return new ColorTransition(c, changeTime);
        }
      }
    }
  }

  /**
   * A {@link NextColor} implementation that chooses colors from a list.
   */
  public static class NextArrayColor extends NextColor {
    private final int[] colors;
    private final float changeTime;
    private final boolean random;

    private int nextIndex = -1;

    public NextArrayColor(int[] colors, float changeTime, boolean random) {
      this.colors = colors.clone();
      this.changeTime = changeTime;
      this.random = random;
    }

    @Override
    protected ColorTransition get() {
      int c;
      if (colors.length == 0) {
        c = BLACK;
      } else if (colors.length == 1) {
        c = colors[0];
      } else {
        // First initialize nextIndex
        if (nextIndex < 0) {
          if (random) {
            nextIndex = Colors.rand.nextInt(colors.length);
          } else {
            nextIndex = 0;
          }
        }

        c = colors[nextIndex];

        // Find the next index
        // For random colors, ensure we don't choose the same color
        if (random) {
          int i = nextIndex;
          do {
            nextIndex = Colors.rand.nextInt(colors.length);
          } while (nextIndex == i);
        } else {
          nextIndex = (nextIndex + 1)%colors.length;
        }
      }
      return new ColorTransition(c, changeTime);
    }
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

package com.giantrainbow.patterns;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.p3lx.P3LXPattern;
import processing.core.PApplet;

/**
 * Abstract base class for patterns that want to:
 * <ol>
 * <li>Eventually extend from {@link P3LXPattern} instead of LXPattern;
 *     this is the transition class.</li>
 * <li>Have a speed knob that lets the speed be controlled independently of the frame rate.</li>
 * <li>Contain an {@code applet} field that provides access to the parent {@link PApplet}.</li>
 * <li>Be "pixel-perfect", in RainbowStudio parlance.</li>
 * </ol>
 */
abstract class P3PixelPerfectBase extends PGBase {
  /** Controls speed in the range 0-1. Defaults to 0.5. */
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 0.5, 0.0, 1.0)
          .setDescription("Controls the speed, if used.");

  protected final PApplet applet;

  /**
   * Subclasses, in order to be loaded by LXStudio, must have a constructor containing just the
   * first parameter.
   *
   * @param lx the {@link LX} context
   * @param drawMode the draw mode
   */
  protected P3PixelPerfectBase(LX lx, String drawMode) {
    super(lx,
        ((RainbowBaseModel)lx.model).pointsWide,
        ((RainbowBaseModel)lx.model).pointsHigh,
        drawMode);

    this.applet = RainbowStudio.pApplet;

    addParameter(speedKnob);
  }

  protected final void imageToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(colors, pg);
  }
}

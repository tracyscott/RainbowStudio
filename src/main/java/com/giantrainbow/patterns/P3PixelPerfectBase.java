package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.P3D;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.Registry;
import com.giantrainbow.input.InputManager;
import com.giantrainbow.model.RainbowBaseModel;
import com.google.common.annotations.Beta;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.P3LXPattern;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import processing.core.PApplet;
import processing.core.PGraphics;

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
abstract class P3PixelPerfectBase extends LXPattern {
  /** Controls speed in the range 0-1. Defaults to 0.5. */
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 0.5, 0.0, 1.0)
          .setDescription("Controls the speed, if used.");

  /** UNUSED. */
  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", GLOBAL_FRAME_RATE, 0.0, GLOBAL_FRAME_RATE)
          .setDescription("Controls the frames per second.");

  protected final PApplet applet;
  protected final PGraphics pg;

  /** Indicates whether {@link #setup()} has been called. */
  private boolean setupCalled;
  // TODO: Fix this whole pattern lifecycle thing

  /** For subclasses to use. It's better to have one source. */
  protected static final Random random = new Random();

  /**
   * Subclasses, in order to be loaded by LXStudio, must have a constructor containing just the
   * first parameter.
   *
   * @param lx the {@link LX} context
   * @param drawMode the draw mode
   */
  protected P3PixelPerfectBase(LX lx, String drawMode) {
    super(lx);

    if (lx instanceof P3LX) {
      this.applet = ((P3LX) lx).applet;
    } else {
      this.applet = RainbowStudio.pApplet;
    }

    int width = ((RainbowBaseModel) lx.model).pointsWide;
    int height = ((RainbowBaseModel) lx.model).pointsHigh;
    if (P3D.equals(drawMode) || P2D.equals(drawMode)) {
      pg = RainbowStudio.pApplet.createGraphics(width, height, drawMode);
    } else {
      pg = RainbowStudio.pApplet.createGraphics(width, height);
    }

    addParameter(speedKnob);
  }

  @Override
  public final void onInactive() {
    setupCalled = false;
    tearDown();
  }

  @Override
  public void run(double deltaMs) {
    if (!setupCalled) {
      pg.beginDraw();
      setup();
      pg.endDraw();
      setupCalled = true;
    }

    pg.beginDraw();
    draw(deltaMs);
    pg.endDraw();

    imageToPoints();
  }

  protected final void imageToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(colors, pg);
  }

  /**
   * Called once before all the draw calls, similar to how a Processing sketch has a setup()
   * call. onActive()/onInactive() call timings appear not to be able to be treated the same
   * as conceptual setup() and tearDown() calls.
   * <p>
   * Calls to {@link PGraphics#beginDraw()} and {@link PGraphics#endDraw()} will surround a call
   * to this method.</p>
   */
  protected void setup() {
  }

  /**
   * Called when {@link #onInactive()} is called. That method has been made {@code final}
   * so that it can guarantee {@link #setup()} is called. This may change in the future.
   */
  @Beta
  protected void tearDown() {
  }

  /**
   * Subclasses implement drawing code here. Use the {@link #pg} graphics context for
   * all drawing.
   * <p>
   * Calls to {@link PGraphics#beginDraw()} and {@link PGraphics#endDraw()} will surround a call
   * to this method.</p>
   */
  protected abstract void draw(double deltaMs);

  /**
   * Returns an instance of the {@link InputManager}.
   */
  protected final InputManager inputManager() {
    return ((RainbowStudio) applet).registry.get(Registry.Key.INPUT_MANAGER);
  }

  /**
   * Returns an instance of the {@link ScheduledExecutorService}.
   */
  protected final ScheduledExecutorService exec() {
    return ((RainbowStudio) applet).registry.get(Registry.Key.EXEC);
  }
}

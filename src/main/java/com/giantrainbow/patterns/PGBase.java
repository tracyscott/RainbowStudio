package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.RainbowStudio.pApplet;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import processing.core.PGraphics;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.P3D;

/** Abstract base class for all Processing PGraphics drawing and mapping to the Rainbow. */
abstract class PGBase extends LXPattern {
  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", 1.0, GLOBAL_FRAME_RATE)
          .setDescription("Controls the frames per second.");

  protected double currentFrame = 0.0;
  protected PGraphics pg;
  protected int previousFrame = -1;
  protected double deltaDrawMs = 0.0;

  /** For subclasses to use. It's better to have one source. */
  protected static final Random random = new Random();

  // For P3D, we need to be on the UI/GL Thread.  We should always be on the GL thread
  // during initialization because we start with Multithreading off.  If somebody enables
  // the Engine thread in the UI we don't want to crash so we will keep track of the GL
  // thread and if the current thread in our run() method doesn't match glThread we will just
  // skip our GL render (image will freeze).
  // UPDATE: Removed this check because Processing already handles this.

  // NOTE(Shawn): The instance is sometimes created on a different thread than the thread
  //              that calls run(). We may not be able to use the value we obtain in the
  //              constructor to check for the GL thread. And besides, Processing makes an
  //              effort to do this anyway, in PGraphicsOpenGL.beginDraw() with the
  //              GL_THREAD_NOT_CURRENT message.

  public PGBase(LX lx, int width, int height, String drawMode) {
    super(lx);

    if (P3D.equals(drawMode) || P2D.equals(drawMode)) {
      pg = pApplet.createGraphics(width, height, drawMode);
    } else {
      pg = pApplet.createGraphics(width, height);
    }

    addParameter(fpsKnob);
  }

  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs / 1000.0) * fps;
    // We don't call draw() every frame so track the accumulated deltaMs for them.
    deltaDrawMs += deltaMs;
    if ((int) currentFrame > previousFrame) {
      // Time for new frame.  Draw
      // if glThread == null this is the default Processing renderer so it is always
      // okay to draw.  If it is not-null, we need to make sure the pattern is
      // executing on the glThread or else Processing will crash.
      // UPDATE: Removed this code because Processing already makes a best effort,
      //         and, in addition, the program crashes anyway if multithreading is
      //         set to 'true'.
      pg.beginDraw();
      draw(deltaDrawMs);
      pg.endDraw();

      previousFrame = (int) currentFrame;
      deltaDrawMs = 0.0;
    }
    // Don't let current frame increment forever.  Otherwise float will
    // begin to lose precision and things get wonky.
    if (currentFrame > 10000.0) {
      currentFrame = 0.0;
      previousFrame = -1;
    }
    imageToPoints();
  }

  // Responsible for calling RenderImageUtil.imageToPointsSemiCircle to
  // RenderImageUtil.imageToPointsPixelPerfect.
  protected abstract void imageToPoints();

  // Implement PGGraphics drawing code here.  PGTexture handles beginDraw()/endDraw();
  protected abstract void draw(double deltaDrawMs);
}

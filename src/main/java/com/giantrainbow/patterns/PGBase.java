package com.giantrainbow.patterns;

import static processing.core.PConstants.P2D;
import static processing.core.PConstants.P3D;

import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import processing.core.PGraphics;

/** Abstract base class for all Processing PGraphics drawing and mapping to the Rainbow. */
abstract class PGBase extends LXPattern {
  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", 1.0, RainbowStudio.GLOBAL_FRAME_RATE)
          .setDescription("Controls the frames per second.");

  protected double currentFrame = 0.0;
  protected PGraphics pg;
  protected int imageWidth;
  protected int imageHeight;
  protected int previousFrame = -1;
  protected double deltaDrawMs = 0.0;

  /** For subclasses to use. It's better to have one source. */
  protected static final Random random = new Random();

  public PGBase(LX lx, int width, int height, String drawMode) {
    super(lx);
    imageWidth = width;
    imageHeight = height;
    if (P3D.equals(drawMode) || P2D.equals(drawMode)) {
      pg = RainbowStudio.pApplet.createGraphics(imageWidth, imageHeight, drawMode);
    } else {
      pg = RainbowStudio.pApplet.createGraphics(imageWidth, imageHeight);
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
      pg.beginDraw();
      draw(deltaDrawMs); // THIS IS NEVER HAPPENDING
      pg.endDraw();
      pg.loadPixels();
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

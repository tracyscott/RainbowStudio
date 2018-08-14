package com.giantrainbow.patterns;

import static processing.core.PApplet.ceil;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PGraphics;

/**
 * Original implemenation of PGDraw.  Left here for a full Processing drawing
 * example in case you need to do something not allowed by extending PGTexture
 * or PGPixelPerfect.
 */
@LXCategory(LXCategory.FORM)
public class PGDraw extends LXPattern {
  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", 1.0, 10.0)
          .setDescription("Controls the frames per second.");

  public final BooleanParameter antialiasKnob =
      new BooleanParameter("antialias", true);

  protected double currentFrame;
  protected PGraphics pg;
  protected int imageWidth;
  protected int imageHeight;
  protected int previousFrame = -1;

  float angle = 0.0f;

  public PGDraw(LX lx) {
    super(lx);
    float radiusInWorldPixels = RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot;
    imageWidth = ceil(radiusInWorldPixels * 2.0f);
    imageHeight = ceil(radiusInWorldPixels);
    pg = RainbowStudio.pApplet.createGraphics(imageWidth, imageHeight);
    addParameter(fpsKnob);
    addParameter(antialiasKnob);
    fpsKnob.setValue(10.0);
  }

  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    if ((int)currentFrame > previousFrame) {
      // Time for new frame.  Draw
      angle += 0.03;
      pg.beginDraw();
      pg.background(70);
      pg.strokeWeight(10.0f);
      pg.stroke(255);
      pg.translate(imageWidth/2.0f, imageHeight/2.0f);
      pg.pushMatrix();
      pg.rotate(angle);
      pg.line(-imageWidth/2.0f + 10, -imageHeight/2.0f + 10, imageWidth/2.0f - 10, imageHeight/2.0f - 10);
      pg.popMatrix();
      pg.endDraw();
      previousFrame = (int)currentFrame;
    }
    // Our bounding rectangle is the full half-circle so that Processing drawing operations
    // can work with radial drawings without coordinate space transformation.
    RenderImageUtil.imageToPointsSemiCircle(lx, colors, pg, antialiasKnob.isOn());
  }
}

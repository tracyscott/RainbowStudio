package com.giantrainbow.patterns;

import static processing.core.PApplet.ceil;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;

/**
 * Abstract base class for Processing drawings when painting the
 * rainbow by sampling a large texture that bounds the top-half
 * of the semi-circle defined by the Rainbow.  Use this class for
 * an accurate 2D representation in physical space.  Because the
 * texture size is bound to the radius of the rainbow, it is also
 * easy to perform radial-based calculations in your texture rendering
 * code (see AnimatedSprite). See PGDraw2 for a sample
 * implementation. Gets FPS knob from PGBase.  It
 * provides an antialias toggle. For 1-1 pixel mapping, use PGPixelPerfect.
 */
abstract class PGTexture extends PGBase {
  public final BooleanParameter antialiasKnob =
      new BooleanParameter("antialias", true);


  public PGTexture(LX lx, String drawMode) {
    super(lx,
        ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot * 2.0f),
        ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot),
        drawMode);
    addParameter(antialiasKnob);
  }

  protected void imageToPoints() {
    RenderImageUtil.imageToPointsSemiCircle(lx, colors, pg, antialiasKnob.isOn());
  }

  // Implement PGGraphics drawing code here.  PGTexture handles beginDraw()/endDraw();
  protected abstract void draw(double deltaDrawMs);
}

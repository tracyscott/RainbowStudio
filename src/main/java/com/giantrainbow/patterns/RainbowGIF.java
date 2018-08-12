package com.giantrainbow.patterns;

import static processing.core.PApplet.ceil;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LXCategory;
import heronarts.p3lx.P3LX;

/**
 * Bounding texture based animated GIFs.  These should be 528x264.  Rainbow points will be sampled
 * out of the texture.  Includes support for an anti-alias toggle.
 */
@LXCategory(LXCategory.FORM)
public class RainbowGIF extends RainbowGIFBase {
  public RainbowGIF(P3LX lx) {
    super(lx, ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot * 2.0f),
        ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot),
        "giftex/",
        "hx_ripple",
        true);
  }

  protected void renderToPoints() {
    RenderImageUtil.imageToPointsSemiCircle(lx, colors, images[(int)currentFrame], antialiasKnob.isOn());
  }
}

package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class RainbowImagePP extends RainbowImageBase {
  public RainbowImagePP(LX lx) {
    super(lx, ((RainbowBaseModel)lx.model).pointsWide, ((RainbowBaseModel)lx.model).pointsHigh,
      "imgpp/",
      "oregon.jpg",
      false);
  }

  protected void renderToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(lx, colors, image);
  }
}

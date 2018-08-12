package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LXCategory;
import heronarts.p3lx.P3LX;

@LXCategory(LXCategory.FORM)
public class RainbowImagePP extends RainbowImageBase {
  public RainbowImagePP(P3LX lx) {
    super(lx, ((RainbowBaseModel)lx.model).pointsWide, ((RainbowBaseModel)lx.model).pointsHigh,
        "imgpp/",
        "oregon.jpg",
        false);
  }

  protected void renderToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(colors, image);
  }
}

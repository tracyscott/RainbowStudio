package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PConstants;
import processing.core.PImage;

@LXCategory(LXCategory.FORM)
public class RainbowImagePP extends RainbowImageBase {
  public RainbowImagePP(LX lx) {
    super(lx, ((RainbowBaseModel)lx.model).pointsWide, ((RainbowBaseModel)lx.model).pointsHigh,
        "imgpp/",
        "oregon.jpg",
        false);
  }

  protected void renderToPoints() {
    pg.beginDraw();
    pg.background(0, 0);
    if (spriteMode.isOn())
      pg.imageMode(PConstants.CENTER);
    pg.image(image, xOff.getValuef(), yOff.getValuef(), image.width*scale.getValuef(),
          image.height*scale.getValuef());
    pg.endDraw();
    RenderImageUtil.imageToPointsPixelPerfect(colors, pg);
  }
}

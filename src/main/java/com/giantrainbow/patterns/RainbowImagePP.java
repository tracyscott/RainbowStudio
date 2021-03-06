package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PConstants;
import processing.core.PImage;

@LXCategory(LXCategory.FORM)
public class RainbowImagePP extends RainbowImageBase {
  float currentX = 430f;

  public RainbowImagePP(LX lx) {
    super(lx, ((RainbowBaseModel)lx.model).pointsWide, ((RainbowBaseModel)lx.model).pointsHigh,
        "imgpp/",
        "oregon.jpg",
        false);
  }

  @Override
  public void onActive() {
    currentX = 430f;
  }

  protected void renderToPoints() {
    pg.beginDraw();
    if (alpha.isOn())
      pg.background(0, 0);
    else
      pg.background(0);
    // Support Rainbow Flag background graphic

    if (rbbg.isOn()) {
      pg.image(RenderImageUtil.rainbowFlag(pg.width, pg.height, rbBright.getValuef()),
          0, 0, pg.width, pg.height);
    }

    if (spriteMode.isOn())
      pg.imageMode(PConstants.CENTER);
    if (Math.abs(speed.getValuef()) > 0.1) {
      if (currentX > 210 - image.width/2) {
        currentX += speed.getValuef();
        xOff.setValue(currentX);
      }
    }
    pg.image(image, xOff.getValuef(), yOff.getValuef(), image.width*scale.getValuef(),
          image.height*scale.getValuef());
    pg.endDraw();
    RenderImageUtil.imageToPointsPixelPerfect(colors, pg);
  }
}

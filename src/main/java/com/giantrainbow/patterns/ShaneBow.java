package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PConstants;

//FPS 4.20 looks pretty cool
//FPS should be a function of width
//TODO: Figure out how to make it slow down when it gets to a small width
@LXCategory(LXCategory.FORM)
public class ShaneBow extends PGPixelPerfect {

  public final LX lx;
  public final CompoundParameter xRateParam = new CompoundParameter("xRate", 0, 2, 10);
  public final CompoundParameter blockRateParam = new CompoundParameter("blockRate", -10, 1, 10);
  public final CompoundParameter hueRateParam = new CompoundParameter("hueRate", -10, 5, 10);
  public final CompoundParameter strokeCutoffParam = new CompoundParameter("strokeCutoff", 0, 10, 420);

  float xOffset = 0;
  float hueOffset = 0;
  float width = 0;
  boolean widthExpanding = true;

  public ShaneBow(LX lx) {
    super(lx, "");
    this.lx = lx;
    addParameter(xRateParam);
    addParameter(blockRateParam);
    addParameter(hueRateParam);
    addParameter(strokeCutoffParam);
  }

  @Override
  protected void draw(double deltaDrawMs) {
    xOffset += xRateParam.getValuef();
    float widthDelta = blockRateParam.getValuef() * (widthExpanding ? 1 : -1);
    this.width += widthDelta;
    if (this.width > imageWidth || this.width <= 1) {
      widthExpanding = !widthExpanding;
    }
    if (this.width <= 0) {
      System.out.println("Adjusting zero width");
      width = 1f;
    }
    hueOffset += hueRateParam.getValuef();
    pg.background(0);
    pg.colorMode(PConstants.HSB, 1000);
    for (float x = xOffset; x < (imageWidth + xOffset); x += this.width) {
      for (float y = 0; y < imageHeight; y += this.width) {

        float startHue = (x / imageWidth) * 1000;
        float offsetHue = (y / imageHeight) * 1000;
        float hue = (startHue + offsetHue) % 1000;
        System.out.format(
            "x,y = %s,%s | width = %s | widthDelta = %s | expanding = %s | hue = %s%n",
            x, y, this.width, widthDelta, widthExpanding, hue);
        if (width > strokeCutoffParam.getValue()) {
          pg.stroke(0, 0, 0);
        } else {
          pg.stroke(hue, 1000, 1000);
        }
        pg.fill(hue, 1000, 1000);
        pg.rect(x % imageWidth, y, this.width, this.width);
        if (x % imageWidth + this.width > imageWidth) {
          float width2 = (x % imageWidth + this.width) - imageWidth;
          pg.rect(0, y, width2, this.width);
        }
      }
    }
  }
}

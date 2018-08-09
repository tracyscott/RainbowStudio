package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PConstants;

//TODO: Tie the xRate to the ambient noise?
//TODO: Reintroduce hueOffset?

/**
 * A simple pattern which draws a grid of boxes in a rotating fashion. When the box width is
 * smaller than the height of the rainbow, multiple rows will be displayed with a second layer of
 * rainbowness that swirls across the arc of the rainbow. The pattern alternates between expanding
 * and contracting box size and changes rotation from clockwise to counter-clockwise after each
 * expansion and contraction of box size.
 */
@LXCategory(LXCategory.FORM)
public class ShaneBow extends PGPixelPerfect {

  public final LX lx;

  public final CompoundParameter xRateParam = new CompoundParameter("xRate", 0, 2, 10);
  public final CompoundParameter xStartParam = new CompoundParameter("xStart", 1, 1, imageWidth + 1);
  public final BooleanParameter clockwiseParam = new BooleanParameter("clockwise", true);

  public final CompoundParameter blockWidthParam = new CompoundParameter("blockWidth", 1, 1, imageWidth + 1);
  public final BooleanParameter blockExpandingParam = new BooleanParameter("blockExp", true);

  public ShaneBow(LX lx) {
    super(lx, "");
    this.lx = lx;
    fpsKnob.setValue(30f);
    removeParameter(fpsKnob);
    addParameter(xRateParam);
    addParameter(xStartParam);
    addParameter(clockwiseParam);
    addParameter(blockWidthParam);
    addParameter(blockExpandingParam);
  }

  @Override
  protected void draw(double deltaDrawMs) {

    float xStart = xStartParam.getValuef();
    float xRate = xRateParam.getValuef();
    float blockWidth = blockWidthParam.getValuef();
    float blockRate = Math.max(0.1f, blockWidth / imageWidth);
    boolean clockwise = clockwiseParam.getValueb();
    System.out.format("clockwise: %s | xStart: %s + xRate: %s | blockWidth: %s + blockRate: %s%n",
        clockwise, xStart, xRate, blockWidth, blockRate);

    // Draw the blocks
    pg.background(0);
    pg.colorMode(PConstants.HSB, 1000);
    for (float x = xStart; x < (imageWidth + xStart); x += blockWidth) {
      float startHue = (x / imageWidth) * 1000;
      for (float y = 0; y < imageHeight; y += blockWidth) {
        float offsetHue = (y / imageHeight) * 1000;
        float hue = (startHue + offsetHue) % 1000;
        pg.stroke(hue, 1000, 1000);
        pg.fill(hue, 1000, 1000);
        pg.rect(x % imageWidth, y, blockWidth, blockWidth);
        if (x % imageWidth + blockWidth > imageWidth) {
          float width2 = (x % imageWidth + blockWidth) - imageWidth;
          pg.rect(0, y, width2, blockWidth);
        }
      }
    }

    // Update the params for the next frame
    if (blockWidth > imageWidth) {
      blockExpandingParam.setValue(false);
    } else if (blockWidth <= 1) {
      blockExpandingParam.setValue(true);
      clockwiseParam.setValue(!clockwise);
    }
    blockWidthParam.setValue(blockWidth + (blockRate * (blockExpandingParam.getValueb() ? 1f : -1f)));
    xStartParam.setValue(
        xStart > imageWidth
            ? 2
            : xStart <= 1
                ? imageWidth - 1
                : xStart + (xRate * (clockwise ? 1 : -1)));
  }
}

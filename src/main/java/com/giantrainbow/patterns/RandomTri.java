package com.giantrainbow.patterns;

import com.giantrainbow.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import static java.lang.Math.ceil;
import static processing.core.PConstants.HSB;

public class RandomTri extends PGPixelPerfect {

  public DiscreteParameter paletteKnob = new DiscreteParameter("palette", 0, 0, Colors.ALL_PALETTES.length + 1);
  public DiscreteParameter sidesKnob = new DiscreteParameter("sides", 3, 2, 5);
  public CompoundParameter bgAlpha = new CompoundParameter("bgalpha", 0.25, 0.0, 1.0);
  // Probability of a new triangle show up each frame.  Should we allow multiple triangles per frame?  nah,
  // probably not.
  public CompoundParameter newTriProbability = new CompoundParameter("newtri", 1.0, 0.0, 10.0);
  // Max triangle size.  Allow only small triangles for example.
  public CompoundParameter maxTriSize = new CompoundParameter("max", 15.0, 3.0, 45.0);
  public CompoundParameter maxOffScreen = new CompoundParameter("off", 0.0, 0.0, 30.0);

  public CompoundParameter fillAlpha = new CompoundParameter("falpha", 0.75, 0.0, 1.0);
  public CompoundParameter saturation = new CompoundParameter("sat", 0.5, 0.0, 1.0);
  public CompoundParameter bright = new CompoundParameter("bright", 1.0, 0.0, 1.0);

  public RandomTri(LX lx) {
    super(lx, "");
    addParameter(paletteKnob);
    addParameter(sidesKnob);
    addParameter(bgAlpha);
    addParameter(newTriProbability);
    addParameter(maxTriSize);
    addParameter(maxOffScreen);
    addParameter(fillAlpha);
    addParameter(saturation);
  }

  /*
  A new random polygon with background fades.  Random 3 or 4 points?  Maybe just 3 points
  * since that will always easily render regardless of the point ordering.  Polygon should
  * be rendered with transparency on a 25% (configurable) transparent background for fading away.
  * What to do for coloring?  Be able to specify saturation while randomizing hue.  Or possibly
  * selecting a random number from 0 to sizeof(palette) and then use that color if palette box is
  * checked?  Or if palette dropdown has a selected palette so can be both rainbow and redbull.


   */
  public void draw(double drawDeltaMs) {
    pg.colorMode(HSB, 1.0f);
    pg.background(0.0f, 0.0f, 0.0f, bgAlpha.getValuef());

    for (int i = 0; i < newTriProbability.getValue(); i++) {
      drawRandomTriangle();
    }
  }

  public void drawRandomTriangle() {
    float centerX = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float centerY = ((float)Math.random() * pg.height + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt1XDelta = ((float)Math.random() * 2.0f * maxTriSize.getValuef()) - maxTriSize.getValuef();
    float pt1YDelta = ((float)Math.random() * maxTriSize.getValuef());
    float pt2XDelta = ((float)Math.random() * maxTriSize.getValuef());
    float pt2YDelta = ((float)Math.random() * 2.0f * maxTriSize.getValuef()) - maxTriSize.getValuef();
    float pt3XDelta = ((float)Math.random() * 2.0f * maxTriSize.getValuef()) - maxTriSize.getValuef();
    float pt3YDelta = ((float)Math.random() * -1.0f * maxTriSize.getValuef());
    /*
    float pt1X = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt1Y = (float)Math.random() * pg.height;
    float pt2X = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt2Y = (float)Math.random() * pg.height;
    float pt3X = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt3Y = (float)Math.random() * pg.height;
    */

    int whichPalette = paletteKnob.getValuei();
    float h = 0.0f;
    float s = 0.0f;
    float b = 0.0f;

    if (whichPalette == 0) {
      h = (float) Math.random();
      s = saturation.getValuef();
      b = bright.getValuef();
    } else {
      int[] palette = Colors.ALL_PALETTES[whichPalette - 1];
      int index = (int) ceil(Math.random() * (palette.length)) - 1;
      if (index < 0) index = 0;
      int color = palette[index];
      float[] hsb = {0.0f, 0.0f, 0.0f};
      LXColor.RGBtoHSB(color, hsb);
      h = hsb[0];
      s = hsb[1];
      b = hsb[2];
    }

    pg.fill(h, s, b, fillAlpha.getValuef());
    //pg.triangle(pt1X, pt1Y, pt2X, pt2Y, pt3X, pt3Y);
    pg.triangle(centerX + pt1XDelta, centerY + pt1YDelta, centerX + pt2XDelta, centerY + pt2YDelta,
        centerX + pt3XDelta, centerY + pt3YDelta);
  }
}

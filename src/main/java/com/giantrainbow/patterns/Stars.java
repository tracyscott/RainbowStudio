package com.giantrainbow.patterns;

import com.giantrainbow.colors.Colors;
import com.google.common.collect.Maps;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import processing.core.PConstants;

/**
 * A class where I test shit out
 */
@LXCategory(LXCategory.FORM)
public class Stars extends PGPixelPerfect {


  public final CompoundParameter numStarParam = new CompoundParameter("stars", 50, 200, 1000);
  public final CompoundParameter newStarParam = new CompoundParameter("newStars", 1, 2, 200);

  private static ThreadLocalRandom rando = ThreadLocalRandom.current();
  private int xoffset = 0;
  private int lastSize = (int) numStarParam.getValue();
  private List<Entry<Integer, Integer>> stars = new ArrayList<>();

  public Stars(LX lx) {
    super(lx, null);
    addParameter(numStarParam);
    addParameter(newStarParam);
  }

  @Override
  protected void draw(double deltaDrawMs) {
    int numStars = (int) numStarParam.getValue();
    if (numStars != lastSize) {
      if (numStars < lastSize) {
        stars.clear();
      }
      lastSize = numStars;
    }
    pg.background(0);
    pg.colorMode(PConstants.HSB, 1000);
    pg.stroke(Colors.WHITE);
    for (int i = 0; i < newStarParam.getValue(); i++) {
      Map.Entry<Integer, Integer> newStar =
          Maps.immutableEntry(
              rando.nextInt(0, pg.width + 1),
              rando.nextInt(0, pg.height + 1));
      if (stars.size() < numStars) {
        stars.add(newStar);
      } else {
        stars.set(rando.nextInt(0, numStars), newStar);
      }
    }
    stars.forEach(s ->  pg.point((s.getKey() + xoffset) % pg.width, s.getValue()));
    xoffset++;
  }
}

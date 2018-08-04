/*
 * Created by shawn on 8/4/18 10:40 AM.
 */
package com.giantrainbow.patterns;

import static processing.core.PApplet.map;
import static processing.core.PApplet.round;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.DILATE;
import static processing.core.PConstants.HSB;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.TWO_PI;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

/**
 * Based on: <a href="http://www.openprocessing.org/sketch/146637">Dragon Tail 3D</a>
 */
@LXCategory(LXCategory.FORM)
public class DragonTail extends PGPixelPerfect {
  private static final float deltaTheta = 0.0523f * 30;

  private int num = 9;
  private int stepY = num * 6;
  private int step = 50;
  private float sz = 8;
  private int[] startX = new int[num];
  private float theta, offset, x, y, startY;

  private int[] col = new int[num];

  public DragonTail(LX lx) {
    super(lx, P2D);
  }

  @Override
  public void onActive() {
//    size(500, 300);
    pg.beginDraw();
    pg.colorMode(HSB, 360, 100, 100);
    pg.endDraw();

    for (int i = 0; i < num; i++) {
      startX[i] = step * (i + 1);
      col[i] = (360 / num) * i;
    }

    num = 9;
    stepY = round(num * 6 * pg.height/300.0f);
    step = round(50 * pg.width/500.0f);
    sz = round(8 * pg.width/500.0f);
    startX = new int[num];

    fpsKnob.setValue(60);
  }

  @Override
  protected void draw(double deltaDrawMs) {
    pg.fill(20, 25);
    pg.noStroke();
    pg.rect(0, 0, pg.width, pg.height);

    for (int i = 0; i < startX.length; i++) {
      for (int x = startX[i]; x < startX[i] + pg.width; x++) {
        offset = (TWO_PI / pg.width) * x;
        startY = map(i, 0, startX.length, -stepY, stepY);
        float y = pg.height*0.55f + startY + map(sin(theta + offset), -1, 1, -stepY, stepY);
        pg.fill(col[i], 70, 90);
        pg.ellipse(x - pg.width/6, y, sz, sz);
      }
    }
    pg.filter(DILATE);

    theta += deltaTheta / fpsKnob.getValuef();
  }
}

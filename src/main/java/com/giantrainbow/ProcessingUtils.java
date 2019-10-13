package com.giantrainbow;

import processing.core.PGraphics;
import static processing.core.PConstants.*;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.map;
import static processing.core.PApplet.sin;


public class ProcessingUtils {
  public static float mapsin(float param, float min, float max) {
    return map(sin(param), -1, 1, min, max);
  }

  public static float mapcos(float param, float min, float max) {
    return map(cos(param), -1, 1, min, max);
  }

  public static void center(PGraphics pg) {
    pg.translate(pg.width * .5f, pg.height * .5f);
  }

  public static void uncenter(PGraphics pg) {
    pg.translate(-pg.width * .5f, -pg.height * .5f);
  }

  public static void rotateCentered(PGraphics pg, float angle) {
    center(pg);
    pg.rotate(angle);
    uncenter(pg);
  }

  public static void colorHSB(PGraphics pg) {
    pg.colorMode(HSB, 100, 100, 100, 100);
  }

  public static void fadeRect(PGraphics pg, float alpha) {
    pg.rectMode(CORNER);
    pg.pushStyle();
    pg.noStroke();
    pg.fill(0, alpha);
    pg.rect(-1, -1, pg.width + 1, pg.height + 1);
    pg.popStyle();
  }
}
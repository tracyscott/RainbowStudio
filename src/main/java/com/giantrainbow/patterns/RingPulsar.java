/*
 * Author: Soma Holiday
 * 2019-10-13
 */
package com.giantrainbow.patterns;

import java.util.logging.Logger;

import com.giantrainbow.ProcessingUtils;
import com.giantrainbow.RainbowStudio;

import static processing.core.PConstants.*;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.COLOR)
public class RingPulsar extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(RingPulsar.class.getName());

  int RING_COUNT = 50;
  Ring[] rings;

  public RingPulsar(LX lx) {
    super(lx, "");

  }

  @Override
  protected void setup() {
    ProcessingUtils.colorHSB(pg);
    pg.blendMode(ADD);
    pg.background(0);

    rings = new Ring[RING_COUNT];

    for (int i=0; i < RING_COUNT; i++) {
      rings[i] = new Ring(i*2/10);
    }
  }

  public void draw(double deltaDrawMs) {
    if (RainbowStudio.pApplet.frameCount %2 == 0) {
      pg.blendMode(BLEND);
      ProcessingUtils.fadeRect(pg, 4);
      pg.blendMode(ADD);
    }

    for (Ring ring : rings) {
      ring.draw();
    }
  }

  @Override
  protected void tearDown() {

  }

  @Override
  public void onActive() {

  }


  public class Ring {
    float radius;
    float hue;
    float strokeWeight;
    float seed;

    public Ring(float radius) {
      this.radius = radius + this.seed;
      this.hue = RainbowStudio.pApplet.random(this.radius * 5);
      this.seed = RainbowStudio.pApplet.random(30);
      this.strokeWeight = this.getStrokeWeight();
    }

    float getStrokeWeight() {
      return ProcessingUtils.mapsin(this.radius + this.hue + RainbowStudio.pApplet.frameCount * 0.1f, 0.5f, 15f);
    }

    void draw() {
      pg.pushStyle();
      pg.pushMatrix();

      this.strokeWeight = this.getStrokeWeight();

      pg.noFill();
      pg.stroke(this.hue, 100, 100, 2);
      pg.strokeWeight(this.strokeWeight);

      pg.translate(pg.width/2, pg.height/2);

      this.drawRing();

      pg.popMatrix();
      pg.popStyle();

      this.update();
    }

    void drawRing() {
      float d = this.radius * 2;
      float w_offset = ProcessingUtils.mapsin((RainbowStudio.pApplet.frameCount + this.seed + this.hue + this.radius) * 0.009f, -d, 100f);
      float h_offset = ProcessingUtils.mapcos((float)(this.seed * 0.73 + RainbowStudio.pApplet.frameCount * 0.01 * this.seed), -d + 30f, 80f);
      pg.ellipse(0, 0, d + w_offset, d + h_offset);
    }

    void update() {
        this.radius += RainbowStudio.pApplet.random(2);

        if (this.radius > 0.7 * pg.width) {
          this.radius = 0;
        }

        this.hue = (this.hue + 1) % 100;
    }
  }
}

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
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.COLOR)
public class RingPulsar extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(RingPulsar.class.getName());

  int RING_COUNT = 50;
  Ring[] rings;

  public final CompoundParameter maxStrokeWeight = new CompoundParameter("weight", 15.0, 1.0, 30.0)
      .setDescription("Maximum stroke weight");
  public final CompoundParameter maxWidthOffset = new CompoundParameter("wOffset", 100.0, 0.0, 150.0)
      .setDescription("Maximum width offset");
  public final CompoundParameter maxHeightOffset = new CompoundParameter("hOffset", 80.0, 0.0, 150.0)
      .setDescription("Maximum height offset");
  public final CompoundParameter strokeAlpha = new CompoundParameter("alpha", 5.0, 0.5, 20.0)
      .setDescription("Stroke alpha");

  public RingPulsar(LX lx) {
    super(lx, "");

    addParameter(maxStrokeWeight);
    addParameter(maxWidthOffset);
    addParameter(maxHeightOffset);
    addParameter(strokeAlpha);
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
    if (RainbowStudio.pApplet.frameCount % 2 == 0) {
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
    float alpha;
    float seed;

    public Ring(float radius) {
      this.radius = radius + this.seed;
      this.hue = RainbowStudio.pApplet.random(this.radius * 5);
      this.seed = RainbowStudio.pApplet.random(30);
      this.strokeWeight = this.getStrokeWeight();
      this.alpha = strokeAlpha.getValuef();
    }

    float getStrokeWeight() {
      float maxWeight = maxStrokeWeight.getValuef();
      return ProcessingUtils.mapsin(this.radius + this.hue + RainbowStudio.pApplet.frameCount * 0.1f, 0.5f, maxWeight);
    }

    void draw() {
      pg.pushStyle();
      pg.pushMatrix();

      this.strokeWeight = this.getStrokeWeight();
      this.alpha = strokeAlpha.getValuef();

      pg.noFill();
      pg.stroke(this.hue, 100, 100, this.alpha);
      pg.strokeWeight(this.strokeWeight);

      pg.translate(pg.width/2, pg.height/2);

      this.drawRing();

      pg.popMatrix();
      pg.popStyle();

      this.update();
    }

    void drawRing() {
      float d = this.radius * 2;
      float max_w_offset = maxWidthOffset.getValuef();
      float max_h_offset = maxHeightOffset.getValuef();

      float w_offset = ProcessingUtils.mapsin((RainbowStudio.pApplet.frameCount + this.seed + this.hue + this.radius) * 0.009f, 0, max_w_offset);
      float h_offset = ProcessingUtils.mapcos((float)(this.seed * 0.73 + RainbowStudio.pApplet.frameCount * 0.01 * this.seed), 0, max_h_offset);
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

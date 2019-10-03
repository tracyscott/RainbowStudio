package com.giantrainbow.patterns;

import com.giantrainbow.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.Math.ceil;
import static processing.core.PConstants.HSB;

public class Tracers extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(Tracers.class.getName());

  public CompoundParameter numTracers = new CompoundParameter("num", 60.0, 0.0, 200.0);
  public CompoundParameter maxSize = new CompoundParameter("max", 15.0, 3.0, 45.0);
  public CompoundParameter minSize = new CompoundParameter("min", 3.0, 1.0, 30.0);
  public CompoundParameter minVelocity = new CompoundParameter("minv", 1.0, 0.0, 30.0);
  public CompoundParameter maxVelocity = new CompoundParameter("maxv", 20.0, 0.0, 60.0);
  public CompoundParameter maxOffScreen = new CompoundParameter("off", 0.0, 0.0, 30.0);

  public CompoundParameter fillAlpha = new CompoundParameter("falpha", 0.75, 0.0, 1.0);
  public CompoundParameter blurKnob = new CompoundParameter("blur", 0.0, 0.0, 1f);
  public BooleanParameter outlinedKnob = new BooleanParameter("outline", true)
      .setDescription("Include black outlines");
  public CompoundParameter randomVKnob = new CompoundParameter("randV", 0.0, 0.0, 20.0)
      .setDescription("Maximum random velocity to add.");

  public static class Tracer {
    public LXPoint pos;
    public boolean hasBeenShown = false;
    public int size;  // radius of tracer.
    public float velocityX;
    public float velocityY;
    public float[] hsb;
    int notShownCounter = 0;
  };

  List<Tracer> tracers = new ArrayList<Tracer>();

  public Tracers(LX lx) {
    super(lx, "");
    addParameter(paletteKnob);
    addParameter(numTracers);
    addParameter(maxSize);
    addParameter(minSize);
    addParameter(minVelocity);
    addParameter(maxVelocity);
    addParameter(maxOffScreen);
    addParameter(fillAlpha);
    addParameter(hue);
    addParameter(saturation);
    addParameter(bright);
    addParameter(blurKnob);
    addParameter(outlinedKnob);
    addParameter(randomPaletteKnob);
    addParameter(randomVKnob);
  }

   public void draw(double drawDeltaMs) {
    //pg.colorMode(HSB, 1.0f);
    pg.colorMode(PConstants.HSB, 1.0f, 1.0f, 1.0f, 1.0f);
    pg.fill(0, 1f - blurKnob.getValuef());
    pg.noStroke();
    pg.rect(0, 0, pg.width+1, pg.height+1);
    pg.fill(255);
    pg.smooth();
    //pg.background(0.0f, 0.0f, 0.0f, 0.0f);

    updateTracers();
    processTracers();
    for (Tracer tracer : tracers) {
      drawTracer(tracer);
    }
  }

  /**
   * Check if tracer is in the render window.
   */
  public boolean isTracerVisible(Tracer t) {
    if (t.pos.x >= 0 && t.pos.x < pg.width && t.pos.y >= 0 && t.pos.y < pg.height) {
      return true;
    }
    return false;
  }

  /**
   * Check if tracer needs to be reset.
   */
  public boolean tracerNeedsReset(Tracer t) {
    if ((t.hasBeenShown && !isTracerVisible(t)) || t.notShownCounter == 100) {
      return true;
    }
    return false;
  }

  /**
   * Resets a tracer to some initial condition based on our parameter settings.
   * @param tracer
   */
  public void resetTracer(Tracer tracer) {
    // Reset the tracer based on our parameter knobs.
    tracer.pos.y = pg.height + 10.0f;
    tracer.pos.x = (int)(Math.random() * pg.width);
    tracer.velocityY = -1.0f;
    tracer.velocityX = (float)(Math.random() * 2.0 * maxVelocity.getValue() - maxVelocity.getValue());
    tracer.hasBeenShown = false;
    tracer.notShownCounter = 0;
    tracer.size = (int)(Math.random() * (maxSize.getValuef() - minSize.getValuef()) + minSize.getValuef());
    getNewHSB(tracer.hsb);
  }

  /**
   * Process tracers, culling tracers that have finished and replacing them with new tracers.
   */
  public void processTracers() {
    //logger.info("processing Tracers.");
    for (Tracer tracer : tracers) {
      if (isTracerVisible(tracer))
        tracer.hasBeenShown = true;
      else
        tracer.notShownCounter++;
      float randVRange = randomVKnob.getValuef();
      if (randVRange > 0.001f) {
        tracer.velocityX += Math.random() * 2f * randVRange - randVRange;
        tracer.velocityY += Math.random() * 2f * randVRange - randVRange;
      }
      tracer.pos.x += tracer.velocityX;
      tracer.pos.y += tracer.velocityY;
      if (tracerNeedsReset(tracer)) {
        //logger.info("resetting tracer");
        resetTracer(tracer);
      }
    }
  }

  /*
  public void getNewHSB(float[] hsb) {
    int whichPalette = paletteKnob.getValuei();

    if (whichPalette == 0) {
      hsb[0] = (float) Math.random();
      hsb[1] = saturation.getValuef();
      hsb[2] = bright.getValuef();
    } else {
      int[] palette = Colors.ALL_PALETTES[whichPalette - 1];
      int index = (int) ceil(Math.random() * (palette.length)) - 1;
      if (index < 0) index = 0;
      int color = palette[index];
      LXColor.RGBtoHSB(color, hsb);
    }
  }
  */

  /**
   * Update tracer positions.
   */
  public void updateTracers() {
    if (tracers.size() < numTracers.getValuef()) {
      logger.info("Initializing " + numTracers.getValuef() + " tracers.");
      int i = tracers.size();
      while (tracers.size() < numTracers.getValuef()) {
        Tracer t = new Tracer();
        t.pos = new LXPoint(i * 5f, 40f, 0f);
        float size = (float)Math.random();
        t.size = (int)(size * (maxSize.getValuef() - minSize.getValuef()) + minSize.getValuef());
        // Assign hue.
        t.hsb = new float[3];
        getNewHSB(t.hsb);
        resetTracer(t);
        tracers.add(t);
        i++;
      }
    }
  }

  public void drawTracer(Tracer tracer) {
    /*
    float centerX = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float centerY = ((float)Math.random() * pg.height + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt1XDelta = ((float)Math.random() * 2.0f * maxTriSize.getValuef()) - maxTriSize.getValuef();
    float pt1YDelta = ((float)Math.random() * maxTriSize.getValuef());
    float pt2XDelta = ((float)Math.random() * maxTriSize.getValuef());
    float pt2YDelta = ((float)Math.random() * 2.0f * maxTriSize.getValuef()) - maxTriSize.getValuef();
    float pt3XDelta = ((float)Math.random() * 2.0f * maxTriSize.getValuef()) - maxTriSize.getValuef();
    float pt3YDelta = ((float)Math.random() * -1.0f * maxTriSize.getValuef());
    */
    /*
    float pt1X = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt1Y = (float)Math.random() * pg.height;
    float pt2X = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt2Y = (float)Math.random() * pg.height;
    float pt3X = ((float)Math.random() * pg.width + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt3Y = (float)Math.random() * pg.height;
    */


    if (outlinedKnob.getValueb()) {
      pg.stroke(0);
      pg.strokeWeight(1f);
    } else {
      pg.strokeWeight(0f);
      pg.noStroke();
    }

    pg.fill(tracer.hsb[0], tracer.hsb[1], tracer.hsb[2], fillAlpha.getValuef());
    //pg.triangle(pt1X, pt1Y, pt2X, pt2Y, pt3X, pt3Y);
    pg.ellipse(tracer.pos.x, tracer.pos.y, tracer.size, tracer.size);
  }
}

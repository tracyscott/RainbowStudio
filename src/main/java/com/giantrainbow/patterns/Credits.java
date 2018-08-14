/*
 * Created by shawn on 8/13/18 9:25 PM.
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.colors.Colors.BLACK;
import static com.giantrainbow.colors.Colors.WHITE;
import static processing.core.PApplet.ceil;
import static processing.core.PConstants.P2D;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * A simple, springy, credits pattern.
 *
 * @author Shawn Silverman
 */
@LXCategory(LXCategory.FORM)
public class Credits extends P3PixelPerfectBase {
  private static final Logger logger = Logger.getLogger(Credits.class.getName());

  private static final float SPRING_K = 20.0f;  // Maximum
  private static final float DAMPING = 0.3f;
  private static final float TEXT_HEIGHT = 0.7f;  // Relative to pg.height

  private static final float VELOCITY = 100.0f;  // Maximum
  private static final float COMPRESSION = 0.8f;  // Relative to pg.width

  private static final float INTER_STRING_SPACING = 120.0f;  // Two segments worth

  /** Credits to roll. Change this to change the display. */
  private static final String[] STRINGS = {
      "Person One, Position 1",
      "Person Two, Position 2",
      "Person Three, Position 3",
  };

  private PFont font;

  private LinkedList<TextBox> textBoxes = new LinkedList<>();
  private int stringsIndex;

  private final CompoundParameter stiffnessKnob =
      new CompoundParameter("Stiffness", 0.5, 0.0, 1.0)
          .setDescription("Spring stiffness");

  public Credits(LX lx) {
    super(lx, P2D);

    font = applet.createFont("fonts/Roboto/Roboto-Black.ttf", pg.height*TEXT_HEIGHT);
    // Can get these fonts from fonts.google.com:
//    font = applet.createFont("fonts/Fontdiner_Swanky/FontdinerSwanky-Regular.ttf", pg.height*TEXT_HEIGHT);
//    font = applet.createFont("fonts/Slackey/Slackey-Regular.ttf", pg.height*TEXT_HEIGHT);
//    font = applet.createFont("fonts/Galindo/Galindo-Regular.ttf", pg.height*TEXT_HEIGHT);

    addParameter(stiffnessKnob);
  }

  @Override
  protected void setup() {
    fpsKnob.setValue(GLOBAL_FRAME_RATE);

    textBoxes.clear();
    stringsIndex = 0;
  }

  @Override
  protected void draw(double deltaDrawMs) {
    pg.background(BLACK);

    for (Iterator<TextBox> iter = textBoxes.iterator(); iter.hasNext(); ) {
      TextBox t = iter.next();
      t.draw(pg);
      t.doPhysics((float) (deltaDrawMs / 1000.0));
      if (t.x() >= pg.width) {
        iter.remove();
      }
    }

    if (stringsIndex < 0 || STRINGS.length <= stringsIndex) {
      return;
    }

    // Add to the end if there's space
    float x;
    TextBox last = textBoxes.peekLast();
    if (last == null) {
      x = INTER_STRING_SPACING;
    } else {
      x = last.x();
    }
    while (x >= INTER_STRING_SPACING) {
      TextBox t = new TextBox(STRINGS[stringsIndex], COMPRESSION, VELOCITY);
      x -= INTER_STRING_SPACING + t.width()/COMPRESSION;
      t.setX(x);
      logger.fine("Adding text box: x=" + t.x() + " width=" + t.width());
      textBoxes.add(t);
      stringsIndex = (stringsIndex + 1)%STRINGS.length;
    }
  }

  private class TextBox {
    private PGraphics textPG;
    private float x1;
    private float x2;
    private float v1;
    private float v2;

    // Group
    private float x;
    private float v;

    private String text;

    /**
     * Creates a new text box at position 0.
     *
     * @param s the text string
     * @param compression the initial width compression
     * @param v the initial left-side velocity
     */
    TextBox(String s, float compression, float v) {
      this.text = s;

      // Determine metrics
      float size = font.getSize();
      float w = 0;
      for (int i = 0; i < s.length(); i++) {
        w += font.width(s.charAt(i));
      }
      float h = font.ascent() + font.descent();

      // Draw the text
      textPG = applet.createGraphics(ceil(w*size), ceil(h*size), P2D);
      textPG.beginDraw();
      textPG.background(BLACK, 0);  // Make the background alpha=0 in case of overlap
      textPG.fill(WHITE);
      textPG.textFont(font);
      textPG.text(s, 0, (textPG.height - size)/2 + font.ascent()*size);
      textPG.endDraw();

      this.x1 = 0;
      this.x2 = compression * textPG.width;
      this.v1 = 0;
      this.v2 = 0;

      this.v = v;
      this.x = 0;
    }

    /**
     * Returns the current width.
     */
    float width() {
      return x2 - x1;
    }

    /**
     * Returns the current X position.
     */
    float x() {
      return x;
    }

    /**
     * Sets the current X position.
     */
    void setX(float x) {
      this.x = x;
    }

    void draw(PGraphics parentPG) {
      int area = textPG.width * textPG.height;
      float drawWidth = width();
      float drawHeight = area / drawWidth;
//      parentPG.noFill();
//      parentPG.stroke(WHITE);
//      parentPG.rect(x1, (parentPG.height - drawHeight)/2, drawWidth, drawHeight);
      parentPG.image(textPG, x + x1, (parentPG.height - drawHeight)/2, drawWidth, drawHeight);
    }

    void doPhysics(float dt) {
      float speed = speedKnob.getValuef();
      x += v * speed * dt;
      x1 += v1 * dt;
      x2 += v2 * dt;

      // Spring equation: F = -kx
      // Where x is how much the spring has been compressed and k is the
      // spring constant
      // Combine the mass into k: a = -kx

      float a1;
      float a2;
      float compression = textPG.width - (x2 - x1);
      if (compression != 0) {
        float a = SPRING_K * stiffnessKnob.getValuef() * compression;
        a1 = -(a + DAMPING*(v1 - v2)) / 2;
        a2 = -a1;
      } else {
        a1 = 0;
        a2 = 0;
      }
      v1 += a1 * dt;
      v2 += a2 * dt;
    }

    @Override
    public String toString() {
      return text;
    }
  }
}

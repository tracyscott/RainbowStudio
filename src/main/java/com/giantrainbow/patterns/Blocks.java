/*
 * Created by shawn on 8/3/18 10:33 PM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.colors.Colors.BLACK;
import static com.giantrainbow.colors.Colors.WHITE;
import static processing.core.PApplet.lerp;
import static processing.core.PApplet.max;
import static processing.core.PApplet.min;
import static processing.core.PApplet.pow;
import static processing.core.PApplet.round;
import static processing.core.PApplet.tan;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.HALF_PI;
import static processing.core.PConstants.HSB;
import static processing.core.PConstants.P3D;
import static processing.core.PConstants.PI;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.StringParameter;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PShape;

/**
 * Randomly rotating blocks.
 * <p>
 * Note: The blocks are skewed near the ends not because of the rainbow curvature,
 * but because of how I'm doing perspective.</p>
 *
 * @author Shawn Silverman
 */
@LXCategory(LXCategory.FORM)
public class Blocks extends PGPixelPerfect {
  private Box[] boxes;

  private static final int SEGMENT_W = 15 * 4;
  private static final int PANEL_H = 30;

  private static final float BOX_W = SEGMENT_W * 0.9f;
  private static final float BOX_H = PANEL_H * 0.9f;

  private final PFont font;
  private final PShape logo;

  // Controls
  private final StringParameter textControl =
      new StringParameter("Text", "R INBOW")
          .setDescription("Text on the blocks");

  public Blocks(LX lx) {
    super(lx, P3D);

    // Create the boxes
    boxes = new Box[7];
    for (int i = 0; i < boxes.length; i++) {
      boxes[i] = new Box(BOX_W, BOX_H);
    }

    font = RainbowStudio.pApplet.createFont("fonts/Roboto/Roboto-Bold.ttf", PANEL_H);

    // From: https://commons.wikimedia.org/wiki/File:Burning-Man.svg
    // Linked from: https://eplaya.burningman.org/viewtopic.php?t=54052
    // From Google search: simple burning man logo vector
    logo = pg.loadShape("img/Burning-Man.svg");

    addParameter(textControl);
  }

  @Override
  public void onActive() {
    // Default parameter setup
    fpsKnob.setValue(GLOBAL_FRAME_RATE);

    // Graphics context setup
    pg.beginDraw();
    pg.colorMode(HSB, 1.0f);
    pg.textFont(font);
    pg.endDraw();
  }

  @Override
  protected void draw(double deltaDrawMs) {
    pg.background(BLACK);
//    pg.ortho();
    pg.camera(pg.width/2.0f, pg.height/2.0f, 3.0f*pg.height, pg.width/2.0f, pg.height/2.0f, 0, 0, 1, 0);
    float cameraZ = (pg.height/2.0f) / tan(PI*60.0f/360.0f);
    pg.perspective(PI/8.0f, (float) pg.width/(float) pg.height, cameraZ/10.0f, cameraZ*10.0f);
    pg.directionalLight(1.0f, 0.0f, 1.0f, 0, 0, -1);
    //pg.pointLight(1.0f, 0, 1.0f, pg.width/2.0f, pg.height/2.0f, pg.height);

    for (int i = 0; i < boxes.length; i++) {
      if (boxes[i].isDone()) {
        boxes[i].restart(
            Colors.randomColor(4),
            HALF_PI * (random.nextBoolean()  ? -1 : 1),
            pow(2.0f, lerp(1, -1, random.nextInt(boxes.length)/(boxes.length - 1.0f))));
      }

      pg.pushMatrix();
      pg.translate(SEGMENT_W/2.0f + SEGMENT_W*i, PANEL_H/2.0f, 0);
      boxes[i].draw(pg, i);
      pg.popMatrix();
    }
  }

  /**
   * Represents one rotatable box.
   */
  private final class Box {
    // Drawing
    private int color1;
    private int color2;
    private float w;
    private float h;
    private float angle1;
    private float angle2;

    // Rotation
    private float totalTicks;
    private int tick;
    private float t;  // The parametric value

    Box(float w, float h) {
      this.color2 = Colors.randomColor(4);
      this.w = w;
      this.h = h;
      this.angle1 = 0.0f;
    }

    /**
     * Restarts the box rotation. Color1 takes on the old color2 value, and
     * a new "side two" is positioned.
     *
     * @param color2 the new side two color
     * @param angle2 the new side two position
     * @param duration the rotation duration
     */
    void restart(int color2, float angle2, float duration) {
      this.color1 = this.color2;
      this.color2 = color2;
      this.angle2 = angle2;
      this.totalTicks = max(round(duration * fpsKnob.getValuef()), 1);
      this.tick = 0;
      this.t = 0.0f;
    }

    /**
     * Checks if this is done rotating.
     */
    boolean isDone() {
      return tick >= totalTicks;
    }

    /**
     * Draws this box. It is assumed that this has been translated to an
     * appropriate position. This does not save or restore the transformation
     * matrix.
     * <p>
     * The specific box index is given so specific things can be chosen to
     * be drawn on top of the box.</p>
     */
    private void draw(PGraphics pg, int index) {
      if (tick < totalTicks) {
        t = (float) tick / (totalTicks - 1);
        tick++;
      } else {
        t = 1.0f;
      }

      draw(pg, t, index);
    }

    /**
     * Draws the box and uses the parametric parameter {@code t} as its
     * position. The value can range from 0-1. This does not save or restore
     * the transformation matrix.
     */
    private void draw(PGraphics pg, float t, int index) {
      // Side 1
      pg.rotateX(lerp(angle1, -angle2, t));
      drawSide(pg, color1, index);

      // Side 2
      pg.rotateX(angle2 - angle1);
      drawSide(pg, color2, index);
    }

    /**
     * Draws one side of the box.
     */
    private void drawSide(PGraphics pg, int c, int index) {
      pg.translate(0, 0, h/2);
      pg.fill(c);

      int stroke;
      if (pg.brightness(c) < 0.5f) {
        stroke = WHITE;
      } else {
        stroke = BLACK;
      }
      pg.stroke(stroke);
      float sw = 1.5f;
      pg.strokeWeight(sw);
      // Rounded corner = 5
      pg.rect(-w/2.0f + sw/2.0f, -h/2.0f + sw/2.0f, w - sw, h - sw, 5.0f);

      String text = textControl.getString();
      String s;
      if (text != null && index < text.length()) {
        s = text.substring(index, index + 1);
      } else {
        s = " ";
      }

      // Side 1 contents
      if (!" ".equals(s)) {
        pg.fill(stroke);
        pg.textSize(h);

        // Display the text just above the rect to avoid flicker
        pg.text(s,
            -pg.textWidth(s)/2.0f,
            pg.textAscent()/2.0f - pg.textDescent()/2.0f,
            0.01f);
      } else {
        pg.pushMatrix();
        pg.pushStyle();
        float scale = min(w/2.0f, h/2.0f)*0.8f;
        pg.scale(scale);
        pg.translate(0, 0, 0.01f);  // Avoid flicker by displaying just in front
        pg.shapeMode(CENTER);
        logo.setFill(stroke);
        pg.shape(logo, 0, 0, 2.0f, 2.0f);
        //drawLogo(pg);
        pg.popStyle();
        pg.popMatrix();
      }
      pg.translate(0, 0, -h/2.0f);
    }
  }
}

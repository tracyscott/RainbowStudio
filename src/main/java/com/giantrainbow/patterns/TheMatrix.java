/*
 * Created by shawn on 8/13/18 6:12 PM.
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.RainbowStudio.pApplet;
import static com.giantrainbow.colors.Colors.BLACK;
import static processing.core.PConstants.P2D;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;

// Inspired from the code here:
// https://forum.processing.org/one/topic/challenge-re-create-the-iconic-matrix-visual-effect-in-most-succinct-and-flexible-algorithm.html
@LXCategory(LXCategory.FORM)
public class TheMatrix extends PGTexture {
  private static final float FONT_SIZE = 15.0f;
  private static final float X_INCREMENT = 2 * FONT_SIZE;
  private static final float MAX_Y_INCREMENT = FONT_SIZE / 2;

  private int frameCount;

  private final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 0.5, 0.0, 1.0)
          .setDescription("Speed control");

  public TheMatrix(LX lx) {
    super(lx, P2D);

    addParameter(speedKnob);
  }

  @Override
  protected void setup() {
    fpsKnob.setValue(GLOBAL_FRAME_RATE);

    pg.textFont(pApplet.createFont("Arial", FONT_SIZE));
    frameCount = 0;

    pg.background(BLACK);
  }

  @Override
  protected void draw(double deltaDrawMs) {
    float yInc = MAX_Y_INCREMENT * speedKnob.getValuef();
    pg.fill(0, 10);
    pg.rect(0, 0, pg.width, pg.height);
    pg.fill(0, pg.height/2, 0);
    for (float x = X_INCREMENT; x < pg.width; x += X_INCREMENT) {
      float y = (frameCount*yInc + pApplet.noise(x)*pg.height*x)%pg.height;
      pg.text((char) random.nextInt(256), x, y);
    }

    frameCount++;
  }
}

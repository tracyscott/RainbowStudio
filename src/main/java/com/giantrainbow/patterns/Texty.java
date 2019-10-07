package com.giantrainbow.patterns;

import static processing.core.PConstants.PI;
import static processing.core.PConstants.BOTTOM;
import static processing.core.PConstants.CENTER;

import com.giantrainbow.colors.Colors;
import com.giantrainbow.model.space.Space3D;
import com.giantrainbow.model.RainbowBaseModel;
import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import heronarts.lx.parameter.DiscreteParameter;
import org.joml.Vector3f;
import processing.core.PImage;
import processing.core.PVector;
import processing.core.PFont;

@LXCategory(LXCategory.FORM)
public class Texty extends CanvasPattern2D {

  public static final int MAX_FONT_SIZE = 120;

  public final CompoundParameter sizeKnob =
      new CompoundParameter("Size", 80, 10, MAX_FONT_SIZE).setDescription("Size");

  public Texty(LX lx) {
    super(lx);
    addParameter(sizeKnob);
    removeParameter(fpsKnob);

    this.font = RainbowStudio.pApplet.createFont("fonts/Roboto/Roboto-Regular.ttf", MAX_FONT_SIZE, false);
  }

  PFont font;

  public static final float lowRadius = RainbowBaseModel.innerRadius - 0 * RainbowBaseModel.radiusInc;
  public static final float highRadius = RainbowBaseModel.innerRadius + 30 * RainbowBaseModel.radiusInc;

  public void onActive() {
    super.onActive();
  }

  public void draw(double deltaMs) {
      pg.background(0);

      if (font != null) {
	  pg.textFont(font);
      }
      pg.textSize((float)sizeKnob.getValue());
      pg.textAlign(CENTER, BOTTOM);

      for (int i = 0; i < 26; i++) {
	  float theta = PI * 2 * i / 26;
	  pg.pushMatrix();
	  pg.translate(canvas.map.subXi((float)Math.cos(theta) * lowRadius),
		       canvas.map.subYi((float)Math.sin(theta) * lowRadius));
	  pg.fill(255,255,255);
	  pg.rotate(theta+PI/2);
	  pg.text(String.format("%s", i), 0, 0);
	  pg.fill(255,0,0);
	  pg.ellipse(0, 0, 10, 10);
	  pg.popMatrix();
      }

      for (int i = 0; i < 26; i++) {
	  float theta = PI * 2 * i / 26;
	  pg.pushMatrix();
	  pg.translate(canvas.map.subXi((float)Math.cos(theta) * highRadius),
		       canvas.map.subYi((float)Math.sin(theta) * highRadius));
	  pg.fill(255,0,0);
	  pg.ellipse(0, 0, 10, 10);
	  pg.popMatrix();
      }
  }
}

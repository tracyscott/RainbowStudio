package com.giantrainbow.patterns;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.CLOSE;
import static processing.core.PConstants.LINES;
import static processing.core.PConstants.PI;

import com.giantrainbow.colors.Colors;
import com.giantrainbow.model.space.Space3D;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import org.joml.Vector3f;
import processing.core.PImage;
import processing.core.PVector;

@LXCategory(LXCategory.FORM)
public class Ferris extends CanvasPattern2D {

  // Radians per millisecond
  public final float SPIN_RATE = 1.f / 1000.f;

  // Speed determines the overall speed of the entire pattern.
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 2.5, 0, 10).setDescription("Speed");

  // The "rotate" paramter determines how fast the whole thing spins.
  public final CompoundParameter rotateKnob =
      new CompoundParameter("Rotate", .25, -1, 1).setDescription("Rotate");

  float WHEEL_CENTER_X = (float) canvas.width() / 2f;

  // Note: the exact center could be computed via geometry from the
  // model, but this is subjective.  The Y-center is off-screen below
  // the rainbow, thus has Y coordinate < 0.
  float WHEEL_CENTER_Y = (float) canvas.height() * -0.13f;

  // Determines how elliptical the curve is.
  public final static float WIDTH_RATIO = 0.95f;
  public final static float HEIGHT_RATIO = 0.98f;

  public final static int CAR_COUNT = 10;

  final float ELLIPSE_A = (canvas.width() * WIDTH_RATIO) / 2f;
  final float ELLIPSE_B = (canvas.width() * HEIGHT_RATIO) / 2f;

  final float CAR_RADIUS = canvas.width() * 0.04f;

  final float CAR_OPEN = PI * 3 / 8;
  final float CAR_CLOSE = PI * 15 / 8;
  final float CAR_OFFSET = -CAR_RADIUS * 0.9f;

  double telapsed;
  double relapsed;

  public Ferris(LX lx) {
    super(lx);

    addParameter(speedKnob);
    addParameter(rotateKnob);
    removeParameter(fpsKnob);
  }

  public void draw(double deltaMs) {
    pg.background(0);

    double speed = speedKnob.getValue();
    telapsed += (float) (deltaMs * speed);
    double rotate = rotateKnob.getValue();
    relapsed += (float) (deltaMs * rotate);

    pg.ellipseMode(CENTER);

    // Center the coordinate system
    pg.translate(WHEEL_CENTER_X, WHEEL_CENTER_Y);

    // Reference line for the wheel:
    // 
    // drawEllipse(2 * ELLIPSE_A, 2 * ELLIPSE_B);

    drawStructure();
  }

  public void drawEllipse(float width, float height) {
	pg.noFill();
	pg.stroke(255, 255, 255);
	pg.strokeWeight(2);

	pg.pushMatrix();

	pg.ellipse(0, 0, width, height);

	pg.popMatrix();
    }

    public void drawStructure() {
	pg.pushMatrix();

	float step = 2 * PI / CAR_COUNT;
	float rotate = (float)(relapsed * SPIN_RATE);

	pg.fill(0, 255, 0);
	pg.noStroke();

	for (int i = 0; i < CAR_COUNT; i++) {
	    float theta1 = rotate + i * step;
	    float x1 = ELLIPSE_A * (float)Math.cos(theta1);
	    float y1 = ELLIPSE_B * (float)Math.sin(theta1);

	    pg.pushMatrix();
	    pg.translate(x1, y1);
	    pg.translate(0, CAR_OFFSET);
	    pg.scale(-1, 1);
	    pg.arc(0, 0, 2 * CAR_RADIUS, 2 * CAR_RADIUS, CAR_OPEN, CAR_CLOSE);
	    pg.popMatrix();	
	}

	pg.noFill();
	pg.stroke(0, 0, 255);
	pg.strokeWeight(5);

	for (int i = 0; i < CAR_COUNT; i++) {
	    float theta1 = rotate + i * step;
	    float theta2 = rotate + (i+1) * step;
	    float x1 = ELLIPSE_A * (float)Math.cos(theta1);
	    float y1 = ELLIPSE_B * (float)Math.sin(theta1);
	    float x2 = ELLIPSE_A * (float)Math.cos(theta2);
	    float y2 = ELLIPSE_B * (float)Math.sin(theta2);

	    pg.line(0, 0, x1, y1);
	    pg.line(x1, y1, x2, y2);
	}

	pg.popMatrix();	
    }
}

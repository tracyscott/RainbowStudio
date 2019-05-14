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
import com.giantrainbow.patterns.ferris.Amusement;

import org.dyn4j.dynamics.World;

@LXCategory(LXCategory.FORM)
public class Ferris extends CanvasPattern2D {

  // Radians per millisecond
  public final float SPIN_RATE = 1.f / 1000.f;

  // Speed determines the overall speed of the entire pattern.
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 0.1, -1, 1).setDescription("Speed");

  // The "torque" paramter determines how fast the whole thing spins.
  public final CompoundParameter torqueKnob =
      new CompoundParameter("Torque", 1000, 0, 1e12).setDescription("Torque");

  float WHEEL_CENTER_X = (float) canvas.width() / 2f;

  // Note: the exact center could be computed via geometry from the
  // model, but this is subjective.  The Y-center is off-screen below
  // the rainbow, thus has Y coordinate < 0.
  float WHEEL_CENTER_Y = (float) canvas.height() * -0.13f;

  // Determines how elliptical the curve is.
  public final static float WIDTH_RATIO = 0.95f;
  public final static float HEIGHT_RATIO = 0.98f;

  final float ELLIPSE_A = (canvas.width() * WIDTH_RATIO) / 2f;
  final float ELLIPSE_B = (canvas.width() * HEIGHT_RATIO) / 2f;

  final float CAR_RADIUS = canvas.width() * 0.04f;

  final float CAR_OPEN = PI * 3 / 8;
  final float CAR_CLOSE = PI * 15 / 8;
  final float CAR_OFFSET = -CAR_RADIUS * 0.9f;

  final Amusement ferris;
  final World world;

  double telapsed;
  double relapsed;

  public Ferris(LX lx) {
    super(lx);

    this.world = new World();
    this.ferris = new Amusement(world, Math.max(ELLIPSE_A, ELLIPSE_B), CAR_RADIUS, CAR_OFFSET);

    addParameter(speedKnob);
    addParameter(torqueKnob);
    removeParameter(fpsKnob);
  }

  public void draw(double deltaMs) {
    pg.background(0);
    world.update(deltaMs/1000);

    ferris.axle.setMotorSpeed(speedKnob.getValue());
    ferris.axle.setMaximumMotorTorque(torqueKnob.getValue());

    relapsed += ferris.wheel.getChangeInOrientation();

    // System.err.println("WHEEL VELOC: " + ferris.wheel.getAngularVelocity());
    // System.err.println("WHEEL POSIT: " + ferris.wheel.getTransform().getRotation());

    // double speed = speedKnob.getValue();
    // telapsed += (float) (deltaMs * speed);
    // double rotate = rotateKnob.getValue();
    // relapsed += (float) (deltaMs * rotate);

    pg.ellipseMode(CENTER);

    // Center the coordinate system
    pg.translate(WHEEL_CENTER_X, WHEEL_CENTER_Y);

    // Spokes, etc.
    drawStructure();

    // Reference line for the wheel.
    drawEllipse(2 * ELLIPSE_A, 2 * ELLIPSE_B);
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

	float step = 2 * PI / Amusement.CAR_COUNT;
	float rotate = (float)(relapsed); //  * SPIN_RATE);

	pg.fill(0, 255, 0);
	pg.noStroke();

	for (int i = 0; i < Amusement.CAR_COUNT; i++) {
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

	for (int i = 0; i < Amusement.CAR_COUNT; i++) {
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

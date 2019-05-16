package com.giantrainbow.patterns;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.CLOSE;
import static processing.core.PConstants.LINES;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.RADIUS;


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

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Vector2;

@LXCategory(LXCategory.FORM)
public class Ferris extends CanvasPattern2D {

  static final double RATE = 10000;

  // Speed determines the desired speed of the wheel.
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 0 * RATE, -100 * RATE, 100 * RATE).setDescription("Speed");

  // Torque determines the motor's output.
  public final CompoundParameter torqueKnob =
      new CompoundParameter("Torque", 1e12, 0, 1e13).setDescription("Torque");

  public final CompoundParameter gravityKnob =
      new CompoundParameter("Gravity", 1e4, 0, 1e5).setDescription("Gravity");

  public final CompoundParameter rotateGravityKnob =
      new CompoundParameter("RotateGravity", 0, 0, Math.PI * 2).setDescription("Rotate Gravity");
    
  public final CompoundParameter brakeKnob =
      new CompoundParameter("Brake", 100, 0, 10000).setDescription("Brake");

  public final CompoundParameter carBrakeKnob =
      new CompoundParameter("CarBrake", 10, 0, 1000).setDescription("Car Brake");

  float WHEEL_CENTER_X = (float) canvas.width() / 2f;

  // Note: the exact center could be computed via geometry from the
  // model, but this is subjective.  The Y-center is off-screen below
  // the rainbow, thus has Y coordinate < 0.
  float WHEEL_CENTER_Y = (float) canvas.height() * -0.13f;

  // Determines how elliptical the curve is.
  public final static float WIDTH_RATIO = 0.95f;
  public final static float HEIGHT_RATIO = 0.98f;

  final float WHEEL_R = canvas.width() / 2f;

  final float ELLIPSE_A = WHEEL_R * WIDTH_RATIO;
  final float ELLIPSE_B = WHEEL_R * HEIGHT_RATIO;

  final float CAR_RADIUS = canvas.width() * 0.04f;

  final float CAR_OPEN = PI * 3 / 8;
  final float CAR_CLOSE = PI * 15 / 8;
  final float CAR_OFFSET = -CAR_RADIUS * 0.9f;

  final float BAR_WIDTH = 0.1f;
  final float SEAT_OFFSET = 0.75f;
  final float SEAT_WIDTH = 1.5f;
  final float CAR_HT = 1.7f;

  final Amusement ferris;
  final World world;

  double wheelRotation;

  public Ferris(LX lx) {
    super(lx);

    this.world = new World();
    this.ferris = new Amusement(world, WHEEL_R, CAR_RADIUS);
    
    addParameter(speedKnob);
    addParameter(torqueKnob);
    addParameter(rotateGravityKnob);
    addParameter(gravityKnob);
    addParameter(brakeKnob);
    addParameter(carBrakeKnob);
    removeParameter(fpsKnob);
  }

  public void draw(double deltaMs) {
    pg.background(0);

    ferris.wheel.setAngularDamping(brakeKnob.getValue());
    for (Body car : ferris.carriages) {
	car.setAngularDamping(carBrakeKnob.getValue());
    }

    world.setGravity(new Vector2(0, -gravityKnob.getValue()).rotate(rotateGravityKnob.getValue()));
    world.update(deltaMs);

    ferris.axle.setMotorSpeed(speedKnob.getValue() / RATE);
    ferris.axle.setMaximumMotorTorque(torqueKnob.getValue());

    pg.ellipseMode(CENTER);

    // Center the coordinate system
    pg.translate(WHEEL_CENTER_X, WHEEL_CENTER_Y);

    // Spokes, etc.
    drawStructure();

    // Reference line for the wheel.
    // drawEllipse(2 * ELLIPSE_A, 2 * ELLIPSE_B);
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

	double wheelRotation = ferris.wheel.getTransform().getRotation();

	for (int i = 0; i < Amusement.CAR_COUNT; i++) {
	    float theta1 = (float)(wheelRotation + i * Amusement.CAR_STEP);
	    float x1 = (float)(WHEEL_R * Math.cos(theta1));
	    float y1 = (float)(WHEEL_R * Math.sin(theta1));

	    Vector2 bary = ferris.carriages[i].getTransform().getTranslation();
	    Vector2 rota = new Vector2(x1, y1).subtract(bary);

	    pg.pushMatrix();
	    pg.translate(WIDTH_RATIO * x1, HEIGHT_RATIO * y1);
	    pg.rotate((float)(rota.getDirection() - Math.PI / 2));
	    pg.translate(0, CAR_OFFSET);
	    drawCar();
	    pg.popMatrix();
	}

	pg.noFill();
	pg.stroke(0, 0, 255);
	pg.strokeWeight(5);

	for (int i = 0; i < Amusement.CAR_COUNT; i++) {
	    float theta1 = (float)(wheelRotation + i * Amusement.CAR_STEP);
	    float theta2 = (float)(wheelRotation + (i+1) * Amusement.CAR_STEP);
	    float x1 = ELLIPSE_A * (float)Math.cos(theta1);
	    float y1 = ELLIPSE_B * (float)Math.sin(theta1);
	    float x2 = ELLIPSE_A * (float)Math.cos(theta2);
	    float y2 = ELLIPSE_B * (float)Math.sin(theta2);

	    pg.line(0, 0, x1, y1);
	    pg.line(x1, y1, x2, y2);
	}

	pg.popMatrix();
    }

    void drawCar() {
	pg.noStroke();
	pg.rectMode(RADIUS);

	float seatY = -CAR_RADIUS * SEAT_OFFSET;
	float seatLX = -SEAT_WIDTH * CAR_RADIUS / 2;
	float seatRX = -seatLX;

	pg.fill(0, 255, 255);

	pg.beginShape();
	pg.curveVertex(seatLX + 10,  seatY - CAR_HT * CAR_RADIUS);
	pg.curveVertex(seatLX,  seatY);
	pg.curveVertex(seatLX,  seatY + CAR_HT * CAR_RADIUS);
	pg.curveVertex(seatRX,  seatY + CAR_HT * CAR_RADIUS);
	pg.curveVertex(seatRX,  seatY);
	pg.curveVertex(seatRX - 10,  seatY - CAR_HT * CAR_RADIUS);
	pg.endShape();

	pg.fill(70, 25, 0);

	pg.rect(0, 0, BAR_WIDTH * CAR_RADIUS, CAR_RADIUS);
	pg.rect(0, -CAR_RADIUS * SEAT_OFFSET, SEAT_WIDTH * CAR_RADIUS / 2, BAR_WIDTH * CAR_RADIUS);
    }
}

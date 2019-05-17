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
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import org.joml.Vector3f;
import processing.core.PImage;
import processing.core.PVector;
import com.giantrainbow.patterns.ferris.Amusement;
import com.giantrainbow.textures.Flowers;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.MassType;
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

  public final CompoundParameter boosterKnob =
      new CompoundParameter("Booster", 0, -1e9, 1e9).setDescription("Booster");

  public final CompoundParameter wheelDensityKnob =
      new CompoundParameter("WheelDensity", 100, 10, 1000).setDescription("Wheel Density");
    
  public final BooleanParameter variableEllipseKnob =
      new BooleanParameter("VarEllipse", true);

  public final BooleanParameter partyModeKnob =
      new BooleanParameter("PartyMode", false);
    
  final float WHEEL_CENTER_X = (float) canvas.width() / 2f;

  // Note: the exact center could be computed via geometry from the
  // model, but this is subjective.  The Y-center is off-screen below
  // the rainbow, thus has Y coordinate < 0.
  final float WHEEL_CENTER_Y = (float) canvas.height() * -0.13f;

  final Vector2 CENTER_PT = new Vector2(WHEEL_CENTER_X, WHEEL_CENTER_Y);

  // Determines how elliptical the curve is.
  public final static float WIDTH_RATIO = 0.95f;
  public final static float HEIGHT_RATIO = 0.98f;

  final float WHEEL_R = canvas.width() / 2f;

  final float CAR_RADIUS = canvas.width() * 0.04f;
  final float SPINNY_RADIUS = canvas.width() * 0.02f;

  final float CAR_OPEN = PI * 3 / 8;
  final float CAR_CLOSE = PI * 15 / 8;
  final float CAR_OFFSET = -CAR_RADIUS * 0.9f;

  final float BAR_WIDTH = 0.1f;
  final float SEAT_OFFSET = 0.75f;
  final float SEAT_WIDTH = 1.5f;
  final float CAR_HT = 1.7f;

  final Amusement ferris;
  final World world;

  Flowers flowers;
  double relapsed;
  BackgroundPulse pulse;
    
  public Ferris(LX lx) {
    super(lx);

    this.world = new World();
    this.ferris = new Amusement(world, WHEEL_R, CAR_RADIUS);
    this.flowers = new Flowers(Amusement.CAR_COUNT);
    this.pulse = new BackgroundPulse(this, "Color");
    
    addParameter(speedKnob);
    addParameter(torqueKnob);
    addParameter(rotateGravityKnob);
    addParameter(gravityKnob);
    addParameter(brakeKnob);
    addParameter(carBrakeKnob);
    addParameter(boosterKnob);
    addParameter(wheelDensityKnob);
    addParameter(variableEllipseKnob);
    addParameter(partyModeKnob);

    pulse.levelKnob.setValue(1);

    removeParameter(fpsKnob);
  }

  public void draw(double deltaMs) {
    relapsed += deltaMs;

    pg.background(0);

    ferris.wheel.setAngularDamping(brakeKnob.getValue());
    ferris.wheelFixture.setDensity(wheelDensityKnob.getValue() / 1000);
    ferris.wheel.setMass(MassType.NORMAL);

    for (Body car : ferris.carriages) {
	car.setAngularDamping(carBrakeKnob.getValue());
	car.applyImpulse(boosterKnob.getValue());
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
  }

    public void drawStructure() {
	pg.pushMatrix();

	double wheelRotation = ferris.wheel.getTransform().getRotation();

	float ellipseA = WHEEL_R * WIDTH_RATIO;
	float ellipseB = WHEEL_R * HEIGHT_RATIO;

	if (variableEllipseKnob.getValueb()) {
	    double radSum = 0;
	    double radCnt = 0;

	    // Compute the average radius
	    for (int i = 0; i < Amusement.CAR_COUNT; i++) {
		float theta1 = (float)(wheelRotation + i * Amusement.CAR_STEP);
		float cosTheta = (float) Math.cos(theta1);
		float sinTheta = (float) Math.sin(theta1);
		float x1 = (float)(WHEEL_R * cosTheta);
		float y1 = (float)(WHEEL_R * sinTheta);

		Vector2 bary = ferris.carriages[i].getTransform().getTranslation();

		if (y1 > 0) {
		    // Average of visible radii.
		    double centMag = bary.getMagnitude();

		    radSum += sinTheta * centMag;
		    radCnt += sinTheta;
		}
	    }

	    // Adjust the center-line
	    double avgRad = radSum / radCnt;
	    double shiftBy = avgRad - WHEEL_R;

	    if (shiftBy > 0) {
		ellipseB *= (1 - shiftBy / 300);
	    }
	}

	// Draw the cars
	for (int i = 0; i < Amusement.CAR_COUNT; i++) {
	    float theta1 = (float)(wheelRotation + i * Amusement.CAR_STEP);
	    float cosTheta = (float) Math.cos(theta1);
	    float sinTheta = (float) Math.sin(theta1);
	    float x1 = (float)(WHEEL_R * cosTheta);
	    float y1 = (float)(WHEEL_R * sinTheta);

	    Vector2 bary = ferris.carriages[i].getTransform().getTranslation();
	    Vector2 rota = new Vector2(x1, y1).subtract(bary);

	    pg.pushMatrix();
	    pg.translate(ellipseA * cosTheta, ellipseB * sinTheta);
	    pg.rotate((float)(rota.getDirection() - Math.PI / 2));
	    pg.translate(0, CAR_OFFSET);
	    drawCar();
	    pg.popMatrix();
	}

	// Draw the wheel
	pg.noFill();

	float troll = (float)(relapsed / 1000);

	for (int i = 0; i < Amusement.CAR_COUNT; i++) {
	    float theta1 = (float)(wheelRotation + i * Amusement.CAR_STEP);
	    float theta2 = (float)(wheelRotation + (i+1) * Amusement.CAR_STEP);
	    float x1 = ellipseA * (float)Math.cos(theta1);
	    float y1 = ellipseB * (float)Math.sin(theta1);
	    float x2 = ellipseA * (float)Math.cos(theta2);
	    float y2 = ellipseB * (float)Math.sin(theta2);

	    pg.stroke(138, 173, 5);
	    pg.strokeWeight(6);

	    pg.line(0, 0, x1, y1);
	    pg.line(x1, y1, x2, y2);

	    PImage texture = flowers.getTexture(6 + (i % 2));
	    if (texture != null) {
		pg.pushMatrix();
		pg.translate((x1+x2)/2, (y1+y2)/2);
		pg.rotate((theta1 + theta2) / 2);
		pg.rotate(troll);

		pg.beginShape();

		pg.noStroke();
		pg.texture(texture);

		float r = SPINNY_RADIUS;
		float w = texture.width;
		pg.vertex(-r, -r, 0, 0);
		pg.vertex(r, -r, w, 0);
		pg.vertex(r, r, w, w);
		pg.vertex(-r, r, 0, w);
		pg.endShape();
	
		pg.popMatrix();
	    }
	}

	pg.popMatrix();
    }

    void drawCar() {
	pg.noStroke();
	pg.rectMode(RADIUS);

	float seatY = -CAR_RADIUS * SEAT_OFFSET;
	float seatLX = -SEAT_WIDTH * CAR_RADIUS / 2;
	float seatRX = -seatLX;

	pg.fill(105, 183, 206);

	pg.beginShape();
	pg.curveVertex(seatLX + 10,  seatY - CAR_HT * CAR_RADIUS);
	pg.curveVertex(seatLX,  seatY);
	pg.curveVertex(seatLX,  seatY + CAR_HT * CAR_RADIUS);
	pg.curveVertex(seatRX,  seatY + CAR_HT * CAR_RADIUS);
	pg.curveVertex(seatRX,  seatY);
	pg.curveVertex(seatRX - 10,  seatY - CAR_HT * CAR_RADIUS);
	pg.endShape();

	pg.fill(223, 93, 34);

	pg.rect(0, 0, BAR_WIDTH * CAR_RADIUS, CAR_RADIUS);
	pg.rect(0, -CAR_RADIUS * SEAT_OFFSET, SEAT_WIDTH * CAR_RADIUS / 2, BAR_WIDTH * CAR_RADIUS);
    }
}

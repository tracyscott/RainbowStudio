package com.giantrainbow.patterns.ferris;

import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Vector2;

public class Amusement {

    public final static int CAR_COUNT = 12;
    public final static double CAR_STEP = 2 * Math.PI / CAR_COUNT;

    public final static double WHEEL_DENSITY = .1;
    public final static double CAR_DENSITY = 1;

    public Body mount;
    public Body wheel;
    public BodyFixture wheelFixture;
    public RevoluteJoint axle;
    public Body []carriages;
    public RevoluteJoint []carriageAxles;

    public Amusement(World world, double wheelRadius, double carRadius) {
	this.mount = new Body();
	this.wheel = new Body();
	this.carriages = new Body[CAR_COUNT];
	this.carriageAxles = new RevoluteJoint[CAR_COUNT];

	// The mount is a unit disk.
	mount.addFixture(new BodyFixture(Geometry.createCircle(1)));
	mount.setMass(MassType.INFINITE);
	world.addBody(mount);

	// The wheel is a real-sized disk.
	wheelFixture = new BodyFixture(Geometry.createCircle(wheelRadius));
	wheelFixture.setDensity(WHEEL_DENSITY);
	wheelFixture.setFriction(0);
	wheel.addFixture(wheelFixture);
	wheel.setMass(MassType.NORMAL);
	world.addBody(wheel);

	// Mount the wheel.
	this.axle = new RevoluteJoint(mount, wheel, new Vector2(0, 0));
	axle.setLimitEnabled(false);
	axle.setLimits(Math.toRadians(0.0), Math.toRadians(0.0));
	axle.setReferenceAngle(Math.toRadians(0.0));
	axle.setMotorEnabled(true);
	axle.setMotorSpeed(Math.toRadians(0.0));
	axle.setMaximumMotorTorque(0);
	axle.setCollisionAllowed(false);
	world.addJoint(axle);

	for (int i = 0; i < CAR_COUNT; i++) {
	    double theta = i * CAR_STEP;
	    double x = (wheelRadius) * Math.cos(theta);
	    double y = (wheelRadius) * Math.sin(theta);

	    Body car = new Body();
	    car.translate(x, y);
	    car.translate(0, -carRadius);
	    RevoluteJoint carAxle = new RevoluteJoint(wheel, car, new Vector2(x, y));

	    this.carriages[i] = car;
	    this.carriageAxles[i] = carAxle;

	    // The carriage is a real-sized disk.
	    BodyFixture carFixture = new BodyFixture(Geometry.createCircle(carRadius));
	    carFixture.setDensity(CAR_DENSITY);
	    carFixture.setFriction(0);
	    car.addFixture(carFixture);
	    car.setMass(MassType.NORMAL);
	    world.addBody(car);

	    // Mount the carriage.
	    carAxle.setLimitEnabled(false);
	    carAxle.setLimits(Math.toRadians(0.0), Math.toRadians(0.0));
	    carAxle.setReferenceAngle(0);
	    carAxle.setMotorEnabled(false);
	    carAxle.setMotorSpeed(Math.toRadians(0.0));
	    carAxle.setMaximumMotorTorque(0);
	    carAxle.setCollisionAllowed(false);
	    world.addJoint(carAxle);
	}
    }
};

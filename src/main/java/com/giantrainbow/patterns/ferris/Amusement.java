package com.giantrainbow.patterns.ferris;

import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

public class Amusement {

    public final static int CAR_COUNT = 10;

    public final static double WHEEL_KG = 100000;

    public final static double CARRIER_KG = 100;
    public final static double CARRIAGE_KG = 10;

    public final static double TORQUE = 1000;

    Body mount;
    Body wheel;
    Body []carriage;
    Body []carrier;

    public Amusement(World world) {
	this.mount = new Body();
	this.wheel = new Body();
	this.carriage = new Body[CAR_COUNT];
	this.carrier = new Body[CAR_COUNT];
	
	mount.setMass(MassType.INFINITE);
	wheel.setMass(WHEEL_KG);

	RevoluteJoint axle = new RevoluteJoint(mount, wheel, new Vector2(0, 0));
	axle.setLimitEnabled(false);
	axle.setLimits(Math.toRadians(0.0), Math.toRadians(0.0));
	axle.setReferenceAngle(Math.toRadians(0.0));
	axle.setMotorEnabled(true);
	axle.setMotorSpeed(Math.toRadians(0.0));
	axle.setMaximumMotorTorque(TORQUE);
	axle.setCollisionAllowed(false);

	world.addJoint(axle);

	for (int i = 0; i < CAR_COUNT; i++) {
	    
	}
	
	for (int i = 0; i < CAR_COUNT; i++) {
	    carrier[i] = new Body();
	    carriage[i] = new Body();
	    // carrier[i].setMass(CARRIER_KG);
	    // carriage[i].setMass(CARRIAGE_KG);
	}
    }
};

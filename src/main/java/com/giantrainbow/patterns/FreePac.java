package com.giantrainbow.patterns;

import static processing.core.PConstants.PI;
import static processing.core.PConstants.BOTTOM;
import static processing.core.PConstants.TOP;
import static processing.core.PConstants.CENTER;

import com.giantrainbow.model.space.Lissajous;

import com.giantrainbow.colors.Colors;
import com.giantrainbow.colors.Gradient;
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
import java.util.ArrayList;
import java.util.Collections;

@LXCategory(LXCategory.FORM)
public class FreePac extends CanvasPattern2D {
    public final static String[] topSeven = {
	"Free Iraq",
	"Free Kashmir",
	"Free Pacman",
	"Free Palestine",
	"Free People",
	"Free Tibet",
	"Free Ukraine",
    };

    public final static double letterFreqs[] = {
	0.1201954987,    // E
	0.09098588613,   // T
	0.08123837787,   // A
	0.07681168165,   // O
	0.07305420097,   // I
	0.06947773761,   // N
	0.06280752374,   // S
	0.06021294219,   // R
	0.05921460426,   // H
	0.04319182899,   // D
	0.0397854122,    // L
	0.02877626808,   // U
	0.02711419999,   // C
	0.02611586205,   // M
	0.02303856766,   // F
	0.02113514314,   // Y
	0.02094864045,   // W
	0.02025748342,   // G
	0.0181894977,    // P
	0.01489278838,   // B
	0.0110749686,    // V
	0.006895114178,  // K
	0.001727892574,  // X
	0.001124501517,  // Q
	0.001031250171,  // J
	0.0007021277763, // Z
    };

    public static final float lowRadius =
	RainbowBaseModel.innerRadius - 0 * RainbowBaseModel.radiusInc;
    public static final float highRadius =
	RainbowBaseModel.innerRadius + 30 * RainbowBaseModel.radiusInc;
    public static final float rangeRadius = highRadius - lowRadius;

    // Note these are in degrees.
    //   RainbowBaseModel.rainbowThetaStart;
    //   RainbowBaseModel.rainbowThetaFinish;
    public static final float rainbowAngleStart =
	(float) Math.toRadians(RainbowBaseModel.rainbowThetaStart);
    public static final float rainbowAngleFinish =
	(float) Math.toRadians(RainbowBaseModel.rainbowThetaFinish);
    public static final float rangeAngle = rainbowAngleFinish - rainbowAngleStart;

    public static final int MAX_FONT_SIZE = 120;

    public static final int NUM_LETTERS = 120;
    
    public static final float TOO_CLOSE = rangeRadius / 6;

    public static final double HZ = 4000;

    // STRIDE is a full step P1..P3
    public static final float STRIDE = rangeRadius * 0.9f;

    public final CompoundParameter sizeKnob =
	new CompoundParameter("FontSize", 68.20, 10, MAX_FONT_SIZE).setDescription("FontSize");

    public final CompoundParameter speedKnob =
	new CompoundParameter("Speed", 5, 0, 10).setDescription("Speed");
    
    // Weird. Without this the image is not cenetered on the visible
    // region?  Fix it here.
    public final CompoundParameter xshiftKnob =
	new CompoundParameter("XShift", -3.4, -10, 10).setDescription("XShift");

    public final BooleanParameter brightKnob =
	new BooleanParameter("Bright", false);
    
    PFont font;
    PImage colorPlane;
    Letter [][]letters;
    ArrayList<Letter> depth;
    Random rnd = new Random();
    double elapsed;
    long epoch;
    boolean init;
    Gradient fullGradient;
    
    public FreePac(LX lx) {
	super(lx);
	addParameter(sizeKnob);
	addParameter(speedKnob);
	addParameter(xshiftKnob);
	addParameter(brightKnob);
	removeParameter(fpsKnob);

	this.font = RainbowStudio.pApplet.createFont("fonts/Roboto/Roboto-Regular.ttf",
						     MAX_FONT_SIZE, false);
	this.colorPlane = RainbowStudio.pApplet.loadImage("images/lab-square-lookup.png");
	this.letters = new Letter[26][];
	this.depth = new ArrayList<Letter>();

	out:
	for (int l = 0; l < 26; l++) {
	    int num = Math.max(1, (int)(NUM_LETTERS * letterFreqs[l]));

	    letters[l] = new Letter[num];

	    for (int i = 0; i < num; i++) {
		letters[l][i] = new Letter((char)('A' + l), depth.size());
		depth.add(letters[l][i]);
		if (depth.size() >= NUM_LETTERS) {
		    break out;
		}
	    }
	}
	Collections.shuffle(depth);
    }

    class Point {
	// World-space coords

	// double theta;
	// double radius;
	double X;
	double Y;

	private Point() {}
	
	void setAngular(double theta, double radius) {
	    this.X = Math.cos(theta) * radius;
	    this.Y = Math.sin(theta) * radius;
	}

	double heading() {
	    return Math.atan2(Y, X);
	}

	Point sub(Point a) {
	    Point r = new Point();
	    r.X = X - a.X;
	    r.Y = Y - a.Y;
	    // Note: theta and radius not computed
	    return r;
	}

	Point add(Point a) {
	    Point r = new Point();
	    r.X = X + a.X;
	    r.Y = Y + a.Y;
	    // Note: theta and radius not computed
	    return r;
	}

	Point scale(double s) {
	    X *= s;
	    Y *= s;
	    // Note: modify in place
	    return this;
	}

	void set(Point a) {
	    this.X = a.X;
	    this.Y = a.Y;
	}
    }

    class Letter {
	String ch;
	int color;
	int number;

	// P1, P2, P3 form a line (P1-P3) and its midpoint (P2).
	// P0 is the prior epoch's midpoint
	// H is the derivative (heading)
	// P is the current position in t=(elapsed%1) from P0 to P2.
	Point P, H, P0, P1, P2, P3;
	
	Letter(char ch, int number) {
	    this.ch = String.format("%s", ch);
	    this.P0 = randPos();
	    this.P1 = randPosNear(this.P0, null, STRIDE/2, null);
	    this.P2 = new Point();
	    this.P3 = randPosNear(this.P1, P0, STRIDE, P2);
	    this.color = randColor();
	    this.number = number;
	}

	void update() {
	    P0.set(P2);
	    P1.set(P3);
	    P3 = randPosNear(this.P1, this.P0, STRIDE, P2);
	}

	void advance() {
	    double t = elapsed % 1.;
	    
	    // System.err.println("ADVANCE " + String.format("%.10f", t));
	    double tt1 = (1 - t)*(1 - t);
	    double tt0 = t*t;

	    // https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Quadratic_B%C3%A9zier_curves
	    // See B(T) and B'(T).
	    this.P = P1.add(P0.sub(P1).scale(tt1)).add(P2.sub(P1).scale(tt0));
	    this.H = P1.sub(P0).scale(2*(1-t)).add(P2.sub(P1).scale(2*t));
	}

	void draw() {
	    pg.pushMatrix();
	    pg.translate(canvas.map.subXi((float)(this.P.X)),
			 canvas.map.subYi((float)this.P.Y));

	    if (brightKnob.getValue() > 0) {
		pg.fill(fullGradient.index(number));
	    } else {
		pg.fill(this.color);
	    }

	    pg.rotate((float)(this.H.heading() + PI/2));

	    pg.pushMatrix();
	    pg.translate(0, (float)(sizeKnob.getValue()/2.));
	    pg.text(ch, 0, 0);
	    pg.popMatrix();

	    pg.popMatrix();

	    // pg.stroke(255);
	    // pg.strokeWeight(2);

	    // pg.fill(255, 0, 0);
	    // pg.ellipse(canvas.map.subXi((float)P.X), canvas.map.subYi((float)P.Y), 5, 5);

	    // pg.fill(255);
	    // pg.ellipse(canvas.map.subXi((float)P0.X), canvas.map.subYi((float)P0.Y), 5, 5);
	    // pg.ellipse(canvas.map.subXi((float)P1.X), canvas.map.subYi((float)P1.Y), 5, 5);
	    // pg.ellipse(canvas.map.subXi((float)P2.X), canvas.map.subYi((float)P2.Y), 5, 5);

	    // pg.line(canvas.map.subXi((float)P0.X), canvas.map.subYi((float)P0.Y),
	    // 	    canvas.map.subXi((float)P1.X), canvas.map.subYi((float)P1.Y));
	    // pg.line(canvas.map.subXi((float)P1.X), canvas.map.subYi((float)P1.Y),
	    // 	    canvas.map.subXi((float)P2.X), canvas.map.subYi((float)P2.Y));
	}
    };

    public int randColor() {
	return colorPlane.get(rnd.nextInt(colorPlane.width),
			      rnd.nextInt(colorPlane.height));
    }

    public Point randPos() {
	double theta = rainbowAngleStart + rnd.nextDouble() * rangeAngle;
	double radius = lowRadius + rnd.nextDouble() * rangeRadius;
	Point p = new Point();
	p.setAngular(theta, radius);
	return p;
    }

    public Point randPosNear(Point near, Point far, float dist, Point mid) {
	double rot;
	for (int loop = 0; ; loop++) {
	    // System.err.println("rPN " + dist);
	    rot = rnd.nextDouble() * 2 * Math.PI;

	    double dx = dist * Math.cos(rot);
	    double dy = dist * Math.sin(rot);

	    double midx = near.X + dx/2;
	    double midy = near.Y + dy/2;

	    if (far != null && loop < 2) {
		double sx = far.X - midx;
		double sy = far.Y - midy;
		double farDist = Math.sqrt(sx * sx + sy * sy);

		if (farDist < TOO_CLOSE) {
		    continue;
		}			
	    }

	    double midTheta = Math.atan2(midy, midx);
	    double midRadius = Math.sqrt(midx*midx+midy*midy);

	    boolean inside =
		(midTheta >= rainbowAngleStart && midTheta <= rainbowAngleFinish &&
		 midRadius >= lowRadius && midRadius <= highRadius);

	    if (!inside) {
		continue;
	    }

	    if (mid != null) {
		mid.X = midx;
		mid.Y = midy;
	    }
	    
	    double x = near.X + dx;
	    double y = near.Y + dy;

	    Point p = new Point();
	    p.X = x;
	    p.Y = y;
	    return p;
	}
    }

    public void draw(double deltaMs) {
	elapsed += deltaMs * speedKnob.getValue() / HZ;

	if (!init) {
	    this.init = true;
	    this.fullGradient = Gradient.compute(pg, NUM_LETTERS);
	}

	long newEpoch = (long)elapsed;
	if (newEpoch > epoch) {
	    epoch = newEpoch;

	    for (Letter l : depth) {
		l.update();
	    }
	}

	pg.background(0);
	pg.translate((float)xshiftKnob.getValue(), 0);

	if (font != null) {
	    pg.textFont(font);
	}
	pg.textSize((float)sizeKnob.getValue());
	pg.textAlign(CENTER, BOTTOM);

	for (Letter l : depth) {
	    l.advance();
	    l.draw();
	}
    }
}

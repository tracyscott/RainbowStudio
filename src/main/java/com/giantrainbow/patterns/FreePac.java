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
import com.giantrainbow.textures.Strange;
import com.giantrainbow.textures.Positioner;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import java.util.Random;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.joml.Vector3f;
import processing.core.PImage;
import processing.core.PVector;
import processing.core.PFont;
import java.util.ArrayList;
import java.util.Collections;

@LXCategory(LXCategory.FORM)
public class FreePac extends CanvasPattern2D implements Positioner {
    public final static String[] messages = {
	"Hello Liverpool",
	"Goodbye Covid",
	"Keep Calm And Shine On",
	"Black Lives Matter",
	"Peace Hope Love",
    };

    public final static int letterFreqs[] = {
	9, // A
	2, // B
	2, // C
	4, // D
	12, // E
	2, // F
	3, // G
	3, // H+1
	9, // I
	1, // J
	1, // K
	4, // L
	2, // M
	6, // N
	8, // O
	2, // P
	1, // Q
	6, // R
	4, // S
	6, // T
	4, // U
	2, // V
	2, // W
	1, // X
	2, // Y
	1 // Z
    };

    public static final float lowRadius =
	RainbowBaseModel.innerRadius - 0 * RainbowBaseModel.radiusInc;
    public static final float highRadius =
	RainbowBaseModel.innerRadius + 30 * RainbowBaseModel.radiusInc;
    public static final float centerRadius = (highRadius + lowRadius) / 2;
    public static final float rangeRadius = highRadius - lowRadius;

    // Note these are in degrees.
    //   RainbowBaseModel.rainbowThetaStart;
    //   RainbowBaseModel.rainbowThetaFinish;
    public static final float rainbowAngleStart =
	(float) Math.toRadians(RainbowBaseModel.rainbowThetaStart);
    public static final float rainbowAngleFinish =
	(float) Math.toRadians(RainbowBaseModel.rainbowThetaFinish);
    public static final float rangeAngle = rainbowAngleFinish - rainbowAngleStart;

    public static final float messageAngleStart =
	(float) Math.toRadians(30);
    public static final float messageAngleFinish =
	(float) Math.toRadians(150);

    public static final int MAX_FONT_SIZE = 120;

    public static final double HZ = 7000;

    public static final long MIN_PERIOD = 10;
    public static final long MAX_PERIOD = 20;

    // STRIDE is a full step P1..P3
    public static final float STRIDE = rangeRadius * 0.9f;

    public final CompoundParameter sizeKnob =
	new CompoundParameter("FontSize", 60, 10, MAX_FONT_SIZE).setDescription("FontSize");

    public final CompoundParameter speedKnob =
	new CompoundParameter("Speed", 5, 0, 10).setDescription("Speed");

    PFont font;
    PImage colorPlane;
    Letter [][]letters;
    ArrayList<Letter> depth;
    Random rnd = new Random();

    Strange strange;
    PImage currentStrange;

    int [][]messageCharIndex;

    ShowState showState;
    double elapsed;
    long epoch;
    long wanderUntil;
    int nextGoalMsgIndex;

    enum ShowState {
	WANDERING,
	APPROACHING,
	ARRIVING,
	MESSAGING,
	DEPARTING,
	RESTARTING1,
	RESTARTING2,
    };

    enum GoalState {
	UNINVOLVED,
	ATTRACTED,
	LOCKEDIN,
	SHOWING,
	MOVINGON,
    };

    static {
	for (int i = 0; i < messages.length; i++) {
	    messages[i] = messages[i].toUpperCase();
	}
    }

    public FreePac(LX lx) {
	super(lx);
	addParameter(sizeKnob);
	addParameter(speedKnob);
	removeParameter(fpsKnob);

	this.font = RainbowStudio.pApplet.createFont("fonts/Roboto/Roboto-Regular.ttf",
						     MAX_FONT_SIZE, false);
	this.colorPlane = RainbowStudio.pApplet.loadImage("images/lab-square-lookup.png");
	this.letters = new Letter[26][];
	this.depth = new ArrayList<Letter>();

	this.showState = ShowState.WANDERING;
	this.elapsed = 0;
	this.epoch = 0;

	this.messageCharIndex = new int[messages.length][];
	this.strange = new Strange(this, this, "Pulse");

	out:
	for (int l = 0; l < 26; l++) {
	    int num = letterFreqs[l];

	    letters[l] = new Letter[num];

	    for (int i = 0; i < num; i++) {
		letters[l][i] = new Letter((char)('A' + l), depth.size());
		depth.add(letters[l][i]);
	    }
	}
	Collections.shuffle(depth);

	// Check for sufficient number of letters
	for (int i = 0; i < messages.length; i++) {
	    HashMap<Integer, Integer> m = new HashMap<Integer, Integer>();
	    String msg = messages[i];
	    int []positions = new int[msg.length()];

	    for (int j = 0; j < msg.length(); j++) {
		char c = msg.charAt(j);
		if (c == ' ') {
		    continue;
		}
		int mc = (int)(c-'A');
		int ex = m.getOrDefault(mc, 0);
		positions[j] = ex;
		m.put(mc, 1 + ex);
	    }

	    for (int j = 0; j < 26; j++) {
		int avail = letterFreqs[j];

		int want = m.getOrDefault(j, 0);
		if (want > avail) {
		    // Just add to letterFreqs
		    throw new RuntimeException("Too few letters, need more " + (char)('A' + j));
		}
	    }

	    messageCharIndex[i] = positions;
	}
	setNextMessage(0);
    }

    void setNextMessage(int g) {
	this.wanderUntil =
	    this.epoch +
	    MIN_PERIOD +
	    (long)(rnd.nextDouble() * (MAX_PERIOD - MIN_PERIOD + 0.5));

	nextGoalMsgIndex = g;

	for (Letter l : depth) {
	    l.goal = GoalState.UNINVOLVED;
	    l.targetTheta = 0;
	}

	String msg = messages[g];
	int len = msg.length();
	double interval = (messageAngleFinish - messageAngleStart) / (len-1);

	for (int p = 0; p < len; p++) {
	    char ch = msg.charAt(p);

	    if (ch == ' ') {
		continue;
	    }
	    int idx = messageCharIndex[g][p];

	    Letter l = letters[(int)(ch - 'A')][messageCharIndex[g][p]];

	    depth.remove(l);
	    depth.add(l);

	    l.goal = GoalState.ATTRACTED;
	    l.targetTheta = messageAngleStart + (float)(p * interval);
	}
    }

    class Point {
	// World-space coords
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

	double length() {
	    return Math.sqrt(lengthSquared());
	}
	double lengthSquared() {
	    return X * X + Y * Y;
	}

	Point norm1() {
	    Point n = new Point();
	    n.X = -Y;
	    n.Y = X;
	    return n.scale(1/length());
	}

	Point norm2() {
	    Point n = new Point();
	    n.X = Y;
	    n.Y = -X;
	    return n.scale(1/length());
	}
    }

    class Letter {
	String ch;
	int color;
	int number;
	GoalState goal;
	float targetTheta;

	// P1, P2, P3 form a line (P1-P3) and its midpoint (P2).
	// P0 is the prior epoch's midpoint
	// H is the derivative (heading)
	// P is the current position in t=(elapsed%1) from P0 to P2.
	Point P, H, P0, P1, P2, P3;

	Letter(char ch, int number) {
	    this.ch = String.format("%s", ch);
	    this.P0 = randPos();
	    this.P1 = randPosNear(this.P0, null, STRIDE/2, null, null);
	    this.P2 = new Point();
	    this.P3 = randPosNear(this.P1, P0, STRIDE, P2, null);
	    this.color = randColor();
	    this.number = number;
	    this.goal = GoalState.UNINVOLVED;
	}

	void update() {
	    P0.set(P2);
	    P1.set(P3);

	    if (goal == GoalState.ATTRACTED && showState == ShowState.ARRIVING) {
		// Set P2 and P3 intentionally: step 1.
		// P1 is the former P3 and we know it's < 2*STRIDE
		// distance to the target.
		Point tgt = targetPos();
		Point gap = P1.sub(tgt);
		gap.scale(0.5);

		// Gap is the mid-point
		double legLen = Math.sqrt(STRIDE*STRIDE - gap.lengthSquared());

		// Choose an arbitrary norm, TODO could choose the better one.
		Point norm = gap.norm1();
		Point leg = gap.add(norm.scale(legLen));
		P3.set(P1.add(leg));
		P2.set(P1.add(leg.scale(0.5)));
		this.goal = GoalState.LOCKEDIN;

	    } else if (goal == GoalState.LOCKEDIN) {

		P3 = targetPos();
		Point v = P3.sub(P1);
		P2 = P1.add(v.scale(0.5));
		this.goal = GoalState.SHOWING;

	    } else if (goal == GoalState.SHOWING) {

		P3 = postTargetPos();
		P2 = P1.add(P3.sub(P1).scale(0.5));
		this.goal = GoalState.MOVINGON;

	    } else if (goal == GoalState.MOVINGON) {

		P3 = randPosNear(this.P1, this.P0, STRIDE, P2, this);

	    } else if (goal == GoalState.UNINVOLVED) {

		P3 = randPosNear(this.P1, this.P0, STRIDE, P2, this);

	    } else {

		P3 = randPosNear(this.P1, this.P0, STRIDE, P2, this);
	    }
	}

	void advance() {
	    double t = elapsed % 1.;

	    double tt1 = (1 - t)*(1 - t);
	    double tt0 = t*t;

	    // https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Quadratic_B%C3%A9zier_curves
	    // See B(T) and B'(T).
	    this.P = P1.add(P0.sub(P1).scale(tt1)).add(P2.sub(P1).scale(tt0));
	    this.H = P1.sub(P0).scale(2*(1-t)).add(P2.sub(P1).scale(2*t));
	}

	Point targetPos() {
	    // Target position is the P1 of the last Bezier curve before
	    // reaching the display position (which is P2).
	    Point p = new Point();
	    p.X = (float)Math.cos(targetTheta) * (centerRadius - STRIDE*5/8);
	    p.Y = (float)Math.sin(targetTheta) * (centerRadius - STRIDE*5/8);
	    return p;
	}

	Point postTargetPos() {
	    Point p = new Point();
	    p.X = (float)Math.cos(targetTheta) * (centerRadius + STRIDE*3/8);
	    p.Y = (float)Math.sin(targetTheta) * (centerRadius + STRIDE*3/8);
	    return p;
	}

	void draw() {
	    pg.pushMatrix();
	    pg.translate(canvas.map.subXi((float)(this.P.X)),
			 canvas.map.subYi((float)this.P.Y));

	    int a = 255;
	    int c = this.color;

	    if (goal == GoalState.UNINVOLVED) {
		if (showState == ShowState.DEPARTING) {
		    double t = elapsed % 1.;
		    a = (int)(255 * (1-t));
		} else if (showState == ShowState.RESTARTING1) {
		    c = 0;
		} else if (showState == ShowState.RESTARTING2) {
		    double t = elapsed % 1.;
		    a = (int)(255 * t);
		}
	    } else {
		float f = (targetTheta - rainbowAngleStart) / rangeAngle;
		c = currentStrange.get((int)(420.*f), 0);
	    }

	    pg.fill(Colors.red(c), Colors.green(c), Colors.blue(c), a);

	    pg.rotate((float)(this.H.heading() + PI/2));

	    pg.pushMatrix();
	    pg.translate(0, (float)(sizeKnob.getValue()/2.));
	    pg.text(ch, 0, 0);
	    pg.popMatrix();

	    pg.popMatrix();
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

    public Point randPosNear(Point near, Point far, float dist, Point mid, Letter l) {
	for (int loop = 0; ; loop++) {
	    double rot = rnd.nextDouble() * 2 * Math.PI;

	    double dx = dist * Math.cos(rot);
	    double dy = dist * Math.sin(rot);

	    double midx = near.X + dx/2;
	    double midy = near.Y + dy/2;

	    if (far != null && loop < 5) {
		double sx = far.X - midx;
		double sy = far.Y - midy;
		double farDist = Math.sqrt(sx * sx + sy * sy);

		if (farDist < dist/4) {
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

	    double x = near.X + dx;
	    double y = near.Y + dy;

	    if (l != null && l.goal != GoalState.UNINVOLVED && loop < 10) {
		double theta = Math.atan2(y, x);

		double dTheta = Math.abs(theta - l.targetTheta);

		double curDTheta = Math.abs(Math.atan2(near.Y, near.X) - l.targetTheta);

		if (dTheta > curDTheta) {
		    continue;
		}
	    }


	    if (mid != null) {
		mid.X = midx;
		mid.Y = midy;
	    }
	    Point p = new Point();
	    p.X = x;
	    p.Y = y;
	    return p;
	}
    }

    boolean inRange() {
	// Uses P3 as the position, consdering this test runs at the
	// end of an interval before update(), so we've approached P2 and
	// are about to change course heading toward P3.  The next stride
	// in this case should put us in range so that the step from P3
	// to the base control point is < 2 steps.
	String msg = messages[nextGoalMsgIndex];
	float maxd = 0;

	for (int p = 0; p < msg.length(); p++) {
	    char ch = msg.charAt(p);
	    if (ch == ' ') {
		continue;
	    }

	    Letter l = letters[(int)(ch - 'A')][messageCharIndex[nextGoalMsgIndex][p]];

	    maxd = Math.max(maxd, (float)l.P3.sub(l.targetPos()).length());
	}

	return maxd < 2 * STRIDE;
    }

    public void draw(double deltaMs) {
	double changeBy = deltaMs * speedKnob.getValue() / HZ;

	if (changeBy > 1) {
	    // This logic breaks badly if elapsed starts to jump by
	    // more than 1. Clamp it.
	    changeBy = 1;
	}
	elapsed += changeBy;

	currentStrange = strange.update(deltaMs);

	updateEpoch((long)elapsed);

	pg.background(0, 0);

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

    void updateEpoch(long newEpoch) {
	if (newEpoch < epoch) {
	    throw new RuntimeException("invalid epoch " + newEpoch);
	}
	if (newEpoch == epoch) {
	    return;
	}

	epoch = newEpoch;

	switch (showState) {
	case RESTARTING1:
	    showState = ShowState.RESTARTING2;
	    break;

	case RESTARTING2:

	    while (true) {
		int next = rnd.nextInt(messages.length);
		if (next != nextGoalMsgIndex) {
		    setNextMessage(next);
		    break;
		}
	    }
	    showState = ShowState.WANDERING;

	case WANDERING:

	    if (newEpoch >= wanderUntil) {
		showState = ShowState.APPROACHING;
	    }
	    break;

	case APPROACHING:
	    if (inRange()) {
		showState = ShowState.ARRIVING;
	    }

	    break;
	case ARRIVING:

	    showState = ShowState.MESSAGING;
	    break;

	case MESSAGING:

	    showState = ShowState.DEPARTING;
	    break;

	case DEPARTING:

	    showState = ShowState.RESTARTING1;
	    break;
	}

	for (Letter l : depth) {
	    l.update();
	}
    }

    // From EpilepticWarning.java
    int patterns[][] = {
	{0, 1, 2, 3},
	{4, 5, 6, 7},
	{8, 1, 9, 6, 8, 6, 9, 1},
    };

    public int[] getPositions(int period) {
	int idx = period % patterns.length;
	return patterns[idx];
    }
}

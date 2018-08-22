package com.giantrainbow.patterns;

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
public class CubeLineup extends CanvasPattern3D {

  public final int MAX_SIZE = 150;
  public final int MAX_CUBES = 300;
  public final float MAX_SPEED = 100000;

  public final float ROLL_RATE = 4;
  public final float MSHZ = 1.f / 10000.f;

  public final Vector3f DEFAULT_EYE = new Vector3f(0, Space3D.MIN_Y + 6, 60);

  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", Math.sqrt(MAX_SPEED), 10, MAX_SPEED).setDescription("Speed");
  public final CompoundParameter rollKnob =
      new CompoundParameter("Roll", 0.15, -1, 1).setDescription("Roll");
  public final CompoundParameter countKnob =
      new CompoundParameter("Count", MAX_CUBES / 20, 10, MAX_CUBES).setDescription("Count");

  public CubeLineup(LX lx) {
    super(lx);
    addParameter(speedKnob);
    addParameter(countKnob);
    addParameter(rollKnob);
    removeParameter(fpsKnob);

    space = new Space3D(DEFAULT_EYE);
    boxes = new Box[MAX_CUBES];
    Random rnd = new Random();

    eye = new PVector(space.eye.x, space.eye.y, space.eye.z);
    center = new PVector(space.center.x, space.center.y, space.center.z);

    int trials = 0;
    for (int i = 0; i < boxes.length; i++) {
      Box b;
      do {
        b = new Box(rnd);
        trials++;
      } while (!space.testBox(
          -b.radius(), -b.radius(), -b.radius(), b.radius(), b.radius(), b.radius()));

      boxes[i] = b;
    }

    System.err.printf(
        "Found boxes by %.1f%% rejection sampling\n", 100. * (float) boxes.length / (float) trials);
  }

  Box boxes[];
  double elapsed;
  double relapsed;
  PImage texture;
  Space3D space;
  PVector eye;
  PVector center;

  public class Box {
    PVector R;
    int W;
    float rotation;

    float radius() {
      return (float) W / 2;
    }

    float partW() {
      return W / (float) Colors.RAINBOW_PALETTE.length;
    }

    Box(Random rnd) {
      W = (int) (rnd.nextFloat() * MAX_SIZE);
      R = PVector.random3D();
    }

    void drawPart(float zoff, int C, int part) {
      pg.beginShape();

      pg.fill(C);

      float xmin = -radius() + (float) part * partW();
      float xmax = xmin + partW();

      pg.vertex(xmin, -radius(), zoff);
      pg.vertex(xmax, -radius(), zoff);
      pg.vertex(xmax, +radius(), zoff);
      pg.vertex(xmin, +radius(), zoff);
      pg.endShape();
    }

    void drawRect(float zoff) {
      for (int i = 0; i < Colors.RAINBOW_PALETTE.length; i++) {
        drawPart(zoff, Colors.RAINBOW_PALETTE[i], i);
      }
    }

    void drawSides() {
      pg.pushMatrix();

      drawRect(radius());
      drawRect(-radius());

      pg.popMatrix();
    }

    void draw3Sides() {
      drawSides();

      pg.pushMatrix();
      pg.rotateX(PI / 2);
      drawSides();
      pg.popMatrix();

      pg.pushMatrix();
      pg.rotateY(PI / 2);
      drawSides();
      pg.popMatrix();
    }

    void draw() {
      pg.pushMatrix();

      pg.rotate(rotation, R.x, R.y, R.z);

      draw3Sides();

      pg.popMatrix();
    }

    void update() {
      rotation = (float) (elapsed / 10000);
    }
  };

  public void draw(double deltaMs) {
    double speed = Math.log10(speedKnob.getValue());
    elapsed += deltaMs * speed;

    double rollspeed = rollKnob.getValue();
    relapsed += deltaMs * rollspeed;

    pg.noStroke();
    pg.background(0);

    float theta = ((float) relapsed) * ROLL_RATE * MSHZ;

    pg.camera(
        eye.x,
        eye.y,
        eye.z,
        center.x,
        center.y,
        center.z,
        (float) Math.sin(theta),
        (float) Math.cos(theta),
        0);

    for (Box b : boxes) {
      b.update();
    }

    for (int i = 0; i < (int) countKnob.getValue(); i++) {
      if (i >= boxes.length) {
        break;
      }
      boxes[i].draw();
    }
  }
}

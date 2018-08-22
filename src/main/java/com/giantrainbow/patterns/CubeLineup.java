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
  public final int MAX_CUBES = 1000;
  public final float MAX_SPEED = 100000;

  // public final float MAX_EYE_RADIUS_RATIO = 1.0;

  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", MAX_SPEED / 5, 10, MAX_SPEED).setDescription("Speed");
  public final CompoundParameter countKnob =
      new CompoundParameter("Count", MAX_CUBES / 5, 10, MAX_CUBES).setDescription("Count");

  public CubeLineup(LX lx) {
    super(lx);
    addParameter(speedKnob);
    addParameter(countKnob);
    removeParameter(fpsKnob);

    Vector3f eye = new Vector3f(0, Space3D.MIN_Y + 6, 60);

    space = new Space3D(eye);
    boxes = new Box[MAX_CUBES];
    rnd = new Random();

    int trials = 0;
    for (int i = 0; i < boxes.length; i++) {
      Box b;
      do {
        b = new Box();
        trials++;
      } while (!space.testBox(
          -b.radius(), -b.radius(), -b.radius(), b.radius(), b.radius(), b.radius()));

      boxes[i] = b;
    }

    System.err.printf(
        "Found boxes by %.1f%% rejection sampling\n", 100. * (float) boxes.length / (float) trials);
  }

  Random rnd;
  Box boxes[];
  double elapsed;
  PImage texture;
  Space3D space;

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

    Box() {
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

    pg.noStroke();
    pg.background(0);

    pg.camera(
        space.eye.x,
        space.eye.y,
        space.eye.z,
        space.center.x,
        space.center.y,
        space.center.z,
        0,
        1,
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

package com.giantrainbow.patterns;

import static com.giantrainbow.colors.Colors.RAINBOW_PALETTE;
import static com.giantrainbow.colors.Colors.rgb;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.RGB;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.canvas.Canvas;
import com.giantrainbow.model.space.Space3D;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import org.joml.Vector3f;
import processing.core.PImage;
import processing.core.PVector;

@LXCategory(LXCategory.FORM)
public class SpinnyBoxes extends CanvasPattern3D {

  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 1, 20).setDescription("Speed.");

  public SpinnyBoxes(LX lx) {
    super(lx, new Canvas(lx.model));
    fpsKnob.setValue(60);
    speedKnob.setValue(1);
    addParameter(speedKnob);

    Vector3f eye = new Vector3f(0, Space3D.MIN_Y + 6, 60);

    space = new Space3D(eye);
    boxes = new Box[100];
    rnd = new Random();
    texture = makeTexture();

    int trials = 0;
    for (int i = 0; i < boxes.length; i++) {
      Box b;
      do {
        b = new Box();
        trials++;
      } while (!space.testBox(
          b.X - b.radius(),
          b.Y - b.radius(),
          b.Z - b.radius(),
          b.X + b.radius(),
          b.Y + b.radius(),
          b.Z + b.radius()));

      boxes[i] = b;
    }

    System.err.printf(
        "Found boxes by %.1f%% rejection sampling\n", 100. * (float) boxes.length / (float) trials);
  }

  PImage makeTexture() {
    PImage img = RainbowStudio.pApplet.createImage(canvas.width(), canvas.width(), RGB);

    img.loadPixels();
    for (int i = 0; i < img.pixels.length; i++) {
      float x = (float) (i % canvas.width()) / (float) canvas.width();
      float x6 = x * 6;
      int xi = (int) x6;

      img.pixels[i] = RAINBOW_PALETTE[xi];
    }
    img.updatePixels();
    return img;
  }

  Random rnd;
  Box boxes[];
  double elapsed;
  PImage texture;
  Space3D space;

  final float maxSize = 150;

  public class Box {
    float X;
    float Y;
    float Z;
    int C;
    PVector R;
    int W;
    float S;

    float radius() {
      return (float) W / 2;
    }

    Box() {
      // TODO: Can't get `perspective` call below working right, these
      // set the sampling space boundary but aren't working w/
      // perspective below.
      X = 0;
      Y = 0;
      Z = 0;
      W = (int) (rnd.nextFloat() * maxSize);

      C = rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
      R = PVector.random3D();
      S = rnd.nextFloat();
    }

    void drawRect(float zoff) {
      pg.beginShape();

      pg.texture(texture);

      pg.fill(C);

      pg.vertex(-radius(), -radius(), zoff, 0, 0);
      pg.vertex(+radius(), -radius(), zoff, canvas.width(), 0);
      pg.vertex(+radius(), +radius(), zoff, canvas.width(), canvas.height());
      pg.vertex(-radius(), +radius(), zoff, 0, canvas.height());
      pg.endShape();
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

      pg.translate(X, Y, -Z);
      pg.rotate((float) (speedKnob.getValue() * elapsed * PI / 10000.), R.x, R.y, R.z);

      draw3Sides();

      pg.popMatrix();
    }
  };

  public void draw(double deltaMs) {
    elapsed += deltaMs;

    pg.lights();
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

    // TODO: Can't get this working right. Perspective should match the Space3D's eye position.
    //
    // pg.perspective(space.fovy(), space.aspect(), 0, Float.POSITIVE_INFINITY);

    for (Box box : boxes) {
      box.draw();
    }
  }
}

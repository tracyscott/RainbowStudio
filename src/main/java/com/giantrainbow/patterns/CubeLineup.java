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
public class CubeLineup extends CanvasPattern3D {

  public final int MAX_SIZE = 150;
  public final int MAX_CUBES = 1000;
  public final float MAX_SPEED = 100000;

  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", MAX_SPEED / 5, 10, MAX_SPEED).setDescription("Speed");
  public final CompoundParameter countKnob =
      new CompoundParameter("Count", MAX_CUBES / 5, 10, MAX_CUBES).setDescription("Count");

  public CubeLineup(LX lx) {
    super(lx, new Canvas(lx.model));
    addParameter(speedKnob);
    addParameter(countKnob);
    removeParameter(fpsKnob);

    Vector3f eye = new Vector3f(0, Space3D.MIN_Y + 6, 60);

    space = new Space3D(eye);
    boxes = new Box[MAX_CUBES];
    rnd = new Random();
    texture = makeTexture();

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

  public class Box {
    int C;
    PVector R;
    int W;
    float rotation;

    float radius() {
      return (float) W / 2;
    }

    Box() {
      W = (int) (rnd.nextFloat() * MAX_SIZE);
      C = rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
      R = PVector.random3D();
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
      boxes[i].draw();
    }
  }
}

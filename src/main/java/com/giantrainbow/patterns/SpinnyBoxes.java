package com.giantrainbow.patterns;

import static com.giantrainbow.colors.Colors.rgb;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.RGB;

import com.giantrainbow.canvas.Canvas;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import processing.core.PApplet;
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

    boxes = new Box[100];
    rnd = new Random();
    texture = makeTexture();

    for (int i = 0; i < boxes.length; i++) {
      boxes[i] = new Box();
    }
  }

  PImage makeTexture() {
    PApplet app = new PApplet();
    PImage img = app.createImage(canvas.width(), canvas.width(), RGB);
    // img.loadPixels();

    for (int i = 0; i < img.pixels.length; i++) {
      float widthFraction = (float) (i % canvas.width()) / (float) canvas.width();
      img.pixels[i] = rgb((int) (widthFraction * 255.), 0, 0);
    }

    // img.updatePixels();
    img.save("/Users/jmacd/Desktop/texture.png");
    return img;
  }

  Random rnd;
  Box boxes[];
  double elapsed;
  PImage texture;

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
      X = rnd.nextFloat() * canvas.width();
      Y = rnd.nextFloat() * canvas.height();
      Z = rnd.nextFloat() * canvas.width();
      W = (int) (rnd.nextFloat() * (float) maxSize);

      C = rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
      R = PVector.random3D();
      S = rnd.nextFloat();
    }

    void drawRect(float zoff) {
      pg.beginShape();

      // pg.texture(texture);

      pg.fill(C);

      pg.vertex(-radius(), -radius(), zoff, 0, 0);
      pg.vertex(+radius(), -radius(), zoff, 1, 0);
      pg.vertex(+radius(), +radius(), zoff, 1, 1);
      pg.vertex(-radius(), +radius(), zoff, 0, 1);
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

    for (Box box : boxes) {
      box.draw();
    }
  }
}

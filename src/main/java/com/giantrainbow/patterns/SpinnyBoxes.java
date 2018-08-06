package com.giantrainbow.patterns;

import static processing.core.PConstants.HSB;
import static processing.core.PConstants.PI;

import com.giantrainbow.canvas.Canvas;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;
import processing.core.PApplet;
import processing.core.PImage;

@LXCategory(LXCategory.FORM)
public class SpinnyBoxes extends CanvasPattern3D {
  public final CompoundParameter sizeKnob =
      new CompoundParameter("size", 1.0, 30.0).setDescription("Size");

  public SpinnyBoxes(LX lx) {
    super(lx, new Canvas(lx.model));
    fpsKnob.setValue(30);
    sizeKnob.setValue(20);
    addParameter(sizeKnob);

    boxes = new Box[100];
    rnd = new Random();

    for (int i = 0; i < boxes.length; i++) {
      boxes[i] = new Box();
    }
  }

  Random rnd;
  Box boxes[];
  double elapsed;

  final float maxSize = 150;

  public class Box {
    float X;
    float Y;
    float Z;
    int W;
    float S;
    PImage T;

    float radius() {
      return (float) W / 2;
    }

    Box() {
      X = rnd.nextFloat() * canvas.width();
      Y = rnd.nextFloat() * canvas.height();
      Z = rnd.nextFloat() * canvas.width();
      W = (int) (rnd.nextFloat() * (float) maxSize);
      S = rnd.nextFloat();
      T = makeTexture();
    }

    PImage makeTexture() {
      PApplet app = new PApplet();
      PImage img = app.createImage(W, W, HSB);
      img.loadPixels();

      for (int i = 0; i < img.pixels.length; i++) {
        img.pixels[i] =
            app.color(
                (int) 255 * rnd.nextFloat(),
                (int) 255 * rnd.nextFloat(),
                (int) 255 * rnd.nextFloat());
      }

      img.updatePixels();
      return img;
    }

    void drawSide() {
      pg.pushMatrix();
      pg.translate(0, 0, radius());

      pg.beginShape();
      // pg.texture(T);
      pg.fill(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
      pg.vertex(-radius(), -radius(), 0, 0, 0);
      pg.vertex(-radius(), +radius(), 0, 0, W);
      pg.vertex(+radius(), +radius(), 0, W, W);
      pg.vertex(+radius(), -radius(), 0, W, 0);
      pg.endShape();

      pg.popMatrix();
    }

    void draw3Sides() {
      drawSide();

      pg.pushMatrix();
      pg.rotateX(PI / 2);
      drawSide();
      pg.popMatrix();

      pg.pushMatrix();
      pg.rotateY(PI / 2);
      drawSide();
      pg.popMatrix();
    }

    void draw() {
      pg.pushMatrix();

      pg.translate(X, Y, -Z);

      draw3Sides();

      pg.rotateX(PI);
      pg.rotateY(PI);

      draw3Sides();

      pg.popMatrix();
    }
  };

  public void draw(double deltaMs) {
    elapsed += deltaMs;

    pg.background(0);

    pg.noFill();
    pg.noStroke();
    // pg.translate(canvas.width() / 2, canvas.height() - size, 0);
    // pg.rotateY(((int) currentFrame % 16) * PI / 16.0f);
    // pg.box(size);

    for (Box box : boxes) {
      box.draw();
    }
  }
}

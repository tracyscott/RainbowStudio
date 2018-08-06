package com.giantrainbow.canvas;

import static processing.core.PConstants.RGB;

import heronarts.lx.model.LXModel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import processing.core.PApplet;
import processing.core.PImage;

/** Canvas anti-aliases Euclidean coordinates onto Rainbow coordinates. */
public class Canvas {

  /** Buffer contains the current state of the canvas. */
  public Buffer buffer;

  /** Map contains the (static) mapping function from sub-pixel to rainbow pixel. */
  public Map map;

  public Canvas(LXModel model) {
    map = Map.newFromModel(model);
    buffer = new Buffer(map.size());
  }

  /**
   * circle renders a colorwheel circle at position `(x,y)` with radius `r`. `position` indicates
   * the angle of rotation.
   */
  public void circle(float x, float y, float r, float position) {
    int xbegin = map.subXi(x - r);
    int xend = map.subXi(x + r);

    int ybegin = map.subYi(y - r);
    int yend = map.subYi(y + r);

    float r2 = r * r;

    for (int xi = xbegin; xi <= xend; xi += 1) {
      float xd = map.iX(xi) - x;
      float xd2 = xd * xd;
      for (int yi = ybegin; yi <= yend; yi += 1) {
        if (xi < 0 || yi < 0 || xi >= map.width || yi >= map.height) {
          continue;
        }

        int idx = map.width * yi + xi;

        if (map.isFar(idx)) {
          continue;
        }

        float yd = map.iY(yi) - y;
        float yd2 = yd * yd;

        if (xd2 + yd2 > r2) {
          continue;
        }

        float theta = (float) (Math.atan(yd / xd) + (Math.PI / 2));

        if (xd < 0) {
          theta += Math.PI;
        }

        float hue = (float) (theta / (2 * Math.PI)) + position;
        float chroma = 0.95F;
        float level = 0.95F;

        buffer.setHSB(idx, hue, chroma, level);
      }
    }
  }

  /** render stores the current buffer into `output`. */
  public void render(int output[]) {
    for (int i = 0; i < output.length; i++) {
      output[i] = map.computePoint(i, buffer);
    }
  }

  /** width returns the sub-sampled width of the canvas. */
  public int width() {
    return map.width;
  }

  /** width returns the sub-sampled height of the canvas. */
  public int height() {
    return map.height;
  }

  /** resolution returns the dimension, _IN FEET_, of the canvas sub-pixels. */
  public float resolution() {
    return map.resolution;
  }

  /** dumpImage is a debugging aid to view the subpixel image as a PNG. */
  public void dumpImage() {
    final BufferedImage image =
        new BufferedImage(map.width, map.height, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = (Graphics2D) image.getGraphics();
    g.setBackground(Color.white);
    g.clearRect(0, 0, map.width, map.height);

    for (int yi = 0; yi < map.height; yi++) {
      for (int xi = 0; xi < map.width; xi++) {
        int idx = yi * map.width + xi;
        // Note: This flips the image for rainbow Y-space coordinates.
        image.setRGB(xi, map.height - yi - 1, buffer.get(idx));
      }
    }

    try {
      ImageIO.write(image, "PNG", new File("/Users/jmacd/Desktop/canvas.png"));
    } catch (IOException e) {
      System.err.println("IO exception" + e);
    }

    // N.B. the following would also work, except that we're in a
    // graphics-drawing context at this point, so...

    // PApplet app = new PApplet();
    // PGraphics img = app.createGraphics(map.width, map.height, P2D);
    // img.loadPixels();
    // buffer.copyInto(img.pixels);
    // img.scale(1, -1);
    // img.image(img, -map.width, 0);
    // img.updatePixels();
    // img.save("/Users/jmacd/Desktop/canvas.png");
  }

  public void dumpRainbow() {
    PApplet app = new PApplet();
    PImage img = app.createImage(map.width, map.height, RGB);
    img.loadPixels();

    for (int i = 0; i < img.pixels.length; i++) {
      img.pixels[i] = app.color(0, 90, 102);
    }

    img.updatePixels();
    img.save("/Users/jmacd/Desktop/rainbow.png");
  }
}

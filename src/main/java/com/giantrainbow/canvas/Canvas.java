package com.giantrainbow.canvas;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Canvas anti-aliases Euclidean coordinates onto Rainbow coordinates. */
public class Canvas {

  /** Buffer contains the current state of the canvas. */
  public Buffer buffer;

  /** Map contains the (static) mapping function from sub-pixel to rainbow pixel. */
  public Map map;

  LXModel model;

  public Canvas(LXModel model, LXPoint perimeter[]) {
    this.model = model;
    this.map = Map.newFromModel(model, perimeter);
    this.buffer = new Buffer(map.size());
  }

  /** render stores the current buffer into `output`. */
  public void render(int output[]) {
    for (int i = 0; i < output.length; i++) {

      // Note: something's wrong here, fixing it w/o really
      // understanding where the trouble lies.  We're mirroring in the
      // X dimension, things were flipped.  This probably relates to
      // the ever-complicating inversion of Y-coordinates.
      LXPoint p = model.points[i];

      int mirrorI = p.y * model.width + (model.width - 1 - p.x);

      output[i] = map.computePoint(mirrorI, buffer);
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
}

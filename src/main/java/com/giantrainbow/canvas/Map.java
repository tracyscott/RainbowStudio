package com.giantrainbow.canvas;

import static com.giantrainbow.colors.ColorHelpers.*;
import static processing.core.PConstants.RGB;

import com.giantrainbow.model.RainbowModel3D;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.math3.distribution.TDistribution;
import processing.core.PApplet;
import processing.core.PImage;

/** Map constructs a mapping from sub-sampled pixel to true pixel in the rainbow canvas. */
public class Map {
  // Aliasing confidence level for the max-distance point, to compute
  // density.  Smaller values treat all subpixels evenly, values
  // closer to 1 weigh nearby points more.
  final double aliasLevel = 0.98;

  // Units are in feet, here.  Sample one inch pixels.
  final float foot = 1;
  final float resolution = foot / 12.0f;

  // This prunes the search for nearby subpixels.
  final float searchLimit = foot / 2;

  int width;
  int height;

  // Indexed by subpixel coordinates, indicates subpixels that are
  // rendered.
  boolean isnear[];

  // Indexed by pixel indexes, positions[i] refers to an offset in
  // subpixels.  The i'th pixel samples from subpixels at
  // subpixels[position[i]..position[i+1]], same for subweights.
  int positions[];
  int subpixels[];
  float subweights[];

  float pxMin = Float.POSITIVE_INFINITY;
  float pyMin = Float.POSITIVE_INFINITY;
  float pxMax = Float.NEGATIVE_INFINITY;
  float pyMax = Float.NEGATIVE_INFINITY;

  /** newFromModel constructs a map using the points of the LXModel. */
  public static Map newFromModel(LXModel model) {
    try {
      Map map = new Map();
      map.buildFromModel(model);
      // map.dumpMap(model);
      return map;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  static class Pixel {
    float x, y;
    ArrayList<Integer> subs = new ArrayList<Integer>();

    Pixel(LXPoint lxp) {
      x = lxp.x;
      y = lxp.y;
    }
  }

  /** isFar returns true when the sub-pixel at index `idx` is not included in the rendering. */
  public boolean isFar(int idx) {
    return !isnear[idx];
  }

  /** size returns the number of sub-pixels in the canvas. */
  public int size() {
    return height * width;
  }

  /**
   * buildFromModel computes the subpixel-to-pixel mapping, excluding points that are nearest to the
   * perimeter or outside: `positions`, `subpixels`, and `isnear` are computed.
   */
  void buildFromModel(LXModel model) {
    // TODO: Add a model-perimeter interface to support testing w/ other
    // panel configurations.
    for (LXPoint pt : ((RainbowModel3D) model).perimeter) {
      pxMin = Math.min(pxMin, pt.x);
      pyMin = Math.min(pyMin, pt.y);
      pxMax = Math.max(pxMax, pt.x);
      pyMax = Math.max(pyMax, pt.y);
    }

    width = subXi(pxMax);
    height = subYi(pyMax);

    positions = new int[model.size + 1];
    isnear = new boolean[size()];

    // Temporary data structure for building the map.
    Pixel[] pixels = new Pixel[model.size];
    RTree<LXPoint, Point> tree = RTree.create();
    HashSet<LXPoint> perimeter = new HashSet<LXPoint>();

    // Array of pixel-to-subpixel lists
    for (LXPoint lxp : model.points) {
      pixels[lxp.index] = new Pixel(lxp);
    }

    // Build the Rtree from interior points.
    for (LXPoint lxp : model.points) {
      tree = tree.add(lxp, Geometries.point(lxp.x, lxp.y));
    }

    // Build the Rtree from the perimeter set.
    for (LXPoint lxp : ((RainbowModel3D) model).perimeter) {
      tree = tree.add(lxp, Geometries.point(lxp.x, lxp.y));
      perimeter.add(lxp);
    }

    int nearcount = 0;

    // For each subpixel, find the nearest pixel and if nearest is
    // not in the perimeter set, add it to the map.
    for (int xi = 0; xi < width; xi++) {
      float x = iX(xi);
      for (int yi = 0; yi < height; yi++) {
        float y = iY(yi);
        int idx = yi * width + xi;

        for (Entry<LXPoint, Point> point :
            tree.nearest(Geometries.point(x, y), searchLimit, 1).toBlocking().toIterable()) {
          LXPoint lxp = point.value();
          if (perimeter.contains(lxp)) {
            continue;
          }
          nearcount++;
          isnear[idx] = true;
          pixels[lxp.index].subs.add(idx);
        }
      }
    }

    int position = 0;
    subpixels = new int[nearcount];
    subweights = new float[nearcount];

    // Build positions: pixel subpixel index
    //       subpixels: subpixel values
    for (LXPoint lxp : model.points) {
      positions[lxp.index] = position;
      for (int sub : pixels[lxp.index].subs) {
        subpixels[position] = sub;
        position++;
      }
    }
    positions[model.size] = position;

    for (LXPoint lxp : model.points) {
      buildWeights(lxp);
    }

    System.out.printf(
        "Canvas has %d pixels; %dx%d=%d subpixels; %.0f%% covered\n",
        model.size, width, height, size(), 100.0 * (float) nearcount / (float) size());
  }

  /**
   * buildWeight computes the relative weight of each subpixel using a gaussian kernel w/ standard
   * deviation set according to the subpixel density and the aliasing confidence level. (I think I'm
   * saying this right)
   */
  void buildWeights(LXPoint lxp) {
    int end = positions[lxp.index + 1];
    int cnt = end - positions[lxp.index];
    float maxd = Float.NEGATIVE_INFINITY;

    // Compute the maximum distance
    for (int off = positions[lxp.index]; off < end; off++) {
      int subidx = subpixels[off];
      float x = iX(subXpos(subidx));
      float y = iY(subYpos(subidx));

      float dx = x - lxp.x;
      float dy = y - lxp.y;

      float d = (float) Math.sqrt(dx * dx + dy * dy);
      if (d > maxd) {
        maxd = d;
      }
    }

    // Set stddev using the math here, but solving for the standard deviation
    // instead of for the mean.
    //
    // https://stackoverflow.com/questions/5564621/
    //         using-apache-commons-math-to-determine-confidence-intervals
    // https://gist.github.com/gcardone/5536578
    TDistribution tDist = new TDistribution(cnt - 1);
    // Calculate critical value, standard deviation at the statistical-limit.
    double critVal = tDist.inverseCumulativeProbability(1.0 - (1 - aliasLevel) / 2);
    // Maxd is the maximum deviation for the aliasing confidence level.
    double stddev = maxd * Math.sqrt(cnt) / critVal;
    double variance = stddev * stddev;

    // Sum the gaussian PDF.
    float gsum = 0;
    for (int off = positions[lxp.index]; off < end; off++) {
      int subidx = subpixels[off];
      float x = iX(subXpos(subidx));
      float y = iY(subYpos(subidx));

      float dx = x - lxp.x;
      float dy = y - lxp.y;

      float dsquared = dx * dx + dy * dy;
      float d = (float) Math.sqrt(dsquared);

      float g = (float) Math.pow(Math.E, -dsquared / (2 * variance));

      gsum += g;
    }

    // Normalize
    for (int off = positions[lxp.index]; off < end; off++) {
      int subidx = subpixels[off];
      float x = iX(subXpos(subidx));
      float y = iY(subYpos(subidx));

      float dx = x - lxp.x;
      float dy = y - lxp.y;

      float dsquared = dx * dx + dy * dy;
      float d = (float) Math.sqrt(dsquared);

      float g = (float) Math.pow(Math.E, -dsquared / (2 * variance));
      float weight = g / gsum;

      subweights[off] = weight;
    }
  }

  int subXpos(int subidx) {
    return subidx % width;
  }

  int subYpos(int subidx) {
    return subidx / width;
  }

  int subXi(float val) {
    return (int) ((val - pxMin) / resolution);
  }

  int subYi(float val) {
    return (int) ((val - pyMin) / resolution);
  }

  float iX(int x) {
    return pxMin + x * resolution;
  }

  float iY(int y) {
    return pyMin + y * resolution;
  }

  int computePoint(int idx, Buffer buf) {
    float r = 0, g = 0, b = 0;
    int end = positions[idx + 1];

    for (int off = positions[idx]; off < end; off++) {
      int s = buf.get(subpixels[off]);
      float w = subweights[off];
      r += w * (float) red(s);
      g += w * (float) green(s);
      b += w * (float) blue(s);
    }
    return rgb((int) r, (int) g, (int) b);
  }

  public void dumpMap(LXModel model) {
    final int trueWidth = RainbowModel3D.LED_WIDTH;

    PApplet app = new PApplet();
    PImage img = app.createImage(width, height, RGB);
    img.loadPixels();

    for (int i = 0; i < size(); i++) {
      img.pixels[i] = app.color(100);
    }

    // (Four-color it.)
    int[] coloring = {
      rgb(255, 0, 0), rgb(0, 255, 0), rgb(0, 0, 255), rgb(255, 255, 0),
    };

    for (int c : coloring) {
      System.err.println("  Colors " + red(c) + " " + green(c) + " " + blue(c));
    }

    for (LXPoint lxp : model.points) {
      int idx = lxp.index;
      int end = positions[idx + 1];

      int trueX = lxp.index % trueWidth;
      int trueY = lxp.index / trueWidth;

      int color = coloring[(trueX % 2) + (trueY % 2) * 2];

      float maxw = Float.NEGATIVE_INFINITY;
      for (int off = positions[idx]; off < end; off++) {
        maxw = Math.max(subweights[off], maxw);
      }

      for (int off = positions[idx]; off < end; off++) {
        int subidx = subpixels[off];
        float w = subweights[off] / maxw;

        int subx = subXpos(subidx);
        int suby = subYpos(subidx);

        img.pixels[(height - suby - 1) * width + subx] =
            rgb(
                (int) (w * (float) red(color)),
                (int) (w * (float) green(color)),
                (int) (w * (float) blue(color)));
      }
    }

    img.updatePixels();
    img.save("/Users/jmacd/Desktop/map.png");
  }
}

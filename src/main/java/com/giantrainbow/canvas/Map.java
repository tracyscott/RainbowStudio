package com.giantrainbow.canvas;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.giantrainbow.model.RainbowModel3D;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

import java.util.HashSet;
import java.util.ArrayList;

public class Map {
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
    // subpixels[position[i]..position[i+1]].
    int positions[];
    int subpixels[];

    float pxMin = Float.POSITIVE_INFINITY;
    float pyMin = Float.POSITIVE_INFINITY;
    float pxMax = Float.NEGATIVE_INFINITY;
    float pyMax = Float.NEGATIVE_INFINITY;
    
    public static Map newFromModel(LXModel model) {
        Map map = new Map();
        map.buildFromModel(model);
        return map;
    }

    static class Pixel {
        ArrayList<Integer> subs = new ArrayList<Integer>();
    }

    public boolean isFar(int idx) {
        return !isnear[idx];
    }

    public int size() {
        return height * width;
    }
    
    void buildFromModel(LXModel model) {
        System.out.printf("Building canvas from model...\n");

        // TODO: Add a model-perimeter interface to support testing w/ other
        // panel configurations.
        for (LXPoint pt : ((RainbowModel3D)model).perimeter) {
            pxMin = Math.min(pxMin, pt.x);
            pyMin = Math.min(pyMin, pt.y);
            pxMax = Math.max(pxMax, pt.x);
            pyMax = Math.max(pyMax, pt.y);
        }

        width = subXi(pxMax);
        height = subYi(pyMax);

        positions = new int[model.size+1];
        isnear = new boolean[size()];

        // Temporary data structure for building the map.
        Pixel[] pixels = new Pixel[model.size];
        RTree<LXPoint, Point> tree = RTree.create();
        HashSet<LXPoint> perimeter = new HashSet<LXPoint>();

        // Array of pixel-to-subpixel lists
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Pixel();
        }

        // Build the Rtree from interior points.
        for (LXPoint lxp : model.points) {
            tree = tree.add(lxp, Geometries.point(lxp.x, lxp.y));
        }

        // Build the Rtree from the perimeter set.
        for (LXPoint lxp : ((RainbowModel3D)model).perimeter) {
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
                int idx = yi*width+xi;

                for (Entry<LXPoint, Point> point :
                         tree.nearest(Geometries.point(x, y), searchLimit, 1).
                         toBlocking().toIterable()) {
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

        for (LXPoint lxp : model.points) {
            positions[lxp.index] = position;
            for (int sub : pixels[lxp.index].subs) {
                subpixels[position] = sub;
                position++;
            }
        }
        positions[model.size] = position;

        System.out.printf("Canvas has %d pixels; %d subpixels; %.0f%% covered\n",
                          model.size, size(), 100.0 * (float)nearcount / (float)size());
    }

    int subXi(float val) {
        return (int)((val - pxMin) / resolution);
    }
    int subYi(float val) {
        return (int)((val - pyMin) / resolution);
    }

    float iX(int idx) {
        return pxMin + idx * resolution;
    }
    float iY(int idx) {
        return pyMin + idx * resolution;
    }

    int computePoint(int idx, Buffer buf) {
        float r = 0, g = 0, b = 0;
        int off = positions[idx];
        int end = positions[idx+1];
        float cnt = (float)(end - off);

        // TODO: these are unweighted, weigh by true distance.
        for (; off < end; off++) {
            int s = buf.get(subpixels[off]);
            r += LXColor.red(s);
            g += LXColor.green(s);
            b += LXColor.blue(s);
        }
        //System.out.println("RGB " + r + ":" + g + ":" + b + " w/ " + cnt);
        return LXColor.rgb((int)(r/cnt), (int)(g/cnt), (int)(b/cnt));
    }
}

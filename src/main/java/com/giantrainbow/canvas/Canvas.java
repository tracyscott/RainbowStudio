package com.giantrainbow.canvas;

import heronarts.lx.model.LXModel;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Canvas {

    public Buffer buffer;
    public Map map;
    
    public Canvas(LXModel model) {
        map = Map.newFromModel(model);
        buffer = new Buffer(map.size());
    }

    public void circle(float x, float y, float r, float position) {
        int xbegin = map.subXi(x-r);
        int xend = map.subXi(x+r);

        int ybegin = map.subYi(y-r);
        int yend = map.subYi(y+r);

        float r2 = r * r;

        for (int xi = xbegin; xi <= xend; xi += 1) {
            float xd = map.iX(xi) - x;
            float xd2 = xd * xd;
            for (int yi = ybegin; yi <= yend; yi += 1) {
                if (xi < 0 || yi < 0 || xi >= map.width || yi >= map.height) {
                    continue;
                }
		    
                int idx = map.width*yi+xi;

                if (map.isFar(idx)) {
                    continue;
                }
		    
                float yd = map.iY(yi) - y;
                float yd2 = yd * yd;

                if (xd2 + yd2 > r2) {
                    continue;
                }

                float theta = (float)(Math.atan(yd / xd) + (Math.PI / 2));

                if (xd < 0) {
                    theta += Math.PI;
                }

                float hue = (float)(theta / (2 * Math.PI)) + position;
                float chroma = 0.95F;
                float level = 0.95F;

                buffer.setHSB(idx, hue, chroma, level);
            }
        }
    }

    public void render(int output[]) {
        for (int i = 0; i < output.length; i++) {
            output[i] = map.computePoint(i, buffer);
        }
    }

    public int width() {
        return map.width;
    }

    public int height() {
        return map.height;
    }

    public void dumpImage() {
        final BufferedImage image = new BufferedImage(map.width, map.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setBackground(Color.white);
        g.clearRect(0, 0, map.width, map.height);

        for (int yi = 0; yi < map.height; yi++) {
            for (int xi = 0; xi < map.width; xi++) {
                int idx = yi*map.width+xi;
                image.setRGB(xi, map.height-yi-1, buffer.get(idx));
            }
        }

        try {
            ImageIO.write(image, "PNG", new File("/Users/jmacd/Desktop/image.png"));
        } catch (IOException e) {
            System.err.println("IO exception" + e);
        }
    }
}

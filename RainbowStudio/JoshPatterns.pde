import java.util.*;

import com.chroma.*;

@LXCategory(LXCategory.FORM)
public class RainbowMeans extends LXPattern {
    public final CompoundParameter swapsKnob =
	new CompoundParameter("Swaps", 1, 20).setDescription("Swaps per frame.");
    public final CompoundParameter brightnessKnob =
	new CompoundParameter("Bright", 1, 100).setDescription("Brightness.");
    public final CompoundParameter saturationKnob =
	new CompoundParameter("Sat", 1, 100).setDescription("Saturation");

    Ball balls[];
    Random rnd;
    RainbowCanvas canvas;

    int width() {
	return ((RainbowBaseModel)lx.model).pointsWide;
    }
    int height() {
	return ((RainbowBaseModel)lx.model).pointsHigh;
    }

    public RainbowMeans(LX lx) {
 	super(lx);
        canvas = new RainbowCanvas(lx);
	balls = new Ball[100];
	rnd = new Random();

	int i;
	for (i = 0; i < balls.length; i++) {
	    balls[i] = new Ball();
	    balls[i].X = rnd.nextInt(width());
	    balls[i].Y = rnd.nextInt(height());
	    balls[i].R = rnd.nextInt(height()/2);
	}

	addParameter(swapsKnob);
	addParameter(brightnessKnob);
	addParameter(saturationKnob);
	brightnessKnob.setValue(100);
	saturationKnob.setValue(100);
	swapsKnob.setValue(5);
    }

    public void run(double deltaMs) {
	for (Ball ball : balls) {
	    ball.draw();
	}
    }

    void set(int x, int y) {

	if (x < 0 || x >= width()) {
	    return;
	}
	if (y < 0 || y >= height()) {
	    return;
	}

	int idx = y * width() + x;
	colors[idx] = new Chroma(ColorSpace.LCH, 50.0, 100.0, 40.0, 255).get();
    }

    public class Ball {
	int X;
	int Y;
	int R;

	void draw() {
    	    // Bresenham algorithm
	    // https://rosettacode.org/wiki/Bitmap/Midpoint_circle_algorithm#Go
	    int r = R;
	    if (r < 0) {
		return;
	    }
	    int x1 = -r;
	    int y1 = 0;
	    int err = 2-2*r;
	    // Bresenham algorithm

	    for (;;) {
		set(X-x1, Y+y1);
		set(X-y1, Y-x1);
		set(X+x1, Y-y1);
		set(X+y1, Y+x1);
		r = err;
		if (r > x1) {
		    x1++;
		    err += x1*2 + 1;
		}
		if (r <= y1) {
		    y1++;
		    err += y1*2 + 1;
		}
		if (x1 >= 0) {
		    break;
		}
	    }
	}
    };

    public class RainbowCanvas {

        public class RGB {
            float R, G, B;

            RGB(float r, float g, float b) {
                this.R = r;
                this.G = g;
                this.B = b;
            }
        }

        private LX lx;
        private int width;
        private int height;
        private RGB samples[];

        // Units are in feet, here.  Sample one inch pixels.
        public final float unit = 1.0f / 12.0f;

        public RainbowCanvas(LX lx) {
            lx = lx;
            width = (int)((lx.model.xMax - lx.model.xMin) / unit);
            height = (int)((lx.model.yMax - lx.model.yMin) / unit);
            samples = new RGB[height * width];
        }

        public int toPix(float val) {
            return (int)(val / unit);
        }

        public void circle(float x, float y, float r) {
            int xbegin = toPix(x-r);
            int xend = toPix(x+r);

            int ybegin = toPix(y-r);
            int yend = toPix(y+r);

            float r2 = r * r;

            for (int xi = xbegin; xi <= xend; xi += unit) {
                for (int yi = ybegin; yi <= yend; yi += unit) {
                    if ((x - xi) * (x - xi) + (y - yi) * (y - yi) < r2) {
                        samples[width*yi+xi] = new RGB(1, 1, 1);
                    }
                }
            }
        }
    }
}
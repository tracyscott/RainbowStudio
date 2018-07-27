import java.util.*;

import com.github.davidmoten.rtree.RTree;

import com.chroma.Chroma;
import com.chroma.ChromaLCH;
import com.chroma.ColorSpace;

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
    Chroma placeholder;


    int width() {
	return ((RainbowBaseModel)lx.model).pointsWide;
    }
    int height() {
	return ((RainbowBaseModel)lx.model).pointsHigh;
    }

    public RainbowMeans(LX lx) {
 	super(lx);

        try {
            
        canvas = new RainbowCanvas(lx);

	balls = new Ball[100];
	rnd = new Random();

        placeholder = new Chroma(ColorSpace.LCH, 50.0, 100.0, 40.0, 255);

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
        catch (Exception e) {
            e.printStackTrace();
            
            System.err.println("EXCEPTION: " + e);
        }
        
    }

    public void run(double deltaMs) {
	for (Ball ball : balls) {
	    ball.draw();
	}
	canvas.render();
    }

    void set(int x, int y) {

	if (x < 0 || x >= width()) {
	    return;
	}
	if (y < 0 || y >= height()) {
	    return;
	}

	int idx = y * width() + x;
	colors[idx] = placeholder.get();
    }

    public class Ball {
	int X;
	int Y;
	int R;

	void draw() {
            canvas.circle(X, Y, R);
	}

	void bresenham() {
    	    // Bresenham algorithm
	    // https://rosettacode.org/wiki/Bitmap/Midpoint_circle_algorithm#Go
	    int r = R;
	    if (r < 0) {
		return;
	    }
	    int x1 = -r;
	    int y1 = 0;
	    int err = 2-2*r;

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

        public class Sub {
            float X;
            float Y;
            float L,C,H;

            Sub(float x, float y) {
		this.X = x;
		this.Y = y;
            }

	    void set(float l, float c, float h) {
                L = l;
                C = c;
                H = h;
	    }
        }

	public class Pixel {
	    ArrayList<Sub> subs;
	}

        private LX lx;
        private int width;
        private int height;
        private Sub samples[];
	private Pixel pixels[];
	private RTree<LXPoint, Point> tree;

        // Units are in feet, here.  Sample one inch pixels.
        public final float unit = 1.0f / 12.0f;
	public final float foot = 12.0f;

        public RainbowCanvas(LX lx) {
            this.lx = lx;
            this.width = (int)((lx.model.xMax - lx.model.xMin) / unit);
	    this.height = (int)((lx.model.yMax - lx.model.yMin) / unit);
            this.samples = new Sub[height * width];
	    this.pixels = new Pixel[lx.model.points.length];
	    
	    for (int xi = 0; xi < width; xi++) {
	    	float x = toPos(xi);
	    	for (int yi = 0; yi < height; yi++) {
	    	    float y = toPos(yi);
	    	    int idx = yi*width+xi;
	    	    samples[idx] = new Sub(x, y);
		}
	    }

	    for (int i = 0; i < lx.model.points.length; i++) {
		pixels[i] = new Pixel();
		pixels[i].subs = new ArrayList<Sub>();
	    }
	    
	    createTree();
	}

	void createTree() {
	    tree = RTree.create();

	    if (lx == null) {
		return;
	    }
	    if (lx.model == null) {
		return;
	    }
	    if (lx.model.points == null) {
		return;
	    }

	    
	    for (LXPoint lxp : lx.model.points) {
	    	tree = tree.add(lxp, Geometries.point(lxp.x, lxp.y));
	    }

	    for (int xi = 0; xi < width; xi++) {
	    	float x = toPos(xi);
	    	for (int yi = 0; yi < height; yi++) {
	    	    float y = toPos(yi);
	    	    int idx = yi*width+xi;

                    for (Entry<LXPoint, Point> point :
                             tree.nearest(Geometries.point(x, y), foot/2., 1).toBlocking().toIterable()) {
                        pixels[point.value().index].subs.add(samples[idx]);
                    }
	    	}
	    }
	}

        public int toPix(float val) {
            return (int)(val / unit);
        }

        public float toPos(int idx) {
            return idx * unit;
        }

        public void circle(float x, float y, float r) {
	    int xbegin = toPix(x-r);
            int xend = toPix(x+r);

            int ybegin = toPix(y-r);
            int yend = toPix(y+r);

            float r2 = r * r;

            for (int xi = xbegin; xi <= xend; xi += 1) {
                float xd = toPos(xi) - x;
                float xd2 = xd * xd;
                for (int yi = ybegin; yi <= yend; yi += 1) {
                    float yd = toPos(yi) - y;
                    float yd2 = yd * yd;

		    if (xi < 0 || yi < 0 || xi >= width || yi >= height) {
			continue;
		    }
		    
                    if (xd2 + yd2 > r2) {
                        continue;
                    }

                    float theta = (float)Math.atan(yd / xd);

                    if (theta < 0) {
                        theta += 2 * Math.PI;
                    }

                    if (xi < x) {
                        theta -= 180; 
                    }
                    
                    float hue = (float)(theta * 180 / Math.PI);
                    float chroma = xd / x;
                    float level = 1.0;

                    samples[width*yi+xi].set(level, chroma, hue);
                }
            }
        }

	public void render() {
	    for (LXPoint lxp : lx.model.points) {
                float la = 0, ca = 0 , ha = 0;
                int cnt = 0;
                // Note: these are unweighted. Not so right...
                for (Sub s : pixels[lxp.index].subs) {
                    cnt++;
                    la += s.L;
                    ca += s.C;
                    ha += s.H;
                }
                colors[lxp.index] = LXColor.hsb(ha/(float)cnt,
                                                ca/(float)cnt * 100,
                                                la/(float)cnt) * 100;
            }
        }
    }
}

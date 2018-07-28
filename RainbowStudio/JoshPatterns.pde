import java.util.*;

import java.awt.Color;

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


    int trueWidth() {
	return ((RainbowBaseModel)lx.model).pointsWide;
    }
    int trueHeight() {
	return ((RainbowBaseModel)lx.model).pointsHigh;
    }

    public RainbowMeans(LX lx) {
 	super(lx);

        try {
	    canvas = new RainbowCanvas(lx);

	    balls = new Ball[100];
	    rnd = new Random();

	    int i;
	    for (i = 0; i < balls.length; i++) {
		balls[i] = new Ball();
		balls[i].X = rnd.nextInt(canvas.width);
		balls[i].Y = rnd.nextInt(canvas.height);
		balls[i].R = rnd.nextInt(canvas.height/2);
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
	// for (Ball ball : balls) {
	//     ball.draw();
	// }
	silly();
	canvas.render();
    }

    void silly() {
	for (int yi = 0; yi < canvas.height; yi++) {
	    for (int xi = 0; xi < canvas.width; xi++) {
		int idx = yi*canvas.width+xi;
		canvas.samples[idx].setHSB((float)xi/(float)canvas.width, (float)yi/(float)canvas.height, 1);
	    }
	}
    }

    public class Ball {
	int X;
	int Y;
	int R;

	void draw() {
            canvas.circle(X, Y, R);
	}
    };

    public class RainbowCanvas {

        public class Sub {
            float X;
            float Y;
            int C;

            Sub(float x, float y) {
		this.X = x;
		this.Y = y;
            }

	    void setHSB(float h, float s, float b) {
		C = Color.HSBtoRGB(h, s, b);
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

	    System.err.println("Subsample size " + width + "x" + height + " = " + width*height); 
	    
	    for (int xi = 0; xi < width; xi++) {
	    	float x = iX(xi);
	    	for (int yi = 0; yi < height; yi++) {
	    	    float y = iY(yi);
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
	    	float x = iX(xi);
	    	for (int yi = 0; yi < height; yi++) {
	    	    float y = iY(yi);
	    	    int idx = yi*width+xi;

                    for (Entry<LXPoint, Point> point :
                             tree.nearest(Geometries.point(x, y), foot/2., 1).toBlocking().toIterable()) {
                        pixels[point.value().index].subs.add(samples[idx]);
                    }
	    	}
	    }

	    for (Pixel p : pixels) {
		if (p.subs.size() < 5) {
		    System.err.println("Pixel with " + p.subs.size() + " sub-pixels");
		}
	    }
	}

        public int subXi(float val) {
            return (int)((val - lx.model.xMin) / unit);
        }
        public int subYi(float val) {
            return (int)((val - lx.model.yMin) / unit);
        }

        public float iX(int idx) {
            return lx.model.xMin + idx * unit;
        }
        public float iY(int idx) {
            return lx.model.yMin + idx * unit;
        }

        public void circle(float x, float y, float r) {
	    int xbegin = subXi(x-r);
            int xend = subXi(x+r);

            int ybegin = subYi(y-r);
            int yend = subYi(y+r);

            float r2 = r * r;

            for (int xi = xbegin; xi <= xend; xi += 1) {
                float xd = iX(xi) - x;
                float xd2 = xd * xd;
                for (int yi = ybegin; yi <= yend; yi += 1) {
                    float yd = iY(yi) - y;
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
                        theta -= Math.PI; 
                    }
                    
                    float hue = (float)(theta / Math.PI / 2);
                    float chroma = xd / x;
                    float level = 1.0;

                    samples[width*yi+xi].setHSB(hue, chroma, level);
                }
            }
        }

	public void render() {

	    for (LXPoint lxp : lx.model.points) {
                float r = 0, g = 0, b = 0;
                int cnt = 0;
                // Note: these are unweighted. Not so right...
                for (Sub s : pixels[lxp.index].subs) {
                    cnt++;
		    r += LXColor.red(s.C);
		    g += LXColor.green(s.C);
		    b += LXColor.blue(s.C);
                }
                colors[lxp.index] = LXColor.rgb((int)(r/(float)cnt + 0.5),
						(int)(g/(float)cnt + 0.5),
						(int)(b/(float)cnt + 0.5));
            }
        }
    }
}

import java.util.*;

import java.io.File;

import com.github.davidmoten.rtree.RTree;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

@LXCategory(LXCategory.FORM)
public class RainbowMeans extends LXPattern {
    public final CompoundParameter speedKnob =
	new CompoundParameter("Speed", 1, 20).setDescription("Speed.");
    public final CompoundParameter brightnessKnob =
	new CompoundParameter("Bright", 1, 100).setDescription("Brightness.");
    public final CompoundParameter saturationKnob =
	new CompoundParameter("Sat", 1, 100).setDescription("Saturation");

    Ball balls[];
    Random rnd;
    RainbowCanvas canvas;

    double elapsed;

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
	    elapsed = 0;
	    balls = new Ball[100];
	    rnd = new Random();

	    int i;
	    for (i = 0; i < balls.length; i++) {
		balls[i] = new Ball();
		balls[i].X = lx.model.xMin + (lx.model.xMax - lx.model.xMin) * rnd.nextFloat();
		balls[i].Y = lx.model.yMin + (lx.model.yMax - lx.model.yMin) * rnd.nextFloat();
		balls[i].R = (lx.model.yMax - lx.model.yMin) * rnd.nextFloat() / 4;
	    }

	    addParameter(speedKnob);
	    addParameter(brightnessKnob);
	    addParameter(saturationKnob);
	    brightnessKnob.setValue(100);
	    saturationKnob.setValue(100);
	    speedKnob.setValue(5);
        }
        catch (Exception e) {
            e.printStackTrace();
            
            System.err.println("EXCEPTION: " + e);
        }
        
    }

    public void run(double deltaMs) {
	elapsed += deltaMs;
	for (Ball ball : balls) {
	    ball.draw();
	}
	canvas.render();
    }

    void silly() {
	for (int yi = 0; yi < canvas.height; yi++) {
	    for (int xi = 0; xi < canvas.width; xi++) {
		int idx = yi*canvas.width+xi;
		canvas.samples[idx].setHSB((float)yi/(float)canvas.height, 1, 1);
	    }
	}
    }

    public class Ball {
	float X;
	float Y;
	float R;

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

	    void setRGB(int r, int g, int b) {
		C = new Color(r, g, b).getRGB();
	    }
        }

	public class Pixel {
	    ArrayList<Sub> subs;
	}

        private LX lx;
        private int width;
        private int height;
	private boolean isfar[];
        private Sub samples[];
	private Pixel pixels[];

	private float pxMin, pxMax, pyMin, pyMax;

        // Units are in feet, here.  Sample one inch pixels.
	public final float unit = 1.0f / 12.0f;
	public final float limit = 12.0f;

        public RainbowCanvas(LX lx) {
            this.lx = lx;
	    this.pixels = new Pixel[lx.model.points.length];

	    pxMin = Float.POSITIVE_INFINITY;
	    pyMin = Float.POSITIVE_INFINITY;
	    pxMax = Float.NEGATIVE_INFINITY;
	    pyMax = Float.NEGATIVE_INFINITY;

	    for (LXPoint pt : ((RainbowModel3D)lx.model).perimeter) {
		pxMin = Math.min(pxMin, pt.x);
		pyMin = Math.min(pyMin, pt.y);
		pxMax = Math.max(pxMax, pt.x);
		pyMax = Math.max(pyMax, pt.y);
	    }

            this.width = subXi(pxMax);
	    this.height = subYi(pyMax);
            this.samples = new Sub[height * width];
            this.isfar = new boolean[height * width];

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

	    RTree<LXPoint, Point> tree = RTree.create();
	    HashSet<LXPoint> perimeter = new HashSet<LXPoint>();
	    
	    for (LXPoint lxp : lx.model.points) {
	    	tree = tree.add(lxp, Geometries.point(lxp.x, lxp.y));
	    }

	    for (LXPoint lxp : ((RainbowModel3D)lx.model).perimeter) {
	    	tree = tree.add(lxp, Geometries.point(lxp.x, lxp.y));
		perimeter.add(lxp);
	    }

	    int farcount = 0;

	    for (int xi = 0; xi < width; xi++) {
	    	float x = iX(xi);
	    	for (int yi = 0; yi < height; yi++) {
	    	    float y = iY(yi);
	    	    int idx = yi*width+xi;

                    for (Entry<LXPoint, Point> point :
                             tree.nearest(Geometries.point(x, y), limit, 1).toBlocking().toIterable()) {
			LXPoint lxp = point.value();
			if (perimeter.contains(lxp)) {
			    isfar[idx] = true;
			    farcount++;
			    continue;
			}
                        pixels[lxp.index].subs.add(samples[idx]);
                    }
	    	}
	    }

	    System.err.printf("Using %.1f%% of sub-sample pixels\n", 100.0*(float)(width*height-farcount)/(float)(width*height));
	}

        public int subXi(float val) {
            return (int)((val - pxMin) / unit);
        }
        public int subYi(float val) {
            return (int)((val - pyMin) / unit);
        }

        public float iX(int idx) {
            return pxMin + idx * unit;
        }
        public float iY(int idx) {
            return pyMin + idx * unit;
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
		    if (xi < 0 || yi < 0 || xi >= canvas.width || yi >= canvas.height) {
			continue;
		    }
		    
		    int idx = width*yi+xi;

		    if (isfar[idx]) {
			continue;
		    }
		    
                    float yd = iY(yi) - y;
                    float yd2 = yd * yd;

                    if (xd2 + yd2 > r2) {
                        continue;
                    }

                    float theta = (float)(Math.atan(yd / xd) + (Math.PI / 2));

		    if (xd < 0) {
			theta += Math.PI;
		    }
                    
		    float hue = (float)(theta / (2 * Math.PI)) + (float)(speedKnob.getValue()*elapsed/10000);
                    float chroma = 0.95;
                    float level = 0.95;

                    samples[idx].setHSB(hue, chroma, level);
                }
            }
        }

	public void render() {
	    //dump();

	    for (LXPoint lxp : lx.model.points) {
                float r = 0, g = 0, b = 0;
                int cnt = 0;
                // Note: these are unweighted.
                for (Sub s : pixels[lxp.index].subs) {
                    cnt++;
		    r += LXColor.red(s.C);
		    g += LXColor.green(s.C);
		    b += LXColor.blue(s.C);
                }
                colors[lxp.index] = LXColor.rgb((int)(r/(float)cnt),
						(int)(g/(float)cnt),
						(int)(b/(float)cnt));
            }
        }

	public void dump() {
	    final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    final Graphics2D g = (Graphics2D) image.getGraphics();
	    g.setBackground(Color.white);
	    g.clearRect(0, 0, width, height);
	    
	    for (int yi = 0; yi < canvas.height; yi++) {
		for (int xi = 0; xi < canvas.width; xi++) {
		    int idx = yi*canvas.width+xi;
		    image.setRGB(xi, canvas.height-yi-1, canvas.samples[idx].C);
		}
	    }

	    try {
		ImageIO.write(image, "PNG", new File("/Users/jmacd/Desktop/image.png"));
	    } catch (IOException e) {
		System.err.println("IO exception" + e);
		throw new RuntimeException("BLAH");
	    }
	}
    }
}

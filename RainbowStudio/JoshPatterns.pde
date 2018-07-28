import java.util.*;

import java.io.File;

import com.github.davidmoten.rtree.RTree;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

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
		balls[i].R = (lx.model.yMax - lx.model.yMin) * rnd.nextFloat() / 5;
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

		    if (xi < 0 || yi < 0 || xi >= canvas.width || yi >= canvas.height) {
			continue;
		    }
		    
                    if (xd2 + yd2 > r2) {
                        continue;
                    }

                    float theta = (float)(Math.atan(yd / xd) + (Math.PI / 2));

		    if (xd < 0) {
			theta += Math.PI;
		    }
                    
		    float hue = (float)(theta / (2 * Math.PI)) + (float)(elapsed/1000);
                    float chroma = 1.0;
                    float level = 1.0;

                    samples[width*yi+xi].setHSB(hue, chroma, level);
                }
            }
        }

	public void render() {
	    // dump();

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

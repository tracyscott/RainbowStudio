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

    int width() {
	return ((RainbowBaseModel)lx.model).pointsWide;
    }
    int height() {
	return ((RainbowBaseModel)lx.model).pointsHigh;
    }

    public RainbowMeans(LX lx) {
	super(lx);

	balls = new Ball[3];
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
	int idx = y * height() + x;
	colors[idx] = new Chroma(ColorSpace.LCH, 50.0, 100.0, 40.0, 255).get();
    }

    public class Ball {
	int X;
	int Y;
	int R;

	void draw() {
	    // https://rosettacode.org/wiki/Bitmap/Midpoint_circle_algorithm#Java
	    int d = (5 - R * 4)/4;
	    int x = 0;
	    int y = R;
 
	    do {
		set(X + x, Y + y);
		set(X + x, Y - y);
		set(X - x, Y + y);
		set(X - x, Y - y);
		set(X + y, Y + x);
		set(X + y, Y - x);
		set(X - y, Y + x);
		set(X - y, Y - x);
		if (d < 0) {
		    d += 2 * x + 1;
		} else {
		    d += 2 * (x - y) + 1;
		    y--;
		}
		x++;
	    } while (x <= y);
	}
    };
}

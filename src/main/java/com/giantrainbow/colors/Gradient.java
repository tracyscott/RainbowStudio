package com.giantrainbow.colors;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PGraphics;

public class Gradient {

    int[] colors;

    Gradient(int[] colors) {
	this.colors = colors;
    }

    public int size() {
	return colors.length;
    }

    // Note the number of returned colors is always a multiple of 3.
    public static Gradient compute(PGraphics gr, int count) {
	// Round
	if (count % 3 != 0) {
	    count += 3 - (count % 3);
	}
	
	int colors[] = new int[count];
	  
	int min1 = Colors.hsb(0, 1, 1);
	int max1 = Colors.hsb(0.333333333f, 1, 1);
	for (int i = 0; i < count/3; i++) {
	    float r = (float)i / (float)(count/3);
	    int c = gr.lerpColor(min1, max1, r);
	    colors[i] = c;
	}

	int min2 = Colors.hsb(0.333333333f, 1, 1);
	int max2 = Colors.hsb(0.666666667f, 1, 1);
	for (int i = 0; i < count/3; i++) {
	    float r = (float)i / (float)(count/3);
	    int c = gr.lerpColor(min2, max2, r);
	    colors[count/3+i] = c;
	}

	int min3 = Colors.hsb(0.666666667f, 1, 1);
	int max3 = Colors.hsb(1, 1, 1);
	for (int i = 0; i < count/3; i++) {
	    float r = (float)i / (float)(count/3);
	    int c = gr.lerpColor(min3, max3, r);
	    colors[2*count/3+i] = c;
	}

	return new Gradient(colors);
    }

    public int index(int p) {
	p %= colors.length;
	if (p < 0) {
	    p += colors.length;
	}
	return colors[p];
    }
}

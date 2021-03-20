package com.giantrainbow.colors;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PGraphics;

public class BlendedGradient {

    int[] gradient;
    static final int perPair = 25;

    public static BlendedGradient blender(PGraphics gr, int[] gradient) {
	BlendedGradient b = new BlendedGradient();
	b.gradient = new int[gradient.length*perPair];
	for (int i = 0; i < gradient.length-1; i++) {
	    for (int j = 0; j < perPair; j++) {
		float r = (float)j / (float)perPair;
		int c = gr.lerpColor(gradient[i], gradient[i+1], r);
		b.gradient[i*perPair+j] = c;
	    }
	}
	return b;
    }

    public int get(float r) {
	return gradient[(int)(r*(gradient.length-1)+0.5)];
    }
}

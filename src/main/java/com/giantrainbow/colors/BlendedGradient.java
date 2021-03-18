package com.giantrainbow.colors;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PGraphics;

public class BlendedGradient {

    int[] blend;
    static final int perBlend = 10;

    public static BlendedGradient blender(PGraphics gr, int[] gradient) {
	this.blend = new Gradient[perBlend*(gradient.length-1)];

	for int i = 0; i < blend.length; i++ {
	    for
	    int c = gr.lerpColor(gradient[i], gradient[i+1], r);
	    colors[i] = c;
	}
    }
}

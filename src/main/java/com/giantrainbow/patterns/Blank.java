package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.logging.Logger;

@LXCategory(LXCategory.FORM)
public class Blank extends PGPixelPerfect {

    public Blank(LX lx) {
        super(lx, null);
    }
    public void draw(double deltaDrawMs) {
    }
}

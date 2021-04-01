package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.COLOR)
public class SpectrumRangeSelect extends LXPattern {

    private CompoundParameter rangeParameter = new CompoundParameter("Rng", 270, 360);
    private CompoundParameter saturationParameter = new CompoundParameter("Sat", 1);



    public SpectrumRangeSelect(LX lx) {
        super(lx);
        addParameter(rangeParameter);
        addParameter(saturationParameter);


    }
    @Override
    protected void run(double deltaMs) {
        int numPixelsPerRow = ((RainbowBaseModel) lx.model).pointsWide;
        int pointNumber = 0;
        float rainbowEndHue = rangeParameter.getBaseValuef();
        float rainbowEndHueDivident = rainbowEndHue/29;
        for (LXPoint p : model.points) {
            int rowNumber = pointNumber / numPixelsPerRow;

            if (rowNumber == 0) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*29, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 1) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*28, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 2) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*27, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 3) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*26, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 4) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*25, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 5) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*24, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 6) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*23, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 7) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*22, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 8) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*21, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 9) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*20, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 10) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*19, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 11) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*18, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 12) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*17, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 13) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*16, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 14) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*15, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 15) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*14, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 16) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*13, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 17) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*12, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 18) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*11, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 19) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*10, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 20) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*9, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 21) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*8, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 22) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*7, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 23) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*6, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 24) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*5, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 25) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*4, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 26) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*3, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 27) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*2, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 28) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*1, saturationParameter.getValuef() * 100, 100);
            }
            else if (rowNumber == 29) {
                colors[p.index] = lx.hsb(rainbowEndHueDivident*0, saturationParameter.getValuef() * 100, 100);
            }

            pointNumber++;

        }
    }
}


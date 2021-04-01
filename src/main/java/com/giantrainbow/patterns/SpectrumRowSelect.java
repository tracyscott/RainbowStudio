package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.COLOR)
public class SpectrumRowSelect extends LXPattern {
    int rainbowEndHue = 290;
    int rainbowEndHueDivident = rainbowEndHue/29;

    private CompoundParameter saturationParameter = new CompoundParameter("Sat", 1);

    private CompoundParameter hueParameter1 = new CompoundParameter("Hue1", 1 + (rainbowEndHueDivident * 29), 360);
    private CompoundParameter hueParameter2 = new CompoundParameter("Hue2", 1 + (rainbowEndHueDivident * 28), 360);
    private CompoundParameter hueParameter3 = new CompoundParameter("Hue3", 1 + (rainbowEndHueDivident * 27), 360);
    private CompoundParameter hueParameter4 = new CompoundParameter("Hue4", 1 + (rainbowEndHueDivident * 26), 360);
    private CompoundParameter hueParameter5 = new CompoundParameter("Hue5", 1 + (rainbowEndHueDivident * 25), 360);
    private CompoundParameter hueParameter6 = new CompoundParameter("Hue6", 1 + (rainbowEndHueDivident * 24), 360);
    private CompoundParameter hueParameter7 = new CompoundParameter("Hue7", 1 + (rainbowEndHueDivident * 23), 360);
    private CompoundParameter hueParameter8 = new CompoundParameter("Hue8", 1 + (rainbowEndHueDivident * 22), 360);
    private CompoundParameter hueParameter9 = new CompoundParameter("Hue9", 1 + (rainbowEndHueDivident * 21), 360);
    private CompoundParameter hueParameter10 = new CompoundParameter("Hue10", 1 + (rainbowEndHueDivident * 20), 360);
    private CompoundParameter hueParameter11 = new CompoundParameter("Hue11", 1 + (rainbowEndHueDivident * 19), 360);
    private CompoundParameter hueParameter12 = new CompoundParameter("Hue12", 1 + (rainbowEndHueDivident * 18), 360);
    private CompoundParameter hueParameter13 = new CompoundParameter("Hue13", 1 + (rainbowEndHueDivident * 17), 360);
    private CompoundParameter hueParameter14 = new CompoundParameter("Hue14", 1 + (rainbowEndHueDivident * 16), 360);
    private CompoundParameter hueParameter15 = new CompoundParameter("Hue15", 1 + (rainbowEndHueDivident * 15), 360);
    private CompoundParameter hueParameter16 = new CompoundParameter("Hue16", 1 + (rainbowEndHueDivident * 14), 360);
    private CompoundParameter hueParameter17 = new CompoundParameter("Hue17", 1 + (rainbowEndHueDivident * 13), 360);
    private CompoundParameter hueParameter18 = new CompoundParameter("Hue18", 1 + (rainbowEndHueDivident * 12), 360);
    private CompoundParameter hueParameter19 = new CompoundParameter("Hue19", 1 + (rainbowEndHueDivident * 11), 360);
    private CompoundParameter hueParameter20 = new CompoundParameter("Hue20", 1 + (rainbowEndHueDivident * 10), 360);
    private CompoundParameter hueParameter21 = new CompoundParameter("Hue21", 1 + (rainbowEndHueDivident * 9), 360);
    private CompoundParameter hueParameter22 = new CompoundParameter("Hue22", 1 + (rainbowEndHueDivident * 8), 360);
    private CompoundParameter hueParameter23 = new CompoundParameter("Hue23", 1 + (rainbowEndHueDivident * 7), 360);
    private CompoundParameter hueParameter24 = new CompoundParameter("Hue24", 1 + (rainbowEndHueDivident * 6), 360);
    private CompoundParameter hueParameter25 = new CompoundParameter("Hue25", 1 + (rainbowEndHueDivident * 5), 360);
    private CompoundParameter hueParameter26 = new CompoundParameter("Hue26", 1 + (rainbowEndHueDivident * 4), 360);
    private CompoundParameter hueParameter27 = new CompoundParameter("Hue27", 1 + (rainbowEndHueDivident * 3), 360);
    private CompoundParameter hueParameter28 = new CompoundParameter("Hue28", 1 + (rainbowEndHueDivident * 2), 360);
    private CompoundParameter hueParameter29 = new CompoundParameter("Hue29", 1 + (rainbowEndHueDivident * 1), 360);
    private CompoundParameter hueParameter30 = new CompoundParameter("Hue30", 1 + (rainbowEndHueDivident * 0), 360);

    public SpectrumRowSelect(LX lx) {
        super(lx);
        addParameter(hueParameter1);
        addParameter(hueParameter2);
        addParameter(hueParameter3);
        addParameter(hueParameter4);
        addParameter(hueParameter5);
        addParameter(hueParameter6);
        addParameter(hueParameter7);
        addParameter(hueParameter8);
        addParameter(hueParameter9);
        addParameter(hueParameter10);
        addParameter(hueParameter11);
        addParameter(hueParameter12);
        addParameter(hueParameter13);
        addParameter(hueParameter14);
        addParameter(hueParameter15);
        addParameter(hueParameter16);
        addParameter(hueParameter17);
        addParameter(hueParameter18);
        addParameter(hueParameter19);
        addParameter(hueParameter20);
        addParameter(hueParameter21);
        addParameter(hueParameter22);
        addParameter(hueParameter23);
        addParameter(hueParameter24);
        addParameter(hueParameter25);
        addParameter(hueParameter26);
        addParameter(hueParameter27);
        addParameter(hueParameter28);
        addParameter(hueParameter29);
        addParameter(hueParameter30);
        addParameter(saturationParameter);
    }

    @Override
    protected void run(double deltaMs) {
        int numPixelsPerRow = ((RainbowBaseModel) lx.model).pointsWide;
        int pointNumber = 0;
        for (LXPoint p : model.points) {
            int rowNumber = pointNumber / numPixelsPerRow;

                if (rowNumber == 0) {
                    colors[p.index] = lx.hsb(hueParameter1.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 1) {
                    colors[p.index] = lx.hsb(hueParameter2.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 2) {
                    colors[p.index] = lx.hsb(hueParameter3.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 3) {
                    colors[p.index] = lx.hsb(hueParameter4.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 4) {
                    colors[p.index] = lx.hsb(hueParameter5.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 5) {
                    colors[p.index] = lx.hsb(hueParameter6.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 6) {
                    colors[p.index] = lx.hsb(hueParameter7.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 7) {
                    colors[p.index] = lx.hsb(hueParameter8.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 8) {
                    colors[p.index] = lx.hsb(hueParameter9.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 9) {
                    colors[p.index] = lx.hsb(hueParameter10.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 10) {
                    colors[p.index] = lx.hsb(hueParameter11.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 11) {
                    colors[p.index] = lx.hsb(hueParameter12.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 12) {
                    colors[p.index] = lx.hsb(hueParameter13.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 13) {
                    colors[p.index] = lx.hsb(hueParameter14.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 14) {
                    colors[p.index] = lx.hsb(hueParameter15.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 15) {
                    colors[p.index] = lx.hsb(hueParameter16.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 16) {
                    colors[p.index] = lx.hsb(hueParameter17.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 17) {
                    colors[p.index] = lx.hsb(hueParameter18.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 18) {
                    colors[p.index] = lx.hsb(hueParameter19.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 19) {
                    colors[p.index] = lx.hsb(hueParameter20.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 20) {
                    colors[p.index] = lx.hsb(hueParameter21.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 21) {
                    colors[p.index] = lx.hsb(hueParameter22.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 22) {
                    colors[p.index] = lx.hsb(hueParameter23.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 23) {
                    colors[p.index] = lx.hsb(hueParameter24.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 24) {
                    colors[p.index] = lx.hsb(hueParameter25.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 25) {
                    colors[p.index] = lx.hsb(hueParameter26.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 26) {
                    colors[p.index] = lx.hsb(hueParameter27.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 27) {
                    colors[p.index] = lx.hsb(hueParameter28.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 28) {
                    colors[p.index] = lx.hsb(hueParameter29.getValuef(), saturationParameter.getValuef() * 100, 100);
                }
                else if (rowNumber == 29) {
                    colors[p.index] = lx.hsb(hueParameter30.getValuef(), saturationParameter.getValuef() * 100, 100);
                }

                pointNumber++;

            }
        }
    }



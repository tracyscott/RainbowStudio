package com.giantrainbow.patterns;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;

import static java.lang.Math.abs;
import static java.lang.Math.max;

@LXCategory(LXCategory.FORM)
public class BoxMask extends LXPattern {




    public enum Axis {
        X, Y, Z
    };

    public final EnumParameter<Axis> axis =
            new EnumParameter<Axis>("Axis", Axis.X)
                    .setDescription("Which axis the plane is drawn across");

    public final CompoundParameter pos = new CompoundParameter("Pos", 0, 1)
            .setDescription("Position of the center of the plane");

    public final CompoundParameter wth = new CompoundParameter("Width", .4, 0, 1)
            .setDescription("Thickness of the plane");

    public final CompoundParameter hue  = new CompoundParameter("hue", 190, 0, 360)
            .setDescription("Hie");

    public final CompoundParameter sat  = new CompoundParameter("sat", 100, 0, 100)
            .setDescription("Hie");

    public BoxMask(LX lx) {
        super(lx);
        addParameter("axis", this.axis);
        addParameter("pos", this.pos);
        addParameter("width", this.wth);
        addParameter(hue);
        addParameter(sat);
    }

    @Override
    public void run(double deltaMs) {
        float hue = this.hue.getValuef();
        float sat = this.sat.getValuef();
        float pos = this.pos.getValuef();
        float falloff = 100 / this.wth.getValuef();
        float n = 0;
        for (LXPoint p : model.points) {
            switch (this.axis.getEnum()) {
                case X: n = p.xn; break;
                case Y: n = p.yn; break;
                case Z: n = p.zn; break;
            }
            colors[p.index] = LX.hsb(hue, sat, max(0, 100 - falloff*abs(n - pos)));
        }
    }
}
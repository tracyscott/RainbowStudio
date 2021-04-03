//Author ROYGBIV (Cameron Nagai) and Jake Lampack

package com.giantrainbow.patterns;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.LXUtils;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@LXCategory(LXCategory.FORM)
public class Sparkle extends LXPattern {
    private CompoundParameter densityParameter = new CompoundParameter("Dens", 0.15);
    private CompoundParameter attackParameter = new CompoundParameter("Attack", 0.4);
    private CompoundParameter decayParameter = new CompoundParameter("Decay", 0.3);
    private CompoundParameter hueParameter = new CompoundParameter("Hue", 0.5);
    private CompoundParameter hueVarianceParameter = new CompoundParameter("HueVar", 0.25);
    private CompoundParameter saturationParameter = new CompoundParameter("Sat", 0.5);
    private CompoundParameter speedParameter = new CompoundParameter("Speed", .5);


    public Sparkle(LX lx) {
        super(lx);
        addParameter(densityParameter);
        addParameter(attackParameter);
        addParameter(decayParameter);
        addParameter(hueParameter);
        addParameter(hueVarianceParameter);
        addParameter(saturationParameter);
        addParameter(speedParameter);
        sparks = new LinkedList<Spark>();
    }

    class Spark {
        int randomPoint;
        float value;
        float hue;
        boolean hasPeaked;

        Spark() {
//            randomPoint = (int) (Math.random() * (model.points.length - 2 + 1) + 1);
            randomPoint = ThreadLocalRandom.current().nextInt(0, model.points.length);
            hue = (float) LXUtils.random(0, 1);
            boolean infiniteAttack = (attackParameter.getValuef() > 0.999);
            hasPeaked = infiniteAttack;
            value = (infiniteAttack ? 1 : 0);
        }

        // returns TRUE if this should die
        boolean age(double ms) {
            if (!hasPeaked) {
                value = value + (float) (ms / 1000.0f * ((attackParameter.getValuef() + 0.01) * 5));
                if (value >= 1.0) {
                    value = (float) 1.0;
                    hasPeaked = true;
                }
                return false;
            } else {
                value = value - (float) (ms / 1000.0f * ((decayParameter.getValuef() + 0.01) * 10));
                return value <= 0;
            }
        }
    }

    private float leftoverMs = 0;
    private List<Spark> sparks;

    public void run(double deltaMs) {
        double SpeedMs = deltaMs*(speedParameter.getValuef()+.5f);
        leftoverMs += SpeedMs;
        float msPerSpark = 1000.f / (float) ((densityParameter.getValuef() + .01) * (model.xRange * 10));
        while (leftoverMs > msPerSpark) {
            leftoverMs -= msPerSpark;
            sparks.add(new Spark());
        }
        for (Spark spark : sparks) {
            float hue = ((float)(hueParameter.getValuef() + (hueVarianceParameter.getValuef() * spark.hue))) % 1.0f;
            int c = lx.hsb(hue * 360, saturationParameter.getValuef() * 100, (spark.value) * 100);
            colors[spark.randomPoint] = c;
        }

        Iterator<Spark> i = sparks.iterator();
        while (i.hasNext()) {
            Spark spark = i.next();
            boolean dead = spark.age(SpeedMs);
            if (dead) {
                i.remove();
            }
        }
    }
}



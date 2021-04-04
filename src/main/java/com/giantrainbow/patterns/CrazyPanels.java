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
public class CrazyPanels extends PGPixelPerfect {
    private CompoundParameter dampenParameter = new CompoundParameter("Dampen", 0.25);
    private CompoundParameter attackParameter = new CompoundParameter("Attack", 0.4);
    private CompoundParameter decayParameter = new CompoundParameter("Decay", 0.3);
    private CompoundParameter speedParameter = new CompoundParameter("Speed", .5);

    public CrazyPanels(LX lx) {
        super(lx, null);
        addParameter(dampenParameter);
        addParameter(attackParameter);
        addParameter(decayParameter);
        addParameter(speedParameter);
        sparks = new LinkedList<Spark>();
    }

    class Spark {
        int randomPanel;
        float value;
        float hue;
        boolean hasPeaked;

        Spark() {
            randomPanel = ThreadLocalRandom.current().nextInt(0, 29);
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

    public void draw(double deltaMs) {
        int panelWidth = 15;
        int panelHeight = 30;
        pg.noSmooth();
        pg.background(0);

        double SpeedMs = deltaMs * (speedParameter.getValuef() + .5f)*10;
        leftoverMs += SpeedMs;
        leftoverMs += deltaMs;
        float msPerSpark = 1000.f / (float) ((dampenParameter.getValuef() + .01) * (model.xRange * 10));
        while (leftoverMs > msPerSpark) {
            leftoverMs -= msPerSpark;
            sparks.add(new Spark());
        }
        for (Spark spark : sparks) {
            if (spark.value != 0) {
                pg.fill(255);
                pg.rect(spark.randomPanel * panelWidth, 0, panelWidth - 1, panelHeight - 1);
                }
            else {
                pg.fill(0);
                pg.rect(spark.randomPanel * panelWidth, 0, panelWidth - 1, panelHeight - 1);

            }
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



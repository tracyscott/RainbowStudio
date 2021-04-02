package com.giantrainbow.patterns;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;

@LXCategory(LXCategory.FORM)
public class Amoeba extends LXPattern {

   private final LXModulator hue = startModulator(new SinLFO(180, 200, 9000));
   private final LXModulator brightness = startModulator(new SinLFO(100, 100, 4000));
   private final LXModulator yPos = startModulator(new SinLFO(0, 1, 50000));
   private final LXModulator xPos = startModulator(new SawLFO(100, 0, 5000));
   private final LXModulator width = startModulator(new SinLFO(.1, 1, 3000));

   public Amoeba(LX lx) {
     super(lx);
   }

   @Override
   public void run(double deltaMs) {
     float hue = this.hue.getValuef();
     float brightness = this.brightness.getValuef();
     float xPos = this.xPos.getValuef();
     float yPos = this.yPos.getValuef();

     float falloff = 100 / (this.width.getValuef());
     for (LXPoint p : model.points) {
       colors[p.index] = LX.hsb(hue, 100, Math.max(0, brightness - falloff * Math.abs(p.yn - yPos)));
       // colors[p.index] = LX.hsb(hue, 100, Math.max(0, brightness - falloff * Math.abs(p.xn - xPos)));

     }
   }
 }
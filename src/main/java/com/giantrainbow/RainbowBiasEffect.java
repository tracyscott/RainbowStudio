package com.giantrainbow;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

/*
 * Effect for simulating the backing paint colors on the rainbow
 */

public class RainbowBiasEffect extends LXEffect {
  public final CompoundParameter depth =
    new CompoundParameter("Depth", 0.5)
    .setDescription("Depth of the bias effect");

  public RainbowBiasEffect(LX lx) {
    super(lx);
    addParameter("depth", this.depth);
  }

  // LGBT 6 Bands  (228,3,3) (255,140,0) (255,237,0) (0,128,38) (0,77,255) (117,7,135)
  @Override
  public void run(double deltaMs, double amount) {
  float amt = this.enabledDamped.getValuef() * this.depth.getValuef();
    for (int i = 0; i < this.colors.length; ++i) {
      int rowNumber = i / ((RainbowBaseModel)(lx.model)).pointsWide;
      int src = 0;
      if (rowNumber < 5) {
        src = LXColor.rgb((int)(228 * amt), (int)(3 * amt), (int)(3 * amt));
      } else if (rowNumber >= 5 && rowNumber < 10) {
        src = LXColor.rgb((int)(255 * amt), (int)(140 * amt), 0);
      } else if (rowNumber >= 10 && rowNumber < 15) {
        src = LXColor.rgb((int)(255 * amt), (int)(237 * amt), 0);
      } else if (rowNumber >= 15 && rowNumber < 20) {
        src = LXColor.rgb(0, (int)(128 * amt), (int)(38 * amt));
      } else if (rowNumber >= 20 && rowNumber < 25) {
        src = LXColor.rgb(0, (int)(77 * amt), (int)(255 * amt));
      } else if (rowNumber >= 25 && rowNumber < 30) {
        src = LXColor.rgb((int)(117 * amt), (int)(7 * amt), (int)(135 * amt));
      }
      src = LXColor.multiply(this.colors[i], src);
      this.colors[i] = LXColor.add(this.colors[i], src);
    }
  }
}

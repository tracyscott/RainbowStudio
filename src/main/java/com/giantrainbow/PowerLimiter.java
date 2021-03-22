package com.giantrainbow;

import com.giantrainbow.colors.Colors;
import com.giantrainbow.patterns.RenderImageUtil;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

import java.util.logging.Logger;

/**
 * Clip detector and power limiter.
 */
public class PowerLimiter extends LXEffect {
  private static final Logger logger = Logger.getLogger(PowerLimiter.class.getName());

  public final CompoundParameter maxPowerPercent =
      new CompoundParameter("target", 1.0, 0.1, 1.0)
          .setDescription("Power target limiter");

  public final BooleanParameter clipDetect = new BooleanParameter("clip", false).setDescription("Clip detector");

  public PowerLimiter(LX lx) {
    super(lx);
    addParameter(maxPowerPercent);
    addParameter(clipDetect);
  }

  @Override
  public void run(double deltaMs, double amount) {
    long totalPower = 0;
    long maxTheoreticalPower = colors.length * 3 * 255;
    for (int i = 0; i < this.colors.length; ++i) {
      totalPower += Colors.red(colors[i]) + Colors.green(colors[i]) + Colors.blue(colors[i]);
    }
    long targetPower = (long)(maxTheoreticalPower * maxPowerPercent.getValuef());
    if (totalPower > targetPower) {
      clipDetect.setValue(true);
      float overLimit = (float)((double)totalPower / (double)targetPower);
      float compressAmt = 1f / overLimit;
      //logger.info("compress target: " + compressAmt);
      for (int i = 0; i < this.colors.length; i++) {
        colors[i] = RenderImageUtil.getWeightedColor(colors[i], compressAmt);
      }
    }
  }
}

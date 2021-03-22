package com.giantrainbow;

import com.giantrainbow.colors.Colors;
import com.giantrainbow.patterns.RenderImageUtil;
import heronarts.lx.*;
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

  /**
   * We need to track down which patterns are playing
   * @return
   */
  public void logActiveChannelNames() {
    for (LXChannelBus channelBus : lx.engine.channels) {

      if (channelBus instanceof LXGroup && channelBus.enabled.getValueb() && channelBus.fader.getValuef() > 0.1) {
        LXGroup g = (LXGroup) channelBus;
        if (g.channels.size() > 0) {
          for (LXChannel c : g.channels) {
            if (c.enabled.getValueb()) {
              logger.info("Child channel " + c.getLabel() + " active during clip");
              logger.info("Active pattern: " + c.getActivePattern().label.getString() +
                  " index: " + c.getActivePatternIndex());
            }
          }
        }
      } else if (channelBus instanceof LXChannel && channelBus.enabled.getValueb()) {
        LXChannel c = (LXChannel) channelBus;
        LXGroup g = c.getGroup();
        if (g == null) {
          logger.info("Channel " + c.getLabel() + " active during clip");
          logger.info("Active pattern: " + c.getActivePattern().label.getString() +
              " index: " + c.getActivePatternIndex());
        }
      }
    }
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
      if (!clipDetect.getValueb()) {
        logger.info("Clip detected!!!!!!!!");
        logActiveChannelNames();
      }
      clipDetect.setValue(true);
      float overLimit = (float)((double)totalPower / (double)targetPower);
      float compressAmt = 1f / overLimit;
      for (int i = 0; i < this.colors.length; i++) {
        colors[i] = RenderImageUtil.getWeightedColor(colors[i], compressAmt);
      }
    }
  }
}

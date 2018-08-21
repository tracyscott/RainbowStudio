package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class SpinnyRainbow extends AbstractSpinnyRainbow {

  BackgroundPulse pulse;

  public SpinnyRainbow(LX lx) {
    super(lx);
    pulse = new BackgroundPulse(this);
  }

  boolean hasBackground() {
    return true;
  }

  int getBackground(double deltaMs) {
    return pulse.get(deltaMs);
  }
};

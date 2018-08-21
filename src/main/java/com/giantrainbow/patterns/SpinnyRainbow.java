package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class SpinnyRainbow extends AbstractSpinnyRainbow {

  BackgroundPulse pulse;

  public SpinnyRainbow(LX lx) {
    super(lx);
    pulse = new BackgroundPulse(this);

    speedKnob.setValue(2.5);
  }

  boolean hasBackground() {
    return true;
  }

  int getBackground(double deltaMs) {
    return pulse.get(deltaMs);
  }
};

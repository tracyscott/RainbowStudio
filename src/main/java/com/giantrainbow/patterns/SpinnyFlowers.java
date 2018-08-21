package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class SpinnyFlowers extends AbstractSpinnyFlowers {

  BackgroundPulse pulse;

  public SpinnyFlowers(LX lx) {
    super(lx);
    this.pulse = new BackgroundPulse(this);

    countKnob.setValue(750);
  }

  boolean hasBackground() {
    return true;
  }

  int getBackground(double deltaMs) {
    return pulse.get(deltaMs);
  }
};

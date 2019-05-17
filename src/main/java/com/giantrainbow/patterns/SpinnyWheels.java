package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class SpinnyWheels extends AbstractSpinnyWheels {

  BackgroundPulse pulse;

  public SpinnyWheels(LX lx) {
    super(lx);
    pulse = new BackgroundPulse(this, "BG");

    countKnob.setValue(500);
  }

  boolean hasBackground() {
    return true;
  }

  int getBackground(double deltaMs) {
    return pulse.get(deltaMs);
  }
};

package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class SpinnyFlowersDash extends AbstractSpinnyFlowers {

  public SpinnyFlowersDash(LX lx) {
    super(lx);

    rotateKnob.setValue(3.5);
    sizeKnob.setValue(7.5);
  }

  boolean hasBackground() {
    return false;
  }

  int getBackground(double deltaMs) {
    return 0;
  }
};

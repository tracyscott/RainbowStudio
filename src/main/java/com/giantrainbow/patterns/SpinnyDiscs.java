package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class SpinnyDiscs extends SpinnyDiscsRainbow {

  public SpinnyDiscs(LX lx) {
    super(lx);
  }

  boolean hasBackground() {
    return true;
  }

  int getBackground() {
    // @@@
    return 0;
  }
};

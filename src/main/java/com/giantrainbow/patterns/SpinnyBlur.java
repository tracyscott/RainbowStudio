package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class SpinnyBlur extends SpinnyDiscsRainbow {

  public SpinnyBlur(LX lx) {
    super(lx);
  }

  boolean hasBackground() {
    return false;
  }

  int getBackground() {
    return 0;
  }
};

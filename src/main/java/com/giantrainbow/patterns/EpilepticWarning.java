package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class EpilepticWarning extends AbstractEpileptic {

  public EpilepticWarning(LX lx) {
    super(lx);
  }

  int patterns[][] = {
    {0, 1, 2, 3},
    {4, 5, 6, 7},
    {8, 1, 9, 6, 8, 6, 9, 1},
  };

  public int[] getPositions(int period) {
    int idx = period % patterns.length;
    return patterns[idx];
  }
};

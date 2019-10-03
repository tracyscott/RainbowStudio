package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.logging.Logger;

@LXCategory(LXCategory.FORM)
public class Patch extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(PanelNum.class.getName());

  public DiscreteParameter startPos = new DiscreteParameter("start", 0, 0, 420);
  public DiscreteParameter patchWidth = new DiscreteParameter("width", 15, 1, 420);

  public Patch(LX lx) {

    super(lx, null);
    addParameter(startPos);
    addParameter(patchWidth);
  }

  public void draw(double deltaDrawMs) {
    int panelWidth = 15;
    int panelHeight = 30;
    pg.noSmooth();
    pg.background(0);
    pg.fill(255);
    pg.rect(startPos.getValuei(), 0, patchWidth.getValuei() - 1, panelHeight - 1);
  }
}

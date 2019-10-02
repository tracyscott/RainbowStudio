package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.logging.Logger;

@LXCategory(LXCategory.TEST)
public class PanelNum extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(PanelNum.class.getName());

  public DiscreteParameter panelNum = new DiscreteParameter("panel", 0, 0, 28);

  public PanelNum(LX lx) {

    super(lx, null);
    addParameter(panelNum);
  }

  public void draw(double deltaDrawMs) {
    int panelWidth = 15;
    int panelHeight = 30;
    pg.noSmooth();
    pg.background(0);
    pg.fill(255);
    pg.rect(panelNum.getValuei() * panelWidth, 0, panelWidth - 1, panelHeight - 1);
  }
}

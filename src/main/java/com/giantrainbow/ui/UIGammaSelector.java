package com.giantrainbow.ui;

import com.giantrainbow.Gamma;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;

public class UIGammaSelector extends UIConfig {
  public static final String RED = "Red";
  public static final String GREEN = "Green";
  public static final String BLUE = "Blue";

  public static final String title = "GAMMA";
  public static final String filename = "gamma.json";

  public UIGammaSelector(final LXStudio.UI ui) {
    super(ui, title, filename);

    registerCompoundParameter(RED, 1.8, 1.0, 3.0);
    registerCompoundParameter(GREEN, 1.8, 1.0, 3.0);
    registerCompoundParameter(BLUE, 1.8, 1.0, 3.0);
    save();
    buildUI(ui);
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    super.onParameterChanged(p);
    if (RED.equals(p.getLabel())) {
      Gamma.buildRedGammaLUT(p.getValuef());
    } else if (GREEN.equals(p.getLabel())) {
      Gamma.buildGreenGammaLUT(p.getValuef());
    } else if (BLUE.equals(p.getLabel())) {
      Gamma.buildBlueGammaLUT(p.getValuef());
    }
  }
}

package com.giantrainbow.ui;

import com.giantrainbow.Gamma;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UISlider;

public class UIGammaSelector extends UICollapsibleSection {
  public static BoundedParameter redGamma = new BoundedParameter("Red", 1.8, 1.0, 3.0);
  public static BoundedParameter greenGamma = new BoundedParameter("Green", 1.8, 1.0, 3.0);
  public static BoundedParameter blueGamma = new BoundedParameter("Blue", 1.8, 1.0, 3.0);
  public UISlider redSlider;
  public UISlider greenSlider;
  public UISlider blueSlider;

  public UIGammaSelector(final LXStudio.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("GAMMA");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    redSlider = new UISlider(0, 0, ui.leftPane.global.getContentWidth() - 10, 20) {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        super.onParameterChanged(parameter);
        // Rebuild Red Gamma
        Gamma.buildRedGammaLUT(parameter.getValuef());
      }
    };
    redSlider.addToContainer(this);
    redSlider.setParameter(redGamma);
    greenSlider = new UISlider(0, 0, ui.leftPane.global.getContentWidth() - 10, 20) {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        Gamma.buildGreenGammaLUT(parameter.getValuef());
      }
    };
    greenSlider.setParameter(greenGamma);
    greenSlider.addToContainer(this);

    blueSlider = new UISlider(0, 0, ui.leftPane.global.getContentWidth() - 10, 20) {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        Gamma.buildBlueGammaLUT(parameter.getValuef());
      }
    };
    //blueGamma.setOscAddress("/blueGamma");
    blueSlider.setParameter(blueGamma);
    //System.out.println("blueGamma path: " + LXOscEngine.getOscAddress(blueGamma));
    blueSlider.addToContainer(this);
  }
}

package com.giantrainbow.ui;

import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIKnob;

public class UIAudioMonitorLevels extends UICollapsibleSection {
  public static BoundedParameter minThresholdP =
      new BoundedParameter("MinThr", -80.0, -100.0, 40.0);
  public static BoundedParameter avgTimeP =
      new BoundedParameter("AvgTS", 1.0, 3.0, 30.0);
  public static BoundedParameter gainIncP =
      new BoundedParameter("GainInc", 1.0, 0.1, 5.0);
  public static UIKnob minThreshold;
  public static UIKnob avgTime;
  public static UIKnob gainInc;

  public UIAudioMonitorLevels(final LXStudio.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("AUDIO MONITOR");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    UI2dContainer knobsContainer =
        new UI2dContainer(0, 30, ui.leftPane.global.getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    minThreshold = new UIKnob(minThresholdP);
    minThreshold.addToContainer(knobsContainer);
    avgTime = new UIKnob(avgTimeP);
    avgTime.addToContainer(knobsContainer);
    gainInc = new UIKnob(gainIncP);
    gainInc.addToContainer(knobsContainer);
    knobsContainer.addToContainer(this);
  }
}

package com.giantrainbow.ui;

import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIKnob;

public class UIAudioMonitorLevels extends UICollapsibleSection {
  public static BoundedParameter minThresholdP = new BoundedParameter("MinThreshold", 10.0, 0.0, 40.0);
  public static BoundedParameter quietTimeP = new BoundedParameter("Quiet Time", 60.0, 0.0, 180.0);
  public static UIKnob minThreshold;
  public static UIKnob quietTime;

  public UIAudioMonitorLevels(final LXStudio.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("AUDIO MONITOR");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    UI2dContainer knobsContainer = new UI2dContainer(0, 30, ui.leftPane.global.getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(3, 3, 3, 3);
    minThreshold = new UIKnob(minThresholdP);
    minThreshold.addToContainer(knobsContainer);
    quietTime = new UIKnob(quietTimeP);
    quietTime.addToContainer(knobsContainer);
    knobsContainer.addToContainer(this);
  }
}

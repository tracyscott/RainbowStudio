package com.giantrainbow.ui;

import com.giantrainbow.OSCSensor;
import heronarts.lx.LX;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIKnob;

import java.util.logging.Logger;

public class OSCSensorUI extends UICollapsibleSection {
  private static final Logger logger = Logger.getLogger(OSCSensorUI.class.getName());
  public OSCSensor oscSensor;
  public UIKnob accelXKnob;
  public UIKnob accelYKnob;
  public UIKnob accelZKnob;

  public OSCSensorUI(LXStudio.UI ui, LX lx, OSCSensor oscSensor) {
    super (ui,0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("OSC Sensors");
    UI2dContainer knobsContainer = new UI2dContainer(0, 30, getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    knobsContainer.addToContainer(this);
    this.oscSensor = oscSensor;
    accelXKnob = new UIKnob(oscSensor.accelXKnob);
    accelXKnob.addToContainer(knobsContainer);
    accelYKnob = new UIKnob(oscSensor.accelYKnob);
    accelYKnob.addToContainer(knobsContainer);
    accelZKnob = new UIKnob(oscSensor.accelZKnob);
    accelZKnob.addToContainer(knobsContainer);
  }
}

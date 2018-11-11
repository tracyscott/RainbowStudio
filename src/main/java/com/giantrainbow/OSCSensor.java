package com.giantrainbow;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.CompoundParameter;

/**
 * Utility class for hosting sensor values sent via OSC, e.g. mobile accelerometer data.
 * This class creates CompoundParameter knobs that can serve as modulation sources.
 */
public class OSCSensor extends LXComponent {

  public CompoundParameter accelXKnob = new CompoundParameter("accelx", 0, -30.0, 30.0);
  public CompoundParameter accelYKnob = new CompoundParameter("accely", 0, -30.0, 30.0);
  public CompoundParameter accelZKnob = new CompoundParameter("accelz", 0, -30.0, 30.0);

  public OSCSensor(LX lx) {
    super(lx, "oscsensor");
    addParameter(accelXKnob);
    addParameter(accelYKnob);
    addParameter(accelZKnob);
  }
}

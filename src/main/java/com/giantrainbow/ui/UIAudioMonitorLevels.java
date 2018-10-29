package com.giantrainbow.ui;

import com.giantrainbow.ParameterFile;
import com.giantrainbow.PropertyFile;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIKnob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIAudioMonitorLevels extends UIConfig {
  public static final String MIN_THRESHOLD = "MinThr";
  public static final String AVG_TIME_SECS = "AvgTS";
  public static final String GAIN_INCREMENT = "GainInc";
  public static final String REDUCE_THRESHOLD = "RThrsh";
  public static final String GAIN_THRESHOLD = "GThrsh";

  public static final String title = "AUDIO MONITOR";
  public static final String filename = "audiomonitor.json";
  public UIAudioMonitorLevels(final LXStudio.UI ui) {
    super(ui, title, filename);

    registerCompoundParameter(MIN_THRESHOLD, -80.0, -100.0, 40.0);
    registerCompoundParameter(AVG_TIME_SECS, 1.0, 3.0, 30.0);
    registerCompoundParameter(GAIN_INCREMENT, 1.0, 0.1, 5.0);
    registerCompoundParameter(REDUCE_THRESHOLD, 45.0, 10.0, 50.0);
    registerCompoundParameter(GAIN_THRESHOLD, 20.0, 1.0, 40.0);

    save();
    buildUI(ui);
  }
}

package com.giantrainbow.ui;

import heronarts.lx.LX;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.studio.LXStudio;

public class UIProgramConfig extends UIConfig {
  public static final String title = "Program";
  public static final String filename = "program.json";
  public LX lx;
  private boolean parameterChanged = false;

  public UIProgramConfig(final LXStudio.UI ui, LX lx) {
    super(ui, title, filename);
    this.lx = lx;
    String[] channelNames = UIModeSelector.standardModeChannelNames;
    for (int i = 0; i < channelNames.length; i++) {
      registerStringParameter(channelNames[i], "00:00-24:00");
    }

    save();

    buildUI(ui);
  }

  public int getChannelStart(String channelName) {
    StringParameter sp = getStringParameter(channelName);
    if (sp == null) {
      return 0;
    }
    String val = sp.getString();
    String[] startEnd = val.split("-");
    if (startEnd.length != 2)
      return 0;
    String[] startHourMinutes = startEnd[0].split(":");
    if (startHourMinutes.length != 2)
      return 0;
    int start = 60 * Integer.parseInt(startHourMinutes[0]) + Integer.parseInt(startHourMinutes[1]);
    return start;
  }

  public int getChannelEnd(String channelName) {
    StringParameter sp = getStringParameter(channelName);
    if (sp == null) return 24 * 60;
    String val = sp.getString();
    String[] startEnd = val.split("-");
    if (startEnd.length != 2)
      return 24 * 60;
    String[] endHourMinutes = startEnd[1].split(":");
    if (endHourMinutes.length != 2)
      return 24 * 60;
    int end = 60 * Integer.parseInt(endHourMinutes[0]) + Integer.parseInt(endHourMinutes[1]);
    return end;
  }
}

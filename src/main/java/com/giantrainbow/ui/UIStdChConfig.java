
package com.giantrainbow.ui;

import heronarts.lx.LX;
import heronarts.lx.studio.LXStudio;

public class UIStdChConfig extends UIConfig {

  public static final String title = "Standard Channels";
  public static final String filename = "stdchannels.json";
  public LX lx;

  public UIStdChConfig(final LXStudio.UI ui, LX lx) {
    super(ui, title, filename);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    this.lx = lx;
    registerStringParameter("stdchannels", "MULTI,GIF,SPECIAL,RBW");
    save();

    buildUI(ui);
  }

  public String[] getStandardChannels() {
    return getStringParameter("stdchannels").getString().split(",");
  }
}

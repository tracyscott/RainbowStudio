package com.giantrainbow.ui;

import com.giantrainbow.ParameterFile;
import com.giantrainbow.PropertyFile;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UITextBox;

import java.io.IOException;

public class UIPixliteConfig extends UIConfig {
  public static final String PIXLITE_1_IP = "Pixlite 1 IP";
  public static final String PIXLITE_1_PORT = "Pixlite 1 Port";
  public static final String PIXLITE_1_PANELS = "Pixlite 1 Panels";
  public static final String PIXLITE_2_IP = "Pixlite 2 IP";
  public static final String PIXLITE_2_PORT = "Pixlite 2 Port";
  public static final String PIXLITE_2_PANELS = "Pixlite 2 Panels";

  public static final String title = "PIXLITE";
  public static final String filename = "pixliteconfig.json";

  public UIPixliteConfig(final LXStudio.UI ui) {
    super(ui, title, filename);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();

    registerStringParameter(PIXLITE_1_IP, "192.168.2.134");
    registerStringParameter(PIXLITE_1_PORT, "6454");
    registerStringParameter(PIXLITE_1_PANELS, "16");
    registerStringParameter(PIXLITE_2_IP, "192.168.2.134");
    registerStringParameter(PIXLITE_2_PORT, "6455");
    registerStringParameter(PIXLITE_2_PANELS, "12");

    save();
  }

  @Override
  public void onSave() {
    // TODO(tracy): Reconfigure output.
  }
}

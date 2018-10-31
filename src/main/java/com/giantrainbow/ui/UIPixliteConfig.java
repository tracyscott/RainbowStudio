package com.giantrainbow.ui;

import com.giantrainbow.Output;
import com.giantrainbow.ParameterFile;
import com.giantrainbow.PropertyFile;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
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
  public LX lx;
  private boolean parameterChanged = false;

  public UIPixliteConfig(final LXStudio.UI ui, LX lx) {
    super(ui, title, filename);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    this.lx = lx;

    registerStringParameter(PIXLITE_1_IP, "192.168.2.134");
    registerStringParameter(PIXLITE_1_PORT, "6454");
    registerStringParameter(PIXLITE_1_PANELS, "16");
    registerStringParameter(PIXLITE_2_IP, "192.168.2.134");
    registerStringParameter(PIXLITE_2_PORT, "6455");
    registerStringParameter(PIXLITE_2_PANELS, "12");

    save();

    buildUI(ui);
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    parameterChanged = true;
  }

  @Override
  public void onSave() {
    // Only reconfigure if a parameter changed.
    if (parameterChanged) {
      boolean originalEnabled = lx.engine.output.enabled.getValueb();
      lx.engine.output.enabled.setValue(false);
      lx.engine.output.removeChild(Output.datagramOutput);
      Output.configureOutputMultiPanel(lx, true, true);
      parameterChanged = false;
      lx.engine.output.enabled.setValue(originalEnabled);
    }
  }
}

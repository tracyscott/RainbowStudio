package com.giantrainbow.ui;

import com.giantrainbow.Output;
import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;

public class UIPixliteConfig extends UIConfig {
  public static final String PIXLITE_1_IP = "ip1";
  public static final String PIXLITE_1_PORT = "port1";
  public static final String PIXLITE_1_PANELS = "panels1";
  public static final String PIXLITE_2_IP = "ip2";
  public static final String PIXLITE_2_PORT = "port2";
  public static final String PIXLITE_2_PANELS = "panels2";

  public static final String title = "pixlite";
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
      Output.configureOutputMultiPanelExpanded(lx, true, true, RainbowStudio.panel16Config,
          RainbowStudio.panel12Config);
      parameterChanged = false;
      lx.engine.output.enabled.setValue(originalEnabled);
    }
  }
}

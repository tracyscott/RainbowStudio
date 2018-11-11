package com.giantrainbow.ui;

import com.giantrainbow.Output;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UIPanelConfig extends UIConfig {
  private static final Logger logger = Logger.getLogger(UIPanelConfig.class.getName());

  public static final String titleBase = "panel";
  public static final String filenameBase = "panelconfig";
  public String title;
  public String filename = "";
  public LX lx;
  private boolean parameterChanged = false;
  private int numPanels;
  Map<String, List<Integer>> panelInputsMap = null;
  public static UIPanelConfig panel16Config;
  public static UIPanelConfig panel12Config;

  static public UIPanelConfig newPanelConfig16(LXStudio.UI ui, LX lx) {
    panel16Config = new UIPanelConfig(ui, lx, titleBase + 16, 16, filenameBase + 16 + ".json");
    return panel16Config;
  }

  static public UIPanelConfig newPanelConfig12(LXStudio.UI ui, LX lx) {
    panel12Config = new UIPanelConfig(ui, lx, titleBase + 12, 12, filenameBase + 12 + ".json");
    return panel12Config;
  }


  public UIPanelConfig(final LXStudio.UI ui, LX lx, String title, int numPanels, String filename) {
    super(ui, title, filename);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    this.lx = lx;
    this.title = title;
    this.filename = filename;
    this.numPanels = numPanels;
    int numInputs = 2;
    int universeNum = 0;
    for (int i = 0; i < numPanels; i++) {
      for (int j = 0; j < numInputs; j++) {
        registerStringParameter(i + "." + j, "" + universeNum);
        universeNum += 2;
      }
    }
    parameterChanged = false;
    save();
    buildUI(ui);
  }

  /**
   * @return The number of panels this configuration represents.
   */
  public int getNumPanels() {
    return numPanels;
  }

  /**
   * Rebuilds the panelInputMap, should be run after any changes and before attempts
   * to rebuild the datagram output.
   */
  protected void rebuildPanelInputMap() {
    panelInputsMap = new HashMap<String, List<Integer>>();
    final int universesPerPanel = 4;
    int universesPerInput = 2;
    for (int i = 0; i < numPanels; i++) {
      // Universes start at 1
      for (int panelInputNum = 0; panelInputNum < 2; panelInputNum++) {
        // We add universeNum and universeNum+1 so we should + panelInputNum*2 here.
        int universeNum = i * universesPerPanel + panelInputNum*2;
        List<Integer> universesThisPanelInput = new ArrayList<Integer>();
        // Reset universe numbers for second pixlite
        // Map 2 universes to a given Panel.PanelInput key.
        universesThisPanelInput.add(universeNum);
        universesThisPanelInput.add(universeNum+1);
        String mapAddress = "" + i + "." + panelInputNum;
        logger.info("Expanded panel.panelInput=" + mapAddress + " startUniverse:" + universeNum);
        panelInputsMap.put(mapAddress, universesThisPanelInput);
      }
    }
  }

  /**
   * @return A Map from panel#.panel_input# to a list of universe numbers.  e.g. 1.0 -> (4,5)  1.1 -> (6,7)
   */
  public Map<String, List<Integer>> getPanelInputsMap() {
    if (panelInputsMap == null)
      rebuildPanelInputMap();
    return panelInputsMap;
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    parameterChanged = true;
    rebuildPanelInputMap();
  }

  @Override
  public void onSave() {
    // Only reconfigure if a parameter changed.
    if (parameterChanged) {
      boolean originalEnabled = lx.engine.output.enabled.getValueb();
      lx.engine.output.enabled.setValue(false);
      lx.engine.output.removeChild(Output.datagramOutput);
      Output.configureOutputMultiPanelExpanded(lx, true, true, panel16Config, panel12Config);
      parameterChanged = false;
      lx.engine.output.enabled.setValue(originalEnabled);
    }
  }
}

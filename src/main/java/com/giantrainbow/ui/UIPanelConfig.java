package com.giantrainbow.ui;

import com.giantrainbow.Output;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;

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
  List<Integer> selectedPanels = new ArrayList<Integer>();
  Map<Integer, UIButton> selectedPanelButtons = new HashMap<Integer, UIButton>();

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

  @Override
  public void buildUI(LXStudio.UI ui) {
    int knobsPerRow = 4;
    int knobCountThisRow = 0;
    setTitle(title);
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    UI2dContainer horizContainer = null;
    for (LXParameter p : parameters) {
      if (p instanceof LXListenableNormalizedParameter) {
        if (knobCountThisRow == 0) {
          horizContainer = new UI2dContainer(0, 30, ui.leftPane.global.getContentWidth(), 45);
          horizContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
          horizContainer.setPadding(0, 0, 0, 0);
          horizContainer.addToContainer(this);
        }
        UIKnob knob = new UIKnob((LXListenableNormalizedParameter)p);
        ((LXListenableParameter)p).addListener(this);
        knob.addToContainer(horizContainer);
        ++knobCountThisRow;
        if (knobCountThisRow == knobsPerRow) {
          knobCountThisRow = 0;
        }
      }
      if (p instanceof StringParameter) {
        knobCountThisRow = 0; // Reset the counter for knob containers
        UI2dContainer textRow = new UI2dContainer(0, 30, ui.leftPane.global.getContentWidth(), 20);
        textRow.setLayout(UI2dContainer.Layout.HORIZONTAL);
        textRow.setPadding(0, 0, 0, 0);
        textRow.addToContainer(this);

        UILabel label = new UILabel(0, 0, 45, 20);
        label.setLabel(p.getLabel());
        label.addToContainer(textRow);
        label.setPadding(5, 0);
        new UIButton(20 , 0, 20, 20) {
          @Override
          public void onToggle(boolean on) {
            // swap values
            if (on) {
              String label = p.getLabel();
              String[] parts = label.split("\\.");
              Integer panelNum = Integer.parseInt(parts[0]);
              Integer inputNum = Integer.parseInt(parts[1]);
              StringParameter thisSp = getStringParameter(label);
              String other = "";
              if (inputNum == 0) {
                other = "" + panelNum + "." + 1;
              } else {
                other = "" + panelNum + "." + 0;
              }
              StringParameter otherSp = getStringParameter(other);
              String thisValue = thisSp.getString();
              thisSp.setValue(otherSp.getString());
              otherSp.setValue(thisValue);
              save();
            }
          }
        }.setLabel("\u2191\u2193").setMomentary(true).addToContainer(textRow);

        UITextBox textBox = new UITextBox(70,0, 80, 20 );
        ((LXListenableParameter)p).addListener(this);
        textBox.setParameter((StringParameter)p);
        textBox.addToContainer(textRow);
        if (p.getLabel().endsWith("0")) {
          String[] parts = p.getLabel().split("\\.");
          int panelNum = Integer.parseInt(parts[0]);
          UIButton selectedButton = new UIButton(160, 0, 180, 20) {
            @Override
            public void onToggle(boolean on) {
              if (on) {
                // add to selected list
                selectedPanels.add(panelNum);
              } else {
                // remove from selected list
                selectedPanels.remove(new Integer(panelNum));
              }
            }
          };
          selectedButton.setLabel(" ").setMomentary (false).addToContainer(textRow);
          selectedPanelButtons.put(panelNum, selectedButton);
        }
      }
    }
    UI2dContainer swapRow = new UI2dContainer(0, 0, ui.leftPane.global.getContentWidth(), 20);
    swapRow.setLayout(UI2dContainer.Layout.HORIZONTAL);
    swapRow.setPadding(0, 0, 0, 0);
    swapRow.addToContainer(this);
    UILabel label = new UILabel(0, 0, 70, 20);
    label.setLabel(" ");
    label.addToContainer(swapRow);
    new UIButton(getContentWidth() - 20, 0, 100, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          // If more than 2 selected, do nothing.  Otherwise, swap the
          // two selected panels.
          if (selectedPanels.size() != 2) {
            return;
          }
          int firstPanel = selectedPanels.get(0);
          int secondPanel = selectedPanels.get(1);
          // Now swap firstPanel.0, firstPanel.1 with secondPanel.0, secondPanel.1
          StringParameter firstPanel0 = getStringParameter(firstPanel + ".0");
          StringParameter firstPanel1 = getStringParameter(firstPanel + ".1");
          StringParameter secondPanel0 = getStringParameter(secondPanel + ".0");
          StringParameter secondPanel1 = getStringParameter(secondPanel + ".1");
          String firstPanel0Value = firstPanel0.getString();
          String firstPanel1Value = firstPanel1.getString();
          firstPanel0.setValue(secondPanel0.getString());
          firstPanel1.setValue(secondPanel1.getString());
          secondPanel0.setValue(firstPanel0Value);
          secondPanel1.setValue(firstPanel1Value);
          UIButton firstBtn = selectedPanelButtons.get(firstPanel);
          firstBtn.setActive(false);
          UIButton secondBtn = selectedPanelButtons.get(secondPanel);
          secondBtn.setActive(false);
          //selectedPanels.clear();
          save();
        }
      }
    }.setLabel("swap panels").setMomentary(true).addToContainer(swapRow);

    // Button saving config.
    new UIButton(getContentWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          save();
        }
      }
    }.setLabel("\u21BA").setMomentary(true).addToContainer(this);
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
        String mapAddress = i + "." + panelInputNum;
        logger.info("mapAddr: " + mapAddress + " = " + getStringParameter(mapAddress).getString());
        int universeNum = Integer.parseInt(getStringParameter(mapAddress).getString()); // * universesPerPanel + panelInputNum*2;
        List<Integer> universesThisPanelInput = new ArrayList<Integer>();
        // Reset universe numbers for second pixlite
        // Map 2 universes to a given Panel.PanelInput key.
        universesThisPanelInput.add(universeNum);
        universesThisPanelInput.add(universeNum+1);
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
      logger.info("Saving new panel mapping and restarting output");
      boolean originalEnabled = lx.engine.output.enabled.getValueb();
      lx.engine.output.enabled.setValue(false);
      lx.engine.output.removeChild(Output.datagramOutput);
      Output.configureOutputMultiPanelExpanded(lx, true, true, panel16Config, panel12Config);
      parameterChanged = false;
      lx.engine.output.enabled.setValue(originalEnabled);
    }
  }
}

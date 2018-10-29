package com.giantrainbow.ui;

import com.giantrainbow.ParameterFile;
import com.giantrainbow.PropertyFile;
import heronarts.lx.parameter.*;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UITextBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIConfig extends UICollapsibleSection implements LXParameterListener {
  public ParameterFile paramFile;
  public List<LXParameter> parameters = new ArrayList<LXParameter>();
  public Map<String, LXParameter> paramLookup = new HashMap<String, LXParameter>();
  public String title;
  public String filename;

  public UIConfig(final LXStudio.UI ui, String title, String filename) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    this.title = title;
    this.filename = filename;
    load();
  }

  public void load() {
    paramFile = new ParameterFile(filename);
    try {
      paramFile.load();
    } catch (PropertyFile.NotFound nfex) {
      System.out.println(filename + ", property not found.");
    } catch (IOException ioex) {
      System.err.println(filename + " not found, will be created.");
    }
  }

  public StringParameter registerStringParameter(String label, String value) {
    StringParameter sp = paramFile.getStringParameter(label, value);
    parameters.add(sp);
    paramLookup.put(label, sp);
    return sp;
  }

  public CompoundParameter registerCompoundParameter(String label, double value, double base, double range) {
    CompoundParameter cp = paramFile.getCompoundParameter(label, value, base, range);
    parameters.add(cp);
    paramLookup.put(label, cp);
    return cp;
  }

  public DiscreteParameter registerDiscreteParameter(String label, int value, int min, int max) {
    DiscreteParameter dp = paramFile.getDiscreteParameter(label, value, min, max);
    parameters.add(dp);
    paramLookup.put(label, dp);
    return dp;
  }

  public StringParameter getStringParameter(String label) {
    return (StringParameter) paramLookup.get(label);
  }

  public CompoundParameter getCompoundParameter(String label) {
    return (CompoundParameter) paramLookup.get(label);
  }

  public DiscreteParameter getDiscreteParameter(String label) {
    return (DiscreteParameter) paramLookup.get(label);
  }

  public void save() {
    try {
      paramFile.save();
    } catch (IOException ioex) {
      System.err.println("Error saving " + filename + " " + ioex.getMessage());
    }
    onSave();
  }

  public void onParameterChanged(LXParameter p) {

  }

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
        UITextBox textBox = new UITextBox(0,0, ui.leftPane.global.getContentWidth() - 10, 20 );
        ((LXListenableParameter)p).addListener(this);
        textBox.setParameter((StringParameter)p);
        textBox.addToContainer(this);
      }
    }
    // Button saving config.
    new UIButton(getContentWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          save();
        }
      }
    }
    .setLabel("\u21BA").setMomentary(true).addToContainer(this);
  }

  /**
   * Method is called after saving a config.  Subclasses should override this method if they
   * need to perform some action only after all the parameters are set.
   */
  public void onSave() {
  }
}

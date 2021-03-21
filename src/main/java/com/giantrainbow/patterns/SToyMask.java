package com.giantrainbow.patterns;

import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.component.UIKnob;
import processing.core.PImage;

import java.util.logging.Logger;

public class SToyMask extends ShaderToy {
  private static final Logger logger = Logger.getLogger(SToyMask.class.getName());
  PImage mask;

  DiscreteParameter maskKnob = new DiscreteParameter("mask", 0, 0, 50)
      .setDescription("Apply imgpp/stoymask#.png after the shader renders");
  public SToyMask(LX lx) {
    super(lx);

    addParameter(maskKnob);

    maskKnob.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) {
        loadMask();
      }
    });
  }

  protected void loadMask() {
    String filename = "imgpp/stoymask" + maskKnob.getValuei() + ".png";
    mask = RainbowStudio.pApplet.loadImage(filename);
    if (mask != null) {
      logger.info("Loaded mask " + filename);
      if (mask.height != pg.height || mask.width != pg.width) {
        logger.info("image wrong size, resizing");
        mask.resize(pg.width, pg.height);
      }
    }
  }

  @Override
  public void postDraw(double drawDeltaMs) {
    if (mask != null)
      pg.image(mask, 0, 0, pg.width, pg.height);
  }

  @Override
  protected void addUI() {
    new UIKnob(maskKnob).setWidth(35).addToContainer(rightPanel);
  }
}

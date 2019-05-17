package com.giantrainbow.patterns;

import com.giantrainbow.textures.Flowers;

import heronarts.lx.LX;
import processing.core.PImage;

public abstract class AbstractSpinnyFlowers extends AbstractSpinnyDiscs {

  Flowers flowers;
    
  public AbstractSpinnyFlowers(LX lx) {
    super(lx);

    this.flowers = new Flowers(BALL_COUNT);
  }

  PImage getTexture(int number) {
      return this.flowers.getTexture(number);
  }
};

package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PConstants;

import java.util.List;

@LXCategory(LXCategory.FORM)
public class TextFxHScroll extends TextFx {

  public TextFxHScroll(LX lx) {
    super(lx);
  }

  /**
   * Renders the characters for this frame.
   *
   * @param deltaMs Time since last frame.
   * @return True if the animation is finished and we should start holdTime countdown.
   */
  @Override
  public boolean drawCharacters(double deltaMs) {
    boolean areChDone = true;
    for (int j = 0; j < taDetails.spritesPerLine.size(); j++) {
      List<CharSprite> thisLineSprites = taDetails.spritesPerLine.get(j);
      for (int i = 0; i < thisLineSprites.size(); i++) {
        CharSprite ch = thisLineSprites.get(i);
        pg.pushMatrix();
        pg.imageMode(PConstants.CENTER);
        ch.curPosX -= xSpeed.getValuef()/10f;
        if (ch.curPosX > -fontSizeKnob.getValuef()*2.0f)
          areChDone = false;
        pg.translate(ch.curPosX + ch.chImage.width / 2, ch.chImage.height / 2f + ch.curPosY);
        pg.image(ch.chImage, 0, 0, ch.chImage.width, ch.chImage.height);
        pg.popMatrix();
      }
    }
    return areChDone;
  }
}

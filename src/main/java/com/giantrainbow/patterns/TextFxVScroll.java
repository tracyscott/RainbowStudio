
package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

import java.util.List;

@LXCategory(LXCategory.FORM)
public class TextFxVScroll extends TextFx {

  public TextFxVScroll(LX lx) {
    super(lx);
    preRenderCh = false;
    spaceChWidth = 4;
  }

  /**
   * Resets the animation to initial conditions.
   */
  protected void resetAnimation() {
    for (int j = 0; j < taDetails.spritesPerLine.size(); j++) {
      List<CharSprite> thisLineSprites = taDetails.spritesPerLine.get(j);
      int lineWidth = taDetails.lineWidths.get(j);
      for (int i = 0; i < thisLineSprites.size(); i++) {
        CharSprite chSprite = thisLineSprites.get(i);
        // During initialization, the target positions are centered horizontally and vertically
        chSprite.curPosX = chSprite.targetPosX;
        chSprite.curPosY = 40 + j * (fontSizeKnob.getValuei() + fontHtOffset.getValuei() + 2);
      }
    }
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
        ch.curPosY = ch.curPosY - xSpeed.getValuef() / 10f;
        if (ch.curPosY > -fontSizeKnob.getValuef() * 2.0f)
          areChDone = false;

        pg.fill(ch.color);
        pg.text(ch.ch, ch.curPosX, Math.round(ch.curPosY));
      }
    }
    return areChDone;
  }
}

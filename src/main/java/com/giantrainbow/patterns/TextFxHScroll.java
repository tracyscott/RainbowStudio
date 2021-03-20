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
   * Resets the animation to initial conditions.
   */
  protected void resetAnimation() {
    if (leftToRight.getValueb()) {
      for (int j = 0; j < taDetails.spritesPerLine.size(); j++) {
        List<CharSprite> thisLineSprites = taDetails.spritesPerLine.get(j);
        for (int i = 0; i < thisLineSprites.size(); i++) {
          CharSprite chSprite = thisLineSprites.get(i);
          // Put the character off the right side of the Rainbow.
          chSprite.curPosX = 430 + chSprite.targetPosX + j * 300;
        }
      }
    } else {
      for (int j = 0; j < taDetails.spritesPerLine.size(); j++) {
        List<CharSprite> thisLineSprites = taDetails.spritesPerLine.get(j);
        int curPos = -10;
        for (int i = 0; i < thisLineSprites.size(); i++) {
          CharSprite chSprite = thisLineSprites.get(i);
          int chWidth = chSprite.chImage.width;
          // Put the character off the left side of the Rainbow.  Since this is a
          // right-to-left language, the next character is at a more negative value.
          chSprite.curPosX = curPos - chWidth;
          curPos = curPos - chWidth;
        }
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
        if (leftToRight.getValueb()) {
          ch.curPosX -= xSpeed.getValuef() / 10f;
          if (ch.curPosX > -fontSizeKnob.getValuef() * 2.0f)
            areChDone = false;
        } else {
          ch.curPosX += xSpeed.getValuef() / 10f;
          if (ch.curPosX < pg.width + 5.0f)
            areChDone = false;
        }
        pg.image(ch.chImage, ch.curPosX, ch.curPosY + yAdj.getValuef(), ch.chImage.width, ch.chImage.height);
      }
    }
    return areChDone;
  }
}

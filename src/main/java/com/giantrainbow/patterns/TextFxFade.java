
package com.giantrainbow.patterns;

import com.giantrainbow.EaseUtil;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;

import java.util.List;
import java.util.logging.Logger;

@LXCategory(LXCategory.FORM)
public class TextFxFade extends TextFx {
  private static final Logger logger = Logger.getLogger(TextFxZoom.class.getName());

  boolean fadeIn = true;
  float fadeElapsed = 0f;
  float fadeDuration = 4f;
  float fadeOutDuration = 4f;

  public TextFxFade(LX lx) {
    super(lx);
    preRenderCh = false;
    spaceChWidth = 4;
  }

  @Override
  public void onActive() {
    super.onActive();
    resetAnimation();
  }

  /**
   * Resets the animation to initial conditions.
   */
  protected void resetAnimation() {
    fadeElapsed = 0f;
    fadeIn = true;
    targetHoldTime = 0.5f; //500;  // Since we fade out, have base battern just had .5 seconds.
  }

  /**
   * Renders the characters for this frame.
   *
   * @param deltaMs Time since last frame.
   * @return True if the animation is finished and we should start holdTime countdown.
   */
  @Override
  public boolean drawCharacters(double deltaMs) {
    float currentFade = EaseUtil.ease(fadeElapsed / (fadeDuration * 1000f), 4);
    if (!fadeIn)
      currentFade = EaseUtil.ease(1f - (fadeElapsed / (fadeOutDuration * 1000f)), 4);

    for (int j = 0; j < taDetails.spritesPerLine.size(); j++) {
      List<CharSprite> thisLineSprites = taDetails.spritesPerLine.get(j);
      for (int i = 0; i < thisLineSprites.size(); i++) {
        CharSprite ch = thisLineSprites.get(i);
        if (leftToRight.isOn()) {
          pg.pushMatrix();
          pg.translate(ch.curPosX, ch.curPosY + fontSizeKnob.getValuef());
          pg.scale(1f + 0.1f);
          pg.fill(0, currentFade * 255f);
          pg.text(ch.ch, 0, yAdj.getValuef());
          pg.popMatrix();
        }
        pg.pushMatrix();
        pg.translate(ch.curPosX, ch.curPosY + fontSizeKnob.getValuef());
        pg.scale(1f);
        pg.fill(ch.color, currentFade * 255f);
        pg.text(ch.ch, 0, yAdj.getValuef());
        pg.popMatrix();
      }
    }
    if (fadeIn) {
      fadeElapsed += deltaMs;
      if (fadeElapsed > (fadeDuration * 1000f)) {
        fadeIn = false;
        fadeElapsed = 0f;
      }
    } else {
      fadeElapsed += deltaMs;
      if (fadeElapsed > (fadeOutDuration * 1000f)) {
        fadeElapsed = fadeOutDuration * 1000f;
        return true;
      }
    }

    return false;
  }
}

package com.giantrainbow.patterns;

import com.giantrainbow.EaseUtil;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;

import java.util.List;
import java.util.logging.Logger;

@LXCategory(LXCategory.FORM)
public class TextFxZoom extends TextFx {
  private static final Logger logger = Logger.getLogger(TextFxZoom.class.getName());

  protected int currentLine = 0;
  float scaleElapsed = 0f;
  float scaleDuration = 4f;

  public TextFxZoom(LX lx) {
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
    for (int j = 0; j < taDetails.spritesPerLine.size(); j++) {
      List<CharSprite> thisLineSprites = taDetails.spritesPerLine.get(j);
      int lineWidth = taDetails.lineWidths.get(j);
      for (int i = 0; i < thisLineSprites.size(); i++) {
        CharSprite chSprite = thisLineSprites.get(i);
        // During initialization, the target positions are centered horizontally and vertically
        chSprite.curPosX = chSprite.targetPosX;
        chSprite.curPosY = chSprite.targetPosY;
      }
    }
    scaleElapsed = 0f;
  }

  /**
   * Renders the characters for this frame.
   *
   * @param deltaMs Time since last frame.
   * @return True if the animation is finished and we should start holdTime countdown.
   */
  @Override
  public boolean drawCharacters(double deltaMs) {
    float currentScale = EaseUtil.ease(scaleElapsed / (scaleDuration * 1000f), 4);
    for (int j = 0; j < taDetails.spritesPerLine.size(); j++) {
      List<CharSprite> thisLineSprites = taDetails.spritesPerLine.get(j);
      for (int i = 0; i < thisLineSprites.size(); i++) {
        CharSprite ch = thisLineSprites.get(i);
        if (leftToRight.isOn()) {
          pg.pushMatrix();
          pg.translate(ch.curPosX, ch.curPosY + fontSizeKnob.getValuef());
          pg.scale(currentScale + 0.1f);
          pg.fill(0);
          pg.text(ch.ch, 0, yAdj.getValuef());
          pg.popMatrix();
        }
        pg.pushMatrix();
        pg.translate(ch.curPosX, ch.curPosY + fontSizeKnob.getValuef());
        pg.scale(currentScale);
        pg.fill(ch.color);
        pg.text(ch.ch, 0, yAdj.getValuef());
        pg.popMatrix();
      }
    }
    scaleElapsed += deltaMs;
    if (scaleElapsed > (scaleDuration * 1000f)) {
      scaleElapsed = scaleDuration * 1000f;
      return true;
    }

    return false;
  }
}

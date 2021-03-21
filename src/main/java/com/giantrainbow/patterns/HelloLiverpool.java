package com.giantrainbow.patterns;

import com.giantrainbow.PathUtils;
import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.logging.Logger;

public class HelloLiverpool extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(HelloLiverpool.class.getName());

  public CompoundParameter time = new CompoundParameter("time", 2.0, 0.1, 10.0)
      .setDescription("Time for text effect");
  public CompoundParameter delay = new CompoundParameter("delay", 5.0, 0.1, 30.0)
      .setDescription("Delay before effect starts");
  public CompoundParameter hold = new CompoundParameter("hold", 10.0, 1.0, 100.0)
      .setDescription("Hold time before advancing the pattern");
  public BooleanParameter advP = new BooleanParameter("advP", false)
      .setDescription("If enabled, auto-advance to next pattern");
  public CompoundParameter minScaleP = new CompoundParameter("minSc", 0.05, 0.01, 10.0);
  public CompoundParameter maxScaleP = new CompoundParameter("maxSc", 0.95, 0.01, 10.0);
  public CompoundParameter xPosP = new CompoundParameter("xPos", 209, -200, 620);
  public CompoundParameter yPosP = new CompoundParameter("yPos", 14, -30, 100);

  PGraphics backgroundG;

  private static final String SPRITE_DIR = "spritepp/";
  private static final String IMGPP_DIR = "imgpp/";
  PImage[] liverLeftFacing;
  PImage[] liverRightFacing;
  PImage rainbowBg;
  PImage skylineBg;
  PImage liverpoolImg;

  private static final String SKYLINE_LOW = "stoymask5.png";
  private static final String LIVERPOOL_IMG = "helloliverpoolcrop.png";
  private static final String LIVERBIRD_LEFT_FACING = "liverbird.gif";
  private static final String LIVERBIRD_RIGHT_FACING = "liverbirdright.gif";

  int leftFacingBirdX = 319;
  int rightFacingBirdX = 117;
  float minScale = 0.01f;
  float elapsedTime = 0f;
  boolean isDone = false;
  boolean autoCycleWasEnabled = false;
  float curHoldDuration = 0f;
  float curDelayDuration = 0f;
  int phase = 0;  // 0 = delay, 1 = scale, 2 = hold

  public HelloLiverpool(LX lx) {
    super(lx, "");
    fpsKnob.setValue(26);

    addParameter(time);
    addParameter(minScaleP);
    addParameter(maxScaleP);
    addParameter(xPosP);
    addParameter(yPosP);
    addParameter(delay);
    addParameter(hold);
    addParameter(advP);

    // Build the static background image out of components.
    liverLeftFacing = PathUtils.loadSprite(RainbowStudio.pApplet, SPRITE_DIR + LIVERBIRD_LEFT_FACING);
    leftFacingBirdX -= liverLeftFacing[0].width + 1;
    liverRightFacing = PathUtils.loadSprite(RainbowStudio.pApplet, SPRITE_DIR + LIVERBIRD_RIGHT_FACING);
    rightFacingBirdX -= liverRightFacing[0].width + 1;
    rainbowBg = RenderImageUtil.rainbowFlagAsPGraphics(pg.width, pg.height);
    skylineBg = RainbowStudio.pApplet.loadImage(IMGPP_DIR + SKYLINE_LOW);
    backgroundG = RainbowStudio.pApplet.createGraphics(pg.width, pg.height);
    backgroundG.beginDraw();
    backgroundG.image(rainbowBg, 0, 0, backgroundG.width, backgroundG.height);
    backgroundG.image(skylineBg, 0, 0, backgroundG.width, backgroundG.height);
    backgroundG.image(liverLeftFacing[0], leftFacingBirdX, 0, liverLeftFacing[0].width, liverLeftFacing[0].height);
    backgroundG.image(liverRightFacing[0], rightFacingBirdX, 0, liverRightFacing[0].width,
        liverRightFacing[0].height);
    backgroundG.endDraw();

    liverpoolImg = RainbowStudio.pApplet.loadImage(IMGPP_DIR + LIVERPOOL_IMG);
    reset();
  }

  /**
   * Easing functions.
   * sin((t * PI) / 2)
   * 1 - (1 - t) * (1 - t)
   * 1 - pow(1 - t, 3)
   * 1 - pow(1 - t, 4)
   * 1 - pow(1 - t, 5)
   */
  public float ease(float t) {
    return 1.0f - (float)Math.pow(1.0 - t, 3);
  }

  public float getScale() {
    // Compute t zero to one
    float t = (elapsedTime/1000f)/time.getValuef();
    float scaleRange = maxScaleP.getValuef() - minScaleP.getValuef();
    return minScale + scaleRange * ease(t);
  }

  public void reset() {
    elapsedTime = 0f;
    phase = 0;
    curDelayDuration = 0f;
    curHoldDuration = 0f;
    isDone = false;
  }

  @Override
  public void onActive() {
    reset();
    autoCycleWasEnabled = getChannel().autoCycleEnabled.getValueb();
    getChannel().autoCycleEnabled.setValue(false);
  }


  @Override
  public void draw(double deltaDrawMs) {
    pg.background(0, 0);
    pg.imageMode(PConstants.CORNERS);
    pg.image(backgroundG, 0, 0, backgroundG.width, backgroundG.height);
    if (phase == 0) {
      pg.imageMode(PConstants.CENTER);
      pg.image(liverpoolImg, (int)xPosP.getValuef(), (int)yPosP.getValuef(),
          liverpoolImg.width * minScaleP.getValuef(),
          liverpoolImg.height * minScaleP.getValuef());
      curDelayDuration += deltaDrawMs;
      if (curDelayDuration/1000f > delay.getValuef())
        phase = 1;
    } else if (phase == 1) {
      pg.imageMode(PConstants.CENTER);
      float scale = getScale();
      pg.image(liverpoolImg, (int)xPosP.getValuef(), (int)yPosP.getValuef(), liverpoolImg.width * scale,
          liverpoolImg.height * scale);
      elapsedTime += deltaDrawMs;
      if (elapsedTime / 1000f > time.getValuef()) {
        phase = 2;
      }
    } else if (phase == 2) {
      pg.imageMode(PConstants.CENTER);
      pg.image(liverpoolImg, (int)xPosP.getValuef(), (int)yPosP.getValuef(),
          liverpoolImg.width * maxScaleP.getValuef(),
          liverpoolImg.height * maxScaleP.getValuef());
      curHoldDuration += deltaDrawMs;
      if (curHoldDuration/1000f > hold.getValuef()) {
        if (advP.getValueb()) {
          // advance pattern
          getChannel().autoCycleEnabled.setValue(autoCycleWasEnabled);
          getChannel().goNext();
        } else {
          reset();
        }
      }
    }
  }
}

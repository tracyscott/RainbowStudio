package com.giantrainbow.patterns;

import com.giantrainbow.PathUtils;
import com.giantrainbow.RainbowStudio;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwShadertoy;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

import java.io.File;
import java.util.logging.Logger;

import static com.giantrainbow.RainbowStudio.pApplet;
import static processing.core.PConstants.P2D;

public class HorseRace extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(HorseRace.class.getName());

  public CompoundParameter speedKnob = new CompoundParameter("speed", 4.2f, 0f, 20f);
  PImage[] horseSprite1;
  PImage[] horseSprite2;
  PImage[] horseSprite3;
  PImage[][] horseSprites;
  PImage tileImage;
  boolean raceOver = false;
  static public final int PHASE_RACE = 0;
  static public final int PHASE_WINNER = 1;
  int numberOfWinnerFrames = 60;
  int currentWinnerFrames = 0;
  int winningHorse = -1;

  int phase = PHASE_RACE;

  float[] horsePositions = new float[3];

  private static final String SPRITE_DIR = "spritepp/";


  private static final String LOCAL_SHADER_DIR = "shaders/";
  private static final String CLOUDS_SHADER = "clouds.frag";
  DwPixelFlow context;
  DwShadertoy toy;
  PGraphics toyGraphics;
  float[] u1, u2;

  public HorseRace(LX lx) {
    super(lx, "");
    fpsKnob.setValue(26);
    horseSprite1 = PathUtils.loadSprite(pApplet, SPRITE_DIR + "horse.gif");
    horseSprite2 = PathUtils.loadSprite(pApplet, SPRITE_DIR + "horse2.gif");
    horseSprite3 = PathUtils.loadSprite(pApplet, SPRITE_DIR + "horse3.gif");
    horseSprites = new PImage[3][];
    horseSprites[0] = horseSprite1;
    horseSprites[1] = horseSprite2;
    horseSprites[2] = horseSprite3;
    logger.info("Sprite width=" + horseSprite1[0].width);

    reset();
    addParameter(speedKnob);

    tileImage = RainbowStudio.pApplet.loadImage("imgpp/background.gif");
    tileImage.loadPixels();

    initShader();
  }

  public void initShader() {
    u1 = new float[4];
    u2 = new float[4];
    toyGraphics = RainbowStudio.pApplet.createGraphics(pg.width, pg.height, P2D);
    loadShader();
    // context initialized in loadShader, print the GL hardware once when loading
    // the pattern.  left in for now while testing performance on different
    // graphics hardware.
    context.print();
    context.printGL();
  }

  protected void loadShader() {
    if (toy != null) {
      // release existing shader texture
      toy.release();
      toy = null;
    }
    if (context != null) context.release();
    context = new DwPixelFlow(RainbowStudio.pApplet);
    // TODO(tracy): Handle file not found issue.

    File local = new File(LOCAL_SHADER_DIR + CLOUDS_SHADER);
    if (local.isFile()) {
      toy = new DwShadertoy(context, local.getPath());
    }
  }

  public PImage runShader() {
    toy.set_iMouse(0f, 0f, 0f, 0f);
    u1[0] = 0.21f;  // cloud scale
    u1[1] = 0.33f;  // cloud speed
    u1[2] = 0.57f;  // cloud darkness
    u1[3] = 0.5f;   // cloud lightness

    u2[0] = 0.25f; // cloud cover
    u2[1] = 0.29f; // cloud alpha
    u2[2] = 0.23f; // sky tint (can go brownish)
    u2[3] = 0f;
    //toy.apply(toyGraphics);
    ShaderToy.shaderApply(context, toy, (PGraphicsOpenGL) toyGraphics, u1, u2);
    toyGraphics.loadPixels();
    toyGraphics.updatePixels();
    return toyGraphics;
  }

  public void reset() {
    for (int i = 0; i < horsePositions.length; i++) {
      horsePositions[i] = -70f;
    }
  }

  public void drawBackground() {
    pg.image(runShader(), 0, 0);
    int numTiles = (int) Math.ceil((float) pg.width / (float) tileImage.width);
    for (int i = 0; i < numTiles; i++) {
      pg.image(tileImage, i * tileImage.width, 0);
    }
  }

  @Override
  public void draw(double deltaDrawMs) {
    pg.background(0, 0);
    drawBackground();
    if (phase == PHASE_RACE) {
      for (int i = 0; i < horsePositions.length; i++) {
        horsePositions[i] += (float) Math.random() * speedKnob.getValuef();
        PImage sprite = horseSprites[i][((int) currentFrame + i) % horseSprite1.length];
        pg.image(sprite, (int) horsePositions[i], 30 - sprite.height, sprite.width, sprite.height);
      }
      boolean allFinished = true;
      for (int i = 0; i < horsePositions.length; i++) {
        if (horsePositions[i] > pg.width + horseSprite1[0].width) {
          if (winningHorse == -1) {
            winningHorse = i;
            raceOver = true;
            phase = PHASE_WINNER;
            currentWinnerFrames = 0;
          }
        } else {
          allFinished = false;
        }
      }
      if (allFinished) {
        phase = PHASE_WINNER;
        reset();
      }
    } else {
      PImage sprite = horseSprites[winningHorse][0];
      pg.image(sprite, pg.width / 2 - sprite.width / 2, 30 - sprite.height, sprite.width, sprite.height);
      ++currentWinnerFrames;
      if (currentWinnerFrames > numberOfWinnerFrames) {
        phase = PHASE_RACE;
        winningHorse = -1;
        reset();
      }
    }
  }
}

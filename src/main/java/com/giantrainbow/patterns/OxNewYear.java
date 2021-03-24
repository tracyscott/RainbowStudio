package com.giantrainbow.patterns;

import com.giantrainbow.FontUtil;
import com.giantrainbow.RainbowStudio;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwShadertoy;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

import java.io.File;
import static processing.core.PConstants.P2D;

public class OxNewYear extends PGPixelPerfect {

  public CompoundParameter speedKnob = new CompoundParameter("speed", 1f, 0f, 60f);
  public CompoundParameter oxFps = new CompoundParameter("OxFPS", 20, .1, 20)
      .setDescription("FPS for ox animation");
  public CompoundParameter txtY = new CompoundParameter("txtY", 24, 0, 30);
  public CompoundParameter txtX = new CompoundParameter("txtX", 56, 0, 200);
  public CompoundParameter fireworksFps = new CompoundParameter("fwkFps", 8, .1, 40);
  public String happyNewYear = "恭禧發財";
  public int[][] oxFrames = new int[4][];
  public int[][] fireworksFrames = new int[4][];
  AnimSprite[] oxen;
  PFont font;
  float txtPos = 0f;
  AnimSprite[] fireworks;

  private static final String LOCAL_SHADER_DIR = "shaders/";
  private static final String CLOUDS_SHADER = "stars.frag";
  DwPixelFlow context;
  DwShadertoy toy;
  PGraphics toyGraphics;
  float[] u1, u2;


  public OxNewYear(LX lx) {
    super(lx, "");
    int oxFramesLeft[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    oxFrames[0] = oxFramesLeft;
    oxen = new AnimSprite[2];
    oxen[0] = new AnimSprite("oxwalk.gif", 480f, 0f, AnimSprite.Direction.LEFT, oxFrames);
    oxen[1] = new AnimSprite("oxwalk.gif", 650f, 0f, AnimSprite.Direction.LEFT, oxFrames);

    int fireworksFront[] = new int[28];
    for (int i = 0; i < fireworksFront.length; i++)
      fireworksFront[i] = i;
    fireworksFrames[2] = fireworksFront;
    fireworks = new AnimSprite[6];
    fireworks[0] = new AnimSprite("fireworks.gif", 105, 0f, AnimSprite.Direction.FRONT, fireworksFrames);
    fireworks[0].enable();
    fireworks[1] = new AnimSprite("fireworks.gif", 285, 0f, AnimSprite.Direction.FRONT, fireworksFrames);
    fireworks[1].enable();
    fireworks[2] = new AnimSprite("fireworks.gif", 45, 0f, AnimSprite.Direction.FRONT, fireworksFrames);
    fireworks[2].enable();
    fireworks[3] = new AnimSprite("fireworks.gif", 345, 0f, AnimSprite.Direction.FRONT, fireworksFrames);
    fireworks[3].enable();
    fireworks[4] = new AnimSprite("fireworks.gif", 165, 0f, AnimSprite.Direction.FRONT, fireworksFrames);
    fireworks[4].enable();
    fireworks[5] = new AnimSprite("fireworks.gif", 225, 0f, AnimSprite.Direction.FRONT, fireworksFrames);
    fireworks[5].enable();


    addParameter(speedKnob);
    addParameter(oxFps);
    addParameter(txtY);
    addParameter(txtX);
    oxen[0].enable();
    oxen[1].enable();
    float txtPos = oxen[0].xPos + txtX.getValuef();
    font = FontUtil.getCachedFont("NotoSansSC-Regular", 24);
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
    u1[0] = 0f;  //
    u1[1] = 0f;  //
    u1[2] = 0f;  //
    u1[3] = 0f;  //

    u2[0] = 0.74f; // U2.x = particle length anti-distortion nominator multiplier
    u2[1] = 1f; // U2.y = particle length anti-distortion denominator multiplier
    u2[2] = 0.08f; // U2.z = twinkle amount.
    u2[3] = 0.5f;  // U2.w = brightness
    //toy.apply(toyGraphics);
    ShaderToy.shaderApply(context, toy, (PGraphicsOpenGL) toyGraphics, u1, u2);
    toyGraphics.loadPixels();
    toyGraphics.updatePixels();
    return toyGraphics;
  }

  @Override
  public void draw(double deltaDrawMs) {
    pg.background(0, 0);
    pg.image(runShader(), 0, 0);
    pg.textFont(font);
    for (int i = 0; i < fireworks.length; i++) {
      PImage frame = fireworks[i].nextFrame(deltaDrawMs, fireworksFps.getValuef(), 0f);
      pg.image(frame, fireworks[i].xPos, fireworks[i].yPos, frame.width, frame.height);
    }
    for (int i = 0; i < oxen.length; i++) {
      PImage frame = oxen[i].nextFrame(deltaDrawMs, oxFps.getValuef(), speedKnob.getValuef());
      if (frame != null)
        pg.image(frame, oxen[i].xPos, oxen[i].yPos, frame.width, frame.height);
      if (oxen[i].xPos < -240)
        oxen[i].xPos = 420;
    }
    pg.fill(200, 0, 0);
    pg.text(happyNewYear, oxen[0].xPos + txtX.getValuef(), txtY.getValuef());
    txtPos -= speedKnob.getValuef();
    if (txtPos < -240) {
      txtPos = 420 + txtX.getValuef();
    }
  }
}

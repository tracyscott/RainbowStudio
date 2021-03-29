package com.giantrainbow.patterns;

import com.giantrainbow.RainbowStudio;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwShadertoy;
import heronarts.lx.LX;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

import java.io.File;

import static processing.core.PConstants.P2D;

public class HereComesSun extends TextFxVScroll {
  private static final String LOCAL_SHADER_DIR = "shaders/";
  private static final String CLOUDS_SHADER = "clouds2.frag";
  DwPixelFlow context;
  DwShadertoy toy;
  PGraphics toyGraphics;
  float[] u1, u2;
  public float startU1x = 0.1f;  // cloud scale
  public float startU1y = 0.25f;  // cloud speed
  public float startU1z = 0.0f;   // cloud darkness
  public float startU1w = 0.36f;  // cloud light start

  public float startU2x = 1f;     // cloud cover start
  public float startU2y = .0f;   // cloud alpha start
  public float startU2z = .0f;   // cloud tint start

  public float finishU1x = 0.02f;  // cloud scale
  public float finishU1y = 0.02f;
  public float finishU1z = 1.0f;
  public float finishU1w = 1.0f;

  public float finishU2x = 0f;
  public float finishU2y = 0f;
  public float finishU2z = 1.0f; // cloud tint finish

  public float mouseYstart = 0.0f;
  public float mouseYfinish = 0.58f;

  float elapsedTime  = 0f;
  float maxElapsedTime = 120f;

  public HereComesSun(LX lx) {
    super(lx);
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

  /**
   * Gets an interpolated value.
   *
   * @param start
   * @param finish
   * @param t
   * @return
   */
  public float getInterp(float start, float finish, float t) {
    return start + (finish - start) * t;
  }

  /**
   * Get the current T value based on elapsed time.
   * @return
   */
  public float curT() {
    float t =  (elapsedTime/1000f) / ((rbBright.getValuef()/255f) * maxElapsedTime);
    if (t > 1.0f)
      t = 1.0f;
    return t;
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
    float sunPos = getInterp(mouseYstart, mouseYfinish, curT());
    toy.set_iMouse(1f, sunPos, 0f, 0f);
    u1[0] = getInterp(startU1x, finishU1x, curT()); //0.21f;  // cloud scale
    u1[1] = getInterp(startU1y, finishU1y, curT()); // 0.33f;  // cloud speed
    u1[2] = getInterp(startU1z, finishU1z, curT()); // 0.57f;  // cloud darkness
    u1[3] = getInterp(startU1w, finishU1w, curT()); //0.5f;   // cloud lightness

    u2[0] = getInterp(startU2x, finishU2x, curT()); //0.25f; // cloud cover
    u2[1] = getInterp(startU2y, finishU2y, curT()); //0.29f; // cloud alpha
    u2[2] = getInterp(startU2z, finishU2z, curT()); // 0.23f; // sky tint (can go brownish)
    u2[3] = 0f;
    //toy.apply(toyGraphics);
    ShaderToy.shaderApply(context, toy, (PGraphicsOpenGL) toyGraphics, u1, u2);
    toyGraphics.loadPixels();
    toyGraphics.updatePixels();
    return toyGraphics;
  }

  @Override
  public void onActive() {
    elapsedTime = 0f;
  }

  @Override
  public boolean drawCharacters(double drawDeltaMs) {
    pg.image(runShader(), 0, 0, pg.width, pg.height);
    elapsedTime += drawDeltaMs;
    return super.drawCharacters(drawDeltaMs);
  }
}

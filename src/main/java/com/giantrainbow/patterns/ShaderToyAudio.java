package com.giantrainbow.patterns;

import static processing.core.PConstants.P2D;

import com.giantrainbow.RainbowStudio;
import com.jogamp.opengl.GL2;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwShadertoy;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.audio.GraphicMeter;
import java.nio.ByteBuffer;
import processing.core.PGraphics;

@LXCategory(LXCategory.FORM)
public class ShaderToyAudio extends PGPixelPerfect {
  DwPixelFlow context;
  DwShadertoy toy;
  PGraphics toyGraphics;

  public ShaderToyAudio(LX lx) {
    super(lx, "");
    fpsKnob.setValue(60);
    context = new DwPixelFlow(RainbowStudio.pApplet);
    context.print();
    context.printGL();
    toyGraphics = RainbowStudio.pApplet.createGraphics(imageWidth, imageHeight, P2D);
    toy = new DwShadertoy(context, "data/audio.frag");
  }

  public void draw(double drawDeltaMs) {
    // Create Audio FFT texture
    GraphicMeter eq = lx.engine.audio.meter;
    byte[] fft = new byte[1024];
    // System.out.println("numBands: " + eq.numBands);
    // ShaderToy expects a 512x2 texture (arranged as 1024 continuous bytes).
    // The first 512 bytes are the FFT data.  The second 512 bytes are the audio
    // data that corresponds to that FFT data.  We currently don't have access to
    // the raw FFT or the raw Audio Buffer.  Need to get some changes from Mark.
    // For now, we are just packing
    // the array with repeating equalizer bands.  In our shader, we just scale
    // our normalized pixel coordinates so that the entire render buffer samples
    // from the first 16 bytes of the texture.
    for (int i = 0; i < 1024; i++) {
      int bandVal = (int)(eq.getBandf(i%16) * 255.0);
      fft[i++] = (byte)(bandVal);
    }
    ByteBuffer fftBuf = ByteBuffer.wrap(fft);
    DwGLTexture tex0 = new DwGLTexture();
    tex0.resize(context, GL2.GL_R8, 512, 2, GL2.GL_RED, GL2.GL_UNSIGNED_BYTE,
      GL2.GL_LINEAR, GL2.GL_MIRRORED_REPEAT, 1, 1, fftBuf);
    toy.set_iChannel(0, tex0);
    pg.background(0);
    toy.apply(toyGraphics);
    toyGraphics.loadPixels();
    toyGraphics.updatePixels();
    pg.image(toyGraphics, 0, 0);
    pg.loadPixels();
    tex0.release();
  }
}

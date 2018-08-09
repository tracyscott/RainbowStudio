package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.RainbowStudio.pApplet;
import static processing.core.PConstants.P2D;

import com.giantrainbow.PathUtils;
import com.google.gson.JsonObject;
import com.jogamp.opengl.GL2;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwShadertoy;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.CustomDeviceUI;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UITextBox;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * This copies shaders from the data/ area into a local shaders/ directory if they don't
 * exist, and then uses those for utilization and editing.
 */
@LXCategory(LXCategory.FORM)
public class ShaderToy extends PGPixelPerfect implements CustomDeviceUI {
  private static final Logger logger = Logger.getLogger(ShaderToy.class.getName());

  public final StringParameter shaderFileKnob = new StringParameter("frag", "sparkles");
  public final BooleanParameter audioKnob = new BooleanParameter("Audio", true);
  public final CompoundParameter knob1 =
      new CompoundParameter("K1", 0, 1)
          .setDescription("Mapped to iMouse.x");
  public final CompoundParameter knob2 =
      new CompoundParameter("K2", 0, 1)
          .setDescription("Mapped to iMouse.y");
  public final CompoundParameter knob3 =
      new CompoundParameter("K3", 0, 1)
          .setDescription("Mapped to iMouse.z");
  public final CompoundParameter knob4 =
      new CompoundParameter("K4", 0, 1)
          .setDescription("Mapped to iMouse.w");

  List<FileItem> fileItems = new ArrayList<FileItem>();
  UIItemList.ScrollList fileItemList;
  List<String> shaderFiles;

  DwPixelFlow context;
  DwShadertoy toy;
  DwGLTexture texNoise = new DwGLTexture();
  PGraphics toyGraphics;
  private static final int CONTROLS_MIN_WIDTH = 200;

  private static final String SHADER_DATA_DIR = "";
  private static final String LOCAL_SHADER_DIR = "shaders/";

  public ShaderToy(LX lx) {
    super(lx, "");
    fpsKnob.setValue(GLOBAL_FRAME_RATE);
    addParameter(audioKnob);
    addParameter(knob1);
    addParameter(knob2);
    addParameter(knob3);
    addParameter(knob4);
    addParameter(shaderFileKnob);
    toyGraphics = pApplet.createGraphics(pg.width, pg.height, P2D);
    loadShader(shaderFileKnob.getString());
    // context initialized in loadShader, print the GL hardware once when loading
    // the pattern.  left in for now while testing performance on different
    // graphics hardware.
    context.print();
    context.printGL();

    // For each shader, ensure there's a local copy in "shaders/"
    File shaderDir = new File(LOCAL_SHADER_DIR);
    if (shaderDir.exists()) {
      if (!shaderDir.isDirectory()) {
        logger.warning("Could not create \"" + LOCAL_SHADER_DIR + "\" directory");
        shaderDir = null;
      }
    } else {
      // Try to create the directory
      if (shaderDir.mkdir()) {
        logger.info("Created \"" + LOCAL_SHADER_DIR + "\" directory");
      } else {
        logger.warning("Could not create \"" + LOCAL_SHADER_DIR + "\" directory");
        shaderDir = null;
      }
    }
    shaderFiles = PathUtils.findDataFiles(SHADER_DATA_DIR, ".frag");
    Collections.sort(shaderFiles);
    for (String filename : shaderFiles) {
      // Copy all the shaders locally
      if (shaderDir != null) {
        try (InputStream in = pApplet.createInput(filename)) {
          File shaderFile = new File(shaderDir, new File(filename).getName());
          if (shaderFile.exists()) {
            logger.info("Not overwriting shader: from=data:" + filename + " to=" + shaderFile);
          } else {
            try {
              Files.copy(in, shaderFile.toPath());
              logger.info("Copied shader: from=data:" + filename + " to=" + shaderFile);
            } catch (IOException ex) {
              logger.log(Level.SEVERE,
                  "Error copying shader: from=data:" + filename + " to=" + shaderFile);
            }
          }
        } catch (IOException ex) {
          logger.log(Level.SEVERE, "Error accessing shader resource: " + filename, ex);
        }
      }

      // Use a name that's suitable for the knob
      int index = filename.lastIndexOf('/');
      if (index >= 0) {
        filename = filename.substring(index + 1);
      }
      index = filename.lastIndexOf('.');
      if (index >= 0) {
        filename = filename.substring(0, index);
      }
      fileItems.add(new FileItem(filename));
    }

    // TODO(tracy):  This is Voronoi-specific data.  ShaderToy shaders
    // that rely on inputs might need custom implemented patterns.
    // Some inputs are standard like Audio data
    // so that can be enabled with a toggle.  Actually, each Channel0..3
    // should have a dropdown to select the input as on shadertoy.com.

    // create noise texture.
    int wh = 256;
    byte[] bdata = new byte[wh * wh * 4];
    ByteBuffer bbuffer = ByteBuffer.wrap(bdata);
    for (int i = 0; i < bdata.length; ) {
      bdata[i++] = (byte) pApplet.random(0, 255);
      bdata[i++] = (byte) pApplet.random(0, 255);
      bdata[i++] = (byte) pApplet.random(0, 255);
      bdata[i++] = (byte) 255;
    }
    // Noise data texture passsed as a texture.
    texNoise.resize(context, GL2.GL_RGBA8, wh, wh, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, GL2.GL_LINEAR, GL2.GL_MIRRORED_REPEAT, 4, 1, bbuffer);
  }

  @Override
  public void load(LX lx, JsonObject obj) {
    super.load(lx, obj);
    loadShader(shaderFileKnob.getString());
  }

  protected void loadShader(String shaderFile) {
    if (toy != null) {
      // release existing shader texture
      toy.release();
      toy = null;
    }
    if (context != null) context.release();
    context = new DwPixelFlow(pApplet);
    // TODO(tracy): Handle file not found issue.

    File local = new File(LOCAL_SHADER_DIR + shaderFile + ".frag");
    if (local.isFile()) {
      toy = new DwShadertoy(context, local.getPath());
    }
  }

  public void draw(double drawDeltaMs) {
    GraphicMeter eq = lx.engine.audio.meter;
    byte[] fftAudioTex = new byte[1024];
    for (int i = 0; i < 256; i++) {
      int fftValue = (int) (256 * eq.fft.get(i));
      // Audio buffer is only 512 bytes, so fft is 256.  Let's
      // just add duplicate values.
      fftAudioTex[i] = (byte) fftValue;
      //fftAudioTex[i*2+1] = (byte) fftValue;
    }
    float[] audioSamples = eq.getSamples();
    for (int i = 512; i < 1024; i++) {
      int audioValue = (int) (256 * audioSamples[i-512]);
      fftAudioTex[i] = (byte) audioValue;
    }
    ByteBuffer audioTexBuf = ByteBuffer.wrap(fftAudioTex);
    DwGLTexture texAudio = new DwGLTexture();
    texAudio.resize(context, GL2.GL_R8, 512, 2, GL2.GL_RED, GL2.GL_UNSIGNED_BYTE,
        GL2.GL_LINEAR, GL2.GL_MIRRORED_REPEAT, 1, 1, audioTexBuf);
    toy.set_iChannel(0, texAudio);
    toy.set_iChannel(1, texNoise);
    pg.background(0);
    if (toy == null) {
      return;
    }
    toy.set_iMouse(knob1.getValuef(), knob2.getValuef(), knob3.getValuef(), knob4.getValuef());
    toy.apply(toyGraphics);
//    toyGraphics.loadPixels();
//    toyGraphics.updatePixels();
    pg.image(toyGraphics, 0, 0);
    texAudio.release();
  }

  protected InputStream getFile() {
    return pApplet.createInput(this.shaderFileKnob.getString() + ".frag");
  }

  //
  // Custom UI to allow for the selection of the shader file
  //
  @Override
    public void buildDeviceUI(UI ui, final UI2dContainer device) {
    device.setContentWidth(CONTROLS_MIN_WIDTH);
    device.setLayout(UI2dContainer.Layout.VERTICAL);
    device.setPadding(3, 3, 3, 3);

    UI2dContainer knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    new UIKnob(fpsKnob).addToContainer(knobsContainer);
    new UIKnob(knob1).addToContainer(knobsContainer);
    new UIKnob(knob2).addToContainer(knobsContainer);
    new UIKnob(knob3).addToContainer(knobsContainer);
    new UIKnob(knob4).addToContainer(knobsContainer);
    knobsContainer.addToContainer(device);

    UI2dContainer filenameEntry = new UI2dContainer(0, 0, device.getWidth(), 30);
    filenameEntry.setLayout(UI2dContainer.Layout.HORIZONTAL);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
      .setParameter(shaderFileKnob)
      .setTextAlignment(PConstants.LEFT)
      .addToContainer(filenameEntry);


    // Button for reloading shader.
    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
      @Override
        public void onToggle(boolean on) {
        if (on) {
          loadShader(shaderFileKnob.getString());
        }
      }
    }
    .setLabel("\u21BA").setMomentary(true).addToContainer(filenameEntry);
    filenameEntry.addToContainer(device);

    // Button for editing a file.
    new UIButton(0, 24, device.getContentWidth(), 16) {
      @Override
      public void onToggle(boolean on) {
        if (!on) {
          return;
        }
        File local = new File(LOCAL_SHADER_DIR + shaderFileKnob.getString() + ".frag");
        if (local.isFile()) {
          try {
            java.awt.Desktop.getDesktop().edit(local);
          } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error editing file: " + local, ex);
          }
        }
      }
    }
    .setLabel("Edit").setMomentary(true).addToContainer(device);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    fileItemList.setShowCheckboxes(false);
    fileItemList.setItems(fileItems);
    fileItemList.addToContainer(device);
  }

  public class FileItem extends FileItemBase {
    public FileItem(String filename) {
      super(filename);
    }
    public void onActivate() {
      shaderFileKnob.setValue(filename);
      loadShader(filename);
    }
  }
}

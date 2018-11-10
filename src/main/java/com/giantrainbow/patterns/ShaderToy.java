package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static processing.core.PConstants.P2D;

import com.giantrainbow.PathUtils;
import com.giantrainbow.RainbowStudio;
import com.google.gson.JsonObject;
import com.jogamp.opengl.GL2;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwShadertoy;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.*;
import heronarts.p3lx.ui.CustomDeviceUI;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * This copies shaders from the data/ area into a local shaders/ directory if they don't
 * exist, and then uses those for utilization and editing.
 */
@LXCategory(LXCategory.FORM)
public class ShaderToy extends PGPixelPerfect implements CustomDeviceUI {
  private static final Logger logger = Logger.getLogger(ShaderToy.class.getName());

  public final StringParameter shaderFileKnob = new StringParameter("frag", "sparkles");
  // textureNameKnob is loaded/saved. textureKnob is just an intermediate knob used for the
  // drop down texture selector.
  public final StringParameter textureNameKnob = new StringParameter("tex", "tunneltex.png");
  public DiscreteParameter textureKnob;
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

  List<String> textureFiles;

  DwPixelFlow context;
  DwShadertoy toy;
  DwGLTexture texNoise = new DwGLTexture();
  DwGLTexture texImage = null;
  PGraphics toyGraphics;
  PImage textureImage;

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
    addParameter(textureNameKnob);

    toyGraphics = RainbowStudio.pApplet.createGraphics(pg.width, pg.height, P2D);
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
    shaderFiles = PathUtils.findDataFiles(SHADER_DATA_DIR, ".frag"); //findShaderFiles(LOCAL_SHADER_DIR);
    Collections.sort(shaderFiles);
    for (String filename : shaderFiles) {
      // Copy all the shaders locally
      if (shaderDir != null) {
        try (InputStream in = RainbowStudio.pApplet.createInput(filename)) {
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

    textureFiles = PathUtils.findDataFiles(SHADER_DATA_DIR + "/textures", ".png");
    textureFiles.add(0, "NO TEXTURE");
    textureKnob = new DiscreteParameter("Texture", 0, 0, textureFiles.size());
    initTextureDropdown();

    // Create a random noise channel for ShaderToy shaders.
    // create noise texture.
    int wh = 256;
    byte[] bdata = new byte[wh * wh * 4];
    ByteBuffer bbuffer = ByteBuffer.wrap(bdata);
    for (int i = 0; i < bdata.length; ) {
      bdata[i++] = (byte) RainbowStudio.pApplet.random(0, 255);
      bdata[i++] = (byte) RainbowStudio.pApplet.random(0, 255);
      bdata[i++] = (byte) RainbowStudio.pApplet.random(0, 255);
      bdata[i++] = (byte) 255;
    }
    // Noise data texture passsed as a texture.
    texNoise.resize(context, GL2.GL_RGBA8, wh, wh, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, GL2.GL_LINEAR, GL2.GL_MIRRORED_REPEAT, 4, 1, bbuffer);

    logger.info("loading texture");
    loadTextureChannel();
  }

  /**
   * Recompute the selected drop down texture item.  We need to do this any time we
   * change the textureNameKnob value programmatically (such as when a project file is
   * loaded).
   */
  public void initTextureDropdown() {
    int textureKnobValue = 0;
    int k = 0;
    for (String textureName : textureFiles) {
      k++;
      if (textureName.equals("textures/" + textureNameKnob.getString())) {
        textureKnobValue = k;
      }
    }
    if (textureKnobValue > 0) {
      textureKnob.setValue(textureKnobValue);
    }
  }

  /**
   * Reloads the texture specified by textureNameKnob.  Textures are stored in data/textures/*.png
   */
  public void loadTextureChannel() {
    synchronized (toy) {
      if (texImage != null) texImage.release();
      texImage = new DwGLTexture();
      // Textures are resized to 256x256 for GL hardware.
      int wh = 256;
      textureImage = RainbowStudio.pApplet.loadImage("textures/" + textureNameKnob.getString());
      textureImage.resize(wh, wh);
      logger.info("ShaderToy image texture " + textureNameKnob.getString() + " size=" + textureImage.width + "x" + textureImage.height);
      textureImage.loadPixels();
      byte[] pixdata = new byte[wh * wh * 4];
      ByteBuffer pixBuffer = ByteBuffer.wrap(pixdata);
      for (int y = 0; y < wh; y++) {
        for (int x = 0; x < wh; x++) {
          int loc = x + y * wh;
          int bufferLoc = loc * 4;
          // The functions red(), green(), and blue() pull out the 3 color components from a pixel.
          pixdata[bufferLoc++] = LXColor.red(textureImage.pixels[loc]);
          pixdata[bufferLoc++] = LXColor.green(textureImage.pixels[loc]);
          pixdata[bufferLoc++] = LXColor.blue(textureImage.pixels[loc]);
          pixdata[bufferLoc++] = LXColor.alpha(textureImage.pixels[loc]);
        }
      }
      logger.info("Creating texture for ShaderToy.");
      texImage.resize(context, GL2.GL_RGBA8, wh, wh, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, GL2.GL_LINEAR, GL2.GL_MIRRORED_REPEAT, 4, 1, pixBuffer);
    }
  }

  @Override
  public void load(LX lx, JsonObject obj) {
    super.load(lx, obj);
    loadShader(shaderFileKnob.getString());
    initTextureDropdown();
    loadTextureChannel();
  }

  protected void loadShader(String shaderFile) {
    if (toy != null) {
      // release existing shader texture
      toy.release();
      toy = null;
    }
    if (context != null) context.release();
    context = new DwPixelFlow(RainbowStudio.pApplet);
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
    // Allow for dynamic texture reloading via the UI.
    synchronized (toy) {
      toy.set_iChannel(0, texAudio);
      toy.set_iChannel(2, texNoise);
      toy.set_iChannel(1, texImage);
    }
    pg.background(0);
    if (toy == null) {
      return;
    }
    toy.set_iMouse(knob1.getValuef(), knob2.getValuef(), knob3.getValuef(), knob4.getValuef());
    toy.apply(toyGraphics);
    toyGraphics.loadPixels();
    toyGraphics.updatePixels();
    pg.image(toyGraphics, 0, 0);
    texAudio.release();
  }

  protected InputStream getFile() {
    return RainbowStudio.pApplet.createInput(this.shaderFileKnob.getString() + ".frag");
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

    new UIDropMenu(0f, 0f, device.getWidth() - 30f, 20f, textureKnob) {
      public void onParameterChanged(LXParameter p) {
        DiscreteParameter dp = (DiscreteParameter)p;
        String textureFilename = textureFiles.get(dp.getValuei());
        File f = new File(textureFilename);
        logger.info("Selected texture: " + textureFilename);
        textureNameKnob.setValue(f.getName());
        loadTextureChannel();
      }
    }.setOptions(textureFiles.toArray(new String[textureFiles.size()])).setDirection(UIDropMenu.Direction.UP).addToContainer(device);

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

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 50);
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

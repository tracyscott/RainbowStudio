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
import processing.opengl.PGraphicsOpenGL;

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
  public final StringParameter textureNameKnob = new StringParameter("tex", "NO TEXTURE");
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
  public final CompoundParameter U1x =
      new CompoundParameter("U1x", 0, 1)
          .setDescription("Mapped to U1.x");
  public final CompoundParameter U1y =
      new CompoundParameter("U1y", 0, 1)
          .setDescription("Mapped to U1.y");
  public final CompoundParameter U1z =
      new CompoundParameter("U1z", 0, 1)
          .setDescription("Mapped to U1.z");
  public final CompoundParameter U1w =
      new CompoundParameter("U1w", 0, 1)
          .setDescription("Mapped to U1.w");
  public final CompoundParameter U2x =
      new CompoundParameter("U2x", 0, 1)
          .setDescription("Mapped to U2.x");
  public final CompoundParameter U2y =
      new CompoundParameter("U2y", 0, 1)
          .setDescription("Mapped to U2.y");
  public final CompoundParameter U2z =
      new CompoundParameter("U2z", 0, 1)
          .setDescription("Mapped to U2.z");
  public final CompoundParameter U2w =
      new CompoundParameter("U2w", 0, 1)
          .setDescription("Mapped to U2.w");


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
  float[] u1, u2;
  protected UI2dContainer bottomHalf, rightPanel, leftPanel;

  private static final int CONTROLS_MIN_WIDTH = 320;

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
    addParameter(U1x);
    addParameter(U1y);
    addParameter(U1z);
    addParameter(U1w);
    addParameter(U2x);
    addParameter(U2y);
    addParameter(U2z);
    addParameter(U2w);
    addParameter(shaderFileKnob);
    addParameter(textureNameKnob);

    u1 = new float[4];
    u2 = new float[4];
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
      if (!"NO TEXTURE".equals(textureNameKnob.getString())) {
        texImage = new DwGLTexture();
        // Textures are resized to 256x256 for GL hardware by default.
        int twidth = 256;
        int theight = 256;
        textureImage = RainbowStudio.pApplet.loadImage("textures/" + textureNameKnob.getString());

        // The special texture map font uses a 1024x1024 texture, so go ahead and use it.  For everything else
        // force the image to be 256x256.
        if ("atext.png".equals(textureNameKnob.getString())) {
          logger.info("a text texture dim: " + twidth + "x" + theight);
          twidth = textureImage.width;
          theight = textureImage.height;
        } else {
          logger.info("texture dim: " + twidth + "x" + theight);
          textureImage.resize(twidth, theight);
        }
        logger.info("ShaderToy image texture " + textureNameKnob.getString() + " size=" + textureImage.width + "x" + textureImage.height);
        textureImage.loadPixels();

        byte[] pixdata = new byte[twidth * theight * 4];
        ByteBuffer pixBuffer = ByteBuffer.wrap(pixdata);
        for (int y = 0; y < theight; y++) {
          for (int x = 0; x < theight; x++) {
            int loc = x + y * twidth;
            int bufferLoc = loc * 4;
            // 0,0 is on the bottom left in texture and top left in image. swap the y coords.
            loc = x + twidth * ((theight - 1) - y);
            // The functions red(), green(), and blue() pull out the 3 color components from a pixel.
            pixdata[bufferLoc++] = LXColor.red(textureImage.pixels[loc]);
            pixdata[bufferLoc++] = LXColor.green(textureImage.pixels[loc]);
            pixdata[bufferLoc++] = LXColor.blue(textureImage.pixels[loc]);
            pixdata[bufferLoc] = LXColor.alpha(textureImage.pixels[loc]);
          }
        }
        logger.info("Creating texture for ShaderToy.");
        texImage.resize(context, GL2.GL_RGBA8, twidth, theight, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, GL2.GL_LINEAR, GL2.GL_MIRRORED_REPEAT, 4, 1, pixBuffer);
      } else {
        texImage = null;
      }
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
    preDraw(drawDeltaMs);
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
      if (texImage != null)
        toy.set_iChannel(1, texImage);
    }
    pg.background(0, 0);
    if (toy == null) {
      return;
    }
    toy.set_iMouse(knob1.getValuef(), knob2.getValuef(), knob3.getValuef(), knob4.getValuef());
    u1[0] = U1x.getValuef();
    u1[1] = U1y.getValuef();
    u1[2] = U1z.getValuef();
    u1[3] = U1w.getValuef();

    u2[0] = U2x.getValuef();
    u2[1] = U2y.getValuef();
    u2[2] = U2z.getValuef();
    u2[3] = U2w.getValuef();
    //toy.apply(toyGraphics);
    shaderApply(context, toy, (PGraphicsOpenGL) toyGraphics, u1, u2);
    toyGraphics.loadPixels();
    toyGraphics.updatePixels();
    pg.image(toyGraphics, 0, 0);
    texAudio.release();
    postDraw(drawDeltaMs);
  }

  /**
   * Pre-draw hook for child classes.
   *
   * @param drawDeltaMs
   */
  protected void preDraw(double drawDeltaMs) {

  }

  /**
   * Post-draw hook for child classes.
   *
   * @param drawDeltaMs
   */
  protected void postDraw(double drawDeltaMs) {

  }

  static public void shaderApply(DwPixelFlow context, DwShadertoy toy, PGraphicsOpenGL pg_dst, float[] u1, float[] u2) {
    toy.resize(pg_dst.width, pg_dst.height);
    pg_dst.getTexture();
    toy.context.begin();
    toy.context.beginDraw(pg_dst);
    shaderRender(context, toy, pg_dst.width, pg_dst.height, u1, u2);
    toy.context.endDraw();
    toy.context.end();
  }

  static public void shaderRender(DwPixelFlow context, DwShadertoy toy, int w, int h, float[] u1, float[] u2) {

    toy.set_iResolution(w, h, 1f);
    toy.set_iFrameRate(context.papplet.frameRate);
    toy.set_iTimeDelta(1f/context.papplet.frameRate);
    toy.set_iTime();
    toy.set_iDate();

    toy.shader.begin();
    toy.shader.uniform3fv    ("iResolution"       , 1, toy.iResolution       );
    toy.shader.uniform1f     ("iTime"             , toy.iTime                );
    toy.shader.uniform1f     ("iTimeDelta"        , toy.iTimeDelta           );
    toy.shader.uniform1i     ("iFrame"            , toy.iFrame               );
    toy.shader.uniform1f     ("iFrameRate"        , toy.iFrameRate           );
    toy.shader.uniform4fv    ("iMouse"            , 1, toy.iMouse            );
    toy.shader.uniform4fv    ("iDate"             , 1, toy.iDate             );
    toy.shader.uniform1f     ("iSampleRate"       , toy.iSampleRate          );
    toy.shader.uniform1fv    ("iChannelTime"      , 4, toy.iChannelTime      );
    toy.shader.uniform3fv    ("iChannelResolution", 4, toy.iChannelResolution);
    toy.shader.uniformTexture("iChannel0"         , toy.iChannel[0]          );
    toy.shader.uniformTexture("iChannel1"         , toy.iChannel[1]          );
    toy.shader.uniformTexture("iChannel2"         , toy.iChannel[2]          );
    toy.shader.uniformTexture("iChannel3"         , toy.iChannel[3]          );
    toy.shader.uniform4fv    ("U1", 1, u1);
    toy.shader.uniform4fv    ("U2", 1, u2);
    toy.shader.drawFullScreenQuad();
    toy.shader.end();

    toy.set_iFrame(toy.iFrame + 1);
  }

  protected InputStream getFile() {
    return RainbowStudio.pApplet.createInput(this.shaderFileKnob.getString() + ".frag");
  }

  /**
   * Hook for subclasses to add additional UI.
   */
  protected void addUI() {

  }

  //
  // Custom UI to allow for the selection of the shader file
  //
  @Override
    public void buildDeviceUI(UI ui, final UI2dContainer device) {
    device.setContentWidth(CONTROLS_MIN_WIDTH);
    device.setLayout(UI2dContainer.Layout.VERTICAL);
    device.setPadding(0, 0, 0, 0);

    UI2dContainer knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    int knobWidth = 35;
    new UIKnob(fpsKnob).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(knob1).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(knob2).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(knob3).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(knob4).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(U1x).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(U1y).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(U1z).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(U1w).setWidth(knobWidth).addToContainer(knobsContainer);
    knobsContainer.addToContainer(device);

    bottomHalf = new UI2dContainer(0, 45, device.getWidth() - 30, device.getHeight() - 45);
    bottomHalf.setLayout(UI2dContainer.Layout.HORIZONTAL);
    bottomHalf.addToContainer(device);
    bottomHalf.setPadding(0);

    leftPanel = new UI2dContainer(0, 0, device.getWidth()/2 - 15, device.getHeight() - 45);
    leftPanel.setLayout(UI2dContainer.Layout.VERTICAL);
    leftPanel.addToContainer(bottomHalf);
    leftPanel.setPadding(0);

    rightPanel = new UI2dContainer(0, 0, device.getWidth()/2 - 15, device.getHeight() - 45);
    rightPanel.setLayout(UI2dContainer.Layout.VERTICAL);
    rightPanel.addToContainer(bottomHalf);
    rightPanel.setPadding(0);

    UI2dContainer u2KnobsContainer = new UI2dContainer(0, 0, rightPanel.getWidth() - 10, 45);
    u2KnobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    u2KnobsContainer.addToContainer(rightPanel);
    u2KnobsContainer.setPadding(0);
    new UIKnob(U2x).setWidth(knobWidth).addToContainer(u2KnobsContainer);
    new UIKnob(U2y).setWidth(knobWidth).addToContainer(u2KnobsContainer);
    new UIKnob(U2z).setWidth(knobWidth).addToContainer(u2KnobsContainer);
    new UIKnob(U2w).setWidth(knobWidth).addToContainer(u2KnobsContainer);


    new UIDropMenu(0f, 0f, leftPanel.getWidth(), 20f, textureKnob) {
      public void onParameterChanged(LXParameter p) {
        DiscreteParameter dp = (DiscreteParameter)p;
        String textureFilename = textureFiles.get(dp.getValuei());
        File f = new File(textureFilename);
        logger.info("Selected texture: " + textureFilename);
        textureNameKnob.setValue(f.getName());
        loadTextureChannel();
      }
    }.setOptions(textureFiles.toArray(new String[textureFiles.size()])).setDirection(UIDropMenu.Direction.UP).addToContainer(leftPanel);

    UI2dContainer filenameEntry = new UI2dContainer(0, 0, leftPanel.getWidth(), 30);
    filenameEntry.setLayout(UI2dContainer.Layout.HORIZONTAL);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, leftPanel.getWidth(), 80);
    new UITextBox(0, 0, leftPanel.getWidth() - 22, 20)
      .setParameter(shaderFileKnob)
      .setTextAlignment(PConstants.LEFT)
      .addToContainer(filenameEntry);

    // Button for reloading shader.
    new UIButton(leftPanel.getWidth() - 20, 0, 22, 20) {
      @Override
        public void onToggle(boolean on) {
        if (on) {
          loadShader(shaderFileKnob.getString());
        }
      }
    }
    .setLabel("\u21BA").setMomentary(true).addToContainer(filenameEntry);
    filenameEntry.addToContainer(leftPanel);


    // Button for editing a file.
    new UIButton(0, 24, leftPanel.getWidth(), 16) {
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
    .setLabel("Edit").setMomentary(true).addToContainer(leftPanel);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, leftPanel.getWidth(), 50);
    fileItemList.setShowCheckboxes(false);
    fileItemList.setItems(fileItems);
    fileItemList.addToContainer(leftPanel);

    // Hook for subclasses.
    addUI();
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

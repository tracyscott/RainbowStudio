package com.giantrainbow.patterns;

import com.giantrainbow.PathUtils;
import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.*;
import heronarts.p3lx.ui.CustomDeviceUI;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UISwitch;
import heronarts.p3lx.ui.component.UITextBox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

abstract class RainbowImageBase extends LXPattern implements CustomDeviceUI {
  private static final Logger logger = Logger.getLogger(RainbowImageBase.class.getName());

  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", 1.0, 10.0)
          .setDescription("Controls the frames per second.");
  public final BooleanParameter antialiasKnob =
      new BooleanParameter("antialias", true);
  public final StringParameter imgKnob =
      new StringParameter("img", "")
          .setDescription("Texture image for rainbow.");
  public final BooleanParameter tileKnob = new BooleanParameter("tile", false);

  protected List<FileItem> fileItems = new ArrayList<FileItem>();
  protected UIItemList.ScrollList fileItemList;
  protected List<String> imgFiles;
  private static final int CONTROLS_MIN_WIDTH = 160;

  private static final List<String> IMG_EXTS = Arrays.asList(".gif", ".png", ".jpg");

  protected PImage image;
  protected PImage tileImage;
  protected int imageWidth = 0;
  protected int imageHeight = 0;
  protected String filesDir;  // Must end in a '/'
  protected boolean includeAntialias;
  protected int paddingX;
  protected int numTiles;
  protected PGraphics pg;

  public RainbowImageBase(LX lx, int imageWidth, int imageHeight,
                          String filesDir, String defaultFile,
                          boolean includeAntialias) {
    super(lx);
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    if (!filesDir.endsWith("/")) {
      filesDir = filesDir + "/";
    }
    this.filesDir = filesDir;
    this.includeAntialias = includeAntialias;
    reloadFileList();
    pg = RainbowStudio.pApplet.createGraphics(imageWidth, imageHeight);

    addParameter(fpsKnob);
    if (includeAntialias) {
      addParameter(antialiasKnob);
    }
    addParameter(imgKnob);
    imgKnob.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        StringParameter iKnob = (StringParameter) parameter;
        loadImg(iKnob.getString());
      }
    });
    imgKnob.setValue(defaultFile);
    loadImg(imgKnob.getString());

    addParameter(tileKnob);

  }

  private void loadImg(String imgname) {
    logger.info("Loading image: " + imgname);
    tileImage = RainbowStudio.pApplet.loadImage(filesDir + imgname);
    if (!tileKnob.getValueb()) {
      tileImage.resize(imageWidth, imageHeight);
      image = tileImage;
    } else {
      // Tile the image to fill the space horizontally.  Scale the image vertically
      // to fit.
      float yScale = imageHeight / tileImage.height;
      tileImage.resize((int)(tileImage.width * yScale), imageHeight);
      tileImage.loadPixels();
      logger.info("tileImage.width=" + tileImage.width + " tileImage.height=" + tileImage.height);
      numTiles = imageWidth  / tileImage.width;
      int remainderPixelsX = imageWidth - (numTiles * tileImage.width);
      // No vertical padding right now int paddingY = imageHeight - image.height;
      paddingX = remainderPixelsX / (numTiles+1);
      logger.info("Tiling image: " + imgname + " numTiles=" + numTiles + " paddingX=" + paddingX);
      pg.beginDraw();
      pg.background(0);

      for (int i = 0; i < numTiles; i++) {
        pg.image(tileImage, i * tileImage.width + (i +1) * paddingX, 0);
      }

      pg.endDraw();
      pg.updatePixels();
      pg.loadPixels();
      image = pg;
    }
  }

  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    /* Leaving FPS and frame logic for now incase we want to do some Ken Burns
    currentFrame += (deltaMs/1000.0) * fps;
    if (currentFrame >= images.length) {
      currentFrame -= images.length;
    }
    */
    try {
      renderToPoints();
    } catch (ArrayIndexOutOfBoundsException ex) {
      // handle race condition while reloading animated gif.
    }
  }

  protected abstract void renderToPoints();

  protected void reloadFileList() {
    imgFiles = PathUtils.findDataFiles(filesDir, IMG_EXTS);
    fileItems.clear();
    for (String filename : imgFiles) {
      // Use a name that's suitable for the knob
      int index = filename.lastIndexOf('/');
      if (index >= 0) {
        filename = filename.substring(index + 1);
      }
      fileItems.add(new FileItem(filename));
    }
    if (fileItemList != null) {
      fileItemList.setItems(fileItems);
    }
  }

  //
  // Custom UI to allow for the selection of the shader file
  //
  @Override
  public void buildDeviceUI(UI ui, final UI2dContainer device) {
    device.setContentWidth(CONTROLS_MIN_WIDTH);
    device.setLayout(UI2dContainer.Layout.VERTICAL);
    device.setPadding(3, 3, 3, 3);

    UI2dContainer knobsContainer = new UI2dContainer(0, 0, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(3, 3, 3, 3);
    new UIKnob(fpsKnob).addToContainer(knobsContainer);
    if (includeAntialias) {
      UISwitch antialiasButton = new UISwitch(0, 0);
      antialiasButton.setParameter(antialiasKnob);
      antialiasButton.setMomentary(false);
      antialiasButton.addToContainer(knobsContainer);
    }

    // We need to reload the image if the tile button is selected.  For tiled images, we build
    // an intermediate PGraphics object and tile the selected image into that and then use it
    // as our base PImage 'image'.
    new UIButton() {
        @Override
      public void onToggle(boolean on) {
          // Need to reload the image
          loadImg(imgKnob.getString());
        }
    }.setParameter(tileKnob).setLabel("tile").setTextOffset(0, 20)
        .setWidth(28).setHeight(25).addToContainer(knobsContainer);

    new UIButton() {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          reloadFileList();
        }
      }
    }.setLabel("rescan dir")
        .setMomentary(true)
        .setWidth(60)
        .setHeight(25)
        .addToContainer(knobsContainer);

    knobsContainer.addToContainer(device);

    UI2dContainer filenameEntry = new UI2dContainer(0, 0, device.getWidth(), 30);
    filenameEntry.setLayout(UI2dContainer.Layout.HORIZONTAL);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
        .setParameter(imgKnob)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(filenameEntry);


    // Button for reloading image file list.
    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          loadImg(imgKnob.getString());
        }
      }
    }.setLabel("\u21BA")
        .setMomentary(true)
        .addToContainer(filenameEntry);
    filenameEntry.addToContainer(device);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    fileItemList.setShowCheckboxes(false);
    fileItemList.setItems(fileItems);
    fileItemList.addToContainer(device);
  }

  public class FileItem extends FileItemBase {
    FileItem(String filename) {
      super(filename);
    }
    public void onActivate() {
      imgKnob.setValue(filename);
      loadImg(filename);
    }
  }
}

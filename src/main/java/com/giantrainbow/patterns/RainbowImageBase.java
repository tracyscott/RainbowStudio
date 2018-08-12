package com.giantrainbow.patterns;

import com.giantrainbow.PathUtils;
import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.StringParameter;
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
import processing.core.PConstants;
import processing.core.PImage;

abstract class RainbowImageBase extends LXPattern implements CustomDeviceUI {
  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", 1.0, 10.0)
          .setDescription("Controls the frames per second.");
  public final BooleanParameter antialiasKnob =
      new BooleanParameter("antialias", true);
  public final StringParameter imgKnob =
      new StringParameter("img", "")
          .setDescription("Texture image for rainbow.");

  protected List<FileItem> fileItems = new ArrayList<FileItem>();
  protected UIItemList.ScrollList fileItemList;
  protected List<String> imgFiles;
  private static final int CONTROLS_MIN_WIDTH = 160;

  private static final List<String> IMG_EXTS = Arrays.asList(".gif", ".png", ".jpg");

  protected PImage image;
  protected int imageWidth = 0;
  protected int imageHeight = 0;
  protected String filesDir;  // Must end in a '/'
  protected boolean includeAntialias;

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

    addParameter(fpsKnob);
    if (includeAntialias) {
      addParameter(antialiasKnob);
    }
    addParameter(imgKnob);
    imgKnob.setValue(defaultFile);
    loadImg(imgKnob.getString());
  }

  private void loadImg(String imgname) {
    image = RainbowStudio.pApplet.loadImage(filesDir + imgname);
    image.resize(imageWidth, imageHeight);
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

    UI2dContainer knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(3, 3, 3, 3);
    new UIKnob(fpsKnob).addToContainer(knobsContainer);
    if (includeAntialias) {
      UISwitch antialiasButton = new UISwitch(0, 0);
      antialiasButton.setParameter(antialiasKnob);
      antialiasButton.setMomentary(false);
      antialiasButton.addToContainer(knobsContainer);
    }
    new UIButton(CONTROLS_MIN_WIDTH, 10, 60, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          reloadFileList();
        }
      }
    }.setLabel("rescan dir")
        .setMomentary(true)
        .addToContainer(knobsContainer);

    knobsContainer.addToContainer(device);

    UI2dContainer filenameEntry = new UI2dContainer(0, 0, device.getWidth(), 30);
    filenameEntry.setLayout(UI2dContainer.Layout.HORIZONTAL);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
        .setParameter(imgKnob)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(filenameEntry);


    // Button for reloading shader.
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

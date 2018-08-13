package com.giantrainbow.patterns;

import com.giantrainbow.PathUtils;
import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
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
import java.util.ArrayList;
import java.util.List;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * A pattern which loads a (possibly animated) GIF and draws it along the arc of the rainbow with
 * configurable speed and direction.
 */
@LXCategory(LXCategory.FORM)
public class AnimatedSpritePP extends PGPixelPerfect implements CustomDeviceUI {

  private static final int CONTROLS_MIN_WIDTH = 120;
  private static final String SPRITE_DIR = "spritepp/";

  public final StringParameter spriteFileKnob = new StringParameter("sprite", "smallcat");
  public final CompoundParameter xSpeed =
      new CompoundParameter("XSpd", 1, 20)
          .setDescription("X speed in pixels per frame");
  public final BooleanParameter clockwise = new BooleanParameter("clockwise", false);

  private List<FileItem> fileItems = new ArrayList<>();
  private UIItemList.ScrollList fileItemList;
  private List<String> spriteFiles;
  private PImage[] images;
  private int currentPos;

  public AnimatedSpritePP(LX lx) {
    super(lx, null);
    xSpeed.setValue(5);
    loadSprite(spriteFileKnob.getString());
    spriteFiles = PathUtils.findDataFiles(SPRITE_DIR, ".gif");
    for (String filename : spriteFiles) {
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
  }

  public void draw(double deltaMs) {
    pg.background(0);
    try {
      PImage frameImg = images[((int)currentFrame)%images.length];
      boolean offScreen = currentPos < 0 - frameImg.width || currentPos > frameImg.width + pg.width;
      if (offScreen) {
        currentPos = clockwise.getValueb()
            ? 0 - frameImg.width + 1
            : pg.width + frameImg.width + 1;
      }
      pg.image(frameImg, currentPos, 0);
      currentPos += (xSpeed.getValue() * (clockwise.getValueb() ? 1 : -1));
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      // handle race condition when reloading images.
    }
  }

  /**
   * Calls {@link PathUtils#loadSprite} but also keeps track of the current position.
   * This prepends {@link #SPRITE_DIR} and appends ".gif".
   *
   * @param path the sprite's name, not including parent paths or the ".gif" suffix
   */
  private void loadSprite(String path) {
    images = PathUtils.loadSprite(RainbowStudio.pApplet, SPRITE_DIR + path + ".gif");

    if (images.length > 0) {
      // Start off the image off-screen
      currentPos = 0 - images[0].width + 1;
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
    new UIKnob(xSpeed).addToContainer(knobsContainer);
    new UIKnob(fpsKnob).addToContainer(knobsContainer);
    new UIButton()
        .setParameter(clockwise)
        .setLabel("clock\nwise")
        .setTextOffset(0,12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);
    knobsContainer.addToContainer(device);

    UI2dContainer filenameEntry = new UI2dContainer(0, 0, device.getWidth(), 30);
    filenameEntry.setLayout(UI2dContainer.Layout.HORIZONTAL);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
        .setParameter(spriteFileKnob)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(filenameEntry);


    // Button for reloading shader.
    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          loadSprite(spriteFileKnob.getString());
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
      spriteFileKnob.setValue(filename);
      loadSprite(filename);
    }
  }
}

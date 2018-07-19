abstract public class RainbowImageBase extends LXPattern implements CustomDeviceUI {
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 10.0)
    .setDescription("Controls the frames per second.");
  public final BooleanParameter antialiasKnob =
    new BooleanParameter("antialias", true);  
  public final StringParameter imgKnob = new StringParameter("img", "")
    .setDescription("Texture image for rainbow.");

  protected List<FileItem> fileItems = new ArrayList<FileItem>();
  protected UIItemList.ScrollList fileItemList;
  protected List<String> imgFiles;
  private static final int CONTROLS_MIN_WIDTH = 160;

  protected PImage image;
  protected int imageWidth = 0;
  protected int imageHeight = 0;
  protected String filesDir;
  protected boolean includeAntialias;
  
  public RainbowImageBase(LX lx, int imageWidth, int imageHeight, String filesDir, String defaultFile,
    boolean includeAntialias) {
    super(lx);
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
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

  protected void loadImg(String imgname) {
    String filename = dataPath(filesDir + imgname);
    image = loadImage(filename);
    image.resize(imageWidth, imageHeight);
    image.loadPixels();
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

  protected File getFile() {
    return new File(dataPath(filesDir + this.imgKnob.getString()));
  }

  protected void reloadFileList() {
    imgFiles = getImgFiles();
    fileItems.clear();
    for (String filename : imgFiles) {
      fileItems.add(new FileItem(filename));
    }
    if (fileItemList != null)
      fileItemList.setItems(fileItems);
  }
  
  protected List<String> getImgFiles() {
    List<String> results = new ArrayList<String>();
    String[] imgExtensions = { ".gif", ".png", ".jpg"};
    File[] files = new File(dataPath(filesDir)).listFiles();
    //If this pathname does not denote a directory, then listFiles() returns null.
    for (File file : files) {
      if (file.isFile()) {
        for (int i = 0; i < imgExtensions.length; i++) {
          if (file.getName().endsWith(imgExtensions[i])) {
            results.add(file.getName());
          }
        }
      }
    }
    return results;
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
    }
    .setLabel("rescan dir").setMomentary(true).addToContainer(knobsContainer);

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
    }
    .setLabel("\u21BA").setMomentary(true).addToContainer(filenameEntry);
    filenameEntry.addToContainer(device);

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
      imgKnob.setValue(filename);
      loadImg(filename);
    }
  }
}

@LXCategory(LXCategory.FORM)
public class RainbowImage extends RainbowImageBase {
  public RainbowImage(LX lx) {
    super(lx, ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot * 2.0),
          ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot), 
      "./img/",
      "oregontex.jpg",
      true);
  }
  
  protected void renderToPoints() {
    RenderImageUtil.imageToPointsSemiCircle(lx, colors, image, antialiasKnob.isOn());    
  }
}

@LXCategory(LXCategory.FORM)
public class RainbowImagePP extends RainbowImageBase {
  public RainbowImagePP(LX lx) {
    super(lx, ((RainbowBaseModel)lx.model).pointsWide, ((RainbowBaseModel)lx.model).pointsHigh,
      "./imgpp/",
      "oregon.jpg",
      false);
  }
  
  protected void renderToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(lx, colors, image);    
  }
}
    

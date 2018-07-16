@LXCategory(LXCategory.FORM)
public class RainbowImagePP extends LXPattern implements CustomDeviceUI {
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 10.0)
    .setDescription("Controls the frames per second.");
  public final StringParameter imgKnob = new StringParameter("img", "oregon.jpg")
    .setDescription("420x30 Pixel perfect image.");

  List<FileItem> fileItems = new ArrayList<FileItem>();
  UIItemList.ScrollList fileItemList;
  List<String> imgFiles;
  private static final int CONTROLS_MIN_WIDTH = 120;

  private PImage image;
  private double currentFrame = 0.0;
  private int imageWidth = 0;
  private int imageHeight = 0;

  public RainbowImagePP(LX lx) {
    super(lx);
    imageWidth = ((RainbowBaseModel)lx.model).pointsWide;
    imageHeight = ((RainbowBaseModel)lx.model).pointsHigh;
    addParameter(fpsKnob);
    loadImg(imgKnob.getString());
    imgFiles = getImgFiles();
    for (String filename : imgFiles) {
      fileItems.add(new FileItem(filename));
    }
  }

  protected void loadImg(String imgname) {
    String filename = dataPath("./imgpp/" + imgname);
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
      RenderImageUtil.imageToPointsPixelPerfect(lx, colors, image);
    } catch (ArrayIndexOutOfBoundsException ex) {
      // handle race condition while reloading animated gif.
    }
  }

  protected File getFile() {
    return new File(dataPath("./imgpp/" + this.imgKnob.getString()));
  }

  protected List<String> getImgFiles() {
    List<String> results = new ArrayList<String>();
    String[] imgExtensions = { ".gif", ".png", ".jpg"};
    File[] files = new File(dataPath("./imgpp/")).listFiles();
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
public class RainbowImage extends LXPattern implements CustomDeviceUI {
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 10.0)
    .setDescription("Controls the frames per second.");
  public final BooleanParameter antialiasKnob =
    new BooleanParameter("antialias", true);  
  public final StringParameter imgKnob = new StringParameter("img", "oregontex.jpg")
    .setDescription("Texture image for rainbow.");

  List<FileItem> fileItems = new ArrayList<FileItem>();
  UIItemList.ScrollList fileItemList;
  List<String> imgFiles;
  private static final int CONTROLS_MIN_WIDTH = 120;

  private PImage image;
  private double currentFrame = 0.0;
  private int imageWidth = 0;
  private int imageHeight = 0;

  public RainbowImage(LX lx) {
    super(lx);
    float radiusInWorldPixels = RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot;
    imageWidth = ceil(radiusInWorldPixels * 2.0);
    imageHeight = ceil(radiusInWorldPixels);
    addParameter(fpsKnob);
    addParameter(antialiasKnob);
    addParameter(imgKnob);
    loadImg(imgKnob.getString());
    imgFiles = getImgFiles();
    for (String filename : imgFiles) {
      fileItems.add(new FileItem(filename));
    }
  }

  protected void loadImg(String imgname) {
    String filename = dataPath("./img/" + imgname);
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
      RenderImageUtil.imageToPointsSemiCircle(lx, colors, image, antialiasKnob.isOn());
    } catch (ArrayIndexOutOfBoundsException ex) {
      // handle race condition while reloading animated gif.
    }
  }

  protected File getFile() {
    return new File(dataPath("./imgpp/" + this.imgKnob.getString()));
  }

  protected List<String> getImgFiles() {
    List<String> results = new ArrayList<String>();
    String[] imgExtensions = { ".gif", ".png", ".jpg"};
    File[] files = new File(dataPath("./img/")).listFiles();
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
    UISwitch antialiasButton = new UISwitch(0, 0);
    antialiasButton.setParameter(antialiasKnob);
    antialiasButton.setMomentary(false);
    antialiasButton.addToContainer(knobsContainer);    
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

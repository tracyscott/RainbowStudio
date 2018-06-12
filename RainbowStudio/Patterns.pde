import gifAnimation.*;

@LXCategory(LXCategory.FORM)
public class Tutorial extends LXPattern {

  // This is a parameter, it has a label, an intial value and a range.  We use
  // this to allow the UI to adjust the height limits of the oscillating line.
  public final CompoundParameter yPos =
    new CompoundParameter("Pos", model.cy, model.yMin, model.yMax)
    .setDescription("Controls where the line is");

  public final CompoundParameter widthModulation =
    new CompoundParameter("Mod", 0, 8)
    .setDescription("Controls the amount of modulation of the width of the line");

  // This is a modulator, it changes values over time automatically based on a sine function.
  // It is the position of the gray-scale line pattern.  The model is currently created in 
  // units of feet.  So this goes from -12 to 24 feet.  But we can use the yPos knob above
  // to provide some offset so in the UI we can adjust it to go from say 12 feet to 48 feet.
  public final SinLFO basicMotion = new SinLFO(
    -12, // This is a lower bound
    24, // This is an upper bound
    7000     // This is 3 seconds, 3000 milliseconds
    );

  // The 'width' knob in the interface determines the maximum value of this
  // sine-based LFO.  The 'width' of the line will vary over time based on
  // this sine function.  Note that we can chain parameters together
  // like Max MSP/Pure Data/FM synthesis.  Here we are tying the maximum
  // value of the LFO to the knob in the interface.
  public final SinLFO sizeMotion = new SinLFO(
    0, 
    widthModulation, // <- check it out, a parameter can be an argument to an LFO!
    13000
    );

  public Tutorial(LX lx) {
    super(lx);
    addParameter(yPos);
    addParameter(widthModulation);
    startModulator(basicMotion);
    startModulator(sizeMotion);
  }

  public void run(double deltaMs) {
    // The position of the line is a sum of the base position plus the motion
    double position = this.yPos.getValue() + this.basicMotion.getValue();
    double lineWidth = 2 + sizeMotion.getValue();


    for (LXPoint p : model.points) {
      // We can get the position of this point via p.x, p.y, p.z

      // Compute a brightness that dims as we move away from the line 
      double brightness = 100 - (100/lineWidth) * Math.abs(p.y - position);
      if (brightness > 0) {
        colors[p.index] = LXColor.gray(brightness);
        // Alternatively, if we wanted to do our own color scheme, we
        // could do a manual color computation:
        //   colors[p.index] = LX.hsb(hue[0-360], saturation[0-100], brightness[0-100])
        //   colors[p.index] = LX.rgb(red, green blue)
        //
        // Note that we do *NOT* use Processing's color() function. That
        // function employs global state and is not thread safe!
        
      } else {
        colors[p.index] = 0;
      }
    }
  }
}

@LXCategory(LXCategory.COLOR)
public class Rainbow extends LXPattern {

  public Rainbow(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    int numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    int pointNumber = 0;
    for (LXPoint p : model.points) {
      int rowNumber = pointNumber / numPixelsPerRow; // Ranges 0-29
      float hue = map(rowNumber, 0, ((RainbowBaseModel)lx.model).pointsHigh - 1, 0, 360);
      // We can get the position of this point via p.x, p.y, p.z
      colors[p.index] = LX.hsb(hue, 100, 100);
      ++pointNumber;
    }
  }
}

static public class ImageUtil {
  
  /*
   * Compute a new RGB color based on a given weight.  Each
   * RGB component is multiplied by the weight (range 0.0-1.0)
   * and a new 32-bit color is returned.  Currently forces
   * alpha to 0xFF.
   */
  static public int getWeightedColor(int clr, float weight) {
    int red = LXColor.red(clr) & 0xFF; // & 0xFF to get byte value unsigned.
    int green = LXColor.green(clr) & 0xFF;
    int blue = LXColor.blue(clr) & 0xFF;
  
    int weightedRed = ((int)((float)red * weight))<< 16;
    int weightedGreen = ((int)((float)green * weight))<<8;
    int weightedBlue = (int)((float)blue * weight);
    return  0xFF000000 | weightedRed | weightedGreen | weightedBlue;
  }
  
  static public void renderRainbowImage(LX lx, int[] colors,
  PImage image, boolean antialias) {
    LXModel model = lx.model;
    int imageWidth = image.width;
    int imageHeight = image.height;
    int numPointsPerRow = ((RainbowBaseModel)(lx.model)).pointsWide;
     // Convert from world space to image coordinate space.
    
    for (LXPoint p : model.points) {
      float pointX = (p.x - model.xMin)*6.0;
      float pointY = (p.y - model.yMin)*6.0;
      // The nearest image coordinate
      int xCoord = (int)round(pointX);
      int yCoord = (int)round(pointY);

      // Don't allow rounding past the right or top of the image.
      if (xCoord == imageWidth)
        xCoord -= 1;
      if (yCoord == imageHeight)
        yCoord -= 1;
        
      if (!antialias) {
        // NEAREST IMAGE PIXEL
        int imgIndex = yCoord * imageWidth + xCoord;        
        //System.out.println("yCoord
        colors[p.index] = image.pixels[imgIndex];
      } else {
        // ANTIALIASING
        float remainderX = pointX - floor(pointX);
        float remainderY = pointY - floor(pointY);
        
        // The image coordinate to the left
        int xLeft = floor(pointX);
        // The image coordinate to the right
        int xRight = ceil(pointX);
        // The image coordinate above
        int yAbove = ceil(pointY);
        // The image coordinate below
        int yBelow = floor(pointY);
         
        // The target pixel index in the image for the above.
        int imgIndexLeft = image.width * yCoord + xLeft;
        int imgIndexRight = image.width * yCoord + xRight;
        int imgIndexAbove = image.width * yAbove + xCoord;
        int imgIndexBelow = image.width * yBelow + xCoord;
        
        int leftColor = 0;
        float leftWeight = 1.0 - remainderX;
        int rightColor = 0;
        float rightWeight = remainderX;
        int aboveColor = 0;
        float aboveWeight = remainderY;
        int belowColor = 0;
        float belowWeight = 1.0 - remainderY;
        // When this is zero, we are on the left edge, so nothing to the left.
        if ((p.index + 1)%numPointsPerRow == 0) {
          leftWeight = 1.0;
        }
        if (p.index%numPointsPerRow != 0) {
          leftColor = image.pixels[imgIndexLeft];
          leftColor = ImageUtil.getWeightedColor(leftColor, leftWeight);
        } else {
          rightWeight = 1.0;
        }
        
        // When this is zero, we are on the right edge
        if ((p.index + 1)%numPointsPerRow != 0) {
          rightColor = image.pixels[imgIndexRight];
          rightColor = ImageUtil.getWeightedColor(rightColor, rightWeight);
        }
  
        if (imgIndexAbove >= imageWidth * imageHeight) {
          belowWeight = 1.0;
        }
        // When this is less than zero, we are on the bottom row.
        if (imgIndexBelow >= 0 && imgIndexBelow < imageWidth * imageHeight) {
          belowColor = image.pixels[imgIndexBelow];
          belowColor = ImageUtil.getWeightedColor(belowColor, belowWeight);
          //System.out.println("belowColorW " + String.format("0x%08X", 1));
        } else {
          aboveWeight = 1.0;
        }
        // When this is greater than our number of pixels we are in the top row
        if (!(imgIndexAbove >= imageWidth * imageHeight)) {
          aboveColor = image.pixels[imgIndexAbove];
          aboveColor = ImageUtil.getWeightedColor(aboveColor, aboveWeight);
        }
        int horizontalColor = ImageUtil.getWeightedColor(LXColor.add(leftColor, rightColor), 0.5);
        int verticalColor = ImageUtil.getWeightedColor(LXColor.add(aboveColor, belowColor), 0.5);
        int totalColor = LXColor.add(horizontalColor, verticalColor);
        colors[p.index] = totalColor;
      }
    }    
  }
}

@LXCategory(LXCategory.FORM)
public class PGDraw extends LXPattern {
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 10.0)
    .setDescription("Controls the frames per second.");

  public final BooleanParameter antialiasKnob =
    new BooleanParameter("antialias", true);
  
  private double currentFrame = 0.0;
  private PGraphics pg;
  int imageWidth = 0;
  int imageHeight = 0;
  public PGDraw(LX lx) {
    super(lx);
    imageWidth = ceil((model.xMax - model.xMin) * 6.0);
    imageHeight = ceil((model.yMax - model.yMin) * 6.0);
    addParameter(fpsKnob);
    addParameter(antialiasKnob);
  }
  
  public void run(double deltaMs) {    
    double fps = fpsKnob.getValue();
    int previousFrame = (int)currentFrame;
    currentFrame += (deltaMs/1000.0) * fps;
    if ((int)currentFrame > previousFrame) {
      // Time for new frame.  Draw
      pg = createGraphics(imageWidth, imageHeight);
      
    }
  }
}

@LXCategory(LXCategory.FORM)
public class RainbowGIF extends LXPattern {

  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 10.0)
    .setDescription("Controls the frames per second.");

  public final BooleanParameter antialiasKnob =
    new BooleanParameter("antialias", true);
  
  public final StringParameter filenameKnob =
    new StringParameter("file", "out_b_beeple");
  private PImage[] images;
  private double currentFrame = 0.0;
  private int imageWidth = 0;
  private int imageHeight = 0;
  
  public RainbowGIF(LX lx) {
    super(lx);
    String filename = filenameKnob.getString() + ".gif";
    imageWidth = ceil((model.xMax - model.xMin) * 6.0);
    imageHeight = ceil((model.yMax - model.yMin) * 6.0);
    images = Gif.getPImages(RainbowStudio.pApplet, filename);
    for (int i = 0; i < images.length; i++) {
      images[i].resize(imageWidth, imageHeight);
      images[i].loadPixels();
    }
    addParameter(fpsKnob);
    addParameter(antialiasKnob);
    addParameter(filenameKnob);
  }
     
  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    if (currentFrame >= images.length) {
      currentFrame -= images.length;
    }

    ImageUtil.renderRainbowImage(lx, colors, images[(int)currentFrame], antialiasKnob.isOn());
  }
}

@LXCategory(LXCategory.FORM)
public class GridAnimatedGIF extends LXPattern {
  
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 10.0)
    .setDescription("Controls the frames per second.");
  
  private PImage[] images;
  private String imagePrefix = "life";
  private int numPointsPerRow = 0;
  private int numPointsHigh = 0;
  private double currentFrame = 0.0;
  
  public GridAnimatedGIF(LX lx) {
    super(lx);
    numPointsPerRow = ((RainbowBaseModel)(lx.model)).pointsWide;
    numPointsHigh = ((RainbowBaseModel)(lx.model)).pointsHigh;
    images = Gif.getPImages(RainbowStudio.pApplet, imagePrefix + ".gif");
    for (int i = 0; i < images.length; i++) {
      images[i].resize(numPointsPerRow, numPointsHigh);
      images[i].loadPixels();
    }
    addParameter(fpsKnob);
  }
  
  public void run(double deltaMs) {
    int pointNumber = 0;
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    if (currentFrame > images.length) {
      currentFrame -= images.length;
    }
    for (LXPoint p : model.points) {
      int rowNumber = pointNumber / numPointsPerRow; 
      int columnPos = pointNumber - rowNumber * numPointsPerRow;
      colors[p.index] = images[(int)currentFrame].pixels[rowNumber * numPointsPerRow + columnPos]; 
      ++pointNumber;
    }
  }
}

@LXCategory(LXCategory.FORM)
public class RainbowScannerPattern extends LXPattern {

    public final CompoundParameter width =
    new CompoundParameter("Width", 0, 45)
    .setDescription("Controls the width of the scanner");

  // In columns per second, 0.2 is 5 columns per second
  public final CompoundParameter speed = new CompoundParameter("Speed", 0.2, 4)
  .setDescription("Controls the speed of the scanner");
  
  private double currentScannerColumn = 0.0;
  private boolean movingForward = true;
  
  public RainbowScannerPattern(LX lx) {
    super(lx);
    addParameter(width);
    addParameter(speed);
    movingForward = true;
  }
  
  @Override
  public void run(double deltaMs) {
    int numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    double columnsPerSecond = speed.getValue();
    double scannerWidth = width.getValue();
    if (movingForward) {
      currentScannerColumn += columnsPerSecond * deltaMs;
    } else {
      currentScannerColumn -= columnsPerSecond * deltaMs;
    }
       
    if (currentScannerColumn > numPixelsPerRow) {
      movingForward = false;
    }
    
    if (currentScannerColumn < 0) {
      movingForward = true;
    }
    
    int pointNumber = 0;
    for (LXPoint p : model.points) {
        int pointColumnNumber = pointNumber % numPixelsPerRow;
        if (pointColumnNumber < (int)currentScannerColumn + scannerWidth 
        && pointColumnNumber >= currentScannerColumn - scannerWidth) {
          colors[p.index] = LXColor.gray(100);
        } else {
          // Set to black
          colors[p.index] = 0;
        }
        ++pointNumber;
    }
  }
}

@LXCategory(LXCategory.FORM)
public class RainbowEqualizerPattern extends LXPattern {

  public RainbowEqualizerPattern(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
    GraphicMeter eq = lx.engine.audio.meter;
    int numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    double numRows = ((RainbowBaseModel)lx.model).pointsHigh;
    
    // We need to distribute eq.numBands across our 420 columns
    int pointsPerBand = ceil((float)numPixelsPerRow/ (float)eq.numBands);  // TODO(tracy): handle left over pixels
    int pointNumber = 0;
    
    for (LXPoint p : model.points) {
      int rowNumber = pointNumber / numPixelsPerRow;  // Which row
      int columnPos = pointNumber - rowNumber * numPixelsPerRow;
      int equalizerColumnNumber = columnPos / pointsPerBand;
      // NOTE(tracy): numRows * 2 is a hand-tuned number.  This is also dependent on the 'Range' field
      // in the Audio Meter in the UI.  Might be better to just remove this and play with that range field.
      // This works for 33.6dB in the UI.
      double bandValueScale = numRows * 2;
      double value = bandValueScale * eq.getBand(equalizerColumnNumber);
      if (value > rowNumber) {
        colors[p.index] = LXColor.gray(70 - rowNumber);
      } else {
        colors[p.index] = 0;
      }
      pointNumber++;
    }
  }
}

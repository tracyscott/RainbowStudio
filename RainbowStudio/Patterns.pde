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

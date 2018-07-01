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

// 1 colors
// Top to bottom
// LGBT 6 Bands  (228,3,3) (255,140,0) (255,237,0) (0,128,38) (0,77,255) (117,7,135)
// Bisexual (214, 2, 112) 123p (155,79,150) 61p  (0,56,168) 123p, so 2:1
// Transgender (91, 206, 250) (245,169,184) (255, 255, 255) (245,169,184) (91,206, 250)
/*
 * Flags
 *
 */

public class Flags extends LXPattern {

    public final CompoundParameter flagKnob =
    new CompoundParameter("Flag", 0, 2)
    .setDescription("Which flag.");

  int[] lgbtFlag;
  int[] biFlag;
  int[] transFlag;
  int[][] flags;
  int[] flag;
  
  public Flags(LX lx) {
    super(lx);
    flags = new int[][] {new int[1], new int[1], new int[1]};
    lgbtFlag = new int[6];
    lgbtFlag[0] = LXColor.rgb(117, 7, 135);
    lgbtFlag[1] = LXColor.rgb(0, 77, 255);
    lgbtFlag[2] = LXColor.rgb(0, 128, 38);
    lgbtFlag[3] = LXColor.rgb(255, 237, 0);
    lgbtFlag[4] = LXColor.rgb(255, 140, 0);
    lgbtFlag[5] = LXColor.rgb(228, 3, 3);
    flags[0] = lgbtFlag;
    transFlag = new int[5];
    transFlag[0] = LXColor.rgb(91, 206, 250);
    transFlag[1] = LXColor.rgb(245, 169, 184);
    transFlag[2] = LXColor.rgb(255, 255, 255);
    transFlag[3] = LXColor.rgb(245, 169, 184);
    transFlag[4] = LXColor.rgb(91, 206, 250);
    flags[1] = transFlag;
    biFlag = new int[3];
    biFlag[0] = LXColor.rgb(0, 56, 178);
    biFlag[1] = LXColor.rgb(155, 79, 150);
    biFlag[2] = LXColor.rgb(214, 2, 112);
    flags[2] = biFlag;
    
    flagKnob.setValue(0);
    addParameter(flagKnob);
    flag = flags[(int)round((float)(flagKnob.getValue()))]; 
  }
  
  public void run(double deltaMs) {
    int numRows = ((RainbowBaseModel)lx.model).pointsHigh;
    int flagNum = (int)round((float)(flagKnob.getValue()));
    if (flagNum < 0) flagNum = 0;
    if (flagNum > flags.length - 1) flagNum = flags.length - 1;
    flag = flags[flagNum]; 
    int numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    int pointNumber = 0;
    for (LXPoint p : model.points) {
      int rowNumber = pointNumber / numPixelsPerRow;
      if (flag == lgbtFlag || flag == transFlag) {
        colors[p.index] = flag[rowNumber / (numRows/flag.length)];
      } else if (flag == biFlag) {
        // 2-1-2 ratio of thickness = 5 so each unit is 6 rows 12-6-12
        if (rowNumber > 17) {
          colors[p.index] = biFlag[2];
        } else if (rowNumber < 12) {
          colors[p.index] = biFlag[1];
        } else {
          colors[p.index] = biFlag[0];
        }
      }
      ++pointNumber;
    }
  }
}

/*
 * Abstract base class for Processing drawings when painting the
 * rainbow by sampling a large texture that bounds the top-half
 * of the semi-circle defined by the Rainbow.  Use this class for
 * an accurate 2D representation in physical space.  Because the
 * texture size is bound to the radius of the rainbow, it is also
 * easy to perform radial-based calculations in your texture rendering
 * code (see AnimatedSprite). See PGDraw2 for a sample
 * implementation. It provides a FPS knob and manages FPS logic.  It
 * also provides an antialias toggle. For 1-1 pixel mapping, use PGPixelPerfect.
 */
abstract public class PGTexture extends LXPattern {
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 60.0)
    .setDescription("Controls the frames per second.");

  public final BooleanParameter antialiasKnob =
    new BooleanParameter("antialias", true);

  protected double currentFrame = 0.0;
  protected PGraphics pg;
  protected int imageWidth = 0;
  protected int imageHeight = 0;
  protected int previousFrame = -1;
  protected double deltaDrawMs = 0.0;

  public PGTexture(LX lx) {
    super(lx);
    float radiusInWorldPixels = RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot;
    imageWidth = ceil(radiusInWorldPixels * 2.0);
    imageHeight = ceil(radiusInWorldPixels);
    pg = createGraphics(imageWidth, imageHeight);
    addParameter(fpsKnob);
    addParameter(antialiasKnob);
  }

  public void run(double deltaMs) {    
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    // We don't call draw() every frame so track the accumulated deltaMs for them.
    deltaDrawMs += deltaMs;
    if ((int)currentFrame > previousFrame) {
      // Time for new frame.  Draw
      pg.beginDraw();
      draw(deltaDrawMs);
      pg.endDraw();
      pg.loadPixels();
      previousFrame = (int)currentFrame;
      deltaDrawMs = 0.0;
    }
    // Don't let current frame increment forever.  Otherwise float will
    // begin to lose precision and things get wonky.
    if (currentFrame > 10000.0) {
      currentFrame = 0.0;
      previousFrame = -1;
    }
    RenderImageUtil.imageToPointsSemiCircle(lx, colors, pg, antialiasKnob.isOn());
  }

  // Implement PGGraphics drawing code here.  PGTexture handles beginDraw()/endDraw();
  abstract protected void draw(double deltaDrawMs);
}

/*
 * PGDraw implementation by extending PGTexture.
 */
@LXCategory(LXCategory.FORM)
public class PGDraw2 extends PGTexture {
  float angle = 0.0;
  public PGDraw2(LX lx) {
    super(lx);
  }

  @Override
  protected void draw(double deltaDrawMs) {
      angle += 0.03;
      pg.background(0);
      pg.strokeWeight(10.0);
      pg.stroke(255);
      pg.translate(imageWidth/2.0, imageHeight/2.0);
      pg.pushMatrix();
      pg.rotate(angle);
      pg.line(-imageWidth/2.0 + 10, -imageHeight/2.0 + 10, imageWidth/2.0 - 10, imageHeight/2.0 - 10);
      pg.popMatrix();
  }
}

/*
 * A simple radial test.  It attempts to alternate one row of leds on and off.  Since
 * it renders to a larger texture and is then sampled for the final output, it is not
 * expected to be perfect because of aliasing issues.  It provides a visual representation
 * of the aliasing.
 */
@LXCategory(LXCategory.FORM)
public class PGRadiusTest extends PGTexture {

  public final CompoundParameter thicknessKnob =
    new CompoundParameter("thickness", 1.0, 10.0)
    .setDescription("Thickness of each band");

  public PGRadiusTest(LX lx) {
    super(lx);
    addParameter(thicknessKnob);
    thicknessKnob.setValue(1);
  }

  public void draw(double deltaDrawMs) {
    pg.background(0);
    int xCenterImgSpace = round(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot);
    int yCenterImgSpace = round(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot);
    pg.ellipseMode(RADIUS);
    pg.noSmooth();
    pg.noFill();
    // Adjust this to change the thickness of the bands.
    int strokeWidth = (int)thicknessKnob.getValue();
    if (strokeWidth < 1)
      strokeWidth = 1;
    pg.strokeWeight(strokeWidth);
    for (int i = 0; i < 30; i += strokeWidth) {
      float radius = round((RainbowBaseModel.innerRadius + i * RainbowBaseModel.radiusInc) *
      RainbowBaseModel.pixelsPerFoot);
      if (i % (2 * (int)strokeWidth) == 0) pg.stroke(255);
      else pg.stroke(0);
      pg.ellipse(xCenterImgSpace, yCenterImgSpace, radius, radius);
    }
  }
}


@LXCategory(LXCategory.FORM)
public class AnimatedSprite extends PGTexture {
  public String filename = "smallcat.gif";
  float angle = 0.0;
  float minAngle = 0.0;
  float maxAngle = PI;
  private PImage[] images;
  int spriteWidth = 0;
  public AnimatedSprite(LX lx) {
    super(lx);
    images = Gif.getPImages(RainbowStudio.pApplet, filename);
    for (int i = 0; i < images.length; i++) {
      images[i].loadPixels();
      // assume frames are the same size.
      spriteWidth = images[i].width;
      minAngle = radians(((RainbowBaseModel)(lx.model)).thetaStart - 10.0);
      maxAngle = radians(((RainbowBaseModel)(lx.model)).thetaFinish + 10.0);
      angle = minAngle;
    }
  }

  public void draw(double deltaDrawMs) {
     if (currentFrame >= images.length) {
       currentFrame = 0.0;
       previousFrame = -1;
     }
      angle += 0.03;
      if (angle > maxAngle) angle = minAngle;

      // Use this constant to fine tune where on the radius it should be. Each radiusInc should be
      // the physical distance between LEDs radially.  Here I picked 12.0 to put it in the middle.
      // Computing and using middleRadiusInWorldPixels could also work.
      float radialIncTune = 12.0 * RainbowBaseModel.radiusInc;
      float tunedRadiusInWorldPixels = (RainbowBaseModel.innerRadius + radialIncTune) * RainbowBaseModel.pixelsPerFoot; 

      // Mathematically, this should be the radius of the center strip of pixels.
      // float radiiThickness = RainbowBaseModel.outerRadius - RainbowBaseModel.innerRadius;
      // float middleRadiusInWorldPixels = (RainbowBaseModel.innerRadius + radiiThickness) * RainbowBaseModel.pixelsPerFoot;

      float outerRadiusInWorldPixels = RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot;

      // The rainbow is centered around 0,0 in world space, but x=0 in image space is actually
      // the outer edge of the circle at -radius in world space. i.e. outerRadiusInWorldPixels.
      // Also, up until now we are targeting the center of the image.  Since image coordinates
      // start at 0,0 at the top left we need to change our coordinate space by half our width (center).
      float xImagePos = tunedRadiusInWorldPixels*cos(angle) - spriteWidth/2.0
                      + outerRadiusInWorldPixels; // account for render buffer x=0 maps to world x=-radius

      // Image coordinates have Y inverted from our 3D world space coordinates.  Also, like above,
      // adjust by half our width to change from middle-of-the-image coordinate space to
      // top-left coordinate space.
      float yImagePos = outerRadiusInWorldPixels - tunedRadiusInWorldPixels*sin(angle) - spriteWidth/2.0;

      pg.background(0);
      pg.image(images[(int)currentFrame], xImagePos, yImagePos);
  }
}

/*
 * Abstract base class for pixel perfect Processing drawings.  Use this
 * class for 1-1 pixel mapping with the rainbow.  The drawing will be
 * a rectangle but in physical space it will be distorted by the bend of
 * the rainbow. It provides a FPS knob and manages FPS logic.
 */
abstract public class PGPixelPerfect extends LXPattern {
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 61)
    .setDescription("Controls the frames per second.");

  protected double currentFrame = 0.0;
  protected PGraphics pg;
  protected int imageWidth = 0;
  protected int imageHeight = 0;
  protected int previousFrame = -1;
  protected double deltaDrawMs = 0.0;

  public PGPixelPerfect(LX lx) {
    super(lx);
    imageWidth = ((RainbowBaseModel)lx.model).pointsWide;
    imageHeight = ((RainbowBaseModel)lx.model).pointsHigh;
    pg = createGraphics(imageWidth, imageHeight);
    addParameter(fpsKnob);
    fpsKnob.setValue(5);
  }

  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    // We don't call draw() every frame so track the accumulated deltaMs for them.
    deltaDrawMs += deltaMs;
    if ((int)currentFrame > previousFrame) {
      // Time for new frame.  Draw
      pg.beginDraw();
      draw(deltaDrawMs);
      pg.endDraw();
      pg.loadPixels();
      previousFrame = (int)currentFrame;
      deltaDrawMs = 0.0;
    }
    // Don't let current frame increment forever.  Otherwise float will
    // begin to lose precision and things get wonky.
    if (currentFrame > 10000.0) {
      currentFrame = 0.0;
      previousFrame = -1;
    }

    RenderImageUtil.imageToPointsPixelPerfect(lx, colors, pg);
  }

  // Implement PGGraphics drawing code here.  PGTexture handles beginDraw()/endDraw();
  abstract protected void draw(double deltaMs);
}

@LXCategory(LXCategory.FORM)
public class BasicMidiPP extends LXPattern {
  public final CompoundParameter brightnessKnob =
    new CompoundParameter("bright", 1.0, 100.0)
    .setDescription("Brightness");
    
  public final CompoundParameter barsKnob =
    new CompoundParameter("bars", 5, 6)
    .setDescription("Brightness");
  

  float currentHue = 100.0;
  float currentBrightness = 0.0;
  heronarts.lx.midi.LXMidiOutput midiThroughOutput;
  heronarts.lx.midi.LXMidiInput midiThroughInput;
  int bar = -1;
  
  public BasicMidiPP(LX lx) {
    super(lx);
    // Find target output for passing MIDI through
    heronarts.lx.midi.LXMidiEngine midi = lx.engine.midi;
    for (heronarts.lx.midi.LXMidiOutput output : midi.outputs) {
      System.out.println(output.getName() + ": " + output.getDescription());
      if (output.getName().equalsIgnoreCase("rainbowStudioOut")) {
           midiThroughOutput = output;
           midiThroughOutput.open();
      }
    }
    
    //Louisa: hopefully this is where MIDI input can be initialized too 
    for (heronarts.lx.midi.LXMidiInput input : midi.inputs) {
      System.out.println(input.getName() + ": " + input.getDescription());
      if (input.getName().equalsIgnoreCase("CoreMIDI4J - MPKmini2")) {
           midiThroughInput = input;
           midiThroughInput.open();
           midiThroughInput.addListener(this); 
           System.out.println("Midi Input Activated!!");
      }
    }
    
    brightnessKnob.setValue(30);
    barsKnob.setValue(6);
    addParameter(brightnessKnob);
    addParameter(barsKnob);
  }

  public void run(double deltaMs) {
    int numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    int numRows = ((RainbowBaseModel)lx.model).pointsHigh;
    int pointNumber = 0;
    int numBars = round((float)(barsKnob.getValue()));
    for (LXPoint p : model.points) {
      int rowNumber = pointNumber / numPixelsPerRow;
      if (numBars < 1) numBars = 1;
      if ((rowNumber)/(numRows/numBars) == bar) {
        colors[p.index] = LXColor.gray(100);
      } else {
        colors[p.index] = LXColor.gray(brightnessKnob.getValue());
      }
      ++pointNumber;
    }
  }
  
  // Map a note to a hue
   public void noteOnReceived(MidiNoteOn note) {
     int pitch = note.getPitch();
     System.out.println("pitch: " + pitch);
     // Start at note 60, White keys
     if (pitch == 60) {
       bar = 0;
     } else if (pitch == 62) {
       bar = 1;
     } else if (pitch == 64) {
       bar = 2;
     } else if (pitch == 65) {
       bar = 3;
     } else if (pitch == 67) {
       bar = 4;
     } else if (pitch == 69) {
       bar = 5;
     }
     int velocity = note.getVelocity();
     // NOTE: my mini keyboard generates between 48 & 72 (small keyboard)
     currentHue = map(pitch, 48.0, 72.0, 0.0, 100.0);
     currentBrightness = map(velocity, 0.0, 127.0, 0.0, 100.0);
     // Forward MIDI notes
     if (midiThroughOutput != null) {
       midiThroughOutput.send(note);
     }
  }

  public void noteOffReceived(MidiNote note) {
    // Releasing any note will turn it off.  Multiple notes can be
    // on at once and to turn off when all notes are released we need
    // to track the notes on and only go black once we have received
    // note-off for all notes.
    currentBrightness = 0.0;
    bar = -1;
     // Forward MIDI notes
     if (midiThroughOutput != null) {
       midiThroughOutput.send(note);
     }
  }
}

@LXCategory(LXCategory.FORM)
public class AnimatedSpritePP extends PGPixelPerfect {

  public final CompoundParameter xSpeed =
    new CompoundParameter("XSpd", 1, 20)
    .setDescription("X speed in pixels per frame");

  public String filename = "smallcat.gif";
  private PImage[] images;
  protected int currentPos = 0;

  public AnimatedSpritePP(LX lx) {
    super(lx);
    images = Gif.getPImages(RainbowStudio.pApplet, filename);
    for (int i = 0; i < images.length; i++) {
      images[i].loadPixels();
    }
    // Start off the screen to the right.
    currentPos = imageWidth + images[0].width + 1;
    addParameter(xSpeed);
    xSpeed.setValue(5);
  }

  public void draw(double deltaMs) {
    pg.background(0);
    PImage frameImg = images[((int)currentFrame)%images.length];
    if (currentPos < 0 - frameImg.width) {
      currentPos = imageWidth + frameImg.width + 1;
    }
    pg.image(frameImg, currentPos, 0);
    currentPos -= xSpeed.getValue();
  }
}

@LXCategory(LXCategory.FORM)
public class AnimatedTextPP extends PGPixelPerfect implements CustomDeviceUI {
  public final StringParameter textKnob = new StringParameter("str", "");
  List<TextItem> textItems = new ArrayList<TextItem>();
  UIItemList.ScrollList textItemList;
  
  private static final int MIN_WIDTH = 120;

  public final CompoundParameter xSpeed =
    new CompoundParameter("XSpd", 0, 20)
    .setDescription("X speed in pixels per frame");

  int textBufferWidth = 200;
  PGraphics textImage;
  float currentPos = 0.0;
  int lastPos = 0;
  String[] defaultTexts = {
    "City of orgies, walks and joys,      City whom that I have lived and sung in your midst will one day make      Not the pageants of you, not your shifting tableaus, your spectacles, repay me,      Not the interminable rows of your houses, nor the ships at the wharves,      Nor the processions in the streets, nor the bright windows with goods in them,      Nor to converse with learn'd persons, or bear my share in the soiree or feast;      Not those, but as I pass O Manhattan, your frequent and swift flash of eyes offering me love,      Offering response to my own—these repay me,      Lovers, continual lovers, only repay me.",
    "What's up?",
    "Hello!",        
  };

  int currentString = 0;
  int renderedTextWidth = 0;
  int textGapPixels = 10;
  PFont font;
  int fontSize = 20;
  
  public AnimatedTextPP(LX lx) {
    super(lx);
    addParameter(textKnob);
    addParameter(xSpeed);
    String[] fontNames = PFont.list();
    for (String fontName : fontNames) {
      System.out.println("Font: " + fontName);
    }
    font = createFont("ComicSansMS", fontSize, true);
    for (int i = 0; i < defaultTexts.length; i++) {
      textItems.add(new TextItem(defaultTexts[i]));
    }

    redrawTextBuffer(textBufferWidth);
    xSpeed.setValue(5);
  }
  
  public void redrawTextBuffer(int bufferWidth) {
    textImage = createGraphics(bufferWidth, 30);
    currentPos = imageWidth + 1;
    lastPos = imageWidth + 2;
    textImage.smooth();    
    textImage.beginDraw();
    textImage.background(0);
    textImage.stroke(255);
    if (font != null) 
      textImage.textFont(font);
    else
      textImage.textSize(fontSize);
    String currentText = textItems.get(currentString).getLabel();
    renderedTextWidth = ceil(textImage.textWidth(currentText));
    // If the text was clipped, try again with a larger width.
    if (renderedTextWidth + 1 >= bufferWidth) {
      System.out.println("text clipped: renderedTextWidth=" + renderedTextWidth);
      textImage.endDraw();
      redrawTextBuffer(renderedTextWidth + 10);
    } else {
      textImage.text(currentText, 0, fontSize + 2);
      textImage.endDraw();
    }
  }
  
  public void draw(double deltaDrawMs) {
    if (currentPos < 0 - (renderedTextWidth + textGapPixels)) {
      currentPos = imageWidth + +1;
      lastPos = imageWidth + 2;
      currentString++;
      if (currentString >= textItems.size()) {
        currentString = 0;
      }
      redrawTextBuffer(renderedTextWidth);
    }
    // Optimization to not re-render if we haven't moved far enough
    // since last frame.
    if (round(currentPos) != lastPos) {
      pg.background(0);
      pg.image(textImage, round(currentPos), 0);
      lastPos = round(currentPos);
    }
    currentPos -= xSpeed.getValue();
  }
  
  /*
   * Animated Text has some custom UI components that allow us to add and delete
   * strings at run time.  This is a moderately complex example of custom Pattern UI.
   */
  @Override
  public void buildDeviceUI(UI ui, final UI2dContainer device) {
    device.setContentWidth(MIN_WIDTH);
    device.setLayout(UI2dContainer.Layout.VERTICAL);
    device.setPadding(3, 3, 3, 3);
    
    UI2dContainer knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(3, 3, 3, 3);
    new UIKnob(xSpeed).addToContainer(knobsContainer);
    new UIKnob(fpsKnob).addToContainer(knobsContainer);
    knobsContainer.addToContainer(device);
    
    UI2dContainer textEntryLine = new UI2dContainer(0, 0, device.getWidth(), 30);
    textEntryLine.setLayout(UI2dContainer.Layout.HORIZONTAL);
    
    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
    .setParameter(textKnob)
    .setTextAlignment(PConstants.LEFT)
    .addToContainer(textEntryLine);

    textItemList =  new UIItemList.ScrollList(ui, 0, 5, MIN_WIDTH, 80);
    
    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          textItems.add(new TextItem(textKnob.getString()));
          textItemList.setItems(textItems);
          textKnob.setValue("");
        }
      }
    }
    .setLabel("+")
    .setMomentary(true)
    .addToContainer(textEntryLine);

    textEntryLine.addToContainer(device);
    
    textItemList.setShowCheckboxes(false);
    textItemList.setItems(textItems);
    textItemList.addToContainer(device);
  }
  
  public class TextItem extends UIItemList.Item {
    private final String text;
    
    public TextItem(String str) {
      this.text = str;
    }
    public boolean isActive() {
      return false;
    }
    public int getActiveColor(UI ui) {
      return ui.theme.getAttentionColor();
    }            
    public String getLabel() {
      return text;
    }
    public void onDelete() {
      textItems.remove(this);
      textItemList.removeItem(this);
    }
  }
}

@LXCategory(LXCategory.FORM)
public class RainbowGIFPP extends LXPattern {
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 10.0)
    .setDescription("Controls the frames per second.");

  private PImage[] images;
  private double currentFrame = 0.0;
  private int imageWidth = 0;
  private int imageHeight = 0;

  public RainbowGIFPP(LX lx) {
    super(lx);
    String filename = "life2.gif";
    imageWidth = ((RainbowBaseModel)lx.model).pointsWide;
    imageHeight = ((RainbowBaseModel)lx.model).pointsHigh;
    images = Gif.getPImages(RainbowStudio.pApplet, filename);
    for (int i = 0; i < images.length; i++) {
      images[i].resize(imageWidth, imageHeight);
      images[i].loadPixels();
    }
    addParameter(fpsKnob);
  }

  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    if (currentFrame >= images.length) {
      currentFrame -= images.length;
    }

    RenderImageUtil.imageToPointsPixelPerfect(lx, colors, images[(int)currentFrame]);
  }
}

/*
 * Display an animated GIF on the rainbow.  Note that this code currently
 * assumes an image size equal to the bounding box of the rainbow segment.
 * If you need to generate your image with effects that care about the 
 * circumference of the rainbow, it would be best to create a pattern that calls
 * RenderImageUtil.imageToPointsSemiCircle().  imageToPointsBBox() is just
 * more memory efficient since the image size is smaller.
 */
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
    float radiusInWorldPixels = RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot;
    imageWidth = ceil(radiusInWorldPixels * 2.0);
    imageHeight = ceil(radiusInWorldPixels);
    //imageWidth = ceil((model.xMax - model.xMin) * RainbowBaseModel.pixelsPerFoot);
    //imageHeight = ceil((model.yMax - model.yMin) * RainbowBaseModel.pixelsPerFoot);
    images = Gif.getPImages(RainbowStudio.pApplet, filename);
    for (int i = 0; i < images.length; i++) {
      images[i].resize(imageWidth, imageHeight);
      images[i].loadPixels();
    }
    addParameter(fpsKnob);
    addParameter(antialiasKnob);
    addParameter(filenameKnob);
    fpsKnob.setValue(10);
  }

  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    if (currentFrame >= images.length) {
      currentFrame -= images.length;
    }

    RenderImageUtil.imageToPointsSemiCircle(lx, colors, images[(int)currentFrame], antialiasKnob.isOn());
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
    width.setValue(2.0);
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

/*
 * Original implemenation of PGDraw.  Left here for a full Processing drawing
 * example in case you need to do something not allowed by extending PGTexture
 * or PGPixelPerfect.
 */
@LXCategory(LXCategory.FORM)
public class PGDraw extends LXPattern {
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 10.0)
    .setDescription("Controls the frames per second.");

  public final BooleanParameter antialiasKnob =
    new BooleanParameter("antialias", true);

  protected double currentFrame = 0.0;
  protected PGraphics pg;
  protected int imageWidth = 0;
  protected int imageHeight = 0;
  protected int previousFrame = -1;

  float angle = 0.0;

  public PGDraw(LX lx) {
    super(lx);
    float radiusInWorldPixels = RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot;
    imageWidth = ceil(radiusInWorldPixels * 2.0);
    imageHeight = ceil(radiusInWorldPixels);
    pg = createGraphics(imageWidth, imageHeight);
    addParameter(fpsKnob);
    addParameter(antialiasKnob);
    fpsKnob.setValue(10.0);
  }

  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    if ((int)currentFrame > previousFrame) {
      // Time for new frame.  Draw
      angle += 0.03;
      pg.beginDraw();
      pg.background(70);
      pg.strokeWeight(10.0);
      pg.stroke(255);
      pg.translate(imageWidth/2.0, imageHeight/2.0);
      pg.pushMatrix();
      pg.rotate(angle);
      pg.line(-imageWidth/2.0 + 10, -imageHeight/2.0 + 10, imageWidth/2.0 - 10, imageHeight/2.0 - 10);
      pg.popMatrix();
      pg.endDraw();
      pg.loadPixels();
      previousFrame = (int)currentFrame;
    }
    // Our bounding rectangle is the full half-circle so that Processing drawing operations
    // can work with radial drawings without coordinate space transformation.
    RenderImageUtil.imageToPointsSemiCircle(lx, colors, pg, antialiasKnob.isOn());
  }
}


import gifAnimation.*;
import java.util.*; 

// Fluids
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;
import processing.core.*;
import processing.opengl.PGraphics2D;
// End Fluids

// ShaderToy
import java.nio.ByteBuffer;
import com.jogamp.opengl.GL2;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwShadertoy;
// End ShaderToy

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

  public final DiscreteParameter flagKnob =
    new DiscreteParameter("Flag", 0, 2)
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
 * Abstract base class for all Processing PGraphics drawing and mapping
 * to the Rainbow.
 */
abstract public class PGBase extends LXPattern {
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 60.0)
    .setDescription("Controls the frames per second.");

  protected double currentFrame = 0.0;
  protected PGraphics pg;
  protected int imageWidth = 0;
  protected int imageHeight = 0;
  protected int previousFrame = -1;
  protected double deltaDrawMs = 0.0;

  // For P3D, we need to be on the UI/GL Thread.  We should always be on the GL thread
  // during initialization because we start with Multithreading off.  If somebody enables
  // the Engine thread in the UI we don't want to crash so we will keep track of the GL
  // thread and if the current thread in our run() method doesn't match glThread we will just
  // skip our GL render (image will freeze).
  protected Thread glThread;

  public PGBase(LX lx, int width, int height, String drawMode) {
    super(lx);
    imageWidth = width;
    imageHeight = height;
    if (P3D.equals(drawMode) || P2D.equals(drawMode)) {
      glThread = Thread.currentThread();
      pg = createGraphics(imageWidth, imageHeight, drawMode);
    } else {
      pg = createGraphics(imageWidth, imageHeight);
    }
    addParameter(fpsKnob);
  }

  public void run(double deltaMs) {    
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    // We don't call draw() every frame so track the accumulated deltaMs for them.
    deltaDrawMs += deltaMs;
    if ((int)currentFrame > previousFrame) {
      // if glThread == null this is the default Processing renderer so it is always
      // okay to draw.  If it is not-null, we need to make sure the pattern is
      // executing on the glThread or else Processing will crash.
      if (glThread == null || Thread.currentThread() == glThread) {
        // Time for new frame.  Draw
        pg.beginDraw();
        draw(deltaDrawMs);
        pg.endDraw();
        pg.loadPixels();
      }
      previousFrame = (int)currentFrame;
      deltaDrawMs = 0.0;
    }
    // Don't let current frame increment forever.  Otherwise float will
    // begin to lose precision and things get wonky.
    if (currentFrame > 10000.0) {
      currentFrame = 0.0;
      previousFrame = -1;
    }
    imageToPoints();
  }

  // Responsible for calling RenderImageUtil.imageToPointsSemiCircle to 
  // RenderImageUtil.imageToPointsPixelPerfect.
  abstract protected void imageToPoints();

  // Implement PGGraphics drawing code here.  PGTexture handles beginDraw()/endDraw();
  abstract protected void draw(double deltaDrawMs);
}
/*
 * Abstract base class for Processing drawings when painting the
 * rainbow by sampling a large texture that bounds the top-half
 * of the semi-circle defined by the Rainbow.  Use this class for
 * an accurate 2D representation in physical space.  Because the
 * texture size is bound to the radius of the rainbow, it is also
 * easy to perform radial-based calculations in your texture rendering
 * code (see AnimatedSprite). See PGDraw2 for a sample
 * implementation. Gets FPS knob from PGBase.  It
 * provides an antialias toggle. For 1-1 pixel mapping, use PGPixelPerfect.
 */
abstract public class PGTexture extends PGBase {
  public final BooleanParameter antialiasKnob =
    new BooleanParameter("antialias", true);


  public PGTexture(LX lx, String drawMode) {
    super(lx, ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot * 2.0), 
      ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot), 
      drawMode);
    addParameter(antialiasKnob);
  }

  protected void imageToPoints()
  {
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
    super(lx, P2D);
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
    super(lx, P2D);
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
  public class PG3DSimple extends PGTexture {
  public final CompoundParameter sizeKnob =
    new CompoundParameter("size", 1.0, 30.0)
    .setDescription("Size");

  public PG3DSimple(LX lx) {
    super(lx, P3D);
    fpsKnob.setValue(30);
    sizeKnob.setValue(20);
    addParameter(sizeKnob);
  }

  public void draw(double deltaDrawMs) {
    pg.background(0);
    float radiiThickness = RainbowBaseModel.outerRadius - RainbowBaseModel.innerRadius;
    float middleRadiusInWorldPixels = (RainbowBaseModel.innerRadius + radiiThickness) * RainbowBaseModel.pixelsPerFoot;
    pg.lights();
    pg.rectMode(CENTER);
    pg.fill(190);
    pg.noStroke();
    pg.translate(middleRadiusInWorldPixels, 20, 0);
    pg.rotateY(((int)currentFrame%16) * PI/16.0);
    pg.box((int)(sizeKnob.getValue()));
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
    super(lx, P2D);
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
 * the rainbow. Gets FPS knob from PGBase.
 */
abstract public class PGPixelPerfect extends PGBase {
  public PGPixelPerfect(LX lx, String drawMode) {
    super(lx, ((RainbowBaseModel)lx.model).pointsWide, 
      ((RainbowBaseModel)lx.model).pointsHigh, 
      drawMode);
  }

  protected void imageToPoints() {
    RenderImageUtil.imageToPointsPixelPerfect(lx, colors, pg);
  }

  // Implement PGGraphics drawing code here.  PGPixelPerfect handles beginDraw()/endDraw();
  abstract protected void draw(double deltaDrawMs);
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
  public class KeyboardMidiPP extends LXPattern {
  public final CompoundParameter brightnessKnob =
    new CompoundParameter("bright", 1.0, 100.0)
    .setDescription("Brightness");

  public final CompoundParameter keysKnob =
    new CompoundParameter("bars", 25, 88)
    .setDescription("Musical Keys");

  final int MIDDLEC = 60;  
  heronarts.lx.midi.LXMidiOutput midiThroughOutput;
  heronarts.lx.midi.LXMidiInput midiThroughInput;

  Queue<Integer> keysPlayed = new LinkedList<Integer>();
  ArrayList<Integer> litColumns = new ArrayList<Integer>(); 

  public KeyboardMidiPP(LX lx) {
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

    brightnessKnob.setValue(30);
    keysKnob.setValue(25);
    addParameter(brightnessKnob);
    addParameter(keysKnob);
  }

  public void run(double deltaMs) {

    int numCol = ((RainbowBaseModel)lx.model).pointsWide;
    int centerRainbow; 
    int centerkeyboard; 

    // Find center based on parity (odd/even)
    // To align cente of keyboard and rainbow
    // Note: Even centers are aligned to the left

    // Center of Rainbow
    if (numCol %2 == 0) {
      centerRainbow = (numCol/2);
    } else {
      centerRainbow = (numCol/2)+1;
    }

    // Center of Keyboard
    int numMidiKeys= (int)keysKnob.getValue();
    if (numMidiKeys %2 == 0) {
      centerkeyboard = (int)(numMidiKeys/2);
    } else {
      centerkeyboard = (int)(numMidiKeys/2)+1;
    }

    // Padding needed to fill up the rainbow
    int padding = 0; 

    if (numMidiKeys < numCol) {
      padding = numCol/numMidiKeys;
    }

    // Find out which keys are displayed on the rainbow
    // And add any padding necessary
    for (int note : keysPlayed) {
      int litKeys;
      if (padding != 0) {
        litKeys = ((note - MIDDLEC)+centerkeyboard)*padding;
      } else {
        litKeys = (note - MIDDLEC)+centerRainbow;
      }
      litColumns.add(litKeys);
      for (int i = 0; i < padding; i++) {
        litColumns.add(litKeys + i);
      }
    }

    // Scan and light up points
    int pointNumber = 0; 
    for (LXPoint p : model.points) {
      int colNumber = pointNumber % numCol;

      // Check for bad values
      if (numMidiKeys < 1) numMidiKeys = 1;

      // Light it up!
      if (litColumns.contains(colNumber)) {
        colors[p.index] = LXColor.hsb(colNumber, 100, 100);
      } else {
        colors[p.index] = LXColor.gray(brightnessKnob.getValue());
      }
      ++pointNumber;
    }

    // Refresh Columns to Light up for next round 
    litColumns.clear();
  }

  // Map a note to a hue
  public void noteOnReceived(MidiNoteOn note) {

    // Collect all the Midi notes played 
    int midiNote = note.getPitch();
    if ( midiNote >= 0 && midiNote <= 127) {
      keysPlayed.offer(midiNote);
    }
    if (midiThroughOutput != null) {
      midiThroughOutput.send(note);
    }
  }

  public void noteOffReceived(MidiNote note) {
    // Releasing any note will turn it off.  Multiple notes can be
    // on at once and to turn off when all notes are released we need
    // to track the notes on and only go black once we have received
    // note-off for all notes.

    // Remove all the Midi notes played 
    int midiNote = note.getPitch();
    if ( midiNote >= 0 && midiNote <= 127) {
      try {  
        keysPlayed.remove(midiNote);
      }
      catch (Exception e) {
        // Do nothing, keep operation going
      }
    }
    // Forward MIDI notes
    if (midiThroughOutput != null) {
      midiThroughOutput.send(note);
    }
  }
}

/*
 * Simple Processing 3D example.  Note, due to threading limitations with
 * OpenGL and Processing/Java to run P3D patterns, you MUST NOT enable the
 * separate thread for the Engine in the UI.  Only a single thread may perform
 * OpenGL operations and since the UI is already using OpenGL to render the
 * interface, this drawing code must run in the same thread as the UI.
 */
@LXCategory(LXCategory.FORM)
  public class PG3DSimplePP extends PGPixelPerfect {
  public PG3DSimplePP(LX lx) {
    super(lx, P3D);
  }

  public void draw(double deltaDrawMs) {
    pg.background(0);
    pg.lights();
    pg.rectMode(CENTER);
    pg.fill(190);
    pg.noStroke();
    pg.translate(100, 10, 0);
    pg.rotateY(((int)currentFrame%16) * PI/16.0);
    pg.rotateX(-0.3);
    pg.box(10);
  }
}


/*
 * Utility class for Fluid Simulation.
 */

@LXCategory(LXCategory.FORM)
  public class FluidPP extends PGPixelPerfect {

  private class MyFluidData implements DwFluid2D.FluidData {

    // update() is called during the fluid-simulation update step.
    @Override
      public void update(DwFluid2D fluid) {

      float px, py, radius, r, g, b, intensity, temperature;

      // LGBT 6 Bands  (228,3,3) (255,140,0) (255,237,0) (0,128,38) (0,77,255) (117,7,135)
      py = 5;
      radius = 5;
      intensity = 1.0f;
      // add impulse: density + temperature
      float animator = abs(sin(fluid.simulation_step*0.01f));
      temperature = animator * 10f;

      // add impulse: density + temperature
      px = 5;
      r = 228.0/255.0;
      g = 3.0/255.0;
      b = 3.0/255.0;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 1.0 * imageWidth/5.0;
      r = 255.0/255.0;
      g = 140.0/255.0;
      b = 0.0;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 2.0 * imageWidth/5.0;
      r = 255.0/255.0;
      g = 237.0/255.0;
      b = 0.0;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 3.0 * imageWidth/5.0;
      r = 0.0;
      g = 128.0/255.0;
      b = 38.0/255.0;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 4*imageWidth/5.0f;
      r = 0.0f;
      g = 77.0/255.0;
      b = 1.0;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = imageWidth - 5;
      r = 117.0 / 255.0;
      g = 7.0 / 255.0;
      b = 135.0 / 255.0;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);
    }
  }

  int fluidgrid_scale = 1;
  DwFluid2D fluid;
  PGraphics2D pg_fluid;
  PGraphics2D pg_obstacles;
  int     BACKGROUND_COLOR           = 0;
  boolean UPDATE_FLUID               = true;
  boolean DISPLAY_FLUID_TEXTURES     = true;
  boolean DISPLAY_FLUID_VECTORS      = false;
  int     DISPLAY_fluid_texture_mode = 0;

  public FluidPP(LX lx) {
    super(lx, "");
    fpsKnob.setValue(60);
    DwPixelFlow context = new DwPixelFlow(RainbowStudio.pApplet);
    context.print();
    context.printGL();
    // fluid simulation
    System.out.println(imageWidth + "," + imageHeight);
    fluid = new DwFluid2D(context, imageWidth, imageHeight, fluidgrid_scale);
    // set some simulation parameters
    fluid.param.dissipation_density     = 0.999f;
    fluid.param.dissipation_velocity    = 0.99f;
    fluid.param.dissipation_temperature = 0.80f;
    fluid.param.vorticity               = 0.10f;
    // interface for adding data to the fluid simulation
    MyFluidData cb_fluid_data = new MyFluidData();
    fluid.addCallback_FluiData(cb_fluid_data);
    // pgraphics for fluid
    pg_fluid = (PGraphics2D) createGraphics(imageWidth, imageHeight, P2D);
    pg_fluid.smooth(4);
    pg_fluid.beginDraw();
    pg_fluid.background(BACKGROUND_COLOR);
    pg_fluid.endDraw();
    // pgraphics for obstacles
    pg_obstacles = (PGraphics2D) createGraphics(imageWidth, imageHeight, P2D);
    pg_obstacles.smooth(0);
    pg_obstacles.beginDraw();
    pg_obstacles.clear();
    // border-obstacle
    pg_obstacles.strokeWeight(1);
    pg_obstacles.stroke(100);
    pg_obstacles.noFill();
    pg_obstacles.rect(0, 0, pg_obstacles.width, pg_obstacles.height);
    pg_obstacles.endDraw();
  }

  public void draw(double deltaDrawMs) {
    pg.background(0);
    fluid.addObstacles(pg_obstacles);
    fluid.update();
    // clear render target
    pg_fluid.beginDraw();
    pg_fluid.background(BACKGROUND_COLOR);
    pg_fluid.endDraw();
    fluid.renderFluidTextures(pg_fluid, DISPLAY_fluid_texture_mode);
    pg_fluid.loadPixels();
    pg_fluid.updatePixels();
    pg.image(pg_fluid, 0, 0);
    pg.loadPixels();
    pg.updatePixels();
  }
}

@LXCategory(LXCategory.FORM)
  public class AnimatedSpritePP extends PGPixelPerfect implements CustomDeviceUI {
  public final StringParameter spriteFileKnob = new StringParameter("sprite", "smallcat");
  public final CompoundParameter xSpeed =
    new CompoundParameter("XSpd", 1, 20)
    .setDescription("X speed in pixels per frame");

  List<FileItem> fileItems = new ArrayList<FileItem>();
  UIItemList.ScrollList fileItemList;
  List<String> spriteFiles;
  private static final int CONTROLS_MIN_WIDTH = 120;
  public String filename = "smallcat.gif";

  private PImage[] images;
  protected int currentPos = 0;

  public AnimatedSpritePP(LX lx) {
    super(lx, "");
    addParameter(xSpeed);
    xSpeed.setValue(5);
    loadSprite(spriteFileKnob.getString());
    spriteFiles = getSpriteFiles();
    for (String filename : spriteFiles) {
      fileItems.add(new FileItem(filename));
    }
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

  protected void loadSprite(String spritename) {
    String filename = dataPath("./spritepp/" + spritename + ".gif");
    images = Gif.getPImages(RainbowStudio.pApplet, filename);
    for (int i = 0; i < images.length; i++) {
      images[i].loadPixels();
    }
    // Start off the screen to the right.
    currentPos = imageWidth + images[0].width + 1;
  }

  protected File getFile() {
    return new File(dataPath("./spritepp/" + this.spriteFileKnob.getString() + ".gif"));
  }

  private String stripExtension (String str) {
    if (str == null) return null;
    int pos = str.lastIndexOf(".");
    if (pos == -1) return str;
    return str.substring(0, pos);
  }

  protected List<String> getSpriteFiles() {
    List<String> results = new ArrayList<String>();

    File[] files = new File(dataPath("./spritepp/")).listFiles();
    //If this pathname does not denote a directory, then listFiles() returns null.
    for (File file : files) {
      if (file.isFile()) {
        if (file.getName().endsWith(".gif")) {
          results.add(stripExtension(file.getName()));
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
    new UIKnob(xSpeed).addToContainer(knobsContainer);
    new UIKnob(fpsKnob).addToContainer(knobsContainer);
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
      spriteFileKnob.setValue(filename);
      loadSprite(filename);
    }
  }
}

@LXCategory(LXCategory.FORM)
  public class ShaderToy extends PGPixelPerfect implements CustomDeviceUI {
  public final StringParameter shaderFileKnob = new StringParameter("frag", "VoronoiDistances");

  List<FileItem> fileItems = new ArrayList<FileItem>();
  UIItemList.ScrollList fileItemList;
  List<String> shaderFiles;

  DwPixelFlow context;
  DwShadertoy toy;
  DwGLTexture tex0 = new DwGLTexture();
  PGraphics toyGraphics;
  private static final int CONTROLS_MIN_WIDTH = 120;

  public ShaderToy(LX lx) {
    super(lx, "");
    fpsKnob.setValue(60);
    toyGraphics = createGraphics(imageWidth, imageHeight, P2D);
    loadShader(shaderFileKnob.getString());
    // context initialized in loadShader, print the GL hardware once when loading
    // the pattern.  left in for now while testing performance on different
    // graphics hardware.
    context.print();
    context.printGL();

    shaderFiles = getShaderFiles();
    for (String filename : shaderFiles) {
      fileItems.add(new FileItem(filename));
    }

    // TODO(tracy):  This is Voronoi-specific data.  ShaderToy shaders
    // that rely on inputs might need custom implemented patterns.
    // Some inputs are standard like Audio data
    // so that can be enabled with a toggle.  Actually, each Channel0..3
    // should have a dropdown to select the input as on shadertoy.com.

    // create noise texture.
    int wh = 256;
    byte[] bdata = new byte[wh * wh * 4];
    ByteBuffer bbuffer = ByteBuffer.wrap(bdata);
    for (int i = 0; i < bdata.length; ) {
      bdata[i++] = (byte) random(0, 255);
      bdata[i++] = (byte) random(0, 255);
      bdata[i++] = (byte) random(0, 255);
      bdata[i++] = (byte) 255;
    }
    // Noise data texture passsed as a texture.
    tex0.resize(context, GL2.GL_RGBA8, wh, wh, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, GL2.GL_LINEAR, GL2.GL_MIRRORED_REPEAT, 4, 1, bbuffer);
  }

  protected void loadShader(String shaderFile) {
    if (toy != null) toy.release();  // release existing shader texture
    if (context != null) context.release();
    context = new DwPixelFlow(RainbowStudio.pApplet);
    toy = new DwShadertoy(context, "data/" + shaderFile + ".frag");
  }

  public void draw(double drawDeltaMs) {
    pg.background(0);
    toy.set_iChannel(0, tex0);
    toy.apply(toyGraphics);
    toyGraphics.loadPixels();
    toyGraphics.updatePixels();
    pg.image(toyGraphics, 0, 0);
    pg.loadPixels();
  }

  protected File getFile() {
    return new File(dataPath(this.shaderFileKnob.getString() + ".frag"));
  }

  private String stripExtension (String str) {
    if (str == null) return null;
    int pos = str.lastIndexOf(".");
    if (pos == -1) return str;
    return str.substring(0, pos);
  }

  protected List<String> getShaderFiles() {
    List<String> results = new ArrayList<String>();

    File[] files = new File(dataPath("./")).listFiles();
    //If this pathname does not denote a directory, then listFiles() returns null.
    for (File file : files) {
      if (file.isFile()) {
        if (file.getName().endsWith(".frag")) {
          results.add(stripExtension(file.getName()));
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

    new UIKnob(fpsKnob).addToContainer(device);

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
        if (on) {
          try {
            File shaderFile = getFile();
            if (!shaderFile.exists()) {
              // For new files, copy the template in.
              java.nio.file.Files.copy(new File(dataPath("basic.frag")).toPath(), 
                shaderFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            java.awt.Desktop.getDesktop().edit(shaderFile);
          } 
          catch (Throwable t) {
            System.err.println(t.getLocalizedMessage());
          }
        }
      }
    }
    .setLabel("Edit").setMomentary(true).addToContainer(device);

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
      shaderFileKnob.setValue(filename);
      loadShader(filename);
    }
  }
}

@LXCategory(LXCategory.FORM)
  public class ShaderToyAudio extends PGPixelPerfect {
  DwPixelFlow context;
  DwShadertoy toy;
  PGraphics toyGraphics;

  public ShaderToyAudio(LX lx) {
    super(lx, "");
    fpsKnob.setValue(60);
    context = new DwPixelFlow(RainbowStudio.pApplet);
    context.print();
    context.printGL();
    toyGraphics = createGraphics(imageWidth, imageHeight, P2D);
    toy = new DwShadertoy(context, "data/audio.frag");
  }

  public void draw(double drawDeltaMs) {
    // Create Audio FFT texture
    GraphicMeter eq = lx.engine.audio.meter;
    byte[] fft = new byte[1024];
    // System.out.println("numBands: " + eq.numBands);
    // ShaderToy expects a 512x2 texture (arranged as 1024 continuous bytes).
    // The first 512 bytes are the FFT data.  The second 512 bytes are the audio
    // data that corresponds to that FFT data.  We currently don't have access to
    // the raw FFT or the raw Audio Buffer.  Need to get some changes from Mark.
    // For now, we are just packing
    // the array with repeating equalizer bands.  In our shader, we just scale
    // our normalized pixel coordinates so that the entire render buffer samples
    // from the first 16 bytes of the texture.
    for (int i = 0; i < 1024; i++) {
      int bandVal = (int)(eq.getBandf(i%16) * 255.0);
      fft[i++] = (byte)(bandVal);
    }
    ByteBuffer fftBuf = ByteBuffer.wrap(fft);
    DwGLTexture tex0 = new DwGLTexture();
    tex0.resize(context, GL2.GL_R8, 512, 2, GL2.GL_RED, GL2.GL_UNSIGNED_BYTE, 
      GL2.GL_LINEAR, GL2.GL_MIRRORED_REPEAT, 1, 1, fftBuf);
    toy.set_iChannel(0, tex0);
    pg.background(0);
    toy.apply(toyGraphics);
    toyGraphics.loadPixels();
    toyGraphics.updatePixels();
    pg.image(toyGraphics, 0, 0);
    pg.loadPixels();
    tex0.release();
  }
}

// TODO(Tracy): Move this to UIUtils.pde.  This is used by a couple different
// patterns.

@LXCategory(LXCategory.FORM)
  public class AnimatedTextPP extends PGPixelPerfect implements CustomDeviceUI {
  public final StringParameter textKnob = new StringParameter("str", "");

  List<TextItem> textItems = new ArrayList<TextItem>();
  UIItemList.ScrollList textItemList;
  private static final int CONTROLS_MIN_WIDTH = 120;
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
    super(lx, "");
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
    device.setContentWidth(CONTROLS_MIN_WIDTH);
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

    textItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);

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
  public class RainbowGIFPP extends LXPattern implements CustomDeviceUI {
  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 10.0)
    .setDescription("Controls the frames per second.");
  public final StringParameter gifKnob = new StringParameter("gif", "life2")
    .setDescription("420x30 Pixel perfect animated gif.");

  List<FileItem> fileItems = new ArrayList<FileItem>();
  UIItemList.ScrollList fileItemList;
  List<String> gifFiles;
  private static final int CONTROLS_MIN_WIDTH = 120;

  private PImage[] images;
  private double currentFrame = 0.0;
  private int imageWidth = 0;
  private int imageHeight = 0;

  public RainbowGIFPP(LX lx) {
    super(lx);
    imageWidth = ((RainbowBaseModel)lx.model).pointsWide;
    imageHeight = ((RainbowBaseModel)lx.model).pointsHigh;
    addParameter(fpsKnob);
    loadGif(gifKnob.getString());
    gifFiles = getGifFiles();
    for (String filename : gifFiles) {
      fileItems.add(new FileItem(filename));
    }
  }

  protected void loadGif(String gifname) {
    String filename = dataPath("./gifpp/" + gifname + ".gif");
    images = Gif.getPImages(RainbowStudio.pApplet, filename);
    for (int i = 0; i < images.length; i++) {
      images[i].resize(imageWidth, imageHeight);
      images[i].loadPixels();
    }
  }

  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    if (currentFrame >= images.length) {
      currentFrame -= images.length;
    }

    RenderImageUtil.imageToPointsPixelPerfect(lx, colors, images[(int)currentFrame]);
  }

  protected File getFile() {
    return new File(dataPath("./gifpp/" + this.gifKnob.getString() + ".gif"));
  }

  private String stripExtension (String str) {
    if (str == null) return null;
    int pos = str.lastIndexOf(".");
    if (pos == -1) return str;
    return str.substring(0, pos);
  }

  protected List<String> getGifFiles() {
    List<String> results = new ArrayList<String>();

    File[] files = new File(dataPath("./gifpp/")).listFiles();
    //If this pathname does not denote a directory, then listFiles() returns null.
    for (File file : files) {
      if (file.isFile()) {
        if (file.getName().endsWith(".gif")) {
          results.add(stripExtension(file.getName()));
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
      .setParameter(gifKnob)
      .setTextAlignment(PConstants.LEFT)
      .addToContainer(filenameEntry);


    // Button for reloading shader.
    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
      @Override
        public void onToggle(boolean on) {
        if (on) {
          loadGif(gifKnob.getString());
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
      gifKnob.setValue(filename);
      loadGif(filename);
    }
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
  public class RainbowGIF extends LXPattern implements CustomDeviceUI {

  public final CompoundParameter fpsKnob =
    new CompoundParameter("Fps", 1.0, 60.0)
    .setDescription("Controls the frames per second.");
  public final BooleanParameter antialiasKnob =
    new BooleanParameter("antialias", true);
  public final StringParameter gifKnob = new StringParameter("gif", "out_b_beeple")
    .setDescription("Animated gif");

  List<FileItem> fileItems = new ArrayList<FileItem>();
  UIItemList.ScrollList fileItemList;
  List<String> gifFiles;
  private static final int CONTROLS_MIN_WIDTH = 120;

  private PImage[] images;
  private double currentFrame = 0.0;
  private int imageWidth = 0;
  private int imageHeight = 0;
  public RainbowGIF(LX lx) {
    super(lx);
    float radiusInWorldPixels = RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot;
    imageWidth = ceil(radiusInWorldPixels * 2.0);
    imageHeight = ceil(radiusInWorldPixels);
    
    loadGif(gifKnob.getString());
    gifFiles = getGifFiles();
    for (String filename : gifFiles) {
      fileItems.add(new FileItem(filename));
    }
    
    addParameter(fpsKnob);
    addParameter(antialiasKnob);
    addParameter(gifKnob);
    fpsKnob.setValue(10);
  }

  protected void loadGif(String gifname) {
    String filename = dataPath("./giftex/" + gifname + ".gif");
    images = Gif.getPImages(RainbowStudio.pApplet, filename);
    for (int i = 0; i < images.length; i++) {
      images[i].resize(imageWidth, imageHeight);
      images[i].loadPixels();
    }
  }

  public void run(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs/1000.0) * fps;
    if (currentFrame >= images.length) {
      currentFrame -= images.length;
    }

    RenderImageUtil.imageToPointsSemiCircle(lx, colors, images[(int)currentFrame], antialiasKnob.isOn());
  }
  
  protected File getFile() {
    return new File(dataPath("./giftex/" + this.gifKnob.getString() + ".gif"));
  }

  private String stripExtension (String str) {
    if (str == null) return null;
    int pos = str.lastIndexOf(".");
    if (pos == -1) return str;
    return str.substring(0, pos);
  }

  protected List<String> getGifFiles() {
    List<String> results = new ArrayList<String>();

    File[] files = new File(dataPath("./giftex/")).listFiles();
    //If this pathname does not denote a directory, then listFiles() returns null.
    for (File file : files) {
      if (file.isFile()) {
        if (file.getName().endsWith(".gif")) {
          results.add(stripExtension(file.getName()));
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
      .setParameter(gifKnob)
      .setTextAlignment(PConstants.LEFT)
      .addToContainer(filenameEntry);

    // Button for reloading shader.
    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
      @Override
        public void onToggle(boolean on) {
        if (on) {
          loadGif(gifKnob.getString());
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
      gifKnob.setValue(filename);
      loadGif(filename);
    }
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

@LXCategory(LXCategory.FORM)
  public class RainbowSort extends LXPattern {
  public final CompoundParameter swapsKnob =
    new CompoundParameter("Swaps", 1, 20).setDescription("Swaps per frame.");
  public final CompoundParameter brightnessKnob =
    new CompoundParameter("Bright", 1, 100).setDescription("Brightness.");
  public final CompoundParameter saturationKnob =
    new CompoundParameter("Sat", 1, 100).setDescription("Saturation");

  float hues[];
  boolean sortDone;
  float sortedHues[];

  public RainbowSort(LX lx) {
    super(lx);
    hues = new float[420];
    sortedHues = new float[420];
    sortDone = true;
    resetSort();
    addParameter(swapsKnob);
    addParameter(brightnessKnob);
    addParameter(saturationKnob);
    brightnessKnob.setValue(100);
    saturationKnob.setValue(100);
    swapsKnob.setValue(5);
  }

  // For each iteration of the run, do one sorting step.  If sortDone==true,
  // re-randomize the hues.
  public void run(double deltaMs) {
    if (isSortDone()) {
      resetSort();
    }

    for (int j = 0; j < swapsKnob.getValue(); j++) {
      while (!swap()) {
      }
      if (isSortDone()) 
        break;
    }

    int pointNumber = 0;
    for (LXPoint p : model.points) {
      int pointCol = pointNumber % ((RainbowBaseModel)lx.model).pointsWide;
      colors[p.index] = LXColor.hsb(sortedHues[pointCol], saturationKnob.getValue(), 
        brightnessKnob.getValue());
      ++pointNumber;
    }
  }

  protected boolean swap() {
    int indexA = -1;
    int indexB = -1;

    // Pick to random indexes, compare and swap.
    while (indexA == indexB) {
      indexA = (int)(Math.random() * sortedHues.length);
      indexB = (int)(Math.random() * sortedHues.length);
    }

    float hueA = sortedHues[indexA];
    float hueB = sortedHues[indexB];
    if (indexA < indexB) {
      if (hueB < hueA) {
        sortedHues[indexA] = hueB;
        sortedHues[indexB] = hueA;
        return true;
      } else {
        return false;
      }
    } else {
      // indexB < indexA
      if (hueA < hueB) {
        sortedHues[indexA] = hueB;
        sortedHues[indexB] = hueA;
        return true;
      } else {
        return false;
      }
    }
  }

  protected boolean isSortDone() {
    for (int i = 0; i < sortedHues.length; i++) {
      if (i == 0) continue;
      if (sortedHues[i-1] > sortedHues[i])
        return false;
    }
    return true;
  }

  protected void resetSort() {
    for (int i = 0; i < sortedHues.length; i++)
      sortedHues[i] = -2.0;
    for (int i = 0; i < hues.length; i++)
      hues[i] = i * 360.0/hues.length;
    for (int i = 0; i < hues.length; i++) {
      boolean notEmpty = true;
      int index = 0;
      // Randomly pick a vertical strip on the rainbow to place our
      // hues.  Keep picking an index until an available index is found.
      while (notEmpty) {
        index = (int)(Math.random() * sortedHues.length);
        if (sortedHues[index] < -1.0) {
          notEmpty = false;
        }
      }
      sortedHues[index] = hues[i];
    }
  }
}

@LXCategory(LXCategory.COLOR)
  public class RainbowRecursion extends LXPattern {
  public final CompoundParameter depthKnob =
    new CompoundParameter("Depth", 0, 10).setDescription("Max recursion depth.");
  public final CompoundParameter thicknessKnob =
    new CompoundParameter("Thick", 1, 10).setDescription("Thickness");
  public final CompoundParameter hueOffsetKnob =
    new CompoundParameter("hOffset", -360, 360).setDescription("Hue offset");

  int maxDepth = 9;
  int currentMaxDepth = 0;
  int pointsWide;
  int pointsHigh;
  int bandHeight = 3;
  boolean forward = true;
  double hueOffset = 0.0;

  public RainbowRecursion(LX lx) {
    super(lx);
    pointsWide = ((RainbowBaseModel)(lx.model)).pointsWide;
    pointsHigh = ((RainbowBaseModel)(lx.model)).pointsHigh;
    addParameter(depthKnob);
    addParameter(thicknessKnob);
    addParameter(hueOffsetKnob);
    depthKnob.setValue(9);
    thicknessKnob.setValue(3);
    hueOffsetKnob.setValue(0);
  }

  void run(double drawDeltaMs) {
    currentMaxDepth = ((int) depthKnob.getValue()) - 1;
    bandHeight = (int) thicknessKnob.getValue();
    for (LXPoint p : model.points) {
      colors[p.index] = 0;
    }
    colorRecursive(0, 0);
  }

  void colorRecursive(int thisDepth, int xOffset) {
    // Draw a band of full hue across points at this level and this chunk. Based on our recursion depth and
    // the xOffset we can compute the batch of points that we need to color.
    if (thisDepth > currentMaxDepth) return;
    int chunkSize = pointsWide / (int)pow(2, thisDepth); 
    int startLedRow = xOffset;
    int endLedRow = startLedRow + (int)thicknessKnob.getValue() * pointsWide;

    for (int currentLed = startLedRow; currentLed < endLedRow; currentLed++) {
      // Some combination of depths and band thickness can go past our available # leds
      if (currentLed >= model.points.length) return;

      // If we are at the end of a chunk, jump up to the next row of LEDs      
     if (currentLed > startLedRow + chunkSize) {
        startLedRow = startLedRow + pointsWide;
        currentLed = startLedRow - 1;  // account for the for loop doing currentLed++
        continue;
      }
      int xpos = currentLed - startLedRow;
      float hue = 360.0 * (float)xpos/(float)chunkSize;
      hue = (float)hueOffsetKnob.getValue() + hue;
      if (hue > 360.0) hue = hue - 360.0;
      if (hue < 0.0) hue = hue + 360.0;
      LXPoint p = model.points[currentLed];
      colors[p.index] = LXColor.hsb(hue, 100, 100);
    }
    colorRecursive(thisDepth + 1, endLedRow);
    colorRecursive(thisDepth + 1, endLedRow + chunkSize/2);
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

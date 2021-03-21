package com.giantrainbow.patterns;

import com.giantrainbow.PathUtils;
import com.giantrainbow.RainbowStudio;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwShadertoy;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.giantrainbow.RainbowStudio.pApplet;
import static processing.core.PConstants.P2D;

public class Hatch extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(Hatch.class.getName());

  public CompoundParameter speedKnob = new CompoundParameter("speed", 20f, 0f, 60f);
  public DiscreteParameter numEggs = new DiscreteParameter("eggs", 30, 1, 60)
      .setDescription("The number of eggs to hatch");
  public CompoundParameter eggFps = new CompoundParameter("EggFPS", 2, .1, 10)
      .setDescription("FPS for egg animation");
  public CompoundParameter chickFps = new CompoundParameter("ChickFPS", 8, .1, 10)
      .setDescription("FPS for chick animation");
  public CompoundParameter xSpeed = new CompoundParameter("Xspd", 1.0, 0.1, 10.0)
      .setDescription("Walking speed of chicks");
  public CompoundParameter directionProb = new CompoundParameter("DirPb", 0.01, 0.005, 1.0)
      .setDescription("Probability of direction change");
  public CompoundParameter hatchProb = new CompoundParameter("HPb", 0.01, 0.005, 1.0)
      .setDescription("Probability egg will hatch");

  PImage[] hatchSprite;
  PImage[] chickSprite;

  PImage tileImage;

  private static final String SPRITE_DIR = "spritepp/";
  private static final String LOCAL_SHADER_DIR = "shaders/";
  private static final String CLOUDS_SHADER = "clouds.frag";
  DwPixelFlow context;
  DwShadertoy toy;
  PGraphics toyGraphics;
  float[] u1, u2;

  List<EggHatch> eggs = new ArrayList<EggHatch>();
  List<Chick> chicks = new ArrayList<Chick>();

  public Hatch(LX lx) {
    super(lx, "");
    fpsKnob.setValue(30);
    addParameter(numEggs);
    addParameter(eggFps);
    addParameter(chickFps);
    addParameter(xSpeed);
    addParameter(directionProb);
    addParameter(hatchProb);

    hatchSprite = PathUtils.loadSprite(pApplet, SPRITE_DIR + "hatch.gif");
    chickSprite = PathUtils.loadSprite(pApplet, SPRITE_DIR + "chick.gif");


    reset();
    addParameter(speedKnob);

    tileImage = RainbowStudio.pApplet.loadImage("imgpp/bgshortdrk.gif");
    tileImage.loadPixels();

    initShader();
  }

  @Override
  public void onActive() {
    eggs.clear();
    chicks.clear();
    for (int i = 0; i < numEggs.getValuei(); i++) {
      EggHatch egg = new EggHatch(hatchSprite, chickSprite);
      egg.isEnabled = true;
      eggs.add(egg);
      chicks.add(egg.chick);
    }
  }

  public void initShader() {
    u1 = new float[4];
    u2 = new float[4];
    toyGraphics = RainbowStudio.pApplet.createGraphics(pg.width, pg.height, P2D);
    loadShader();
    // context initialized in loadShader, print the GL hardware once when loading
    // the pattern.  left in for now while testing performance on different
    // graphics hardware.
    context.print();
    context.printGL();
  }

  protected void loadShader() {
    if (toy != null) {
      // release existing shader texture
      toy.release();
      toy = null;
    }
    if (context != null) context.release();
    context = new DwPixelFlow(RainbowStudio.pApplet);
    // TODO(tracy): Handle file not found issue.

    File local = new File(LOCAL_SHADER_DIR + CLOUDS_SHADER);
    if (local.isFile()) {
      toy = new DwShadertoy(context, local.getPath());
    }
  }

  public PImage runShader() {
    toy.set_iMouse(0f, 0f, 0f, 0f);
    u1[0] = 0.21f;  // cloud scale
    u1[1] = 0.33f;  // cloud speed
    u1[2] = 0.57f;  // cloud darkness
    u1[3] = 0.5f;   // cloud lightness

    u2[0] = 0.25f; // cloud cover
    u2[1] = 0.29f; // cloud alpha
    u2[2] = 0.23f; // sky tint (can go brownish)
    u2[3] = 0f;
    //toy.apply(toyGraphics);
    ShaderToy.shaderApply(context, toy, (PGraphicsOpenGL) toyGraphics, u1, u2);
    toyGraphics.loadPixels();
    toyGraphics.updatePixels();
    return toyGraphics;
  }

  public void reset() {
    // Remove all sprites.
  }

  public void drawBackground() {
    pg.image(runShader(), 0, 0);
    int numTiles = (int) Math.ceil((float) pg.width / (float) tileImage.width);
    for (int i = 0; i < numTiles; i++) {
      pg.image(tileImage, i * tileImage.width, 0);
    }
  }

  @Override
  public void draw(double deltaDrawMs) {
    pg.background(0, 0);
    drawBackground();
    for (int i = 0; i < eggs.size(); i++) {
      EggHatch hatch = eggs.get(i);
      if (!hatch.hasStarted && Math.random() < hatchProb.getValuef())
        hatch.hasStarted = true;
      PImage frame = hatch.nextFrame(deltaDrawMs, eggFps.getValuef());
      if (frame != null)
        pg.image(frame, hatch.xPos, 15, frame.width, frame.height);
      Chick chick = chicks.get(i);
      if (Math.random() < directionProb.getValuef())
        chick.changeDirection();
      frame = chick.nextFrame(deltaDrawMs, chickFps.getValuef(), xSpeed.getValuef());
      if (frame != null)
        pg.image(frame, chick.xPos, 15, frame.width, frame.height);
    }
  }

  static public class EggHatch {
    PImage[] hSprite;
    public float xPos;
    boolean hasStarted = false;
    public Chick chick;
    double currentFPSFrame = 0.0;
    int previousFrame = -1;
    PImage lastFrame = null;
    public boolean isEnabled = true;

    public EggHatch(PImage[] hSprite, PImage[] chickSprite) {
      this.hSprite = hSprite;
      xPos = (float)Math.random() * 400f + 15f;
      chick = new Chick(chickSprite,  xPos);
    }

    public PImage nextFrame(double drawDeltaMs, float chickFps) {
      if (!isEnabled) {
        return null;
      }
      if (!hasStarted) {
        return hSprite[0];
      }
      currentFPSFrame += (drawDeltaMs / 1000.0) * chickFps;
      // If we haven't advanced a frame yet, just return our last frame.
      if ((int) currentFPSFrame <= previousFrame) {
        return lastFrame;
      }
      // Otherwise, update our previousFrame counter and pick a new frame.
      previousFrame = (int) currentFPSFrame;
      lastFrame = hSprite[previousFrame % hSprite.length];
      if (previousFrame == hSprite.length - 1) {
        isEnabled = false;
        chick.enable();
      }
      return lastFrame;
    }
  }

  static public class Chick {
    public PImage[] cSprite;
    public int[] leftFrames = {0, 1, 2};
    public int[] frontFrames = {3, 4, 5};
    public int[] backFrames = {6, 7, 8};
    public int[] rightFrames = {9, 10, 11};
    enum Direction {LEFT, RIGHT, FRONT, BACK}
    int curFrame = 0;
    int turningAround = 0;
    int turningAroundFrameNum = 0;
    PImage lastFrame;
    protected double currentFPSFrame = 0.0;
    Direction direction = Direction.FRONT;
    Direction nextDirection = Direction.LEFT;
    int previousFrame = -1;
    public boolean isEnabled = false;
    public float xPos;

    public Chick(PImage[] cSprite, float xPos) {
      this.cSprite = cSprite;
      this.xPos = xPos;
    }

    public void enable() {
      isEnabled = true;
      // Chick starts off facing the camera
      turningAround = 1;
      if (Math.random() < 0.5f)
        nextDirection = Chick.Direction.LEFT;
      else
        nextDirection = Chick.Direction.RIGHT;
    }

    public void setDirection(Direction dir) {
      direction = dir;
      curFrame = 0;
    }

    /**
     * Handles picking the appropriate next frame.  Also handles turning around logic.
     * @return
     */
    public PImage nextFrame(double drawDeltaMs, float chickFps, float xSpeed) {
      if (!isEnabled)
        return null;

      currentFPSFrame += (drawDeltaMs / 1000.0) * chickFps;
      // If we haven't advanced a frame yet, just return our last frame.
      if ((int) currentFPSFrame <= previousFrame) {
        return lastFrame;
      }
      // Otherwise, update our previousFrame counter and pick a new frame.
      previousFrame = (int) currentFPSFrame;
      if (turningAround == 0) {
        PImage frame = cSprite[directionFrames()[curFrame]];
        curFrame = (curFrame + 1) % directionFrames().length;
        lastFrame = frame;
        if (direction == Direction.LEFT)
          xPos -= xSpeed;
        else
          xPos += xSpeed;
        return frame;
      } else {
        // If we are turning around, keep track of FRONT/BACK turningAroundFrameNum
        // current frame.  Once we hit turningAroundFrameNum == frames.length, then
        // unset turningAround to 0 and set direction = nextDirection
        PImage frame = cSprite[turningAroundFrames()[turningAroundFrameNum]];
        turningAroundFrameNum = turningAroundFrameNum + 1;
        if (turningAroundFrameNum == turningAroundFrames().length) {
          turningAroundFrameNum = 0;
          turningAround = 0;
          direction = nextDirection;
        }
        lastFrame = frame;
        return frame;
      }
    }

    /**
     * Have the chick turn around.  Only has an effect if chick is already
     * heading LEFT or RIGHT.
     */
    public void changeDirection() {
      //if (direction != Direction.LEFT || direction != Direction.RIGHT)
      //  return;
      turningAround = 1;  // 1 for front, 2 for back.
      if (direction == Direction.LEFT)
        nextDirection = Direction.RIGHT;
      else
        nextDirection = Direction.LEFT;
    }

    public int[] directionFrames() {
      switch (direction) {
        case LEFT:
          return leftFrames;
        case RIGHT:
          return rightFrames;
        case FRONT:
          return frontFrames;
        case BACK:
          return backFrames;
        default:
          return frontFrames;
      }
    }

    public int[] turningAroundFrames() {
      if (turningAround == 1) {
        return frontFrames;
      } else {
        return backFrames;
      }
    }
  }
}

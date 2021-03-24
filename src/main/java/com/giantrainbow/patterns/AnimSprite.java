package com.giantrainbow.patterns;

import com.giantrainbow.PathUtils;
import processing.core.PImage;

import java.util.HashMap;
import java.util.Map;

import static com.giantrainbow.RainbowStudio.pApplet;

public class AnimSprite {
  static public final String SPRITE_DIR = "spritepp/";

  public PImage[] sprite;
  enum Direction {LEFT, RIGHT, FRONT, BACK, ANY, LEFTRIGHT}
  int curFrame = 0;
  int turningAround = 0;
  int turningAroundFrameNum = 0;
  PImage lastFrame;
  protected double currentFPSFrame = 0.0;
  Direction direction = Direction.FRONT;
  Direction nextDirection = Direction.LEFT;
  Direction supportedDirections;
  int previousFrame = -1;
  public boolean isEnabled = false;
  public float xPos;
  public float yPos;
  int[][] directionFrames;
  boolean hasFrontFrames = false;
  boolean hasBackFrames = true;

  // Cache and share sprite frames across all patterns.
  public static Map<String, PImage[]> spriteCache = new HashMap<String, PImage[]>();

  public AnimSprite(String filename, float xPos, float yPos, Direction supportedDirections, int[][] directionFrames) {
    this.sprite = spriteCache.get(filename);
    if (this.sprite == null) {
      this.sprite = PathUtils.loadSprite(pApplet, SPRITE_DIR + filename);
      spriteCache.put(filename, this.sprite);
    }
    this.supportedDirections = supportedDirections;
    this.xPos = xPos;
    this.directionFrames = directionFrames;
    if (directionFrames[2] != null && directionFrames[2].length > 0) {
      hasFrontFrames = true;
    }
    if (directionFrames[3] != null && directionFrames[3].length > 0) {
      hasBackFrames = true;
    }
  }

  /**
   * Enable the sprite and set an initial direction.
   */
  public void enable() {
    isEnabled = true;
    // If we support multiple directions, randomly pick LEFT or RIGHT
    // as the starting direction.  Otherwise
    if (supportedDirections == Direction.ANY ||
        supportedDirections == Direction.LEFTRIGHT) {
      // If we support ANY direction, start off by facing the camera and
      // turning around.
      if (supportedDirections == Direction.ANY) {
        direction = Direction.FRONT;
        turningAround = 1;
      } else {
        turningAround = 0;
        direction = Direction.LEFT;
      }
      if (Math.random() < 0.5f)
        nextDirection = Direction.LEFT;
      else
        nextDirection = Direction.RIGHT;
    } else {
      // If we only support a single direction, set the direction and disable turning around.
      turningAround = 0;
      nextDirection = supportedDirections;
      direction = supportedDirections;
    }
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
      PImage frame = sprite[directionFrames()[curFrame]];
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
      PImage frame = sprite[turningAroundFrames()[turningAroundFrameNum]];
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
   * Have the sprite turn around.  Only has an effect if sprite is already
   * heading LEFT or RIGHT and if the sprite supports Direction.ANY or Direction.LEFTRIGHT
   */
  public void changeDirection() {
    if (supportedDirections == Direction.ANY || supportedDirections == Direction.LEFTRIGHT) {
      turningAround = 1;  // 1 for front, 2 for back.
      if (direction == Direction.LEFT)
        nextDirection = Direction.RIGHT;
      else
        nextDirection = Direction.LEFT;
    }
  }

  public int[] directionFrames() {
    switch (direction) {
      case LEFT:
        return directionFrames[0];
      case RIGHT:
        return directionFrames[1];
      case FRONT:
        return directionFrames[2];
      case BACK:
        return directionFrames[3];
      default:
        return directionFrames[0];
    }
  }

  /**
   * Turning around frames.  If turningAround == 1, this will be the font frames.  Otherwise if
   * turningAround == 2, this will be back frames.
   *
   * @return
   */
  public int[] turningAroundFrames() {
    if (turningAround == 1) {
      return directionFrames[2];
    } else {
      return directionFrames[3];
    }
  }
}
package com.giantrainbow.patterns;

import static processing.core.PApplet.ceil;

import com.giantrainbow.PathUtils;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PImage;

@LXCategory(LXCategory.FORM)
public class FlamingoRace extends PGPixelPerfect {
  public final CompoundParameter xSpeed =
    new CompoundParameter("XSpd", 1, 20)
    .setDescription("X speed in pixels per frame");

  public final DiscreteParameter jumpWait = new DiscreteParameter("JumpW", 10, 100);
  public final DiscreteParameter fireWidth = new DiscreteParameter("FireW", 2, 14);

  private PImage[] flamingoWalk;
  private PImage[] flamingoJump;
  private PImage[] flame;
  private PImage[] fireworks;
  private PImage[] effigy;
  private PImage[] effigyBrown;
  private PImage[] desertNight;
  private PImage[] desertSky;

  protected int currentPos1 = 0;
  protected int currentPos2 = 0;
  protected boolean flamingo1Left;
  protected boolean flamingo1Right;
  protected boolean flamingo1Jump;
  protected boolean flamingo2Left;
  protected boolean flamingo2Right;
  protected boolean flamingo2Jump;
  protected int currentJumpFrame1;
  protected int currentJumpFrame2;
  protected int currentFrame1;
  protected int currentFrame2;
  protected int currentFrameFlame;
  protected int currentFireworksFrame;
  protected boolean waitingForRight1;
  protected boolean waitingForLeft1;
  protected boolean waitingForRight2;
  protected boolean waitingForLeft2;
  protected int[] flamePositions = new int[4];
  protected int flameWidth;
  protected int framesUntilJump = 80;
  protected int framesUntilJump1;
  protected int framesUntilJump2;
  // Attempt to debounce the pads.
  protected int lastNote;
  protected long lastNoteMs;
  protected int fireworksSequence;
  protected boolean fireworksSequenceLeft;
  protected int framesUntilStartInit = 30;
  protected int framesUntilStart = framesUntilStartInit;
  protected boolean debugLines = false;

  private static final String SPRITE_DIR = "spritepp/";

  public FlamingoRace(LX lx) {
    super(lx, "");
    addParameter(xSpeed);
    xSpeed.setValue(5);
    flamingoWalk = PathUtils.loadSprite(SPRITE_DIR + "Ringo2.gif");
    flamingoJump = PathUtils.loadSprite(SPRITE_DIR + "Jingo.gif");
    flame = PathUtils.loadSprite(SPRITE_DIR + "Flame.gif");
    fireworks = PathUtils.loadSprite(SPRITE_DIR + "fireworks.gif");
    effigy = PathUtils.loadSprite(SPRITE_DIR + "Effigy.gif");
    desertNight = PathUtils.loadSprite(SPRITE_DIR + "desertnightblurred.gif");
    desertSky = PathUtils.loadSprite(SPRITE_DIR + "cloudblurred.gif");
    effigyBrown = PathUtils.loadSprite(SPRITE_DIR + "EffigyBrown.gif");
    currentPos2 = imageWidth;
    waitingForRight1 = true;
    waitingForRight2 = true;
    currentFireworksFrame = -1;
    flameWidth = flame[0].width;
    flamePositions[0] = ceil(imageWidth/2.0f * 0.33f - flameWidth/2.0f);
    flamePositions[1] = ceil(imageWidth/2.0f * 0.67f - flameWidth/2.0f);
    flamePositions[2] = ceil(imageWidth/2.0f + imageWidth/2.0f * 0.33f - flameWidth/2.0f);
    flamePositions[3] = ceil(imageWidth/2.0f + imageWidth/2.0f * 0.66f - flameWidth/2.0f - 2);
    addParameter(fireWidth);
    fireWidth.setValue(8);
    addParameter(jumpWait);
    jumpWait.setValue(20);
  }

  public void resetFlamingos() {
    currentPos2 = imageWidth;
    currentPos1 = 0;
  }

  public void draw(double deltaMs) {
    boolean daytime = true;
    pg.background(0);

    try {
      if (framesUntilJump1 > 0) framesUntilJump1--;
      if (framesUntilJump2 > 0) framesUntilJump2--;
      if (framesUntilStart > 0) framesUntilStart--;

      // tile the background
      PImage background;
      if (!daytime)
        background = desertNight[0];
      else {
        background = desertSky[0];
        effigy = effigyBrown;
      }

      for (int i = 0; i < imageWidth/background.width; i++) {
        pg.image(background, i *background.width, 0);
      }
      pg.fill(255, 0, 0);
      if (framesUntilJump1 > 0)
        pg.rect(0,0, framesUntilJump1/2, 4);
      if (framesUntilJump2 > 0)
        pg.rect(imageWidth - framesUntilJump2/2, 0, framesUntilJump/2, 4);

      PImage frameImg1;
      PImage frameImg2;
      if (flamingo1Right && waitingForRight1) {
        waitingForLeft1 = true;
        waitingForRight1 = false;
        currentPos1 += xSpeed.getValue();
      } else if (flamingo1Left && waitingForLeft1) {
        waitingForRight1 = true;
        waitingForLeft1 = false;
        currentPos1 += xSpeed.getValue();
      }

      if (flamingo2Right && waitingForRight2) {
        waitingForLeft2 = true;
        waitingForRight2 = false;
        currentPos2 -= xSpeed.getValue();
      } else if (flamingo2Left && waitingForLeft2) {
        waitingForRight1= true;
        waitingForLeft1 = false;
        currentPos2 -= xSpeed.getValue();
      }

      // Reset hit detectors after we have processed them
      flamingo1Right = false;
      flamingo1Left = false;
      flamingo2Right = false;
      flamingo2Left = false;

      if (!flamingo2Jump && fireCollisionFlamingo2()) {
        currentPos2 += 1.0 * flameWidth;
      }
      if (!flamingo1Jump && fireCollisionFlamingo1())  {
        currentPos1 -= 1.0 * flameWidth;
      }
      if (flamingo1Jump) {
        currentFrame1 = currentJumpFrame1++;
        if (currentFrame1 >= flamingoJump.length) {
          flamingo1Jump = false;
        }
        currentPos1 += xSpeed.getValue() * 0.65;
      }
      if (flamingo2Jump) {
        currentFrame2 = currentJumpFrame2++;
        if (currentFrame2 >= flamingoJump.length) {
          flamingo2Jump = false;
        }
        currentPos2 -= xSpeed.getValue() * 0.65;
      }
      pg.image(effigy[0], imageWidth/2.0f - effigy[0].width/2.0f, framesUntilStart);
      if (!flamingo1Jump) {
        frameImg1 = flamingoWalk[((int)currentFrame1)%flamingoWalk.length];
      } else {
        frameImg1 = flamingoJump[((int)currentFrame1)%flamingoJump.length];
      }
      if (currentPos1 > imageWidth / 2 - frameImg1.width) {
        currentFireworksFrame = 0;
        fireworksSequence = 3;
        fireworksSequenceLeft = true;
        resetFlamingos();
      }
      if (!flamingo2Jump) {
        frameImg2 = flamingoWalk[((int)currentFrame2)%flamingoWalk.length];
      } else {
        frameImg2 = flamingoJump[((int)currentFrame2)%flamingoJump.length];
      }
      if (currentPos2 < imageWidth / 2 + frameImg2.width) {
        fireworksSequence = 3;
        fireworksSequenceLeft = false;
        currentFireworksFrame = 0;
        resetFlamingos();
      }
      PImage flameImg = flame[currentFrameFlame];
      pg.image(flameImg, flamePositions[0], 0);
      pg.image(flameImg, flamePositions[1], 0);
      pg.image(flameImg, flamePositions[2], 0);
      pg.image(flameImg, flamePositions[3], 0);
      if (currentFireworksFrame >= 0 || fireworksSequence > 0) {
        PImage fireworksImg = fireworks[currentFireworksFrame];
        int fireworksPos = 127;
        if (fireworksSequenceLeft) {
          if (fireworksSequence == 3) {
            fireworksPos = 20;
          } else if (fireworksSequence == 2) {
            fireworksPos = imageWidth/4 - fireworksImg.width/2;
          } else if (fireworksSequence == 1) {
            fireworksPos = imageWidth/2 - fireworksImg.width/2;
          }
        } else {
          if (fireworksSequence == 3) {
            fireworksPos = imageWidth - fireworksImg.width - 20;
          } else if (fireworksSequence == 2) {
            fireworksPos = imageWidth/2 + imageWidth/4 + fireworksImg.width/2;
          } else if (fireworksSequence == 1) {
            fireworksPos = imageWidth/2 - fireworksImg.width/2;
          }
        }

        pg.image(fireworksImg, fireworksPos, 0);
        currentFireworksFrame++;
        if (currentFireworksFrame == fireworks.length) {
          currentFireworksFrame = 0;
          fireworksSequence--;
          if (fireworksSequence == 0) {
            currentFireworksFrame = -1;
            framesUntilStart = framesUntilStartInit;
          }
        }
      }
      pg.image(frameImg1, currentPos1, 0);
      pg.pushMatrix();
      pg.scale(-1,1);
      pg.image(frameImg2, -currentPos2, 0);
      pg.popMatrix();

      currentFrameFlame++;
      if (currentFrameFlame >= flame.length)
        currentFrameFlame = 0;
      currentFrame1++;
      if (currentFrame1 >= flamingoWalk.length)
        currentFrame1 = 0;
      currentFrame2++;
      if (currentFrame2 >= flamingoWalk.length)
        currentFrame2 = 0;

    } catch (ArrayIndexOutOfBoundsException ex) {
      // handle race condition when reloading images.
    }
  }

  protected boolean fireCollisionFlamingo1() {
    return fireCollision(currentPos1 + flamingoWalk[0].width/2);
  }

  protected boolean fireCollisionFlamingo2() {
    // Account for negative scale above with our flamingo position.
    return fireCollision(currentPos2 - flamingoWalk[0].width/2);
  }

  protected boolean fireCollision(int flamingoPos) {
    if (debugLines) {
      pg.rect(flamingoPos, 0, 2, 30);
    }
    for (int i = 0; i < flamePositions.length; i++) {
      int pos = flamePositions[i];
      pos += flameWidth/2 - 1;
      if (debugLines) {
        pg.fill(255, 255, 0);
        pg.rect(pos, 0, 2, 30);
      }
      if (flamingoPos > pos - fireWidth.getValue() && flamingoPos < pos + fireWidth.getValue()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void noteOnReceived(MidiNoteOn note) {
    long debounceTimeMs = 50;
    int pitch = note.getPitch();
    // Pyle rollup electric drum kit
    int topLeft = 57;
    int topMiddleLeft = 50;
    int topMiddleRight = 47;
    int topRight = 51;
    int bottomLeft = 38;
    int bottomMiddle = 46;
    int bottomRight = 41;

    long now = System.currentTimeMillis();
    if (pitch == lastNote) {
      if (now - lastNoteMs < debounceTimeMs) {
        System.out.println("debounced.");
        return;
      }
    }
    lastNote = pitch;
    lastNoteMs = now;

    if (framesUntilStart > 0 || fireworksSequence > 0)
      return;

    if (pitch == topRight) {
      flamingo2Right = true;
    } else if (pitch == bottomRight) {
      flamingo2Left = true;

    } else if (pitch == topMiddleRight) {
      if (framesUntilJump2 == 0) {
        flamingo2Jump = true;
        currentJumpFrame2 = 0;
        framesUntilJump2 = (int) jumpWait.getValue();
      }
    } else if (pitch == topLeft) {
      flamingo1Left = true;
    } else if (pitch == bottomLeft) {
      flamingo1Right = true;
    } else if (pitch == topMiddleLeft) {
      if (framesUntilJump1 == 0) {
        flamingo1Jump = true;
        currentJumpFrame1 = 0;
        framesUntilJump1 = (int) jumpWait.getValue();
      }
    }
  }
}

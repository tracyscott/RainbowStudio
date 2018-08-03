package com.giantrainbow.patterns;

import static processing.core.PApplet.ceil;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXPoint;

@LXCategory(LXCategory.FORM)
public class Pong extends MidiBase {
  private int paddle1Pos;
  private int paddle1X = 2;
  private int paddle2Pos;
  private int paddle2X = 420 - 4;
  private int paddleHeight = 8;
  private int paddleWidth = 2;
  private int ballPosX;
  private int ballPosY;
  private float ballVelocityX;
  private float ballVelocityY;
  private int numPixelsPerRow;
  private int numPixelsHigh;

  public Pong(LX lx) {
    super(lx);
    numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    numPixelsHigh = ((RainbowBaseModel)lx.model).pointsHigh;

    ballPosX = numPixelsPerRow / 2;
    ballPosY = numPixelsHigh / 2;
    ballVelocityX = 1.0f;
    ballVelocityY = 1.0f;
  }

  public void run(double deltaMs) {
    // Compute ball position
    ballPosX += ballVelocityX;
    ballPosY += ballVelocityY;
    int topOfPaddle1 = paddle1Pos + paddleHeight;
    int edgeOfPaddle1 = paddle1X + paddleWidth;
    int topOfPaddle2 = paddle2Pos + paddleHeight;
    int edgeOfPaddle2 = paddle2X + paddleWidth;
    int ballSize = 2;

    if (ballPosX == paddle1X && ballPosY < topOfPaddle1 && ballPosY >= paddle1Pos) {
      ballVelocityX = -ballVelocityX;
      ballPosX = ceil(ballPosX + 2.0f * ballVelocityX);
    }

    if (ballPosX == paddle2X && ballPosY < topOfPaddle2 && ballPosY >= paddle2Pos) {
      ballVelocityX = -ballVelocityX;
      ballPosX = ceil(ballPosX + 2.0f * ballVelocityX);
    }

    if (ballPosY > numPixelsHigh) {
      ballVelocityY = -ballVelocityY;
      ballPosY = ceil(ballPosY + 2.0f * ballVelocityY);
    } else if (ballPosY < 0) {
      ballVelocityY = -ballVelocityY;
      ballPosY = ceil(ballPosY + 2.0f * ballVelocityY);
    }

    if (ballPosX > numPixelsPerRow) {
      // Right loses
      ballVelocityX = 1.0f;
      ballPosX = numPixelsPerRow / 2;
      ballPosY = numPixelsHigh / 2;
    } else if (ballPosX < 0) {
      // Left loses
      ballVelocityX = -1.0f;
      ballPosX = numPixelsPerRow / 2;
      ballPosY = numPixelsHigh / 2;
    }

    int pointNumber = 0;
    for (LXPoint p : model.points) {
      int rowNumber = pointNumber / numPixelsPerRow;  // Which row
      int columnPos = pointNumber - rowNumber * numPixelsPerRow;
      if (rowNumber < topOfPaddle1 && rowNumber >= paddle1Pos && columnPos >= paddle1X && columnPos < edgeOfPaddle1) {
        colors[p.index] = LXColor.gray(100);
      } else if (rowNumber < topOfPaddle2 && rowNumber >= paddle2Pos && columnPos >= paddle2X && columnPos < edgeOfPaddle2) {
        colors[p.index] = LXColor.gray(100);
      } else if (rowNumber >= ballPosY && rowNumber < ballPosY + ballSize && columnPos >= ballPosX && columnPos < ballPosX + ballSize) {
        colors[p.index] = LXColor.gray(100);
      } else if (rowNumber == numPixelsHigh - 1 || rowNumber == 0) {
        colors[p.index] = LXColor.gray(40);
      } else {
        colors[p.index] = 0xff000000;
      }
      pointNumber++;
    }
  }

  @Override
  public void noteOnReceived(MidiNoteOn note) {
    int pitch = note.getPitch();
    // Pyle rollup electric drum kit
    int topLeft = 57;
    int topMiddleLeft = 50;
    int topMiddleRight = 47;
    int topRight = 51;
    int bottomLeft = 38;
    int bottomMiddle = 46;
    int bottomRight = 41;

    int bar = 0;
    if (pitch == topRight) {
      paddle2Pos++;
      if (paddle2Pos > 30 - paddleHeight) {
        paddle2Pos--;
      }
    } else if (pitch == bottomRight) {
      paddle2Pos--;
      if (paddle2Pos < 0) {
        paddle2Pos = 0;
      }
    }

    if (pitch == topLeft) {
      paddle1Pos++;
      if (paddle1Pos > 30 - paddleHeight) {
        paddle1Pos--;
      }
    } else if (pitch == bottomLeft) {
      paddle1Pos--;
      if (paddle1Pos < 0) {
        paddle1Pos = 0;
      }
    }
    // Necessary to call for MIDI Through note forwarding.
    super.noteOnReceived(note);
  }

  @Override
  public void noteOffReceived(MidiNote note) {
    super.noteOffReceived(note);
  }
}

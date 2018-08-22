package com.giantrainbow.patterns;

import static processing.core.PApplet.ceil;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import java.util.logging.Logger;

@LXCategory(LXCategory.FORM)
public class Pong extends MidiBase {
  private static final Logger logger = Logger.getLogger(Pong.class.getName());
  CompoundParameter ballSpeedKnob = new CompoundParameter("ballSpd", 0.5, 0.1, 3.0);

  int paddle1Pos;
  int paddle1X = 10;
  int paddle2Pos;
  int paddle2X = 420 - 12;
  int paddleHeight = 8;
  int paddleWidth = 2;
  float ballPosX;
  float ballPosY;
  float ballVelocityX;
  float ballVelocityY;
  int numPixelsPerRow;
  int numPixelsHigh;
  int playerOneWins;
  int playerTwoWins;

  public Pong(LX lx) {
    super(lx);
    addParameter(ballSpeedKnob);
    ballSpeedKnob.setValue(0.5);
    numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    numPixelsHigh = ((RainbowBaseModel)lx.model).pointsHigh;

    ballPosX = numPixelsPerRow / 2;
    ballPosY = numPixelsHigh / 2;
    ballVelocityX = ballSpeedKnob.getValuef();
    ballVelocityY = ballSpeedKnob.getValuef();
    playerOneWins = 0;
    playerTwoWins = 0;
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

    if (ballPosX > paddle1X - 1.0 && ballPosX < paddle1X + 1.0 && ballPosY < topOfPaddle1 + 1.0 && ballPosY >= paddle1Pos - 1.0) {
      ballVelocityX = -ballVelocityX;
      ballPosX = ballPosX + 2.0f * ballVelocityX;
    }

    if (ballPosX > paddle2X - 1.0 && ballPosX < paddle2X + 1.0 && ballPosY < topOfPaddle2 + 1.0 && ballPosY >= paddle2Pos - 1.0) {
      ballVelocityX = -ballVelocityX;
      ballPosX = ballPosX + 2.0f * ballVelocityX;
    }

    if (ballPosY >= numPixelsHigh - 2) {
      ballVelocityY = -ballVelocityY;
      ballPosY = ballPosY + 2.0f * ballVelocityY;
    } else if (ballPosY <= 0) {
      ballVelocityY = -ballVelocityY;
      ballPosY = ballPosY + 2.0f * ballVelocityY;
    }

    if (ballPosX > numPixelsPerRow) {
      // Right loses
      ballVelocityX = ballSpeedKnob.getValuef();
      ballPosX = numPixelsPerRow / 2;
      ballPosY = numPixelsHigh / 2;
      playerOneWins++;
    } else if (ballPosX < 0) {
      // Left loses
      ballVelocityX = -1.0f * ballSpeedKnob.getValuef();
      ballPosX = numPixelsPerRow / 2;
      ballPosY = numPixelsHigh / 2;
      playerTwoWins++;
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
      drawScore(p, rowNumber, columnPos);

      if (playerTwoWins == 3 || playerOneWins == 3) {
        getChannel().goNext();
        playerOneWins = 0;
        playerTwoWins = 0;
      }
      pointNumber++;
    }
  }

  public void drawScore(LXPoint p, int rowPos, int colPos) {
     if (playerOneWins >= 2) {
       if (rowPos >= 24 && rowPos < 28 && colPos >= 210 - 24 && colPos < 210 - 20) {
         colors[p.index] = LXColor.rgba(255, 0, 0, 255);
       }
     }
     if (playerOneWins >= 1) {
       if (rowPos >= 24 && rowPos < 28 && colPos >= 210 - 32 && colPos < 210 - 28) {
         colors[p.index] = LXColor.rgba(0, 255, 0, 255);
       }
     }

    if (playerTwoWins >= 2) {
      if (rowPos >= 24 && rowPos < 28 && colPos < 234 && colPos >= 230) {
        colors[p.index] = LXColor.rgba(255, 0, 0, 255);
      }
    }
    if (playerTwoWins >= 1) {
      if (rowPos >= 24 && rowPos < 28 && colPos < 242 && colPos >= 238) {
        colors[p.index] = LXColor.rgba(0, 255, 0, 255);
      }
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

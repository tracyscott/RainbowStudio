package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;

import java.util.logging.Logger;

@LXCategory(LXCategory.TEST)
public class ExPanelTest extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(ExPanelTest.class.getName());

  private int curBlockPanel;
  private int curBlockPosX;
  private int curBlockPosY;
  private long currentPanelTestFrame;

  public ExPanelTest(LX lx) {
    super(lx, null);
  }

  public void draw(double deltaDrawMs) {
    pg.noSmooth();
    pg.background(255);
    int panelWidth = 15;
    int numPanels = ((RainbowBaseModel) lx.model).pointsWide / panelWidth;
    int panelHeight = 30;

    int curFillPanel = (int)currentFrame / 80 % 28;
    for (int curPanel = 0; curPanel < numPanels; curPanel++) {
      // Draw a 15x30 rect of gray, alternating intensity between 30 and 60
      //pg.noStroke();
      pg.strokeWeight(1);
      if (curPanel % 2 == 1) {
        if ((int)currentFrame/20 % 3 == 0)
          pg.stroke(255,0,0);
        else if ((int)currentFrame/20 % 3 == 1)
          pg.stroke(0,255,0);
        else if ((int)currentFrame/20 % 3 == 2)
          pg.stroke(0, 0, 255);
        //pg.stroke(255,0, 0);
        if (curPanel == 27)
          pg.stroke(255, 0, 255);
        pg.fill(30);
      } else {
        if ((int)currentFrame/20 % 3 == 0)
          pg.stroke(0,255,0);
        else if ((int)currentFrame/20 % 3 == 1)
          pg.stroke(0,0,255);
        else if ((int)currentFrame/20 % 3 == 2)
          pg.stroke(255, 0, 0);

        if (curPanel == 0)
          pg.stroke(255, 255, 0);
        //pg.stroke(0,255,0);
        pg.fill(60);
      }
      if (curPanel == curFillPanel) {
        if ((int)currentFrame/20 % 4 == 0) {
          pg.stroke(255,0,0);
          pg.fill(255, 0, 0);
        } else if ((int)currentFrame/20 % 4 == 1) {
          pg.stroke(0,255,0);
          pg.fill(0, 255, 0);
        } else if ((int)currentFrame/20 % 4 == 2) {
          pg.stroke(0, 0, 255);
          pg.fill(0, 0, 255);
        } else if ((int)currentFrame/20 % 4 == 3) {
          pg.stroke(255);
          pg.fill(255);
        }

        // Colored block debugger.
        //pg.rect(curBlockPosX + curBlockPanel * panelWidth, curBlockPosY, 2, 2);

        //pg.stroke(255,0, 0);
        //pg.fill(255);
        //pg.stroke(255);
      }

      pg.rect(curPanel * panelWidth, 0, panelWidth - 1, panelHeight - 1);

      // For ExPanelTest, instead of rendering a number we need to render some panel #
      // encoding into the first 200 pixels of a universe.  We should also differentiate
      // between the first half of the panel and the second half of the panel somehow.
      // Let's draw a 3x3 block for every 5 and a 2x1 pixel bar for each 1 with 1 space in between.
      // So panel 27 would be 5 3x3 blocks and 2 2x1 pixel bars so 15 + 4 = 19 + 6 spaces = 25 pixels.
      int numOfFives = curPanel / 5;
      int remOfFives = curPanel % 5;
      pg.strokeWeight(0);
      for (int i = 0; i < numOfFives; i++) {
        if (i % 2 == 1) pg.fill(255, 0, 0);
        else pg.fill(0, 255, 0);
        pg.rect(1 + curPanel * 15, 1 + i * 3, 3, 3);
      }
      pg.strokeWeight(0);
      for (int i = 0; i < remOfFives; i++) {
        if (i % 2 == 1) pg.fill(255, 0, 0);
        else pg.fill(0, 255, 0);
        pg.rect(1 + curPanel*15, 19 + i * 3, 2, 1);
      }

      // Now do it for second input

      for (int i = 0; i < numOfFives; i++) {
        if (i % 2 == 1) pg.fill(255, 0, 0);
        else pg.fill(0, 0, 255);
        pg.rect(9 + curPanel * 15, 1 + i * 3, 3, 3);
      }
      pg.strokeWeight(0);
      for (int i = 0; i < remOfFives; i++) {
        if (i % 2 == 1) pg.fill(255, 0, 0);
        else pg.fill(255, 0, 0);
        pg.rect(9 + curPanel*15, 19 + i * 3, 2, 1);
      }

      /*
      pg.fill(255);
      pg.stroke(255);
      int fontSize = 16;
      pg.textSize(fontSize);
      int curPanel01 = curPanel % 10;
      int curPanel10 = curPanel / 10;
      pg.text(""+curPanel01, curPanel * panelWidth + 2, fontSize + 12);
      if (curPanel10 > 0)
        pg.text(""+curPanel10, curPanel * panelWidth + 2, fontSize - 2);
        */

    }
    /*
    if (currentPanelTestFrame%80 == 79) {
      curBlockPosY += 3;
      if (curBlockPosY >= panelHeight -1) {
        curBlockPosX += 3;
        curBlockPosY = 0;
      }
      if (curBlockPosX >= panelWidth) {
        curBlockPosX = 0;
        curBlockPanel++;
      }
      if (curBlockPanel > 29) {
        curBlockPanel = 0;
      }
      logger.info("Block coords panel=" + curBlockPanel + " x=" + curBlockPosX + " y=" + curBlockPosY);
    }
    */
    currentPanelTestFrame++;
  }


}

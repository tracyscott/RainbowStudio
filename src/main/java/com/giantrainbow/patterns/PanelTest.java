package com.giantrainbow.patterns;

import heronarts.lx.LXCategory;
import heronarts.p3lx.P3LX;
import java.util.logging.Logger;

/**
 * PanelTest.  Generate numbered sequences on panels to help debug
 * any physical wiring issues.
 */
@LXCategory(LXCategory.TEST)
public class PanelTest extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(PanelTest.class.getName());

  private int curBlockPanel;
  private int curBlockPosX;
  private int curBlockPosY;
  private long currentPanelTestFrame;

  public PanelTest(P3LX lx) {
    super(lx, null);
    curBlockPosX = 0;
    curBlockPosY = 0;
    curBlockPanel = 0;
    currentPanelTestFrame = 0;
  }

  public void draw(double deltaDrawMs) {
    pg.noSmooth();
    pg.background(255);
    int numPanels = 28;
    int panelWidth = 15;
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
        pg.fill(70);
      } else {
        if ((int)currentFrame/20 % 3 == 0)
          pg.stroke(0,255,0);
        else if ((int)currentFrame/20 % 3 == 1)
          pg.stroke(0,0,255);
        else if ((int)currentFrame/20 % 3 == 2)
          pg.stroke(255, 0, 0);
        //pg.stroke(0,255,0);
        pg.fill(120);
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
        pg.rect(curBlockPosX + curBlockPanel * panelWidth, curBlockPosY, 2, 2);

        //pg.stroke(255,0, 0);
        //pg.fill(255);
        //pg.stroke(255);
      }

      pg.rect(curPanel * panelWidth, 0, panelWidth - 1, panelHeight - 1);


      pg.stroke(255);
      pg.fill(255);
      int fontSize = 16;
      pg.textSize(fontSize);
      int curPanel01 = curPanel % 10;
      int curPanel10 = curPanel / 10;
      pg.text(""+curPanel01, curPanel * panelWidth + 2, fontSize + 12);
      if (curPanel10 > 0)
        pg.text(""+curPanel10, curPanel * panelWidth + 2, fontSize - 2);
    }
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
    currentPanelTestFrame++;
  }
}

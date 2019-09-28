package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;

import java.util.logging.Logger;

@LXCategory(LXCategory.TEST)
public class ExBridgeTest extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(ExBridgeTest.class.getName());

  public enum PanelIdType {
    NUM, BLOCK;
  };

  private final BooleanParameter cycle = new BooleanParameter("cycle", true).setDescription("Toggle color test cycle");
  private final BooleanParameter panelBorders = new BooleanParameter("borders", true)
      .setDescription("Toggle panel borders");
  private final BooleanParameter panelIds = new BooleanParameter("ids", false)
      .setDescription("Toggle panel IDs");
  private final EnumParameter<PanelIdType> panelIdType = new EnumParameter<PanelIdType>("idType", PanelIdType.NUM)
      .setDescription("Toggle panel IDs as numbers or blocks ((block = 5, bar = 1)");

  private int panelWidth = 15;
  private int panelHeight = 30;
  private int numPanels;

  public ExBridgeTest(LX lx) {
    super(lx, null);

    numPanels = ((RainbowBaseModel) lx.model).pointsWide / panelWidth;

    addParameter(cycle);
    addParameter(panelBorders);
    addParameter(panelIds);
    addParameter(panelIdType);
  }

  public void draw(double deltaDrawMs) {
    pg.noSmooth();
    pg.background(0);

    if (cycle.getValueb()) {
      drawColorCycle();
    }

    if (panelBorders.getValueb()) {
      drawPanelBorders();
    }

    if (panelIds.getValueb()) {
      switch (panelIdType.getEnum()) {
      case NUM:
        drawPanelNumbers();
        break;
      case BLOCK:
        drawPanelIds();
        break;
      }
    }
  }

  private void drawColorCycle() {
    pg.pushStyle();
    pg.noStroke();

    if ((int) currentFrame / 20 % 4 == 0) {
      pg.fill(255, 0, 0);
    } else if ((int) currentFrame / 20 % 4 == 1) {
      pg.fill(0, 255, 0);
    } else if ((int) currentFrame / 20 % 4 == 2) {
      pg.fill(0, 0, 255);
    } else if ((int) currentFrame / 20 % 4 == 3) {
      pg.fill(255);
    }

    pg.rect(0, 0, pg.width - 1, pg.height - 1);
    pg.popStyle();
  }

  private void drawPanelBorders() {
    pg.pushStyle();
    pg.noFill();

    if ((int) currentFrame / 20 % 4 == 0) {
      pg.stroke(0, 255, 0);
    } else if ((int) currentFrame / 20 % 4 == 1) {
      pg.stroke(0, 0, 255);
    } else if ((int) currentFrame / 20 % 4 == 2) {
      pg.stroke(255);
    } else if ((int) currentFrame / 20 % 4 == 3) {
      pg.stroke(255, 0, 0);
    }

    for (int curPanel = 0; curPanel < numPanels; curPanel++) {
      pg.rect(curPanel * panelWidth, 0, panelWidth - 1, panelHeight - 1);
    }

    pg.popStyle();
  }

  private void drawPanelIds() {
    pg.pushStyle();

    for (int curPanel = 0; curPanel < numPanels; curPanel++) {
      // For ExPanelTest, instead of rendering a number we need to render some panel #
      // encoding into the first 200 pixels of a universe. We should also
      // differentiate
      // between the first half of the panel and the second half of the panel somehow.
      // Let's draw a 3x3 block for every 5 and a 2x1 pixel bar for each 1 with 1
      // space in between.
      // So panel 27 would be 5 3x3 blocks and 2 2x1 pixel bars so 15 + 4 = 19 + 6
      // spaces = 25 pixels.
      int numOfFives = curPanel / 5;
      int remOfFives = curPanel % 5;
      for (int i = 0; i < numOfFives; i++) {
        if (i % 2 == 1)
          pg.fill(255, 0, 0);
        else
          pg.fill(0, 255, 0);
        pg.rect(1 + curPanel * panelWidth, 1 + i * 3, 3, 3);
      }
      pg.strokeWeight(0);
      for (int i = 0; i < remOfFives; i++) {
        if (i % 2 == 1)
          pg.fill(255, 0, 0);
        else
          pg.fill(0, 255, 0);
        pg.rect(1 + curPanel * panelWidth, 19 + i * 3, 2, 1);
      }

      // Now do it for second input
      for (int i = 0; i < numOfFives; i++) {
        if (i % 2 == 1)
          pg.fill(255, 0, 0);
        else
          pg.fill(0, 0, 255);
        pg.rect(9 + curPanel * panelWidth, 1 + i * 3, 3, 3);
      }
      pg.strokeWeight(0);
      for (int i = 0; i < remOfFives; i++) {
        if (i % 2 == 1)
          pg.fill(255, 0, 0);
        else
          pg.fill(255, 0, 0);
        pg.rect(9 + curPanel * panelWidth, 19 + i * 3, 2, 1);
      }

    }
    pg.popStyle();
  }

  private void drawPanelNumbers() {
    pg.pushStyle();
    pg.noStroke();
    int fontSize = 16;
    pg.textSize(fontSize);

    if ((int) currentFrame / 20 % 4 == 0) {
      pg.fill(0, 255, 0);
    } else if ((int) currentFrame / 20 % 4 == 1) {
      pg.fill(0, 0, 255);
    } else if ((int) currentFrame / 20 % 4 == 2) {
      pg.fill(255);
    } else if ((int) currentFrame / 20 % 4 == 3) {
      pg.fill(255, 0, 0);
    }

    for (int curPanel = 0; curPanel < numPanels; curPanel++) {
      int curPanel01 = curPanel % 10;
      int curPanel10 = curPanel / 10;
      pg.text("" + curPanel01, curPanel * panelWidth + 2, fontSize + 12);
      if (curPanel10 > 0) {
        pg.text("" + curPanel10, curPanel * panelWidth + 2, fontSize - 2);
      }
    }
    pg.popStyle();
  }

}

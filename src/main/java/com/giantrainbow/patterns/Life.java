package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import java.util.concurrent.ThreadLocalRandom;

public class Life extends PGPixelPerfect {

  protected int grid[][];
  int rows = 7;
  int cols = 105;
  protected int gridColors[][];

  BooleanParameter resetBtn = new BooleanParameter("reset", false);
  CompoundParameter densityKnob = new CompoundParameter("density", 0.4f, 0.1f, 1.0f);

  public Life(LX lx) {
    super(lx, "");

    fpsKnob.setValue(3);
    paletteKnob.setValue(1);
    hue.setValue(0.42);
    addParameter(resetBtn);
    addParameter(densityKnob);
    addParameter(paletteKnob);
    addParameter(randomPaletteKnob);
    addParameter(hue);
    addParameter(saturation);
    addParameter(bright);

    resetBtn.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) {
        randomInit();
      }
    });
    bindRandomPalette();
    randomInit();
  }

  public void draw(double deltaDrawMs) {
    pg.background(0);
    iterate();
    pg.fill(255);
    pg.noStroke();
    drawGrid();
  }

  protected void drawGrid() {
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        if (grid[i][j] == 1) {
          pg.fill(gridColors[i][j]);
          pg.rect(j * 4, i * 4, 4, 4);
        }
      }
    }
  }

  protected void randomInit() {
    grid = new int[rows][cols];
    gridColors = new int[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        if (Math.random() < densityKnob.getValuef()) {
          grid[i][j] = 1;
          int paletteIndex = ThreadLocalRandom.current().nextInt(0, palette.length);
          gridColors[i][j] = getNewRGB(paletteIndex);
        } else {
          grid[i][j] = 0;
        }
      }
    }
  }

  void iterate() {
    int[][] nextGrid = new int[rows][cols];

    for (int l = 1; l < rows - 1; l++) {
      for (int m = 1; m < cols - 1; m++) {
        // number of neighbors alive
        int neighborSum = 0;
        for (int i = -1; i <= 1; i++)
          for (int j = -1; j <= 1; j++)
            neighborSum += grid[l + i][m + j];

        // current cell needs to be subtracted from
        // the sum because it was counted in loop above
        neighborSum -= grid[l][m];

        // too few neighbors, die.
        if ((grid[l][m] == 1) && (neighborSum < 2))
          nextGrid[l][m] = 0;
        // overpopulation.
        else if ((grid[l][m] == 1) && (neighborSum > 3))
          nextGrid[l][m] = 0;
        // new cell
        else if ((grid[l][m] == 0) && (neighborSum == 3)) {
          nextGrid[l][m] = 1;
          gridColors[l][m] = getNewRGB();
        } else
          nextGrid[l][m] = grid[l][m];
      }
    }
    grid = nextGrid;
  }

  @Override
  public void onActive() {
    super.onActive();
    randomInit();
  }
}

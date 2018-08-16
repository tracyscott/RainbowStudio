/*
 * Created by shawn on 8/15/18 4:31 PM.
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.colors.Colors.BLACK;
import static processing.core.PApplet.lerp;
import static processing.core.PConstants.P2D;

import com.giantrainbow.colors.ColorRainbow;
import com.giantrainbow.colors.Colors;
import com.google.common.collect.EvictingQueue;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Game of Life implementation.
 *
 * @author Shawn Silverman
 * @link <a href="https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life">Conway's Game of Life</a>
 */
@LXCategory(LXCategory.FORM)
public class GameOfLife extends P3PixelPerfectBase {
  private static final Logger logger = Logger.getLogger(GameOfLife.class.getName());

  private static final float COLOR_CHANGE_TIME = 5.0f;

  /** The maximum grid update rate. */
  private static final float MAX_UPDATE_RATE = 40;

  /**
   * Time to maintain the game after detecting a repeat. This should be a multiple of
   * 1/{@link #MAX_UPDATE_RATE}.
   */
  private static final float MAINTAIN_TIME = 4.0f;

  private static final float SCALE = 4.0f;  // Global size scale factor
  private static final float MAX_RING_MULT = 2.0f;

  /** For tracking repeats so we can reset. */
  private static final int HISTORY_SIZE = 6;

  private Grid grid;
  private ColorRainbow rainbow = new ColorRainbow(
      new ColorRainbow.NextArrayColor(Colors.RAINBOW_PALETTE, COLOR_CHANGE_TIME, true));
  private EvictingQueue<int[]> history = EvictingQueue.create(HISTORY_SIZE);

  /** Keeps track of the update time. */
  private double updateTimer;

  /** Keeps track of the time during the MAINTAIN state. */
  private float maintainTimer;

  /**
   * For keeping track of wheher we are running or displaying for a while after detecting
   * a repeat.
   */
  private enum State {
    RUNNING,
    MAINTAIN,
  }
  private State state;

  // Parameters
  private final BooleanParameter resetBtn =
      new BooleanParameter("Reset").setMode(BooleanParameter.Mode.MOMENTARY)
          .setDescription("Reset the game");
  private final BooleanParameter ringModeBtn =
      new BooleanParameter("Ring Mode", true)
          .setDescription("Draw with ring mode");
  private final CompoundParameter ringSizeKnob =
      new CompoundParameter("Circle Size", 0.5f, 0.0f, 1.0f)
          .setDescription("Changes the circle size");

  public GameOfLife(LX lx) {
    super(lx, P2D);

    grid = new Grid((int) (pg.width/SCALE), (int) (pg.height/SCALE), false);
    resetBtn.addListener(lxParameter -> {
      if (((BooleanParameter) lxParameter).isOn()) {
        grid.randomize();
      }
    });

    addParameter(resetBtn);
    addParameter(ringModeBtn);
    addParameter(ringSizeKnob);
  }

  @Override
  protected void setup() {
    fpsKnob.setValue(GLOBAL_FRAME_RATE);

    rainbow.reset(fpsKnob.getValuef());
    reset();
    state = State.RUNNING;
  }

  @Override
  protected void tearDown() {
    history.clear();
  }

  /** Resets all the state back to a starting point. */
  private void reset() {
    grid.randomize();
    history.clear();
    updateTimer = Float.POSITIVE_INFINITY;
  }

  @Override
  protected void draw(double deltaDrawMs) {
    updateTimer += deltaDrawMs;
    maintainTimer += deltaDrawMs;
    float updateRate = lerp(0.0f, MAX_UPDATE_RATE, speedKnob.getValuef());
    if (updateTimer < 1000.0f/updateRate) {
      return;
    }
    updateTimer = 0.0;

    // Draw the current state

    pg.background(BLACK);
    int c = rainbow.get(pg, updateRate);
    if (ringModeBtn.isOn()) {
      pg.stroke(c);
      pg.noFill();
    } else {
      pg.noStroke();
      pg.fill(c);
    }

    float drawMult = lerp(1.0f, MAX_RING_MULT, ringSizeKnob.getValuef());
    for (int y = grid.height(); --y >= 0; ) {
      for (int x = grid.width(); --x >= 0; ) {
        if (grid.get(x, y)) {
          pg.ellipse(x*SCALE, y*SCALE, SCALE*drawMult, SCALE*drawMult);
        }
      }
    }

    // Either running or maintaining

    switch (state) {
      case RUNNING:
        // Check for repeats
        int repeatIndex = -1;
        boolean foundRepeat = false;
        for (int[] g : history) {
          repeatIndex++;
          if (Arrays.equals(grid.getCurrent(), g)) {
            foundRepeat = true;
            break;
          }
        }
        if (foundRepeat) {
          logger.info("Found a repeat at: " + -(history.size() - repeatIndex));
          state = State.MAINTAIN;
          maintainTimer = 0.0f;
        } else {
          history.add(grid.getCurrent().clone());
          grid.update();
        }
        break;

      case MAINTAIN:
        if (maintainTimer >= MAINTAIN_TIME * 1000.0) {
          reset();
          state = State.RUNNING;
        } else {
          grid.update();
        }
        break;
    }
  }

  /**
   * Represents a Game of Life rectangular grid.
   */
  private static final class Grid {
    private final int width;
    private final int height;
    private final boolean wrap;
    private final int[][] states;  // 1 for alive and 0 for dead

    private int whichGrid;

    /**
     * Creates the grid with random states. If {@code wrap} is {@code true}
     * then the sides are wrapped to the opposite sides, resulting in a toroid.
     * If it is {@code false} then it is assumed that everything outside the
     * edges is dead.
     */
    Grid(int width, int height, boolean wrap) {
      width += 2;
      height += 2;
      this.width = width;
      this.height = height;
      this.wrap = wrap;

      states = new int[2][width * height];
      randomize();

      whichGrid = 0;
    }

    /**
     * Randomizes the grid.
     */
    void randomize() {
      // Fill only the middle with random values
      int[] grid = getCurrent();
      int index = width;  // Start at (0, 1)
      for (int y = 1; y < height - 1; y++) {
        index++;  // Start at (1, y)
        for (int x = 1; x < width - 1; x++) {
          grid[index] = random.nextBoolean() ? 1 : 0;
          index++;
        }
        index++;  // Skip the last point in the row
      }
    }

    int width() {
      return width - 2;
    }

    int height() {
      return height - 2;
    }

    boolean get(int x, int y) {
      return getCurrent()[(y + 1)*width + (x + 1)] != 0;
    }

    /**
     * Returns a reference to the current grid.
     */
    int[] getCurrent() {
      return states[whichGrid];
    }

    // Updates the cells
    void update() {
      int[] thisGrid = states[whichGrid];
      int[] nextGrid = states[whichGrid ^= 1];

      if (wrap) {
        // Fill the edges with their wrapped counterparts
        System.arraycopy(thisGrid, thisGrid.length - 2*width + 1, thisGrid, 1, width - 2);
        System.arraycopy(thisGrid, width + 1, thisGrid, thisGrid.length - width + 1, width - 2);
        int index = width;
        for (int y = 1; y < height - 1; y++) {
          thisGrid[index] = thisGrid[index + width - 2];
          thisGrid[index + width - 1] = thisGrid[index + 1];
          index += width;
        }
//        System.out.println("this[");
//        System.out.println(toString(thisGrid));
//        System.out.println("]");

        // Corners
        thisGrid[0] = thisGrid[thisGrid.length - width - 2];
        thisGrid[width - 1] = thisGrid[thisGrid.length - 2*width + 1];
        thisGrid[thisGrid.length - width] = thisGrid[2*width - 2];
        thisGrid[thisGrid.length - 1] = thisGrid[width + 1];
      } else {
        // Don't have to do anything here; the edges are already filled with zero
      }

      // Update the middle
      int index = width;  // Start at (0, 1)
      for (int y = 1; y < height - 1; y++) {
        index++;  // Start at (1, y)
        for (int x = 1; x < width - 1; x++) {
          int count =
              thisGrid[index + 1]
              + thisGrid[index - width + 1]
              + thisGrid[index - width]
              + thisGrid[index - width - 1]
              + thisGrid[index - 1]
              + thisGrid[index + width - 1]
              + thisGrid[index + width]
              + thisGrid[index + width + 1];
          nextGrid[index] = nextState(thisGrid[index], count);
          index++;
        }
        index++;  // Skip the last point in the row
      }
    }

    private int nextState(int currState, int count) {
      if (currState == 0) {  // Dead
        if (count == 3) {
          return 1;
        }
      } else {  // Alive
        if (count < 2 || 3 < count) {
          return 0;
        }
      }
      return currState;
    }

    /**
     * To assist in debugging.
     */
    private String toString(int[] grid) {
      StringBuilder buf = new StringBuilder();
      int index = 0;
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          buf.append(' ').append(grid[index++]);
        }
        buf.append('\n');
      }
      return buf.toString();
    }
  }
}

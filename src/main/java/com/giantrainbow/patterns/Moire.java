/*
 * Created by shawn on 8/4/18 1:27 PM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.RainbowStudio.inputManager;
import static com.giantrainbow.colors.Colors.BLACK;
import static processing.core.PApplet.map;
import static processing.core.PApplet.sqrt;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.RADIUS;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.colors.ColorRainbow;
import com.giantrainbow.colors.Colors;
import com.giantrainbow.input.InputManager;
import com.giantrainbow.input.LowPassFilter;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BooleanParameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a Moire pattern with intersecting concentric circles or lines, emanating
 * from a point source.
 *
 * @author Shawn Silverman
 */
@LXCategory(LXCategory.FORM)
public class Moire extends PGPixelPerfect {
  private static final int POINT_COUNT = 4;
  private static final int RADIUS_PER_WIDTH = 25;

  private static final int YELLOW = 0xffffff00;

  private static final float COLOR_CHANGE_TIME = 10.0f;
  private static final int START_COLOR = YELLOW;
  private static final int LINE_COLOR = BLACK;

  private static final float MAX_RADIUS_BEAT_MULTIPLIER = 2.0f;

  private List<Point> points = new ArrayList<>();
  private float radiusInc;

  private InputManager.Beats beats;

  // Color interpolation
  private volatile ColorRainbow[] rainbows;
  private ColorRainbow[] multiRainbows = new ColorRainbow[6];
  private ColorRainbow[] solidRainbows = new ColorRainbow[1];
  {
    for (int i = 0; i < multiRainbows.length; i++) {
      multiRainbows[i] = new ColorRainbow(
          new ColorRainbow.NextArrayColor(Colors.RAINBOW_PALETTE, COLOR_CHANGE_TIME, false, i));
    }
    solidRainbows[0] = new ColorRainbow(
        new ColorRainbow.NextArrayColor(Colors.RAINBOW2_PALETTE, COLOR_CHANGE_TIME, true));
  }

  private final BooleanParameter solidToggle =
      new BooleanParameter("Solid", false)
          .setDescription("Solid color or rainbow");

  public Moire(LX lx) {
    super(lx, P2D);

    addParameter(solidToggle);
    solidToggle.addListener(lxParameter -> {
          if (((BooleanParameter) lxParameter).isOn()) {
            rainbows = solidRainbows;
          } else {
            rainbows = multiRainbows;
          }
        });
    this.rainbows = multiRainbows;  // draw() can be called before onActive()??
  }

  @Override
  public void setup() {
    fpsKnob.setValue(GLOBAL_FRAME_RATE);

    pg.ellipseMode(RADIUS);

    radiusInc = pg.width / RADIUS_PER_WIDTH;
    points.clear();
    for (int i = 0; i < POINT_COUNT; i++) {
      points.add(new Point());
    }

    for (ColorRainbow cr : multiRainbows) {
      cr.reset(fpsKnob.getValuef());
    }
    for (ColorRainbow cr : solidRainbows) {
      cr.reset(fpsKnob.getValuef());
    }
    this.rainbows = solidToggle.isOn()
        ? solidRainbows
        : multiRainbows;

    beats = inputManager.getBeats();
  }

  @Override
  public void tearDown() {
    points.clear();
  }

  @Override
  protected void draw(double deltaDrawMs) {
    pg.background(BLACK);
    pg.noStroke();
    ColorRainbow[] rainbows = this.rainbows;
    float h = pg.height / rainbows.length;
    for (int i = 0; i < rainbows.length; i++) {
      pg.fill(rainbows[i].get(pg, fpsKnob.getValuef()));
      pg.rect(0, i*h, pg.width, h);
    }

    pg.stroke(LINE_COLOR);
    pg.noFill();

    // TODO: Sound input
    beats = inputManager.getBeats(beats, 0);
    for (int i = 0; i < points.size(); i++) {
      float beatLevel = 0.0f;
      if (i < 3) {
        if (beats.isBeat(i)) {
          beatLevel = 0.5f;
        }
      }
      points.get(i).drawCircles(beatLevel, (float) (deltaDrawMs / 1000));
    }
  }

  final class Point {
    private float noiseX;
    private float noiseY;
    private float noiseInc;
    private long seed = random.nextLong();

    private float radiusInc;
    private LowPassFilter filter;  // For filtering radius decay

    Point() {
      radiusInc = Moire.this.radiusInc * RainbowStudio.pApplet.random(0.5f, 0.8f);
      noiseInc = 0.07f / fpsKnob.getValuef();
      fpsKnob.addListener(lxParameter -> noiseInc = 0.07f / lxParameter.getValuef());

      filter = new LowPassFilter(
          inputManager.getAudioSampleSize() / inputManager.getAudioSampleRate() * 4,
          1.0f);
    }

    void drawCircles(float beatLevel, float dt) {
      noiseX += noiseInc;
      noiseY += noiseInc;
      RainbowStudio.pApplet.noiseSeed(seed);

      float x = map(RainbowStudio.pApplet.noise(noiseX, 0), 0.0f, 1.0f, -radiusInc, pg.width + radiusInc);
      float y = map(RainbowStudio.pApplet.noise(0, noiseY), 0.0f, 1.0f, -pg.height, pg.height + pg.height);
      float maxRadius = 2.0f*sqrt(pg.width*pg.width + pg.height*pg.height);
      float radius = 0.0f;
      beatLevel = filter.next(beatLevel, dt);
      float beatMultiplier = map(beatLevel, 0.0f, 1.0f, 1.0f, MAX_RADIUS_BEAT_MULTIPLIER);
      do {
        radius += radiusInc * beatMultiplier;
        pg.ellipse(x, y, radius, radius);
      } while (radius < maxRadius);
    }
  }
}

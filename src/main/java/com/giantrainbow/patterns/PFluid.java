package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.P2D;

import com.giantrainbow.RainbowStudio;
import com.giantrainbow.colors.Colors;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import heronarts.lx.parameter.CompoundParameter;
import processing.core.PConstants;
import processing.opengl.PGraphics2D;

/**
 * Utility class for Fluid Simulation.
 */
@LXCategory(LXCategory.FORM)
public class PFluid extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(FluidPP.class.getName());

  public CompoundParameter tempKnob = new CompoundParameter("temp", 2f, -20f, 20f);
  public CompoundParameter intensityKnob = new CompoundParameter("intensity", 1f, 0.1f, 2.0f);
  public CompoundParameter radiusKnob = new CompoundParameter("radius", 15f, 1f, 40f);
  public CompoundParameter numEmitters = new CompoundParameter("emitters", 6f, 1f, 60f);
  public CompoundParameter yPosKnob = new CompoundParameter("yPos", 5f, 0f, 30f);
  public CompoundParameter xPosOffsetKnob = new CompoundParameter("xOff", 0f, -20f, 20f);

  public static class Emitter {
    float px;
    float py;
    float hsb[] = { 1f, 1f, 1f};
    float radius;
    float intensity;
    float temperature;
  }

  private class MyFluidData implements DwFluid2D.FluidData {

    // update() is called during the fluid-simulation update step.
    @Override
    public void update(DwFluid2D fluid) {
      int i = 0;
      for (Emitter emitter: emitters) {
        int color = Colors.HSBtoRGB(emitter.hsb);
        //color = Colors.RED;
        fluid.addDensity(emitter.px, emitter.py, emitter.radius, (float)Colors.red(color)/255f,
            (float)Colors.green(color)/255f, (float)Colors.blue(color)/255f, emitter.intensity);
        fluid.addTemperature(emitter.px, emitter.py, emitter.radius, emitter.temperature);
        //if (i == 1 || i == 2) {
          //logger.info("rgb= " + Colors.red(color) + " " + Colors.green(color) + " " + Colors.blue(color));
       // }

        i++;
      }
    }
  }

  private int fluidgrid_scale = 1;
  private DwFluid2D fluid;
  private PGraphics2D pg_fluid;
  private PGraphics2D pg_obstacles;
  private int     BACKGROUND_COLOR           = 0;
  private boolean UPDATE_FLUID               = true;
  private boolean DISPLAY_FLUID_TEXTURES     = true;
  private boolean DISPLAY_FLUID_VECTORS      = false;
  private int     DISPLAY_fluid_texture_mode = 0;

  protected List<Emitter> emitters = new ArrayList<Emitter>();

  public PFluid(LX lx) {
    super(lx, "");
    fpsKnob.setValue(GLOBAL_FRAME_RATE);
    addParameter(paletteKnob);
    addParameter(randomPaletteKnob);
    addParameter(hue);
    addParameter(saturation);
    addParameter(bright);
    addParameter(tempKnob);
    addParameter(intensityKnob);
    addParameter(radiusKnob);
    addParameter(numEmitters);
    addParameter(yPosKnob);
    addParameter(xPosOffsetKnob);

    DwPixelFlow context = new DwPixelFlow(RainbowStudio.pApplet);
    context.print();
    context.printGL();
    // fluid simulation
    fluid = new DwFluid2D(context, pg.width, pg.height, fluidgrid_scale);
    // set some simulation parameters
    fluid.param.dissipation_density     = 0.999f;
    fluid.param.dissipation_velocity    = 0.99f;
    fluid.param.dissipation_temperature = 0.80f;
    fluid.param.vorticity               = 0.10f;
    // interface for adding data to the fluid simulation
    MyFluidData cb_fluid_data = new MyFluidData();
    fluid.addCallback_FluiData(cb_fluid_data);
    // pgraphics for fluid
    pg_fluid = (PGraphics2D) RainbowStudio.pApplet.createGraphics(pg.width, pg.height, P2D);
    pg_fluid.smooth(4);
    pg_fluid.beginDraw();
    pg_fluid.background(BACKGROUND_COLOR);
    pg_fluid.endDraw();
    // pgraphics for obstacles
    pg_obstacles = (PGraphics2D) RainbowStudio.pApplet.createGraphics(pg.width, pg.height, P2D);
    pg_obstacles.smooth(0);
    pg_obstacles.beginDraw();
    pg_obstacles.clear();
    // border-obstacle
    pg_obstacles.strokeWeight(1);
    pg_obstacles.stroke(100);
    pg_obstacles.noFill();
    pg_obstacles.rect(0, 0, pg_obstacles.width, pg_obstacles.height);
    pg_obstacles.endDraw();

    for (int i = 0; i < numEmitters.getValuef(); i++) {
      Emitter emitter = newEmitter(i);
      emitters.add(emitter);
    }
  }

  protected Emitter newEmitter(int i) {
    Emitter emitter = new Emitter();
    emitter.px = i * pg.width / (numEmitters.getValuef() - 1f);
    if (i == 0) emitter.px = 5;
    if (i == 5) emitter.px -= 5;
    emitter.py = 5;
    emitter.radius = 15;
    emitter.intensity = 1f;
    emitter.temperature = 2f;
    return emitter;
  }

  public void draw(double deltaDrawMs) {
    pg.colorMode(PConstants.HSB, 1.0f, 1.0f, 1.0f, 1.0f);
    pg.background(0);
    for (int x = emitters.size(); x < numEmitters.getValuef(); x++) {
      emitters.add(newEmitter(x));
    }
    int extraEmitters = emitters.size() - (int)numEmitters.getValuef();
    for (int j = extraEmitters; j > 0; j--) {
      emitters.remove(emitters.size()-1);
    }
    for (int i = 0; i < emitters.size(); i++) {
      Emitter emitter = emitters.get(i);
      getNewHSB(emitter.hsb, i % palette.length);
      emitter.temperature = tempKnob.getValuef();
      emitter.intensity = intensityKnob.getValuef();
      emitter.radius = radiusKnob.getValuef();
      emitter.px = i * pg.width / (numEmitters.getValuef() - 2f);
      emitter.py = yPosKnob.getValuef();
      if (i == 0) emitter.px = 5;
      emitter.px += xPosOffsetKnob.getValuef();
    }

    fluid.addObstacles(pg_obstacles);
    fluid.update();
    // clear render target
    pg_fluid.beginDraw();
    pg_fluid.background(BACKGROUND_COLOR);
    pg_fluid.endDraw();
    fluid.renderFluidTextures(pg_fluid, DISPLAY_fluid_texture_mode);
    pg_fluid.loadPixels();
    pg_fluid.updatePixels();
    pg.image(pg_fluid, 0, 0);
    pg.loadPixels();
    pg.updatePixels();
  }
}

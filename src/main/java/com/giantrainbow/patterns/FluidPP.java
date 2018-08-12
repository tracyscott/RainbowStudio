package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.P2D;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;
import heronarts.lx.LXCategory;
import heronarts.p3lx.P3LX;
import java.util.logging.Logger;
import processing.opengl.PGraphics2D;

/**
 * Utility class for Fluid Simulation.
 */
@LXCategory(LXCategory.FORM)
public class FluidPP extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(FluidPP.class.getName());

  private class MyFluidData implements DwFluid2D.FluidData {

    // update() is called during the fluid-simulation update step.
    @Override
      public void update(DwFluid2D fluid) {

      float px, py, radius, r, g, b, intensity, temperature;

      // LGBT 6 Bands  (228,3,3) (255,140,0) (255,237,0) (0,128,38) (0,77,255) (117,7,135)
      py = 5;
      radius = 5;
      intensity = 1.0f;
      // add impulse: density + temperature
      float animator = abs(sin(fluid.simulation_step*0.01f));
      temperature = animator * 10f;

      // add impulse: density + temperature
      px = 5;
      r = 228.0f/255.0f;
      g = 3.0f/255.0f;
      b = 3.0f/255.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 1.0f * pg.width/5.0f;
      r = 255.0f/255.0f;
      g = 140.0f/255.0f;
      b = 0.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 2.0f * pg.width/5.0f;
      r = 255.0f/255.0f;
      g = 237.0f/255.0f;
      b = 0.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 3.0f * pg.width/5.0f;
      r = 0.0f;
      g = 128.0f/255.0f;
      b = 38.0f/255.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 4*pg.width/5.0f;
      r = 0.0f;
      g = 77.0f/255.0f;
      b = 1.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = pg.width - 5;
      r = 117.0f / 255.0f;
      g = 7.0f / 255.0f;
      b = 135.0f / 255.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);
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

  public FluidPP(P3LX lx) {
    super(lx, "");
    fpsKnob.setValue(GLOBAL_FRAME_RATE);
    DwPixelFlow context = new DwPixelFlow(applet);
    context.print();
    context.printGL();
    // fluid simulation
    logger.info(pg.width + "," + pg.height);
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
    pg_fluid = (PGraphics2D) applet.createGraphics(pg.width, pg.height, P2D);
    pg_fluid.smooth(4);
    pg_fluid.beginDraw();
    pg_fluid.background(BACKGROUND_COLOR);
    pg_fluid.endDraw();
    // pgraphics for obstacles
    pg_obstacles = (PGraphics2D) applet.createGraphics(pg.width, pg.height, P2D);
    pg_obstacles.smooth(0);
    pg_obstacles.beginDraw();
    pg_obstacles.clear();
    // border-obstacle
    pg_obstacles.strokeWeight(1);
    pg_obstacles.stroke(100);
    pg_obstacles.noFill();
    pg_obstacles.rect(0, 0, pg_obstacles.width, pg_obstacles.height);
    pg_obstacles.endDraw();
  }

  public void draw(double deltaDrawMs) {
    pg.background(0);
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

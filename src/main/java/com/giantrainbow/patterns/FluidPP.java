package com.giantrainbow.patterns;

/*
 * Utility class for Fluid Simulation.
 */

import static processing.core.PApplet.abs;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.P2D;

import com.giantrainbow.RainbowStudio;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.opengl.PGraphics2D;

@LXCategory(LXCategory.FORM)
public class FluidPP extends PGPixelPerfect {

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

      px = 1.0f * imageWidth/5.0f;
      r = 255.0f/255.0f;
      g = 140.0f/255.0f;
      b = 0.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 2.0f * imageWidth/5.0f;
      r = 255.0f/255.0f;
      g = 237.0f/255.0f;
      b = 0.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 3.0f * imageWidth/5.0f;
      r = 0.0f;
      g = 128.0f/255.0f;
      b = 38.0f/255.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = 4*imageWidth/5.0f;
      r = 0.0f;
      g = 77.0f/255.0f;
      b = 1.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = imageWidth - 5;
      r = 117.0f / 255.0f;
      g = 7.0f / 255.0f;
      b = 135.0f / 255.0f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);
    }
  }

  int fluidgrid_scale = 1;
  DwFluid2D fluid;
  PGraphics2D pg_fluid;
  PGraphics2D pg_obstacles;
  int     BACKGROUND_COLOR           = 0;
  boolean UPDATE_FLUID               = true;
  boolean DISPLAY_FLUID_TEXTURES     = true;
  boolean DISPLAY_FLUID_VECTORS      = false;
  int     DISPLAY_fluid_texture_mode = 0;

  public FluidPP(LX lx) {
    super(lx, "");
    fpsKnob.setValue(60);
    DwPixelFlow context = new DwPixelFlow(RainbowStudio.pApplet);
    context.print();
    context.printGL();
    // fluid simulation
    System.out.println(imageWidth + "," + imageHeight);
    fluid = new DwFluid2D(context, imageWidth, imageHeight, fluidgrid_scale);
    // set some simulation parameters
    fluid.param.dissipation_density     = 0.999f;
    fluid.param.dissipation_velocity    = 0.99f;
    fluid.param.dissipation_temperature = 0.80f;
    fluid.param.vorticity               = 0.10f;
    // interface for adding data to the fluid simulation
    MyFluidData cb_fluid_data = new MyFluidData();
    fluid.addCallback_FluiData(cb_fluid_data);
    // pgraphics for fluid
    pg_fluid = (PGraphics2D) RainbowStudio.pApplet.createGraphics(imageWidth, imageHeight, P2D);
    pg_fluid.smooth(4);
    pg_fluid.beginDraw();
    pg_fluid.background(BACKGROUND_COLOR);
    pg_fluid.endDraw();
    // pgraphics for obstacles
    pg_obstacles = (PGraphics2D) RainbowStudio.pApplet.createGraphics(imageWidth, imageHeight, P2D);
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

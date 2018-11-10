package com.giantrainbow.patterns;

import com.giantrainbow.RainbowStudio;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.flowfieldparticles.DwFlowFieldParticles;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwFlowField;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwFilter;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.Merge;
import com.thomasdiewald.pixelflow.java.utils.DwUtils;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PApplet;
import processing.opengl.PGraphics2D;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static java.lang.Math.random;
import static processing.core.PConstants.*;

/**
 * Use PixelFlow particles to generate rainbow colored exploding particles.
 */
@LXCategory(LXCategory.FORM)
public class RainbowParticles extends PGPixelPerfect {
  public RainbowParticles(LX lx) {
    super(lx, "");

    fpsKnob.setValue(GLOBAL_FRAME_RATE);

    pg_canvas = (PGraphics2D) RainbowStudio.pApplet.createGraphics(pg.width, pg.height, P2D);
    pg_canvas.smooth(0);

    pg_impulse = (PGraphics2D) RainbowStudio.pApplet.createGraphics(pg.width, pg.height, P2D);
    pg_impulse.smooth(0);

    pg_gravity = (PGraphics2D) RainbowStudio.pApplet.createGraphics(pg.width, pg.height, P2D);
    pg_gravity.smooth(0);
    pg_gravity.beginDraw();
    pg_gravity.blendMode(REPLACE);
    pg_gravity.background(0, 255, 0);
    pg_gravity.endDraw();


    pg_obstacles = (PGraphics2D) RainbowStudio.pApplet.createGraphics(pg.width, pg.height, P2D);
    pg_obstacles.smooth(0);
    pg_obstacles.beginDraw();
    pg_obstacles.clear();
    pg_obstacles.noStroke();
    pg_obstacles.blendMode(REPLACE);
    pg_obstacles.rectMode(CORNER);
    pg_obstacles.fill(0, 255);
    pg_obstacles.rect(0, 0, pg.width, pg.height);
    pg_obstacles.fill(0, 0);
    pg_obstacles.rect(2, 2, pg.width-4, pg.height-4);
    pg_obstacles.endDraw();

    boolean[] RESIZED = { false };
    pg_luminance  = DwUtils.changeTextureSize(RainbowStudio.pApplet, pg_luminance , pg.width, pg.height, 0, RESIZED);


    context = new DwPixelFlow(RainbowStudio.pApplet);
    context.print();
    context.printGL();

    ff_acc = new DwFlowField(context);
    ff_acc.param.blur_iterations = 0;
    ff_acc.param.blur_radius     = 1;

    ff_impulse = new DwFlowField(context);
    ff_impulse.param.blur_iterations = 1;
    ff_impulse.param.blur_radius     = 1;

    particles = new DwFlowFieldParticles(context, 100);
    particles.param.col_A = new float[]{0.80f, 0.10f, 0.20f, 5};
    particles.param.col_B = new float[]{0.20f, 0.05f, 0.10f, 0};
    particles.param.shader_type = 1;
    particles.param.shader_collision_mult = 0.30f;
    particles.param.steps = 1;
    particles.param.velocity_damping  = 1;
    particles.param.size_display   = 3;
    particles.param.size_collision = 3;
    particles.param.size_cohesion  = 2;
    particles.param.mul_coh = 1.00f;
    particles.param.mul_col = 2.00f;
    particles.param.mul_obs = 3.00f;

    particles.param.wh_scale_col =  0;
    particles.param.wh_scale_coh =  4;
    particles.param.wh_scale_obs =  0;

    reset();
  }

  PGraphics2D pg_canvas;
  PGraphics2D pg_obstacles;
  PGraphics2D pg_gravity;
  PGraphics2D pg_impulse;
  PGraphics2D pg_luminance;

  DwPixelFlow context;
  DwFlowFieldParticles particles;
  DwFlowField ff_acc;
  DwFlowField ff_impulse;

  public void draw(double deltaDrawMs) {
    pg.background(0);
    updateColor();

    setTimestep();
    spawnParticles();
    //addImpulse();
    // update particle simulation
    particles.update(ff_acc);


    pg_canvas.beginDraw();
    pg_canvas.background(0);
    pg_canvas.image(pg_obstacles, 0, 0);
    pg_canvas.endDraw();
    particles.displayParticles(pg_canvas);
    pg.blendMode(REPLACE);
    applyBloom();
    pg_canvas.loadPixels();
    pg_canvas.updatePixels();
    pg.image(pg_canvas, 0, 0);
    pg.blendMode(BLEND);
    pg.loadPixels();
    pg.updatePixels();
  }

  public void setTimestep(){
    particles.param.timestep = 1f/(fpsKnob.getValuef());
//    particles.param.timestep = 1f/120;
  }

  public void applyBloom() {
      DwFilter filter = DwFilter.get(context);
      filter.luminance_threshold.param.threshold = 0.3f; // when 0, all colors are used
      filter.luminance_threshold.param.exponent  = 5;
      filter.luminance_threshold.apply(pg_canvas, pg_luminance);

      filter.bloom.setBlurLayers(10);
//      filter.bloom.gaussianpyramid.setBlurLayers(10);
      filter.bloom.param.blur_radius = 1;
      filter.bloom.param.mult   = 1.2f;    //map(mouseX, 0, width, 0, 10);
      filter.bloom.param.radius = 0.1f;//map(mouseY, 0, height, 0, 1);
      filter.bloom.apply(pg_luminance, null, pg_canvas);
  }

  public void spawnParticles(){

    float px,py,vx,vy,radius;
    int count, vw, vh;
    float vel = 0f;

    vw = pg.width;
    vh = pg.height;

    count = 1;
    radius = 10;
    px = vw/2f;
    py = vh/4f;
    vx = 0;
    vy = 4;

    DwFlowFieldParticles.SpawnRadial sr = new DwFlowFieldParticles.SpawnRadial();
//    sr.num(count);
//    sr.dim(radius, radius);
//    sr.pos(px, vh-1-py);
//    sr.vel(vx, vy);
//    particles.spawn(vw, vh, sr);

    if (((int)currentFrame) % 240 == 0) {
      System.out.println("Spawning particles");
      count = RainbowStudio.pApplet.ceil(particles.getCount() * 0.0025f);
      count = 1000; //RainbowStudio.pApplet.min(RainbowStudio.pApplet.max(count, 1), 5000);

      float pr = particles.getCollisionSize() * 0.25f;
      radius = RainbowStudio.pApplet.ceil(RainbowStudio.pApplet.sqrt(count * pr * pr));
      px = pg.width/2;
      py = pg.height/2;
      vx = 10f * +vel;
      vy = 10f * -vel;

      sr.num(count);
      sr.dim(radius, radius);
      sr.pos(px, vh-1-py);
      sr.vel(vx, vy);
      System.out.println("px: " + px + " py:" + py + " vx:" + vx + " vy:" + vy + " vw: " + vw + " vh:" + vh);
      particles.spawn(vw, vh, sr);
    }
  }

  public void reset() {
    System.out.println("Resetting particles!");
    particles.reset();
    particles.resizeWorld(pg.width, pg.height);
    particles.createObstacleFlowField(pg_obstacles, new int[]{0,0,0,255}, false);
    float border = 4;
    float dimx = pg.width  - border;
    float dimy = pg.height - border;
    float particle_size = particles.param.size_collision;
    int numx = (int) (dimx / (particle_size+0.1f));
    int numy = (int) (dimy / (particle_size+0.1f));


    DwFlowFieldParticles.SpawnRect spawn = new DwFlowFieldParticles.SpawnRect();
    spawn.dim(dimx, dimy);
    spawn.pos(pg.width/2-dimx/2, pg.height/2-dimy/2);
    spawn.vel(0, 0);

    System.out.println("Initial spawn numx=" + numx + " numy=" + numy);
    spawn.num(numx, numy);

    particles.spawn(pg.width, pg.height, spawn);
  }


  float impulse_max = 256;
  float impulse_mul = 15;
  float impulse_tsmooth = 0.90f;
  int   impulse_blur  = 0;
  float impulse_size = 60;

  /**
   * Code for adding impulses to the particles.
   */
  public void addImpulse(){
    PApplet pApplet = RainbowStudio.pApplet;
    impulse_size = pApplet.min(pg.width, pg.height) / 10f;

    int w = pg.width;
    int h = pg.height;

    // impulse center/velocity
    // TODO(tracy): Vary the position of the impulse.  Also expose impulse magnitude as a parameter
    // Maybe we should have low/mid/high frequency impulse areas to make the particles jump.
    float mx = w/2;
    float my = h/2;
    float vx = 10f * +impulse_mul; // (mouseX - pmouseX) * +impulse_mul;
    float vy = 10f * -impulse_mul; // (mouseY - pmouseY) * -impulse_mul; // flip vertically
    // clamp velocity
    float vv_sq = vx*vx + vy*vy;
    float vv_sq_max = impulse_max*impulse_max;
    if(vv_sq > vv_sq_max){
      vx = impulse_max * vx / pApplet.sqrt(vv_sq);
      vy = impulse_max * vy / pApplet.sqrt(vv_sq);
    }
    // map velocity, to UNSIGNED_BYTE range
    final int mid = 127;
    vx = pApplet.map(vx, -impulse_max, +impulse_max, 0, mid<<1);
    vy = pApplet.map(vy, -impulse_max, +impulse_max, 0, mid<<1);
    // render "velocity"
    pg_impulse.beginDraw();
    pg_impulse.background(mid, mid, mid);
    pg_impulse.noStroke();


    // if(mousePressed){
    // NOTE(tracy): Always add the impulse when we call this for now.
      pg_impulse.fill(vx, vy, mid);
      pg_impulse.ellipse(mx, my, impulse_size, impulse_size);
    // }

    pg_impulse.endDraw();


    // create impulse texture
    ff_impulse.resize(w, h);
    {
      Merge.TexMad ta = new Merge.TexMad(ff_impulse.tex_vel, impulse_tsmooth, 0);
      Merge.TexMad tb = new Merge.TexMad(pg_impulse,  1, -mid/255f);
      DwFilter.get(context).merge.apply(ff_impulse.tex_vel, ta, tb);
      ff_impulse.blur(1, impulse_blur);
    }


    // create acceleration texture
    ff_acc.resize(w, h);
    {
      Merge.TexMad ta = new Merge.TexMad(ff_impulse.tex_vel, 1, 0);
      Merge.TexMad tb = new Merge.TexMad(pg_gravity, -0.08f, 0);
      DwFilter.get(context).merge.apply(ff_acc.tex_vel, ta, tb);
    }
  }


  float[][] pallette = {
      { 32,  32, 32},
      {196,  96,  0},
      {128, 128,  0},
      {  0,  96,196},
      { 96,  96, 96},
  };

  public void updateColor(){
    if (((int)currentFrame)%5 == 0) {
//    float mix = sin(frameCount*0.001f) * 0.5f + 0.5f;
      //float mix = RainbowStudio.pApplet.map(mouseX, 0, width, 0, 1);
      float mix = 0.5f;
      float[] rgb1 = DwUtils.getColor(pallette, mix, null);
      float s1 = 1f / 255f;
      float s2 = s1 * 0.25f;
      float red = (float) Math.random();
      float green = (float) Math.random();
      float blue = (float) Math.random();

      //particles.param.col_A = new float[]{rgb1[0] * s1, rgb1[1] * s1, rgb1[2] * s1, 1.0f};
      //particles.param.col_B = new float[]{rgb1[0] * s2, rgb1[1] * s2, rgb1[2] * s2, 0.0f};
      particles.param.col_A = new float[]{red, green, blue, 1.0f};
      particles.param.col_B = new float[]{red * 0.25f, green * 0.25f, blue * 0.25f, 0.0f};
    }
  }


}

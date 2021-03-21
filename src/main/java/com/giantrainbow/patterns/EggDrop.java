package com.giantrainbow.patterns;

import com.giantrainbow.PathUtils;
import com.giantrainbow.RainbowStudio;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.DwShadertoy;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import processing.core.*;
import processing.opengl.PGraphicsOpenGL;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import static com.giantrainbow.RainbowStudio.pApplet;
import static processing.core.PConstants.P2D;

public class EggDrop extends PGTexture {
  Box2DProcessing box2d;
  ArrayList<Ground> boundaries;
  ArrayList<Egg> eggs;
  private static final String SPRITE_DIR = "spritepp/";
  public CompoundParameter eggRadius = new CompoundParameter("rad", 7, 1, 10f)
      .setDescription("Egg collision radius");
  public CompoundParameter eggDensity = new CompoundParameter("dens", 2f, 100f, 100f)
      .setDescription("Egg mass density");
  public CompoundParameter eggFriction = new CompoundParameter("frc", 1f, 0f, 1f)
      .setDescription("Egg friction");
  public CompoundParameter eggRestitution = new CompoundParameter("res", 0f, 0f, 1f)
      .setDescription("Egg restitution, aka bouncy");
  public CompoundParameter spawnRate = new CompoundParameter("rate", 0.05f, 0f, 1f);
  public CompoundParameter stepRate = new CompoundParameter("step", 2f, 1f, 60f)
      .setDescription("Sets the physics step rate to 1/step");
  public CompoundParameter gravity = new CompoundParameter("gravity", 15f, 1f, 30f);
  public BooleanParameter drawBackground = new BooleanParameter("bg", true)
      .setDescription("Whether to draw a background. Unselected this can be layered on another channel");

  PImage[] eggSprites = new PImage[3];

  private static final String LOCAL_SHADER_DIR = "shaders/";
  private static final String CLOUDS_SHADER = "clouds.frag";
  DwPixelFlow context;
  DwShadertoy toy;
  PGraphics toyGraphics;
  float[] u1, u2;

  public EggDrop(LX lx) {
    super(lx, "");
    addParameter(eggRadius);
    addParameter(eggDensity);
    addParameter(eggFriction);
    addParameter(eggRestitution);
    addParameter(spawnRate);
    addParameter(stepRate);
    addParameter(gravity);
    addParameter(drawBackground);

    gravity.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) {
        box2d.setGravity(0, -1f * ((CompoundParameter)p).getValuef());
      }
    });
    box2d = new Box2DProcessing(RainbowStudio.pApplet);
    box2d.createWorld();
    // We are setting a custom gravity
    box2d.setGravity(0, -10);
    // Create ArrayLists
    eggs = new ArrayList<Egg>();
    boundaries = new ArrayList<Ground>();
    boundaries.add(new Ground(264, 264, 264-35));
    eggSprites[0] = PathUtils.loadSprite(pApplet, SPRITE_DIR + "egg2small.gif")[0];
    eggSprites[1] = PathUtils.loadSprite(pApplet, SPRITE_DIR + "egg3small.gif")[0];
    eggSprites[2] = PathUtils.loadSprite(pApplet, SPRITE_DIR + "egg4small.gif")[0];
    initShader();
  }

  public void initShader() {
    u1 = new float[4];
    u2 = new float[4];
    toyGraphics = RainbowStudio.pApplet.createGraphics(pg.width, pg.height, P2D);
    loadShader();
    // context initialized in loadShader, print the GL hardware once when loading
    // the pattern.  left in for now while testing performance on different
    // graphics hardware.
    context.print();
    context.printGL();
    toy.set_iMouse(0f, 0f, 0f, 0f);
    u1[0] = 0.72f;  // cloud scale
    u1[1] = 0.15f;  // cloud speed
    u1[2] = 0.57f;  // cloud darkness
    u1[3] = 0.5f;   // cloud lightness

    u2[0] = 0.0f; // cloud cover
    u2[1] = 0.06f; // cloud alpha
    u2[2] = 0.33f; // sky tint (can go brownish)
    u2[3] = 0f;
  }

  public PImage runShader() {
    ShaderToy.shaderApply(context, toy, (PGraphicsOpenGL) toyGraphics, u1, u2);
    toyGraphics.loadPixels();
    toyGraphics.updatePixels();
    return toyGraphics;
  }


  protected void loadShader() {
    if (toy != null) {
      // release existing shader texture
      toy.release();
      toy = null;
    }
    if (context != null) context.release();
    context = new DwPixelFlow(RainbowStudio.pApplet);
    // TODO(tracy): Handle file not found issue.

    File local = new File(LOCAL_SHADER_DIR + CLOUDS_SHADER);
    if (local.isFile()) {
      toy = new DwShadertoy(context, local.getPath());
    }
  }


  public void resetEggs() {
    // reset eggs.
    for (int i = eggs.size()-1; i >= 0; i--) {
      eggs.get(i).killBody();
      eggs.remove(i);
    }
  }

  public void onActive() {
    resetEggs();
  }

  public void draw(double deltaDrawMs) {
    pg.background(0, 0);
    if (drawBackground.getValueb()) {
      pg.image(runShader(), 0, 0);
      // Display all the boundaries
      for (Ground wall: boundaries) {
        wall.display();
      }
    }

    // We must always step through time!
    box2d.step();

    // Boxes fall from the top every so often
    if (Math.random() < spawnRate.getValuef()) {
      Egg p = new Egg((int)(30f + 468f * Math.random()), -20);

      eggs.add(p);
    }

    // Display all the eggs
    for (Egg b: eggs) {
      b.display();
    }

    if (eggs.size() > 120)
      resetEggs();

    // Eggs that leave the screen, we delete them
    // (note they have to be deleted from both the box2d world and our list
    for (int i = eggs.size()-1; i >= 0; i--) {
      Egg b = eggs.get(i);
      if (b.done()) {
        eggs.remove(i);
      }
    }
  }

  // A fixed boundary class
  class Ground {
    // A boundary is a simple circle with center at x,y,width,and height
    float x;
    float y;
    float radius;

    // But we also have to make a body for box2d to know about it
    Body b;

    Ground(float x, float y, float radius) {
      this.x = x;
      this.y = y;
      this.radius = radius;

      CircleShape cs = new CircleShape();
      cs.setRadius(this.radius);

      // Create the body
      BodyDef bd = new BodyDef();
      bd.type = BodyType.STATIC;
      bd.position.set(box2d.coordPixelsToWorld(x,y));
      b = box2d.createBody(bd);

      // Attached the shape to the body using a Fixture
      b.createFixture(cs,1);
    }

    // Draw the boundary, if it were at an angle we'd have to do something fancier
    void display() {
      pg.fill(0, 50, 0);
      pg.stroke(32, 32, 0);
      pg.rectMode(PConstants.CENTER);
      pg.ellipseMode(PConstants.RADIUS);
      pg.ellipse(x, y, radius, radius);
    }

  }

  // Modeled as a circle.
  class Egg {
    Body body;
    PImage sprite;

    // Constructor
    Egg(float x, float y) {
      // Add the body to the box2d world
      makeBody(new Vec2(x, y));
      Random random = new Random();
      int eggNumber = random.nextInt(eggSprites.length);
      sprite = eggSprites[eggNumber];
    }

    // This function removes the particle from the box2d world
    void killBody() {
      box2d.destroyBody(body);
    }

    // Is the particle ready for deletion?
    boolean done() {
      // Let's find the screen position of the particle
      Vec2 pos = box2d.getBodyPixelCoord(body);
      // Is it off the bottom of the screen?
      if (pos.y > pg.height + sprite.height) {
        killBody();
        return true;
      }
      return false;
    }

    // Drawing the box
    void display() {
      // We look at each body and get its screen position
      Vec2 pos = box2d.getBodyPixelCoord(body);
      // Get its angle of rotation
      float a = body.getAngle();

      pg.rectMode(PConstants.CENTER);
      pg.pushMatrix();
      pg.translate(pos.x, pos.y);
      pg.rotate(-a);
      pg.image(sprite, 0, 0, sprite.width, sprite.height);
      pg.popMatrix();
    }

    // This function adds the rectangle to the box2d world
    void makeBody(Vec2 center) {

      // Define a polygon (this is what we use for a rectangle)
      CircleShape sd2 = new CircleShape();
      sd2.setRadius(eggRadius.getValuef());

      // Define a fixture
      FixtureDef fd = new FixtureDef();
      fd.shape = sd2;
      // Parameters that affect physics
      fd.density = eggDensity.getValuef();
      fd.friction = eggFriction.getValuef();
      fd.restitution = eggRestitution.getValuef();

      // Define the body and make it from the shape
      BodyDef bd = new BodyDef();
      bd.type = BodyType.DYNAMIC;
      bd.position.set(box2d.coordPixelsToWorld(center));
      bd.bullet = true;
      bd.angularDamping = 0.8f;

      body = box2d.createBody(bd);
      body.createFixture(fd);

      // Give it some initial random velocity
      body.setLinearVelocity(new Vec2(RainbowStudio.pApplet.random(-40, 40),
          RainbowStudio.pApplet.random(2, 5)));
      //body.setAngularVelocity(RainbowStudio.pApplet.random(-5, 5));
    }
  }

  public class Box2DProcessing {
    private static final int Y_FLIP_INDICATOR = -1;
    PApplet parent;
    // The Box2D world
    World world;

    // Variables to keep track of translating between world and screen coordinates
    float transX;
    float transY;
    float scaleFactor;// = 10.0f;
    int yFlip;// = Y_FLIP_INDICATOR; //flip y coordinate

    Body groundBody;

    // Construct with a default scaleFactor of 10
    public Box2DProcessing(PApplet p) {
      this(p,1);
    }

    public Box2DProcessing(PApplet p, float sf) {
      parent = p;
      transX = pg.width/2;  // parent.width/2
      transY = pg.height/2;
      scaleFactor = sf;
      yFlip = Y_FLIP_INDICATOR;

    }

    // This is the all important physics "step" function
    // Says to move ahead one unit in time
    // Default
    public void step() {
      float timeStep = 1.0f / stepRate.getValuef(); //5f; //1.0f / 60f;
      this.step(timeStep,10,8);
      world.clearForces();
    }

    // Custom
    public void step(float timeStep, int velocityIterations, int positionIterations) {
      world.step(timeStep, velocityIterations, positionIterations);
    }

    public void setWarmStarting(boolean b) {
      world.setWarmStarting(b);
    }

    public void setContinuousPhysics(boolean b) {
      world.setContinuousPhysics(b);
    }

    // Create a default world with default gravity
    public void createWorld() {
      Vec2 gravity = new Vec2(0.0f, -10.0f);
      createWorld(gravity);
      setWarmStarting(true);
      setContinuousPhysics(true);
    }

    public void createWorld(Vec2 gravity) {
      createWorld(gravity,true,true);
    }

    public void createWorld(Vec2 gravity, boolean warmStarting, boolean continous) {
      world = new World(gravity);
      setWarmStarting(warmStarting);
      setContinuousPhysics(continous);

      BodyDef bodyDef = new BodyDef();
      groundBody = world.createBody(bodyDef);
    }

    public Body getGroundBody() {
      return groundBody;
    }



    // Set the gravity (this can change in real-time)
    public void setGravity(float x, float y) {
      world.setGravity(new Vec2(x,y));
    }

    // These functions are very important
    // Box2d has its own coordinate system and we have to move back and forth between them
    // convert from Box2d world to pixel space
    public Vec2 coordWorldToPixels(Vec2 world) {
      return coordWorldToPixels(world.x,world.y);
    }

    public PVector coordWorldToPixelsPVector(Vec2 world) {
      Vec2 v = coordWorldToPixels(world.x,world.y);
      return new PVector(v.x,v.y);
    }

    public Vec2 coordWorldToPixels(float worldX, float worldY) {
      float pixelX = PApplet.map(worldX, 0f, 1f, transX, transX+scaleFactor);
      float pixelY = PApplet.map(worldY, 0f, 1f, transY, transY+scaleFactor);
      if (yFlip == Y_FLIP_INDICATOR) pixelY = PApplet.map(pixelY,0f,pg.width, pg.width,0f);
      return new Vec2(pixelX, pixelY);
    }

    // convert Coordinate from pixel space to box2d world
    public Vec2 coordPixelsToWorld(Vec2 screen) {
      return coordPixelsToWorld(screen.x,screen.y);
    }

    public Vec2 coordPixelsToWorld(PVector screen) {
      return coordPixelsToWorld(screen.x,screen.y);
    }

    public Vec2 coordPixelsToWorld(float pixelX, float pixelY) {
      float worldX = PApplet.map(pixelX, transX, transX+scaleFactor, 0f, 1f);
      float worldY = pixelY;
      if (yFlip == Y_FLIP_INDICATOR) worldY = PApplet.map(pixelY,pg.width,0f,0f,pg.width);
      worldY = PApplet.map(worldY, transY, transY+scaleFactor, 0f, 1f);
      return new Vec2(worldX,worldY);
    }

    // Scale scalar quantity between worlds
    public float scalarPixelsToWorld(float val) {
      return val / scaleFactor;
    }

    public float scalarWorldToPixels(float val) {
      return val * scaleFactor;
    }

    // Scale vector between worlds
    public Vec2 vectorPixelsToWorld(Vec2 v) {
      Vec2 u = new Vec2(v.x/scaleFactor,v.y/scaleFactor);
      u.y *=  yFlip;
      return u;
    }

    public Vec2 vectorPixelsToWorld(PVector v) {
      Vec2 u = new Vec2(v.x/scaleFactor,v.y/scaleFactor);
      u.y *=  yFlip;
      return u;
    }

    public Vec2 vectorPixelsToWorld(float x, float y) {
      Vec2 u = new Vec2(x/scaleFactor,y/scaleFactor);
      u.y *=  yFlip;
      return u;
    }

    public Vec2 vectorWorldToPixels(Vec2 v) {
      Vec2 u = new Vec2(v.x*scaleFactor,v.y*scaleFactor);
      u.y *=  yFlip;
      return u;
    }

    public PVector vectorWorldToPixelsPVector(Vec2 v) {
      PVector u = new PVector(v.x*scaleFactor,v.y*scaleFactor);
      u.y *=  yFlip;
      return u;
    }

    // A common task we have to do a lot
    public Body createBody(BodyDef bd) {
      return world.createBody(bd);
    }

    // A common task we have to do a lot
    public Joint createJoint(JointDef jd) {
      return world.createJoint(jd);
    }

    // Another common task, find the position of a body
    // so that we can draw it
    public Vec2 getBodyPixelCoord(Body b) {
      Transform xf = b.getTransform();//b.getXForm();
      //return coordWorldToPixels(xf.position);
      return coordWorldToPixels(xf.p);
    }

    public PVector getBodyPixelCoordPVector(Body b) {
      Transform xf = b.getTransform();
      return coordWorldToPixelsPVector(xf.p);
    }

    public void destroyBody(Body b) {
      world.destroyBody(b);
    }


  }

}

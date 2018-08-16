package com.giantrainbow.model;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXModel;
import java.util.logging.Logger;

/**
 * Abstract class for tracking things common to our models.
 * These can be used by the Patterns so that a pattern can
 * work on either the full rainbow or some subset of LEDs
 * for test panels.  Also, knowing the radius and arc in
 * degrees is important for some patterns.
 *
 * TODO(tracy): Maybe it would be better to use the Metrics
 * class that the 2D GridModel uses.  But their standard
 * pixel coordinate system is 0,0 top left and max_x,max_y on
 * the bottom right. It might be better to migrate to that
 * coordinate system so some of the existing patterns in
 * the LX library work.  It requires rewriting our existing
 * patterns. Although, Processing uses this coordinate space so
 * probably less confusing to keep this.
 */
public abstract class RainbowBaseModel extends LXModel {
  private static final Logger logger = Logger.getLogger(RainbowBaseModel.class.getName());

  public final int pointsWide;
  public final int pointsHigh;
  public float thetaStart;
  public float thetaFinish;

  public RainbowBaseModel(LXFixture fixture, int width, int height) {
    super(fixture);
    this.pointsWide = width;
    this.pointsHigh =  height;

    logger.info("X: " + xMin + " - " + xMax);
    logger.info("Y: " + yMin + " - " + yMax);
    logger.info("pointsWide: " + pointsWide);
    logger.info("pointsHigh: " + pointsHigh);
  }

  // From CAD drawings.  Note that these numbers are off the mechanical dimensions, so
  // there might still be some small adjustments.  Also, the variables below have the
  // theta start and theta finish slightly adjusted so that the model generates the
  // proper 12600 leds.
  // Starts at 9.3165082 degrees
  // arc is 161.3669836 degrees
  // end point at 170.6834918
  // radius is 36.9879  Based on a 73' chord of a circle with a perpendicular height to
  // the circle of 31'.

  public static float radiusInc = 2.75f / 12.0f;
  public static float innerRadius = 36.9879f;
  public static float outerRadius = 0.0f;
  public static float rainbowThetaStart = 9.41f; //9.4165082;
  public static float rainbowThetaFinish = 170.53f; //170.5383491;
  public static float rainbowPointsPerRow = 420.0f;
  public static float rainbowThetaInc = (rainbowThetaFinish-rainbowThetaStart) / (rainbowPointsPerRow-1);
  public static float radialPixelDensity = 2.75f; // 2.75 inches between pixels radially
  public static float innerRadiusPixelDensity = 2.5f;
  public static float outerRadiusPixelDensity = 3.0f;
  public static float pixelsPerFoot = 6.0f;  // Based on 2" pixel density.
}

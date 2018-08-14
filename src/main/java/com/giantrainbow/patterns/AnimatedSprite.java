package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.pApplet;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.radians;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.PI;

import com.giantrainbow.PathUtils;
import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PImage;

@LXCategory(LXCategory.FORM)
public class AnimatedSprite extends PGTexture {
  private static final String SPRITE_DIR = "spritepp/";
  private static final String SPRITE_NAME = "smallcat.gif";

  float angle = 0.0f;
  float minAngle = 0.0f;
  float maxAngle = PI;
  private PImage[] images;
  int spriteWidth = 0;

  public AnimatedSprite(LX lx) {
    super(lx, P2D);

    images = PathUtils.loadSprite(pApplet, SPRITE_DIR + SPRITE_NAME);
    for (int i = 0; i < images.length; i++) {
      images[i].loadPixels();
      // assume frames are the same size.
      spriteWidth = images[i].width;
      minAngle = radians(((RainbowBaseModel)(lx.model)).thetaStart - 10.0f);
      maxAngle = radians(((RainbowBaseModel)(lx.model)).thetaFinish + 10.0f);
      angle = minAngle;
    }
  }

  public void draw(double deltaDrawMs) {
    if (currentFrame >= images.length) {
      currentFrame = 0.0;
      previousFrame = -1;
    }
    angle += 0.03;
    if (angle > maxAngle) angle = minAngle;

    // Use this constant to fine tune where on the radius it should be. Each radiusInc should be
    // the physical distance between LEDs radially.  Here I picked 12.0 to put it in the middle.
    // Computing and using middleRadiusInWorldPixels could also work.
    float radialIncTune = 12.0f * RainbowBaseModel.radiusInc;
    float tunedRadiusInWorldPixels = (RainbowBaseModel.innerRadius + radialIncTune) * RainbowBaseModel.pixelsPerFoot;

    // Mathematically, this should be the radius of the center strip of pixels.
    // float radiiThickness = RainbowBaseModel.outerRadius - RainbowBaseModel.innerRadius;
    // float middleRadiusInWorldPixels = (RainbowBaseModel.innerRadius + radiiThickness) * RainbowBaseModel.pixelsPerFoot;

    float outerRadiusInWorldPixels = RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot;

    // The rainbow is centered around 0,0 in world space, but x=0 in image space is actually
    // the outer edge of the circle at -radius in world space. i.e. outerRadiusInWorldPixels.
    // Also, up until now we are targeting the center of the image.  Since image coordinates
    // start at 0,0 at the top left we need to change our coordinate space by half our width (center).
    float xImagePos = tunedRadiusInWorldPixels*cos(angle) - spriteWidth/2.0f
      + outerRadiusInWorldPixels; // account for render buffer x=0 maps to world x=-radius

    // Image coordinates have Y inverted from our 3D world space coordinates.  Also, like above,
    // adjust by half our width to change from middle-of-the-image coordinate space to
    // top-left coordinate space.
    float yImagePos = outerRadiusInWorldPixels - tunedRadiusInWorldPixels*sin(angle) - spriteWidth/2.0f;

    pg.background(0);
    pg.image(images[(int)currentFrame], xImagePos, yImagePos);
  }
}

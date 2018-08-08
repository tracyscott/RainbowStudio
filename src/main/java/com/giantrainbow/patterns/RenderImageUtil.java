package com.giantrainbow.patterns;

import static processing.core.PApplet.ceil;
import static processing.core.PApplet.floor;
import static processing.core.PApplet.round;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import java.util.logging.Logger;
import processing.core.PImage;

/**
 * Utility class for rendering images.  Implements the mapping of
 * the polar coordinate generated points into an image, including
 * the option of antialiasing.
 */
class RenderImageUtil {
  private static final Logger logger = Logger.getLogger(RenderImageUtil.class.getName());

  /**
   * Compute a new RGB color based on a given weight.  Each
   * RGB component is multiplied by the weight (range 0.0-1.0)
   * and a new 32-bit color is returned.  Currently forces
   * alpha to 0xFF.
   */
  public static int getWeightedColor(int clr, float weight) {
    int red = LXColor.red(clr) & 0xFF; // & 0xFF to get byte value unsigned.
    int green = LXColor.green(clr) & 0xFF;
    int blue = LXColor.blue(clr) & 0xFF;

    int weightedRed = ((int)((float)red * weight))<< 16;
    int weightedGreen = ((int)((float)green * weight))<<8;
    int weightedBlue = (int)((float)blue * weight);
    return  0xFF000000 | weightedRed | weightedGreen | weightedBlue;
  }

  /**
   * Render an image to the rainbow pixel-perfect.  This effectively treats the
   * rainbow as a 420x30 image.  The image will effectively have a bend distortion
   * and lesser intensity at the top due to reduced pixel density but it is
   * doesn't suffer from the aliasing caused by the led positions not being
   * perfectly cartesian.
   */
  public static void imageToPointsPixelPerfect(LX lx, int[] colors, PImage image) {
    int pointsWide = ((RainbowBaseModel)lx.model).pointsWide;
    int pointsHigh = ((RainbowBaseModel)lx.model).pointsHigh;
    int pointNumber = 0;

    image.loadPixels();
    // NOTE: This code assumes that the point layout starts with point 0,0 on
    // the bottom left corner of the points.
    for (LXPoint p : lx.model.points) {
      int y = pointNumber / pointsWide;
      int x = pointNumber % pointsWide;
      int imageY = (pointsHigh - 1) - y;  // Y is backwards for images.
      int imageX = x;
      int imageIndex = imageY * image.width + imageX;
      //if (imageIndex == 12600) {
      if (imageY == 29) {
        //System.out.println("x: " + x + " y:" + y + " imageX:" + imageX + " imageY:" + imageY + " imageIndex:" + imageIndex);
      }
      //}
      colors[pointNumber] = image.pixels[imageIndex];
      pointNumber++;
    }
  }

  /**
   * Given our Rainbow points/leds, sample an image to assign led colors.  The image is
   * the same size as the top half of the circle of the Rainbow.  Use this method if you
   * are generating images that rely on the circumference of the circle.  With this method
   * you can use RainbowBaseModel.innerRadius, etc. directly in your code.  If you try to
   * use radial effects with imageToPointsBBox code you might lose your mind dealing with coordinate
   * space transformations.
   *
   * The image coordinate system is different than our world-space coordinate system.  For
   * images, 0,0 is the top left and imageWidth-1,imageHeight-1 is the bottom right.  In world space
   * increasing Y goes up, while in image space increasing Y moves down.  Also, our points/leds are
   * in world space as units of FEET. For now we assume a base led density of 6 per foot
   * (every 2 inches).  So, the bounding rectange size for our image is (2*Radius*6, Radius*6).
   * The image origin (0,0) is located at (-Radius, Radius) in world space.  Or (-6*Radius, 6*Radius) in world-pixel-space.
   */
  public static void imageToPointsSemiCircle(LX lx, int[] colors, PImage image, boolean antialias) {
    float radiusInWorldPixels = (RainbowBaseModel.outerRadius) * RainbowBaseModel.pixelsPerFoot;
    float imageOriginWorldX = -radiusInWorldPixels;
    float worldPixelsHeight = radiusInWorldPixels;

    image.loadPixels();

    for (LXPoint p : lx.model.points) {
      // Adjust the x-coordinate by one radius since the world space is centered around x=0
      // but image space starts at 0,0.
      float pointXWorldPixels = p.x * RainbowBaseModel.pixelsPerFoot;
      float pointYWorldPixels = p.y * RainbowBaseModel.pixelsPerFoot;
      float pointXImagePixels = pointXWorldPixels - imageOriginWorldX;
      // Invert Y coordinates to account for difference in Y orientation between world space
      // and image coordinate space.
      float pointYImagePixels = worldPixelsHeight - pointYWorldPixels;
      if (pointYImagePixels < 0) {
        // This should never happen, output stats if it does.
        logger.info("p.x: " + p.x + " p.y:" + p.y + " pointYWorldPixels: " +
        pointYWorldPixels + " poingYImagePixels: " + pointYImagePixels + " worldPixelsHeight:" +
        worldPixelsHeight);
        logger.info("model.y.max: " + lx.model.yMax * 6.0);
      }
      // The nearest image coordinate.  Use this directly for non anti-aliased case.
      int xCoordImagePixels = round(pointXImagePixels);
      int yCoordImagePixels = round(pointYImagePixels);

      // If we end up rounding outside the image, just take the boundary pixel.
      if (yCoordImagePixels >= image.height) {
        yCoordImagePixels = image.height - 1;
      }
      if (yCoordImagePixels < 0) {
        yCoordImagePixels = 0;
      }
      if (xCoordImagePixels < 0) {
        xCoordImagePixels = 0;
      }
      if (xCoordImagePixels > image.width) {
        xCoordImagePixels = image.width - 1;
      }

      if (!antialias) {
        int imgIndex = yCoordImagePixels * image.width + xCoordImagePixels;
        colors[p.index] = image.pixels[imgIndex];
      } else {
        // Deal with anti-aliasing.
        // ANTIALIASING
        float remainderX = pointXImagePixels - floor(pointXImagePixels);
        float remainderY = pointYImagePixels - floor(pointYImagePixels);

        // The image coordinate to the left
        int xLeft = floor(pointXImagePixels);
        // The image coordinate to the right
        int xRight = ceil(pointXImagePixels);
        int yAbove = ceil(pointYImagePixels);
        // The image coordinate below
        int yBelow = floor(pointYImagePixels);

        // If we round outside our boundaries, clamp the values. We will skip
        // these when doing weighted averages.
        if (xLeft < 0) {
          xLeft = -1;
        }
        if (xRight >= image.width) {
          xRight = image.width;
        }
        if (yAbove >= image.height) {
          yAbove = image.height;
        }
        if (yBelow < 0) {
          yBelow = -1;
        }

        // Compute the image index of the nearest pixels.
        int imgIndexLeft = image.width * yCoordImagePixels + xLeft;
        int imgIndexRight = image.width * yCoordImagePixels + xRight;
        int imgIndexBelow = image.width * yBelow + xCoordImagePixels;
        int imgIndexAbove = image.width * yAbove + xCoordImagePixels;

        // These will hold our weighted sampled color values from the nearest image pixels.
        int leftColor = 0;
        float leftWeight = 1.0f - remainderX;
        int rightColor = 0;
        float rightWeight = remainderX;
        int aboveColor = 0;
        float aboveWeight = remainderY;
        int belowColor = 0;
        float belowWeight = 1.0f - remainderY;

        // If the right image pixel position is past the edge of the image, then use
        // only the left weight.
        if (imgIndexRight == image.width) {
          leftWeight = 1.0f;
        }
        if (imgIndexLeft != 1) {
          leftColor = image.pixels[imgIndexLeft];
          leftColor = RenderImageUtil.getWeightedColor(leftColor, leftWeight);
        } else {
          // Use only the right image pixel position since left was outside image.
          rightWeight = 1.0f;
        }

        // When this is zero, we are on the right edge
        if (imgIndexRight != image.width) {
          rightColor = image.pixels[imgIndexRight];
          rightColor = RenderImageUtil.getWeightedColor(rightColor, rightWeight);
        }

        if (yAbove == image.height) {
          belowWeight = 1.0f;
        }
        if (yBelow >= 0) {
          belowColor = image.pixels[imgIndexBelow];
          belowColor = RenderImageUtil.getWeightedColor(belowColor, belowWeight);
        } else {
          aboveWeight = 1.0f;
        }

        if (yAbove < image.height) {
          aboveColor = image.pixels[imgIndexAbove];
          aboveColor = RenderImageUtil.getWeightedColor(aboveColor, aboveWeight);
        }

        // TODO(tracy): Change this weight when combining top and bottom colors.
        int horizontalColor = RenderImageUtil.getWeightedColor(LXColor.add(leftColor, rightColor), 0.5f);
        int verticalColor = RenderImageUtil.getWeightedColor(LXColor.add(belowColor, aboveColor), 0.5f);
        int totalColor = LXColor.add(horizontalColor, verticalColor);
        colors[p.index] = totalColor;
      }
    }
  }

  /**
   * Render an image on the rainbow. Includes an option for antialiasing.
   * This method assumes that the image has been scaled to correspond
   * to the bounding box size of the rainbow arc.  The pixel density is
   * 2 inch spacing (via RainbowBaseModel.pixelsPerFoot).  That number
   * was chosen because the maximum density of the pixels on a rainbow
   * panel are 2.5 inches apart.  The pixel density at the top is 3 inches.
   * The radial pixel density is a consistent 2.75".
   * Here the image is expected to be the size in pixels of the bounding box of the rainbow.
   * If you want to generate an image with radial effects matching the circumference
   * of the rainbow, you would want to make the image size the bounding box of the top half
   * of the semi-circle and use imageToPointsSemiCircle. That way you can use
   * RainbowBaseModel.innerRadius, etc when generating image.  See the PGDraw pattern.  It does
   * that so Processing draw code isn't overly complicated by coordinate space transformations.
   *
   * TODO(tracy): This is only left in for efficiency sake.  It might be better to always use
   * a larger semi-circle bounding image.  Leaving this in case the larger image is an issue
   * or the node.js pipeline.
   *
   * @deprecated There is a bug in here somewhere.  Use imageToPointsSemiCircle until this
   * can be cleaned up.
   */
   /*
  public static void imageToPointsBBox2(LX lx, int[] colors,
  PImage image, boolean antialias) {
    LXModel model = lx.model;
    int imageWidth = image.width;
    int imageHeight = image.height;
    int numPointsPerRow = ((RainbowBaseModel)(lx.model)).pointsWide;
     // Convert from world space to image coordinate space.

    image.loadPixels();

    for (LXPoint p : model.points) {
      float pointX = (p.x - model.xMin) *RainbowBaseModel.pixelsPerFoot;
      float pointY = (p.y - model.yMin) *RainbowBaseModel.pixelsPerFoot;
      // The nearest image coordinate
      int xCoord = (int)round(pointX);
      int yCoord = imageHeight - (int)round(pointY);

      // Don't allow rounding past the right or top of the image.
      if (xCoord == imageWidth)
        xCoord -= 1;
      if (yCoord == imageHeight)
        yCoord -= 1;

      if (!antialias) {
        // NEAREST IMAGE PIXEL
        int imgIndex = yCoord * imageWidth + xCoord;
        //System.out.println("yCoord
        colors[p.index] = image.pixels[imgIndex];
      } else {
        // ANTIALIASING
        float remainderX = pointX - floor(pointX);
        float remainderY = pointY - floor(pointY);

        // The image coordinate to the left
        int xLeft = floor(pointX);
        // The image coordinate to the right
        int xRight = ceil(pointX);
        // The image coordinate above
        int yAbove = ceil(pointY);
        // The image coordinate below
        int yBelow = floor(pointY);

        // The target pixel index in the image for the above.
        int imgIndexLeft = image.width * yCoord + xLeft;
        int imgIndexRight = image.width * yCoord + xRight;
        int imgIndexAbove = image.width * yAbove + xCoord;
        int imgIndexBelow = image.width * yBelow + xCoord;

        int leftColor = 0;
        float leftWeight = 1.0 - remainderX;
        int rightColor = 0;
        float rightWeight = remainderX;
        int aboveColor = 0;
        float aboveWeight = remainderY;
        int belowColor = 0;
        float belowWeight = 1.0 - remainderY;
        // When this is zero, we are on the left edge, so nothing to the left.
        if ((p.index + 1)%numPointsPerRow == 0) {
          leftWeight = 1.0;
        }
        if (p.index%numPointsPerRow != 0) {
          leftColor = image.pixels[imgIndexLeft];
          leftColor = RenderImageUtil.getWeightedColor(leftColor, leftWeight);
        } else {
          rightWeight = 1.0;
        }

        // When this is zero, we are on the right edge
        if ((p.index + 1)%numPointsPerRow != 0) {
          rightColor = image.pixels[imgIndexRight];
          rightColor = RenderImageUtil.getWeightedColor(rightColor, rightWeight);
        }

        if (imgIndexAbove >= imageWidth * imageHeight) {
          belowWeight = 1.0;
        }
        // When this is less than zero, we are on the bottom row.
        if (imgIndexBelow >= 0 && imgIndexBelow < imageWidth * imageHeight) {
          belowColor = image.pixels[imgIndexBelow];
          belowColor = RenderImageUtil.getWeightedColor(belowColor, belowWeight);
        } else {
          aboveWeight = 1.0;
        }
        // When this is greater than our number of pixels we are in the top row
        if (!(imgIndexAbove >= imageWidth * imageHeight)) {
          aboveColor = image.pixels[imgIndexAbove];
          aboveColor = RenderImageUtil.getWeightedColor(aboveColor, aboveWeight);
        }
        int horizontalColor = RenderImageUtil.getWeightedColor(LXColor.add(leftColor, rightColor), 0.5);
        int verticalColor = RenderImageUtil.getWeightedColor(LXColor.add(aboveColor, belowColor), 0.5);
        int totalColor = LXColor.add(horizontalColor, verticalColor);
        colors[p.index] = totalColor;
      }
    }
  }
  */
}

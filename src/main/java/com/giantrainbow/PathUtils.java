/*
 * Created by shawn on 8/2/18 4:46 PM.
 */
package com.giantrainbow;

import static processing.core.PConstants.ARGB;

import com.google.common.reflect.ClassPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Utilities for finding things in paths, including images and animations.
 */
public class PathUtils {
  private static final Logger logger = Logger.getLogger(PathUtils.class.getName());

  /**
   * Loads a sprite from the given path. This does not append ".gif" to the path.
   * If there was any kind of loading error, then this returns only the images
   * that were successfully loaded.
   *
   * @return the sprite's sequence of images.
   */
  public static PImage[] loadSprite(PApplet applet, String path) {
    ArrayList<PImage> frames = new ArrayList<>();

    // gifAnimator isn't written well to handle exceptions properly :(
    try (InputStream in = applet.createInput(path)) {
      if (in == null) {
        return new PImage[0];
      }
      Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("gif");
      if (!iter.hasNext()) {
        return new PImage[0];
      }
      ImageReader r = iter.next();
      try (ImageInputStream iis = ImageIO.createImageInputStream(in)) {
        r.setInput(iis);
        int count = r.getNumImages(true);
        for (int i = 0; i < count; i++) {
          BufferedImage img = r.read(i);
          // NOTE: The PImage(java.awt.Image) constructor may not respect alpha
          PImage pImg = applet.createImage(img.getWidth(), img.getHeight(), ARGB);
          frames.add(pImg);
          pImg.loadPixels();
          img.getRGB(0, 0, img.getWidth(), img.getHeight(), pImg.pixels, 0, img.getWidth());
          pImg.updatePixels();
        }
      }
    } catch (IOException | RuntimeException ex) {
      // There's a potential "ArrayIndexOutOfBoundsException: 4096" from ImageIO loading GIFs
      logger.log(Level.SEVERE, "Error loading sprite: " + path, ex);
    }

    return frames.toArray(new PImage[0]);
  }

  /**
   * Finds shader files.
   */
  public static List<String> findShaderFiles(String path) {
    List<String> matches = new ArrayList<String>();
    File shaderDir = new File(path);
    for (final File fileEntry : shaderDir.listFiles()) {
      if (fileEntry.getName().endsWith(".frag"))
        matches.add(fileEntry.getName());
    }
    return matches;
  }

  /**
   * Calls {@link #findDataFiles(String, List)} after converting the varargs to a {@code List}.
   */
  public static List<String> findDataFiles(String path, String... exts) {
    return findDataFiles(path, Arrays.asList(exts));
  }

  /**
   * Finds the locations of all files under the data directory having specific extensions
   * under the given path. This does not search recursively. Extensions can be limited by
   * the suffixes in the {@code ext} argument, but if that argument is null or empty, then
   * all extensions are allowed.
   * <p>
   * If the path does not end in a '/' then one will be appended.</p>
   * <p>
   * All extensions <em>must</em> begin with a '.'.</p>
   *
   * @param path the path under which to search
   * @param exts the desired extensions, or empty or {@code null} to not limit the extension
   * @return a list of all the found files.
   */
  public static List<String> findDataFiles(String path, List<String> exts) {
    List<String> results = new ArrayList<>();
    if (exts == null) {
      exts = Collections.emptyList();
    }

    if (!path.endsWith("/")) {
      path = path + "/";
    }

    // TODO: Improve the following code; it works well enough for now
    // For example: "//path" won't have all leading '/' characters removed
    // Example: Just "."
    if (path.startsWith("./")) {
      path = path.substring(2);
    } else if (path.startsWith("/")) {
      path = path.substring(1);
    }
    String dataPath = "data/" + path;

    try {
      ClassPath cp = ClassPath.from(PathUtils.class.getClassLoader());
      for (ClassPath.ResourceInfo r : cp.getResources()) {
        String name = r.getResourceName();
        if (!name.startsWith(dataPath)) {
          continue;
        }
        name = name.substring(dataPath.length());
        if (name.contains("/")) {  // Only look at things in this directory
          continue;
        }
        String ext = null;
        if (!exts.isEmpty()) {
          int dotIndex = name.lastIndexOf('.');
          if (dotIndex < 0) {
            continue;
          }
          ext = name.substring(dotIndex);
        }
        if (ext == null || exts.contains(ext.toLowerCase())) {
          results.add(path + name);
        }
      }
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Error accessing resources", ex);
    }

    return results;
  }
}

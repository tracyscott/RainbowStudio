/*
 * Created by shawn on 8/2/18 4:46 PM.
 */
package com.giantrainbow;

import static processing.core.PConstants.ARGB;

import com.google.common.reflect.ClassPath;
import gifAnimation.GifDecoder;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
  public static PImage[] loadSprite(String path) {
    ArrayList<PImage> frames = new ArrayList<>();

    // gifAnimator isn't written well to handle exceptions properly :(
    try (InputStream in = RainbowStudio.pApplet.createInput(path)) {
      GifDecoder d = new GifDecoder();
      d.read(in);  // Boo, no exceptions :(

      int n = d.getFrameCount();
      for (int i = 0; i < n; i++) {
        BufferedImage f = d.getFrame(i);
        PImage img = RainbowStudio.pApplet.createImage(f.getWidth(), f.getHeight(), ARGB);
        frames.add(img);
        img.loadPixels();
        System.arraycopy(
            f.getRGB(0, 0, f.getWidth(), f.getHeight(), null, 0, f.getWidth()), 0,
            img.pixels, 0,
            f.getWidth() * f.getHeight());
      }
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Error loading sprite: " + path, ex);
    }

    return frames.toArray(new PImage[0]);
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

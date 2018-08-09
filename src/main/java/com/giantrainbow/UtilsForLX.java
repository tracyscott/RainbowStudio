/*
 * Created by shawn on 8/9/18 1:56 AM.
 */
package com.giantrainbow;

import heronarts.lx.audio.LXAudioOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;

/**
 * Utility functions for making it easier to work with the odd difficult LX thing.
 * <p>
 * The name was chosen to avoid making this seem like an LX class, "LXUtil".</p>
 */
public final class UtilsForLX {
  private static final Logger logger = Logger.getLogger(UtilsForLX.class.getName());

  private UtilsForLX() {
  }

  /**
   * Gets the internal {@code mediaPath} field from an {@link LXAudioOutput}.
   * If it can't be retrieved then a default of "." is returned.
   *
   * @param out the audio output object
   * @return its internal {@code mediaPath} value.
   */
  public static String getMediaPath(LXAudioOutput out) {
    Class<?> clazz = out.getClass();
    String mediaPath = ".";
    try {
      Field field = clazz.getDeclaredField("mediaPath");
      field.setAccessible(true);
      mediaPath = (String) field.get(out);
    } catch (ReflectiveOperationException ex) {
      logger.log(Level.WARNING, "Error getting mediaPath; defaulting to \".\"", ex);
    }
    return mediaPath;
  }

  /**
   * Copies audio input to a temporary file and returns a {@link File} object. This is
   * useful because currently, {@link LXAudioOutput} can only play files.
   *
   * @param inputName the input name, retrieved using
   *                  {@link processing.core.PApplet#createInput(String)}
   * @param out the audio output instance
   * @return a file suitable for playing via the given audio output object.
   */
  public static File copyAudioForOutput(PApplet pApplet, String inputName, LXAudioOutput out) {
    File audioFile = null;
    try (InputStream in = pApplet.createInput(inputName)) {
      if (in == null) {
        logger.warning("Missing audio input: " + inputName);
      } else  {
        String mediaPath = getMediaPath(out);
        File file = File.createTempFile("audio", ".tmp", new File(mediaPath));
        file.deleteOnExit();
        logger.info("Created temp file for audio: " + file);
        Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        audioFile = file;
      }
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Error copying audio file", ex);
    }
    return audioFile;
  }
}

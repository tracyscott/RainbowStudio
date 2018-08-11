/*
 * Created by shawn on 8/9/18 5:29 PM.
 */
package com.giantrainbow.patterns;

import static com.giantrainbow.RainbowStudio.GLOBAL_FRAME_RATE;
import static com.giantrainbow.RainbowStudio.pApplet;
import static com.giantrainbow.colors.Colors.BLACK;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.PI;

import com.giantrainbow.UtilsForLX;
import com.giantrainbow.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.io.File;
import java.util.logging.Logger;

// Sound resources were from here:
// https://forums.parallax.com/discussion/128129/cylon-eye-sound

/**
 * Cylon eye and sound.
 *
 * @author Shawn Silverman
 */
@LXCategory(LXCategory.FORM)
public class Cylon extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(Cylon.class.getName());

  private static final int INFINITE_PERIOD = Integer.MAX_VALUE;

  private static final int SILVER = 0xffc0c0c0;

  private static final int INITIAL_PERIOD = 2000;
  private final float BLOB_WIDTH = pg.width * 0.2f;
  private final float BLOB_HEIGHT = pg.height * 0.25f;

  private final float HELMET_STROKE_WIDTH = pg.height * 0.3f;
  private final float HELMET_CORNER_RADIUS = HELMET_STROKE_WIDTH * 2;

  private static final String SOUND_FILE = "sounds/cylon_eye.wav";

  private final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 0.5, 0.0, 1.0)
          .setDescription("Cycles per second");

  private int period;  // In ms
  private final int wavelength = pg.width * 2;  // Wavelength is twice the width
  private long phi;

  private long startTime;

  private File audioFile;
  private String oldAudioFileName;  // For restoring the original file
  private Boolean oldAudioLooping;  // For restoring the original looping setting

  public Cylon(LX lx) {
    super(lx, P2D);

    // Initial period
    phi = 0L;
    startTime = System.currentTimeMillis();
    period = INITIAL_PERIOD;
    speedKnob.setValue(1000.0 / period);


    // Watch for future speed changes
    speedKnob.addListener(lxParameter -> {
      double v = lxParameter.getValue();
      int p;
      if (v == 0.0) {
        p = INFINITE_PERIOD;
      } else {
        p = (int) (1000.0 / v);
      }
      adjustPeriod(System.currentTimeMillis() - startTime, p);
    });

    addParameter(speedKnob);

    // Write the audio to a temp file because LX's audio engine only works with File
    audioFile = UtilsForLX.copyAudioForOutput(pApplet, SOUND_FILE, lx.engine.audio.output);
  }

  @Override
  public void setup() {
    fpsKnob.setValue(GLOBAL_FRAME_RATE);

    // Start the audio
    oldAudioFileName = null;
    oldAudioLooping = null;
    if (audioFile != null) {
      logger.info("Starting audio: " + audioFile);
      oldAudioFileName = lx.engine.audio.output.file.getString();
      oldAudioLooping = lx.engine.audio.output.looping.getValueb();
      lx.engine.audio.output.file.setValue(audioFile.getName());
      lx.engine.audio.output.looping.setValue(true);
      lx.engine.audio.output.play.setValue(true);
    }
  }

  @Override
  public void onInactive() {
    super.onInactive();

    // Stop the audio
    if (audioFile != null) {
      logger.info("Stopping audio: " + audioFile);
      lx.engine.audio.output.play.setValue(false);
      lx.engine.audio.output.file.setValue(oldAudioFileName);
      lx.engine.audio.output.looping.setValue(oldAudioLooping);
    }
  }

  @Override
  protected void draw(double deltaDrawMs) {
    pg.background(BLACK);

    // The wave front will be in the range 0 to width*2
    int v = frontAt(System.currentTimeMillis() - startTime);
    v %= wavelength;
    if (v >= pg.width) {
      // Convert the range w->2w to w->0
      v = 2*pg.width - v;
    }
    float x = v - BLOB_WIDTH/2.0f;
    float y = (pg.height - BLOB_HEIGHT)/2.0f;
    pg.strokeWeight(1.0f);
    for (int i = 0; i < BLOB_WIDTH; i++) {
      float theta = (float) i / (BLOB_WIDTH - 1);
      int red = (int) (sin(PI*theta) * 255.0f);
      pg.stroke(Colors.rgb(red, 0, 0));
      pg.line(x, y, x, y + BLOB_HEIGHT);
      x++;
    }

    // Draw the helmet part
    pg.noFill();
    pg.stroke(SILVER);
    pg.strokeWeight(HELMET_STROKE_WIDTH);
    pg.rect(0, 0, pg.width, pg.height, HELMET_CORNER_RADIUS);
  }

  // Waves

  private int frontAt(long t) {
    if (period == INFINITE_PERIOD) {
      t = 0;
    }

    return (int) ((t - phi)*wavelength/period);
  }

  private void adjustPeriod(long t, int newPeriod) {
    if (newPeriod == Integer.MIN_VALUE) {
      newPeriod = INFINITE_PERIOD;
    }
    if (newPeriod == period) {
      return;
    }

    if (newPeriod == INFINITE_PERIOD) {
      phi = (phi - t)*newPeriod/period;
    } else if (period == INFINITE_PERIOD) {
      phi = t + phi*newPeriod/period;
    } else {
      phi = t + (phi - t)*newPeriod/period;
    }

    period = newPeriod;
  }
}

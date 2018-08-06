/*
 * Created by shawn on 8/5/18 10:45 PM.
 */
package com.giantrainbow.input;

import ddf.minim.analysis.BeatDetect;
import heronarts.lx.LX;
import heronarts.lx.audio.LXAudioInput;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * One place for all input.
 *
 * @author Shawn Silverman
 */
public class InputManager implements AutoCloseable {
  private BeatDetect beatDetect;
  private Timer beatSampleTimer;
  private Beats beats = new Beats();
  private float[] audioSample;
  private int audioSampleRate;
  private int audioSampleSize;

  private volatile boolean closed;

  public InputManager(LX lx) {
    startBeatDetect(lx);
  }

  private void startBeatDetect(LX lx) {
    LXAudioInput audioIn = lx.engine.audio.getInput();
    beatDetect = new BeatDetect(audioIn.mix.bufferSize(), audioIn.mix.sampleRate());
    beatSampleTimer = new Timer("Beat Detect Sample Timer");
    audioSample = new float[audioIn.mix.bufferSize()];
    audioSampleRate = audioIn.mix.sampleRate();
    audioSampleSize = audioIn.mix.bufferSize();

    long rate = Math.round((float) audioIn.mix.bufferSize() / (float) audioIn.mix.sampleRate() * 1000L);
    beatSampleTimer.scheduleAtFixedRate(new TimerTask() {
          @Override
          public void run() {
            synchronized (audioSample) {
              audioIn.mix.getSamples(audioSample);
            }
            beatDetect.detect(audioSample);
            synchronized (beats) {
              beats.timestamp = System.currentTimeMillis();
              beats.beats[0] = beatDetect.isKick();
              beats.beats[1] = beatDetect.isSnare();
              beats.beats[2] = beatDetect.isHat();
            }
          }
        }, 0L, rate);
  }

  @Override
  public void close() {
    synchronized (this) {
      if (closed) {
        return;
      }
      closed = true;
    }
    beatSampleTimer.cancel();
  }

  /**
   * Gets the latest audio sample. The sample size can be found from
   * {@link #getAudioSampleSize()}.
   * <p>
   * This will return the given sample array. If it is {@code null} then a new array
   * will be created and returned.</p>
   *
   * @param sample array in which to store the sample
   * @return the latest audio sample.
   */
  public float[] getAudioSample(float[] sample) {
    if (closed) {
      return new float[0];
    }
    if (sample == null) {
      sample = new float[audioSample.length];
    }
    synchronized (audioSample) {
      System.arraycopy(
          audioSample, 0,
          sample, 0,
          Math.min(audioSample.length, sample.length));
    }
    return sample;
  }

  /**
   * Gets the audio sample rate.
   *
   * @return the audio sample rate.
   */
  public float getAudioSampleRate() {
    if (closed) {
      return 0;
    }
    return audioSampleRate;
  }

  /**
   * Gets the audio sample size.
   *
   * @return the audio sample size.
   */
  public int getAudioSampleSize() {
    if (closed) {
      return 0;
    }
    return audioSampleSize;
  }

  /**
   * Gets the latest beats. This returns an object that keeps track of when the last beat
   * was received so that successive calls before new beats are calculated return false. To
   * make use of this feature, call this method once and then {@link #getBeats(Beats)} for
   * each time thereafter.
   *
   * @return
   */
  public Beats getBeats() {
    return getBeats(null);
  }

  /**
   * Gets the latest beats. This returns an object that keeps track of when the last beat
   * was received so that successive calls before new beats are calculated return false.
   * Passing {@code null} will create a new {@link Beats} object from the current beats state.
   * <p>
   * In other words, repeated calls before new beats are measured will return the actual beat
   * values once, and then {@code false} thereafter.</p>
   *
   * @param latest the timestamped beats retrieved from any previous call
   */
  public Beats getBeats(Beats latest) {
    if (closed) {
      return new Beats();
    }
    synchronized (beats) {
      if (latest == null) {
        return (Beats) beats.clone();
      }
      if (beats.timestamp >= latest.timestamp) {
        System.arraycopy(beats.beats, 0, latest.beats, 0, beats.beats.length);
        latest.timestamp = beats.timestamp;
      } else {
        // Set all the beats to zero
        Arrays.fill(latest.beats, false);
        latest.timestamp = System.currentTimeMillis();
      }
      return latest;
    }
  }

  /**
   * Beats with an associated timestamp. There are three beat ranges.
   */
  public static final class Beats implements Cloneable {
    private boolean[] beats = new boolean[beatsCount()];
    long timestamp;

    /**
     * Returns the number of beats in a set.
     */
    public static int beatsCount() {
      return 3;
    }

    public boolean isBeat(int i) {
      // Try/catch is free
      try {
        return beats[i];
      } catch (ArrayIndexOutOfBoundsException ex) {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return Objects.hash(beats, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Beats)) {
        return false;
      }
      Beats b = (Beats) obj;
      return Arrays.equals(this.beats, b.beats) && this.timestamp == b.timestamp;
    }

    protected Object clone() {
      try {
        Beats b = (Beats) super.clone();
        b.beats = this.beats.clone();
        return b;
      }  catch (CloneNotSupportedException ex) {
        // Ignore
      }
      return null;
    }
  }
}

/*
 * Created by shawn on 8/5/18 10:45 PM.
 */
package com.giantrainbow.input;

import ddf.minim.analysis.BeatDetect;
import heronarts.lx.LX;
import heronarts.lx.audio.LXAudioInput;
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
              beatDetect.detect(audioSample);
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
   * Gets an array of three booleans indicating low, mid, and high beats.
   *
   * @return any detected beats.
   */
  public boolean[] getBeats() {
    if (closed) {
      return new boolean[3];
    }
    synchronized (audioSample) {
      return new boolean[] { beatDetect.isKick(), beatDetect.isSnare(), beatDetect.isHat() };
    }
  }
}

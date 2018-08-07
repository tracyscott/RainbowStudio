/*
 * Created by shawn on 8/6/18 12:01 AM.
 * (c) 2014-2018 Shawn Silverman
 */
package com.giantrainbow.input;

/**
 * Implements a simple RC low-pass filter.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Low-pass_filter">Low-pass filter</a>
 *
 * @author Shawn Silverman
 */
public final class LowPassFilter {
  private float tau;
  private float y1;

  /**
   *
   * @param tau the time constant
   * @param y0 the initial value
   */
  public LowPassFilter(float tau, float y0) {
    this.tau = tau;
    this.y1 = y0;
  }

  public float next(float x, float dt) {
    float alpha = 1.0f/(tau/dt + 1.0f);
    float y = y1 + alpha*(x - y1);
    y1 = y;
    return y;
  }

  public void force(float y) {
    this.y1 = y;
  }
}

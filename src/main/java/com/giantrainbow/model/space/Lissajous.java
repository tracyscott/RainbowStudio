package com.giantrainbow.model.space;

public class Lissajous {

  public static float locationX(float A, float a, float delta, float t) {
    return A * (float) Math.sin(a * t + delta);
  }

  public static float locationY(float B, float b, float t) {
    return B * (float) Math.sin(b * t);
  }
};

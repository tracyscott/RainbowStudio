package com.giantrainbow.model.space;

import org.joml.Matrix4f; // https://github.com/JOML-CI/JOML
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Space3D {
  // The rainbow is centered at X=0
  public final float EYE_Z = -70;
  public final float EYE_Y = 6;
  public final float EYE_X = 0;

  // Objects should be at least this far from the viewer.
  public final float MIN_RADIUS = 200;

  // RainbowModel3D prints:
  // X: -43.042877 - 43.042877
  // Y:   6.066558 - 43.633526
  public final float MIN_X = -43.042877f;
  public final float MAX_X = 43.042877f;

  // MIN_Y is treated as an offset for EYE_Y, so the bottom of the
  // rainbow is ground-level.
  public final float MIN_Y = 6.066558f;
  public final float MAX_Y = 43.633526f;

  public final float WIDTH = MAX_X - MIN_X;
  public final float HEIGHT = MAX_Y - MIN_Y;

  static final float FAR_CLIP = Float.POSITIVE_INFINITY;

  public Space3D() {
    Vector3f eye = new Vector3f(EYE_X, EYE_Y + MIN_Y, EYE_Z);
    Vector3f lowerLeft = new Vector3f(MIN_X, MIN_Y, 0);
    Vector3f xAxis = new Vector3f(WIDTH, 0, 0);
    Vector3f yAxis = new Vector3f(0, HEIGHT, 0);

    Matrix4f view = new Matrix4f();
    Matrix4f proj = new Matrix4f();
    Matrix4f.projViewFromRectangle(eye, lowerLeft, xAxis, yAxis, FAR_CLIP, false, proj, view);

    Matrix4f viewproj = view.mul(proj);
    Vector4f mypoint = new Vector4f(MIN_X + 1, MIN_Y + 1, -1, 1);
    Vector4f output = new Vector4f();
    mypoint.mul(viewproj, output);

    System.err.println("mypoint" + mypoint + "\noutput" + output);

    System.err.println("x = " + (output.x / output.w) + "\ny = " + (output.y / output.w));
  }
}

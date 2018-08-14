package com.giantrainbow.model.space;

// JOML is the Java OpenGL Math Library
// https://github.com/JOML-CI/JOML
import org.joml.Matrix4f;
import org.joml.Vector3f;

// Space3D has some hard-coded parameters about the Rainbow 3D model.
// Ideally, it would take these parameters from the model itself, but
// that would require a bunch of refactoring (let's prove it's useful
// first).
//
// We use a right-handed coordinate system, so eye posistions should
// have positive Z values and test points should have negative
// positions.
public class Space3D {
  // RainbowModel3D prints:
  // X: -43.042877 - 43.042877
  // Y:   6.066558 - 43.633526
  public static final float MIN_X = -43.042877f;
  public static final float MAX_X = 43.042877f;

  // MIN_Y is treated as an offset for eye position, so the bottom of
  // the rainbow is ground-level.
  public static final float MIN_Y = 6.066558f;
  public static final float MAX_Y = 43.633526f;

  public static final float WIDTH = MAX_X - MIN_X;
  public static final float HEIGHT = MAX_Y - MIN_Y;

  public static final float FAR_CLIP = Float.POSITIVE_INFINITY;

  public Vector3f eye;
  public Vector3f center;
  public Matrix4f viewproj;

  // TODO although this accepts arbitrary eye positions, Z must be
  // positive and oblique viewing angles are not supported, so X
  // should be zero and Y should be (MIN_Y+MAX_Y)/2.  Search for
  // "oblique view frustum".
  //
  // Presently, if x or y are off center, you have distorted
  // perspective. :)
  public Space3D(float x, float y, float z) {
    this(new Vector3f(x, y, z));
  }

  public Space3D(Vector3f eye) {
    this.eye = eye;
    this.center = new Vector3f((MIN_X + MAX_X) / 2, (MIN_Y + MAX_Y) / 2, 0);

    Vector3f lowerLeft = new Vector3f(MIN_X, MIN_Y, 0);
    Vector3f xAxis = new Vector3f(WIDTH, 0, 0);
    Vector3f yAxis = new Vector3f(0, HEIGHT, 0);

    Matrix4f view = new Matrix4f();
    Matrix4f proj = new Matrix4f();
    Matrix4f.projViewFromRectangle(eye, lowerLeft, xAxis, yAxis, FAR_CLIP, false, proj, view);

    this.viewproj = proj.mul(view);
  }

  public boolean testPoint(float x, float y, float z) {
    return viewproj.testPoint(x, y, z);
  }

  public boolean testSphere(float x, float y, float z, float r) {
    return viewproj.testSphere(x, y, z, r);
  }

  public boolean testBox(float minx, float miny, float minz, float maxx, float maxy, float maxz) {
    return viewproj.testAab(minx, miny, minz, maxx, maxy, maxz);
  }

  // Vertical field of view
  public float fovy() {
    Vector3f et = new Vector3f(center.x, MAX_Y, center.z).sub(center);
    Vector3f eb = new Vector3f(center.x, MIN_Y, center.z).sub(center);

    float cosTheta = et.dot(eb) / (et.length() * eb.length());
    float theta = (float) Math.acos(cosTheta);
    return theta;
  }

  public float aspect() {
    return WIDTH / HEIGHT;
  }
}

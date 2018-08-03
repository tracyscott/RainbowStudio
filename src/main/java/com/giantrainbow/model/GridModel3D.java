package com.giantrainbow.model;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

public class GridModel3D extends LXModel {

  public final static int SIZE = 20;

  public GridModel3D() {
    super(new Fixture());
  }

  public static class Fixture extends LXAbstractFixture {
    Fixture() {
      for (int z = 0; z < SIZE; ++z) {
        for (int y = 0; y < SIZE; ++y) {
          for (int x = 0; x < SIZE; ++x) {
            addPoint(new LXPoint(x, y, z));
          }
        }
      }
    }
  }
}

LXModel buildModel() {
  // A three-dimensional grid model
  // return new GridModel3D();
  return new RainbowModel3D();
}

public static class GridModel3D extends LXModel {
  
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

public static class RainbowModel3D extends LXModel {
  public RainbowModel3D() {
    super(new Fixture());
  }
  
  public static class Fixture extends LXAbstractFixture {
    Fixture() {
      // add Points based on led position.
      // Starts at 9.3165082 degrees
      // arc is 161.3669836 degrees
      // end point at 170.6834918
      // radius is 36.9879
      
      // For each strip at the given radius, generate 420 points
      float pointsPerRow = 420;
      float z = 0;
      float r = 36.9879;  // Feet
      int ledCount = 0;
      for (int rowNum = 0; rowNum < 30; rowNum++) {
        r += 2.75 / 12.0;  // Each strip is separated by 2.75 inches.  r is in units of feet.
        for (float angle = 9.4165082; angle < 170.5834918; angle += 161.3669836 / pointsPerRow) {
          float x = r * cos(radians(angle));
          float y = r * sin(radians(angle));
          addPoint(new LXPoint(x, y, z));
          ledCount++;
        }
      }
      System.out.println("ledCount: " + ledCount);
    }
  }
}

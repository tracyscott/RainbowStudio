@LXCategory(LXCategory.FORM)
public class Lines extends LXPattern {
  public final CompoundParameter posKnob =
    new CompoundParameter("Pos", 0, 0, ((RainbowBaseModel)lx.model).pointsHigh)
    .setDescription("Controls where the line is. e.g. Animate with LFO.");
  public final CompoundParameter widthKnob =
    new CompoundParameter("Width", 0, ((RainbowBaseModel)lx.model).pointsHigh * 2)
    .setDescription("Controls the width of the line+deadspace");

  public Lines(LX lx) {
    super(lx);
    addParameter(posKnob);
    addParameter(widthKnob);
    widthKnob.setValue(2);
  }

  public void run(double deltaMs) {
    double position = this.posKnob.getValue();
    double lineWidth = widthKnob.getValue();

    int numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    int pointNumber = 0;      

    for (LXPoint p : model.points) {
      int rowNumber = pointNumber / numPixelsPerRow; // Ranges 0-29
      // TODO(tracy): Compute a brightness that dims as we move away from the line 
      // double brightness = 100.0 - (100.0/lineWidth) * Math.abs((rowNumber+1)%(lineWidth*2));
      double brightness = 0;
      if (((position + rowNumber) % lineWidth*2) < lineWidth)
        brightness = 100;
      else
        brightness = 0;
      if (brightness > 0) {
        colors[p.index] = LXColor.gray(brightness);
      } else {
        colors[p.index] = 0;
      }
      pointNumber++;
    }
  }
}

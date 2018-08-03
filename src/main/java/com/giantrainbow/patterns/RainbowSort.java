package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;

@LXCategory(LXCategory.FORM)
public class RainbowSort extends LXPattern {
  public final CompoundParameter swapsKnob =
    new CompoundParameter("Swaps", 1, 20).setDescription("Swaps per frame.");
  public final CompoundParameter brightnessKnob =
    new CompoundParameter("Bright", 1, 100).setDescription("Brightness.");
  public final CompoundParameter saturationKnob =
    new CompoundParameter("Sat", 1, 100).setDescription("Saturation");

  float hues[];
  float sortedHues[];
  Random rnd;

  public RainbowSort(LX lx) {
    super(lx);
    hues = new float[420];
    sortedHues = new float[420];
    rnd = new Random();
    resetSort();
    addParameter(swapsKnob);
    addParameter(brightnessKnob);
    addParameter(saturationKnob);
    brightnessKnob.setValue(100);
    saturationKnob.setValue(100);
    swapsKnob.setValue(5);

    for (int i = 0; i < hues.length; i++)
      hues[i] = 360.0f * i / hues.length;
  }

  // For each iteration of the run, do one sorting step.
  public void run(double deltaMs) {
    if (isSortDone()) {
      resetSort();
    }

    for (int j = 0; j < swapsKnob.getValue(); j++) {
      while (!swap()) {
      }
      if (isSortDone())
        break;
    }

    setOutput();
  }

  public void setOutput() {
    int pointNumber = 0;
    for (LXPoint p : model.points) {
      int pointCol = pointNumber % ((RainbowBaseModel)lx.model).pointsWide;
      colors[p.index] = LXColor.hsb(sortedHues[pointCol], saturationKnob.getValue(),
        brightnessKnob.getValue());
      ++pointNumber;
    }
  }

  protected boolean swap() {
    int indexA = rnd.nextInt(sortedHues.length);
    int indexB = indexA;

    // Pick to random indexes, compare and swap.
    while (indexA == indexB) {
      indexB = rnd.nextInt(sortedHues.length);
    }

    float hueA = sortedHues[indexA];
    float hueB = sortedHues[indexB];
    if (indexA < indexB) {
      if (hueB < hueA) {
        sortedHues[indexA] = hueB;
        sortedHues[indexB] = hueA;
        return true;
      } else {
        return false;
      }
    } else {
      // indexB < indexA
      if (hueA < hueB) {
        sortedHues[indexA] = hueB;
        sortedHues[indexB] = hueA;
        return true;
      } else {
        return false;
      }
    }
  }

  protected boolean isSortDone() {
    for (int i = 1; i < sortedHues.length; i++) {
      if (sortedHues[i-1] > sortedHues[i])
        return false;
    }
    return true;
  }

  protected void resetSort() {
    for (int i = 0; i < sortedHues.length; i++)
      sortedHues[i] = hues[i];
    for (int i = hues.length; i > 1; i--) {
	int a = rnd.nextInt(i);
	int b = i - 1;
	float tmp = sortedHues[a];
	sortedHues[a] = sortedHues[b];
	sortedHues[b] = tmp;
    }
  }
}

package com.giantrainbow.patterns;

// 1 colors
// Top to bottom
// LGBT 6 Bands  (228,3,3) (255,140,0) (255,237,0) (0,128,38) (0,77,255) (117,7,135)
// Bisexual (214, 2, 112) 123p (155,79,150) 61p  (0,56,168) 123p, so 2:1
// Transgender (91, 206, 250) (245,169,184) (255, 255, 255) (245,169,184) (91,206, 250)
/*
 * Flags
 *
 */

import static processing.core.PApplet.round;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

public class Flags extends LXPattern {

  public final DiscreteParameter flagKnob =
    new DiscreteParameter("Flag", 0, 2)
    .setDescription("Which flag.");

  int[] lgbtFlag;
  int[] biFlag;
  int[] transFlag;
  int[][] flags;
  int[] flag;

  public Flags(LX lx) {
    super(lx);
    flags = new int[][] {new int[1], new int[1], new int[1]};
    lgbtFlag = new int[6];
    lgbtFlag[0] = LXColor.rgb(117, 7, 135);
    lgbtFlag[1] = LXColor.rgb(0, 77, 255);
    lgbtFlag[2] = LXColor.rgb(0, 128, 38);
    lgbtFlag[3] = LXColor.rgb(255, 237, 0);
    lgbtFlag[4] = LXColor.rgb(255, 140, 0);
    lgbtFlag[5] = LXColor.rgb(228, 3, 3);
    flags[0] = lgbtFlag;
    transFlag = new int[5];
    transFlag[0] = LXColor.rgb(91, 206, 250);
    transFlag[1] = LXColor.rgb(245, 169, 184);
    transFlag[2] = LXColor.rgb(255, 255, 255);
    transFlag[3] = LXColor.rgb(245, 169, 184);
    transFlag[4] = LXColor.rgb(91, 206, 250);
    flags[1] = transFlag;
    biFlag = new int[3];
    biFlag[0] = LXColor.rgb(0, 56, 178);
    biFlag[1] = LXColor.rgb(155, 79, 150);
    biFlag[2] = LXColor.rgb(214, 2, 112);
    flags[2] = biFlag;

    flagKnob.setValue(0);
    addParameter(flagKnob);
    flag = flags[(int)round((float)(flagKnob.getValue()))];
  }

  public void run(double deltaMs) {
    int numRows = ((RainbowBaseModel)lx.model).pointsHigh;
    int flagNum = (int)round((float)(flagKnob.getValue()));
    if (flagNum < 0) flagNum = 0;
    if (flagNum > flags.length - 1) flagNum = flags.length - 1;
    flag = flags[flagNum];
    int numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    int pointNumber = 0;
    for (LXPoint p : model.points) {
      int rowNumber = pointNumber / numPixelsPerRow;
      if (flag == lgbtFlag || flag == transFlag) {
        colors[p.index] = flag[rowNumber / (numRows/flag.length)];
      } else if (flag == biFlag) {
        // 2-1-2 ratio of thickness = 5 so each unit is 6 rows 12-6-12
        if (rowNumber > 17) {
          colors[p.index] = biFlag[2];
        } else if (rowNumber < 12) {
          colors[p.index] = biFlag[1];
        } else {
          colors[p.index] = biFlag[0];
        }
      }
      ++pointNumber;
    }
  }
}

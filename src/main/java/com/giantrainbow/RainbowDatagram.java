package com.giantrainbow;

import heronarts.lx.LX;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.parameter.LXParameter;

/*
 * Based on TenereDatagram.  Use a LUT for Gamma correction for speed.
 */
public class RainbowDatagram extends ArtNetDatagram {
  private final LXParameter brightness;
  int universe = 0;
  int dataLength = 0;

  public RainbowDatagram(LX lx, int[] indices, byte channel) {
    super(indices, channel);
    dataLength = indices.length * 3;
    this.brightness = lx.engine.output.brightness;
  }

  public RainbowDatagram(LX lx, int[] indices, int universeNumber) {
    super(indices, 3*indices.length, universeNumber);
    this.brightness = lx.engine.output.brightness;
    dataLength = indices.length * 3;
  }

  public RainbowDatagram(LX lx, int[] indices, int dataLength, int universeNumber) {
    super(indices, dataLength, universeNumber);
    this.brightness = lx.engine.output.brightness;
    this.dataLength = dataLength;
  }


  @Override
  protected LXDatagram copyPoints(int[] colors, int[] pointIndices, int offset) {
    // final byte[] gamma = Gamma.GAMMA_LUT[Math.round(255 * this.brightness.getValuef())];
    final byte[] gammaRed = Gamma.GAMMA_LUT_RED[Math.round(255 * this.brightness.getValuef())];
    final byte[] gammaGreen = Gamma.GAMMA_LUT_GREEN[Math.round(255 * this.brightness.getValuef())];
    final byte[] gammaBlue = Gamma.GAMMA_LUT_BLUE[Math.round(255 * this.brightness.getValuef())];

    int i = offset;
    // TODO(tracy): Modify gamma correction based on background color at this point
    // on the Rainbow.  For example, reduce power to leds backed by yellow.
    for (int index : pointIndices) {
      int c = (index >= 0) ? colors[index] : 0xff000000;
      this.buffer[i    ] = gammaRed[0xff & (c >> 16)]; // R
      this.buffer[i + 1] = gammaGreen[0xff & (c >> 8)]; // G
      this.buffer[i + 2] = gammaBlue[0xff & c]; // B
      i += 3;
      // Deal with an issue where the passed in pointIndices array is larger than the
      // dataLength we used to construct the object.
      if (i >= this.buffer.length)
        break;
    }
    return this;
  }
}

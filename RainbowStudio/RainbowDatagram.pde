/*
 * Based on TenereDatagram.  Use a LUT for Gamma correction for speed.
 */
public static class RainbowDatagram extends ArtNetDatagram {

  static final float GAMMA = 1.8;
  
  static final byte[][] GAMMA_LUT = new byte[256][256];
  
  static {
    for (int b = 0; b < 256; ++b) {
      for (int in = 0; in < 256; ++in) {
        GAMMA_LUT[b][in] = (byte) (0xff & (int) Math.round(Math.pow(in * b / 65025.f, GAMMA) * 255.f));
      }
    }
  }
    
  private final LXParameter brightness; 
    
  public RainbowDatagram(LX lx, int[] indices, byte channel) {
    super(indices, channel);
    this.brightness = lx.engine.output.brightness;
  }
  
  public RainbowDatagram(LX lx, int[] indices, int universeNumber) {
    super(indices, 3*indices.length, universeNumber);
    this.brightness = lx.engine.output.brightness;
  }

  
  @Override
  protected LXDatagram copyPoints(int[] colors, int[] pointIndices, int offset) {
    final byte[] gamma = GAMMA_LUT[Math.round(255 * this.brightness.getValuef())];
    int i = offset;
    // TODO(tracy): Modify gamma correction based on background color at this point
    // on the Rainbow.  For example, reduce power to leds backed by yellow.
    for (int index : pointIndices) {
      int c = (index >= 0) ? colors[index] : #000000;
      this.buffer[i    ] = gamma[0xff & (c >> 16)]; // R
      this.buffer[i + 1] = gamma[0xff & (c >> 8)]; // G
      this.buffer[i + 2] = gamma[0xff & c]; // B
      i += 3;
    }
    return this;
  }

}

public static class Gamma {
  static float defaultGamma = 1.8;
  static float defaultGammaRed = 1.8;
  static float defaultGammaGreen = 1.8;
  static float defaultGammaBlue = 1.8;
  
  static final byte[][] GAMMA_LUT = new byte[256][256];
  static final byte[][] GAMMA_LUT_RED = new byte[256][256];
  static final byte[][] GAMMA_LUT_GREEN = new byte[256][256];
  static final byte[][] GAMMA_LUT_BLUE = new byte[256][256];
  
  static {
    buildGammaLUT(defaultGamma);
    buildRedGammaLUT(defaultGammaRed);
    buildGreenGammaLUT(defaultGammaGreen);
    buildBlueGammaLUT(defaultGammaBlue);
  }
  
  static public void buildGammaLUT(float gamma) {
    for (int b = 0; b < 256; ++b) {
      for (int in = 0; in < 256; ++in) {
        GAMMA_LUT[b][in] = (byte) (0xff & (int) Math.round(Math.pow(in * b / 65025.f, gamma) * 255.f));
      }
    }
  }
  
  static public void buildRedGammaLUT(float gammaRed) {
    for (int b = 0; b < 256; ++b) {
      for (int in = 0; in < 256; ++in) {
        GAMMA_LUT_RED[b][in] = (byte) (0xff & (int) Math.round(Math.pow(in * b / 65025.f, gammaRed) * 255.f));
      }
    }
  }

  static public void buildGreenGammaLUT(float gammaGreen) {
    for (int b = 0; b < 256; ++b) {
      for (int in = 0; in < 256; ++in) {
        GAMMA_LUT_GREEN[b][in] = (byte) (0xff & (int) Math.round(Math.pow(in * b / 65025.f, gammaGreen) * 255.f));
      }
    }
  }

  static public void buildBlueGammaLUT(float gammaBlue) {
    for (int b = 0; b < 256; ++b) {
      for (int in = 0; in < 256; ++in) {
        GAMMA_LUT_BLUE[b][in] = (byte) (0xff & (int) Math.round(Math.pow(in * b / 65025.f, gammaBlue) * 255.f));
      }
    }
  }
}

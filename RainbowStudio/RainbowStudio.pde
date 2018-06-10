/** 
 * By using LX Studio, you agree to the terms of the LX Studio Software
 * License and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 */

// ---------------------------------------------------------------------------
//
// Welcome to LX Studio! Getting started is easy...
// 
// (1) Quickly scan this file
// (2) Look at "Model" to define your model
// (3) Move on to "Patterns" to write your animations
// 
// ---------------------------------------------------------------------------

// Reference to top-level LX instance
heronarts.lx.studio.LXStudio lx;

static public final int ARTNET_PORT = 6454;
static public final String LED_CONTROLLER_IP = "192.168.2.1";

static public final int FULL_RAINBOW = 0;
static public final int SRIKANTH_PANEL = 1;

void setup() {
  // Processing setup, constructs the window and the LX instance
  size(800, 720, P3D);
 
  int modelType = FULL_RAINBOW; // FULL_RAINBOW;
  
  LXModel model = buildModel(modelType);
  lx = new heronarts.lx.studio.LXStudio(this, model, MULTITHREADED);
  lx.ui.setResizable(RESIZABLE);
  
  if (modelType == FULL_RAINBOW) {
    // Uncomment this to generate ArtNet packets. Also, you will need to configure
    // the IP and Port in the method below.
    // RainbowModel3D.configureOutput(lx);
  } else if (modelType == SRIKANTH_PANEL) {
    // SrikanthPanel.configureOutput(lx);
  }
}

void initialize(final heronarts.lx.studio.LXStudio lx, heronarts.lx.studio.LXStudio.UI ui) {
  // Add custom components or output drivers here
}

void onUIReady(heronarts.lx.studio.LXStudio lx, heronarts.lx.studio.LXStudio.UI ui) {
  // Add custom UI components here
}

void draw() {
  // All is handled by LX Studio
}

// Configuration flags
final static boolean MULTITHREADED = true;
final static boolean RESIZABLE = true;

// Helpful global constants
final static float INCHES = 1;
final static float IN = INCHES;
final static float FEET = 12 * INCHES;
final static float FT = FEET;
final static float CM = IN / 2.54;
final static float MM = CM * .1;
final static float M = CM * 100;
final static float METER = M;

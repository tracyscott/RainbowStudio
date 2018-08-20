/*
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

package com.giantrainbow;

import com.giantrainbow.model.RainbowBaseModel;
import com.giantrainbow.model.RainbowModel3D;
import com.giantrainbow.model.SimplePanel;
import com.giantrainbow.ui.UIAudioMonitorLevels;
import com.giantrainbow.ui.UIGammaSelector;
import com.giantrainbow.ui.UIMidiControl;
import com.giantrainbow.ui.UIModeSelector;
import com.giantrainbow.ui.UIPixliteConfig;
import com.google.common.reflect.ClassPath;
import com.google.gson.JsonObject;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.midi.LXMidiOutput;
import heronarts.lx.model.LXModel;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI3dContext;
import heronarts.p3lx.ui.UIEventHandler;
import heronarts.p3lx.ui.component.UIGLPointCloud;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import processing.core.PApplet;
import processing.event.KeyEvent;

public class RainbowStudio extends PApplet {
  static {
    System.setProperty(
        "java.util.logging.SimpleFormatter.format",
        "%3$s: %1$tc [%4$s] %5$s%6$s%n");

    // JOGL debugging:
    // The first enables just Animator debugging
    // The second enables all
//    System.setProperty("jogl.debug.Animator", "true");
//    System.setProperty("jogl.debug", "true");
  }

  /**
   * Set the main logging level here.
   *
   * @param level the new logging level
   */
  public static void setLogLevel(Level level) {
    // Change the logging level here
    Logger root = Logger.getLogger("");
    root.setLevel(level);
    for (Handler h : root.getHandlers()) {
      h.setLevel(level);
    }
  }

  /**
   * Adds logging to a file. The file name will be appended with a dash, date stamp, and
   * the extension ".log".
   *
   * @param prefix prefix of the log file name
   * @throws IOException if there was an error opening the file.
   */
  public static void addLogFileHandler(String prefix) throws IOException {
    String suffix = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    Logger root = Logger.getLogger("");
    Handler h = new FileHandler(prefix + "-" + suffix + ".log");
    h.setFormatter(new SimpleFormatter());
    root.addHandler(h);
  }

  private static final Logger logger = Logger.getLogger(RainbowStudio.class.getName());

  public static void main(String[] args) {
    PApplet.main(RainbowStudio.class.getName(), args);
  }

  private static final String LOG_FILENAME_PREFIX = "rainbowstudio";

  // Reference to top-level LX instance
  private heronarts.lx.studio.LXStudio lx;

  public static PApplet pApplet;

  public static final boolean disableOutputOnStart = true;
  public static final int GLOBAL_FRAME_RATE = 60;
  public static final boolean enableArtNet = false;
  public static final int ARTNET_PORT = 6454;
  public static final String LED_CONTROLLER_IP = "192.168.2.134";

  private static final int FULL_RAINBOW = 0;
  private static final int SRIKANTH_PANEL = 1;
  private static final int RAINBOW_PANEL = 2;
  private static final int LARGE_PANEL = 3;
  private static final int RAINBOW_PANEL_4 = 4;
  private static final int RAINBOW_PANEL_2 = 5;
  private static final int RAINBOW_PANEL_1 = 6;
  private static final int RAINBOW_START_PANEL = 7;
  private static final int RAINBOW_END_PANEL = 8;
  private static final int MODEL_TYPE = FULL_RAINBOW; // RAINBOW_PANEL, RAINBOW_PANEL_4 or FULL_RAINBOW

  /** Stores a registry of commonly-used global things. */
  public Registry registry;

  public static boolean fullscreenMode = false;
  private static UI3dContext fullscreenContext;
  private static UIGammaSelector gammaControls;
  private static UIModeSelector modeSelector;
  private static UIAudioMonitorLevels audioMonitorLevels;
  private static UIPixliteConfig pixliteConfig;
  public static UIMidiControl uiMidiControl;

  @Override
  public void settings() {
    size(1024, 600, P3D);
  }

  /**
   * Registers all patterns and effects that LX doesn't already have registered.
   * This check is important because LX just adds to a list.
   *
   * @param lx the LX environment
   */
  private void registerAll(LXStudio lx) {
    List<Class<? extends LXPattern>> patterns = lx.getRegisteredPatterns();
    List<Class<? extends LXEffect>> effects = lx.getRegisteredEffects();
    final String parentPackage = getClass().getPackage().getName();

    try {
      ClassPath classPath = ClassPath.from(getClass().getClassLoader());
      for (ClassPath.ClassInfo classInfo : classPath.getAllClasses()) {
        // Limit to this package and sub-packages
        if (!classInfo.getPackageName().startsWith(parentPackage)) {
          continue;
        }
        Class<?> c = classInfo.load();
        if (Modifier.isAbstract(c.getModifiers())) {
          continue;
        }
        if (LXPattern.class.isAssignableFrom(c)) {
          Class<? extends LXPattern> p = c.asSubclass(LXPattern.class);
          if (!patterns.contains(p)) {
            lx.registerPattern(p);
            logger.info("Added pattern: " + p);
          }
        } else if (LXEffect.class.isAssignableFrom(c)) {
          Class<? extends LXEffect> e = c.asSubclass(LXEffect.class);
          if (!effects.contains(e)) {
            lx.registerEffect(e);
            logger.info("Added effect: " + e);
          }
        }
      }
    } catch (IOException ex) {
      logger.log(Level.WARNING, "Error finding pattern and effect classes", ex);
    }
  }

  @Override
  public void setup() {
    // Processing setup, constructs the window and the LX instance
    pApplet = this;

    try {
      addLogFileHandler(LOG_FILENAME_PREFIX);
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Error creating log file: " + LOG_FILENAME_PREFIX, ex);
    }

    LXModel model = buildModel(MODEL_TYPE);
    logger.info("Current renderer:" + sketchRenderer());
    logger.info("Current graphics:" + getGraphics());
    logger.info("Current graphics is GL:" + getGraphics().isGL());
    logger.info("Multithreaded hint: " + MULTITHREADED);
    logger.info("Multithreaded actually: " + (MULTITHREADED && !getGraphics().isGL()));
    lx = new LXStudio(this, model, MULTITHREADED && !getGraphics().isGL());

    // Common components stored (dumbly) as static variables
    registry = new Registry(this, lx);

    lx.ui.setResizable(RESIZABLE);

    modeSelector = (UIModeSelector) new UIModeSelector(lx.ui, lx).setExpanded(true).addToContainer(lx.ui.leftPane.global);
    gammaControls = (UIGammaSelector) new UIGammaSelector(lx.ui)
        .setExpanded(false).addToContainer(lx.ui.leftPane.global);
    audioMonitorLevels = (UIAudioMonitorLevels) new UIAudioMonitorLevels(lx.ui).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    pixliteConfig = (UIPixliteConfig) new UIPixliteConfig(lx.ui).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    uiMidiControl = (UIMidiControl) new UIMidiControl(lx.ui, lx, modeSelector).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    lx.engine.midi.addListener(uiMidiControl);

    if (MODEL_TYPE == RAINBOW_PANEL) {
      // Manually force the camera settings for a single panel.  A single panel is
      // way at the top of the world space and it is difficult to zoom in on it.
      float cameraY = RainbowBaseModel.innerRadius +
          (RainbowBaseModel.outerRadius - RainbowBaseModel.innerRadius) / 2.0f;
      lx.ui.preview.setCenter(0.0f, cameraY, 0.0f);
      lx.ui.preview.setRadius(8.0f);
    }

    // Output the model bounding box for reference.
    logger.info("minx, miny: " + model.xMin + "," + model.yMin);
    logger.info("maxx, maxy: " + model.xMax + "," + model.yMax);
    logger.info("bounds size: " + (model.xMax - model.xMin) + "," + (model.yMax - model.yMin));

    int texturePixelsWide = ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot) * 2;
    int texturePixelsHigh = ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot);
    logger.info("texture image size: " + texturePixelsWide + "x" + texturePixelsHigh);

    int innerRadiusPixels = floor(RainbowBaseModel.innerRadius * RainbowBaseModel.pixelsPerFoot);
    int outerRadiusPixels = ceil(RainbowBaseModel.outerRadius * RainbowBaseModel.pixelsPerFoot);
    logger.info("innerRadiusPixels = " + innerRadiusPixels);
    logger.info("outerRadiusPixels = " + outerRadiusPixels);

    // FULL_RAINBOW is
    // rectangle bounds size: 86.52052, 37.74478
    // Roughly, 87, 38 feet with led's per 2 inch (highest density) = 87*6, 38*6 = 522x228
    // 86.52052 * 6 = 519.12312
    // 37.74478 * 6 = 226.46868
    // NOTE(tracy): Using images at larger sizes reduces aliasing artifacts
    // when not resorting to averaging neighbors in the pattern code.

    if (enableArtNet) {
      switch (MODEL_TYPE) {
        case FULL_RAINBOW:
          SimplePanel.configureOutputMultiPanel(lx);
          break;
        case SRIKANTH_PANEL:
          SimplePanel.configureOutputSrikanthPanel(lx);
          break;
        case RAINBOW_PANEL:
          SimplePanel.configureOutputRainbowPanel(lx);
          break;
        case RAINBOW_PANEL_4:
          SimplePanel.configureOutputMultiPanel(lx);
          break;
        case RAINBOW_PANEL_2:
          SimplePanel.configureOutputMultiPanel(lx);
          break;
        case RAINBOW_PANEL_1:
          SimplePanel.configureOutputMultiPanel(lx);
          break;
        case RAINBOW_START_PANEL:
          Output.configureOutputMultiPanel(lx, true, false);
          break;
        case RAINBOW_END_PANEL:
          Output.configureOutputMultiPanel(lx, false, true);
      }
    }
    if (disableOutputOnStart)
      lx.engine.output.enabled.setValue(false);

    // Check for data/PLAYASIDE
    try (InputStream in = createInput("PLAYASIDE")) {
      if (in != null) {
        logger.info("\"PLAYASIDE\" exists.");
        modeSelector.autoAudioModeP.setValue(true);
      } else {
        logger.info("\"PLAYASIDE\" does not exist.");
        modeSelector.autoAudioModeP.setValue(false);
      }
    } catch (IOException ex) {
      // Do nothing; it's the result of auto-closing the InputStream
    }

    // Dump our MIDI device names for reference.
    LXMidiEngine midi = lx.engine.midi;
    for (LXMidiOutput output : midi.outputs) {
      logger.info(output.getName() + ": " + output.getDescription());
    }

    // Support Fullscreen Mode.  We create a second UIGLPointCloud and
    // add it to a LXStudio.UI layer.  When entering fullscreen mode,
    // toggleFullscreen() will set the
    // standard UI components visibility to false and the larger
    // fullscreenContext visibility to true.
    UIGLPointCloud fullScreenPointCloud = new UIGLPointCloud(lx);
    fullscreenContext = new UI3dContext(lx.ui);
    fullscreenContext.addComponent(fullScreenPointCloud);
    lx.ui.addLayer(fullscreenContext);
    fullscreenContext.setVisible(false);

    lx.ui.setTopLevelKeyEventHandler(new TopLevelKeyEventHandler());
    frameRate(GLOBAL_FRAME_RATE);
  }

  public class TopLevelKeyEventHandler extends UIEventHandler {
    TopLevelKeyEventHandler() {
      super();
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
      super.onKeyPressed(keyEvent, keyChar, keyCode);
      if (keyCode == 70) {
        toggleFullscreen();
      }
    }
  }

  private void toggleFullscreen() {
    if (!fullscreenMode) {
      lx.ui.leftPane.setVisible(false);
      lx.ui.rightPane.setVisible(false);
      lx.ui.helpBar.setVisible(false);
      lx.ui.bottomTray.setVisible(false);
      lx.ui.toolBar.setVisible(false);
      lx.ui.preview.setVisible(false);

      fullscreenContext.setVisible(true);
      fullscreenMode = true;
    } else {
      fullscreenContext.setVisible(false);

      lx.ui.leftPane.setVisible(true);
      lx.ui.rightPane.setVisible(true);
      lx.ui.helpBar.setVisible(true);
      lx.ui.bottomTray.setVisible(true);
      lx.ui.toolBar.setVisible(true);
      lx.ui.preview.setVisible(true);
      fullscreenMode = false;
    }
  }

  private class Settings extends LXComponent {

    private final LXStudio.UI ui;

    private Settings(LX lx, LXStudio.UI ui) {
      super(lx);
      this.ui = ui;
    }

    private static final String KEY_STDMODE_TIME = "stdModeTime";
    private static final String KEY_STDMODE_TIME2 = "stdModeTime2";
    private static final String KEY_STDMODE_TIME3 = "stdModeTime3";

    private static final String KEY_STDMODE_FADETIME = "stdModeFadeTime";

    private static final String KEY_AUDIOMONITOR_MINTHR = "minThr";
    private static final String KEY_AUDIOMONITOR_AVGTS = "avgTs";
    private static final String KEY_AUDIOMONITOR_GAININC = "gainInc";
    private static final String KEY_AUDIOMONITOR_RTHRSH = "reduceThrsh";
    private static final String KEY_AUDIOMONITOR_GTHRSH = "gainThrsh";

    private static final String KEY_GAMMA_RED = "gammaRed";
    private static final String KEY_GAMMA_GREEN = "gammaGreen";
    private static final String KEY_GAMMA_BLUE = "gammaBlue";

    private static final String KEY_PIXLITE1_IP = "pixlite1Ip";
    private static final String KEY_PIXLITE1_PORT = "pixlite1Port";
    private static final String KEY_PIXLITE2_IP = "pixlite2Ip";
    private static final String KEY_PIXLITE2_PORT = "pixlite2Port";
    private static final String KEY_MIDICTRL_MIDICH = "midiCtrlMidiCh";
    private static final String KEY_MIDICTRL_NXTGAME = "midiCtrlNxtGame";
    private static final String KEY_MIDICTRL_PRVGAME = "midiCtrlPrvGame";
    private static final String KEY_MIDICTRL_AUDIOM = "midiCtrlAudioM";
    private static final String KEY_MIDICTRL_STDM = "midiCtrlStdM";
    private static final String KEY_MIDICTRL_INSTRM = "midiCtlrInstrM";
    private static final String KEY_MIDICTRL_INTERM = "midiCtrlInterM";
    private static final String KEY_MIDICTRL_AUTOAU = "midiCtrlAutoAu";


    @Override
    public void save(LX lx, JsonObject obj) {
      obj.addProperty(KEY_AUDIOMONITOR_MINTHR, UIAudioMonitorLevels.minThresholdP.getValue());
      obj.addProperty(KEY_AUDIOMONITOR_AVGTS, UIAudioMonitorLevels.avgTimeP.getValue());
      obj.addProperty(KEY_AUDIOMONITOR_GAININC, UIAudioMonitorLevels.gainIncP.getValue());
      obj.addProperty(KEY_AUDIOMONITOR_RTHRSH, UIAudioMonitorLevels.reduceThrshP.getValue());
      obj.addProperty(KEY_AUDIOMONITOR_GTHRSH, UIAudioMonitorLevels.gainThrshP.getValue());

      obj.addProperty(KEY_STDMODE_TIME, UIModeSelector.timePerChannelP.getValue());
      obj.addProperty(KEY_STDMODE_TIME2, UIModeSelector.timePerChannelP2.getValue());
      obj.addProperty(KEY_STDMODE_TIME3, UIModeSelector.timePerChannelP3.getValue());
      obj.addProperty(KEY_STDMODE_FADETIME, UIModeSelector.fadeTimeP.getValue());
      
      obj.addProperty(KEY_GAMMA_RED, UIGammaSelector.redGamma.getValue());
      obj.addProperty(KEY_GAMMA_GREEN, UIGammaSelector.greenGamma.getValue());
      obj.addProperty(KEY_GAMMA_BLUE, UIGammaSelector.blueGamma.getValue());
      obj.addProperty(KEY_PIXLITE1_IP, UIPixliteConfig.pixlite1IpP.getString());
      obj.addProperty(KEY_PIXLITE1_PORT, UIPixliteConfig.pixlite1PortP.getString());
      obj.addProperty(KEY_PIXLITE2_IP, UIPixliteConfig.pixlite2IpP.getString());
      obj.addProperty(KEY_PIXLITE2_PORT, UIPixliteConfig.pixlite2PortP.getString());

      obj.addProperty(KEY_MIDICTRL_MIDICH, UIMidiControl.midiChP.getValue());
      obj.addProperty(KEY_MIDICTRL_NXTGAME, UIMidiControl.nextGameP.getValue());
      obj.addProperty(KEY_MIDICTRL_PRVGAME, UIMidiControl.prevGameP.getValue());
      obj.addProperty(KEY_MIDICTRL_AUDIOM, UIMidiControl.audioModeP.getValue());
      obj.addProperty(KEY_MIDICTRL_STDM, UIMidiControl.standardModeP.getValue());
      obj.addProperty(KEY_MIDICTRL_INSTRM, UIMidiControl.instrumentModeP.getValue());
      obj.addProperty(KEY_MIDICTRL_INTERM, UIMidiControl.interactiveModeP.getValue());
      obj.addProperty(KEY_MIDICTRL_AUTOAU, UIMidiControl.autoAudioP.getValue());
    }

    @Override
    public void load(LX lx, JsonObject obj) {
      logger.info("Loading settings....");
      if (obj.has(KEY_AUDIOMONITOR_AVGTS)) {
        UIAudioMonitorLevels.avgTimeP.setValue(obj.get(KEY_AUDIOMONITOR_AVGTS).getAsDouble());
      }
      if (obj.has(KEY_AUDIOMONITOR_MINTHR)) {
        UIAudioMonitorLevels.minThresholdP.setValue(obj.get(KEY_AUDIOMONITOR_MINTHR).getAsDouble());
      }
      if (obj.has(KEY_AUDIOMONITOR_GAININC)) {
        UIAudioMonitorLevels.gainIncP.setValue(obj.get(KEY_AUDIOMONITOR_GAININC).getAsDouble());
      }
      if (obj.has(KEY_AUDIOMONITOR_RTHRSH)) {
        UIAudioMonitorLevels.reduceThrshP.setValue(obj.get(KEY_AUDIOMONITOR_RTHRSH).getAsDouble());
      }
      if (obj.has(KEY_AUDIOMONITOR_GTHRSH)) {
        UIAudioMonitorLevels.gainThrshP.setValue(obj.get(KEY_AUDIOMONITOR_GTHRSH).getAsDouble());
      }
      if (obj.has(KEY_STDMODE_TIME)) {
        UIModeSelector.timePerChannelP.setValue(obj.get(KEY_STDMODE_TIME).getAsDouble());
      }
      if (obj.has(KEY_STDMODE_TIME2)) {
        UIModeSelector.timePerChannelP2.setValue(obj.get(KEY_STDMODE_TIME2).getAsDouble());
      }
      if (obj.has(KEY_STDMODE_TIME3)) {
        UIModeSelector.timePerChannelP3.setValue(obj.get(KEY_STDMODE_TIME3).getAsDouble());
      }
      if (obj.has(KEY_STDMODE_FADETIME)) {
        UIModeSelector.fadeTimeP.setValue(obj.get(KEY_STDMODE_FADETIME).getAsDouble());
      }
      if (obj.has(KEY_GAMMA_RED)) {
        UIGammaSelector.redGamma.setValue(obj.get(KEY_GAMMA_RED).getAsDouble());
      }
      if (obj.has(KEY_GAMMA_GREEN)) {
        UIGammaSelector.greenGamma.setValue(obj.get(KEY_GAMMA_GREEN).getAsDouble());
      }
      if (obj.has(KEY_GAMMA_BLUE)) {
        UIGammaSelector.blueGamma.setValue(obj.get(KEY_GAMMA_BLUE).getAsDouble());
      }
      if (obj.has(KEY_PIXLITE1_IP)) {
        UIPixliteConfig.pixlite1IpP.setValue(obj.get(KEY_PIXLITE1_IP).getAsString());
      }
      if (obj.has(KEY_PIXLITE1_PORT)) {
        UIPixliteConfig.pixlite1PortP.setValue(obj.get(KEY_PIXLITE1_PORT).getAsString());
      }
      if (obj.has(KEY_PIXLITE2_IP)) {
        UIPixliteConfig.pixlite2IpP.setValue(obj.get(KEY_PIXLITE2_IP).getAsString());
      }
      if (obj.has(KEY_PIXLITE2_PORT)) {
        UIPixliteConfig.pixlite2PortP.setValue(obj.get(KEY_PIXLITE2_PORT).getAsString());
      }
      if (obj.has(KEY_MIDICTRL_MIDICH))
        UIMidiControl.midiChP.setValue(obj.get(KEY_MIDICTRL_MIDICH).getAsInt());
      if (obj.has(KEY_MIDICTRL_NXTGAME))
        UIMidiControl.nextGameP.setValue(obj.get(KEY_MIDICTRL_NXTGAME).getAsInt());
      if (obj.has(KEY_MIDICTRL_PRVGAME))
        UIMidiControl.prevGameP.setValue(obj.get(KEY_MIDICTRL_PRVGAME).getAsInt());
      if (obj.has(KEY_MIDICTRL_AUDIOM))
        UIMidiControl.audioModeP.setValue(obj.get(KEY_MIDICTRL_AUDIOM).getAsInt());
      if (obj.has(KEY_MIDICTRL_STDM))
        UIMidiControl.standardModeP.setValue(obj.get(KEY_MIDICTRL_STDM).getAsInt());
      if (obj.has(KEY_MIDICTRL_INSTRM))
        UIMidiControl.instrumentModeP.setValue(obj.get(KEY_MIDICTRL_INSTRM).getAsInt());
      if (obj.has(KEY_MIDICTRL_INTERM))
        UIMidiControl.interactiveModeP.setValue(obj.get(KEY_MIDICTRL_INTERM).getAsInt());
      if (obj.has(KEY_MIDICTRL_AUTOAU))
        UIMidiControl.autoAudioP.setValue(obj.get(KEY_MIDICTRL_AUTOAU).getAsInt());
    }
  }

  public void initialize(final LXStudio lx, LXStudio.UI ui) {
    // Add custom components or output drivers here
    // Register settings
    lx.engine.registerComponent("rainbowSettings", new Settings(lx, ui));

    // Register any patterns and effects LX doesn't recognize
    registerAll(lx);
  }

  public void onUIReady(LXStudio lx, LXStudio.UI ui) {

  }

  public void draw() {
    // All is handled by LX Studio
  }

  // Configuration flags
  private final static boolean MULTITHREADED = false;  // Disabled for anything GL
                                                       // Enable at your own risk!
                                                       // Could cause VM crashes.
  private final static boolean RESIZABLE = true;

  // Helpful global constants
  final static float INCHES = 1.0f / 12.0f;
  final static float IN = INCHES;
  final static float FEET = 1.0f;
  final static float FT = FEET;
  final static float CM = IN / 2.54f;
  final static float MM = CM * .1f;
  final static float M = CM * 100;
  final static float METER = M;

  public static final int LEDS_PER_UNIVERSE = 170;

  private LXModel buildModel(int modelType) {
    // A three-dimensional grid model
    // return new GridModel3D();
    if (modelType == FULL_RAINBOW) {
      return new RainbowModel3D();
    } else if (modelType == SRIKANTH_PANEL) {
      return new SimplePanel();
    } else if (modelType == RAINBOW_PANEL) {
      return new RainbowModel3D(1);
    } else if (modelType == LARGE_PANEL) {
      return new SimplePanel(100, 50);
    } else if (modelType == RAINBOW_PANEL_4) {
      return new RainbowModel3D(4);
    } else if (modelType == RAINBOW_PANEL_2) {
      return new RainbowModel3D(2);
    } else if (modelType == RAINBOW_PANEL_1) {
      return new RainbowModel3D(1);
    } else if (modelType == RAINBOW_START_PANEL) {
      return new RainbowModel3D(1);
    } else if (modelType == RAINBOW_END_PANEL) {
      return new RainbowModel3D(1);
    } else {
      return null;
    }
  }
}

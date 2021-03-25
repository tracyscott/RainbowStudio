package com.giantrainbow.ui;

import com.giantrainbow.ParameterFile;
import com.giantrainbow.PropertyFile;
import com.giantrainbow.RainbowStudio;
import com.giantrainbow.UtilsForLX;
import com.giantrainbow.patterns.AnimatedTextPP;
import com.giantrainbow.patterns.HereComesSun;
import com.giantrainbow.patterns.OxNewYear;
import com.giantrainbow.patterns.TextFx;
import heronarts.lx.*;
import heronarts.lx.parameter.*;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIKnob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UIModeSelector extends UICollapsibleSection {
  private static final Logger logger = Logger.getLogger(UIModeSelector.class.getName());

  public final UIButton autoMode;
  public final UIButton audioMode;
  public final UIButton standardMode;
  public final UIButton interactiveMode;
  public final UIButton instrumentMode;
  public final UIButton textMode;

  protected LX lx;
  protected LXStudio.UI ui;
  public BooleanParameter autoAudioModeP; // = new BooleanParameter("autoaudio", false);
  public BooleanParameter audioModeP = new BooleanParameter("audio", false);
  public BooleanParameter standardModeP = new BooleanParameter("standard", false);
  public BooleanParameter interactiveModeP = new BooleanParameter("interactive", false);
  public BooleanParameter instrumentModeP = new BooleanParameter("instrument", false);
  public BooleanParameter textModeP = new BooleanParameter("text", false);

  static public List<CompoundParameter> timesPerChannel = new ArrayList<CompoundParameter>();
  static public CompoundParameter timePerAudioChannelP1;
  static public CompoundParameter timePerAudioChannelP2;

  static public CompoundParameter fadeTimeP;
  public final UIKnob fadeTime;
  public final UIKnob timePerAudioChannel1;
  public final UIKnob timePerAudioChannel2;

  public static String[] standardModeChannelNames;
  public List<LXChannelBus> standardModeChannels;
  public String[] audioModeChannelNames = { "AUDIO-1", "AUDIO-MULTI" };
  public List<LXChannelBus> audioModeChannels = new ArrayList<LXChannelBus>(audioModeChannelNames.length);

  public int currentPlayingChannel = 3;  // Defaults to multi
  public int previousPlayingChannel = 0;
  public int currentPlayingAudioChannel = 1;
  public int previousPlayingAudioChannel = 0;
  public UIAudioMonitorLevels audioMonitorLevels;

  // Items related to loading and saving parameter values.  Typical UIConfig classes inherit all this
  // infrastructure but this class predates that model so some of the functionality has just been
  // copied over.
  public ParameterFile paramFile;
  public List<LXParameter> parameters = new ArrayList<LXParameter>();
  public Map<String, LXParameter> paramLookup = new HashMap<String, LXParameter>();
  static public final String filename = "modeselector.json";
  static public StandardModeCycle standardModeCycle;

  public UIModeSelector(final LXStudio.UI ui, LX lx, UIAudioMonitorLevels audioMonitor) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("MODE");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    this.lx = lx;
    this.ui = ui;
    standardModeChannelNames = RainbowStudio.stdChConfig.getStandardChannels();
    standardModeChannels = new ArrayList<LXChannelBus>(standardModeChannelNames.length);

    load();
    autoAudioModeP = registerBooleanParameter("autoaudio", false);
    // For each of the standard channels, add the time per channel parameter.
    for (int i = 0; i < standardModeChannelNames.length; i++) {
      timesPerChannel.add(registerCompoundParameter("T" + standardModeChannelNames[i],
          60 * 5, 2, 60 * 60));
    }
    fadeTimeP = registerCompoundParameter("FadeT", 1.0, 0.0, 10.0);
    timePerAudioChannelP1 = registerCompoundParameter("Aud-1", 60, 2, 360);
    timePerAudioChannelP2 = registerCompoundParameter("Aud-Multi", 60, 2, 360);
    save();

    audioMonitorLevels = audioMonitor;
    // When enabled, audio monitoring can trigger automatic channel switching.
    autoMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18)
    .setParameter(autoAudioModeP)
    .setLabel("Auto Audio Detect")
    .setActive(false)
    .addToContainer(this);

    audioMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          standardMode.setActive(false);
          interactiveMode.setActive(false);
          instrumentMode.setActive(false);
          textMode.setActive(false);
          // Build our list of Audio Channels based on our names.  Putting it here allows it to
          // work after loading a new file (versus startup initialization).
          audioModeChannels.clear();
          for (String channelName: audioModeChannelNames) {
            LXChannelBus ch = UtilsForLX.getChannelByLabel(lx, channelName);
            audioModeChannels.add(ch);
          }
          setAudioChannelEnabled(true);
        } else {
          // Disable AUDIO channel
          setAudioChannelEnabled(false);
          audioModeP.setValue(false);
        }
      }
    }
    .setParameter(audioModeP)
    .setLabel("Audio")
    .setActive(false)
    .addToContainer(this);

    standardMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          logger.info("Turning on standard mode.");
          audioMode.setActive(false);
          interactiveMode.setActive(false);
          instrumentMode.setActive(false);
          textMode.setActive(false);
          // Build our list of Standard Channels based on our names.  Putting it here allows it to
          // work after loading a new file (versus startup initialization).
          standardModeChannels.clear();
          for (String channelName: standardModeChannelNames) {
            LXChannelBus ch = UtilsForLX.getChannelByLabel(lx, channelName);
            standardModeChannels.add(ch);
          }
          setStandardChannelsEnabled(true);
        } else {
          logger.info("Disabling standard mode.");
          // Disable Standard Channels
          setStandardChannelsEnabled(false);
        }
      }
    }
    .setParameter(standardModeP)
    .setLabel("Standard")
    .setActive(false)
    .addToContainer(this);

    interactiveMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          audioMode.setActive(false);
          standardMode.setActive(false);
          instrumentMode.setActive(false);
          textMode.setActive(false);
          // Enable Interactive channel
          setInteractiveChannelEnabled(true);
        } else {
          // Disable Interactive channel
          setInteractiveChannelEnabled(false);
        }
      }
    }
    .setParameter(interactiveModeP)
    .setLabel("Interactive")
    .setActive(false)
    .addToContainer(this);

    instrumentMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          audioMode.setActive(false);
          standardMode.setActive(false);
          interactiveMode.setActive(false);
          textMode.setActive(false);
          // Enable Instrument/AUDIO channels
          setInstrumentChannelsEnabled(true);
        } else {
          // Disable Instrument/AUDIO channels
          setInstrumentChannelsEnabled(false);
        }
      }
    }
    .setParameter(instrumentModeP)
    .setLabel("Instrument")
    .setActive(false)
    .addToContainer(this);

    textMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          audioMode.setActive(false);
          standardMode.setActive(false);
          interactiveMode.setActive(false);
          instrumentMode.setActive(false);
          setTextChannelEnabled(true);
        } else {
          setTextChannelEnabled(false);
        }
      }
    }
    .setParameter(textModeP)
        .setLabel("Text")
        .setActive(false)
        .addToContainer(this);

    UI2dContainer knobsContainer;

    knobsContainer = new UI2dContainer(0, 30, getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    fadeTime = new UIKnob(fadeTimeP);
    fadeTime.addToContainer(knobsContainer);
    knobsContainer.addToContainer(this);

    UICollapsibleSection section;
    section = new UICollapsibleSection(this.ui, 0, 0, getContentWidth(), 30);
    section.setTitle("Standard Channel Timing");
    section.setLayout(UI2dContainer.Layout.VERTICAL);
    section.setChildMargin(2);
    section.setBackgroundColor(0xFF222222);
    section.addToContainer(this);

    //knobsContainer = new UI2dContainer(0, 30, getContentWidth(), 45);
    //knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    //knobsContainer.setPadding(0, 0, 0, 0);
    for (int i = 0; i < timesPerChannel.size(); i++) {
      if (i % 4 == 0) {
        knobsContainer = new UI2dContainer(0, 30, getContentWidth(), 45);
        knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
        knobsContainer.setPadding(0, 0, 0, 0);
        knobsContainer.addToContainer(section);
      }
      new UIKnob(timesPerChannel.get(i)).addToContainer(knobsContainer);
    }
    /*
    timePerChannel = new UIKnob(timePerChannelP);
    timePerChannel.addToContainer(knobsContainer);
    timePerChannel2 = new UIKnob(timePerChannelP2);
    timePerChannel2.addToContainer(knobsContainer);
    timePerChannel3 = new UIKnob(timePerChannelP3);
    timePerChannel3.addToContainer(knobsContainer);
    timePerChannel4 = new UIKnob(timePerChannelP4);
    timePerChannel4.addToContainer(knobsContainer);
    */
    //knobsContainer.addToContainer(section);

    section = new UICollapsibleSection(this.ui, 0, 0, getContentWidth(), 30);
    section.setTitle("Audio Channel Timing");
    section.setLayout(UI2dContainer.Layout.VERTICAL);
    section.setChildMargin(2);
    section.setBackgroundColor(0xFF222222);
    section.addToContainer(this);

    knobsContainer = new UI2dContainer(0, 30, getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    timePerAudioChannel1 = new UIKnob(timePerAudioChannelP1);
    timePerAudioChannel1.addToContainer(knobsContainer);
    timePerAudioChannel2 = new UIKnob(timePerAudioChannelP2);
    timePerAudioChannel2.addToContainer(knobsContainer);
    knobsContainer.addToContainer(section);

    // Button saving config.
    new UIButton(10, 7, 40, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          save();
        }
      }
    }.setLabel("save").setMomentary(true).addToContainer(this);

    if (lx.engine.audio.input != null) {
      if (lx.engine.audio.input.device.getObject().isAvailable()) {
        this.standardMode.setActive(true);
        lx.engine.audio.enabled.setValue(true);
        lx.engine.addLoopTask(new AudioMonitor());
      } else {
        logger.warning("Audio Input device is not available!");
      }
    } else {
      logger.warning("Audio Input is null.");
    }

    standardModeCycle = new StandardModeCycle();
    lx.engine.addLoopTask(new StandardModeCycle());
    lx.engine.addLoopTask(new AudioModeCycle());
  }

  public void load() {
    paramFile = new ParameterFile(filename);
    try {
      paramFile.load();
    } catch (PropertyFile.NotFound nfex) {
      System.out.println(filename + ", property not found.");
    } catch (IOException ioex) {
      System.err.println(filename + " not found, will be created.");
    }
  }

  public void save() {
    try {
      paramFile.save();
    } catch (IOException ioex) {
      System.err.println("Error saving " + filename + " " + ioex.getMessage());
    }
  }

  public StringParameter registerStringParameter(String label, String value) {
    StringParameter sp = paramFile.getStringParameter(label, value);
    parameters.add(sp);
    paramLookup.put(label, sp);
    return sp;
  }

  public CompoundParameter registerCompoundParameter(String label, double value, double base, double range) {
    CompoundParameter cp = paramFile.getCompoundParameter(label, value, base, range);
    parameters.add(cp);
    paramLookup.put(label, cp);
    return cp;
  }

  public DiscreteParameter registerDiscreteParameter(String label, int value, int min, int max) {
    DiscreteParameter dp = paramFile.getDiscreteParameter(label, value, min, max);
    parameters.add(dp);
    paramLookup.put(label, dp);
    return dp;
  }

  public BooleanParameter registerBooleanParameter(String label, boolean value) {
    BooleanParameter bp = paramFile.getBooleanParameter(label, value);
    parameters.add(bp);
    paramLookup.put(label, bp);
    return bp;
  }

  /**
   * Initialize mode to Standard Mode with the RBW channel playing.
   * NOTE: In order to defeat the smart buttons, we need to toggle them on
   * and then off in order to trigger their onToggle callback.  Otherwise,
   * just calling setActive(false) does nothing because the button is already
   * in an inactive state (the default at startup).  This
   * is necessary in case somebody saves a show file that is not
   * in Standard mode.  Here we are effectively forcing the show
   * back into Standard mode.
   */
  public void initMode() {
    standardMode.setActive(false);
    interactiveMode.setActive(true);
    interactiveMode.setActive(false);
    audioMode.setActive(true);
    audioMode.setActive(false);
    instrumentMode.setActive(true);
    instrumentMode.setActive(false);
    textMode.setActive(true);
    textMode.setActive(false);
    currentPlayingChannel = 3;
    standardMode.setActive(true);
  }


  public void switchToMode(String mode) {
    if ("Audio".equalsIgnoreCase(mode)){
      // TODO
      audioMode.setActive(true);
      /*
      interactiveMode.setActive(false);
      instrumentMode.setActive(false);
      setStandardChannelsEnabled(false);
      LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "TEXT");
      if (channel != null)
        channel.enabled.setValue(false);
      channel = UtilsForLX.getChannelByLabel(lx, "RBW");
      if (channel != null)
        channel.enabled.setValue(false);
      */
    } else if ("Standard".equalsIgnoreCase(mode)) {
      /*
      audioMode.setActive(false);
      interactiveMode.setActive(false);
      instrumentMode.setActive(false);
      textMode.setActive(false);
      setStandardChannelsEnabled(true);
      */
      standardMode.setActive(true);
    } else if ("Interactive".equalsIgnoreCase(mode)) {
      // TODO
      interactiveMode.setActive(true);
    } else if ("Instrument".equalsIgnoreCase(mode)) {
      // TODO
      instrumentMode.setActive(true);
    } else if ("Text".equalsIgnoreCase(mode)) {
      textMode.setActive(true);
      /*
      audioMode.setActive(false);
      interactiveMode.setActive(false);
      instrumentMode.setActive(false);
      setStandardChannelsEnabled(false);
      LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "TEXT");
      if (channel != null)
        channel.enabled.setValue(true);
      channel = UtilsForLX.getChannelByLabel(lx, "RBW");
      if (channel != null)
        channel.enabled.setValue(false);
      */
    } else if ("Rainbow".equalsIgnoreCase(mode)) {
      // NOTE(tracy): There currently isn't a Rainbow mode button, so we
      // can't just enable it to disable everything else.  Should probably
      // just add a Rainbow mode.
      audioMode.setActive(false);
      interactiveMode.setActive(false);
      instrumentMode.setActive(false);
      standardMode.setActive(false);
      textMode.setActive(false);
      LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "RBW");
      if (channel != null)
        channel.enabled.setValue(true);
    } else if ("None".equalsIgnoreCase(mode)) {
      // Disable all modes, will stay on currently selected channel.
      // TODO(tracy): Fix this since disabling Standard Mode will turn off
      // all standard mode channels which is probably not what we want.
      // This is currently meant to work with custom channels that won't be
      // touched by the other stuff.
      audioMode.setActive(false);
      interactiveMode.setActive(false);
      instrumentMode.setActive(false);
      // We don't want to inactivate current channel, we just want to
      // inactivate the auto-scheduling.
      standardMode.setActive(false);
      LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "RBW");
      if (channel != null)
        channel.enabled.setValue(false);
      channel = UtilsForLX.getChannelByLabel(lx, "TEXT");
      if (channel != null)
        channel.enabled.setValue(false);
    }
  }

  public void setAudioChannelEnabled(boolean on) {
    if (on) {
      // Disable all channels
      for (LXChannelBus channel : audioModeChannels) {
        if (channel != null) {
          channel.fader.setValue(0);
          channel.enabled.setValue(false);
        }
      }
      // Enable the currentPlayingAudioChannel
      LXChannelBus currentChannel = audioModeChannels.get(currentPlayingAudioChannel);
      if (currentChannel != null) {
        currentChannel.enabled.setValue(true);
        currentChannel.fader.setValue(100);
      }
    } else {
      for (LXChannelBus channel : audioModeChannels) {
        if (channel != null)
          channel.enabled.setValue(on);
      }
    }
  }

  public void setStandardChannelsEnabled(boolean on) {
    if (on) {
      // Disable all channels
      for (LXChannelBus channel : standardModeChannels) {
        if (channel != null) {
          channel.fader.setValue(0);
          channel.enabled.setValue(false);
        }
      }
      // Enable the currentPlayingChannel
      LXChannelBus currentChannel = standardModeChannels.get(currentPlayingChannel);
      if (currentChannel != null) {
        currentChannel.enabled.setValue(true);
        currentChannel.fader.setValue(100);
      }
    } else {
      for (LXChannelBus channel : standardModeChannels) {
        if (channel != null)
          channel.enabled.setValue(on);
      }
    }
  }

  public void setInteractiveChannelEnabled(boolean on) {
    LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "INTERACTIVE");
    if (channel != null) channel.enabled.setValue(on);
  }

  public void setInstrumentChannelsEnabled(boolean on) {
    LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "AUDIO");
    if (channel != null) channel.enabled.setValue(on);
    channel = UtilsForLX.getChannelByLabel(lx, "MIDI");
    if (channel != null) channel.enabled.setValue(on);
  }

  public void setTextChannelEnabled(boolean on) {
    LXChannelBus textChannel = UtilsForLX.getChannelByLabel(lx, "TEXT");
    if (textChannel != null) textChannel.enabled.setValue(on);
  }



  public class StandardModeCycle implements LXLoopTask {
    public double currentChannelPlayTime = 0.0;
    public double timePerChannel = 5000.0;  // Default. This is settable in the UI. milliseconds.
    public double fadeTime = 1000.0;  // This is settable in the UI. milliseconds
    public double prevChannelDisableThreshold = 0.1;  // Settable in UI? How low slider goes before full disable.
    public double channelFadeFullThreshold = 0.1; // At this value just set it to 1.  Deals with chunkiness around time.
    public double fadeTimeRemaining = 0.0;
    public boolean noEligibleChannels = true;
    public boolean isInitialized = false;

    /**
     * With the program scheduling feature, it is possible that somebody configured things such that there
     * are no eligible channels to play.  In that case return false and set the noEligibleChannels flag so
     * that our loop doesn't attempt to do anything.
     * @return
     */
    public boolean initializeChannel() {
      isInitialized = true;
      int okChannel = -1;
      LXChannelBus okChannelBus = null;
      // Go through all channels and disable them.  Enable the first channel that is eligible to play in this
      // timeslot.
      logger.info("Initializing STD mode");
      for (int i = 0; i < standardModeChannels.size(); i++) {
        LXChannelBus currentChannel = standardModeChannels.get(i);
        if (currentChannel != null) {
          logger.info("Checking channel: " + currentChannel.getLabel());
          currentChannel.enabled.setValue(false);
          if (channelIsEligibleByTime(currentChannel) && okChannel == -1) {
            currentChannel.enabled.setValue(true);
            noEligibleChannels = false;
            okChannel = i;
            okChannelBus = currentChannel;
          }
          // TODO(tracy): This is an emergency hack.  If the file was saved while
          // a message text pattern was playing, then the file will be saved with
          // the channel autoCycleEnabled set to false.  Typically, a text pattern
          // will re-enable autoCycleEnabled after it finishes one message and goes
          // to advance the pattern.  This ensures that even if somebody happens to
          // save the show file under that scenario, when we reload the show file
          // later, autoCycleEnabled will be re-initialized to true.
          if (currentChannel instanceof LXGroup) {
            LXGroup g = (LXGroup) currentChannel;
            if (g.channels.size() > 0) {
              for (LXChannel c : g.channels) {
                if ("MSGTXT".equals(c.getLabel())) {
                  c.autoCycleEnabled.setValue(true);
                }
              }
            }
          }
        }
      }
      currentPlayingChannel = okChannel;
      currentChannelPlayTime = 0.0;
      if (okChannelBus != null) {
        okChannelBus.fader.setValue(1.0);
      }
      fadeTimeRemaining = 0.0;
      if (noEligibleChannels)
        logger.info("WARNING! No eligible channel time-slots at startup, disabling standard mode!");
      return !noEligibleChannels;
    }

    // TODO(tracy): Audio-Standard mode switching with faders.
    // When auto audio detection is fading in, we want to fade any active standard channels to 0
    // and fully reset the Standard-mode channel switching state once we are faded to 0.
    // (i.e. reset currentChannelPlayTime and fadeTimeRemaining).
    // When auto audio detection is fading out, we want to fade in some standard channel, but it should
    // be whatever the last currentChannel that was active in standard mode.  We need to reset
    // the Standard-mode channel switching state so that we are only dealing with a single fader.
    // i.e. reset currentChannelPlayTime and fadeTimeRemaining so we don't end up in a three-way
    // fader situation.  Also make sure that autoAudioFadeTime is much less than currentChannelPlayTime
    // or else we could end up in a three-way fader situation.

    public void loop(double deltaMs) {
      if (!UIModeSelector.this.standardModeP.getValueb())
        return;

      if (!isInitialized) {
        initializeChannel();
      }

      if (noEligibleChannels)
        return;

      fadeTime = UIModeSelector.fadeTimeP.getValue() * 1000f;
      // UI is in seconds.  Convert to milliseconds.
      timePerChannel = UIModeSelector.timesPerChannel.get(currentPlayingChannel).getValuef() * 1000f;

      // Disable Standard-mode channel switching for AnimatedTextPP/TextFX patterns to prevent
      // fading in the middle of text.  We achieve this by effectively stalling the
      // currentChannelPlayTime until the current channel is no longer an
      // AnimatedTextPP/TextFX pattern.
      LXChannelBus channelBus = standardModeChannels.get(currentPlayingChannel);
      if (channelBus instanceof LXChannel) {
        LXChannel c = (LXChannel) channelBus;
        if (c.patterns.size() > 0) {
          LXPattern p = c.getActivePattern();
          if (p instanceof AnimatedTextPP || p instanceof TextFx || p instanceof OxNewYear
          || p instanceof HereComesSun) {
            // Let's always set it at 5 seconds until transition so that if we have something like
            // a Flags pattern have a series of text patterns then the scheduler can change channels
            // while the Flags pattern is playing.
            currentChannelPlayTime = timePerChannel - 5000f;
          }
        }
      } else if (channelBus instanceof LXGroup) {
        LXGroup g = (LXGroup) channelBus;
        if (g.channels.size() > 0) {
          for (LXChannel c : g.channels) {
            if (c.patterns.size() > 0) {
              LXPattern p = c.getActivePattern();
              if (p instanceof AnimatedTextPP || p instanceof TextFx || p instanceof OxNewYear
              || p instanceof HereComesSun) {
                currentChannelPlayTime = timePerChannel - 5000f;
              }
            }
          }
        }
      }

      // If our current configuration doesn't have multiple standard channel names, just no-op.
      if (standardModeChannels.size() < 2) {
        logger.warning("Too few standard channels.");
        return;
      }

      // We are still fading,
      if (fadeTimeRemaining > 0.0) {
        double previousChannelPercent = fadeTimeRemaining / fadeTime;
        double percentDone = 1.0 - previousChannelPercent;
        if (percentDone + channelFadeFullThreshold >= 1.0) {
          percentDone = 1.0;
        }
        LXChannelBus previousChannel = standardModeChannels.get(previousPlayingChannel);
        LXChannelBus currentChannel = standardModeChannels.get(currentPlayingChannel);
        if (previousChannel != null) {
          previousChannel.fader.setValue(previousChannelPercent);
          if (previousChannelPercent < prevChannelDisableThreshold)
            previousChannel.enabled.setValue(false);
        }
        if (currentChannel != null) currentChannel.fader.setValue(percentDone);
        // When this goes below zero we are done fading until the timePerChannel time
        // is reached and fadeTimeRemaining is reset to fadeTime.
        fadeTimeRemaining -= deltaMs;
      }

      currentChannelPlayTime += deltaMs;
      // Exceeded our per-channel time, select new channel and begin to fade.
      // We will keep looking for a new channel until we find something that is
      // eligible for the current time window.  If we can't find an additional
      // channel, we will just stick to the current channel.  If even the
      // current channel has become ineligible due to time constraints (past it's
      // window) then we will still stick with the current channel.
      if (currentChannelPlayTime + deltaMs > timePerChannel) {
        LXChannelBus currentChannel;
        previousPlayingChannel = currentPlayingChannel;
        int loopCount = 0;
        do {
          // Next channel index, loop if that the end.
          ++currentPlayingChannel;
          if (currentPlayingChannel >= standardModeChannels.size()) {
            currentPlayingChannel = 0;
          }
          // It is possible that we have only one eligible channel and that it is no longer eligible.  In which case,
          // we should just stick with the current channel and keep trying until maybe we get some other eligible
          // channels.  This is an abnormal case, but we want to do something reasonable in the face of user error.
          // This scenario will be indicated by multiple passes through this loop.
          if (currentPlayingChannel == previousPlayingChannel) {
            loopCount++;
          }
          // if (currentChannel != null)
          //   logger.info("previous: " + currentChannel.getLabel());
          currentChannel = standardModeChannels.get(currentPlayingChannel);
        } while (!channelIsEligibleByTime(currentChannel) && loopCount < 2);

        // If there is only one eligible channel then the currentChannel will
        // equal to the previous channel, so in that case, just reset the
        // currentChannelPlayTime to 0.0
        if (previousPlayingChannel != currentPlayingChannel) {
          // If a standard channel was not in the project file, currentChannel can be null
          // here.
          if (currentChannel != null) {
            //logger.info("current: " + currentChannel.getLabel());
            currentChannel.enabled.setValue(true);
            currentChannelPlayTime = 0.0; // Reset play time counter.
            fadeTimeRemaining = fadeTime;
          }
        } else {
          currentChannelPlayTime = 0.0;
        }
        // logger.info("Switching channels:" + currentPlayingChannel);
      }
    }
  }

  /**
   * Determine if this channel is eligible to be played at this time.  This allows us
   * to assign channels to blocks of time which opens up more program scheduling options.
   * @param channel
   * @return
   */
  public boolean channelIsEligibleByTime(LXChannelBus channel) {
    if (channel == null) return false;
    int startMinutes = RainbowStudio.programConfig.getChannelStart(channel.getLabel());
    int endMinutes = RainbowStudio.programConfig.getChannelEnd(channel.getLabel());
    int now = LXTime.hour() * 60 + LXTime.minute();
    if (startMinutes < endMinutes) {
      // Normal daytime interval
      return (now >= startMinutes) && (now < endMinutes);
    } else {
      // Wrapping around midnight
      return (now >= startMinutes) || (now < endMinutes);
    }
  }

  public class AudioModeCycle implements LXLoopTask {
    public double currentChannelPlayTime = 0.0;
    public double timePerChannel = 5000.0;
    public double fadeTime = 1000.0;
    public double prevChannelDisableThreshold = 0.1;  // Settable in UI? How low slider goes before full disable.
    public double channelFadeFullThreshold = 0.1; // At this value just set it to 1.  Deals with chunkiness around time.
    public double fadeTimeRemaining = 0.0;

    public void loop(double deltaMs) {
      if (!UIModeSelector.this.audioModeP.isOn())
        return;

      fadeTime = UIModeSelector.fadeTimeP.getValue();

      // UI is in seconds.  Convert to milliseconds.
      if (currentPlayingAudioChannel == 0) {
        timePerChannel = UIModeSelector.timePerAudioChannelP1.getValue() * 1000f;
      } else if (currentPlayingChannel == 1) {
        timePerChannel = UIModeSelector.timePerAudioChannelP2.getValue() * 1000f;
      }

      // If our current configuration doesn't have multiple standard channel names, just no-op.
      if (audioModeChannels.size() < 2) {
        logger.warning("Too few audio channels.");
        return;
      }

      // We are still fading,
      if (fadeTimeRemaining > 0.0) {
        double previousChannelPercent = fadeTimeRemaining / fadeTime;
        double percentDone = 1.0 - previousChannelPercent;
        if (percentDone + channelFadeFullThreshold >= 1.0) {
          percentDone = 1.0;
        }
        LXChannelBus previousChannel = audioModeChannels.get(previousPlayingAudioChannel);
        LXChannelBus currentChannel = audioModeChannels.get(currentPlayingAudioChannel);
        if (previousChannel != null) {
          previousChannel.fader.setValue(previousChannelPercent);
          if (previousChannelPercent < prevChannelDisableThreshold)
            previousChannel.enabled.setValue(false);
        }
        if (currentChannel != null) currentChannel.fader.setValue(percentDone);
        // When this goes below zero we are done fading until the timePerChannel time
        // is reached and fadeTimeRemaining is reset to fadeTime.
        fadeTimeRemaining -= deltaMs;
      }

      currentChannelPlayTime += deltaMs;

      // Exceeded our per-channel time, begin to fade.
      if (currentChannelPlayTime + deltaMs > timePerChannel) {
        LXChannelBus currentChannel = audioModeChannels.get(currentPlayingAudioChannel);
        previousPlayingAudioChannel = currentPlayingAudioChannel;
        currentPlayingAudioChannel = (currentPlayingAudioChannel + 1) % audioModeChannels.size();
        currentChannel = audioModeChannels.get(currentPlayingAudioChannel);
        if (currentChannel != null) {
          currentChannel.enabled.setValue(true);
          currentChannelPlayTime = 0.0; // Reset play time counter.
        }
        fadeTimeRemaining = fadeTime;
      }
    }
  }

  public class AudioMonitor implements LXLoopTask {
    public double deltaLastModeSwap = 0.0;
    public boolean audioMode = false;
    public double avgDb = 0.0;
    public long sampleCount = 0;
    public double avgTimeRemaining;

    public void loop(double deltaMs) {
      // TODO(tracy): We need a number of samples over time and then decide if we should switch
      // based on that.
      if (!autoAudioModeP.isOn()) return;

      double gainDelta = audioMonitorLevels.getCompoundParameter(UIAudioMonitorLevels.GAIN_INCREMENT).getValue();
      double reduceThreshold = audioMonitorLevels.getCompoundParameter(UIAudioMonitorLevels.REDUCE_THRESHOLD).getValue();//45.0;
      double gainThreshold = audioMonitorLevels.getCompoundParameter(UIAudioMonitorLevels.GAIN_THRESHOLD).getValue(); // 10.0;
      double minThreshold = audioMonitorLevels.getCompoundParameter(UIAudioMonitorLevels.MIN_THRESHOLD).getValue();
      double currentDb = lx.engine.audio.meter.getDecibels();

      avgTimeRemaining -= deltaMs;
      sampleCount++;
      avgDb = avgDb + currentDb/sampleCount;
      if (avgTimeRemaining > 0.0)
        return;
      // We are done averaging samples, reset our variables.
      sampleCount = 0;
      avgTimeRemaining = audioMonitorLevels.getCompoundParameter(UIAudioMonitorLevels.AVG_TIME_SECS).getValue() * 1000.0;

      // We have built our dB level average over some seconds, check to see if we should
      // perform AutoGain Control
      if (avgDb > reduceThreshold) {
        lx.engine.audio.meter.gain.setValue(lx.engine.audio.meter.gain.getValue() - gainDelta);
      } else if (avgDb < gainThreshold) {
        lx.engine.audio.meter.gain.setValue(lx.engine.audio.meter.gain.getValue() + gainDelta);
      }

      if (avgDb > minThreshold
          //&& deltaLastModeSwap > UIAudioMonitorLevels.quietTimeP.getValue() * 1000.0
          && !audioMode) {
        logger.info("Enabling audio mode, avgDb: " + avgDb);
        // TODO: Start fading audio channel up, standard channels down.  Once standard-mode
        // channels are faded down, reset the standard-mode channel switching state.
        UIModeSelector.this.standardMode.setActive(false);
        UIModeSelector.this.interactiveMode.setActive(false);
        UIModeSelector.this.instrumentMode.setActive(false);
        UIModeSelector.this.audioMode.setActive(true);
        audioMode = true;
        deltaLastModeSwap = 0.0;
      } else if (avgDb < minThreshold
          //&& deltaLastModeSwap > UIAudioMonitorLevels.quietTimeP.getValue() * 1000.0
          && audioMode) {
        logger.info("Disabling audio mode, avgDb: " + avgDb);
        // TODO: Reset standard-mode state.  Start fading current Standard-mode
        // channel up, audio channel down.
        UIModeSelector.this.interactiveMode.setActive(false);
        UIModeSelector.this.instrumentMode.setActive(false);
        UIModeSelector.this.setAudioChannelEnabled(false);
        UIModeSelector.this.standardMode.setActive(true);
        audioMode = false;
        deltaLastModeSwap = 0.0;
      } else {
        deltaLastModeSwap += deltaMs;
      }
      avgDb = 0.0;  // Reset rolling average.
    }
  }
}

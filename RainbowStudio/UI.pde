static public class UIPixliteConfig extends UICollapsibleSection {
  static public StringParameter pixlite1IpP = new StringParameter("Pixlite 1 IP", "192.168.2.134");
  static public StringParameter pixlite1PortP = new StringParameter("Pixlite 1 Port", "6454");
  static public StringParameter pixlite2IpP = new StringParameter("Pixlite 2 IP", "192.168.2.134");
  static public StringParameter pixlite2PortP = new StringParameter("Pixlite 2 Port", "6455");
  
  static public UITextBox pixlite1Ip;
  static public UITextBox pixlite1Port;
  static public UITextBox pixlite2Ip;
  static public UITextBox pixlite2Port;
  
  public UIPixliteConfig(final LXStudio.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    setTitle("Pixlite Config");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    pixlite1Ip = new UITextBox(0,0, contentWidth - 10, 20 );
    pixlite1Ip.setParameter(pixlite1IpP);
    pixlite1Ip.addToContainer(this);
    pixlite1Port = new UITextBox(0, 0, contentWidth - 10, 20);
    pixlite1Port.setParameter(pixlite1PortP);
    pixlite1Port.addToContainer(this);
    pixlite2Ip = new UITextBox(0, 0, contentWidth - 10, 20);
    pixlite2Ip.setParameter(pixlite2IpP);
    pixlite2Ip.addToContainer(this);
    pixlite2Port = new UITextBox(0, 0, contentWidth - 10, 20);
    pixlite2Port.setParameter(pixlite2PortP);
    pixlite2Port.addToContainer(this);
  }
}

static public class UIAudioMonitorLevels extends UICollapsibleSection {
  static public BoundedParameter minThresholdP = new BoundedParameter("MinThreshold", 10.0, 0.0, 40.0);
  static public BoundedParameter quietTimeP = new BoundedParameter("Quiet Time", 60.0, 0.0, 180.0);
  static public UIKnob minThreshold;
  static public UIKnob quietTime;
  
  public UIAudioMonitorLevels(final LXStudio.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("AUDIO MONITOR");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    UI2dContainer knobsContainer = new UI2dContainer(0, 30, ui.leftPane.global.getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(3, 3, 3, 3);
    minThreshold = new UIKnob(minThresholdP);
    minThreshold.addToContainer(knobsContainer);
    quietTime = new UIKnob(quietTimeP);
    quietTime.addToContainer(knobsContainer);
    knobsContainer.addToContainer(this);
  }
}

static public class UIGammaSelector extends UICollapsibleSection {
  static public BoundedParameter redGamma = new BoundedParameter("Red", 1.8, 1.0, 2.0);
  static public BoundedParameter greenGamma = new BoundedParameter("Green", 1.8, 1.0, 2.0);
  static public BoundedParameter blueGamma = new BoundedParameter("Blue", 1.8, 1.0, 2.0);
  public UISlider redSlider;
  public UISlider greenSlider;
  public UISlider blueSlider;
  
  public UIGammaSelector(final LXStudio.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("GAMMA");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    redSlider = new UISlider(0, 0, ui.leftPane.global.getContentWidth() - 10, 20) {
      @Override
      public void onParameterChanged(LXParameter parameter) {  
        super.onParameterChanged(parameter);
        // Rebuild Red Gamma
        Gamma.buildRedGammaLUT(parameter.getValuef());
      }
    };
    redSlider.addToContainer(this);
    redSlider.setParameter(redGamma);
    greenSlider = new UISlider(0, 0, ui.leftPane.global.getContentWidth() - 10, 20) {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        Gamma.buildGreenGammaLUT(parameter.getValuef());
      }
    };
    greenSlider.setParameter(greenGamma);
    greenSlider.addToContainer(this);
    
    blueSlider = new UISlider(0, 0, ui.leftPane.global.getContentWidth() - 10, 20) {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        Gamma.buildBlueGammaLUT(parameter.getValuef());
      }
    };
    blueSlider.setParameter(blueGamma);
    blueSlider.addToContainer(this);
  }
}

    
    
public class UIModeSelector extends UICollapsibleSection {
  
  public final UIButton autoMode;
  public final UIButton audioMode;
  public final UIButton standardMode;
  public final UIButton interactiveMode;
  public final UIButton instrumentMode;
  protected LX lx;
  public BooleanParameter autoAudioModeP = new BooleanParameter("autoaudio", false);
  public BooleanParameter audioModeP = new BooleanParameter("audio", false);
  public BooleanParameter standardModeP = new BooleanParameter("standard", false);
  public BooleanParameter interactiveModeP = new BooleanParameter("interactive", false);
  public BooleanParameter instrumentModeP = new BooleanParameter("instrument", false);
  
  public UIModeSelector(final LXStudio.UI ui, LX lx) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("MODE");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    this.lx = lx;
    
    // When enabled, audio monitoring can trigger automatic channel switching.
    this.autoMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18)
    .setParameter(autoAudioModeP)
    .setLabel("Auto Audio Detect")
    .setActive(false)
    .addToContainer(this);
    
    this.audioMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          standardModeP.setValue(false);
          interactiveModeP.setValue(false);
          instrumentModeP.setValue(false);
          // Enable AUDIO channel
          setAudioChannelEnabled(true);
          //audioModeP.setValue(true);
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
    
    this.standardMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          UIModeSelector.this.audioMode.setActive(false);
          UIModeSelector.this.interactiveMode.setActive(false);
          UIModeSelector.this.instrumentMode.setActive(false);
          // Enable Standard Channels
          setStandardChannelsEnabled(true);
        } else {
          System.out.println("Disabling standard mode.");
          // Disable Standard Channels
          setStandardChannelsEnabled(false);
        }
      }
    }
    .setParameter(standardModeP)
    .setLabel("Standard")
    .setActive(false)
    .addToContainer(this);
    
    this.interactiveMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          UIModeSelector.this.audioMode.setActive(false);
          UIModeSelector.this.standardMode.setActive(false);
          UIModeSelector.this.instrumentMode.setActive(false);
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
    
    this.instrumentMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          UIModeSelector.this.audioMode.setActive(false);
          UIModeSelector.this.standardMode.setActive(false);
          UIModeSelector.this.interactiveMode.setActive(false);
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
    
    this.standardMode.setActive(true);
    lx.engine.audio.enabled.setValue(true);
    this.addLoopTask(new AudioMonitor());
  }
  
  // TODO(tracy): We need to bring down everybody else's faders while bringing
  // up Audio faders.  We need to define Macros labeled
  // AudioMode
  // StandardMode
  // LiveInstrumentMode
  // InteractiveMode
  // lx.engine.modulation.getModulators() where modulator.getLabel() == "Mode Selector"
  // modulator is instance of MacroKnobs.macro1 macro2 ... macro5
  // Not renamable, so we will just need to hard code it.

  public void setAudioChannelEnabled(boolean on) {
    LXChannel audioChannel = getChannelByLabel(lx, "AUDIO");
    if (audioChannel != null) audioChannel.enabled.setValue(on);
  }
  
  // which channels to enable for standard mode?  We need to enable
  // either a combination of Form+Color+Texture or we need to enable
  // the Special channel.  Should we just bounce between those two
  // channel groups based on some timing?  If so, how long should
  // we spend on each group?  Also, how do we cleanly fade between
  // them.  With a macro?  Need an envelope output to macro input
  // or two envelope mappings with opposite polarity.  Need to
  // programmatically trigger an envelope.  We need a separate
  // envelope modulator to go the opposite direction.  Also, do
  // we need to disable the Channel when the fader is at 0?  If we
  // use envelopes, how do we know that fader is at 0?  LXChannelBus.fader
  // Timing is not super critical, just extra cpu cycles
  // Maybe hook into LXChannel.Listener interface to detect when we
  // have played the last pattern in a channel.  If so, trigger
  // a hop to another channel group.  What does patternWillChange
  // do?
  public void setStandardChannelsEnabled(boolean on) {
    LXChannel channel = getChannelByLabel(lx, "FORM");
    if (channel != null) channel.enabled.setValue(on);
    channel = getChannelByLabel(lx, "COLOR");
    if (channel != null) channel.enabled.setValue(on);
    channel = getChannelByLabel(lx, "TEXTURE");
    if (channel != null) channel.enabled.setValue(on);
    channel = getChannelByLabel(lx, "GIF");
    if (channel != null) channel.enabled.setValue(on);
    channel = getChannelByLabel(lx, "SPECIAL");
    if (channel != null) channel.enabled.setValue(on);
  }
  
  public void setInteractiveChannelEnabled(boolean on) {
    LXChannel channel = getChannelByLabel(lx, "INTERACTIVE");
    if (channel != null) channel.enabled.setValue(on);
  }
  
  public void setInstrumentChannelsEnabled(boolean on) {
    LXChannel channel = getChannelByLabel(lx, "AUDIO");
    if (channel != null) channel.enabled.setValue(on);
    channel = getChannelByLabel(lx, "MIDI");
    if (channel != null) channel.enabled.setValue(on);
  }
    
  public LXChannel getChannelByLabel(LX lx, String label) {
    for (LXChannelBus channelBus : lx.engine.channels) {
      LXChannel channel = (LXChannel) channelBus;
      if (label.equalsIgnoreCase(channel.getLabel()))
        return channel;
    }
    return null;
  }
  
  public class AudioMonitor implements LXLoopTask {
    public double deltaLastModeSwap = 0.0;
    public boolean audioMode = false;
    
    public void loop(double deltaMs) {
      // TODO(tracy): We need a number of samples over time and then decide if we should switch
      // based on that.
      if (!autoAudioModeP.isOn()) return;
      
      if (lx.engine.audio.meter.getDecibels() > 0.0 && deltaLastModeSwap > 5000.0 && !audioMode) {
        System.out.println(lx.engine.audio.meter.getDecibels());
        UIModeSelector.this.standardMode.setActive(false);
        UIModeSelector.this.interactiveMode.setActive(false);
        UIModeSelector.this.instrumentMode.setActive(false);
        UIModeSelector.this.setAudioChannelEnabled(true);
        audioMode = true;
        deltaLastModeSwap = 0.0;
      } else if (lx.engine.audio.meter.getDecibels() < 0.0 && deltaLastModeSwap > 5000.0 && audioMode) {
        System.out.println("Disabling audio mode.");
        UIModeSelector.this.interactiveMode.setActive(false);
        UIModeSelector.this.instrumentMode.setActive(false);
        UIModeSelector.this.setAudioChannelEnabled(false);
        UIModeSelector.this.standardMode.setActive(true);
        audioMode = false;
        deltaLastModeSwap = 0.0;     
      } else {
        deltaLastModeSwap += deltaMs;
      }
    }
  }
}

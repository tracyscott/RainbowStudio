public class UIModeSelector extends UICollapsibleSection {
  
  public final UIButton audioMode;
  public final UIButton standardMode;
  public final UIButton interactiveMode;
  public final UIButton instrumentMode;
  protected LX lx;
  
  public UIModeSelector(final LXStudio.UI ui, LX lx) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("MODE");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    this.lx = lx;
    
    this.audioMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          UIModeSelector.this.standardMode.setActive(false);
          UIModeSelector.this.interactiveMode.setActive(false);
          UIModeSelector.this.instrumentMode.setActive(false);
          // Enable AUDIO channel
          setAudioChannelEnabled(true);
        } else {
          // Disable AUDIO channel
          setAudioChannelEnabled(false);
        }
      }
    }
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
          // Disable Standard Channels
          setStandardChannelsEnabled(false);
        }
      }
    }
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

package com.giantrainbow.ui;

import com.giantrainbow.UtilsForLX;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXChannelBus;
import heronarts.lx.midi.*;
import heronarts.lx.studio.LXStudio;

import java.util.logging.Logger;

public class UIMidiControl extends UIConfig implements LXMidiListener  {
  private static final Logger logger = Logger.getLogger(UIMidiControl.class.getName());

  public static String MIDI_CH = "MidiCh";
  public static String NEXT_GAME = "NxtGm";
  public static String PREV_GAME = "PrvGm";
  public static String AUDIO_MODE = "AudioM";
  public static String STANDARD_MODE = "StdM";
  public static String INSTRUMENT_MODE = "InstrM";
  public static String INTERACTIVE_MODE = "InterM";
  public static String AUTO_AUDIO = "AutoAu";

  // Pads produce this cc messages on Rainbow Bridge keyboard controller
  // cc 0, 2, 3, 4, 6, 8, 9, 11, 65

  protected static LX lx;
  protected static UIModeSelector modeSelector;
  public static String title = "MIDI CTRL";
  public static String filename = "midictrl.json";

  public UIMidiControl(final LXStudio.UI ui, LX lx, UIModeSelector modeSelector) {
    super(ui, title, filename);
    this.lx = lx;
    this.modeSelector = modeSelector;

    registerDiscreteParameter(MIDI_CH, 0, 0, 33);
    registerDiscreteParameter(NEXT_GAME, 0, 0, 128);
    registerDiscreteParameter(PREV_GAME, 2, 0, 128);
    registerDiscreteParameter(AUDIO_MODE, 3, 0, 128);
    registerDiscreteParameter(STANDARD_MODE, 4, 0, 128);
    registerDiscreteParameter(INSTRUMENT_MODE, 6, 0, 128);
    registerDiscreteParameter(INTERACTIVE_MODE, 8, 0, 128);
    registerDiscreteParameter(AUTO_AUDIO, 9, 0, 128);

    save();
    buildUI(ui);

  }

  public void controlChangeReceived(MidiControlChange cc) {
    logger.info("cc: " + cc.getCC() + "  value:" + cc.getValue() + " ch: " + cc.getChannel());

    if (cc.getChannel() == getDiscreteParameter(MIDI_CH).getValuei()) {
      if (cc.getCC() == getDiscreteParameter(PREV_GAME).getValuei()) {
        LXChannelBus interactive = UtilsForLX.getChannelByLabel(lx, "INTERACTIVE");
        if (interactive != null) {
          ((LXChannel) interactive).goPrev();
        }
      }
      if (cc.getCC() == getDiscreteParameter(NEXT_GAME).getValuei()) {
        LXChannelBus interactive = UtilsForLX.getChannelByLabel(lx, "INTERACTIVE");
        if (interactive != null) {
          ((LXChannel) interactive).goNext();
        }
      }
      if (cc.getCC() == getDiscreteParameter(AUDIO_MODE).getValuei()) {
        modeSelector.audioModeP.setValue(true);
      }
      if (cc.getCC() == getDiscreteParameter(STANDARD_MODE).getValuei()) {
        modeSelector.standardModeP.setValue(true);
      }
      if (cc.getCC() == getDiscreteParameter(INSTRUMENT_MODE).getValuei()) {
        modeSelector.instrumentModeP.setValue(true);
      }
      if (cc.getCC() == getDiscreteParameter(INTERACTIVE_MODE).getValuei()) {
        modeSelector.interactiveModeP.setValue(true);
      }
      if (cc.getCC() == getDiscreteParameter(AUTO_AUDIO).getValuei()) {
        logger.info("Setting autoAudioModeP");
        modeSelector.autoAudioModeP.toggle();
      }
    }
  }

  public void aftertouchReceived(MidiAftertouch aftertouch) {}
  public void noteOffReceived(MidiNote note) {}
  public void noteOnReceived(MidiNoteOn note) {}
  public void pitchBendReceived(MidiPitchBend pitchBend) {}
  public void programChangeReceived(MidiProgramChange pc) {}
}

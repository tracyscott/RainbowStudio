package com.giantrainbow.ui;

import com.giantrainbow.UtilsForLX;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXChannelBus;
import heronarts.lx.midi.*;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIKnob;

import java.util.logging.Logger;

public class UIMidiControl extends UICollapsibleSection implements LXMidiListener  {
  private static final Logger logger = Logger.getLogger(UIMidiControl.class.getName());

  LXMidiOutput midiThroughOutput;

  public static StringParameter midiControl = new StringParameter("MIDI Control", "NONE");
  public static DiscreteParameter midiChoice = new DiscreteParameter("midictrl", 0, 10);

  public static DiscreteParameter midiChP = new DiscreteParameter("MidiCh", 9, 0, 32 );
  public static DiscreteParameter nextGameP = new DiscreteParameter("NxtGm", 48, 0, 127);
  public static DiscreteParameter prevGameP = new DiscreteParameter("PrvGm",49, 0, 127);
  public static DiscreteParameter audioModeP = new DiscreteParameter("AudioM", 40, 0, 127);
  public static DiscreteParameter standardModeP = new DiscreteParameter("StdM", 41, 0, 127);
  public static DiscreteParameter instrumentModeP = new DiscreteParameter("InstrM", 42, 0, 127);
  public static DiscreteParameter interactiveModeP = new DiscreteParameter("InterM", 43, 0, 127);
  public static DiscreteParameter autoAudioP = new DiscreteParameter("AutoAu", 48, 0, 127);

  public static UIKnob midiCh;
  public static UIKnob nextGame;
  public static UIKnob prevGame;
  public static UIKnob audioMode;
  public static UIKnob standardMode;
  public static UIKnob instrumentMode;
  public static UIKnob interactiveMode;
  public static UIKnob autoAudio;

  protected static LX lx;
  protected static UIModeSelector modeSelector;

  public UIMidiControl(final LXStudio.UI ui, LX lx, UIModeSelector modeSelector) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("MIDI CTRL");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    UIMidiControl.lx = lx;
    UIMidiControl.modeSelector = modeSelector;
    UI2dContainer knobsContainer = new UI2dContainer(0, 30, getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    midiCh = addKnob(midiChP, knobsContainer);
    nextGame = addKnob(nextGameP, knobsContainer);
    prevGame = addKnob(prevGameP, knobsContainer);
    audioMode = addKnob(audioModeP, knobsContainer);
    knobsContainer.addToContainer(this);
    knobsContainer = new UI2dContainer(0, 30, getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    standardMode = addKnob(standardModeP, knobsContainer);
    instrumentMode = addKnob(instrumentModeP, knobsContainer);
    interactiveMode = addKnob(interactiveModeP, knobsContainer);
    autoAudio = addKnob(autoAudioP, knobsContainer);
    knobsContainer.addToContainer(this);
  }

  protected UIKnob addKnob(DiscreteParameter p, UI2dContainer container) {
    UIKnob uiKnob = new UIKnob(p);
    uiKnob.addToContainer(container);
    return uiKnob;
  }

  public void aftertouchReceived(MidiAftertouch aftertouch) {
    logger.info("aftertouch");
  }
  public void controlChangeReceived(MidiControlChange cc) {
    logger.info("cc");
  }
  public void noteOffReceived(MidiNote note) {
    logger.info("noteOffReceived");
  }
  public void noteOnReceived(MidiNoteOn note) {
    logger.info("noteOnReceived: " + note.getPitch() + " ch: " + note.getChannel());
    if (note.getChannel() == midiChP.getValuei()) {
      if (note.getPitch() == prevGameP.getValuei()) {
        LXChannelBus interactive = UtilsForLX.getChannelByLabel(lx, "INTERACTIVE");
        if (interactive != null) {
          ((LXChannel) interactive).goPrev();
        }
      }
      if (note.getPitch() == nextGameP.getValuei()) {
        LXChannelBus interactive = UtilsForLX.getChannelByLabel(lx, "INTERACTIVE");
        if (interactive != null) {
          ((LXChannel) interactive).goNext();
        }
      }
      if (note.getPitch() == audioModeP.getValuei()) {
        modeSelector.audioModeP.setValue(true);
      }
      if (note.getPitch() == standardModeP.getValuei()) {
        modeSelector.standardModeP.setValue(true);
      }
      if (note.getPitch() == instrumentModeP.getValuei()) {
        modeSelector.instrumentModeP.setValue(true);
      }
      if (note.getPitch() == interactiveModeP.getValuei()) {
        modeSelector.interactiveModeP.setValue(true);
      }
      if (note.getPitch() == autoAudioP.getValuei()) {
        logger.info("Setting autoAudioModeP");
        modeSelector.autoAudioModeP.toggle();
      }
    }
  }

  public void pitchBendReceived(MidiPitchBend pitchBend) {
    logger.info("pitchBend");
  }
  public void programChangeReceived(MidiProgramChange pc) {
    logger.info("programChange");
  }
}

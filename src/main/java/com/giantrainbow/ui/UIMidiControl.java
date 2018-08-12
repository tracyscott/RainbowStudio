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
import heronarts.p3lx.ui.component.UIDropMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UIMidiControl extends UICollapsibleSection implements LXMidiListener  {
  private static final Logger logger = Logger.getLogger(UIMidiControl.class.getName());

  LXMidiOutput midiThroughOutput;

  public static StringParameter midiControl = new StringParameter("MIDI Control", "NONE");
  public static DiscreteParameter midiChoice = new DiscreteParameter("midictrl", 0, 10);
  public static UIDropMenu midiChoices;
  protected static LX lx;
  protected static UIModeSelector modeSelector;

  public UIMidiControl(final LXStudio.UI ui, LX lx, UIModeSelector modeSelector) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("MIDI CTRL");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    this.lx = lx;
    this.modeSelector = modeSelector;

    LXMidiEngine midi = lx.engine.midi;

    List<String> midiInputs = new ArrayList<String>();
    midiInputs.add("NONE");
    logger.info("Checking midi inputs");
    for (LXMidiInput input: midi.inputs) {
      logger.info("midi input: " + input.getName() + ": " + input.getDescription());
      midiInputs.add(input.getName());
    }
    midiChoices = new UIDropMenu(0f,0f, ui.leftPane.global.getContentWidth(), 20f, midiChoice);
    String[] choices = new String[midiInputs.size()];
    choices = midiInputs.toArray(choices);
    midiChoices.setOptions(choices);
    midiChoices.addToContainer(this);

    // Find target output for passing MIDI through

    for (LXMidiOutput output : midi.outputs) {
      logger.info(output.getName() + ": " + output.getDescription());
      if (output.getName().equalsIgnoreCase("rainbowStudioOut")) {
        midiThroughOutput = output;
        midiThroughOutput.open();
      }
    }
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
    int prevGamePitch = 48;
    int nextGamePitch = 50;
    int modeSelectionChannel = 9;
    int gameSelectionChannel = 0;
    int audioModePitch = 40;
    int standardModePitch = 41;
    int instrumentModePitch = 42;
    int interactiveModePitch = 43;
    int autoAudioModePitch = 48;

    logger.info("noteOnReceived" + note.getPitch() + " ch: " + note.getChannel());
    if (note.getChannel() == gameSelectionChannel) {
      if (note.getPitch() == prevGamePitch) {
        LXChannelBus interactive = UtilsForLX.getChannelByLabel(lx, "INTERACTIVE");
        if (interactive != null) {
          ((LXChannel) interactive).goPrev();
        }
      } else if (note.getPitch() == nextGamePitch) {
        LXChannelBus interactive = UtilsForLX.getChannelByLabel(lx, "INTERACTIVE");
        if (interactive != null) {
          ((LXChannel) interactive).goNext();
        }
      }
    }

    if (note.getChannel() == modeSelectionChannel) {
      if (note.getPitch() == audioModePitch) {
        modeSelector.audioModeP.setValue(true);
      }

      if (note.getPitch() == standardModePitch) {
        modeSelector.standardModeP.setValue(true);
      }
      if (note.getPitch() == instrumentModePitch) {
        modeSelector.instrumentModeP.setValue(true);
      }
      if (note.getPitch() == interactiveModePitch) {
        modeSelector.interactiveModeP.setValue(true);
      }
      if (note.getPitch() == autoAudioModePitch) {
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

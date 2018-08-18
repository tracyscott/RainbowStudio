package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.midi.LXMidiOutput;
import heronarts.lx.midi.MidiAftertouch;
import heronarts.lx.midi.MidiControlChange;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;
import heronarts.lx.midi.MidiProgramChange;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.logging.Logger;

/**
 * Base class for MIDI patterns.  This class implements some MIDI Through logic that
 * is necessary on Windows.  Relies on a virtual MIDI port created in 'loopmidi' program.
 */
abstract class MidiBase extends LXPattern {
  private static final Logger logger = Logger.getLogger(MidiBase.class.getName());

  LXMidiOutput midiThroughOutput;
  DiscreteParameter midiCh = new DiscreteParameter("midiCh", 0, 0, 16);

  public MidiBase(LX lx) {
    super(lx);
    addParameter(midiCh);
    // Find target output for passing MIDI through
    LXMidiEngine midi = lx.engine.midi;
    for (LXMidiOutput output : midi.outputs) {
      logger.info(output.getName() + ": " + output.getDescription());
      if (output.getName().equalsIgnoreCase("rainbowStudioOut")) {
        midiThroughOutput = output;
        midiThroughOutput.open();
      }
    }
  }

  public void noteOnReceived(MidiNoteOn note) {
    if (midiThroughOutput != null)
      midiThroughOutput.send(note);
  }

  public void noteOffReceived(MidiNote note) {
    if (midiThroughOutput != null)
      midiThroughOutput.send(note);
  }

  public void afterTouchReceived(MidiAftertouch aftertouch) {
    if (midiThroughOutput != null)
      midiThroughOutput.send(aftertouch);
  }

  public void controlChangeReceived(MidiControlChange cc) {
    if (midiThroughOutput != null)
      midiThroughOutput.send(cc);
  }

  public void pitchBendReceived(MidiPitchBend pitchBend) {
    if (midiThroughOutput != null)
      midiThroughOutput.send(pitchBend);
  }

  public void programChangeReceived(MidiProgramChange pc) {
    if (midiThroughOutput != null)
      midiThroughOutput.send(pc);
  }
}

package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.midi.MidiAftertouch;
import heronarts.lx.midi.MidiControlChange;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;
import heronarts.lx.midi.MidiProgramChange;

/*
 * Base class for MIDI patterns.  This class implements some MIDI Through logic that
 * is necessary on Windows.  Relies on a virtual MIDI port created in 'loopmidi' program.
 */
abstract class MidiBase extends LXPattern {
  heronarts.lx.midi.LXMidiOutput midiThroughOutput;

  public MidiBase(LX lx) {
    super(lx);
    // Find target output for passing MIDI through
    heronarts.lx.midi.LXMidiEngine midi = lx.engine.midi;
    for (heronarts.lx.midi.LXMidiOutput output : midi.outputs) {
      System.out.println(output.getName() + ": " + output.getDescription());
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

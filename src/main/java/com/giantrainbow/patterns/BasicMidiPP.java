package com.giantrainbow.patterns;

import static processing.core.PApplet.map;
import static processing.core.PApplet.round;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.LXMidiOutput;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import java.util.logging.Logger;

@LXCategory(LXCategory.FORM)
public class BasicMidiPP extends MidiBase {
  private static final Logger logger = Logger.getLogger(BasicMidiPP.class.getName());

  public final CompoundParameter brightnessKnob =
      new CompoundParameter("bright", 1.0, 100.0)
          .setDescription("Brightness");

  public final CompoundParameter barsKnob =
      new CompoundParameter("bars", 5, 6)
          .setDescription("Brightness");


  private float currentHue = 100.0f;
  private float currentBrightness = 0.0f;
  LXMidiOutput midiThroughOutput;
  private int bar = -1;

  public BasicMidiPP(LX lx) {
    super(lx);
    brightnessKnob.setValue(30);
    barsKnob.setValue(6);
    addParameter(brightnessKnob);
    addParameter(barsKnob);
  }

  public void run(double deltaMs) {
    int numPixelsPerRow = ((RainbowBaseModel)lx.model).pointsWide;
    int numRows = ((RainbowBaseModel)lx.model).pointsHigh;
    int pointNumber = 0;
    int numBars = round((float)(barsKnob.getValue()));
    for (LXPoint p : model.points) {
      int rowNumber = pointNumber / numPixelsPerRow;
      if (numBars < 1) numBars = 1;
      if ((rowNumber)/(numRows/numBars) == bar) {
        colors[p.index] = LXColor.gray(100);
      } else {
        colors[p.index] = LXColor.gray(brightnessKnob.getValue());
      }
      ++pointNumber;
    }
  }

  // Map a note to a hue
  public void noteOnReceived(MidiNoteOn note) {
    int pitch = note.getPitch();
    logger.info("pitch: " + pitch);
    // Start at note 60, White keys
    if (pitch == 60) {
      bar = 0;
    } else if (pitch == 62) {
      bar = 1;
    } else if (pitch == 64) {
      bar = 2;
    } else if (pitch == 65) {
      bar = 3;
    } else if (pitch == 67) {
      bar = 4;
    } else if (pitch == 69) {
      bar = 5;
    }
    int velocity = note.getVelocity();
    // NOTE: my mini keyboard generates between 48 & 72 (small keyboard)
    currentHue = map(pitch, 48.0f, 72.0f, 0.0f, 100.0f);
    currentBrightness = map(velocity, 0.0f, 127.0f, 0.0f, 100.0f);

    // Necessary to call for MIDI Through note forwarding.
    super.noteOnReceived(note);
  }

  public void noteOffReceived(MidiNote note) {
    // Releasing any note will turn it off.  Multiple notes can be
    // on at once and to turn off when all notes are released we need
    // to track the notes on and only go black once we have received
    // note-off for all notes.
    currentBrightness = 0.0f;
    bar = -1;

    // Necessary to call for MIDI Through note forwarding.
    super.noteOffReceived(note);
  }
}

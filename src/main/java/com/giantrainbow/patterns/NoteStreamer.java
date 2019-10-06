package com.giantrainbow.patterns;

import com.giantrainbow.model.RainbowBaseModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.midi.*;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PConstants;

import java.util.*;
import java.util.logging.Logger;

@LXCategory(LXCategory.FORM)
public class NoteStreamer extends MidiBasePP {
  private static final Logger logger = Logger.getLogger(NoteStreamer.class.getName());

  DiscreteParameter midiCh = new DiscreteParameter("midiCh", 0, 0, 16);

  public final CompoundParameter keysKnob =
      new CompoundParameter("keys", 49, 25, 88)
          .setDescription("Musical Keys");

  public final CompoundParameter fadeTime =
      new CompoundParameter("fadeMs", 1000, 0, 10000)
      .setDescription("Fade time for old notes");

  public final CompoundParameter speedKnob =
      new CompoundParameter("noteSpd", 1f, 0.1f, 20f);

  static private final int MIDDLEC = 60;

  protected int defaultNoteWidth = 10;

  static public class Note {
    int noteVal;
    long startTimeMs;
    long endTimeMs;
    float xPos;
    float yPos;
    int noteHeight;
    int noteWidth;
    boolean done;
  }

  // Track all played notes so that we can slowly fade them out as they
  // move up.  Once they are fully faded out or out of the render window
  // they can then be removed from the list.
  protected List<Note> playedNotes = new ArrayList<Note>();

  public NoteStreamer(LX lx) {
    super(lx);
    keysKnob.setValue(49);
    addParameter(keysKnob);
    addParameter(fadeTime);
    addParameter(speedKnob);
    addParameter(paletteKnob);
    addParameter(randomPaletteKnob);
    addParameter(saturation);
    addParameter(hue);
    addParameter(bright);
  }

  public void draw(double drawDeltaMs) {
    // Render notes
    defaultNoteWidth = pg.width / (int)keysKnob.getValuef();
    pg.colorMode(PConstants.HSB, 1.0f, 1.0f, 1.0f, 1.0f);
    pg.background(0, 0);
    updateNotePositions();
    for (Note note : playedNotes) {
      float percentFaded = 0f;
      if (note.endTimeMs > 0)
        percentFaded = (System.currentTimeMillis() - note.endTimeMs)/fadeTime.getValuef();
      if (percentFaded > 1.0f) {
        note.done = true;
      } else {
        drawNote(note, (1f - percentFaded));
      }
    }
    garbageCollectNotes();
  }

  protected void drawNote(Note note, float alpha) {
    float hsb[] = {1f, 1f, 1f};
    getNewHSB(hsb, note.noteVal % palette.length);
    pg.fill(hsb[0], hsb[1], hsb[2], alpha);
    pg.noStroke();
    if (note.endTimeMs > 0)
      pg.rect(note.xPos, note.yPos, note.noteWidth, note.noteHeight);
    else
      pg.rect(note.xPos, note.yPos, note.noteWidth, pg.height - (int)note.yPos);
  }

  /**
   * Based on the noteVal, choose a corresponding X position for the note.
   * @param n
   */
  protected void setNoteXPos(Note n) {
    int numCol = ((RainbowBaseModel)lx.model).pointsWide;
    int centerRainbow;

    // Center of Rainbow
    if (numCol %2 == 0) {
      centerRainbow = (numCol/2);
    } else {
      centerRainbow = (numCol/2) + 1;
    }
    n.xPos = (n.noteVal - MIDDLEC) * n.noteWidth + centerRainbow;
  }


  protected void updateNotePositions() {
    for (Note note: playedNotes) {
      note.yPos -= speedKnob.getValuef()/10f;
    }
  }

  protected void garbageCollectNotes() {
    Iterator<Note> it = playedNotes.iterator();
    while (it.hasNext()) {
      Note n = it.next();
      if (n.done)
        it.remove();
    }
  }

  // Map a note to a hue
  @Override
  public void noteOnReceived(MidiNoteOn note) {
    // Only light up keys for specified midiCh.  Allow the drumkit to pass through
    // though.
    if (note.getChannel() == midiCh.getValuei()) {
      int midiNote = note.getPitch();
      if (midiNote >= 0 && midiNote <= 127) {
        Note n = new Note();
        n.noteVal = midiNote;
        n.startTimeMs = System.currentTimeMillis();
        n.noteWidth = defaultNoteWidth;
        n.noteHeight = 2;
        n.yPos = pg.height - 2;
        setNoteXPos(n);
        playedNotes.add(n);
      }
    }

    super.noteOnReceived(note);
  }

  Note findMostRecentExistingNote(int note) {
    ListIterator<Note> nIt = playedNotes.listIterator(playedNotes.size());
    while (nIt.hasPrevious()) {
      Note n = nIt.previous();
      if (n.noteVal == note)
        return n;
    }
    return null;
  }

  @Override
  public void noteOffReceived(MidiNote note) {
    // Only keyboard notes should light up leds but let drumkit passthrough.
    if (note.getChannel() == midiCh.getValuei()) {
      int midiNote = note.getPitch();
      if (midiNote >= 0 && midiNote <= 127) {
        Note n = findMostRecentExistingNote(midiNote);
        if (n != null) {
          n.endTimeMs = System.currentTimeMillis();
          n.noteHeight = pg.height - (int)n.yPos;
        }
      }
    }
    super.noteOffReceived(note);
  }
}

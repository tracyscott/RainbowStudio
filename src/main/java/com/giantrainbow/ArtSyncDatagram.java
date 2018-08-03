package com.giantrainbow;

import heronarts.lx.output.LXDatagram;

public class ArtSyncDatagram extends LXDatagram {

  private final static int ARTSYNC_HEADER_LENGTH = 14;

  public ArtSyncDatagram(int artnetPort) {
    super(ARTSYNC_HEADER_LENGTH);
    setPort(artnetPort);

    this.buffer[0] = 'A';
    this.buffer[1] = 'r';
    this.buffer[2] = 't';
    this.buffer[3] = '-';
    this.buffer[4] = 'N';
    this.buffer[5] = 'e';
    this.buffer[6] = 't';
    this.buffer[7] = 0;
    this.buffer[8] = 0x00; // OpSync low byte
    this.buffer[9] = 0x52; // OpSync hi byte
    this.buffer[10] = 0; // Protocol version
    this.buffer[11] = 14; // Protocol version
    this.buffer[12] = 0; // Aux1
    this.buffer[13] = 0; // Aux2
  }

  @Override
  public void onSend(int[] colors) {
    // Nothing else needed!
  }

}

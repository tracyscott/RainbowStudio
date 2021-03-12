package com.giantrainbow.patterns;

import com.giantrainbow.PathUtils;
import com.giantrainbow.RainbowStudio;
import com.giantrainbow.VideoUtil;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.CustomDeviceUI;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.*;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MP4/H264 Video playing pattern.  This pattern will stream a video file from disk, looping when reaching the end
 * of the file. There a few performance related parameters that might need to be tuned for the given hardware.
 * The pattern supports reading from the video file synchronously or asynchronously via a dedicated decoding thread.
 * It is preferable to use a dedicated decoding thread to utilize additional cpu cores.
 */
public class Video extends PGPixelPerfect implements CustomDeviceUI {
  private static final Logger logger = Logger.getLogger(Video.class.getName());

  private static final String VIDEOS_WORKING_DIR = "videos/";
  private static final String VIDEOS_PKG_DIR = "videos/";

  private VideoUtil.FrameRingBuffer ringBuffer;
  private int wakeupDecoderThreshold = 4;  // Size of ringBuffer.numBuffered() before waking up Decoder.
  private VideoUtil.FrameDecoder decoder;
  private String defaultVideoFilename = "video.mp4";
  private boolean async = true;  // If true, use a ring buffer and separate decoding thread.
  public final StringParameter videoKnob =
      new StringParameter("video", "video.mp4")
          .setDescription("Texture image for rainbow.");
  protected List<FileItem> fileItems = new ArrayList<FileItem>();
  protected UIItemList.ScrollList fileItemList;
  protected List<String> videoFiles;
  private static final int CONTROLS_MIN_WIDTH = 160;

  private static final List<String> VIDEO_EXTS = Arrays.asList(".mp4");

  public Video(LX lx) {
    super(lx, null);
    reloadFileList();
    addParameter(videoKnob);
    videoKnob.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        StringParameter iKnob = (StringParameter) parameter;
        loadVideo(iKnob.getString());
      }
    });
    installVideoFiles();
    videoKnob.setValue(defaultVideoFilename);
    loadVideo(videoKnob.getString());
  }

  /**
   * Loads the video file.  This will create our ring buffer and start the
   * frame decoder.  This is called each time we reload a new file.
   *
   * @param filename
   */
  public void loadVideo(String filename) {
    ringBuffer = new VideoUtil.FrameRingBuffer(10, 5);
    decoder = new VideoUtil.FrameDecoder(ringBuffer, VIDEOS_WORKING_DIR + filename, pg.width, pg.height);
    if (async) startDecoder();
  }

  public void startDecoder() {
    // Start the decoder and have it preload some frames.
    new Thread(decoder).start();
  }

  protected void reloadFileList() {
    videoFiles = PathUtils.findDataFiles(VIDEOS_WORKING_DIR, VIDEO_EXTS);
    fileItems.clear();
    for (String filename : videoFiles) {
      // Use a name that's suitable for the knob
      int index = filename.lastIndexOf('/');
      if (index >= 0) {
        filename = filename.substring(index + 1);
      }
      fileItems.add(new Video.FileItem(filename));
    }
    if (fileItemList != null) {
      fileItemList.setItems(fileItems);
    }
  }

  /**
   * Gets the next frame out of the ringbuffer, notifies the decoder if necessary, and then copies the frame
   * into our PGraphics drawing surface.
   */
  public void renderNextFrame() {
    PImage nextFrame;
    if (async) {
      nextFrame = nextFrameRingBuffer();
    } else {
      nextFrame = nextFrameBlocking();
    }

    if (nextFrame == null) {
      // This might happen at startup, just don't render until we are loaded.
      // logger.info("Frame was null, skipping!");
      return;
    }

    pg.image(nextFrame, 0, 0);
  }

  /**
   * Return the next frame from the ring buffer.  This happens in async mode when we start a separate decoder
   * thread.  We need to check how much head room we have and potentially notify the decoder thread that it needs
   * to decode some more frames.
   * @return
   */
  public PImage nextFrameRingBuffer() {
    PImage next = ringBuffer.take();
    int numBuffered = ringBuffer.numBuffered();
    if (numBuffered < wakeupDecoderThreshold) {
      synchronized(ringBuffer) {
        ringBuffer.notifyAll();
      }
    }

    // TODO(tracy): Should be some way to monitor frame starvation for asynchronous decoder thread and maybe
    // auto-adjust tuning parameters.  Although, if it can't keep up, nothing can be done.  Mostly an issue if we
    // are trying to skimp on memory with minimizing the number of read-ahead frames.
    /*
    if (numBuffered < 2) {
      logger.info("-----");
      logger.info("currentTakePos: " + ringBuffer.currentTakePos());
      logger.info("currentPutPos: " + ringBuffer.currentPutPos());
      logger.info("num buffered: " + numBuffered);
    }
    */

    return next;
  }

  public PImage nextFrameBlocking() {
    return decoder.nextFrame();
  }

  @Override
  protected void draw(double deltaDrawMs) {
    renderNextFrame();
  }

  @Override
  protected void imageToPoints() {
    super.imageToPoints();
  }


  //
  // Custom UI to allow for the selection of the shader file
  //
  @Override
  public void buildDeviceUI(UI ui, final UI2dContainer device) {
    device.setContentWidth(CONTROLS_MIN_WIDTH);
    device.setLayout(UI2dContainer.Layout.VERTICAL);
    device.setPadding(3, 3, 3, 3);

    UI2dContainer knobsContainer = new UI2dContainer(0, 0, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(3, 3, 3, 3);
    new UIKnob(fpsKnob).addToContainer(knobsContainer);

    new UIButton() {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          reloadFileList();
        }
      }
    }.setLabel("rescan dir")
        .setMomentary(true)
        .setWidth(60)
        .setHeight(25)
        .addToContainer(knobsContainer);

    knobsContainer.addToContainer(device);

    UI2dContainer filenameEntry = new UI2dContainer(0, 0, device.getWidth(), 30);
    filenameEntry.setLayout(UI2dContainer.Layout.HORIZONTAL);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
        .setParameter(videoKnob)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(filenameEntry);


    // Button for reloading image file list.
    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          loadVideo(videoKnob.getString());
        }
      }
    }.setLabel("\u21BA")
        .setMomentary(true)
        .addToContainer(filenameEntry);
    filenameEntry.addToContainer(device);

    fileItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);
    fileItemList.setShowCheckboxes(false);
    fileItemList.setItems(fileItems);
    fileItemList.addToContainer(device);
  }

  public class FileItem extends FileItemBase {
    FileItem(String filename) {
      super(filename);
    }
    public void onActivate() {
      videoKnob.setValue(filename);
      loadVideo(filename);
    }
  }

  /**
   * Standard video files will be packaged with the jar.  On startup, we copy the video
   * files into videos/ directory off of the working directory.  This allows us to both
   * have a single package JAR file and potentially add new videos at runtime if we
   * would like to do that.
   */
  public void installVideoFiles() {
    // For each video, ensure there's a local copy in "videos/"
    File videosDir = new File(VIDEOS_WORKING_DIR);
    if (videosDir.exists()) {
      if (!videosDir.isDirectory()) {
        logger.warning("Could not create \"" + VIDEOS_WORKING_DIR + "\" directory");
        videosDir = null;
      }
    } else {
      // Try to create the directory
      if (videosDir.mkdir()) {
        logger.info("Created \"" + VIDEOS_WORKING_DIR + "\" directory");
      } else {
        logger.warning("Could not create \"" + VIDEOS_WORKING_DIR + "\" directory");
        videosDir = null;
      }
    }
    videoFiles = PathUtils.findDataFiles(VIDEOS_PKG_DIR, ".mp4"); //findShaderFiles(LOCAL_SHADER_DIR);
    Collections.sort(videoFiles);
    for (String filename : videoFiles) {
      // Copy all the videos locally to our videos/ working directory.
      if (videosDir != null) {
        try (InputStream in = RainbowStudio.pApplet.createInput(filename)) {
          File videoFile = new File(videosDir, new File(filename).getName());
          if (videoFile.exists()) {
            logger.info("Not overwriting video: from=data:" + filename + " to=" + videoFile);
          } else {
            try {
              Files.copy(in, videoFile.toPath());
              logger.info("Copied video: from=data:" + filename + " to=" + videoFile);
            } catch (IOException ex) {
              logger.log(Level.SEVERE,
                  "Error copying video: from=data:" + filename + " to=" + videoFile);
            }
          }
        } catch (IOException ex) {
          logger.log(Level.SEVERE, "Error accessing packaged video resource: " + filename, ex);
        }
      }
    }
  }
}

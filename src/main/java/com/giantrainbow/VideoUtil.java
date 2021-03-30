package com.giantrainbow;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.scale.AWTUtil;
import processing.core.PImage;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Utility class for decoding MP4/H264 video files.  See Video pattern for usage.
 */
public class VideoUtil {
  private static final Logger logger = Logger.getLogger(VideoUtil.class.getName());

  /**
   * Initial implementation of a video frame ringbuffer.  This might need some fine tuning on the
   * synchronization.  As long as decoding is actually outside of the synchronization blocks it should be
   * fine though.
   */
  static public class FrameRingBuffer {
    private PImage[] frames;
    public int readAhead;
    private int currentTakePos = 0;
    private int currentPutPos = 0;

    public FrameRingBuffer(int bufferSize, int readAheadNum) {
      frames = new PImage[bufferSize];
      readAhead = readAheadNum;
    }

    public synchronized PImage take() {
      PImage frame = frames[currentTakePos];
      currentTakePos++;
      // Wrap around for ring buffer.
      if (currentTakePos >= frames.length)
        currentTakePos -= frames.length;
      return frame;
    }

    public synchronized void put(PImage frame) {
      frames[currentPutPos] = frame;
      currentPutPos++;
      // Wrap around for ring buffer.
      if (currentPutPos >= frames.length)
        currentPutPos -= frames.length;
    }

    public synchronized int numBuffered() {
      int distance = currentPutPos - currentTakePos;
      // Account for wrap around.
      if (distance < 0) {
        distance = (frames.length - currentTakePos) + currentPutPos;
      }
      return distance;
    }

    public synchronized int currentPutPos() {
      return currentPutPos;
    }

    public synchronized int currentTakePos() {
      return currentTakePos;
    }
  }

  /**
   * Video decoder.  Can be run as either a thread or synchronously.  When run as a thread, the decoder
   * will read ahead FrameRingBuffer.readAhead number of frames and sleep until the number of read-ahead frames
   * decreases to some threshold amount, at which point the consumer of the frames should call
   * FrameRingBuffer.notifyAll to wake up the thread and decode some additional frames.  The purpose of having
   * an asynchronous decoder is so that the renderer can run smoothly.  Also, on machines with additional cores/threads,
   * a separate decoder thread helps utilize the additional cores.
   *
   * NOTE(tracy): Besides the threaded decoder read-ahead to ensure rendering smoothness, we also need to initially read-ahead
   * additional frames so that we can properly order the frames since H264 I,B,P frames can be out of order.  For
   * synchronous mode, there will be an extra startup cost but after that the reorder buffer is kept ahead at the same
   * rate as the rendering code requests additional frames.
   */
  static public class FrameDecoder implements Runnable {
    private FrameRingBuffer ringBuffer;
    private FrameGrab frameGrabber;
    private int renderWidth;
    private int renderHeight;

    // Frames can be out of order so we need to read-ahead and sort them.
    private TreeMap<Double, BufferedImage> reorderBuffer = new TreeMap<Double, BufferedImage>();
    private TreeMap<Double, BufferedImage> nextReorderBuffer = new TreeMap<Double, BufferedImage>();
    private boolean endOfVideo = false;
    private int frameReorderLookAhead = 7;

    int frameNumber = 0;
    int orderedFrameNumber = 0;

    public FrameDecoder(FrameRingBuffer ringBuffer, String filename, int renderWidth, int renderHeight) {
      this.ringBuffer = ringBuffer;
      this.renderWidth = renderWidth;
      this.renderHeight = renderHeight;
      try {
        File videoFile = new File(filename);
        frameGrabber = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));
      } catch (FileNotFoundException fnfex) {
        logger.info("FileNotFoundException: file: " + filename + " msg: " + fnfex.getMessage());
      } catch (IOException ioex) {
        logger.info("IOException: file: " + filename + " msg: " + ioex.getMessage());
      } catch (JCodecException jcex) {
        logger.info("JCodecException: file: " + filename + " msg: " + jcex.getMessage());
      }
    }

    /**
     * Runs the decoder as an independent thread that keeps ringBuffer populated with frames.  This still requires
     * that the decoder thread can keep up with the frame rate over time but it removes the decoding process from
     * the latency sensitive rendering loop and helps utilize additional cores.
     */
    public void run() {
      while (true) {
        // Generate Frame from video file
        PImage frame = nextFrame();
        ringBuffer.put(frame);
        // If we have read ahead enough frames, we will go to sleep and await for the consumer to notify us
        // when the read ahead buffer distance has reduced to some unsafe threshold.
        if (ringBuffer.numBuffered() >= ringBuffer.readAhead) {
          try {
            synchronized(ringBuffer) {
              ringBuffer.wait();
            }
          } catch (InterruptedException iex) {

          }
        }
      }
    }


    /**
     * Return the next PictureWithMetadata frame from the video.
     *
     * @return PictureWithMetadata for next frame.  Null for end of file.
     */
    public PictureWithMetadata nextPictureWithMd() {
      try {
        PictureWithMetadata pmeta = frameGrabber.getNativeFrameWithMetadata();
        frameNumber++;
        return pmeta;
      } catch (IOException ioex) {
        logger.info("IOException reading frames: " + ioex.getMessage());
      }
      return null;
    }

    /**
     * Returns the next frame as a BufferedImage in timestamp order.
     *
     * @return Next frame in timestamp order as a BufferedImage.
     */
    public BufferedImage nextPictureInOrder() {
      // If the look-ahead buffer is too small, fill it up.  In order to handle the end of file case, we
      // actually want to make sure that we have at least frameReorderLookAhead number of frames between the current
      // reorder buffer and the next reorder buffer, which represents the beginning of the video when we wrap.
      // We need to have separate data structures because we are sorting by timestamp, so attempting to
      // add the wrapped-around beginning-of-video frames to our working reorder buffer would glitch out.
      while ((reorderBuffer.size() + nextReorderBuffer.size()) < frameReorderLookAhead) {
        PictureWithMetadata pmd = nextPictureWithMd();
        // We have hit the end of the stream.  We need to reset the seek position to the beginning of the file,
        // and set a flag that we should prepare to switch over to nextReorderBuffer once the current reorderBuffer
        // is drained.  The flag will also determine if we should be putting additional frames into the
        // nextReorderBuffer, which represents wrapping around to the beginning of the video file.
        if (pmd == null) {
          endOfVideo = true;
          // logger.info("End of video: frameNumber=" + frameNumber);
          frameNumber = 0;
          orderedFrameNumber = 0;
          try {
            frameGrabber.seekToFramePrecise(0);
          } catch (JCodecException jcex) {
            logger.info("JCodecException: Resetting video: " + jcex.getMessage());
          } catch (IOException ioex) {
            logger.info("IOException: Seeking to start of video: " + ioex.getMessage());
          }
        } else {
          //logger.info("pmd timestamp: " + pmd.getTimestamp());
          if (!endOfVideo) {
            reorderBuffer.put(pmd.getTimestamp(), AWTUtil.toBufferedImage(pmd.getPicture()));
          } else {
            nextReorderBuffer.put(pmd.getTimestamp(), AWTUtil.toBufferedImage(pmd.getPicture()));
          }
        }
      }
      double leastPts = reorderBuffer.keySet().iterator().next();
      BufferedImage soonestPmd = reorderBuffer.get(leastPts);
      reorderBuffer.remove(leastPts);

      // If we have drained the existing reorder buffer since we have hit the end of the video, we should swap
      // the variables so that what was the nextReorderBuffer becomes our current operating reorder buffer.
      // reorderBuffer.size() can equal 0 here if there are frameReorderLookAhead number of entries
      // in nextReorderBufferSize.
      if (reorderBuffer.size() == 0 && endOfVideo) {
        TreeMap<Double, BufferedImage> tmp = reorderBuffer;
        reorderBuffer = nextReorderBuffer;
        nextReorderBuffer = tmp;
        endOfVideo = false;
      }
      //logger.info("soonestPmd: " + leastPts);
      return soonestPmd;
    }

    /**
     * Return the next frame of the video file.  If we are at the end of the video, wrap back around to the first
     * frame of the video.
     * NOTE(tracy): Due to b-frame reordering, these frames may not be in the exact expected order.  We need to
     * read-ahead and then sort the frames.
     * https://github.com/jcodec/jcodec/issues/165
     * @return The frame of the video as a PImage.
     */
    public PImage nextFrame() {
        BufferedImage bufferedImage = nextPictureInOrder();
        BufferedImage scaledImg = bufferedImage;

        // For testing, render frames to a file.
        // File outputfile = new File("frame" + String.format("%02d", orderedFrameNumber) + ".jpg");
        // if (!outputfile.exists())
        //  ImageIO.write(bufferedImage, "jpg", outputfile);

        ++orderedFrameNumber;

        // If the video is not renderWidth x renderHeight, force it to fit.  Note that we should never really do this
        // at runtime.  We should offline process the video to be renderWidth x renderHeight using ffmpeg or such.
        if (bufferedImage.getWidth() != renderWidth || bufferedImage.getHeight() != renderHeight) {
          float renderAspectRatio = renderWidth / renderHeight;

          // We will always be cropping out some video to fit it so we should figure out the target source height based on
          // the source width.  With that information, we can crop the source image to get the expected aspect ratio
          // and then scale the image to the render target image width x height
          float sourceImgCroppedHeight = (float) bufferedImage.getWidth() / renderAspectRatio;
          float topHeightOffset = ((float) bufferedImage.getHeight() - sourceImgCroppedHeight) / 2.0f;
          // TODO(tracy): Allow this to be configured for panning around video topHeightOffset = 100f;
          // NOTE(tracy): This offset coord space is 0,0 top left corner and width, height bottom right corner.
          BufferedImage fixedAspectRatioImg = bufferedImage.getSubimage(0, (int) topHeightOffset,
              bufferedImage.getWidth(), (int) sourceImgCroppedHeight);
          // For pre-processed panning videos, we can pre-render them to 420x30 for example and pan the 30 pixel
          // strip up and down once we have a configurable bottomHeightOffset
          // TODO(tracy): Allow for selectable scaling interpolation for performance purposes.
          if (bufferedImage.getWidth() != renderWidth)
            scaledImg = scaleBilinear(fixedAspectRatioImg, renderWidth, renderHeight);
          else
            scaledImg = fixedAspectRatioImg;
        }
        PImage renderImage = new PImage(scaledImg);
        return renderImage;
    }
  }

  public static BufferedImage scaleBilinear(BufferedImage before, int width, int height) {
    final int interpolation = AffineTransformOp.TYPE_BILINEAR;
    return scale(before, interpolation, width, height);
  }

  public static BufferedImage scaleBicubic(BufferedImage before, int width, int height) {
    final int interpolation = AffineTransformOp.TYPE_BICUBIC;
    return scale(before, interpolation, width, height);
  }

  public static BufferedImage scaleNearest(BufferedImage before, int width, int height) {
    final int interpolation = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
    return scale(before, interpolation, width, height);
  }

  /**
   * Scales an image to the target size of width x height.
   *
   * @param before
   * @param type
   * @return
   */
  public static BufferedImage scale(final BufferedImage before, final int type, int width, int height) {
    int w2 = width;
    int h2 = height;
    double scale = (float) w2/ (float)before.getWidth();
    BufferedImage after = new BufferedImage(w2, h2, before.getType());
    AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
    AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, type);
    scaleOp.filter(before, after);
    return after;
  }
}

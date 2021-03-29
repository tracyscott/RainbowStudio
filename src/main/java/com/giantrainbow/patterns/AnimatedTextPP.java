package com.giantrainbow.patterns;

import static processing.core.PApplet.ceil;
import static processing.core.PApplet.round;

import com.giantrainbow.FontUtil;
import com.giantrainbow.RainbowOSC;
import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.*;
import heronarts.p3lx.ui.CustomDeviceUI;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;

@LXCategory(LXCategory.FORM)
public class AnimatedTextPP extends PGPixelPerfect implements CustomDeviceUI {
  private static final Logger logger = Logger.getLogger(AnimatedTextPP.class.getName());

  public final StringParameter textKnob = new StringParameter("str", "");

  List<TextItem> textItems = new ArrayList<>();
  UIItemList.ScrollList textItemList;
  private static final int CONTROLS_MIN_WIDTH = 320;
  public final CompoundParameter xSpeed =
      new CompoundParameter("XSpd", 1.0, 0, 20).setDescription("X speed in pixels per frame");
  public final CompoundParameter fadeTime =
      new CompoundParameter("fade", 1.0, 0.0, 10.0);
  public final BooleanParameter clockwise = new BooleanParameter("clockwise", false);
  public final BooleanParameter oneShot = new BooleanParameter("oneShot", false);
  public final BooleanParameter reset = new BooleanParameter("reset", false);
  public final BooleanParameter advancePattern = new BooleanParameter("advP", true);
  public final BooleanParameter multiply = new BooleanParameter("mult", true);
  public final BooleanParameter osc = new BooleanParameter("osc", false);
  public final BooleanParameter centered = new BooleanParameter("centr", false);
  public final BooleanParameter rbbg = new BooleanParameter("rbbg", false);

  public final DiscreteParameter fontKnob = new DiscreteParameter("font", 0, FontUtil.names().length);
  public final DiscreteParameter fontSizeKnob = new DiscreteParameter("fontsize", 24, 8, 32);
  public final DiscreteParameter txtsKnob = new DiscreteParameter("txts", 0, 0, 41)
      .setDescription("Which AnimatedText#.txts file to use for text input");
  public final CompoundParameter yAdjust = new CompoundParameter("yAdj", 0f, -30f, 30f)
      .setDescription("Adjusts y position of text");
  public final CompoundParameter rbBright = new CompoundParameter("rbbrt", 0.5f, 0.0f, 1.0f);

  String[] defaultTexts = {
      "RAINBOW BRIDGE"
  };

  public final DiscreteParameter  whichText = new DiscreteParameter("which", -1, 0, 1000);


  PGraphics textImage;
  PGraphics multiplyImage;

  volatile boolean doRedraw;
  boolean blankUntilReactivated = false;
  float currentPos = 0.0f;
  int lastPos = 0;
  boolean autoCycleWasEnabled = false;
  boolean noTextUpdateAvailable = true;
  private float curTransparency = 0.0f;
  private double sinceFadeStartMs = 0.0;
  private double sinceFullAlpha = 0.0f;
  private double fullAlphaMs = 5000.0;
  private boolean fadeIn = true;
  private boolean fadeOut = false;
  protected boolean needTextsReload = true;
  protected boolean imageNeedsRerender = true;

  int currIndex;
  UIItemList.Item currItem;

  int textGapPixels = 30;
  PFont font;
  int fontSize = pg.height;

  public AnimatedTextPP(LX lx) {
    super(lx, "");
    reset.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) {
        blankUntilReactivated = false;
        logger.info("Reset value changed");
        // TODO(tracy): Change this to loadNewTextItem() to properly
        // account for OSC updates.
        currItem = textItemList.getFocusedItem();
        redrawTextBuffer();
        BooleanParameter b = (BooleanParameter)p;
        if (b.isOn()) {
          b.setValue(false);
        }
      }
    });
    addParameter(textKnob);
    addParameter(xSpeed);
    addParameter(clockwise);
    addParameter(oneShot);
    addParameter(reset);
    addParameter(advancePattern);
    addParameter(fontKnob);
    addParameter(multiply);
    addParameter(osc);
    addParameter(fontSizeKnob);
    addParameter(centered);
    addParameter(fadeTime);
    addParameter(whichText);
    addParameter(txtsKnob);
    addParameter(yAdjust);
    addParameter(hue);
    addParameter(bright);
    addParameter(saturation);
    addParameter(rbbg);
    addParameter(rbBright);
    randomPaletteKnob.setValue(false);


    txtsKnob.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) {
        needTextsReload = true;
        imageNeedsRerender = true;
      }
    });

    logger.info("listing fonts");
    String[] fontNames = PFont.list();
    for (String fontName : fontNames) {
      logger.info("Font: " + fontName);
    }

    font = FontUtil.getCachedFont("Noto Sans SC", 24);

    /* Emoji smiley, left for reference.  Need to revert to java.awt.Font and Java2D
       to render emoji's. Processing PFont does not support surrogate pairs.
    char[] ch = Character.toChars(0x1F601);
    */

    fontKnob.addListener(new LXParameterListener () {
      @Override
      public void onParameterChanged(LXParameter p) {
        font = FontUtil.getCachedFont(FontUtil.names()[fontKnob.getValuei()], fontSizeKnob.getValuei());
        imageNeedsRerender = true;
      }
    });

    fontSizeKnob.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) {
        font = FontUtil.getCachedFont(FontUtil.names()[fontKnob.getValuei()], fontSizeKnob.getValuei());
        imageNeedsRerender = true;
      }
    });

    whichText.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) { imageNeedsRerender = true; }
    });

    for (int i = 0; i < defaultTexts.length; i++) {
      textItems.add(new TextItem(defaultTexts[i]));
    }
    xSpeed.setValue(1);

    currIndex = -1;
    if (textItems.size() > 0) {
      currIndex = 0;
    }
    doRedraw = true;
  }


  @Override
  protected void setup() {
    // Set the font here so that we can use it for sizing
    if (font == null) {
      pg.textSize(fontSize);
    } else {
      pg.textFont(font);
    }
    doRedraw = true;

    if (textItemList.getItems().size() > 0) {
      if (currIndex < 0) {
        currIndex = 0;
      }
      textItemList.setFocusIndex(currIndex);
      currItem = textItemList.getFocusedItem();
    }
  }

  @Override
  protected void tearDown() {
    if (textImage != null) {
      textImage.dispose();
      textImage = null;
    }
  }

  @Override
  public void onActive() {
    // Reset the guard that prevents the next text item from starting to show
    // while we are performing our fade transition to the next pattern.
    logger.info("onActive: font name=" + FontUtil.names()[fontKnob.getValuei()]);
    imageNeedsRerender = true;
    blankUntilReactivated = false;
    autoCycleWasEnabled = getChannel().autoCycleEnabled.getValueb();
    getChannel().autoCycleEnabled.setValue(false);
    if (font == null) {
      logger.info("FONT NOT FOUND! " + FontUtil.names()[fontKnob.getValuei()]);
    }
  }

  /**
   * Redraws the Text image that we use to scroll across the screen.
   */
  public void redrawTextBuffer() {
    imageNeedsRerender = false;
    String label;

    // If needTextsReload, reload the file containing the list of texts to display.  We also should reset currIndex=0
    if (needTextsReload) {
      needTextsReload = false;
      // logger.info("Reloading texts file AnimatedText" + txtsKnob.getValuei() + ".txts");
      currIndex = 0;
      try {
        String contents = new String(Files.readAllBytes(Paths.get("AnimatedText" + txtsKnob.getValuei() + ".txts")));
        String[] splitContents = contents.split("\\{");
        textItems.clear();
        for (int i = 0; i < splitContents.length; i++) {
          splitContents[i] = splitContents[i].trim();
          // logger.info("Text item: " + splitContents[i]);
          textItems.add(new TextItem(splitContents[i]));
        }
        textItemList.setItems(textItems);
        whichText.setRange(-1, textItems.size());
        textItemList.setFocusIndex(currIndex);
      } catch (IOException ioex) {
        logger.info("Reading TextFx texts: IOException: " + ioex.getMessage());
      }
      needTextsReload = false;
    }

    if (osc.getValueb()) {
      label = RainbowOSC.getTextUpdateMessage();
      // item.getLabel();
      if (label == null) {
        // If there is still no text update available via OSC, just return from this method.
        noTextUpdateAvailable = true;
        return;
      } else {
        noTextUpdateAvailable = false;
      }
      logger.info("received text update=" + label);
    } else {
      UIItemList.Item item = textItemList.getFocusedItem();
      if (item == null) {
        return;
      }
      label = item.getLabel();
      // Allow for whichText to override auto-iteration over the list of texts.  Each text item
      // will be displayed by independent patterns in the channel.
      if (whichText.getValuei() != -1) {
        label = textItems.get(whichText.getValuei()).text;
      }
    }

    if (textImage != null) {
      textImage.dispose();
    }

    if (pg != null) {
      if (font != null) {
        pg.textFont(font);
      } else {
        pg.textSize(fontSize);
      }
    }
    logger.info("Creating text image with font: " + FontUtil.names()[fontKnob.getValuei()]);
    textImage = RainbowStudio.pApplet.createGraphics(ceil(pg.textWidth(label)), pg.height);
    //textImage.noSmooth();
    textImage.beginDraw();
    textImage.background(0, 0);

    if (!multiply.isOn()) {
      // Pick a random index for the entire text string.
      int rgb = getNewRGB(-1);
      textImage.fill(rgb);
    } else {
      textImage.fill(255);
    }

    // Reset the font based on the font dropdown.
    if (font != null) {
      textImage.textFont(font);
    } else {
      textImage.textSize(fontSize);
    }

    textImage.text(label, 0, textImage.height - textImage.textDescent());
    textImage.endDraw();
    multiplyImage = RenderImageUtil.rainbowFlagAsPGraphics(textImage.width, textImage.height);

    if (multiply.getValueb()) {
      textImage.blend(multiplyImage, 0, 0, textImage.width, textImage.height, 0, 0,
          textImage.width, textImage.height, RainbowStudio.pApplet.MULTIPLY);
    }
    textImage.loadPixels();
    /*
    if (fadeIn)
      sinceFadeStartMs += deltaDrawMs;
    else if (fadeOut)
      sinceFadeStartMs -= deltaDrawMs;
    float alpha = 1.0f;
    if (fadeIn || fadeOut)
      alpha = (float)(sinceFadeStartMs / fadeTime.getValue());
    if (alpha >= 1.0) {
      sinceFadeStartMs = 0.0;
      sinceFullAlpha = deltaDrawMs;
    }
    if (sinceFullAlpha > fullAlphaMs) {
      fadeOut = true;
      sinceFadeStartMs = fadeTime.getValue();
    }
    */
    for (int i = 0; i < textImage.width * textImage.height; i++) {
      if (textImage.pixels[i] == 0xFF000000)
        textImage.pixels[i] = 0x00000000;
      else {
        /*
        // apply alpha fade.
        LXColor.rgba(LXColor.red(textImage.pixels[i]),
            LXColor.green(textImage.pixels[i]),
                LXColor.blue(textImage.pixels[i]),
            (int)(alpha * 255f));
            */
      }
    }
    currentPos = clockwise.getValueb()
        ? -textImage.width - textGapPixels
        : pg.width + 1;
    lastPos = Integer.MIN_VALUE;

    doRedraw = false;
  }

  public void draw(double deltaDrawMs) {
    // If no new textupdate is available via OSC input, just return (stay empty).

    if (imageNeedsRerender)
      redrawTextBuffer();

    if (noTextUpdateAvailable && osc.getValueb()) {
      // redrawTextBuffer() might do nothing in OSC input mode.  If there is no text available,
      // it will just return.  Effectively waiting until the next frame to check for an update.
      redrawTextBuffer(); // Check for an update.
      return;
    }

    // Once we have finished with one text item, keep that final blank buffer rendering to the
    // screen until the next time our pattern is activated.  This prevents the next text item
    // from bleeding into the visuals while we are fade transitioning to the next pattern.
    if (blankUntilReactivated) {
      return;
    }

    if (!centered.getValueb()) {
      boolean offscreen =
          textImage == null
              || currentPos < -(textImage.width + textGapPixels)
              || (currentPos > pg.width && clockwise.getValueb());

      // NOTE: Don't redraw when the selection changes
      boolean needsRedraw =
          doRedraw
              || textItemList.getFocusedItem() == null
//        || !Objects.equals(textItemList.getFocusedItem(), currItem)
              || offscreen;

      if (needsRedraw) {
        if (offscreen) {
          if (textImage != null) {
            if (!textItems.isEmpty()) {
              // Increment only if we're not starting fresh
              currIndex = (currIndex + 1) % textItems.size();

              // Prevent next text item from starting while we are in a next-pattern fade
              // transition.  Otherwise, we have to disable transitions for all patterns in
              // the channel containing this AnimatedTextPP.
              if (advancePattern.isOn() || oneShot.isOn()) blankUntilReactivated = true;
              getChannel().autoCycleEnabled.setValue(autoCycleWasEnabled);
              // In order to not randomly fade out text in the middle of playing, the channel
              // scheduling code in UIModeSelector will disable fading between standard-mode
              // channels while text is playing.  In order for that to not wreak havoc, this
              // pattern will need to advance to another pattern when the entire chunk of text
              // has been displayed. This also implies that a standard-mode channel should not
              // consist of only AnimatedTextPP patterns otherwise channel switching will stall.
              if (!oneShot.isOn() && advancePattern.isOn())
                getChannel().goNext();
            }
          }
          textItemList.setFocusIndex(currIndex);
        }
        currItem = textItemList.getFocusedItem();
        currIndex = textItemList.getFocusedIndex();
        if (currIndex >= 0) {
          redrawTextBuffer();
        }
      }
    } else {
      // TODO(tracy): This is for fade-in, disabled for now
      // redrawTextBuffer(deltaDrawMs);
    }

    // Optimization to not re-render if we haven't moved far enough since last frame.
    if (textImage != null) {
      pg.background(0, 0);
      if (rbbg.isOn()) {
        pg.image(RenderImageUtil.rainbowFlagAsPGraphics(pg.width, pg.height, (int)(rbBright.getValuef()*255f)), 0, 0);
      }
      pg.image(textImage, round(currentPos), (30f - textImage.height)/2f + yAdjust.getValuef());
      //lastPos = round(currentPos);
    }
    currentPos += xSpeed.getValue() * (clockwise.getValueb() ? 1 : -1);
  }

  /**
   * Animated Text has some custom UI components that allow us to add and delete
   * strings at run time.  This is a moderately complex example of custom Pattern UI.
   */
  @Override
  public void buildDeviceUI(UI ui, final UI2dContainer device) {
    device.setContentWidth(CONTROLS_MIN_WIDTH);
    device.setLayout(UI2dContainer.Layout.VERTICAL);
    device.setPadding(0, 0, 0, 0);

    int knobWidth = 35;
    UI2dContainer knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    new UIKnob(xSpeed).addToContainer(knobsContainer);
    new UIKnob(fpsKnob).addToContainer(knobsContainer);
    new UIButton()
        .setParameter(clockwise)
        .setLabel("clock\nwise")
        .setTextOffset(0,12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);
    new UIKnob(fontSizeKnob).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(whichText).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(txtsKnob).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(yAdjust).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIButton()
            .setParameter(rbbg)
            .setLabel("rbbg")
            .setTextOffset(0, 12)
            .setWidth(24)
            .setHeight(16)
            .addToContainer(knobsContainer);
    new UIKnob(rbBright).setWidth(knobWidth).addToContainer(knobsContainer);

    knobsContainer.addToContainer(device);
    knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 35);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    new UIButton()
            .setParameter(oneShot)
            .setLabel("oneShot")
            .setTextOffset(0, 12)
            .setWidth(28)
            .setHeight(16)
            .addToContainer(knobsContainer);
    new UIButton()
            .setParameter(reset)
            .setLabel("reset")
            .setTextOffset(0, 12)
            .setWidth(24)
            .setHeight(16)
            .addToContainer(knobsContainer);
    new UIButton()
            .setParameter(advancePattern)
            .setLabel("advP")
            .setTextOffset(0, 12)
            .setWidth(24)
            .setHeight(16)
            .addToContainer(knobsContainer);
    new UIButton()
        .setParameter(multiply)
        .setLabel("mult")
        .setTextOffset(0, 12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);
    new UIButton()
        .setParameter(osc)
        .setLabel("osc")
        .setTextOffset(0, 12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);

    new UIDropMenu(0f, 0f, 170f, 20f, fontKnob).setOptions(FontUtil.names()).setDirection(UIDropMenu.Direction.UP).addToContainer(knobsContainer);

    knobsContainer.addToContainer(device);

    UI2dContainer bottomHalf = new UI2dContainer(0, 45, device.getWidth() - 30, device.getHeight() - 90);
    bottomHalf.setLayout(UI2dContainer.Layout.HORIZONTAL);
    bottomHalf.setPadding(0);

    UI2dContainer leftPanel = new UI2dContainer(0, 0, device.getWidth()/2 - 15, device.getHeight() - 45);
    leftPanel.setLayout(UI2dContainer.Layout.VERTICAL);
    leftPanel.addToContainer(bottomHalf);
    leftPanel.setPadding(0);

    UI2dContainer rightPanel = new UI2dContainer(0, 0, device.getWidth()/2 - 15, device.getHeight() - 45);
    rightPanel.setLayout(UI2dContainer.Layout.VERTICAL);
    rightPanel.addToContainer(bottomHalf);
    rightPanel.setPadding(0);

    UI2dContainer textEntryLine = new UI2dContainer(0, 0, leftPanel.getWidth(), 25);
    textEntryLine.setLayout(UI2dContainer.Layout.HORIZONTAL);

    new UITextBox(0, 0, device.getWidth() - 42, 20)
        .setParameter(textKnob)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(textEntryLine);

    textItemList =  new UIItemList.ScrollList(ui, 0, 5, leftPanel.getWidth(), 60);

    new UIButton(device.getWidth() - 5, 0, 20, 20) {
      @Override
        public void onToggle(boolean on) {
        if (on && !textKnob.getString().isEmpty()) {
          textItems.add(new TextItem(textKnob.getString()));
          textItemList.setItems(textItems);
          textKnob.setValue("");
        }
      }
    }.setLabel("+")
        .setMomentary(true)
        .addToContainer(textEntryLine);

    // Allow the text entry line to have the full width of the device UI.
    textEntryLine.addToContainer(device);
    bottomHalf.addToContainer(device);

    textItemList.setShowCheckboxes(false);
    textItemList.setItems(textItems);
    textItemList.addToContainer(leftPanel);

    UI2dContainer colorArea = new UI2dContainer(0, 0, rightPanel.getWidth(), 40);
    colorArea.setLayout(UI2dContainer.Layout.HORIZONTAL);
    colorArea.setPadding(0);
    colorArea.addToContainer(rightPanel);
    new UIKnob(paletteKnob).setWidth(knobWidth).addToContainer(colorArea);
    new UIKnob(hue).setWidth(knobWidth).addToContainer(colorArea);
    new UIKnob(saturation).setWidth(knobWidth).addToContainer(colorArea);
    new UIKnob(bright).setWidth(knobWidth).addToContainer(colorArea);
  }

  public class TextItem extends UIItemList.Item {
    private final String text;

    TextItem(String str) {
      this.text = str;
    }
    public boolean isActive() {
      return false;
    }
    public int getActiveColor(UI ui) {
      return ui.theme.getAttentionColor();
    }
    public String getLabel() {
      return text;
    }
    public void onDelete() {
      textItems.remove(this);
      textItemList.removeItem(this);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(text);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null || !(obj instanceof TextItem)) {
        return false;
      }
      return Objects.equals(this.text, ((TextItem) obj).text);
    }

    @Override
    public String toString() {
      return text;
    }
  }
}

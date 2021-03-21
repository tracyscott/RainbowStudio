package com.giantrainbow.patterns;

import com.giantrainbow.FontUtil;
import com.giantrainbow.RainbowOSC;
import com.giantrainbow.RainbowStudio;
import com.giantrainbow.ui.UITextBox2;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.*;
import heronarts.p3lx.ui.CustomDeviceUI;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.*;

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
import processing.core.PImage;

/**
 * Text effects.
 */
@LXCategory(LXCategory.FORM)
public class TextFx extends PGPixelPerfect implements CustomDeviceUI {
  private static final Logger logger = Logger.getLogger(TextFx.class.getName());

  private static final int CONTROLS_MIN_WIDTH = 320;

  public final BooleanParameter osc = new BooleanParameter("osc", false)
      .setDescription("Wait to receive text via OSC");
  public final BooleanParameter multiply = new BooleanParameter("multi", false)
      .setDescription("Multiply text times Rainbow Flag colors");
  public final BooleanParameter leftToRight = new BooleanParameter("LtR", true)
      .setDescription("Is language read left to right?");
  public final BooleanParameter oneShot = new BooleanParameter("oneShot", false)
      .setDescription("Animation will play once and hold");
  public final BooleanParameter reset = new BooleanParameter("reset", false)
      .setDescription("Resets the animation");
  public final BooleanParameter advancePattern = new BooleanParameter("advP", true)
      .setDescription("Advances to next pattern in channel when animation is finished");
  public final DiscreteParameter fontKnob = new DiscreteParameter("font", 0, FontUtil.names().length);
  public final DiscreteParameter fontSizeKnob = new DiscreteParameter("fontsize", 24, 6, 32);
  public final DiscreteParameter fontHtOffset = new DiscreteParameter("htOff", 0, -20, 20)
      .setDescription("Font height offset for misbehaving fonts");
  public final DiscreteParameter spriteAdj = new DiscreteParameter("sprSzAdj", 0, -10, 20)
      .setDescription("Manual sprite size adjust to handle clipping fonts");
  public final StringParameter textKnob = new StringParameter("str", "");
  public final CompoundParameter xSpeed =
      new CompoundParameter("XSpd", 20, 0, 100).setDescription("X speed in pixels per frame");
  public CompoundParameter blurKnob = new CompoundParameter("blur", 0f, 0.0, 255f);
  public final DiscreteParameter txtsKnob = new DiscreteParameter("txts", 0, 0, 41)
      .setDescription("Which TextFx#.txts file to use for text input");
  public final CompoundParameter yAdj =
      new CompoundParameter("yAdj", 0, -40, 40)
      .setDescription("Y offset adjust for rendering");

  // TODO(tracy): Need some way to render text with newlines.
  String[] defaultTexts = {
      "THANK YOU\nLIVERPOOL",
      "Your Ad Here!\nCall 415-793-8032\nAsk for Sri!",
      "We only matter at all in so far as\nwe matter to each other", "A", "I"
  };
  public final DiscreteParameter whichText = new DiscreteParameter("which", -1, -1, 1000)
      .setDescription("Manual text item override. Always display this one");
  List<TextItem> textItems = new ArrayList<>();
  UIItemList.ScrollList textItemList;
  UITextBox2 multiLineText;

  private PImage[] images;
  private int currentPos;
  boolean noTextUpdateAvailable = true;
  volatile boolean needRerender = true;
  boolean blankUntilReactivated = false;
  int lastPos = 0;
  boolean autoCycleWasEnabled = false;
  int currIndex;
  UIItemList.Item currItem;
  // How long to hold the animation when it is done before a reset.
  float targetHoldTime = 5.0f;
  float curHoldTime = 0.0f;
  boolean needTextsReload = true;

  int textGapPixels = 10;
  PFont font;
  List<CharSprite> chSprites = new ArrayList<CharSprite>();
  protected TextAnimDetails taDetails = new TextAnimDetails();


  public TextFx(LX lx) {
    super(lx, "");
    addParameter(textKnob);
    addParameter(xSpeed);
    addParameter(multiply);
    addParameter(leftToRight);
    addParameter(oneShot);
    addParameter(reset);
    addParameter(advancePattern);
    addParameter(fontKnob);
    addParameter(osc);
    addParameter(fontSizeKnob);
    addParameter(fontHtOffset);
    addParameter(spriteAdj);
    addParameter(whichText);
    addParameter(blurKnob);
    addParameter(txtsKnob);
    addParameter(yAdj);
    addParameter(paletteKnob);
    addParameter(hue);
    addParameter(bright);
    addParameter(saturation);
    randomPaletteKnob.setValue(false);

    // When we change which lists of texts, set a flag so that the renderer knows that it
    // needs to reload the text file.  This will happen on the next needsRerender request
    // which should happen after the current animation is done.
    txtsKnob.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) {
        needTextsReload = true;
      }
    });
    reset.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) {
        blankUntilReactivated = false;
        // TODO(tracy): Change this to loadNewTextItem() to properly
        // account for OSC updates.
        currItem = textItemList.getFocusedItem();
        renderCharacters();
        BooleanParameter b = (BooleanParameter)p;
        if (b.isOn()) {
          b.setValue(false);
        }
      }
    });

    logger.info("listing fonts");
    String[] fontNames = PFont.list();
    for (String fontName : fontNames) {
      logger.info("Font: " + fontName);
    }

    font = FontUtil.getCachedFont("PressStart2P", 24);

    fontKnob.addListener(new LXParameterListener () {
      @Override
      public void onParameterChanged(LXParameter p) {
        font = FontUtil.getCachedFont(FontUtil.names()[fontKnob.getValuei()], fontSizeKnob.getValuei());
        needRerender = true;
      }
    });

    fontSizeKnob.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) {
        font = FontUtil.getCachedFont(FontUtil.names()[fontKnob.getValuei()], fontSizeKnob.getValuei());
        needRerender = true;
      }
    });

    whichText.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) { needRerender = true; }
    });

    fontHtOffset.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) { needRerender = true; }
    });

    spriteAdj.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) { needRerender = true; }
    });



    for (int i = 0; i < defaultTexts.length; i++) {
      textItems.add(new TextItem(defaultTexts[i]));
    }
    currIndex = -1;
    if (textItems.size() > 0) {
      currIndex = 0;
    }
  }

  @Override
  protected void setup() {
    // Set the font here so that we can use it for sizing
    if (font == null) {
      pg.textSize(fontSizeKnob.getValuei());
    } else {
      pg.textFont(font);
    }
    needRerender = true;

    if (textItemList.getItems().size() > 0) {
      if (currIndex < 0) {
        currIndex = 0;
      }
      textItemList.setFocusIndex(currIndex);
      currItem = textItemList.getFocusedItem();
    }
  }

  @Override
  public void onActive() {
    // Reset the guard that prevents the next text item from starting to show
    // while we are performing our fade transition to the next pattern.
    blankUntilReactivated = false;
    autoCycleWasEnabled = getChannel().autoCycleEnabled.getValueb();
    getChannel().autoCycleEnabled.setValue(false);
  }


  /**
   * Redraws the text image.  After calling this method, an image of the text will exist in the textImage
   * member variable.
   */
  public void renderCharacters() {

    // If needTextsReload, reload the file containing the list of texts to display.  We also should reset currIndex=0
    if (needTextsReload) {
      currIndex = 0;
      try {
          String contents = new String(Files.readAllBytes(Paths.get("TextFx" + txtsKnob.getValuei() + ".txts")));
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
    String label;

    if (osc.getValueb()) {
      label = RainbowOSC.getTextUpdateMessage();
      if (label == null) {
        // If there is still no text update available via OSC, just return from this method.
        noTextUpdateAvailable = true;
        return;
      } else {
        noTextUpdateAvailable = false;
      }
      logger.info("received text update=" + label);
    } else {
      TextItem item = (TextItem) textItemList.getFocusedItem();
      if (item == null) {
        return;
      }
      label = item.text;
      // Allow for whichText to override auto-iteration over the list of texts.  Each text item
      // will be displayed by independent patterns in the channel.
      if (whichText.getValuei() != -1) {
        label = textItems.get(whichText.getValuei()).text;
      }
    }

    if (pg != null) {
      if (font != null) {
        // logger.info("Setting font: " + font.getName());
        pg.textFont(font);
      } else {
        pg.textSize(fontSizeKnob.getValuei());
      }
    }

    taDetails = new TextAnimDetails();
    taDetails.fullText = label;
    int paddingForRotate = 0;
    List<CharSprite> oldSprites = chSprites;
    chSprites = new ArrayList<CharSprite>();
    int curPos = 0;
    int lineNumber = 0;
    float xOffset = 0f; //80f;
    List<CharSprite> spritesThisLine = new ArrayList<CharSprite>();
    taDetails.spritesPerLine.add(spritesThisLine);

    for (int i = 0; i < label.length(); i++) {
      CharSprite chSprite = new CharSprite();
      chSprite.ch = "" + label.charAt(i);
      if ("\n".equals(chSprite.ch)) {
        spritesThisLine = new ArrayList<CharSprite>();
        taDetails.spritesPerLine.add(spritesThisLine);
        taDetails.lineWidths.add(curPos);
        ++lineNumber;
        curPos = 0;
        continue;
      }

      float chWidth = pg.textWidth(chSprite.ch);
      if (lineNumber == 1)
        xOffset = 0f; //130f;
      // Initial targetPosX is just 0 + character offset, i.e. relative to the start of the line.
      // Down below we will adjust it for centering it on
      // the line.  We also wait to initialize Y values until we know how many lines of text we have.
      chSprite.targetPosX = curPos + xOffset; //i * perChWidth + 80f;
      curPos += chWidth;
      chSprite.scale = 1f;
      int chWidthNoCrash = 8;
      if (chWidth == 0) {
        chWidth = chWidthNoCrash;
      }

      chSprite.chImage = RainbowStudio.pApplet.createGraphics((int)chWidth, fontSizeKnob.getValuei() + spriteAdj.getValuei());
      // chSprite.chImage.noSmooth();
      chSprite.chImage.beginDraw();
      chSprite.chImage.background(0, 0);

      if (!multiply.isOn()) {
        int rgb = getNewRGB(i);
        chSprite.chImage.fill(rgb);
      } else {
        chSprite.chImage.fill(255);
      }
      if (font != null) {
        chSprite.chImage.textFont(font);
      } else {
        chSprite.chImage.textSize(fontSizeKnob.getValuei());
      }
      chSprite.chImage.text(chSprite.ch, 0, chSprite.chImage.height - chSprite.chImage.textDescent());
      chSprite.chImage.endDraw();

      if (multiply.isOn()) {
        PImage multiplyImage = RenderImageUtil.rainbowFlagAsPGraphics(chSprite.chImage.width, chSprite.chImage.height);
        chSprite.chImage.blend(multiplyImage, 0, 0, chSprite.chImage.width, chSprite.chImage.height, 0, 0,
            chSprite.chImage.width, chSprite.chImage.height, RainbowStudio.pApplet.MULTIPLY);

        chSprite.chImage.loadPixels();
        // This is probably pretty inefficient but blend() above is not respecting transparency as I would expect.
        for (int i2 = 0; i2 < chSprite.chImage.width * chSprite.chImage.height; i2++) {
          if (chSprite.chImage.pixels[i2] == 0xFF000000)
            chSprite.chImage.pixels[i2] = 0x00000000;
        }
      }

      chSprites.add(chSprite);
      spritesThisLine.add(chSprite);
    }
    // Add the width of the final line.
    taDetails.lineWidths.add(curPos);

    // By default, our target positions are just centered horizontally and vertically based on the number of
    // lines of text.  We also initialize the
    float totalYHeight = taDetails.spritesPerLine.size() *
        (fontSizeKnob.getValuei() + fontHtOffset.getValuei());
    // Account for spacing between lines.
    totalYHeight += ((taDetails.spritesPerLine.size() - 1) * 2);
    float yTopMargin = (pg.height - totalYHeight)/2f;
    for (int j = 0; j < taDetails.spritesPerLine.size(); j++) {
      List<CharSprite> thisLineSprites = taDetails.spritesPerLine.get(j);
      for (int i = 0; i < thisLineSprites.size(); i++) {
        CharSprite ch = thisLineSprites.get(i);
        ch.targetPosX += (420 - taDetails.lineWidths.get(j)) / 2;
        ch.curPosX = ch.targetPosX;
        ch.targetPosY = j * (fontSizeKnob.getValuei() + fontHtOffset.getValuei() + 2) + yTopMargin;
        ch.curPosY = ch.targetPosY;
      }
    }

    for (int i = 0; i < oldSprites.size(); i++) {
      CharSprite old = oldSprites.get(i);
      old.chImage.dispose();
    }
    // Dump the line widths for debugging purposes.
    for (int i = 0; i < taDetails.lineWidths.size(); i++) {
      // logger.info("line width " + i + " =" + taDetails.lineWidths.get(i));
    }
    // Set the characters to their starting positions.
    resetAnimation();
    needRerender = false;
  }

  /**
   * Resets the animation to initial conditions.
   */
  protected void resetAnimation() {
    for (int j = 0; j < taDetails.spritesPerLine.size(); j++) {
      List<CharSprite> thisLineSprites = taDetails.spritesPerLine.get(j);
      for (int i = 0; i < thisLineSprites.size(); i++) {
        CharSprite chSprite = thisLineSprites.get(i);
        // Put the character off the right side of the Rainbow.
        chSprite.curPosX = 430 + chSprite.targetPosX + j * 300;
      }
    }
  }

  /**
   * Renders the characters for this frame.
   *
   * @param deltaMs Time since last frame.
   * @return True if the animation is finished and we should start holdTime countdown.
   */
  public boolean drawCharacters(double deltaMs) {
    boolean areChDone = true;
    //pg.noSmooth(); // TODO(tracy): Decide what to do here.
    for (int j = 0; j < taDetails.spritesPerLine.size(); j++) {
      List<CharSprite> thisLineSprites = taDetails.spritesPerLine.get(j);
      for (int i = 0; i < thisLineSprites.size(); i++) {
        CharSprite ch = thisLineSprites.get(i);
        pg.pushMatrix();
        pg.imageMode(PConstants.CENTER);
        // NOTE(tracy): There needs to be some lineheight override since some fonts vary. For example, 3Dventure
        // has too much vertical space for a given font size.  Specify as a 0 to 1 multiplier.
        ch.curPosX -= xSpeed.getValuef()/10f;
        //logger.info("curPosX = " + ch.curPosX);
        //ch.curPosY += xSpeed.getValuef()/10f;
        ch.angle = (int)(currentFrame) % 360;
        // If we are close to our target X position, lock it in.
        if (Math.abs(ch.curPosX - ch.targetPosX) < 4f) {
          ch.curPosX = ch.targetPosX;
          ch.angle = 0;
        }
        // If we are close to our target Y position, lock it in.
        if (Math.abs(ch.curPosY - ch.targetPosY) < 4f)
          ch.curPosY = ch.targetPosY;
        if (ch.curPosX != ch.targetPosX || ch.curPosY != ch.targetPosY)
          areChDone = false;

        pg.translate(ch.curPosX + ch.chImage.width / 2, (int)(ch.chImage.height / 2f + ch.curPosY + yAdj.getValuef()));

        if (Math.abs(ch.angle) > 1f) pg.rotate(ch.angle);
        //pg.blend(ch.chImage, 0, 0, ch.chImage.width, ch.chImage.height, 0, 0, pg.width, pg.height, PConstants.ADD);
        pg.image(ch.chImage, 0, 0, ch.chImage.width, ch.chImage.height);
        pg.popMatrix();
      }
    }
    return areChDone;
  }

  public void draw(double deltaMs) {
    if (needRerender)
      renderCharacters();

    /*
    pg.colorMode(PConstants.HSB, 1.0f, 1.0f, 1.0f, 255.0f);
    pg.fill(0, 255 - (int)blurKnob.getValuef());
    pg.rect(0, 0, pg.width, pg.height);
    pg.fill(255);
    */
    pg.background(0, 0);

    boolean areChDone = drawCharacters(deltaMs);

    if (areChDone) {
      curHoldTime += deltaMs/1000f;
      if (curHoldTime >= targetHoldTime) {
        // Animation is done.  Advance pattern, reset animation and play again, or hold indefinitely if 'one-shot'
        // is enabled.
        if (!oneShot.getValueb()) {
          if (!textItems.isEmpty()) {
            // Increment only if we're not starting fresh
            currIndex = (currIndex + 1) % textItems.size();
            textItemList.setFocusIndex(currIndex);
            needRerender = true;
          }
          if (advancePattern.getValueb()) {
            // advance pattern
            getChannel().autoCycleEnabled.setValue(autoCycleWasEnabled);
            getChannel().goNext();
          } else {
            resetAnimation();
          }
        }
        curHoldTime = 0.0f;
      }
    } else {
      curHoldTime = 0.0f;
    }
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
    new UIKnob(xSpeed).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(fpsKnob).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(blurKnob).setWidth(knobWidth).addToContainer(knobsContainer);

    // When we change the font size, we need to reload the 'font' object with the new size setting.
    new UIKnob(fontSizeKnob).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(fontHtOffset).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(spriteAdj).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(whichText).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(txtsKnob).setWidth(knobWidth).addToContainer(knobsContainer);
    new UIKnob(yAdj).setWidth(knobWidth).addToContainer(knobsContainer);

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
        .setParameter(osc)
        .setLabel("osc")
        .setTextOffset(0, 12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);
    new UIButton()
        .setParameter(multiply)
        .setLabel("multi")
        .setTextOffset(0,12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);
    new UIButton()
        .setParameter(leftToRight)
        .setLabel("LtoR")
        .setTextOffset(0, 12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);

    knobsContainer.addToContainer(device);


    UI2dContainer bottomHalf = new UI2dContainer(0, 45, device.getWidth() - 30, device.getHeight() - 90);
    bottomHalf.setLayout(UI2dContainer.Layout.HORIZONTAL);
    bottomHalf.addToContainer(device);
    bottomHalf.setPadding(0);

    UI2dContainer leftPanel = new UI2dContainer(0, 0, device.getWidth()/2 - 15, device.getHeight() - 45);
    leftPanel.setLayout(UI2dContainer.Layout.VERTICAL);
    leftPanel.addToContainer(bottomHalf);
    leftPanel.setPadding(0);

    UI2dContainer rightPanel = new UI2dContainer(0, 0, device.getWidth()/2 - 15, device.getHeight() - 45);
    rightPanel.setLayout(UI2dContainer.Layout.VERTICAL);
    rightPanel.addToContainer(bottomHalf);
    rightPanel.setPadding(0);

    new UIDropMenu(0f, 0f, leftPanel.getWidth(), 20f, fontKnob)
        .setOptions(FontUtil.names()).setDirection(UIDropMenu.Direction.UP).addToContainer(knobsContainer);

    UI2dContainer textEntryLine = new UI2dContainer(0, 0, leftPanel.getWidth(), 25);
    textEntryLine.setLayout(UI2dContainer.Layout.HORIZONTAL);

    new UITextBox(0, 0, leftPanel.getWidth() - 22, 20)
        .setParameter(textKnob)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(textEntryLine);

    textItemList =  new UIItemList.ScrollList(ui, 0, 5, leftPanel.getWidth(), 60);

    new UIButton(leftPanel.getWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on && !textKnob.getString().isEmpty()) {
          textItems.add(new TextItem(textKnob.getString()));
          textItemList.setItems(textItems);
          textKnob.setValue("");
          whichText.setRange(-1, textItems.size());
        }
      }
    }.setLabel("+")
        .setMomentary(true)
        .addToContainer(textEntryLine);

    textEntryLine.addToContainer(leftPanel);

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

    UI2dContainer multiLineArea = new UI2dContainer(0,0, rightPanel.getWidth(), bottomHalf.getHeight() - 40);
    multiLineArea.setLayout(UI2dContainer.Layout.HORIZONTAL);
    multiLineArea.setPadding(0);
    multiLineArea.addToContainer(rightPanel);
    multiLineText = (UITextBox2) new UITextBox2(0, 0, rightPanel.getWidth() - 22, bottomHalf.getHeight() - 40)
        //.setParameter(textKnob)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(multiLineArea);
    new UIButton(rightPanel.getWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        // TODO(tracy): We should track which TextItem we came from and then just edit that one?
        if (on && !multiLineText.getEditBuffer().isEmpty()) {
          textItems.add(new TextItem(multiLineText.getEditBuffer()));
          textItemList.setItems(textItems);
          textKnob.setValue("");
          whichText.setRange(-1, textItems.size());
        }
      }
    }.setLabel("+")
        .setMomentary(true)
        .addToContainer(multiLineArea);
  }

  public String getPlatformIndependentFontName(String fname) {
    if (RainbowStudio.pApplet.platform == PConstants.MACOSX) {
      //04b 30", "Press Start Regular"
      if (fname.equals("04b 30")) return "04b";
      if (fname.equals("Press Start Regular")) return "PressStart2P";
    }
    return fname;
  }

  public class TextItem extends UIItemList.Item {
    public final String text;

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
      return text.split("\n")[0];
    }
    public void onDelete() {
      textItems.remove(this);
      textItemList.removeItem(this);
      whichText.setRange(-1, textItems.size());
    }


    public void onActivate() {
      // Need to bind this item to the UITextBox2
      logger.info("Activated: " + text);
      needRerender = true;
      currIndex = textItemList.getItems().indexOf(this);
      logger.info("currIndex=" + currIndex);
    }


    public void onFocus() {
      multiLineText.setValue(text);
      multiLineText.setEditBuffer(text);
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

  /**
   * Holds one character image.  For text effects, we will render each character into it's own image and
   * also be able to track the current rotation angle and targetPosX for the character.
   */
  static public class CharSprite {
    public String ch;
    public float angle;
    public float targetPosX;
    public float targetPosY;
    public float curPosX;
    public float curPosY;
    public float scale;
    public PGraphics chImage;
  }

  /**
   * This is a wrapper class for various text animation details.  The goal will be for a particular text animation
   * to fill this in as needed.  The text animator will just receive the fullText, the full list of sprites, and
   * a list of words and the computed width of each word.  It will be up to the particular text animation class
   * to break the text up into multiple lines if necessary.  For example some text animations might want to vertically
   * scroll text like Star Wars, etc.  There should also be some support for detecting embedded newlines and
   * using them to create additional lines of text. Although for large amounts of text we may need to be smart
   * about memory use and free unneeded character glyphs and create soon to be needed ones on demand.
   */
  static public class TextAnimDetails {
    String fullText;
    // These are the computed widths of each line.  They can be computed from the list of sprites for each line.
    // These can be used for things like centering the text on the rainbow.
    List<Integer> lineWidths = new ArrayList<Integer>();
    // All character sprites.
    List<CharSprite> chSprites = new ArrayList<CharSprite>();
    // For each line, all the sprites for that line.  These will include the space characters.
    List<List<CharSprite>> spritesPerLine = new ArrayList<List<CharSprite>>();
    // Text broken up into words.  This is meant for computing the auto wrapping of text.
    List<String> words = new ArrayList<String>();
    // For each word, the computed width.
    List<Integer> wordWidths = new ArrayList<Integer>();
  }
}

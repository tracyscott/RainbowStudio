package com.giantrainbow.patterns;

import static processing.core.PApplet.ceil;
import static processing.core.PApplet.round;

import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.CustomDeviceUI;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UITextBox;
import java.util.ArrayList;
import java.util.List;
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
  private static final int CONTROLS_MIN_WIDTH = 120;
  public final CompoundParameter xSpeed =
      new CompoundParameter("XSpd", 0, 20).setDescription("X speed in pixels per frame");
  public final BooleanParameter clockwise = new BooleanParameter("clockwise", false);

  PGraphics textImage;
  volatile boolean doRedraw;
  float currentPos = 0.0f;
  int lastPos = 0;
  String[] defaultTexts = {
    "City of orgies, walks and joys,      City whom that I have lived and sung in your midst will one day make      Not the pageants of you, not your shifting tableaus, your spectacles, repay me,      Not the interminable rows of your houses, nor the ships at the wharves,      Nor the processions in the streets, nor the bright windows with goods in them,      Nor to converse with learn'd persons, or bear my share in the soiree or feast;      Not those, but as I pass O Manhattan, your frequent and swift flash of eyes offering me love,      Offering response to my own—these repay me,      Lovers, continual lovers, only repay me.",
    "What's up?",
    "Hello!",
  };

  int currentStringIndex;
  String currentString;

  int textGapPixels = 10;
  PFont font;
  int fontSize = pg.height;

  public AnimatedTextPP(LX lx) {
    super(lx, "");
    addParameter(textKnob);
    addParameter(xSpeed);
    addParameter(clockwise);
    String[] fontNames = PFont.list();
    for (String fontName : fontNames) {
      logger.fine("Font: " + fontName);
    }
    font = RainbowStudio.pApplet.createFont("04b", fontSize, true);
    for (int i = 0; i < defaultTexts.length; i++) {
      textItems.add(new TextItem(defaultTexts[i]));
    }
    xSpeed.setValue(5);
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
      textItemList.setFocusIndex(0);
    }
    currentStringIndex = -1;
  }

  @Override
  protected void tearDown() {
    if (textImage != null) {
      textImage.dispose();
      textImage = null;
    }
  }

  public void redrawTextBuffer() {
    UIItemList.Item item = textItemList.getFocusedItem();
    if (item == null) {
      return;
    }
    currentString = item.getLabel();

    // Find which is the focused index
    List<? extends UIItemList.Item> items = textItemList.getItems();
    currentStringIndex = -1;
    for (int i = 0; i < items.size(); i++) {
      if (currentString.equals(items.get(i).getLabel())) {
        currentStringIndex = i;
        break;
      }
    }

    if (textImage != null) {
      textImage.dispose();
    }
    textImage = RainbowStudio.pApplet.createGraphics(ceil(pg.textWidth(currentString)), pg.height);
    textImage.noSmooth();
    textImage.beginDraw();
    textImage.background(0);
    textImage.stroke(255);
    if (font != null) {
      textImage.textFont(font);
    } else {
      textImage.textSize(fontSize);
    }

    textImage.text(currentString, 0, textImage.height - textImage.textDescent());
    textImage.endDraw();

    currentPos = clockwise.getValueb()
        ? -textImage.width + textGapPixels
        : pg.width + 1;
    lastPos = Integer.MIN_VALUE;

    doRedraw = false;
  }

  public void draw(double deltaDrawMs) {
    boolean offscreen =
        textImage == null
        || currentPos < -(textImage.width + textGapPixels)
        || (currentPos > pg.width && clockwise.getValueb());

    boolean needsRedraw =
        doRedraw
        || textItemList.getFocusedItem() == null
        || !textItemList.getFocusedItem().getLabel().equals(currentString)
        || offscreen;

    if (needsRedraw) {
      if (offscreen) {
        currentStringIndex++;
        if (currentStringIndex >= textItems.size()) {
          currentStringIndex = 0;
        }
        textItemList.setFocusIndex(currentStringIndex);
      }
      redrawTextBuffer();
    }

    // Optimization to not re-render if we haven't moved far enough since last frame.
    if (textImage != null && round(currentPos) != lastPos) {
      pg.background(0);
      pg.image(textImage, round(currentPos), 0);
      lastPos = round(currentPos);
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
    device.setPadding(3, 3, 3, 3);

    UI2dContainer knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(3, 3, 3, 3);
    new UIKnob(xSpeed).addToContainer(knobsContainer);
    new UIKnob(fpsKnob).addToContainer(knobsContainer);
    new UIButton()
        .setParameter(clockwise)
        .setLabel("clock\nwise")
        .setTextOffset(0,12)
        .setWidth(24)
        .setHeight(16)
        .addToContainer(knobsContainer);
    knobsContainer.addToContainer(device);

    UI2dContainer textEntryLine = new UI2dContainer(0, 0, device.getWidth(), 30);
    textEntryLine.setLayout(UI2dContainer.Layout.HORIZONTAL);

    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
        .setParameter(textKnob)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(textEntryLine);

    textItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 80);

    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
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

    textEntryLine.addToContainer(device);

    textItemList.setShowCheckboxes(false);
    textItemList.setItems(textItems);
    textItemList.addToContainer(device);
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
  }
}

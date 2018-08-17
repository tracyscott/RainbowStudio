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
  private static final int CONTROLS_MIN_WIDTH = 120;
  public final CompoundParameter xSpeed =
      new CompoundParameter("XSpd", 0, 20).setDescription("X speed in pixels per frame");
  public final BooleanParameter clockwise = new BooleanParameter("clockwise", false);

  PGraphics textImage;
  volatile boolean doRedraw;
  boolean blankUntilReactivated = false;
  float currentPos = 0.0f;
  int lastPos = 0;
  // TODO: Change the defaultTexts to Larry Harvey quotes.
  String[] defaultTexts = {
    //"City of orgies, walks and joys,      City whom that I have lived and sung in your midst will one day make      Not the pageants of you, not your shifting tableaus, your spectacles, repay me,      Not the interminable rows of your houses, nor the ships at the wharves,      Nor the processions in the streets, nor the bright windows with goods in them,      Nor to converse with learn'd persons, or bear my share in the soiree or feast;      Not those, but as I pass O Manhattan, your frequent and swift flash of eyes offering me love,      Offering response to my own—these repay me,      Lovers, continual lovers, only repay me.",
    "What's up?",
    "Hello!",
  };

  int currIndex;
  UIItemList.Item currItem;

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

    currIndex = -1;
    if (textItems.size() > 0) {
      currIndex = 0;
    }
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
    blankUntilReactivated = false;
  }

  public void redrawTextBuffer() {
    UIItemList.Item item = textItemList.getFocusedItem();
    if (item == null) {
      return;
    }
    String label = item.getLabel();

    if (textImage != null) {
      textImage.dispose();
    }
    textImage = RainbowStudio.pApplet.createGraphics(ceil(pg.textWidth(label)), pg.height);
    textImage.noSmooth();
    textImage.beginDraw();
    textImage.background(0);
    textImage.stroke(255);
    if (font != null) {
      textImage.textFont(font);
    } else {
      textImage.textSize(fontSize);
    }

    textImage.text(label, 0, textImage.height - textImage.textDescent());
    textImage.endDraw();

    currentPos = clockwise.getValueb()
        ? -textImage.width + textGapPixels
        : pg.width + 1;
    lastPos = Integer.MIN_VALUE;

    doRedraw = false;
  }

  public void draw(double deltaDrawMs) {
    // Once we have finished with one text item, keep that final blank buffer rendering to the
    // screen until the next time our pattern is activated.  This prevents the next text item
    // from bleeding into the visuals while we are fade transitioning to the next pattern.
    if (blankUntilReactivated) {
      return;
    }

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
            currIndex = (currIndex + 1)%textItems.size();
            // In order to not randomly fade out text in the middle of playing, the channel
            // scheduling code in UIModeSelector will disable fading between standard-mode
            // channels while text is playing.  In order for that to not wreak havoc, this
            // pattern will need to advance to another pattern when the entire chunk of text
            // has been displayed. This also implies that a standard-mode channel should not
            // consist of only AnimatedTextPP patterns otherwise channel switching will stall.
            getChannel().goNext();
            // Prevent next text item from starting while we are in a next-pattern fade
            // transition.  Otherwise, we have to disable transitions for all patterns in
            // the channel containing this AnimatedTextPP.
            blankUntilReactivated = true;
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

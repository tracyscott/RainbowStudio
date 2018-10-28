package com.giantrainbow.patterns;

import static processing.core.PApplet.ceil;
import static processing.core.PApplet.round;

import com.giantrainbow.RainbowStudio;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.*;
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
  private static final int CONTROLS_MIN_WIDTH = 140;
  public final CompoundParameter xSpeed =
      new CompoundParameter("XSpd", 0, 20).setDescription("X speed in pixels per frame");
  public final BooleanParameter clockwise = new BooleanParameter("clockwise", false);
  public final BooleanParameter oneShot = new BooleanParameter("oneShot", false);
  public final BooleanParameter reset = new BooleanParameter("reset", false);
  public final BooleanParameter advancePattern = new BooleanParameter("advP", true);


  PGraphics textImage;
  volatile boolean doRedraw;
  boolean blankUntilReactivated = false;
  float currentPos = 0.0f;
  int lastPos = 0;
  boolean autoCycleWasEnabled = false;

  // TODO: Change the defaultTexts to Larry Harvey quotes.
  String[] defaultTexts = {
    //"City of orgies, walks and joys,      City whom that I have lived and sung in your midst will one day make      Not the pageants of you, not your shifting tableaus, your spectacles, repay me,      Not the interminable rows of your houses, nor the ships at the wharves,      Nor the processions in the streets, nor the bright windows with goods in them,      Nor to converse with learn'd persons, or bear my share in the soiree or feast;      Not those, but as I pass O Manhattan, your frequent and swift flash of eyes offering me love,      Offering response to my own—these repay me,      Lovers, continual lovers, only repay me.",
    "It avoids a self-conscious relationship to the act. We live in the most self-conscious society in the history of mankind. There are good things in that, but there are also terrible things. The worst of it is, that we find it hard to give ourselves to the cultural process.",
      "If all of your self worth and esteem is invested in how much you consume, how many likes you get, or other quantifiable measures, the desire to simply possess things trumps our ability or capability to make moral connections with people around us.",
      "I've learned never to expect people to be better than they are, but to always have faith that they can be more.",
      "Black Rock gives us all a chance to heal, to become ourselves.",
      "Well it seems to me, that all real communities grow out of a shared confrontation with survival. Communities are not produced by sentiment or mere goodwill. They grow out of a shared struggle. Our situation in the desert is an incubator for community.",
      "Burning Man is like a big family picnic. Would you sell things to one another at a family picnic? No, you'd share things.",
      "We take people to the threshold of religion. Our aim is to induce immediate experience that is beyond the odd, beyond the strange, and beyond the weird. It verges on the wholly other.",
      "We've been civilized from the beginning. In the desert, it's a baroque city like Paris or Rome.",
      "People give because they identify with Burning Man, with our city, with our civic life. The idea of giving something to the citizens of Black Rock City has enormous appeal to them because it enhances their sense of who they are and magnifies their sense of being. That's a spiritual reward.",
      "I'll believe in utopia when I meet my first perfect person, and this community is made up of 70,000 imperfect persons.",
      "I grew up on a farm in Oregon, an adopted child, with one sibling, and parents the age of all my peers' grandparents. We lived in isolation from the people around us, and it was always a struggle to cope with as a child. The heart can really expire under those conditions. I always felt like I was looking at the world from the outside.",
      "Belief is thought at rest.",
      "We see culture as a self-organising thing.",
      "People out here build whole worlds out of nothing, through cooperating.",
      "The essence of the desert is that you are free to create your own world, your own visionary reality. … Both Burning Man and the Internet make it possible to regather the tribe of mankind.",
      "What we have to do is make progress in the quality of connection between people, not the quantity of consumption.",
      "What counts is the connection, not the commodity.",
      "I elevated passions into duties (p.s. that's not enough...)",
      "Instead of doing art about the state of society, we do art that creates society around it.",
      "So when they say we’re a cult, we reply that it’s a self-service cult. You wash your own brain.",
  };

  int currIndex;
  UIItemList.Item currItem;

  int textGapPixels = 10;
  PFont font;
  int fontSize = pg.height;

  public AnimatedTextPP(LX lx) {
    super(lx, "");
    reset.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter p) {
        blankUntilReactivated = false;
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
    autoCycleWasEnabled = getChannel().autoCycleEnabled.getValueb();
    getChannel().autoCycleEnabled.setValue(false);
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
            if (!oneShot.isOn() && advancePattern.isOn())
              getChannel().goNext();
            // Prevent next text item from starting while we are in a next-pattern fade
            // transition.  Otherwise, we have to disable transitions for all patterns in
            // the channel containing this AnimatedTextPP.
            if (advancePattern.isOn() || oneShot.isOn()) blankUntilReactivated = true;
            getChannel().autoCycleEnabled.setValue(autoCycleWasEnabled);
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
    knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 35);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(3, 3, 3, 3);
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
    knobsContainer.addToContainer(device);
    UI2dContainer textEntryLine = new UI2dContainer(0, 0, device.getWidth(), 25);
    textEntryLine.setLayout(UI2dContainer.Layout.HORIZONTAL);

    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
        .setParameter(textKnob)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(textEntryLine);

    textItemList =  new UIItemList.ScrollList(ui, 0, 5, CONTROLS_MIN_WIDTH, 60);

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

package com.giantrainbow;

import processing.core.PConstants;
import processing.core.PFont;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling fonts.  Now that there are multiple Text rendering patterns, we should have some
 * centralized location to keep track of which fonts we expect to be installed.  Also, we should cache the
 * font creation process since it is expensive and can be shared among patterns.
 *
 * TODO(tracy): It would probably be a good idea to garbage collect unused fonts.  Maybe expire them?
 */
public class FontUtil {

  static public final String[] fontNames = {
      "04b 30",
      "Press Start Regular",
      "NotoSansSC-Regular",
      "Noto Sans Arabic Regular",
      "3Dventure",
      "FTBlockbusta",
      "Lunch",
      "Messages",
      "Verdana",
      "AvantGarde-Medium",
      "AvantGarde-Bold",
      "Noto Sans SC",
      "Noto Sans Malayalam",
      "Noto Nastaliq Urdu"};

  static public Map<String, PFont> fontCache = new HashMap<String, PFont>();

  /**
   * @return A list of supported font names.
   */
  static public String[] names() {
    return fontNames;
  }

  /**
   * Create a PFont instance without caching.
   *
   * @param fontName
   * @param fontSize
   * @return
   */
  static public PFont createFontUncached(String fontName, int fontSize) {
    return RainbowStudio.pApplet.createFont(getPlatformIndependentFontName(fontName), fontSize, true);
  }

  /**
   * Returns a the PFont instance for a font.  If it does not exist, it is created.
   *
   * @param fontName  The name of the font.
   * @param fontSize  The size of the font.
   *
   * @return An instance of PFont representing the requested font name and size.
   */
  synchronized static public PFont getCachedFont(String fontName, int fontSize) {
    PFont pFont = fontCache.get(fontName + "-" + fontSize);
    if (pFont == null) {
      pFont = RainbowStudio.pApplet.createFont(getPlatformIndependentFontName(fontName), fontSize, true);
      fontCache.put(fontName + "-" + fontSize, pFont);
    }
    return pFont;
  }

  /**
   * Need to handle platform-specific issues here as registered font names can vary
   * for a given font from even the same font definition file.
   *
   * @param fname
   * @return
   */
  static public String getPlatformIndependentFontName(String fname) {
    if (RainbowStudio.pApplet.platform == PConstants.MACOSX) {
      if (fname.equals("04b 30")) return "04b";
      if (fname.equals("Press Start Regular")) return "PressStart2P";
      if (fname.equals("Noto Sans Arabic Regular")) return "NotoSansArabic-Regular";
      if (fname.equals("Noto Sans SC")) return "NotoSansSC-Regular";
    } else {
      if (fname.equals("NotoSansSC-Regular")) return "Noto Sans SC";
      if (fname.equals("NotoSansArabic-Regular")) return "Noto Sans Arabic Regular";
    }
    return fname;
  }
}


/*
 * Utility base class to clean up all the patterns that have file
 * inputs.
 */
public class FileItemBase extends UIItemList.Item {
  protected final String filename;

  public FileItemBase(String str) {
    this.filename = str;
  }
  public boolean isActive() {
    return false;
  }
  public int getActiveColor(UI ui) {
    return ui.theme.getAttentionColor();
  }
  public String getLabel() {
    return filename;
  }
}

static public class ImgUtil {
  static public String stripExtension (String str) {
    if (str == null) return null;
    int pos = str.lastIndexOf(".");
    if (pos == -1) return str;
    return str.substring(0, pos);
  }
}

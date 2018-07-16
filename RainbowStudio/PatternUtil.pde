
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

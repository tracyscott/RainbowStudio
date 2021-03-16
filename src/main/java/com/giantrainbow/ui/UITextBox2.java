package com.giantrainbow.ui;
/**
 * NOTE(tracy): This was hacked and brought over to our repo so that we could have a very primitive multiline
 * editor for the TextFx patterns. Ideally, the TextFx#.txts files should be edited in an external editor versus
 * messing with this broken multiline emergency hack.
 */
/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */


    import heronarts.lx.parameter.LXParameter;
    import heronarts.lx.parameter.LXParameterListener;
    import heronarts.lx.parameter.StringParameter;

public class UITextBox2 extends UIInputBox2 {

  private final static String NO_VALUE = "-";

  private String value = NO_VALUE;
  private StringParameter parameter = null;

  private final LXParameterListener parameterListener = new LXParameterListener() {
    public void onParameterChanged(LXParameter p) {
      setValue(parameter.getString());
    }
  };

  public UITextBox2() {
    this(0, 0, 0, 0);
  }

  public UITextBox2(float x, float y, float w, float h) {
    super(x, y, w, h);
  }

  public UITextBox2 setParameter(StringParameter parameter) {
    if (this.parameter != null) {
      this.parameter.removeListener(this.parameterListener);
    }
    this.parameter = parameter;
    if (parameter != null) {
      this.parameter.addListener(this.parameterListener);
      setValue(parameter.getString());
    } else {
      setValue(NO_VALUE);
    }
    return this;
  }

  @Override
  public String getDescription() {
    return heronarts.p3lx.ui.component.UIParameterControl.getDescription(this.parameter);
  }

  public String getValue() {
    return this.value;
  }

  @Override
  protected String getValueString() {
    return this.value;
  }

  public UITextBox2 setValue(String value) {
    if (!this.value.equals(value)) {
      this.value = value;
      if (this.parameter != null) {
        this.parameter.setValue(this.value);
      }
      this.onValueChange(this.value);
      redraw();
    }
    return this;
  }

  /**
   * Subclasses may override to handle value changes
   *
   * @param value New value being set
   */
  protected /* abstract */ void onValueChange(String value) {}


  @Override
  protected void saveEditBuffer() {
    String value = this.editBuffer.trim();
    if (value.length() > 0) {
      setValue(value);
    }
  }

  private static final String VALID_CHARACTERS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ,.<>?;':\"[]{}-=_+`~!@#$%^&*()|1234567890\n\r";

  public static boolean isValidTextCharacter(char keyChar) {
    return VALID_CHARACTERS.indexOf(keyChar) >= 0;
  }

  @Override
  protected boolean isValidCharacter(char keyChar) {
    return isValidTextCharacter(keyChar);
  }

}

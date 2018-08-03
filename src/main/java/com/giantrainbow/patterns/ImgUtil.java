package com.giantrainbow.patterns;

class ImgUtil {
  public static String stripExtension (String str) {
    if (str == null) return null;
    int pos = str.lastIndexOf(".");
    if (pos == -1) return str;
    return str.substring(0, pos);
  }
}

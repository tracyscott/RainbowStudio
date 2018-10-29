package com.giantrainbow;

import com.google.gson.JsonObject;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to encapsulate a set of LXParameter objects that can have
 * their values stored in a backing file independent of the project file.
 * For example, installation specific configuration values such as the particular
 * PixLite configuration.
 */
public class ParameterFile {
  PropertyFile props;
  Map<String, LXParameter> params = new HashMap<String, LXParameter>();

  public static final int TYPE_STRING = 0;
  public static final int TYPE_INT = 1;
  public static final int TYPE_FLOAT = 2;
  public static final int TYPE_DOUBLE = 3;

  public ParameterFile(String filename) {
    props = new PropertyFile(filename);
  }

  public boolean exists() {
    return props.exists();
  }

  /**
   * Load a property file.  This will create a map of LXParameter instances keyed by
   * parameter name.  A UI component can process this map to build the appropriate UI.
   */
  public void load() throws IOException, PropertyFile.NotFound {
    props.load();
    for (String key : props.keys()) {
      JsonObject param = props.getObj(key);
      int type = param.get("t").getAsInt();
      if (type == PropertyFile.TYPE_STRING) {
        StringParameter p = new StringParameter(key, param.get("v").getAsString());
        params.put(key, p);
      } else if (type == PropertyFile.TYPE_INT) {
        int min = param.get("min").getAsInt();
        int max = param.get("max").getAsInt();
        int v = param.get("v").getAsInt();
        DiscreteParameter p = new DiscreteParameter(key, v, min, max);
        params.put(key, p);
      } else if (type == PropertyFile.TYPE_FLOAT) {
        float min = param.get("min").getAsFloat();
        float max = param.get("max").getAsFloat();
        float v = param.get("v").getAsFloat();
        CompoundParameter p = new CompoundParameter(key, v, min, max);
        params.put(key, p);
      } else if (type == PropertyFile.TYPE_DOUBLE) {
        double base = param.get("base").getAsDouble();
        double range = param.get("range").getAsDouble();
        double v = param.get("v").getAsDouble();
        System.out.println("Loading " + key + " double: " + v + " base: " + base + " range:" + range);
        CompoundParameter p = new CompoundParameter(key, v, base, range);
        params.put(key, p);
      }
    }
  }

  /**
   * Save our parameters to the backing properties file.
   *
   * @throws IOException
   */
  public void save() throws IOException {
    for (LXParameter p: params.values()) {
      if (p instanceof StringParameter) {
        props.setString(p.getLabel(), ((StringParameter) p).getString());
      } else if (p instanceof DiscreteParameter) {
        DiscreteParameter dp = (DiscreteParameter)p;
        JsonObject obj = new JsonObject();
        obj.addProperty("v", dp.getValuei());
        obj.addProperty("t", PropertyFile.TYPE_INT);
        obj.addProperty("min", dp.getMinValue());
        obj.addProperty("max", dp.getMaxValue());
        props.setObj(dp.getLabel(), obj);
      } else if (p instanceof CompoundParameter) {
        CompoundParameter cp = (CompoundParameter)p;
        JsonObject obj = new JsonObject();
        obj.addProperty("v", cp.getValue());
        obj.addProperty("t", PropertyFile.TYPE_DOUBLE);
        obj.addProperty("base", cp.range.v0);
        obj.addProperty("range", cp.range.v1);
        props.setObj(cp.getLabel(), obj);
      }
    }
    props.save();
  }

  /**
   * Create a new StringParameter to be saved.
   *
   * @param name The label for the StringParameter. Used as key in properties file.  Must be unique.
   * @param value The string value of the StringParameter.
   * @return A new StringParameter.
   */
  public StringParameter newStringParameter(String name, String value) {
    StringParameter sp = new StringParameter(name, value);
    params.put(name, sp);
    return sp;
  }

  /**
   * Get the named StringParameter.  If not found, create it with specified default value.
   *
   * @param name The label for the StringParameter.
   * @param value The default value if not found.
   * @return The requested StringParameter or a new one if not found.
   */
  public StringParameter getStringParameter(String name, String value) {
    StringParameter sp = (StringParameter)params.get(name);
    if (sp == null) {
      System.out.println("Did not find existing, creating new.");
      sp = newStringParameter(name, value);
    } else {
      System.out.println("Found existing StringParameter.");
    }
    return sp;
  }

  /**
   * Create a new DiscreteParameter to be saved.
   *
   * @param name The label for the DiscreteParameter.  Used as a key in properties file.  Must be unique.
   * @param value An integer value between min and max.
   * @param min The minimum value for the parameter.
   * @param max The maximum value for the parameter.
   * @return A new DiscreteParameter
   */
  public DiscreteParameter newDiscreteParameter(String name, int value, int min, int max) {
    DiscreteParameter dp = new DiscreteParameter(name, value, min, max);
    params.put(name, dp);
    return dp;
  }

  /**
   * Get the named DiscreteParameter.  If not found, create it with specified defaults.
   *
   * @param name The label for the DiscreteParameter.
   * @param value The default value of the DiscreteParameter.
   * @param min The default min value.
   * @param max The default max value.
   * @return The requested DiscreteParameter or a new one if not found.
   */
  public DiscreteParameter getDiscreteParameter(String name, int value, int min, int max) {
    DiscreteParameter dp = (DiscreteParameter)params.get(name);
    if (dp == null)
      dp = newDiscreteParameter(name, value, min, max);
    return dp;
  }

  /**
   * Create a new CompoundParameter to be saved.
   * // TODO(tracy): Consider just using a BoundedParameter?  Not sure if these values need to participate in
   * // modulation.
   *
   * @param name The label for the CompoundParameter.  Used as a key in properties file. Must be unique.
   * @param value A double value greater than base and less than base + range.
   * @param base The base/min value for the parameter.
   * @param range The range of acceptable values for the parameter.
   * @return A new CompoundParameter.
   */
  public CompoundParameter newCompoundParameter(String name, double value, double base, double range) {
    CompoundParameter cp = new CompoundParameter(name, value, base, range);
    params.put(name, cp);
    return cp;
  }

  /**
   * Get the named CompoundParameter.  If not found, create it with given defaults.
   *
   * @param name The label of the CompoundParameter.
   * @param value The default value of the CompoundParameter.
   * @param base The default base value of the CompoundParameter.
   * @param range The default range of the CompoundParameter.
   * @return The requested existing CompoundParameter or a new one if not found.
   */
  public CompoundParameter getCompoundParameter(String name, double value, double base, double range) {
    CompoundParameter cp = (CompoundParameter) params.get(name);
    if (cp == null) {
      System.out.println("Didn't find existing compound parameter");
      cp = newCompoundParameter(name, value, base, range);
    } else {
      System.out.println("Founding existing compound parameter");
    }
    return cp;
  }

}

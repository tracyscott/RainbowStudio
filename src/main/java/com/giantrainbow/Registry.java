/*
 * Created by shawn on 8/19/18 4:13 PM.
 */
package com.giantrainbow;

import com.giantrainbow.input.InputManager;
import heronarts.lx.LX;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import processing.core.PApplet;

/**
 * A registry of globally-accessible objects. This helps avoid using public static fields
 * in the main program. All keys are guaranteed to exist in the registry.
 */
public class Registry {
  /**
   * All these keys are guaranteed to exist in the registry.
   */
  public static final class Key<T> {
    /** The parent {@link PApplet}. */
    public static final Key<PApplet> APPLET = new Key<>();

    /** The input manager ({@link InputManager}). */
    public static final Key<InputManager> INPUT_MANAGER = new Key<>();

    /** The executor service ({@link ScheduledExecutorService}). */
    public static final Key<ScheduledExecutorService> EXEC = new Key<>();

    private Key() {
    }
  }

  /** The actual registry. */
  private Map<Key<?>, Object> registry = new HashMap<>();

  Registry(PApplet applet, LX lx) {
    Objects.requireNonNull(applet, "applet");
    Objects.requireNonNull(lx, "lx");
    registry.put(Key.APPLET, applet);
    registry.put(Key.INPUT_MANAGER, new InputManager(lx));
    registry.put(Key.EXEC, Executors.newScheduledThreadPool(1));
  }

  /**
   * Gets an object from the registry. The returned type will be correct.
   */
  @SuppressWarnings("unchecked")
  public <U> U get(Key<U> key) {
    return (U) registry.get(key);
  }
}

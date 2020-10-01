package com.mailuefterl.matriarch;

/*-
 * #%L
 * MatriarchSwitch
 * %%
 * Copyright (C) 2020 Mailüfterl s.r.o.
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

/**
 * List of supported property names for MatriarchSwitch application
 */
public enum MatProperty {
  PACKAGE_VERSION("MatSwitch.version"),
  MIDI_TIMEOUT("MatSwitch.midi.timeout"),
  PARAMETER_DEF_URL("MatSwitch.paramdef.url");
  
  /** property key */
  private final String key;
  
  /** private constructor */
  private MatProperty(final String keyname) {
    key = keyname;
  }
  
  @Override
  public String toString() {
    return key;
  }
  
  /** gets the system property value for this key */
  public String getProperty() {
    return System.getProperty(key);
  }
  
  /** gets the system property value, with default */
  @SuppressWarnings("unchecked")
  public <T> T getProperty(T defaultValue) {
    try {
      final String value = System.getProperty(key);
      if (value == null) {
        return defaultValue;
      }
      if (defaultValue instanceof Long) {
        return (T)Long.valueOf(value);
      } else if (defaultValue instanceof Integer) {
        return (T)Integer.valueOf(value);
      } else {
        return (T)value;
      }
    }
    catch (final ClassCastException e) { return defaultValue; }
    catch (final NumberFormatException e) { return defaultValue; }
  }
}

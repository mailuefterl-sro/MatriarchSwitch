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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main class for MatriarchSwitch, a standalone utility to manipulate global
 * parameters in the Moog Matriarch Synthesizer.
 */
public abstract class MatriarchSwitch {
  /**
   * Start the standalone application
  */
  public static void main(String[] args) {
    loadProperties();
    MatSwitchController ctl = new MatSwitchController();
    ctl.setUiHandler(new MatSwitchUi(ctl));
    ctl.setMidiHandler(new MatSwitchMidi(ctl));
    ctl.start();
  }
  
  /** load properties from file embedded in jar */
  private static void loadProperties() {
    final String loadFrom = "/MatSwitch.properties";
    final InputStream is = MatriarchSwitch.class.getResourceAsStream(loadFrom);
    if (is == null) {
      System.err.println("Unable to find "+ loadFrom);
      return;
    }
    final Properties fromFile = new Properties();
    try {
      fromFile.load(is);      
    }
    catch (final IOException e) {
      System.err.println("Unable to load "+ loadFrom +": "+ e);
    }
    finally {
      try { is.close(); } catch (final IOException ignore) {}
    }
    
    // write loaded properties into system-properties, but only if they are
    // not already there (to not overwrite e.g. commandline properties)
    final Properties fromSys = System.getProperties();
    for (final Object okey: fromFile.keySet()) {
      fromSys.putIfAbsent(okey, fromFile.get(okey));
    }
  }
}

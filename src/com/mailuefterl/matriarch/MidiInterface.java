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

import javax.sound.midi.MidiDevice;

/**
 * wrapper class to contain a MidiDevice and a legible name (for combobox).
 */
public class MidiInterface {
  
  /** underlying midi device */
  final MidiDevice dev;
  /** name to display */
  final String name;
  
  /** constructor */
  public MidiInterface(final MidiDevice dev) {
    this.dev = dev;
    MidiDevice.Info minfo = dev.getDeviceInfo();
    this.name = minfo.getName() +", "+ minfo.getDescription();
  }
  
  /** string representation (the reason why this class even exists) */
  public String toString() {
    return name;
  }
  
  /** get the underlying midi device */
  public MidiDevice getDevice() {
    return dev;
  }
}

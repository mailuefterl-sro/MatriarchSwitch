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
 * Information about a Matriarch Unit on MIDI
 */
public class MatriarchUnit {
  /** name of unit */
  private final String name;
  /** unit-ID */
  private final byte unitId;
  /** is this a pseudo-unit (without MIDI communication) ? */
  private final boolean pseudo;
  
  /** constructor */
  public MatriarchUnit(final String name, final byte unitId) {
    this.name = name;
    this.unitId = unitId;
    this.pseudo = false;
  }
  
  /** constructor for pseudo units */
  public MatriarchUnit(final String name) {
    this.name = name;
    this.unitId = -1;
    this.pseudo = true;
  }
  
  /** string representation */
  public String toString() {
    return name;
  }
  
  /** get MIDI unit ID */
  public byte getUnitId() {
    return unitId;
  }
  
  /** is this a pseudo unit? */
  public boolean isPseudo() {
    return pseudo;
  }
}

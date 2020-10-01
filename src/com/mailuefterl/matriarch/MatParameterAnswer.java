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

import java.text.ParseException;

import javax.sound.midi.SysexMessage;

/**
 * Answer from Matriarch for one parameter (immutable)
 */
public class MatParameterAnswer {
  
  /** parameter ID */
  public final byte paramId;
  /** parameter value */
  public final int paramValue;
  /** Unit id of sender (not available in firmware 1.2.0) */
  public final byte unitId;
  /** is this a reply to fetchParameter? (byte offset 14 == 01) */
  public final boolean isAnswer;
  
  /** constructor: parse Sysex message:<p>
   * {@code F0 04 17 23 [Parameter ID], [value MSB], [value LSB], 00 00 00 00 00 00 00 01 ([Unit ID]) F7}
   * <p>Note Matriarch Firmware 1.2.0 is buggy and does not send the UnitId in the answer. */
  public MatParameterAnswer(final SysexMessage sysex) throws ParseException {
    byte[] data = sysex.getMessage();
    if ((data.length != 17) && (data.length != 16)) {
      throw new ParseException("wrong sysex length", 0);
    }
    if (data[1] != 0x04) {
      throw new ParseException("missing Moog ID 04", 1);
    }
    if (data[2] != 0x17) {
      throw new ParseException("missing Matriarch ID 17", 2);
    }
    if (data[3] != 0x23) {
      throw new ParseException("not a parameter message", 3);
    }
    if ((data[14] != 1) && (data[14] != 0)) {
      throw new ParseException("invalid parameter reply field", 14);
    }
    paramId = data[4];
    paramValue = data[5] * 128 + data[6];
    unitId = data[15];
    isAnswer = (data[14] == 1);
  }
}

package com.mailuefterl.matriarch.parameter;

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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * possible value for one Parameter
 */
public class ParameterChoiceValue implements IParameterValue {
  /** integer representation for value (0=key C0, 1=key D0 etc.) */
  public final int number;
  /** String representation for value ("Off", "On", "Both" etc.) */
  public final String name;
  /** true if this is the default value */
  public final boolean isDefault;
  
  /** constructor */
  @JsonCreator(mode=JsonCreator.Mode.PROPERTIES)
  public ParameterChoiceValue(
          @JsonProperty("name") final String name,
          @JsonProperty("number") final int num,
          @JsonProperty("default") final boolean isDefault) {
    this.number = num;
    this.name = name;
    this.isDefault = isDefault;
  }
  
  /** String representation (for combobox etc.) */
  public String toString() {
    return name;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(IParameterValue another) {
    return (number == another.getNumber());
  }

  /** {@inheritDoc} */
  @Override
  public int getNumber() {
    return number;
  }
}

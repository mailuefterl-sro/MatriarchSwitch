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
 * A parameter value defined by a numeric range (and a current value)
 */
public class ParameterRange {
  /** minimum allowed number for range */
  public final int rangeMin;
  /** maximum allowed number for range */
  public final int rangeMax;
  /** default number */
  public final int defaultVal;
  
  /** constructor */
  @JsonCreator(mode=JsonCreator.Mode.PROPERTIES)
  public ParameterRange(
          @JsonProperty("minimum") final int minimum,
          @JsonProperty("maximum") final int maximum,
          @JsonProperty("default") final int defaultInt
          ) {
    this.rangeMin = minimum;
    this.rangeMax = maximum;
    this.defaultVal = defaultInt;
  }
  
  /** factory for actual values within the range */
  public IParameterValue getValue(final int val) {
    if ((val >= rangeMin) && (val <= rangeMax)) {
      return new ParameterRangeValue(val);
    }
    return null;
  }

  
  /** inner class that represents an actual numeric value within the configured range (immutable) */
  private class ParameterRangeValue implements IParameterValue {
    /** the actual numeric value */
    private final int numberValue;
    
    /** constructor (only used in factory method in ParameterRange) */
    private ParameterRangeValue(final int val) {
      numberValue = val;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(IParameterValue another) {
      return (this.numberValue == another.getNumber());
    }

    /** {@inheritDoc} */
    @Override
    public int getNumber() {
      return numberValue;
    }
    
    @Override
    public String toString() {
      return String.valueOf(numberValue);
    }
  }
}

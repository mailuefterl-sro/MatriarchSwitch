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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mailuefterl.matriarch.util.ILogger;
import com.mailuefterl.matriarch.util.LogManager;

/**
 * Moog Matriarch Global Parameter.
 * Contains data about one Global Parameter (group, name, description, possible values)
 */
public class Parameter {
  /** logger instance */
  private static final ILogger log = LogManager.getLogger();
  /** group this parameter belongs to */
  public final ParameterGroup group;
  /** number of parameter within group, corresponds to black key */
  public final int parameterNumber;
  /** number of parameter when fetching/setting parameter via MIDI */
  public final byte midiNumber;
  /** parameter name */
  public final String name;
  /** detailed description of parameter */
  public final String description;
  /** list of allowed values */
  public final List<ParameterChoiceValue> choices;
  /** numeric range for value */
  public final ParameterRange range;
  /** the default value (must be contained in choices list or range) */
  public final IParameterValue defaultValue;
  /** the value the parameter is currently set to in GUI */
  private IParameterValue currentValue;
  /** the value that was last read from Matriarch */
  private IParameterValue retrievedValue;
  
  /** generic constructor */
  @JsonCreator(mode=JsonCreator.Mode.PROPERTIES)
  public Parameter(
          @JsonProperty("group") final String groupName,
          @JsonProperty("number") final int parameterNumber,
          @JsonProperty("midinumber") final byte midiNumber,
          @JsonProperty("name") final String parameterName,
          @JsonProperty("description") final String description,
          @JsonProperty("choices") final List<ParameterChoiceValue> choices,
          @JsonProperty("range") final ParameterRange range
          ) {
    this.group = ParameterGroup.findOrCreateGroup(groupName);
    this.group.addParameter(this);
    this.parameterNumber = parameterNumber;
    this.midiNumber = midiNumber;
    this.name = parameterName;
    this.description = description;
    this.range = range;
    if ((range == null) && ((choices == null) || (choices.size() == 0))) {
      log.error("Configuration for Parameter ", name, " has neither a range nor choices");
    } else if ((range != null) && (choices != null)) {
      log.error("Configuration for Parameter ", name, " has both range and choices");
    }
    if (range != null) {
      this.choices = null;
    } else if (choices == null) {
      this.choices = new ArrayList<ParameterChoiceValue>(0);
    } else {
      this.choices = Collections.unmodifiableList(choices);
    }
    IParameterValue defaultVal = null;
    if (range != null) {
      defaultVal = findValue(range.defaultVal);
    } else {
      for (ParameterChoiceValue pv: choices) {
        if (pv.isDefault) {
          if (defaultVal == null) {
            defaultVal = pv;
          } else {
            log.error("Configuration for Parameter ", name, " has ambiguous default value");
            break;
          }
        }
      }
    }
    if (defaultVal == null) {
      log.error("Configuration for Parameter ", name, " is missing default value");
      defaultVal = choices.get(0);
    }
    this.defaultValue = defaultVal;
    this.currentValue = defaultVal;
    this.retrievedValue = defaultVal;
  }

  /** string representation (for combobox etc.) */
  public String toString() {
    return name;
  }
  
  /** find the ParameterValue that matches the given integer */
  public IParameterValue findValue(final int intVal) {
    if (range != null) {
      return range.getValue(intVal);
    } else {
      for (final ParameterChoiceValue parVal: getChoices()) {
        if (parVal.number == intVal) {
          return parVal;
        }
      }
    }
    return null;
  }
  
  /** set current value */
  public void setCurrentValue(final IParameterValue val) {
    currentValue = val;
  }
  
  /** set retrieved value */
  public void setRetrievedValue(final IParameterValue val) {
    retrievedValue = val;
  }
  
  /** is this parameter defined by a numeric range? */
  public boolean isRange() {
    return (range != null);
  }
  
  /** get allowed range */
  public ParameterRange getRange() {
    return range;
  }
  
  /** get list of possible choices */
  public List<ParameterChoiceValue> getChoices() {
    return choices;
  }
  
  /** get current value (or default) */
  public IParameterValue getCurrentValue() {
    return (currentValue != null) ? currentValue : defaultValue;
  }
  
  /** get the value that was last retrieved from hardware (or default) */
  public IParameterValue getRetrievedValue() {
    return (retrievedValue != null) ? retrievedValue : defaultValue;
  }

  /** get default value */
  public IParameterValue getDefaultValue() {
    return defaultValue;
  }
  
  /** has the parameter been changed from last-retrieved value? */
  public boolean isChanged() {
    return !(retrievedValue.equals(currentValue));
  }
  
  /** is the current parameter value equal to the default? */
  public boolean isDefault() {
    return (defaultValue.equals(currentValue));
  }
}

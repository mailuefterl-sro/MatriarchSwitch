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

/**
 * A parameter group for Moog Matriarch, as defined in the user manual.
 */
public class ParameterGroup {
  /** registry for all groups (list instead of map so that sort order stays intact) */
  private static List<ParameterGroup> allGroups = new ArrayList<ParameterGroup>(20);
  /** parameter group name (unique) */
  public final String name;
  /** list of parameters belonging to group */
  private final List<Parameter> parameters;
  
  /* constructor can only be called by static methods below */
  private ParameterGroup(final String name) {
    this.name = name;
    this.parameters = new ArrayList<Parameter>(10);
    allGroups.add(this);
  }
  
  /** getter for parameter list (readonly) */
  public List<Parameter> getParameters() {
    return Collections.unmodifiableList(parameters);
  }
  
  /** add a parameter to this group */
  public void addParameter(final Parameter p) {
    parameters.add(p);
  }
  
  /** get (or create) group with given name */
  @JsonCreator(mode=JsonCreator.Mode.PROPERTIES)
  public static ParameterGroup findOrCreateGroup(
          @JsonProperty("name") final String name
          ) {
    for (ParameterGroup pg: allGroups) {
      if (name.equals(pg.name)) {
        return pg;
      }
    }
    return new ParameterGroup(name);
  }
  
  /** create a group containing all parameters from all other groups */
  public static ParameterGroup createSuperGroup(final String name) {
    ParameterGroup psg = new ParameterGroup(name);
    for (ParameterGroup pg: allGroups) {
      if (pg != psg) {
        psg.parameters.addAll(pg.parameters);
      }
    }
    return psg;
  }
  
  /** get a list of all groups (including super-group) */
  public static List<ParameterGroup> getAllGroups() {
    return Collections.unmodifiableList(allGroups);
  }
  
  /** string representation for combobox etc. */
  public String toString() {
    return name;
  }
}

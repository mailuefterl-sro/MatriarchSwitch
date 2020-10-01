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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Helper class for Json deserialization of Parameters:
 * Top-level list of parameters and file information (version etc).
 * 
 */
public class JsonParameterLoader {
  public final String vendor;
  public final String device;
  public final String type;
  public final String version;
  public final List<Parameter> parameters = new ArrayList<Parameter>();
  public final List<ParameterGroup> groups = new ArrayList<ParameterGroup>();
  
  @JsonCreator(mode=JsonCreator.Mode.PROPERTIES)
  public JsonParameterLoader(
          @JsonProperty("vendor") String vendor,
          @JsonProperty("device") String device,
          @JsonProperty("type") String type,
          @JsonProperty("version") String version
          ) {
    this.vendor = vendor;
    this.device = device;
    this.type = type;
    this.version = version;
  }
}

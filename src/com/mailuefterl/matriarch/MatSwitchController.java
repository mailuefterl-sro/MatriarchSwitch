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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.SysexMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailuefterl.matriarch.parameter.IParameterValue;
import com.mailuefterl.matriarch.parameter.JsonParameterLoader;
import com.mailuefterl.matriarch.parameter.Parameter;
import com.mailuefterl.matriarch.parameter.ParameterGroup;
import com.mailuefterl.matriarch.util.ILogger;
import com.mailuefterl.matriarch.util.LogManager;

/**
 * Controller for MatriarchSwitch, interfaces UI with background tasks.
 */
public class MatSwitchController {
  /** logger object */
  private final static ILogger log = LogManager.getLogger();
  /** pseudo-unit for simulation without physical Matriarch */
  private final static MatriarchUnit pseudoMatriarch = new MatriarchUnit("No physical unit (Simulation)");

  /** User interface handler */
  private MatSwitchUi gui;
  /** MIDI handler for specific Sysex messages */
  private MatSwitchMidi midi;
  /** currently selected Matriarch unit */
  private MatriarchUnit currentMatriarch;
  /** parameter group containing ALL parameters */
  private ParameterGroup allParametersGroup;
  /** information about the loaded parameters file */
  private JsonParameterLoader paramLoader;
  
  /** constructor for new controller */
  public MatSwitchController() {
    log.info("MatriarchSwitcher starting up");
    loadParameters();
  }
  
  /** register the user interface handler */
  protected void setUiHandler(final MatSwitchUi gui) {
    this.gui = gui;
  }
  
  /** register midi handler */
  protected void setMidiHandler(final MatSwitchMidi midi) {
    this.midi = midi;
  }
  
  /** get it all going */
  public void start() {
    gui.start();
  }
  
  /** interface method: retrieve a list of midi interfaces */
  public List<MidiInterface> fetchMidiOutPorts() {
    if (midi == null) {
      return new ArrayList<MidiInterface>(0);
    }
    return midi.fetchInterfaces(false, true);
  }
  
  /** interface method: retrieve a list of midi interfaces */
  public List<MidiInterface> fetchMidiInPorts() {
    if (midi == null) {
      return new ArrayList<MidiInterface>(0);
    }
    return midi.fetchInterfaces(true, false);
  }
  
  /** set the currently selected Midi interface */
  public void setMidiOutPort(final MidiInterface iface) {
    if (midi != null) {
      midi.setOutPort(iface);
    }
  }
  
  /** set the currently selected Midi interface */
  public void setMidiInPort(final MidiInterface iface) {
    if (midi != null) {
      midi.setInPort(iface);
    }
  }
  
  /** interface method: retrieve a list of devices found on a MIDI interface */
  public List<MatriarchUnit> fetchMidiUnits() {
    if (midi == null) {
      final List<MatriarchUnit> ret = new ArrayList<MatriarchUnit>(0);
      ret.add(pseudoMatriarch);
      return ret;
    }
    final List<MatriarchUnit> ret = midi.fetchUnits();
    ret.add(pseudoMatriarch);
    return ret;
  }
  
  /** interface method: Midi told us our connection is gone, close ports and
   * tell UI to re-fetch interfaces.
   */
  public void midiFailed() {
    setMidiOutPort(null);
    setMidiInPort(null);
    gui.midiFailed();
  }
  
  /** set the currently selected Matriarch unitId */
  public void setMatUnit(final MatriarchUnit unit) {
    currentMatriarch = unit;
  }
  
  /** retrieve one parameter from selected Matriarch */
  public IParameterValue retrieveParameter(final Parameter param) {
    if ((midi == null) || (currentMatriarch == null) || currentMatriarch.isPseudo()) {
      param.setRetrievedValue(param.defaultValue);
      param.setCurrentValue(param.defaultValue);
      return param.defaultValue;
    } else {
      log.info("Retrieving parameter ", param);
      int val = midi.fetchParameter(currentMatriarch.getUnitId(), param.midiNumber);
      if (val < 0) {
        log.error("Unable to retrieve parameter ", param);
        return null;
      }
      final IParameterValue parsival = param.findValue(val);
      if (parsival == null) {
        log.error("Unable to parse parameter value: ", param, " ", val);
      } else {
        param.setRetrievedValue(parsival);
        param.setCurrentValue(parsival);
      }
      return parsival;
    }
  }
  
  /** retrieve ALL parameters from selected Matriarch */
  public boolean retrieveAllParameters() {
    log.info("Retrieving all parameters...");
    int numParams = 0;
    for (final Parameter param: allParametersGroup.getParameters()) {
      if (retrieveParameter(param) == null) {
        log.error("Retrieval of all parameters failed.");
        return false;
      }
      numParams++;
    }
    log.info("Successfully retrieved ", numParams, " Parameters.");
    return true;
  }
  
  /** get a list of all changed parameters (currentValue != retrievedValue) */
  public List<Parameter> getChangedParameters() {
    final List<Parameter> ret = new ArrayList<Parameter>();
    for (final Parameter param: allParametersGroup.getParameters()) {
      if (param.isChanged()) {
        ret.add(param);
      }
    }
    return ret;
  }
  
  /** store one parameter to Matriarch */
  public boolean storeParameter(final Parameter param) {
    log.info("Storing Parameter ", param);
    final boolean ret = midi.storeParameter(currentMatriarch.getUnitId(),
            param.midiNumber, param.getCurrentValue().getNumber());
    if (ret) {
      param.setRetrievedValue(param.getCurrentValue());
    } else {
      log.error("Failed storing Parameter ", param);
    }
    return ret;
  }
  
  /** store all changed parameters to Matriarch */
  public boolean storeParameters() {
    log.info("Storing changed parameters...");
    if ((midi == null) || (currentMatriarch == null) || currentMatriarch.isPseudo()) {
      for (final Parameter param: getChangedParameters()) {
        param.setRetrievedValue(param.getCurrentValue());
      }
      log.info("Simulation mode, mocking success.");
      return true;
    }
    boolean ret = true;
    int numStored = 0;
    for (final Parameter param: getChangedParameters()) {
      ret = storeParameter(param);
      if (ret) {
        numStored++;
      } else {
        break;
      }
    }
    if (ret) {
      log.info("Successfully stored ", numStored, " Parameters.");
    } else {
      log.info("Aborted after ", numStored, " successfully stored Parameters.");
    }
    return ret;
  }
  
  /** reset all parameters to their default value */
  public void resetParamsDefault() {
    int numParams = 0;
    int numChanged = 0;
    for (final Parameter param: allParametersGroup.getParameters()) {
      numParams ++;
      if (!param.isDefault()) {
        numChanged ++;
        param.setCurrentValue(param.getDefaultValue());
      }
    }
    log.info("Reset ", numParams, " Parameters to default (changed ", numChanged, " Parameters)");
  }
  
  /** export parameters to a SysEx file */
  public boolean exportSysex(final File exportFile, final boolean allParameters) {
    log.info("Exporting Parameters to ", exportFile.getAbsolutePath());
    byte unitId = (currentMatriarch == null) ? 0 : currentMatriarch.getUnitId();
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    try {
      fos = new FileOutputStream(exportFile);
      bos = new BufferedOutputStream(fos);
      int numParams = 0;
      for (final Parameter param: allParametersGroup.getParameters()) {
        if (allParameters || param.isChanged()) {
          log.debug("Exporting parameter ", param);
          final MidiMessage msg = midi.createStoreParamRequest(unitId, param.midiNumber, param.getCurrentValue().getNumber());
          if (msg != null) {
            bos.write(msg.getMessage());
            numParams ++;
          }
        }
      }
      log.info("Exported ", numParams, " Parameters to ", exportFile.getName());
      return true;
    }
    catch (final IOException e) {
      log.error("Parameter export failed: ", e);
      return false;
    }
    finally {
      if (bos != null) {
        try { bos.close(); } catch (IOException ignore) {}
      } else if (fos != null) {
        try { fos.close(); } catch (IOException ignore) {}
      }
    }
  }
  
  /** import parameters from a sysex file (file must ONLY contain the store-parameter messages!) */
  public boolean importSysex(final File importFile) {
    log.info("Importing Parameters from ", importFile.getAbsolutePath());
    int numParams = 0;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    try {
      fis = new FileInputStream(importFile);
      bis = new BufferedInputStream(fis);
      for (SysexMessage msg = getNextSysex(bis); msg != null; msg = getNextSysex(bis)) {
        final MatParameterAnswer mpa = new MatParameterAnswer(msg);
        final Parameter param = findParameter(mpa.paramId);
        if (param == null) {
          throw new InvalidMidiDataException("Unknown parameter with number "+ mpa.paramId);
        }
        final IParameterValue val = param.findValue(mpa.paramValue);
        if (val == null) {
          throw new InvalidMidiDataException("Value "+ mpa.paramValue +" is not valid for Parameter "+ param);
        }
        param.setCurrentValue(val);
        numParams ++;
        log.debug("Parameter ", param, " imported with value ", val);
      }
      log.info("Successfully imported ", numParams, " Parameters from ", importFile.getName());
      return true;
    }
    catch (final Exception e) {
      log.error("Parameter import failed: ", e);
      log.error("Giving up after ", numParams, " parameters have been imported.");
      return false;
    }
    finally {
      if (bis != null) {
        try { bis.close(); } catch (IOException ignore) {}
      } else if (fis != null) {
        try { fis.close(); } catch (IOException ignore) {}
      }
    }
  }
  
  /** get version of loaded parameters file */
  public String getParametersVersion() {
    if (paramLoader == null) {
      return "-not loaded-";
    } else {
      return paramLoader.version;
    }
  }
  
  /** helper function: find the parameter with the given midiNumber */
  private Parameter findParameter(final byte midiNumber) {
    for (final Parameter param: allParametersGroup.getParameters()) {
      if (param.midiNumber == midiNumber) {
        return param;
      }
    }
    return null;
  }
  
  /** helper function: extract a Sysex message from an InputStream */
  private SysexMessage getNextSysex(final InputStream bis)
          throws IOException, InvalidMidiDataException {
    final byte[] buffer = new byte[100];
    if (bis.read(buffer, 0, 1) == -1) {
      return null; // EOF instead of new Sysex start byte, OK
    }
    for (int i = 1; i < buffer.length; i++) {
      if (bis.read(buffer, i, 1) == -1) {
        // EOF in mid of Sysex, not OK
        throw new InvalidMidiDataException("Encountered EOF instead of Sysex end marker F7");
      }
      if (buffer[i] == (byte)0xF7) {
        // end of sysex, try to parse and return it
        return new SysexMessage(buffer, i);
      } else if (buffer[i] < 0) {
        // some MIDI status byte, invalid
        int bv = buffer[i]; if (bv < 0) { bv += 256; }
        throw new InvalidMidiDataException("Encountered status byte "
                + Integer.toHexString(bv) + " instead of Sysex end marker F7");
      }
    }
    // ran out of buffer (message too long)
    throw new InvalidMidiDataException("MIDI Sysex message longer than "+ buffer.length +" bytes");
  }
  
  /** add known Matriarch parameters from file */
  private void loadParameters() {
    final ObjectMapper om = new ObjectMapper();
    final String urlString = System.getProperty("MatSwitch.MatriarchParametersUrl");
    final JsonParameterLoader pl;
    String loadFrom = "null";
    try {
      if (urlString != null) {
        loadFrom = urlString;
        final URL jsonUrl = new URL(urlString);
        pl = om.readValue(jsonUrl, JsonParameterLoader.class);
      } else {
        loadFrom = "/matriarch-parameters.json";
        final InputStream is = getClass().getResourceAsStream(loadFrom);
        pl = om.readValue(is, JsonParameterLoader.class);
      }
      paramLoader = pl;
      log.info("Parameter definition version is ", pl.version);
      log.info("Loaded ", pl.parameters.size(), " Parameter definitions from ", loadFrom);
    }
    catch (final Exception e) {
      log.error("Cannot read Parameter definitions from ", loadFrom, ": ", e);
    }
    allParametersGroup = ParameterGroup.createSuperGroup("ALL Parameters");
  }
}

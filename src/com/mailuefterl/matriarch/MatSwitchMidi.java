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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiDeviceTransmitter;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.SysexMessage;

import com.mailuefterl.matriarch.util.ILogger;
import com.mailuefterl.matriarch.util.LogManager;

/**
 * Midi interface for MatriarchSwitcher, generates and interprets Sysex Messages
 * for parameter manipulation.
 */
public class MatSwitchMidi {
  /** logger */
  private final static ILogger log = LogManager.getLogger();
  
  /** timeout [msec] for MIDI requests */
  private final static long TIMEOUT = MatProperty.MIDI_TIMEOUT.getProperty(1000L);
  
  /** controller instance */
  private final MatSwitchController ctl;
  /** currently selected Midi interface for sending */
  private MidiDeviceTransmitter currentInPort;
  /** currently selected Midi interface for receiving */
  private MidiDeviceReceiver currentOutPort;
  /** internal receiver to get incoming Sysex messages */
  private final SysexReceiver sysexReceiver = new SysexReceiver();
  /** queue for incoming Sysex messages */
  private final Queue<SysexMessage> incomingMessages = new ArrayDeque<SysexMessage>(100);
  

  /** constructor */
  public MatSwitchMidi(final MatSwitchController ctl) {
    this.ctl = ctl;
  }
  
  /** retrieve the list of active interfaces */
  public List<MidiInterface> fetchInterfaces(final boolean doTransmitters, final boolean doReceivers) {
    List<MidiInterface> interfaces = new ArrayList<MidiInterface>();
    for (MidiDevice.Info minfo: MidiSystem.getMidiDeviceInfo()) {
      try {
        MidiDevice mdev = MidiSystem.getMidiDevice(minfo);
        if ((mdev instanceof Synthesizer) || (mdev instanceof Sequencer)) {
          continue;  // ignore software synth & seq
        }
        if (doTransmitters && (mdev.getMaxTransmitters() != 0)) {
          log.debug("found MIDI InPort ", minfo.getName());
          interfaces.add(new MidiInterface(mdev));
        } else if (doReceivers && (mdev.getMaxReceivers() != 0)) {
          log.debug("found MIDI OutPort ", minfo.getName());
          interfaces.add(new MidiInterface(mdev));
        }
      }
      catch (final MidiUnavailableException e) {
        log.error("Warning: unable to fetch Midi interface ", minfo.getName(), ": ", e);
      }
    }
    log.info("Found "+ interfaces.size() +" MIDI interfaces");
    return interfaces;
  }

  /** set the current MIDI input port */
  public void setInPort(final MidiInterface intf) {
    if (intf == null) {
      if (currentInPort != null) {
        currentInPort.close();
        currentInPort.getMidiDevice().close();
        currentInPort = null;
      }
      return;
    }
    final MidiDevice mdev = intf.getDevice();
    final MidiDevice currentDevice = (currentInPort == null) ? null : currentInPort.getMidiDevice();
    if (mdev == currentDevice) {
      return;
    }
    try {
      mdev.open();
      MidiDeviceTransmitter newTx = (MidiDeviceTransmitter)mdev.getTransmitter();
      if (currentInPort != null) {
        currentInPort.close();
        currentDevice.close();
      }
      currentInPort = newTx;
      newTx.setReceiver(sysexReceiver);
      log.info("Using MIDI InPort ", intf);
    }
    catch (final MidiUnavailableException e) {
      log.error("Unable to use MIDI InPort ", intf, ": ", e);
    }
  }
  
  /** set the current MIDI output port */
  public void setOutPort(final MidiInterface intf) {
    if (intf == null) {
      if (currentOutPort != null) {
        currentOutPort.close();
        currentOutPort.getMidiDevice().close();
        currentOutPort = null;
      }
      return;
    }
    final MidiDevice mdev = intf.getDevice();
    final MidiDevice currentDevice = (currentOutPort == null) ? null : currentOutPort.getMidiDevice();
    if (mdev == currentOutPort) {
      return;
    }
    try {
      mdev.open();
      MidiDeviceReceiver newRx = (MidiDeviceReceiver)mdev.getReceiver();
      if (currentOutPort != null) {
        currentOutPort.close();
        currentDevice.close();
      }
      currentOutPort = newRx;
      log.info("Using MIDI OutPort ", intf);
    }
    catch (final MidiUnavailableException e) {
      log.error("Unable to use MIDI OutPort ", intf, ": ", e);
    }
  }
    
  /** retrieve list of Matriarch devices on a MIDI interface: sends a fetchParameterRequest
   * to the broadcast id 7F and looks at all answers */
  public List<MatriarchUnit> fetchUnits() {
    List<MatriarchUnit> units = new ArrayList<MatriarchUnit>();
    Set<Byte> unitIds = new HashSet<Byte>(10);
    if ((currentInPort == null) || (currentOutPort == null)) {
      log.debug("fetchUnits: InPort or OutPort not opened yet");
      return units;
    }
    log.info("Searching for Matriarch units on MIDI bus...");
    purgeIncomingMessages();
    sendMidi(createFetchParamRequest((byte)0, (byte)0x7f));
    long timeoutTime = System.currentTimeMillis() + TIMEOUT;
    do {
      MatParameterAnswer answer = receiveParameterAnswer(0, TIMEOUT);
      if (answer != null) {
        byte unitId = answer.unitId;
        if (unitId < 0) {
          // workaround for Matriarch firmware 1.2.0 bug, does not send unitId
          unitId = (byte)answer.paramValue;
        }
        if (unitIds.add(unitId)) {
          units.add(new MatriarchUnit(String.format("Matriarch unit %02X", unitId), unitId));
          log.info(String.format("Found Matriarch unitId %02X", unitId));
        }
      }
    } while (System.currentTimeMillis() < timeoutTime);
    log.info("Found "+ units.size() +" Matriarch units on MIDI bus");
    return units;
  }
  
  /** retrieve a parameter and wait for answer */
  public int fetchParameter(final byte unitId, final byte paramId) {
    if ((currentInPort == null) || (currentOutPort == null) || (unitId < 0)) {
      log.error("fetchParameter: InPort or OutPort not opened yet");
      return -1;
    }
    purgeIncomingMessages();
    if (!sendMidi(createFetchParamRequest(paramId, unitId))) {
      // error in sending Midi, tell Controller our connection is down.
      ctl.midiFailed();
    };
    final MatParameterAnswer answer = receiveParameterAnswer(paramId, TIMEOUT);
    if (answer == null) {
      log.error("fetchParameter: No answer from Matriarch");
      return -1;
    }
    if (answer.paramId != paramId) {
      log.error("fetchParameter: Answer for unexpected paramId ", answer.paramId);
      return -1;
    }
    return answer.paramValue;
  }
  
  /** store a parameter to Matriarch */
  public boolean storeParameter(final byte unitId, final byte paramId, final int value) {
    if ((currentInPort == null) || (currentOutPort == null) || (unitId < 0)) {
      log.error("fetchParameter: InPort or OutPort not opened yet");
      return false;
    }
    final MidiMessage msg = createStoreParamRequest(unitId, paramId, value);
    if (msg == null) {
      return false;
    }
    if (!sendMidi(msg)) {
      // error in sending Midi, tell Controller our connection is down.
      ctl.midiFailed();
      return false;
    }
    return true;
  }
  
  /** purge the queue of incoming messages */
  private void purgeIncomingMessages() {
    synchronized(incomingMessages) {
      incomingMessages.clear();
    }
  }
  
  /** wait for an incoming parameter message from Matriarch */
  private MatParameterAnswer receiveParameterAnswer(final int paramId, long timeout) {
    long timeoutTime = System.currentTimeMillis() + timeout;
    synchronized(incomingMessages) {
      do {
        SysexMessage msg = incomingMessages.poll();
        long timeLeft = timeoutTime - System.currentTimeMillis();
        if (msg== null) {
          if (timeLeft > 0) {
            try {
              incomingMessages.wait(timeLeft);
            }
            catch (final InterruptedException ignore) {}
          }
        } else {
          try {
            MatParameterAnswer ans = new MatParameterAnswer(msg);
            if (ans.paramId == paramId) {
              return ans;
            }
          }
          catch (final ParseException e) {
            log.debug("Unable to parse Sysex message: "+ e);
          }
        }
      } while (System.currentTimeMillis() < timeoutTime);
    }
    return null;
  }
  
  /** send a "fetch parameter" message:
   * F0 04 17 3E [Parameter ID] 00 00 00 00 00 00 00 00 00 00 [Unit ID] F7 */
  public MidiMessage createFetchParamRequest(final byte paramId, final byte deviceId) {
    byte[] fetchParam = new byte[] { (byte)0xf0, 0x04, 0x17, 0x3e, (byte)paramId, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte)deviceId, (byte)0xf7};
    try {
      return new SysexMessage(fetchParam, fetchParam.length);
    }
    catch (final InvalidMidiDataException e) {
      log.error("Internal error in createFetchParamRequest: ", e);
      return null;
    }
  }
  
  /** create a "store parameter" message:
   * F0 04 17 23 [Parameter ID], [value MSB], [value LSB], 00 00 00 00 00 00 00 00 [Unit ID] F7 */
  public MidiMessage createStoreParamRequest(final byte deviceId, final byte paramId, final int value) {
    byte valMsb = (byte)(value / 128);
    byte valLsb = (byte)(value % 128);
    byte[] storeParam = new byte[] { (byte)0xf0, 0x04, 0x17, 0x23, (byte)paramId, valMsb, valLsb, 0,0,0,0,0,0,0,0, (byte)deviceId, (byte)0xf7};
    try {
      return new SysexMessage(storeParam, storeParam.length);
    }
    catch (final InvalidMidiDataException e) {
      log.error("Internal error in createStoreParamRequest: ", e);
      return null;
    }
  }
  
  /** send a message to MIDI out port */
  private boolean sendMidi(final MidiMessage msg) {
    if ((currentOutPort != null) && (msg != null)) {
      log.iohex("Sending MIDI ", msg.getMessage());
      try {
        currentOutPort.send(msg, -1);
        return true;
      }
      catch (final IllegalStateException e) {
        log.error("Error in sendMidi: ", e);
      }
    }
    return false;
  }
  
  /** helper class to receive SysexMessages (passes it on to  incomingMessages Queue) */
  private final class SysexReceiver implements Receiver {
    /** empty constructor */
    private SysexReceiver() {
    }

    /** {@inheritDoc} */
    @Override
    public void send(final MidiMessage message, final long timeStamp) {
      log.iohex("Receive MIDI ", message.getMessage());
      if (message instanceof SysexMessage) {
        synchronized(incomingMessages) {
          incomingMessages.add((SysexMessage)message);
          incomingMessages.notify();
        }
      }
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
      // nada
    }
  }
}

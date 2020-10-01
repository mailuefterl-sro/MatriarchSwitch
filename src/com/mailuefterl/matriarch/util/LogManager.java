package com.mailuefterl.matriarch.util;

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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Manages logs delegated from ILogger, does the actual buffering of logs
 * and notifies callbacks when new logs arrive.
 * Singleton, can only be instantiated once by getLogManager static method.
 */
public class LogManager {
  /** static pointer to singleton instance */
  private static final LogManager singleInstance = new LogManager();
  
  /** actual log buffer */
  private Queue<LogEntry> logBuffer = new ArrayDeque<>(500);
  /** list of callbacks to be notified when new logs arrive */
  private List<ILogListener> logListeners = new ArrayList<ILogListener>(5);

  
  /** private constructor (use getLogManager() static method instead) */
  private LogManager() {
  }
  
  /** static method to get the singleton instance */
  public static LogManager getLogManager() {
    return singleInstance;
  }
  
  /** static method to get a logger instance */
  public static ILogger getLogger() {
    return new Logger(singleInstance);
  }

  /** add a listener to be notified when logs are added */
  public boolean addLogListener(final ILogListener listener) {
    if ((listener != null) && !logListeners.contains(listener)) {
      logListeners.add(listener);
      return true;
    }
    return false;
  }
  
  /** remove a log listener */
  public boolean removeLogListener(final ILogListener listener) {
    return logListeners.remove(listener);
  }
  
  /** add a log entry */
  private void addLog(final ILogger.LogType type, final String text) {
    final LogEntry entry = new LogEntry(type, text);
    synchronized(logBuffer) {
      logBuffer.add(entry);
    }
    for (ILogListener l: logListeners) {
      l.logAdded(entry);
    }
  }
  
  /** log something (delegated from ILogger instances) */
  public void log(final ILogger.LogType type, final Object ... args) {
    final StringBuilder sb = new StringBuilder();
    for (Object ob: args) {
      sb.append(ob);
    }
    addLog(type, sb.toString());
  }
  
  /** log a hexdump of a data snippet (one-line) */
  public void logHex(final ILogger.LogType type, final String prefix, final byte[] data) {
    final StringBuilder sb = new StringBuilder(prefix);
    for (final byte bite: data) {
      sb.append(String.format(" %02X", bite));
    }
    addLog(type, sb.toString());
  }
  
  /** clear the logbuffer (does not notify any listeners) */
  public void clearLogs() {
    synchronized(logBuffer) {
      logBuffer.clear();
    }
  }
  
  /** get a list of all currently buffered logs */
  public Collection<LogEntry> getLogs() {
    return Collections.unmodifiableCollection(logBuffer);
  }
  
  /** Definition of a callback that is executed when log messages are added. */
  public static interface ILogListener {
    /** notify the listener that a log entry is being added */
    public void logAdded(LogEntry newEntry);
  }

  /** One entry in the log buffer (immutable) */
  public static class LogEntry {
    /** type of log entry */
    public final ILogger.LogType type;
    /** timestamp of log */
    public final long timestamp;
    /** text of log entry */
    public final String text;
    
    /** constructor */
    private LogEntry(ILogger.LogType type, String text) {
      this.type = type;
      this.timestamp = System.currentTimeMillis();
      this.text = text;
    }
  }
}

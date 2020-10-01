package com.mailuefterl.matriarch.ui;

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.mailuefterl.matriarch.util.LogManager;

/**
 * Provides a log facility for other classes, and displays the log information
 * in a TextArea (selectable by type/level of logs)
 */
public class LogPanel extends JPanel {
  /** needed to shut up warning because JPanel is serializable */
  private static final long serialVersionUID = 1L;
  
  /** LogManager instance */
  private final LogManager logMgr;
  /** text area to display the logs */
  private JTextPane jtLogs;
  /** button to clear logs */
  private JButton btClear;
  /** checkbox for Info logs */
  private JCheckBox cbInfo;
  /** checkbox for Debug logs */
  private JCheckBox cbDebug;
  /** checkbox for I/O logs */
  private JCheckBox cbIo;
  /** display attributes for ERROR logs */
  private MutableAttributeSet attrError;
  /** display attributes for INFO logs */
  private MutableAttributeSet attrInfo;
  /** display attributes for DEBUG logs */
  private MutableAttributeSet attrDebug;
  /** display attributes for IO logs */
  private MutableAttributeSet attrIo;
  /** timestamp formatter */
  private SimpleDateFormat dfTimestamp = new SimpleDateFormat("HH:mm:ss.SSS ");
  
  /** constructor (should be called in EDT) */
  public LogPanel() {
    logMgr = LogManager.getLogManager();
    setupUi();
    setupListeners();
  }
  
  /** set up the UI components (called in EDT) */
  private void setupUi() {
    setBorder(BorderFactory.createTitledBorder("Log"));
    setLayout(new BorderLayout());
    
    jtLogs = new JTextPane();
    jtLogs.setEditable(false);
    
    JScrollPane jspLogs = new JScrollPane(jtLogs);
    add(jspLogs, BorderLayout.CENTER);
    
    JPanel pTop = new JPanel();
    pTop.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 10));
    btClear = new JButton("Clear logs");
    pTop.add(btClear);
    cbInfo = new JCheckBox("Display Info logs", true);
    pTop.add(cbInfo);
    cbDebug = new JCheckBox("Display Debug logs", false);
    pTop.add(cbDebug);
    cbIo = new JCheckBox("Display raw MIDI messages", false);
    pTop.add(cbIo);
    add(pTop, BorderLayout.NORTH);
    
    attrError = new SimpleAttributeSet();
    attrError.addAttribute(StyleConstants.Bold, true);
    attrError.addAttribute(StyleConstants.Foreground, Color.RED);
    attrInfo = new SimpleAttributeSet();
    attrInfo.addAttribute(StyleConstants.Bold, true);
    attrDebug = new SimpleAttributeSet();
    attrIo = new SimpleAttributeSet();
  }
  
  /** set up listeners on components (called in EDT) */
  private void setupListeners() {
    btClear.addActionListener(l -> {
      logMgr.clearLogs();
      refreshLogs();
    });
    
    cbInfo.addActionListener(l -> refreshLogs());
    cbDebug.addActionListener(l -> refreshLogs());
    cbIo.addActionListener(l -> refreshLogs());
    
    logMgr.addLogListener(new LogManager.ILogListener() {
      @Override public void logAdded(final LogManager.LogEntry le) {
        addLog(jtLogs.getStyledDocument(), le);
      }
    });
    refreshLogs();
  }
  
  /** (re-)fetch all logs from the log manager. Only needed when log
   * levels or configuration are changed. Must be called in EDT. */
  private void refreshLogs() {
    final Collection<LogManager.LogEntry> allLogs = logMgr.getLogs();
    final StyledDocument newDoc = new DefaultStyledDocument();
    for (LogManager.LogEntry le: allLogs) {
      addLog(newDoc, le);
    }
    jtLogs.setStyledDocument(newDoc);
    jtLogs.select(newDoc.getLength(), newDoc.getLength());
  }
    
  /** add one log entry (can be called in any thread, as doc.insertString() is thread safe) */
  private void addLog(final StyledDocument doc, final LogManager.LogEntry le) {
    boolean doDisplay;
    AttributeSet attr;
    switch (le.type) {
    case LOG_ERROR: doDisplay = true; attr = attrError; break;
    case LOG_INFO:  doDisplay = cbInfo.isSelected(); attr = attrInfo; break;
    case LOG_DEBUG: doDisplay = cbDebug.isSelected(); attr = attrDebug; break;
    case LOG_IO:    doDisplay = cbIo.isSelected(); attr = attrIo; break;
    default: doDisplay = false; attr = null;
    }
    if (doDisplay) {
      int oldLen = doc.getLength();
      try {
        doc.insertString(oldLen, getText(le), attr);
        jtLogs.select(doc.getLength(), doc.getLength());
      }
      catch (final BadLocationException ignore) {}
    }
  }
  
  /** format a log entry (timestamp + text) */
  private String getText(final LogManager.LogEntry le) {
    final Date dt = new Date(le.timestamp);
    return dfTimestamp.format(dt) + le.text +'\n';
  }
}

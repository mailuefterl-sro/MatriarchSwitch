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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

import com.mailuefterl.matriarch.parameter.Parameter;
import com.mailuefterl.matriarch.parameter.ParameterChoiceValue;
import com.mailuefterl.matriarch.parameter.ParameterGroup;
import com.mailuefterl.matriarch.parameter.ParameterRange;
import com.mailuefterl.matriarch.ui.ButtonPanel;
import com.mailuefterl.matriarch.ui.ConfirmOverwriteFileChooser;
import com.mailuefterl.matriarch.ui.LogPanel;
import com.mailuefterl.matriarch.ui.RangedNumberInputField;
import com.mailuefterl.matriarch.ui.TranslucentPanel;

/**
 * User interface for MatriarchSwitch application.
 */
public class MatSwitchUi {

  /** controller instance */
  private final MatSwitchController ctl;
  /** background worker Thread for non-UI tasks */
  private Thread backgroundWorker;
  /** work queue for backgroundWorker */
  private Queue<Runnable> backgroundWorkQueue;
  /** main window */
  private JFrame mainWindow;
  /** panel for MIDI controls */
  private JPanel pMidi;
  /** panel for parameter */
  private JPanel pParams;
  /** a glasspane that can be used to disable/enable all controls */
  private JPanel pDisablePanel;
  /** Combobox for Midi transmit interface */
  private JComboBox<MidiInterface> cbMidiOutPort;
  /** Combobox for Midi receive interface */
  private JComboBox<MidiInterface> cbMidiInPort;
  /** Combobox for Midi Channel / Device ID */
  private JComboBox<MatriarchUnit> cbMidiDevice;
  /** Button to refresh MIDI interfaces */
  private JButton bRefreshMidi;
  /** Combobox for Parameter Group */
  private JComboBox<ParameterGroup> cbParameterGroup;
  /** Combobox for Parameter */
  private JComboBox<Parameter> cbParameter;
  /** Combobox for Parameter value */
  private JComboBox<ParameterChoiceValue> cbParameterValue;
  /** Numeric input field for ranged parameter */
  private RangedNumberInputField tfParameterValue;
  /** text document for description of current parameter */
  private StyledDocument docParamDesc;
  /** button to retrieve all parameters from Matriarch */
  private JButton bRetrieve;
  /** button to store changed parameters to Matriarch */
  private JButton bStore;
  /** Panel for Log */
  private JPanel pLog;
  /** top menu bar */
  private JMenuBar mbMenu;
  /** file chooser for import/export */
  private JFileChooser fcExport;
  /** reference counter for showWait/hideWait */
  private int numWait;
  /** remembered focus component before showWait */
  private Component focusOwnerBeforeWait;
  
  
  /** constructor with Controller parent */
  public MatSwitchUi(final MatSwitchController ctl) {
    this.ctl = ctl;
    createWorker();
    SwingUtilities.invokeLater(() -> {
      setupUi();
    });
  }
  
  /** create the user interface components */
  private void setupUi() {
    mainWindow = new JFrame("MatriarchSwitch");
    mainWindow.setMinimumSize(new Dimension(640, 480));
    mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    setupMidiControls();
    setupParamControls();
    pLog = new LogPanel();
    setupMenu();
    
    final JSplitPane jsp1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    jsp1.setResizeWeight(0.1D);
    jsp1.setTopComponent(pMidi);
    jsp1.setBottomComponent(pParams);
    
    final JSplitPane jsp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    jsp2.setResizeWeight(0.2D);
    jsp2.setTopComponent(jsp1);
    jsp2.setBottomComponent(pLog);
    mainWindow.add(jsp2);
    mainWindow.setJMenuBar(mbMenu);
    
    pDisablePanel = new TranslucentPanel(new Color(100, 100, 100, 50));
    pDisablePanel.setVisible(false);
    pDisablePanel.setFocusTraversalKeysEnabled(false);
    pDisablePanel.addMouseListener(new MouseAdapter() {});
    mainWindow.setGlassPane(pDisablePanel);
    
    fcExport = new ConfirmOverwriteFileChooser();
        
    mainWindow.pack();
    mainWindow.validate();
    
    setupEventHandlers();
  }
  
  /** create user interface controls for midi */
  private void setupMidiControls() {
    pMidi = new JPanel();
    pMidi.setBorder(BorderFactory.createTitledBorder("MIDI settings"));
    pMidi.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);
    
    gbc.gridx = 1; gbc.gridy = 10;
    pMidi.add(new JLabel("MIDI OUT port", JLabel.RIGHT), gbc);
    gbc.gridx = 2;
    cbMidiOutPort = new JComboBox<MidiInterface>();
    pMidi.add(cbMidiOutPort, gbc);

    gbc.gridx = 1; gbc.gridy = 20;
    pMidi.add(new JLabel("MIDI IN port", JLabel.RIGHT), gbc);
    gbc.gridx = 2;
    cbMidiInPort = new JComboBox<MidiInterface>();
    pMidi.add(cbMidiInPort, gbc);
    
    gbc.gridx = 1; gbc.gridy = 30;
    pMidi.add(new JLabel("Unit", JLabel.RIGHT), gbc);
    gbc.gridx = 2;
    cbMidiDevice = new JComboBox<MatriarchUnit>();
    pMidi.add(cbMidiDevice, gbc);
    
    // to center the comboboxes above the text pane:
    gbc.gridx = 0; gbc.weightx = 10;
    pMidi.add(new JLabel(), gbc);
    gbc.gridx = 3; gbc.weightx = 10;
    pMidi.add(new JLabel(), gbc);

    gbc.gridx = 0; gbc.gridwidth = 4; gbc.gridy = 40;
    JPanel pButtons = new ButtonPanel();
    bRefreshMidi = new JButton("Refresh Midi Interfaces");
    bRefreshMidi.setToolTipText("Use this after a connection has been added or removed (e.g. Matriarch switched on)");
    pButtons.add(bRefreshMidi);
    pMidi.add(pButtons, gbc);
    pButtons.setVisible(false);
  }
  
  /** create user interface controls for parameters */
  private void setupParamControls() {
    pParams = new JPanel();
    pParams.setBorder(BorderFactory.createTitledBorder("Parameters"));
    pParams.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);

    gbc.gridx = 1; gbc.gridy = 30;
    pParams.add(new JLabel("Parameter group", JLabel.RIGHT), gbc);
    gbc.gridx = 2;
    cbParameterGroup = new JComboBox<ParameterGroup>();
    pParams.add(cbParameterGroup, gbc);
    
    gbc.gridx = 1; gbc.gridy = 40;
    pParams.add(new JLabel("Parameter", JLabel.RIGHT), gbc);
    gbc.gridx = 2;
    cbParameter = new JComboBox<Parameter>();
    pParams.add(cbParameter, gbc);
    
    gbc.gridx = 1; gbc.gridy = 50;
    pParams.add(new JLabel("Parameter value", JLabel.RIGHT), gbc);
    gbc.gridx = 2;
    cbParameterValue = new JComboBox<ParameterChoiceValue>();
    pParams.add(cbParameterValue, gbc);
    tfParameterValue = new RangedNumberInputField(10, 0, 0, 0);
    tfParameterValue.setVisible(false);
    pParams.add(tfParameterValue, gbc); // insert at same position, only one is visible anytime
    
    // to center the comboboxes above the text pane:
    gbc.gridx = 0; gbc.weightx = 10;
    pParams.add(new JLabel(), gbc);
    gbc.gridx = 3; gbc.weightx = 10;
    pParams.add(new JLabel(), gbc);
    
    // buttons
    gbc.gridx = 0; gbc.gridy = 80;
    gbc.gridwidth = 4;
    JPanel pButtons = new ButtonPanel();
    bRetrieve = new JButton("Retrieve Parameters");
    bRetrieve.setToolTipText("Retrieves all parameter values from Matriarch");
    pButtons.add(bRetrieve);
    bStore = new JButton("Store Parameters");
    bStore.setToolTipText("Sends all changed parameters to Matriarch");
    pButtons.add(bStore);
    pParams.add(pButtons, gbc);
    
    gbc.gridx = 0; gbc.gridy = 60;
    gbc.gridwidth = 4;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 10;
    docParamDesc = new DefaultStyledDocument();// HTMLDocument();
    final JTextPane jtParamDesc = new JTextPane(docParamDesc);
    jtParamDesc.setEditable(false);
    pParams.add(new JScrollPane(jtParamDesc), gbc);
  }
  
  /** create the menu and all items in it, including event handlers */
  private void setupMenu() {
    mbMenu = new JMenuBar();
    
    // File menu
    final JMenu mFile = new JMenu("File");
    mbMenu.add(mFile);
    final JMenuItem miExportChanged = new JMenuItem("Export changed parameters to Sysex file");
    miExportChanged.addActionListener(l -> exportSysex(false));
    mFile.add(miExportChanged);
    final JMenuItem miExportAll = new JMenuItem("Export ALL parameters to Sysex file");
    miExportAll.addActionListener(l -> exportSysex(true));
    mFile.add(miExportAll);
    final JMenuItem miImportSysex = new JMenuItem("Import parameters from Sysex file");
    miImportSysex.addActionListener(l -> importSysex());
    mFile.add(miImportSysex);
    
    // Tools menu
    final JMenu mTools = new JMenu("Tools");
    mbMenu.add(mTools);
    final JMenuItem miRefreshMidi = new JMenuItem("Refresh list of MIDI interfaces");
    miRefreshMidi.addActionListener(l -> fetchMidiInterfaces());
    mTools.add(miRefreshMidi);
    final JMenuItem miResetDefault = new JMenuItem("Reset all parameters to default");
    miResetDefault.addActionListener(l -> resetParamsDefault());
    mTools.add(miResetDefault);
    
    // Help menu
    final JMenu mHelp = new JMenu("Help");
    mbMenu.add(mHelp);
    final JMenuItem miHelpAbout = new JMenuItem("About");
    miHelpAbout.addActionListener(l -> helpAbout());
    mHelp.add(miHelpAbout);
  }
  
  /** create event handlers on the controls */
  private void setupEventHandlers() {
    cbMidiOutPort.addActionListener(l -> {
      showWait();
      MidiInterface midiInterface = getSelectedItem(cbMidiOutPort);
      backgroundWork(() -> {
        ctl.setMidiOutPort(midiInterface);
        fetchMidiUnits();
      });
    });
    
    cbMidiInPort.addActionListener(l -> {
      showWait();
      MidiInterface midiInterface = getSelectedItem(cbMidiInPort);
      backgroundWork(() -> {
        ctl.setMidiInPort(midiInterface);
        fetchMidiUnits();
      });
    });
    
    cbMidiDevice.addActionListener(l -> {
      ctl.setMatUnit(getSelectedItem(cbMidiDevice));
    });
    
    bRefreshMidi.addActionListener(l -> fetchMidiInterfaces());
    
    cbParameterGroup.addActionListener(l -> {
      ParameterGroup currentGroup = getSelectedItem(cbParameterGroup);
      setComboboxChoices(currentGroup.getParameters(), cbParameter, null);
    });
    
    cbParameter.addActionListener(l -> {
      Parameter currentParam = getSelectedItem(cbParameter);
      if (currentParam.isRange()) {
        ParameterRange ranger = currentParam.getRange();
        cbParameterValue.setVisible(false);
        tfParameterValue.setRange(ranger.rangeMin, ranger.rangeMax);
        tfParameterValue.setText(String.valueOf(currentParam.getCurrentValue().getNumber()));
        tfParameterValue.setVisible(true);
        tfParameterValue.requestFocusInWindow();
      } else {
        tfParameterValue.setVisible(false);
        setComboboxChoices(currentParam.getChoices(), cbParameterValue, currentParam.getCurrentValue().toString());
        cbParameterValue.setVisible(true);
      }
      setParamDesc(currentParam);
    });
    
    cbParameterValue.addActionListener(l-> {
      Parameter currentParam = getSelectedItem(cbParameter);
      ParameterChoiceValue currentValue = getSelectedItem(cbParameterValue);
      currentParam.setCurrentValue(currentValue);
    });
    
    tfParameterValue.addActionListener(l -> {
      Parameter currentParam = getSelectedItem(cbParameter);
      String sVal = tfParameterValue.getText();
      if (sVal.length() == 0) {
        sVal = "0";
        tfParameterValue.setText(sVal);
      }
      int iVal = 0;
      try {
        iVal = Integer.parseInt(sVal);
      }
      catch (final NumberFormatException ignore) {}
      currentParam.setCurrentValue(currentParam.findValue(iVal));      
    });
    
    setComboboxChoices(ParameterGroup.getAllGroups(), cbParameterGroup, "");
    
    bRetrieve.addActionListener(l -> retrieveParameters());    
    bStore.addActionListener(l -> storeParameters());
  }
  
  /** start the user interface */
  public void start() {
    SwingUtilities.invokeLater(() -> {
      mainWindow.setVisible(true);
    });
    fetchMidiInterfaces();
  }
  
  /** reload the currently selected parameter (show current param value) */
  private void reloadCurrentParameter() {
    final Parameter curParam = getSelectedItem(cbParameter);
    if (curParam.isRange()) {
      tfParameterValue.setText(String.valueOf(curParam.getCurrentValue().getNumber()));
    } else {
      cbParameterValue.setSelectedItem(curParam.getCurrentValue());
    }
  }
  
  /** populate comboxes with Midi OutPorts/InPorts */
  private void fetchMidiInterfaces() {
    SwingUtilities.invokeLater(() -> showWait());
    backgroundWork(() -> {
      List<MidiInterface> midiTx = ctl.fetchMidiOutPorts();
      List<MidiInterface> midiRx = ctl.fetchMidiInPorts();
      if (midiTx.isEmpty() && midiRx.isEmpty()) {
        fetchMidiUnits();
      }
      SwingUtilities.invokeLater(() -> {
        setComboboxChoices(midiTx, cbMidiOutPort, "Matriarch");
        setComboboxChoices(midiRx, cbMidiInPort, "Matriarch");
        hideWait();
      });
    });
  }
  
  /** Controller told us that our Midi connection failed. Re-fetch interfaces. */
  public void midiFailed() {
    fetchMidiInterfaces();
  }
  
  /** find available Matriarch units and populate combobox */
  private void fetchMidiUnits() {
    List<MatriarchUnit> availableDevices = ctl.fetchMidiUnits();
    SwingUtilities.invokeLater(() -> {
      cbMidiDevice.removeAllItems();
      setComboboxChoices(availableDevices, cbMidiDevice, "Matriarch");
      hideWait();
    });
  }
  
  /** Retrieve Button has been pressed (called in EDT) */
  private void retrieveParameters() {
    final List<Parameter> changedParams = ctl.getChangedParameters();
    if (changedParams.size() == 0) {
      reallyRetrieveParameters();
    } else {
      final StringBuilder sb = new StringBuilder("The following Parameters have been changed locally:");
      for (final Parameter param: changedParams) {
        sb.append("\n        ");
        sb.append(param.toString());
      }
      sb.append("\nDo you really want to refetch them from Matriarch");
      sb.append("\n(and lose local changes)?");
      if (JOptionPane.showConfirmDialog(
              mainWindow,
              sb.toString(),
              "Retrieving Parameters from Matriarch",
              JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.QUESTION_MESSAGE
              ) == JOptionPane.OK_OPTION) {
        reallyRetrieveParameters();
      }
    }
  }

  /** Do the real work to retrieve parameters (called in EDT) */
  private void reallyRetrieveParameters() {
    showWait();
    backgroundWork(() -> {
      ctl.retrieveAllParameters();
      SwingUtilities.invokeLater(() -> {
        reloadCurrentParameter();
        hideWait();
      });
    });
  }
  
  /** Store button has been pressed (called in EDT) */
  private void storeParameters() {
    final List<Parameter> changedParams = ctl.getChangedParameters();
    if (changedParams.size() == 0) {
      JOptionPane.showMessageDialog(
              mainWindow,
              "No parameters have been changed. Nothing to store.",
              "Storing Parameters to Matriarch",
              JOptionPane.INFORMATION_MESSAGE);
    } else {
      final StringBuilder sb = new StringBuilder("The following Parameters have been changed:");
      for (final Parameter param: changedParams) {
        sb.append("\n        ");
        sb.append(param.toString());
      }
      sb.append("\nDo you really want to send them to Matriarch?");
      if (JOptionPane.showConfirmDialog(
              mainWindow,
              sb.toString(),
              "Storing Parameters to Matriarch",
              JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.QUESTION_MESSAGE
              ) == JOptionPane.OK_OPTION) {
        reallyStoreParameters();
      }
    }
  }
  
  /** Do the real work to store parameters (called in EDT) */
  private void reallyStoreParameters() {
    showWait();
    backgroundWork(() -> {
      ctl.storeParameters();
      SwingUtilities.invokeLater(() -> hideWait());
    });
  }
  
  /** export parameters to sysex file */
  private void exportSysex(final boolean allParameters) {
    fcExport.setDialogTitle("Export "+ (allParameters ? "all" : "changed") +" Parameters");
    fcExport.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fcExport.resetChoosableFileFilters();
    fcExport.addChoosableFileFilter(new FileNameExtensionFilter("MIDI SysEx files", "syx"));
    fcExport.setAcceptAllFileFilterUsed(true);
    fcExport.setSelectedFile(new File("filename.syx"));
    if (fcExport.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
      File exportFile = fcExport.getSelectedFile();
      //if (!exportFile.getName().contains(".")) {
      //  exportFile = new File(exportFile, exportFile.getName() + ".syx");
      //}
      if (exportFile.exists()) {
        System.err.println("File exists");
      }
      showWait();
      backgroundWork(() -> {
        ctl.exportSysex(fcExport.getSelectedFile(), allParameters);
        SwingUtilities.invokeLater(() -> hideWait());
      });
    }
  }
  
  /** import parameters from sysex file */
  private void importSysex() {
    fcExport.setDialogTitle("Import Parameters");
    fcExport.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fcExport.resetChoosableFileFilters();
    fcExport.addChoosableFileFilter(new FileNameExtensionFilter("MIDI SysEx files", "syx"));
    fcExport.setAcceptAllFileFilterUsed(true);
    if (fcExport.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
      showWait();
      backgroundWork(() -> {
        ctl.importSysex(fcExport.getSelectedFile());
        SwingUtilities.invokeLater(() -> hideWait());
      });
    }
  }
  
  /** reset all parameters to their default value */
  private void resetParamsDefault() {
    ctl.resetParamsDefault();
    reloadCurrentParameter();
  }
  
  /** show Help/About information */
  private void helpAbout() {
    final StringBuilder sb = new StringBuilder();
    sb.append("MatriarchSwitch is a tool to manage Global Parameters");
    sb.append("\non the Moog Matriarch Synthesizer.");
    sb.append("\n\nProgram version: ");
    sb.append(MatProperty.PACKAGE_VERSION.getProperty("unknown"));
    sb.append("\nParameter file version: ");
    sb.append(ctl.getParametersVersion());
    sb.append("\nJava Runtime version: ");
    sb.append(System.getProperty("java.version"));
    JOptionPane.showMessageDialog(
            mainWindow,
            sb.toString(),
            "About MatriarchSwitch",
            JOptionPane.INFORMATION_MESSAGE);
  }
  
  /** show a wait cursor and disable all controls (background operation in progress). Must be called on EDT*/
  private synchronized void showWait() {
    if (numWait++ == 0) {
      mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      focusOwnerBeforeWait = mainWindow.getFocusOwner();
      pDisablePanel.setVisible(true);
      pDisablePanel.requestFocusInWindow();
    }
  }
  
  /** hide the wait cursor (if reference count == 0). Must be called on EDT */
  private synchronized void hideWait() {
    if (--numWait <= 0) {
      numWait = 0;
      pDisablePanel.setVisible(false);
      if (focusOwnerBeforeWait != null) {
        focusOwnerBeforeWait.requestFocusInWindow();
      }
      mainWindow.setCursor(Cursor.getDefaultCursor());
    }
  }
  
  /** replace the current combobox contents with new ones (leave current selection intact if possible) */
  private <T> void setComboboxChoices(final List<T> choices, final JComboBox<T> combox, final String preferredSelection) {
    MutableComboBoxModel<T> model = (MutableComboBoxModel<T>)combox.getModel();
    int oldSize = model.getSize();
    List<T> toDelete = new ArrayList<T>(oldSize);
    List<T> toAdd = new ArrayList<T>(choices);
    for (int i = 0; i < oldSize; i++) {
      T oe = model.getElementAt(i);
      if (toAdd.remove(oe) == false) {
        toDelete.add(oe);
      }
    }
    for (T ae: toAdd) {
      model.addElement(ae);
    }
    if ((oldSize == 0) || toDelete.contains(model.getSelectedItem())) {
      if (preferredSelection != null) {
        for (int i = 0; i < model.getSize(); i++) {
          T elem = model.getElementAt(i);
          if (elem.toString().contains(preferredSelection) && !toDelete.contains(elem)) {
            model.setSelectedItem(elem);
            break;
          }
        }
      }
    }
    for (T de: toDelete) {
      model.removeElement(de);
    }
  }
  
  /** replace the current text in docParamDesc with the description of given parameter */
  private void setParamDesc(final Parameter param) {
    try {
      docParamDesc.remove(0, docParamDesc.getLength());
      docParamDesc.insertString(0, param.description, null);
      //docParamDesc.replace(0, docParamDesc.getLength(), param.description, null);
    }
    catch (final BadLocationException ignore) {}
  }
  
  /** create worker thread for background (non-UI) tasks */
  private void createWorker() {
    backgroundWorkQueue = new ArrayDeque<Runnable>();
    backgroundWorker = new Thread(() -> { backgroundRunner(); }, "MatSwitchUi backgroundWorker");
    backgroundWorker.setDaemon(true);
    backgroundWorker.start();
  }
  
  /** run method for backgroundWorker thread */
  private void backgroundRunner() {
    Thread myself = Thread.currentThread();
    Runnable nextForrest;
    while (myself == backgroundWorker) {
      synchronized(backgroundWorkQueue) {
        nextForrest = backgroundWorkQueue.poll();
        if (nextForrest == null) {
          try {
            backgroundWorkQueue.wait();
          }
          catch (InterruptedException ignore) {}
        }
      }
      if (nextForrest != null) {
        nextForrest.run();
      }
    }
  }
  
  /** enqueue a background task for backgroundWorker */
  private void backgroundWork(final Runnable forrest) {
    synchronized(backgroundWorkQueue) {
      backgroundWorkQueue.add(forrest);
      backgroundWorkQueue.notify();
    }
  }
  
  /** helper: return the currently selected Item in a combobox in a type-safe way (Grrr!) */
  private static <T> T getSelectedItem(final JComboBox<T> combox) {
    return combox.getItemAt(combox.getSelectedIndex());
  }
}

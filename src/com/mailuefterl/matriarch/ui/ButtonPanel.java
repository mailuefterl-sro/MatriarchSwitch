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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * ButtonPanel is a simple Panel suitable to arrange buttons in a row, with
 * some space between them. It uses GridBagLayout and some empty JLabels
 * internally to tune the spacing.
 */
public class ButtonPanel extends JPanel {
  /** constraints for laying out components */
  private final GridBagConstraints gbc;
  
  /** constructor */
  public ButtonPanel() {
    super(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.weightx = 10D;
    add(new JLabel(), gbc);
    gbc.gridx = 1000;
    add(new JLabel(), gbc);
    gbc.gridx = 10;
    gbc.weightx = 1D;
  }
  
  /** add a component (button) to the layout */
  @Override
  public Component add(Component comp) {
    if (gbc.gridx > 10) {
      super.add(new JLabel(), gbc);
      gbc.gridx ++;
    }
    super.add(comp, gbc);
    gbc.gridx ++;
    return comp;
  }

}

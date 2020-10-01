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

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/**
 * A text field, constrained to numeric input within a given range
 */
public class RangedNumberInputField extends JTextField {
  /** our document filter */
  private NumericDocumentFilter filter;
  
  /** constructor */ 
  public RangedNumberInputField(final int width, final int minimum, final int maximum, final int current) {
    super(width);
    PlainDocument doc = new PlainDocument();
    filter = new NumericDocumentFilter(width);
    filter.setRange(minimum, maximum);
    doc.setDocumentFilter(filter);
    setDocument(doc);
    setText(String.valueOf(current));
  }
  
  /** change range min/max */
  public void setRange(final int min, final int max) {
    filter.setRange(min, max);
  }

  /** our implementation of a document filter that allows only integers within a given range */
  private class NumericDocumentFilter extends DocumentFilter {
    /** max number of characters (should be big enough for the given range) */
    private final int width;
    /** minimum allowed value */
    private int rangeMin;
    /** maximum allowed value */
    private int rangeMax;

    /** constructor */
    private NumericDocumentFilter(final int numChars) {
      width = numChars;
    }
    
    /** change range min/max */
    private void setRange(final int min, final int max) {
      rangeMin = min;
      rangeMax = max;
    }
    
    /** check whether the document contains numeric input within allowed range */
    private boolean checkValid(final String str) {
      boolean ret;
      try {
        int i = Integer.parseInt(str);
        ret = ((i >= rangeMin) && (i <= rangeMax));
      }
      catch (final NumberFormatException e) {
        ret = false;
      }
      return ret;
    }
    
    /** {@inheritDoc} */
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
      final Document doc = fb.getDocument();
      final StringBuilder sb = new StringBuilder(doc.getText(0, doc.getLength()));
      sb.delete(offset,  offset + length);
      if ((sb.length() == 0) || checkValid(sb.toString())) {
        super.remove(fb, offset, length);
      } else {
        Toolkit.getDefaultToolkit().beep();
      }
    }

    /** {@inheritDoc} */
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
      final Document doc = fb.getDocument();
      final StringBuilder sb = new StringBuilder(doc.getText(0, doc.getLength()));
      sb.insert(offset,  string);
      if (checkValid(sb.toString())) {
        super.insertString(fb, offset, string, attr);
      } else {
        Toolkit.getDefaultToolkit().beep();
      }
    }

    /** {@inheritDoc} */
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
      final Document doc = fb.getDocument();
      final StringBuilder sb = new StringBuilder(doc.getText(0, doc.getLength()));
      sb.replace(offset, offset + length, text);
      if (checkValid(sb.toString())) {
        super.replace(fb, offset, length, text, attrs);
      } else {
        Toolkit.getDefaultToolkit().beep();
      }
    }
    
  }
}

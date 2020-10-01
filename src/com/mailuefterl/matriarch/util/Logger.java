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

/**
 * Default implementation of a logger, instantiated in each user class.
 */
public class Logger implements ILogger {
  
  /** log manager doing the actual work */
  private final LogManager manager;
  
  /** Constructor, should only be used by LogManager */
  Logger(final LogManager mgr) {
    manager = mgr;
  }

  /** {@inheritDoc} */
  @Override
  public void error(Object... args) {
    manager.log(LogType.LOG_ERROR, args);
  }

  /** {@inheritDoc} */
  @Override
  public void info(Object... args) {
    manager.log(LogType.LOG_INFO, args);
  }

  /** {@inheritDoc} */
  @Override
  public void debug(Object... args) {
    manager.log(LogType.LOG_DEBUG, args);
  }

  /** {@inheritDoc} */
  @Override
  public void io(Object... args) {
    manager.log(LogType.LOG_IO, args);
  }

  /** {@inheritDoc} */
  @Override
  public void iohex(String prefix, byte[] data) {
    manager.logHex(LogType.LOG_IO, prefix, data);
  }

}

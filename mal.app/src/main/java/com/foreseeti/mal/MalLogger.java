/*
 * Copyright 2019 Foreseeti AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreseeti.mal;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MalLogger extends Logger {
  @SuppressWarnings("serial")
  private static class MalLevel extends Level {
    public static final Level DEBUG = new MalLevel("DEBUG", Level.SEVERE.intValue() + 1);
    public static final Level INFO = new MalLevel("INFO", Level.SEVERE.intValue() + 2);
    public static final Level WARNING = new MalLevel("WARNING", Level.SEVERE.intValue() + 3);
    public static final Level ERROR = new MalLevel("ERROR", Level.SEVERE.intValue() + 4);

    private MalLevel(String name, int value) {
      super(name, value);
    }
  }

  private static class MalFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
      StringBuffer sb = new StringBuffer();
      sb.append("[");
      sb.append(record.getLevel());
      sb.append("] ");
      sb.append(record.getMessage());
      sb.append("\n");
      return sb.toString();
    }
  }

  public MalLogger() {
    this(false, false);
  }

  public MalLogger(boolean verbose, boolean debug) {
    this(null, null);
    if (debug) {
      setLevel(MalLevel.DEBUG);
    } else if (verbose) {
      setLevel(MalLevel.INFO);
    } else {
      setLevel(MalLevel.WARNING);
    }
  }

  private MalLogger(String name, String resourceBundleName) {
    super(name, resourceBundleName);
    setUseParentHandlers(false);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setFormatter(new MalFormatter());
    addHandler(handler);
  }

  public void debug(Position pos, String msg) {
    debug(String.format("%s %s", pos.posString(), msg));
  }

  public void debug(String msg) {
    log(MalLevel.DEBUG, msg);
  }

  public void info(Position pos, String msg) {
    info(String.format("%s %s", pos.posString(), msg));
  }

  @Override
  public void info(String msg) {
    log(MalLevel.INFO, msg);
  }

  public void warning(Position pos, String msg) {
    warning(String.format("%s %s", pos.posString(), msg));
  }

  @Override
  public void warning(String msg) {
    log(MalLevel.WARNING, msg);
  }

  public void error(Position pos, String msg) {
    error(String.format("%s %s", pos.posString(), msg));
  }

  public void error(String msg) {
    log(MalLevel.ERROR, msg);
  }
}

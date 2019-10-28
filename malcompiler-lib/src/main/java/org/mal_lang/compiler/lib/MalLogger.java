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
package org.mal_lang.compiler.lib;

import java.util.Set;
import java.util.TreeSet;
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
      sb.append(printLevel(record.getLoggerName(), record.getLevel()));
      sb.append(" ");
      sb.append(record.getMessage());
      sb.append(String.format("%n"));
      return sb.toString();
    }

    private String printLevel(String loggerName, Level level) {
      String colorInit = "";
      String colorClear = "";

      if (System.console() != null) {
        switch (level.getName()) {
          case "ERROR":
            colorInit = "\u001B[1;31m";
            break;
          case "WARNING":
            colorInit = "\u001B[1;33m";
            break;
          case "INFO":
            colorInit = "\u001B[1;34m";
            break;
          case "DEBUG":
            colorInit = "\u001B[1;36m";
            break;
          default:
        }
        colorClear = "\u001B[m";
      }
      return String.format("[%s%s %s%s]", colorInit, loggerName, level.getName(), colorClear);
    }
  }

  private class LogMessage implements Comparable<LogMessage> {
    public final Level level;
    public final String message;

    public LogMessage(Level level, String message) {
      this.level = level;
      this.message = message;
    }

    @Override
    public int compareTo(LogMessage o) {
      if (o instanceof LogMessagePosition) {
        return -1;
      }
      int cmp = Integer.compare(this.level.intValue(), o.level.intValue());
      if (cmp != 0) {
        return cmp;
      }
      return this.message.compareTo(o.message);
    }

    @Override
    public String toString() {
      return message;
    }
  }

  private class LogMessagePosition extends LogMessage {
    public final Position position;

    public LogMessagePosition(Level level, String message, Position position) {
      super(level, message);
      this.position = position;
    }

    @Override
    public int compareTo(LogMessage o) {
      if (!(o instanceof LogMessagePosition)) {
        return 1;
      }
      var other = (LogMessagePosition) o;
      int cmp = this.position.compareTo(other.position);
      if (cmp != 0) {
        return cmp;
      }
      cmp = Integer.compare(this.level.intValue(), other.level.intValue());
      if (cmp != 0) {
        return cmp;
      }
      return this.message.compareTo(other.message);
    }

    @Override
    public String toString() {
      return String.format("%s %s", position.posString(), message);
    }
  }

  private boolean verbose;
  private boolean debug;
  private boolean isBuffered;

  private Set<LogMessage> logMessages = new TreeSet<>();

  public MalLogger(String name) {
    this(name, false, false);
  }

  public MalLogger(String name, boolean verbose, boolean debug) {
    this(name, verbose, debug, true);
  }

  public MalLogger(String name, boolean verbose, boolean debug, boolean isBuffered) {
    this(name, null);
    this.verbose = verbose;
    this.debug = debug;
    this.isBuffered = isBuffered;
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

  public boolean isVerbose() {
    return this.verbose;
  }

  public boolean isDebug() {
    return this.debug;
  }

  public boolean isBuffered() {
    return isBuffered;
  }

  private void log(LogMessage logMessage) {
    if (isBuffered) {
      logMessages.add(logMessage);
    } else {
      log(logMessage.level, logMessage.toString());
    }
  }

  public void debug(Position pos, String msg) {
    log(new LogMessagePosition(MalLevel.DEBUG, msg, pos));
  }

  public void debug(String msg) {
    log(new LogMessage(MalLevel.DEBUG, msg));
  }

  public void info(Position pos, String msg) {
    log(new LogMessagePosition(MalLevel.INFO, msg, pos));
  }

  @Override
  public void info(String msg) {
    log(new LogMessage(MalLevel.INFO, msg));
  }

  public void warning(Position pos, String msg) {
    log(new LogMessagePosition(MalLevel.WARNING, msg, pos));
  }

  @Override
  public void warning(String msg) {
    log(new LogMessage(MalLevel.WARNING, msg));
  }

  public void error(Position pos, String msg) {
    log(new LogMessagePosition(MalLevel.ERROR, msg, pos));
  }

  public void error(String msg) {
    log(new LogMessage(MalLevel.ERROR, msg));
  }

  public void print() {
    for (var logMessage : logMessages) {
      log(logMessage.level, logMessage.toString());
    }
  }
}

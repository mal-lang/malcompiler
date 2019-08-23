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
package org.mal_lang.mal.lib;

import java.io.File;
import java.util.Locale;

public abstract class Generator {
  protected final MalLogger LOGGER;

  protected Generator(boolean verbose, boolean debug) {
    Locale.setDefault(Locale.ROOT);
    LOGGER = new MalLogger("GENERATOR", verbose, debug, false);
  }

  protected Generator(MalLogger LOGGER) {
    this.LOGGER = LOGGER;
  }

  /** Name generator to avoid variable duplication. */
  protected static class Name {
    private static int value = 0;

    public static String get() {
      return String.format("_%s", Integer.toHexString(value++));
    }

    public static void reset() {
      value = 0;
    }
  }

  protected static boolean isEmpty(File dir) {
    for (File file : dir.listFiles()) {
      if (!file.getName().startsWith(".")) {
        return false;
      }
    }
    return true;
  }

  protected File getOutputDirectory(String path) throws CompilerException {
    var output = new File(path);
    if (!output.isAbsolute()) {
      throw error("Argument 'path' must be an absolute path");
    } else if (output.isFile()) {
      throw error("Argument 'path' is a file but must be an empty directory");
    } else if (output.isDirectory() && !isEmpty(output)) {
      throw error("Argument 'path' must be an empty directory");
    }
    return output;
  }

  protected CompilerException error() {
    return error(null);
  }

  protected CompilerException error(String msg) {
    if (msg != null) {
      LOGGER.error(msg);
    }
    return new CompilerException("There were generator errors");
  }
}

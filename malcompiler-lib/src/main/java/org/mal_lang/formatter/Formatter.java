/*
 * Copyright 2020 Foreseeti AB
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
package org.mal_lang.formatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.Lexer;
import org.mal_lang.compiler.lib.MalLogger;

/**
 * Code formatter for MAL. The layout algorithm is based on three simple combinators; stacking,
 * juxtaposing, and choice. The three combinators can be concatenated to produce virtually any
 * layout for the lowest cost.
 *
 * <p>Yelland, P. (2016). A New Approach to Optimal Code Formatting.
 */
public class Formatter {
  private static MalLogger LOGGER;

  public static String format(File file, Map<String, String> opts)
      throws IOException, CompilerException {
    boolean inplace = false;
    if (opts.containsKey("inplace")) {
      switch (opts.get("inplace").toLowerCase().strip()) {
        case "true":
          inplace = true;
          break;
        case "false":
          inplace = false;
          break;
        default:
          throw new CompilerException(
              "Optional argument 'inplace' must be either 'true' or 'false'");
      }
    }
    int margin = opts.containsKey("margin") ? Integer.parseInt(opts.get("margin")) : 100;
    if (margin < 0) {
      throw new CompilerException("Optional argument 'margin' must be a positive integer");
    }
    return format(file, margin, inplace);
  }

  public static String format(File file, int margin, boolean inplace)
      throws IOException, CompilerException {
    Locale.setDefault(Locale.ROOT);
    LOGGER = new MalLogger("FORMATTER", false, false);
    try {
      org.mal_lang.compiler.lib.Parser.parse(file);
    } catch (IOException e) {
      throw e;
    } catch (CompilerException e) {
      LOGGER.error("Code to be formatted must be syntactically valid");
      LOGGER.print();
      throw e;
    }
    var p = new Parser(file);
    p.parse();
    var output = p.getOutput(margin);
    output = output.replaceAll("(?m) +$", "");
    var bytes = output.getBytes();
    var tempFile = File.createTempFile("formatted", ".tmp");
    tempFile.deleteOnExit();
    try (var fos = new FileOutputStream(tempFile)) {
      fos.write(bytes);
      if (!Lexer.syntacticallyEqual(new Lexer(file), new Lexer(tempFile))) {
        throw new CompilerException(
            "The formatter has produced an AST that differs from the input.");
      }
    } catch (Exception e) {
      LOGGER.error("The formatter has produced an invalid AST. Please report this as a bug.");
      LOGGER.print();
      throw e;
    }
    if (inplace) {
      try (var fos = new FileOutputStream(file, false)) {
        fos.write(bytes);
      }
    } else {
      System.out.print(output);
    }
    return output;
  }
}

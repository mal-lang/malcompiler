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
package org.mal_lang.compiler.cli;

import static org.mal_lang.compiler.cli.CLIParser.HasArgument.NO_ARGUMENT;
import static org.mal_lang.compiler.cli.CLIParser.HasArgument.REQUIRED_ARGUMENT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.mal_lang.compiler.lib.AST;
import org.mal_lang.compiler.lib.Analyzer;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.lib.LangConverter;
import org.mal_lang.compiler.lib.Lexer;
import org.mal_lang.compiler.lib.MalInfo;
import org.mal_lang.compiler.lib.MalLogger;
import org.mal_lang.compiler.lib.Parser;
import org.mal_lang.compiler.lib.Token;
import org.mal_lang.compiler.lib.TokenType;
import org.mal_lang.formatter.Formatter;

public class Main {
  private static boolean useSGR = System.console() != null;

  private static class Options {
    public boolean lexer = false;
    public boolean parser = false;
    public boolean analyzer = false;
    public boolean formatter = false;
    public String target = "reference";
    public Map<String, String> args = new HashMap<>();
    public boolean verbose = false;
    public boolean debug = false;
  }

  private static Map<String, String> argsToMap(String args) {
    var map = new HashMap<String, String>();
    var arg = "";
    var rest = args.strip();
    do {
      // Fetch the first argument from a comma separated list
      var idx = rest.indexOf(',');
      if (idx == -1) {
        // This is the last argument
        arg = rest.strip();
        rest = "";
      } else {
        arg = rest.substring(0, idx).strip();
        rest = rest.substring(idx + 1).strip();
      }
      // Extract option and parameter from argument
      idx = arg.indexOf('=');
      var opt = "";
      var param = "";
      if (idx == -1) {
        // Argument has no parameter
        opt = arg.strip();
        param = "";
      } else {
        opt = arg.substring(0, idx).strip();
        param = arg.substring(idx + 1).strip();
      }
      if (!opt.isBlank()) {
        map.put(opt, param);
      }
    } while (!rest.isBlank());
    return map;
  }

  private static void printError(String error) {
    var sgrError = SGR.of(SGR.bold(SGR.fgRed("Error:")), " ", error);
    if (useSGR) {
      System.err.println(sgrError.getSGRString());
    } else {
      System.err.println(sgrError.getPlainString());
    }
  }

  private static void printHelp(CLIParser cli) {
    List<SGR> lines = new ArrayList<>();
    lines.add(
        SGR.of(
            SGR.bold("Usage:"),
            " malc [",
            SGR.italicized("OPTION"),
            "]... ",
            SGR.italicized("FILE")));
    lines.add(SGR.of());
    lines.add(SGR.bold("Options:"));
    lines.addAll(cli.getSGROptionLines());
    lines.add(SGR.of());
    lines.add(SGR.bold("Targets:"));
    lines.add(SGR.of("  reference [", SGR.italicized("default"), "]"));
    lines.add(SGR.of("  securicad"));
    lines.add(SGR.of("  format"));
    lines.add(SGR.of("  d3"));
    lines.add(SGR.of());
    lines.add(SGR.of(SGR.bold("Args:"), " [", SGR.italicized("reference"), "]"));
    lines.add(
        CLIParser.getSGROptionLine(
            SGR.of(SGR.fgRGB(135, 206, 235, "path"), "=", SGR.italicized("PATH")),
            "Write generated sources to PATH"));
    lines.add(
        CLIParser.getSGROptionLine(
            SGR.of(SGR.fgRGB(135, 206, 235, "package"), "=", SGR.italicized("PACKAGE")),
            String.format("Use PACKAGE as the package for the generated%nsources")));
    lines.add(
        CLIParser.getSGROptionLine(
            SGR.of(
                "[",
                SGR.fgRGB(135, 206, 235, "core"),
                "=",
                SGR.italicized(SGR.bold("true")),
                "|",
                SGR.italicized("false"),
                "]"),
            "Specifies if the core package should be generated"));
    lines.add(SGR.of());
    lines.add(SGR.of(SGR.bold("Args:"), " [", SGR.italicized("securicad"), "]"));
    lines.add(
        CLIParser.getSGROptionLine(
            SGR.of(SGR.fgRGB(135, 206, 235, "path"), "=", SGR.italicized("PATH")),
            "Write generated sources to PATH"));
    lines.add(
        CLIParser.getSGROptionLine(
            SGR.of(SGR.fgRGB(135, 206, 235, "package"), "=", SGR.italicized("PACKAGE")),
            String.format("Use PACKAGE as the package for the generated%nsources")));
    lines.add(
        CLIParser.getSGROptionLine(
            SGR.of("[", SGR.fgRGB(135, 206, 235, "icons"), "=", SGR.italicized("PATH"), "]"),
            "Icons are located at PATH"));
    lines.add(
        CLIParser.getSGROptionLine(
            SGR.of(
                "[",
                SGR.fgRGB(135, 206, 235, "mock"),
                "=",
                SGR.italicized("true"),
                "|",
                SGR.italicized(SGR.bold("false")),
                "]"),
            String.format("Specifies if mocked dependencies should be%ngenerated")));
    lines.add(
        CLIParser.getSGROptionLine(
            SGR.of(
                "[",
                SGR.fgRGB(135, 206, 235, "debug"),
                "=",
                SGR.italicized("true"),
                "|",
                SGR.italicized(SGR.bold("false")),
                "]"),
            "Specifies if debug steps should be kept"));
    lines.add(SGR.of());
    lines.add(SGR.of(SGR.bold("Args:"), " [", SGR.italicized("d3"), "]"));
    lines.add(SGR.of("  Not yet implemented"));
    lines.add(SGR.of());
    lines.add(SGR.of(SGR.bold("Args:"), " [", SGR.italicized("format"), "]"));
    lines.add(
        CLIParser.getSGROptionLine(
            SGR.of(SGR.fgRGB(135, 206, 235, "margin"), "=", SGR.italicized("MARGIN")),
            "Use MARGIN as maximum line width"));
    lines.add(
        CLIParser.getSGROptionLine(
            SGR.of(
                SGR.fgRGB(135, 206, 235, "inplace"),
                "=",
                SGR.italicized("true"),
                "|",
                SGR.italicized("false")),
            "Specifies if the formatter should format inplace"));
    if (useSGR) {
      for (var line : lines) {
        System.err.println(line.getSGRString());
      }
    } else {
      for (var line : lines) {
        System.err.println(line.getPlainString());
      }
    }
  }

  private static void printVersion() {
    try {
      var title = MalInfo.getTitle();
      var version = MalInfo.getVersion();
      System.err.println(String.format("%s %s", title, version));
    } catch (IOException e) {
      System.err.println(String.format("Error: %s", e.getMessage()));
    }
  }

  public static void main(String[] args) {
    Locale.setDefault(Locale.ROOT);

    // Parse command line arguments
    var cli = new CLIParser();
    int LEXER = cli.addOption('l', "lexer", NO_ARGUMENT, "Run the lexer and print the tokens");
    int PARSER = cli.addOption('p', "parser", NO_ARGUMENT, "Run the parser and print the AST");
    int ANALYZER =
        cli.addOption('a', "analyzer", NO_ARGUMENT, "Run the analyzer and print the results");
    int TARGET = cli.addOption('t', "target", REQUIRED_ARGUMENT, "TARGET", "Compilation target");
    int ARGS = cli.addOption("args", REQUIRED_ARGUMENT, "ARGS", "Code generation arguments");
    int VERBOSE = cli.addOption('v', "verbose", NO_ARGUMENT, "Print verbose output");
    int DEBUG = cli.addOption('d', "debug", NO_ARGUMENT, "Print debug output");
    int HELP = cli.addOption('h', "help", NO_ARGUMENT, "Print this help and exit");
    int VERSION = cli.addOption('V', "version", NO_ARGUMENT, "Print version information and exit");
    var cliArgs = cli.parse(args);
    var options = cliArgs.getOptions();
    var operands = cliArgs.getOperands();
    var opts = new Options();

    for (var opt : options) {
      int value = opt.getValue();
      if (value == -1) {
        printError(((CLIArguments.InvalidOption) opt).getError());
        printHelp(cli);
        System.exit(1);
      } else if (value == LEXER) {
        opts.lexer = true;
      } else if (value == PARSER) {
        opts.parser = true;
      } else if (value == ANALYZER) {
        opts.analyzer = true;
      } else if (value == TARGET) {
        opts.target = opt.getArgument();
      } else if (value == ARGS) {
        opts.args.putAll(argsToMap(opt.getArgument()));
      } else if (value == VERBOSE) {
        opts.verbose = true;
      } else if (value == DEBUG) {
        opts.debug = true;
      } else if (value == HELP) {
        printHelp(cli);
        System.exit(1);
      } else if (value == VERSION) {
        printVersion();
        System.exit(1);
      }
    }

    // Check if no file was supplied
    if (operands.isEmpty()) {
      printError("A file must be specified");
      printHelp(cli);
      System.exit(1);
    }

    // Check if multiple files were supplied
    if (operands.size() > 1) {
      printError("Only one file can be specified");
      printHelp(cli);
      System.exit(1);
    }

    var file = new File(operands.get(0));
    var LOGGER = new MalLogger("MAIN", opts.verbose, opts.debug);

    // Execute requested phase
    try {
      if (opts.lexer) {
        Lexer lexer = new Lexer(file, opts.verbose, opts.debug);
        Token token = lexer.next();
        while (token.type != TokenType.EOF) {
          System.out.println(token.toString());
          token = lexer.next();
        }
      } else if (opts.parser) {
        AST ast = Parser.parse(file, opts.verbose, opts.debug);
        System.out.print(ast.toString());
      } else if (opts.analyzer) {
        Analyzer.analyze(Parser.parse(file), opts.verbose, opts.debug);
      } else if (opts.target.equals("format")) {
        Formatter.prettyPrint(file, opts.args);
      } else if (opts.target.equals("reference")) {
        AST ast = Parser.parse(file);
        Analyzer.analyze(ast);
        Lang lang = LangConverter.convert(ast);
        org.mal_lang.compiler.lib.reference.Generator.generate(
            lang, opts.args, opts.verbose, opts.debug);
      } else if (opts.target.equals("securicad")) {
        AST ast = Parser.parse(file);
        Analyzer.analyze(ast);
        Lang lang = LangConverter.convert(ast);
        org.mal_lang.compiler.lib.securicad.Generator.generate(
            lang, opts.args, opts.verbose, opts.debug);
      } else if (opts.target.equals("d3")) {
        throw new CompilerException("Target 'd3' not yet implemented");
      } else {
        throw new CompilerException(String.format("Invalid compilation target %s", opts.target));
      }
    } catch (IOException | CompilerException e) {
      var msg = e.getMessage();
      if (msg != null && !msg.isBlank()) {
        LOGGER.error(e.getMessage());
      }
      LOGGER.print();
      System.exit(1);
    }
  }
}

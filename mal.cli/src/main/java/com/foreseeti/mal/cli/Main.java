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
package com.foreseeti.mal.cli;

import com.foreseeti.mal.lib.AST;
import com.foreseeti.mal.lib.Analyzer;
import com.foreseeti.mal.lib.CompilerException;
import com.foreseeti.mal.lib.Lang;
import com.foreseeti.mal.lib.LangConverter;
import com.foreseeti.mal.lib.Lexer;
import com.foreseeti.mal.lib.MalInfo;
import com.foreseeti.mal.lib.MalLogger;
import com.foreseeti.mal.lib.Parser;
import com.foreseeti.mal.lib.Token;
import com.foreseeti.mal.lib.TokenType;
import com.foreseeti.mal.lib.generator.ReferenceGenerator;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

public class Main {
  @Command(name = "mal")
  private static class Options {
    @Parameters(paramLabel = "FILE", description = "MAL specification to compile")
    public List<File> files;

    @Option(
        names = {"-l", "--lexer"},
        description = "Run the lexer and print the tokens")
    public boolean lexer = false;

    @Option(
        names = {"-p", "--parser"},
        description = "Run the parser and print the AST")
    public boolean parser = false;

    @Option(
        names = {"-a", "--analyzer"},
        description = "Run the analyzer and print the results")
    public boolean analyzer = false;

    @Option(
        names = {"-t", "--target"},
        paramLabel = "TARGET",
        description = "Compilation target")
    public String target = "reference";

    @Option(
        names = {"--args"},
        paramLabel = "ARGS",
        description = "Code generation arguments")
    public String args = "";

    @Option(
        names = {"-v", "--verbose"},
        description = "Print verbose output")
    public boolean verbose = false;

    @Option(
        names = {"-d", "--debug"},
        description = "Print debug output")
    public boolean debug = false;

    @Option(
        names = {"-h", "--help"},
        usageHelp = true,
        description = "Print this help and exit")
    public boolean help = false;

    @Option(
        names = {"-V", "--version"},
        versionHelp = true,
        description = "Print version information and exit")
    public boolean version = false;
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
    var opts = new Options();
    var cli = new CommandLine(opts);
    var err = false;
    String msg = null;

    try {
      cli.parse(args);
    } catch (ParameterException e) {
      err = true;
      msg = e.getMessage();
    }

    // Check if help was requested
    if (cli.isUsageHelpRequested()) {
      cli.usage(System.err);
      System.exit(1);
    }

    // Check if version was requested
    if (cli.isVersionHelpRequested()) {
      printVersion();
      System.exit(1);
    }

    // Check if command line arguments had errors
    if (err) {
      System.err.println(msg);
      cli.usage(System.err);
      System.exit(1);
    }

    // Check if no file was supplied
    if (opts.files == null || opts.files.size() == 0) {
      System.err.println("A file must be specified");
      cli.usage(System.err);
      System.exit(1);
    }

    // Check if multiple files were supplied
    if (opts.files.size() > 1) {
      System.err.println("Only one file can be specified");
      cli.usage(System.err);
      System.exit(1);
    }

    Map<String, String> argmap = new HashMap<>();
    if (!opts.args.isBlank()) {
      argmap = argsToMap(opts.args);
    }

    var file = opts.files.get(0);
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
      } else if (opts.target.equals("reference")) {
        AST ast = Parser.parse(file);
        Analyzer.analyze(ast);
        Lang lang = LangConverter.convert(ast);
        ReferenceGenerator.generate(lang, argmap, opts.verbose, opts.debug);
      } else if (opts.target.equals("securicad")) {
        throw new CompilerException("Target 'securicad' not yet implemented");
      } else if (opts.target.equals("d3")) {
        throw new CompilerException("Target 'd3' not yet implemented");
      } else {
        throw new CompilerException(String.format("Invalid compilation target %s", opts.target));
      }
    } catch (IOException | CompilerException e) {
      msg = e.getMessage();
      if (msg != null && !msg.isBlank()) {
        LOGGER.error(e.getMessage());
      }
      System.exit(1);
    }
  }
}

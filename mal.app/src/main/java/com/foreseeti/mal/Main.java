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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

public class Main {
  private static final Logger LOGGER = Logger.getGlobal();

  @Command(name = "com.foreseeti.mal.Main")
  private static class Options {
    @Parameters(paramLabel = "FILE",
        description = "MAL specification to compile")
    public File file;
    @Option(names = {"-l", "--lexer"},
        description = "Run the lexer and print the tokens")
    public boolean lexer = false;
    @Option(names = {"-p", "--parser"},
        description = "Run the parser and print the AST")
    public boolean parser = false;
    @Option(names = {"-a", "--analyzer"},
        description = "Run the analyzer and print the results")
    public boolean analyzer = false;
    @Option(names = {"-t", "--target"},
        paramLabel = "TARGET",
        description = "Compilation target")
    public String target = "reference";
    @Option(names = {"-v", "--verbose"},
        description = "Print verbose output")
    public boolean verbose = false;
    @Option(names = {"-d", "--debug"},
        description = "Print debug output")
    public boolean debug = false;
    @Option(names = {"-h", "--help"},
        usageHelp = true,
        description = "Print this help and exit")
    public boolean help = false;
    @Option(names = {"-V", "--version"},
        versionHelp = true,
        description = "Print version information and exit")
    public boolean version = false;
  }

  private static void printVersion() {
    Enumeration<URL> resources = null;
    try {
      resources = Main.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
    } catch (IOException e) {
      System.err.println(e.getMessage());
      return;
    }

    while (resources.hasMoreElements()) {
      Manifest manifest = null;
      try {
        manifest = new Manifest(resources.nextElement().openStream());
      } catch (IOException e) {
        continue;
      }

      var attributes = manifest.getMainAttributes();
      String title = null;
      String version = null;
      try {
        var pkg = attributes.getValue("Package");
        if (pkg == null || !pkg.equals("com.foreseeti.mal")) {
          continue;
        }

        title = attributes.getValue("Implementation-Title");
        version = attributes.getValue("Implementation-Version");
      } catch (IllegalArgumentException e) {
        continue;
      }

      System.err.println(String.format("%s %s", title, version));
      return;
    }

    System.err.println("Error: Couldn't find version information");
  }

  public static void main(String[] args) {
    var opts = new Options();
    var cli = new CommandLine(opts);
    var err = false;
    var msg = "";

    try {
      cli.parse(args);
    } catch (ParameterException e) {
      err = true;
      msg = e.getMessage();
    }

    if (cli.isUsageHelpRequested()) {
      cli.usage(System.err);
      System.exit(1);
    }

    if (cli.isVersionHelpRequested()) {
      printVersion();
      System.exit(1);
    }

    if (err) {
      System.err.println(msg);
      cli.usage(System.err);
      System.exit(1);
    }

    if (opts.lexer) {
      try {
        Lexer lexer = new Lexer(opts.file);
        Token t = lexer.next();
        while (t.type != TokenType.EOF) {
          System.out.printf("%s:%d:%d %s\n", t.type.toString(), t.line, t.col, t.stringValue);
          t = lexer.next();
        }
      } catch (Exception e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
    } else if (opts.parser) {
      try {
        var parser = new Parser(opts.file);
        var ast = parser.parse();
        System.out.print(ast.toString());
      } catch (IOException | SyntaxError e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
    } else if (opts.analyzer) {
      try {
        var parser = new Parser(opts.file);
        var ast = parser.parse();
        var a = new Analyzer(ast, opts.verbose, opts.debug);
        a.analyze();
        System.out.println("done");
      } catch (IOException | SyntaxError | SemanticError e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
    } else if (opts.target.equals("reference")) {
      System.err.println("Not yet implemented");
      System.exit(1);
    } else if (opts.target.equals("securicad")) {
      System.err.println("Not yet implemented");
      System.exit(1);
    } else if (opts.target.equals("d3")) {
      System.err.println("Not yet implemented");
      System.exit(1);
    } else {
      System.err.println(String.format("Error: Invalid compilation target %s", opts.target));
      System.exit(1);
    }
  }
}

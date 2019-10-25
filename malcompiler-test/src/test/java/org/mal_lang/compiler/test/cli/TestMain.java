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
package org.mal_lang.compiler.test.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.cli.Main;
import org.mal_lang.compiler.lib.MalInfo;
import org.mal_lang.compiler.test.MalTest;

public class TestMain extends MalTest {
  private static final String helpMsg = "Usage: mal";
  private static final String versionMsg = "%s %s";
  private static final String invalidOptMsg = "Error: Invalid option %s";
  private static final String missingArgMsg = "Error: Option %s requires an argument";
  private static final String superfluousArgMsg = "Error: Option %s doesn't allow an argument";
  private static final String missingFileMsg = "Error: A file must be specified";
  private static final String multipleFilesMsg = "Error: Only one file can be specified";

  private void assertFails(String test, String args[], String startMsg) {
    try {
      resetTestSystem();
      Main.main(args);
      fail(String.format("%s should exit with status code 1", test));
    } catch (ExitSecurityException e) {
      assertEquals(1, e.getStatus());
      assertEmptyOut();
      assertTrue(getPlainErr().startsWith(startMsg));
    }
  }

  private void assertHelp(String[] args) {
    assertFails("Help", args, helpMsg);
  }

  private void assertVersion(String[] args) {
    // Fetch "Implementation-Title" from manifest
    String title = null;
    try {
      title = MalInfo.getTitle();
    } catch (IOException e) {
      fail(e.getMessage());
    }
    assertNotNull(title);
    assertEquals("MAL Compiler", title);

    // Fetch "Implementation-Version" from manifest
    String version = null;
    try {
      version = MalInfo.getVersion();
    } catch (IOException e) {
      fail(e.getMessage());
    }
    assertNotNull(version);
    assertTrue(version.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"));

    assertFails("Version", args, String.format(versionMsg, title, version));
  }

  private void assertInvalidOpt(String[] args, String arg) {
    assertFails("Invalid option", args, String.format(invalidOptMsg, arg));
  }

  private void assertMissingArg(String[] args, String arg) {
    assertFails("Missing argument", args, String.format(missingArgMsg, arg));
  }

  private void assertSuperfluousArg(String[] args, String arg) {
    assertFails("Superfluous argument", args, String.format(superfluousArgMsg, arg));
  }

  private void assertMissingFile(String[] args) {
    assertFails("Missing file", args, missingFileMsg);
  }

  private void assertMultipleFiles(String[] args) {
    assertFails("Multiple files", args, multipleFilesMsg);
  }

  @Test
  public void testHelp() {
    // Only help
    assertHelp(new String[] {"-h"});
    assertHelp(new String[] {"--help"});
    // Help followed by invalid option
    assertHelp(new String[] {"-h", "-i"});
    assertHelp(new String[] {"--help", "--invalid"});
    assertHelp(new String[] {"--help", "--invalid=arg"});
    // Valid option followed by help
    assertHelp(new String[] {"-l", "-h"});
    assertHelp(new String[] {"--lexer", "--help"});
    // File followed by help
    assertHelp(new String[] {"file", "-h"});
    assertHelp(new String[] {"file", "--help"});
  }

  @Test
  public void testVersion() {
    // Only version
    assertVersion(new String[] {"-V"});
    assertVersion(new String[] {"--version"});
    // Version followed by invalid option
    assertVersion(new String[] {"-V", "-i"});
    assertVersion(new String[] {"--version", "--invalid"});
    assertVersion(new String[] {"--version", "--invalid=arg"});
    // Valid option followed by version
    assertVersion(new String[] {"-p", "-V"});
    assertVersion(new String[] {"--parser", "--version"});
    // File followed by version
    assertVersion(new String[] {"file", "-V"});
    assertVersion(new String[] {"file", "--version"});
  }

  @Test
  public void testInvalidOpt() {
    // Only invalid option
    assertInvalidOpt(new String[] {"-i"}, "-i");
    assertInvalidOpt(new String[] {"--invalid"}, "--invalid");
    assertInvalidOpt(new String[] {"--invalid=arg"}, "--invalid");
    // Invalid option followed by missing argument
    assertInvalidOpt(new String[] {"-i", "-t"}, "-i");
    assertInvalidOpt(new String[] {"--invalid", "--target"}, "--invalid");
    assertInvalidOpt(new String[] {"--invalid=arg", "--args"}, "--invalid");
    // Valid option followed by invalid option
    assertInvalidOpt(new String[] {"-a", "-i"}, "-i");
    assertInvalidOpt(new String[] {"--analyzer", "--invalid"}, "--invalid");
    assertInvalidOpt(new String[] {"--analyzer", "--invalid=arg"}, "--invalid");
    // File followed by invalid option
    assertInvalidOpt(new String[] {"file", "-i"}, "-i");
    assertInvalidOpt(new String[] {"file", "--invalid"}, "--invalid");
    assertInvalidOpt(new String[] {"file", "--invalid=arg"}, "--invalid");
  }

  @Test
  public void testMissingArg() {
    // Only missing argument
    assertMissingArg(new String[] {"-t"}, "-t");
    assertMissingArg(new String[] {"--target"}, "--target");
    assertMissingArg(new String[] {"--args"}, "--args");
    // Valid option followed by missing argument
    assertMissingArg(new String[] {"-v", "-t"}, "-t");
    assertMissingArg(new String[] {"--verbose", "--target"}, "--target");
    assertMissingArg(new String[] {"--verbose", "--args"}, "--args");
    // File followed by missing argument
    assertMissingArg(new String[] {"file", "-t"}, "-t");
    assertMissingArg(new String[] {"file", "--target"}, "--target");
    assertMissingArg(new String[] {"file", "--args"}, "--args");
  }

  @Test
  public void testSuperfluousArg() {
    // Only superfluous argument
    assertSuperfluousArg(new String[] {"--verbose=arg"}, "--verbose");
    assertSuperfluousArg(new String[] {"--debug=arg"}, "--debug");
    // Superfluous argument followed by help
    assertSuperfluousArg(new String[] {"--verbose=arg", "-h"}, "--verbose");
    assertSuperfluousArg(new String[] {"--debug=arg", "--help"}, "--debug");
    // Valid option followed by superfluous argument
    assertSuperfluousArg(new String[] {"-d", "--verbose=arg"}, "--verbose");
    assertSuperfluousArg(new String[] {"--debug", "--debug=arg"}, "--debug");
    // File followed by superfluous argument
    assertSuperfluousArg(new String[] {"file", "--verbose=arg"}, "--verbose");
    assertSuperfluousArg(new String[] {"file", "--debug=arg"}, "--debug");
  }

  @Test
  public void testMissingFile() {
    // Only missing file
    assertMissingFile(new String[] {});
    // Missing file and valid option
    assertMissingFile(new String[] {"-l"});
    assertMissingFile(new String[] {"--lexer"});
  }

  @Test
  public void testMultipleFiles() {
    // Only multiple files
    assertMultipleFiles(new String[] {"file1", "file2"});
    // Multiple files and valid option
    assertMultipleFiles(new String[] {"-p", "file1", "file2"});
    assertMultipleFiles(new String[] {"--parser", "file1", "file2"});
  }

  private void assertPhase(String phase, String[] args, String outFile, String errFile) {
    try {
      resetTestSystem();
      Main.main(args);
      assertOutLinesFile(
          outFile == null ? null : assertGetFileClassPath(outFile).getAbsolutePath());
      assertErrLinesFile(
          errFile == null ? null : assertGetFileClassPath(errFile).getAbsolutePath());
    } catch (ExitSecurityException e) {
      fail(
          String.format(
              "Phase '%s' exited with status code %d%n%s", phase, e.getStatus(), getErr()));
    }
  }

  @Test
  public void testPhases() {
    var inputFile = assertGetFileClassPath("analyzer/complex.mal").getAbsolutePath();
    // Test lexer
    assertPhase("lexer", new String[] {"--lexer", inputFile}, "analyzer/complex-lexer.txt", null);
    // Test lexer with verbose
    assertPhase(
        "lexer",
        new String[] {"--lexer", "--verbose", inputFile},
        "analyzer/complex-lexer.txt",
        "analyzer/complex-lexer-verbose.txt");
    // Test lexer with debug
    assertPhase(
        "lexer",
        new String[] {"--lexer", "--debug", inputFile},
        "analyzer/complex-lexer.txt",
        "analyzer/complex-lexer-debug.txt");
    // Test parser
    assertPhase(
        "parser", new String[] {"--parser", inputFile}, "analyzer/complex-parser.txt", null);
    // Test parser with verbose
    assertPhase(
        "parser",
        new String[] {"--parser", "--verbose", inputFile},
        "analyzer/complex-parser.txt",
        "analyzer/complex-parser-verbose.txt");
    // Test parser with debug
    assertPhase(
        "parser",
        new String[] {"--parser", "--debug", inputFile},
        "analyzer/complex-parser.txt",
        "analyzer/complex-parser-debug.txt");
    // Test analyzer
    assertPhase(
        "analyzer",
        new String[] {"--analyzer", inputFile},
        null,
        "analyzer/complex-analyzer-warnings.txt");
    // Test analyzer with verbose
    assertPhase(
        "analyzer",
        new String[] {"--analyzer", "--verbose", inputFile},
        null,
        "analyzer/complex-analyzer-verbose.txt");
    // Test analyzer with debug
    assertPhase(
        "analyzer",
        new String[] {"--analyzer", "--debug", inputFile},
        null,
        "analyzer/complex-analyzer-debug.txt");
    // TODO: Test generators (including invalid)
  }
}

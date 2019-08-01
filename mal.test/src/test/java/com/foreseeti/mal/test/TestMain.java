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
package com.foreseeti.mal.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.foreseeti.mal.cli.Main;
import com.foreseeti.mal.lib.MalInfo;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class TestMain extends MalTest {
  private static final String helpMsg = "Usage: mal";
  private static final String versionMsg = "%s %s";
  private static final String unknownArgMsg = "Unknown option: %s";
  private static final String missingArgMsg = "Missing required parameter for option '%s' (%s)";
  private static final String missingFileMsg = "A file must be specified";
  private static final String multipleFilesMsg = "Only one file can be specified";

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

  private void assertUnknownArg(String[] args, String arg) {
    assertFails("Unknown argument", args, String.format(unknownArgMsg, arg));
  }

  private void assertMissingArg(String[] args, String arg, String param) {
    assertFails("Missing argument", args, String.format(missingArgMsg, arg, param));
  }

  private void assertMissingFile(String[] args) {
    assertFails("Missing file", args, missingFileMsg);
  }

  private void assertMultipleFiles(String[] args) {
    assertFails("Multiple files", args, multipleFilesMsg);
  }

  @Test
  public void testHelp() {
    // Help should be printed if requested
    assertHelp(new String[] {"-h", "-v", "file"});
    assertHelp(new String[] {"--help", "-v", "file"});
    // Help takes precedence over version
    assertHelp(new String[] {"-h", "-V", "-v", "file"});
    assertHelp(new String[] {"--help", "-V", "-v", "file"});
    // Help takes precedence over unknown argument
    assertHelp(new String[] {"-h", "-u", "-v", "file"});
    assertHelp(new String[] {"--help", "-u", "-v", "file"});
    // Help takes precedence over missing argument
    assertHelp(new String[] {"-h", "-v", "file", "-t"});
    assertHelp(new String[] {"--help", "-v", "file", "-t"});
    // Help takes precedence over missing file
    assertHelp(new String[] {"-h", "-v"});
    assertHelp(new String[] {"--help", "-v"});
    // Help takes precedence over multiple files
    assertHelp(new String[] {"-h", "-v", "file1", "file2"});
    assertHelp(new String[] {"--help", "-v", "file1", "file2"});
  }

  @Test
  public void testVersion() {
    // Version should be printed if requested
    assertVersion(new String[] {"-V", "-v", "file"});
    assertVersion(new String[] {"--version", "-v", "file"});
    // Version takes precedence over unknown argument
    assertVersion(new String[] {"-V", "-u", "-v", "file"});
    assertVersion(new String[] {"--version", "-u", "-v", "file"});
    // Version takes precedence over missing argument
    assertVersion(new String[] {"-V", "-v", "file", "-t"});
    assertVersion(new String[] {"--version", "-v", "file", "-t"});
    // Version takes precedence over missing file
    assertVersion(new String[] {"-V", "-v"});
    assertVersion(new String[] {"--version", "-v"});
    // Version takes precedence over multiple files
    assertVersion(new String[] {"-V", "-v", "file1", "file2"});
    assertVersion(new String[] {"--version", "-v", "file1", "file2"});
  }

  @Test
  public void testUnknownArg() {
    // Unknown argument should be printed if present
    assertUnknownArg(new String[] {"-u", "-v", "file"}, "-u");
    assertUnknownArg(new String[] {"--unknown", "-v", "file"}, "--unknown");
    // Unknown argument takes precedence over missing file
    assertUnknownArg(new String[] {"-u", "-v"}, "-u");
    assertUnknownArg(new String[] {"--unknown", "-v"}, "--unknown");
    // Unknown argument takes precedence over multiple files
    assertUnknownArg(new String[] {"-u", "-v", "file1", "file2"}, "-u");
    assertUnknownArg(new String[] {"--unknown", "-v", "file1", "file2"}, "--unknown");
  }

  @Test
  public void testMissingArg() {
    // Missing argument should be printed if present
    assertMissingArg(new String[] {"-v", "file", "-t"}, "--target", "TARGET");
    assertMissingArg(new String[] {"-v", "file", "--target"}, "--target", "TARGET");
    // Missing argument takes precedence over missing file
    assertMissingArg(new String[] {"-v", "-t"}, "--target", "TARGET");
    assertMissingArg(new String[] {"-v", "--target"}, "--target", "TARGET");
    // Missing argument takes precedence over multiple files
    assertMissingArg(new String[] {"-v", "file1", "file2", "-t"}, "--target", "TARGET");
    assertMissingArg(new String[] {"-v", "file1", "file2", "--target"}, "--target", "TARGET");
  }

  @Test
  public void testMissingFile() {
    assertMissingFile(new String[] {});
    assertMissingFile(new String[] {"-v"});
  }

  @Test
  public void testMultipleFiles() {
    assertMultipleFiles(new String[] {"file1", "file2"});
    assertMultipleFiles(new String[] {"-v", "file1", "file2"});
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
              "Phase '%s' exited with status code %d\n%s", phase, e.getStatus(), getErr()));
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
  }
}

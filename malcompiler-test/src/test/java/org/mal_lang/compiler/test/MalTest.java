/*
 * Copyright 2019-2022 Foreseeti AB
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
package org.mal_lang.compiler.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class MalTest {
  public static final String fileSep = System.getProperty("file.separator");
  public static final String lineSep = System.getProperty("line.separator");

  @SuppressWarnings("serial")
  public static class ExitSecurityException extends SecurityException {
    private int status;

    public ExitSecurityException(int status) {
      this.status = status;
    }

    public int getStatus() {
      return this.status;
    }
  }

  private class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkExit(int status) {
      throw new ExitSecurityException(status);
    }

    @Override
    public void checkPermission(Permission perm) {}
  }

  private SecurityManager s = new NoExitSecurityManager();
  private SecurityManager oldS = System.getSecurityManager();
  private ByteArrayOutputStream out;
  private ByteArrayOutputStream err;
  private PrintStream outStream;
  private PrintStream errStream;
  private PrintStream oldOut = System.out;
  private PrintStream oldErr = System.err;
  private List<File> tmpDirs = new ArrayList<>();

  private void initTestSystem() {
    System.setSecurityManager(s);
    out = new ByteArrayOutputStream();
    err = new ByteArrayOutputStream();
    outStream = new PrintStream(out);
    errStream = new PrintStream(err);
    System.setOut(outStream);
    System.setErr(errStream);
  }

  private void clearTestSystem() {
    System.setSecurityManager(oldS);
    out = null;
    err = null;
    if (outStream != null) {
      outStream.close();
    }
    if (errStream != null) {
      errStream.close();
    }
    outStream = null;
    errStream = null;
    System.setOut(oldOut);
    System.setErr(oldErr);
  }

  protected void resetTestSystem() {
    clearTestSystem();
    initTestSystem();
  }

  private static void deleteRecursive(File file) {
    if (file.isDirectory()) {
      for (var subFile : file.listFiles()) {
        deleteRecursive(subFile);
      }
    }
    file.delete();
  }

  @BeforeEach
  public void init() {
    initTestSystem();
  }

  @AfterEach
  public void tearDown() {
    clearTestSystem();
    deleteTmpDirs();
  }

  protected String getOut() {
    return out.toString();
  }

  protected String getErr() {
    return err.toString();
  }

  protected String getPlainOut() {
    return getOut().replaceAll("\u001B\\[[;\\d]*m", "");
  }

  protected String getPlainErr() {
    return getErr().replaceAll("\u001B\\[[;\\d]*m", "");
  }

  private static void assertEmptyStream(String streamname, String stream) {
    assertTrue(
        stream.isEmpty(), String.format("%s should be empty, found:%n%s", streamname, stream));
  }

  protected void assertEmptyOut() {
    assertEmptyStream("stdout", getOut());
  }

  protected void assertEmptyErr() {
    assertEmptyStream("stderr", getErr());
  }

  private static void assertStreamLines(String[] lines, String streamname, String stream) {
    if (lines == null) {
      assertEmptyStream(streamname, stream);
    } else {
      var streamLines = stream.split("\\R", -1);
      var len = Math.min(lines.length, streamLines.length);
      for (int i = 0; i < len; i++) {
        assertEquals(
            lines[i], streamLines[i], String.format("Invalid line %d in %s", i + 1, streamname));
      }
      assertEquals(
          lines.length,
          streamLines.length,
          String.format("Invalid number of lines in %s", streamname));
    }
  }

  private static void assertStreamLinesFile(String filename, String streamname, String stream) {
    try {
      if (filename == null) {
        assertEmptyStream(streamname, stream);
      } else {
        var fileLines = Files.readString(Path.of(filename)).split("\\R", -1);
        var streamLines = stream.split("\\R", -1);
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        var len = Math.min(fileLines.length, streamLines.length);
        for (int i = 0; i < len; i++) {
          assertEquals(
              fileLines[i],
              streamLines[i],
              String.format("Invalid line %d in %s (%s)", i + 1, streamname, filename));
        }
        assertEquals(
            fileLines.length,
            streamLines.length,
            String.format("Invalid number of lines in %s (%s)", streamname, filename));
      }
    } catch (InvalidPathException | IOException e) {
      var msg = e.getMessage();
      if (msg == null || msg.isBlank()) {
        fail(e.getClass().getSimpleName());
      } else {
        fail(String.format("%s: %s", e.getClass().getSimpleName(), msg));
      }
    }
  }

  protected void assertOutLines(String[] lines) {
    assertStreamLines(lines, "stdout", getPlainOut());
  }

  protected void assertOutLinesFile(String filename) {
    assertStreamLinesFile(filename, "stdout", getPlainOut());
  }

  protected void assertErrLines(String[] lines) {
    assertStreamLines(lines, "stderr", getPlainErr());
  }

  protected void assertErrLinesFile(String filename) {
    assertStreamLinesFile(filename, "stderr", getPlainErr());
  }

  public static File getFileClassPath(String filename) throws IOException, URISyntaxException {
    var resource = MalTest.class.getClassLoader().getResource(filename);
    if (resource == null) {
      throw new IOException(String.format("%s: No such file or directory", filename));
    }
    return new File(resource.toURI());
  }

  public static File assertGetFileClassPath(String filename) {
    try {
      return getFileClassPath(filename);
    } catch (IOException | URISyntaxException e) {
      fail(e.getMessage());
    }
    throw new RuntimeException("This should be unreachable");
  }

  public static String readFileClassPath(String filename) throws IOException, URISyntaxException {
    return Files.readString(getFileClassPath(filename).toPath());
  }

  public static String assertReadFileClassPath(String filename) {
    try {
      return readFileClassPath(filename);
    } catch (IOException | URISyntaxException e) {
      fail(e.getMessage());
    }
    throw new RuntimeException("This should be unreachable");
  }

  public void failPrintOutErr(String message) {
    String errorMessage = String.format("%s%n", message);
    if (!getOut().isEmpty()) {
      errorMessage = String.format("%s%nSTDOUT:%n%s", errorMessage, getPlainOut());
    }
    if (!getErr().isEmpty()) {
      errorMessage = String.format("%s%nSTDERR:%n%s", errorMessage, getPlainErr());
    }
    fail(errorMessage);
  }

  public String getNewTmpDir(String prefix) {
    try {
      var tmpPath = Files.createTempDirectory(prefix);
      var tmpFile = tmpPath.toFile();
      tmpDirs.add(tmpFile);
      return tmpFile.getAbsolutePath();
    } catch (IOException e) {
      deleteTmpDirs();
      fail(e.getMessage());
    }
    throw new RuntimeException("This should be unreachable");
  }

  private void deleteTmpDirs() {
    for (var tmpDir : tmpDirs) {
      deleteRecursive(tmpDir);
    }
  }
}

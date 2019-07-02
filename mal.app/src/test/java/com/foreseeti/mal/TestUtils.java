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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;

@SuppressWarnings("serial")
class ExitSecurityException extends SecurityException {
  private int status;

  public ExitSecurityException(int status) {
    this.status = status;
  }

  public int getStatus() {
    return this.status;
  }
}

class NoExitSecurityManager extends SecurityManager {
  @Override
  public void checkExit(int status) {
    throw new ExitSecurityException(status);
  }

  @Override
  public void checkPermission(Permission perm) {}
}

public class TestUtils {
  private static SecurityManager s = new NoExitSecurityManager();
  private static SecurityManager oldS = System.getSecurityManager();
  private static ByteArrayOutputStream out;
  private static ByteArrayOutputStream err;
  private static PrintStream outStream;
  private static PrintStream errStream;
  private static PrintStream oldOut = System.out;
  private static PrintStream oldErr = System.err;

  public static void initTestSystem() {
    System.setSecurityManager(s);
    out = new ByteArrayOutputStream();
    err = new ByteArrayOutputStream();
    outStream = new PrintStream(out);
    errStream = new PrintStream(err);
    System.setOut(outStream);
    System.setErr(errStream);
  }

  public static void clearTestSystem() {
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

  public static void resetTestSystem() {
    clearTestSystem();
    initTestSystem();
  }

  public static String getOut() {
    return out.toString();
  }

  public static String getErr() {
    return err.toString();
  }

  public static String getPlainOut() {
    return out.toString().replaceAll("\u001B\\[[:\\d]*m", "");
  }

  public static String getPlainErr() {
    return err.toString().replaceAll("\u001B\\[[:\\d]*m", "");
  }
}

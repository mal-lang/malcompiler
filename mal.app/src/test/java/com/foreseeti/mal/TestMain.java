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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.foreseeti.mal.Main;

public class TestMain {
  private static ByteArrayOutputStream out = new ByteArrayOutputStream();
  private static ByteArrayOutputStream err = new ByteArrayOutputStream();
  private static PrintStream outStream = null;
  private static PrintStream errStream = null;
  private static PrintStream oldOut = System.out;
  private static PrintStream oldErr = System.err;

  @BeforeEach
  public void init() {
    outStream = new PrintStream(out);
    errStream = new PrintStream(err);
    System.setOut(outStream);
    System.setErr(errStream);
  }

  @AfterEach
  public void tearDown() {
    outStream.close();
    errStream.close();
    outStream = null;
    errStream = null;
    System.setOut(oldOut);
    System.setErr(oldErr);
  }

  @Test
  public void testMainMethod() {
    assertEquals(true, true);
  }
}

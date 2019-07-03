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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestAnalyzer {
  private static PrintStream defaultOut = System.out;
  private static PrintStream defaultErr = System.err;
  private static ByteArrayOutputStream out = new ByteArrayOutputStream();
  private static ByteArrayOutputStream err = new ByteArrayOutputStream();
  private static PrintStream outStream = new PrintStream(out);
  private static PrintStream errStream = new PrintStream(err);

  @BeforeEach
  public void init() {
    err.reset();
    out.reset();
    System.setOut(outStream);
    System.setErr(errStream);
  }

  @AfterEach
  public void tearDown() {
    System.setOut(defaultOut);
    System.setErr(defaultErr);
  }

  private static String getPlainOut() {
    return out.toString().replaceAll("\u001B\\[[:\\d]*m", "");
  }

  private static String getPlainErr() {
    return err.toString().replaceAll("\u001B\\[[:\\d]*m", "");
  }

  private static void run(String filename) throws CompilerException {
    AST ast = AssertAST.assertGetAST(filename);
    Analyzer analyzer = new Analyzer(ast);
    analyzer.analyze();
  }

  @Test
  public void testBad1() {
    try {
      run("analyzer/bad1.mal");
      fail("analyzer/bad1.mal should have failed");
    } catch (CompilerException e) {
      assertTrue(out.toString().isEmpty());
      String[] rows = getPlainErr().split("\\n");
      String[] expected = {
          "[ANALYZER ERROR] <bad1.mal:4:1> Define 'custom' previously defined at <bad1.mal:3:1>",
          "[ANALYZER ERROR] Missing required define '#id: \"\"'",
          "[ANALYZER ERROR] Missing required define '#version: \"\"'",
          "[ANALYZER WARNING] <bad1.mal:5:10> Category 'emp' contains no assets or metadata",
          "[ANALYZER ERROR] <bad1.mal:35:9> Asset 'AA' previously defined at <bad1.mal:29:18>",
          "[ANALYZER ERROR] <bad1.mal:11:3> Metadata 'info' previously defined at <bad1.mal:10:3>",
          "[ANALYZER ERROR] <bad1.mal:12:3> Metadata 'rationale' previously defined at <bad1.mal:7:3>",
          "[ANALYZER ERROR] <bad1.mal:16:5> Metadata 'rationale' previously defined at <bad1.mal:15:5>",
          "[ANALYZER ERROR] <bad1.mal:50:5> Metadata 'assumptions' previously defined at <bad1.mal:49:5>",
          "[ANALYZER WARNING] <bad1.mal:29:18> Asset 'AA' is abstract but never extended to",
          "[ANALYZER ERROR] <bad1.mal:22:7> Attack step 'compromise' previously defined at <bad1.mal:18:7>",
          "[ANALYZER ERROR] <bad1.mal:30:7> Cannot override attack step 'compromise' previously defined at <bad1.mal:18:7> with different type 'ALL' =/= 'ANY'",
          "[ANALYZER ERROR] <bad1.mal:31:14> Cannot inherit attack step 'access' without previous definition",
          "[ANALYZER ERROR] <bad1.mal:48:6> Field 'a' previously defined at <bad1.mal:48:6>",
          "[ANALYZER ERROR] <bad1.mal:20:8> Last step is not attack step",
          "[ANALYZER WARNING] <bad1.mal:21:7> Step 'c' defined as variable at <bad1.mal:19:11> and field at <bad1.mal:47:25>",
          "[ANALYZER ERROR] <bad1.mal:22:18> Require '<-' may only be defined for attack step type exist 'E' or not-exist '!E'",
          "[ANALYZER ERROR] <bad1.mal:25:8> Types 'C' and 'A' have no common ancestor",
          "[ANALYZER ERROR] <bad1.mal:26:7> Asset 'C' cannot be of type 'A'",
          "[ANALYZER WARNING] <bad1.mal:27:7> Step 'invalidate' defined as variable at <bad1.mal:24:11> and attack step at <bad1.mal:23:7>",
          "[ANALYZER ERROR] <bad1.mal:32:7> Attack step 'authorize' not defined for asset 'AA'",
          "[ANALYZER ERROR] <bad1.mal:33:7> Field 'b' not defined for asset 'AA'",
          "[ANALYZER ERROR] <bad1.mal:38:9> Variable 'var1' previously defined at <bad1.mal:37:9>",
          "[ANALYZER ERROR] <bad1.mal:41:11> Variable 'aaa' previously defined at <bad1.mal:40:11>",
          "[ANALYZER ERROR] <bad1.mal:37:9> Variable 'var1' contains cycle 'var1 -> var1'",
      "[ANALYZER ERROR] <bad1.mal:43:7> Previous asset 'C' is not of type 'A'"};
      assertEquals(expected.length, rows.length);
      for (int i = 0; i < rows.length; i++) {
        assertEquals(expected[i], rows[i]);
      }
      assertEquals("There were semantic errors", e.getMessage());
    }
  }

  @Test
  public void testBad2() {
    try {
      run("analyzer/bad2.mal");
      fail("analyzer/bad2.mal should have failed");
    } catch (CompilerException e) {
      assertTrue(out.toString().isEmpty());
      String[] rows = getPlainErr().split("\\n");
      String[] expected = {"[ANALYZER ERROR] <bad2.mal:1:1> Define 'id' cannot be empty",
          "[ANALYZER ERROR] <bad2.mal:2:1> Define 'version' must be valid semantic versioning without pre-release identifier and build metadata",
          "[ANALYZER ERROR] <bad2.mal:4:9> Asset 'A' extends in loop 'A -> B -> C -> D -> A'",
          "[ANALYZER ERROR] <bad2.mal:5:9> Asset 'B' extends in loop 'B -> C -> D -> A -> B'",
          "[ANALYZER ERROR] <bad2.mal:6:9> Asset 'C' extends in loop 'C -> D -> A -> B -> C'",
      "[ANALYZER ERROR] <bad2.mal:7:9> Asset 'D' extends in loop 'D -> A -> B -> C -> D'"};
      assertEquals(expected.length, rows.length);
      for (int i = 0; i < rows.length; i++) {
        assertEquals(expected[i], rows[i]);
      }
      assertEquals("There were semantic errors", e.getMessage());
    }
  }

  @Test
  public void testScope() {
    try {
      run("analyzer/scope.mal");
      assertTrue(out.toString().isEmpty());
      assertTrue(err.toString().isEmpty());
    } catch (CompilerException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testComplex() {
    try {
      run("analyzer/complex.mal");
      assertTrue(out.toString().isEmpty());
      assertTrue(err.toString().isEmpty());
    } catch (CompilerException e) {
      fail(e.getMessage());
    }
  }
}

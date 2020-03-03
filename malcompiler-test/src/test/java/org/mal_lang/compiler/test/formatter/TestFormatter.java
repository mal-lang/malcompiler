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
package org.mal_lang.compiler.test.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.test.MalTest;
import org.mal_lang.formatter.Formatter;

public class TestFormatter extends MalTest {

  public void outputEqual(String unformattedPath, String formattedPath, int lineWidth) {
    String formattedString = assertReadFileClassPath(formattedPath);
    File input = assertGetFileClassPath(unformattedPath);
    try {
      String formatted = new String(Formatter.prettyPrint(input, lineWidth));
      if (!formattedString.equals(formatted)) {
        fail(String.format("%s formatted does not equal %s", unformattedPath, formattedPath));
      }
    } catch (IOException | CompilerException e) {
      fail(e.getMessage());
    }
  }

  public void formats(String path) {
    File file = assertGetFileClassPath(path);
    try {
      Formatter.prettyPrint(file, 100);
    } catch (IOException | CompilerException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testInvalid() {
    File file = assertGetFileClassPath("parser/bad-asset1.mal");
    try {
      Formatter.prettyPrint(file, 100);
      fail("bad-asset1.mal should not compile");
    } catch (IOException | CompilerException e) {
      assertEquals("There were syntax errors", e.getMessage());
    }
    assertEmptyOut();
    String[] expected = {
      "[PARSER ERROR] <bad-asset1.mal:2:3> expected 'abstract', 'asset', or '}', found identifier",
      "[FORMATTER ERROR] Code to be formatted must be syntactically valid",
      ""
    };
    assertErrLines(expected);
  }

  @Test
  public void testReadable() {
    outputEqual("formatter/readable.mal", "formatter/readable.ans", 100);
  }

  @Test
  public void testOneline() {
    outputEqual("formatter/oneline.mal", "formatter/oneline.ans", 100);
  }

  @Test
  public void testMargin50() {
    outputEqual("formatter/margin.mal", "formatter/margin50.ans", 50);
  }

  @Test
  public void testMargin30() {
    outputEqual("formatter/margin.mal", "formatter/margin30.ans", 30);
  }

  @Test
  public void testComplexFormat() {
    formats("analyzer/complex.mal");
  }

  @Test
  public void testDistributionsFormat() {
    formats("analyzer/distributions.mal");
  }

  @Test
  public void testBledFormat() {
    formats("bled/bled.mal");
  }

  @Test
  public void testAllFeatures() {
    formats("all-features/core.mal");
  }

  @Test
  public void testAttackStepSet() {
    formats("generator/attack-step-set.mal");
  }

  @Test
  public void testDebugStep() {
    formats("generator/debug-step.mal");
  }

  @Test
  public void testDist() {
    formats("generator/dist.mal");
  }

  @Test
  public void testNaming() {
    formats("generator/naming.mal");
  }

  @Test
  public void testNested() {
    formats("generator/nested.mal");
  }

  @Test
  public void testSteps() {
    formats("generator/steps.mal");
  }

  @Test
  public void testVariable() {
    formats("generator/variable.mal");
  }

  @Test
  public void testReverse() {
    formats("lang-converter/reverse.mal");
  }

  @Test
  public void testVehicleLang() {
    formats("vehiclelang/vehicleLang.mal");
    // formats("vehiclelang/vehicleLangEncryption.mal"); // Doesn't compile
    formats("vehiclelang/vehicleLangEthernet.mal");
    formats("vehiclelang/vehicleLangPublicInterfaces.mal");
  }
}

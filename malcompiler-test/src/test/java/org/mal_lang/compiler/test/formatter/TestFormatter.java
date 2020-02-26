package org.mal_lang.compiler.test.formatter;

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
      fail(getOut() + e.getMessage());
    }
  }

  @Test
  public void testEqualOutput() {
    outputEqual("formatter/readable.mal", "formatter/readable.ans", 100);
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
    // TODO: vehicleLang.mal should be ok - it's fine when you format using the commandline, there's
    // something about line 183 and '’', line: "...vehicle’s..."
    // formats("vehicleLang/vehicleLang.mal");
    // formats("vehicleLang/vehicleLangEncryption.mal"); // Doesn't compile
    formats("vehicleLang/vehicleLangEthernet.mal");
    formats("vehicleLang/vehicleLangPublicInterfaces.mal");
  }
}

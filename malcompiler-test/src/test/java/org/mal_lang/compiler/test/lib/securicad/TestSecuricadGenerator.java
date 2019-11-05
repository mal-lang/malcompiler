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
package org.mal_lang.compiler.test.lib.securicad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.lib.securicad.Generator;
import org.mal_lang.compiler.test.lib.JavaGeneratorTest;

public class TestSecuricadGenerator extends JavaGeneratorTest {
  @BeforeAll
  public static void initGenerator() {
    generatorClass = Generator.class;
    defaultArgs = Map.of("package", "lang", "mock", "true");
  }

  @AfterAll
  public static void clearGenerator() {
    generatorClass = null;
    defaultArgs = Map.of();
  }

  private void assertPathMissing(Lang lang, Map<String, String> args) {
    String[] expectedErrors = {
      "[GENERATOR ERROR] SecuriCAD generator requires argument 'path'", ""
    };
    assertGeneratorErrors(lang, args, expectedErrors);
  }

  private void assertPathRelative(Lang lang, Map<String, String> args) {
    String[] expectedErrors = {"[GENERATOR ERROR] Argument 'path' must be an absolute path", ""};
    assertGeneratorErrors(lang, args, expectedErrors);
  }

  private void assertPathFile(Lang lang, Map<String, String> args) {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Argument 'path' is a file but must be an empty directory", ""
    };
    assertGeneratorErrors(lang, args, expectedErrors);
  }

  private void assertPathNotEmpty(Lang lang, Map<String, String> args) {
    String[] expectedErrors = {"[GENERATOR ERROR] Argument 'path' must be an empty directory", ""};
    assertGeneratorErrors(lang, args, expectedErrors);
  }

  private void assertPackageMissing(Lang lang, Map<String, String> args) {
    String[] expectedWarnings = {
      "[GENERATOR WARNING] Missing optional argument 'package', using default", ""
    };
    assertGeneratorWarnings(lang, args, expectedWarnings);
  }

  private void assertPackageInvalid(Lang lang, Map<String, String> args) {
    String[] expectedErrors = {
      String.format(
          "[GENERATOR ERROR] Package '%s' is not a valid package name", args.get("package")),
      ""
    };
    assertGeneratorErrors(lang, args, expectedErrors);
  }

  private void assertIconsRelative(Lang lang, Map<String, String> args) {
    String[] expectedErrors = {"[GENERATOR ERROR] Argument 'icons' must be an absolute path", ""};
    assertGeneratorErrors(lang, args, expectedErrors);
  }

  private void assertIconsFile(Lang lang, Map<String, String> args) {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Argument 'icons' is a file but must be a directory", ""
    };
    assertGeneratorErrors(lang, args, expectedErrors);
  }

  private void assertMockFilesPresent(File dir, String[] files) {
    if (!dir.isDirectory()) {
      fail(String.format("%s is not a directory", dir.getPath()));
    }
    var filesMap = new HashMap<String, Boolean>();
    for (var file : files) {
      filesMap.put(file, Boolean.FALSE);
    }
    for (var mockFile : dir.listFiles()) {
      if (mockFile.isFile()) {
        assertTrue(
            filesMap.containsKey(mockFile.getName()),
            String.format("Unexpected file %s", mockFile.getPath()));
        assertEquals(
            Boolean.FALSE,
            filesMap.get(mockFile.getName()),
            String.format("Duplicate file %s", mockFile.getPath()));
        filesMap.put(mockFile.getName(), Boolean.TRUE);
      }
    }
    for (var entry : filesMap.entrySet()) {
      assertEquals(
          Boolean.TRUE,
          entry.getValue(),
          String.format("File %s not found in %s", entry.getKey(), dir.getPath()));
    }
  }

  private void assertMockInvalid(Lang lang, Map<String, String> args) {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Optional argument 'mock' must be either 'true' or 'false'", ""
    };
    assertGeneratorErrors(lang, args, expectedErrors);
  }

  private void assertMockPresent(String outDir) {
    // com.foreseeti.corelib
    var corelibDir = new File(outDir, "com/foreseeti/corelib");
    String[] corelibFiles = {
      "AbstractSample.java",
      "AssociationManager.java",
      "BaseSample.java",
      "DefaultValue.java",
      "FAnnotations.java",
      "FClass.java",
      "Link.java",
      "ModelElement.java"
    };
    assertMockFilesPresent(corelibDir, corelibFiles);

    // com.foreseeti.corelib.math
    var corelibMathDir = new File(corelibDir, "math");
    String[] corelibMathFiles = {
      "FBernoulliDistribution.java",
      "FBinomialDistribution.java",
      "FDistribution.java",
      "FExponentialDistribution.java",
      "FGammaDistribution.java",
      "FMath.java",
      "FLogNormalDistribution.java",
      "FParetoDistribution.java",
      "FTruncatedNormalDistribution.java",
      "FUniformDistribution.java"
    };
    assertMockFilesPresent(corelibMathDir, corelibMathFiles);

    // com.foreseeti.corelib.util
    var corelibUtilDir = new File(corelibDir, "util");
    String[] corelibUtilFiles = {"FProb.java", "FProbSet.java"};
    assertMockFilesPresent(corelibUtilDir, corelibUtilFiles);

    // com.foreseeti.simulator
    var simulatorDir = new File(outDir, "com/foreseeti/simulator");
    String[] simulatorFiles = {
      "Asset.java",
      "AbstractAttacker.java",
      "AttackStep.java",
      "AttackStepMax.java",
      "AttackStepMin.java",
      "BaseLangLink.java",
      "ConcreteSample.java",
      "Defense.java",
      "MultiParentAsset.java",
      "SingleParentAsset.java"
    };
    assertMockFilesPresent(simulatorDir, simulatorFiles);
  }

  private void assertMockNotPresent(String outDir) {
    // com.foreseeti.corelib
    var corelibDir = new File(outDir, "com/foreseeti/corelib");
    assertFalse(corelibDir.exists(), String.format("%s exists", corelibDir));

    // com.foreseeti.corelib.math
    var corelibMathDir = new File(corelibDir, "math");
    assertFalse(corelibMathDir.exists(), String.format("%s exists", corelibMathDir));

    // com.foreseeti.corelib.util
    var corelibUtilDir = new File(corelibDir, "util");
    assertFalse(corelibUtilDir.exists(), String.format("%s exists", corelibUtilDir));

    // com.foreseeti.simulator
    var simulatorDir = new File(outDir, "com/foreseeti/simulator");
    assertFalse(simulatorDir.exists(), String.format("%s exists", simulatorDir));
  }

  @Test
  public void testBadPath() {
    assertPathMissing(null, Map.of());
    resetTestSystem();
    assertPathMissing(null, Map.of("path", ""));
    resetTestSystem();
    assertPathMissing(null, Map.of("path", " \t "));
    resetTestSystem();
    assertPathRelative(null, Map.of("path", "a"));
    resetTestSystem();
    assertPathRelative(null, Map.of("path", "a/b"));
    resetTestSystem();
    var bledFile = assertGetFileClassPath("bled/bled.mal");
    assertPathFile(null, Map.of("path", bledFile.getAbsolutePath()));
    resetTestSystem();
    var bledDir = assertGetFileClassPath("bled");
    assertPathNotEmpty(null, Map.of("path", bledDir.getAbsolutePath()));
  }

  @Test
  public void testGoodPath() {
    assertGeneratorOK(emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator")));
    resetTestSystem();
    assertGeneratorOK(
        emptyLang, Map.of("path", String.format("%s/a", getNewTmpDir("test-securicad-generator"))));
    resetTestSystem();
    assertGeneratorOK(
        emptyLang,
        Map.of("path", String.format("%s/a/b", getNewTmpDir("test-securicad-generator"))));
  }

  @Test
  public void testBadPackage() {
    removedArgs = Set.of("package");
    assertPackageMissing(emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator")));
    resetTestSystem();
    assertPackageMissing(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "package", ""));
    resetTestSystem();
    assertPackageMissing(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "package", " \t "));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "package", "int"));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "package", "true"));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "package", "null"));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "package", "a/b"));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "package", "a-b"));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "package", "a.int"));
    removedArgs = Set.of();
  }

  @Test
  public void testBadIcons() {
    // Relative icons
    assertIconsRelative(
        null, Map.of("path", getNewTmpDir("test-securicad-generator"), "icons", "a"));
    resetTestSystem();
    assertIconsRelative(
        null, Map.of("path", getNewTmpDir("test-securicad-generator"), "icons", "a/b"));
    resetTestSystem();
    // File icons
    var bledFile = assertGetFileClassPath("bled/bled.mal");
    assertIconsFile(
        null,
        Map.of(
            "path", getNewTmpDir("test-securicad-generator"), "icons", bledFile.getAbsolutePath()));
  }

  @Test
  public void testGoodIcons() {
    // Missing icons
    assertGeneratorOK(emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator")));
    resetTestSystem();
    // Empty icons
    assertGeneratorOK(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "icons", ""));
    resetTestSystem();
    // Blank icons
    assertGeneratorOK(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "icons", " \t "));
    resetTestSystem();
    // Good icons
    assertGeneratorOK(
        emptyLang,
        Map.of(
            "path",
            getNewTmpDir("test-securicad-generator"),
            "icons",
            getNewTmpDir("test-securicad-generator")));
  }

  @Test
  public void testBadMock() {
    removedArgs = Set.of("mock");
    assertMockInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-securicad-generator"), "mock", "a"));
    removedArgs = Set.of();
  }

  @Test
  public void testGoodMock() {
    // Test that {"mock": "true"} generates mock files
    var outDir = getNewTmpDir("test-securicad-generator");
    assertGeneratorOK(emptyLang, Map.of("path", outDir, "mock", "true"));
    assertMockPresent(outDir);
    resetTestSystem();
    // Test that {"mock": "false"} doesn't generate mock files
    outDir = getNewTmpDir("test-securicad-generator");
    assertGeneratorOK(emptyLang, Map.of("path", outDir, "mock", "false"));
    assertMockNotPresent(outDir);
    assertEmptyOut();
    assertEmptyErr();
    resetTestSystem();
    // Test that missing "mock" argument doesn't generate mock files
    removedArgs = Set.of("mock");
    outDir = getNewTmpDir("test-securicad-generator");
    assertGeneratorOK(emptyLang, Map.of("path", outDir));
    assertMockNotPresent(outDir);
    assertEmptyOut();
    assertEmptyErr();
    removedArgs = Set.of();
  }

  @Test
  public void testBadLang() {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Asset 'int' is a java keyword",
      "[GENERATOR ERROR] Attack step 'null' in asset 'int' is a java keyword",
      "[GENERATOR ERROR] Field 'static' in asset 'int' is a java keyword",
      "[GENERATOR ERROR] Field 'false' in asset 'int' is a java keyword",
      ""
    };
    assertLangNotGenerated("generator/bad-lang.mal", expectedErrors);
  }

  @Test
  public void testAllFeaturesNotGenerated() {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Category 'C1' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
      "[GENERATOR ERROR] Category 'C2' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
      "[GENERATOR ERROR] Category 'C3' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
      ""
    };
    assertLangNotGenerated("all-features/all-features.mal", expectedErrors);
  }

  @Test
  public void testComplexNotGenerated() {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Category 'Person' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
      "[GENERATOR ERROR] Category 'Hardware' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
      ""
    };
    assertLangNotGenerated("analyzer/complex.mal", expectedErrors);
  }

  @Test
  public void testBledNotGenerated() {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Category 'LatestAndGreatest' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
      ""
    };
    assertLangNotGenerated("bled/bled.mal", expectedErrors);
  }

  @Test
  public void testVehicleLangGenerated() {
    assertLangGenerated("vehiclelang/vehicleLang.mal");
  }

  @Test
  public void testAttackStepSet() {
    assertLangGenerated("generator/attack-step-set.mal");
  }

  @Test
  public void testDist() {
    assertLangGenerated("generator/dist.mal");
  }

  @Test
  public void testNaming() {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Attack step 'a1' shares name with its asset 'A1'",
      "[GENERATOR ERROR] Attack step 'A2' shares name with its asset 'A2'",
      ""
    };
    assertLangNotGenerated("generator/naming.mal", expectedErrors);
  }
}

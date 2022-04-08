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
package org.mal_lang.compiler.test.lib.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.lib.reference.Generator;
import org.mal_lang.compiler.test.lib.JavaGeneratorTest;

public class TestReferenceGenerator extends JavaGeneratorTest {
  @BeforeAll
  public static void initGenerator() {
    generatorClass = Generator.class;
    defaultArgs = Map.of("package", "lang");
  }

  @AfterAll
  public static void clearGenerator() {
    generatorClass = null;
    defaultArgs = Map.of();
  }

  private void assertPathMissing(Lang lang, Map<String, String> args) {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Reference generator requires argument 'path'", ""
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

  private void assertCoreInvalid(Lang lang, Map<String, String> args) {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Optional argument 'core' must be either 'true' or 'false'", ""
    };
    assertGeneratorErrors(lang, args, expectedErrors);
  }

  private static void assertAttackerProfilePresent(String outDir) {
    var attackerProfileFile = new File(outDir, "attackerProfile.ttc");
    var attackerProfilePath = attackerProfileFile.getPath();
    assertTrue(
        attackerProfileFile.exists(), String.format("%s does not exist", attackerProfilePath));
    assertTrue(
        attackerProfileFile.isFile(), String.format("%s is not a file", attackerProfilePath));
  }

  private static void assertCorePresent(String outDir) {
    var coreDir = new File(outDir, "core");
    var corePath = coreDir.getPath();
    assertTrue(coreDir.exists(), String.format("%s does not exist", corePath));
    assertTrue(coreDir.isDirectory(), String.format("%s is not a directory", corePath));
    var coreFiles = coreDir.listFiles();
    assertEquals(6, coreFiles.length, String.format("%s should contain 6 files", corePath));
    var coreFilesList =
        List.of(
            "Asset.java",
            "Attacker.java",
            "AttackStep.java",
            "AttackStepMax.java",
            "AttackStepMin.java",
            "Defense.java");
    var coreFilesMap = new HashMap<String, Boolean>();
    for (var coreFile : coreFilesList) {
      coreFilesMap.put(coreFile, Boolean.FALSE);
    }
    for (var coreFile : coreFiles) {
      var coreFileName = coreFile.getName();
      var coreFilePath = coreFile.getPath();
      assertTrue(
          coreFilesMap.containsKey(coreFileName),
          String.format("Unexpected file %s", coreFilePath));
      assertEquals(
          Boolean.FALSE,
          coreFilesMap.get(coreFileName),
          String.format("Duplicate file %s", coreFilePath));
      coreFilesMap.put(coreFileName, Boolean.TRUE);
      assertTrue(coreFile.exists(), String.format("%s does not exist", coreFilePath));
      assertTrue(coreFile.isFile(), String.format("%s is not a file", coreFilePath));
    }
    for (var entry : coreFilesMap.entrySet()) {
      assertEquals(
          Boolean.TRUE,
          entry.getValue(),
          String.format("File %s not found in %s", entry.getKey(), corePath));
    }
  }

  private static void assertCoreNotPresent(String outDir) {
    var coreDir = new File(outDir, "core");
    var corePath = coreDir.getPath();
    assertFalse(coreDir.exists(), String.format("%s exists", corePath));
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
    assertGeneratorOK(emptyLang, Map.of("path", getNewTmpDir("test-reference-generator")));
    resetTestSystem();
    assertGeneratorOK(
        emptyLang, Map.of("path", String.format("%s/a", getNewTmpDir("test-reference-generator"))));
    resetTestSystem();
    assertGeneratorOK(
        emptyLang,
        Map.of("path", String.format("%s/a/b", getNewTmpDir("test-reference-generator"))));
  }

  @Test
  public void testBadPackage() {
    removedArgs = Set.of("package");
    assertPackageMissing(emptyLang, Map.of("path", getNewTmpDir("test-reference-generator")));
    resetTestSystem();
    assertPackageMissing(
        emptyLang, Map.of("path", getNewTmpDir("test-reference-generator"), "package", ""));
    resetTestSystem();
    assertPackageMissing(
        emptyLang, Map.of("path", getNewTmpDir("test-reference-generator"), "package", " \t "));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-reference-generator"), "package", "int"));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-reference-generator"), "package", "true"));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-reference-generator"), "package", "null"));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-reference-generator"), "package", "a/b"));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-reference-generator"), "package", "a-b"));
    resetTestSystem();
    assertPackageInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-reference-generator"), "package", "a.int"));
    removedArgs = Set.of();
  }

  @Test
  public void testBadCore() {
    assertCoreInvalid(
        emptyLang, Map.of("path", getNewTmpDir("test-reference-generator"), "core", "a"));
  }

  @Test
  public void testGoodCore() {
    // Test that {"core": "true"} generates core package
    var outDir = getNewTmpDir("test-reference-generator");
    assertGeneratorOK(emptyLang, Map.of("path", outDir, "core", "true"));
    assertAttackerProfilePresent(outDir);
    assertCorePresent(outDir);
    assertEmptyOut();
    assertEmptyErr();
    resetTestSystem();
    // Test that {"core": "false"} doesn't generate core package
    outDir = getNewTmpDir("test-reference-generator");
    assertGeneratorOK(emptyLang, Map.of("path", outDir, "core", "false"));
    assertAttackerProfilePresent(outDir);
    assertCoreNotPresent(outDir);
    assertEmptyOut();
    assertEmptyErr();
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
  public void testAllFeaturesGenerated() {
    assertLangGenerated("all-features/all-features.mal");
  }

  @Test
  public void testComplexNotGenerated() {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Advanced TTC, used at Computer.bypassFirewall, is not supported", ""
    };
    assertLangNotGenerated("analyzer/complex.mal", expectedErrors);
  }

  @Test
  public void testBledGenerated() {
    assertLangGenerated("bled/bled.mal");
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
  public void testNaming() {
    String[] expectedErrors = {
      "[GENERATOR ERROR] Attack step 'a1' shares name with its asset 'A1'",
      "[GENERATOR ERROR] Attack step 'A2' shares name with its asset 'A2'",
      ""
    };
    assertLangNotGenerated("generator/naming.mal", expectedErrors);
  }

  @Test
  public void testNested() {
    assertLangGenerated("generator/nested.mal");
  }

  @Test
  public void testSteps() {
    assertLangGenerated("generator/steps.mal");
  }

  @Test
  public void testSubtype() {
    assertLangGenerated("generator/subtype.mal");
  }
}

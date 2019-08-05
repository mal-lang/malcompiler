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
package com.foreseeti.mal.test.lib.reference;

import static com.foreseeti.mal.test.lib.AssertLang.assertGetLangClassPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.foreseeti.mal.lib.CompilerException;
import com.foreseeti.mal.lib.Lang;
import com.foreseeti.mal.lib.reference.Generator;
import com.foreseeti.mal.test.MalTest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class TestReferenceGenerator extends MalTest {
  private static Lang emptyLang = new Lang(Map.of(), Map.of(), Map.of(), List.of());

  private void assertGeneratorErrors(Lang lang, Map<String, String> args, String[] expectedErrors) {
    try {
      Generator.generate(lang, args);
      fail("Generator.generate should have thrown CompilerException");
    } catch (IOException e) {
      fail("Generator.generate should have thrown CompilerException");
    } catch (CompilerException e) {
      assertEquals("There were generator errors", e.getMessage());
      assertEmptyOut();
      assertErrLines(expectedErrors);
    }
  }

  private void assertGeneratorWarnings(
      Lang lang, Map<String, String> args, String[] expectedWarnings) {
    try {
      Generator.generate(lang, args);
      assertEmptyOut();
      assertErrLines(expectedWarnings);
    } catch (IOException | CompilerException e) {
      fail(String.format("%s\n%s", e.getMessage(), getPlainErr()));
    }
  }

  private void assertGeneratorOK(Lang lang, Map<String, String> args) {
    try {
      Generator.generate(lang, args);
      assertEmptyOut();
      assertEmptyErr();
    } catch (IOException | CompilerException e) {
      fail(String.format("%s\n%s", e.getMessage(), getPlainErr()));
    }
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
    assertGeneratorOK(emptyLang, Map.of("path", getNewTmpDir(), "package", "a"));
    resetTestSystem();
    assertGeneratorOK(
        emptyLang, Map.of("path", String.format("%s/a", getNewTmpDir()), "package", "a"));
    resetTestSystem();
    assertGeneratorOK(
        emptyLang, Map.of("path", String.format("%s/a/b", getNewTmpDir()), "package", "a"));
  }

  @Test
  public void testBadPackage() {
    assertPackageMissing(emptyLang, Map.of("path", getNewTmpDir()));
    resetTestSystem();
    assertPackageMissing(emptyLang, Map.of("path", getNewTmpDir(), "package", ""));
    resetTestSystem();
    assertPackageMissing(emptyLang, Map.of("path", getNewTmpDir(), "package", " \t "));
    resetTestSystem();
    assertPackageInvalid(emptyLang, Map.of("path", getNewTmpDir(), "package", "int"));
    resetTestSystem();
    assertPackageInvalid(emptyLang, Map.of("path", getNewTmpDir(), "package", "true"));
    resetTestSystem();
    assertPackageInvalid(emptyLang, Map.of("path", getNewTmpDir(), "package", "null"));
    resetTestSystem();
    assertPackageInvalid(emptyLang, Map.of("path", getNewTmpDir(), "package", "a/b"));
    resetTestSystem();
    assertPackageInvalid(emptyLang, Map.of("path", getNewTmpDir(), "package", "a-b"));
    resetTestSystem();
    assertPackageInvalid(emptyLang, Map.of("path", getNewTmpDir(), "package", "a.int"));
  }

  @Test
  public void testBadCore() {
    assertCoreInvalid(emptyLang, Map.of("path", getNewTmpDir(), "package", "a", "core", "a"));
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
  public void testGoodCore() {
    // Test that {"core": "true"} generates core package
    var outDir = getNewTmpDir();
    assertGeneratorOK(emptyLang, Map.of("path", outDir, "package", "a", "core", "true"));
    assertAttackerProfilePresent(outDir);
    assertCorePresent(outDir);
    assertEmptyOut();
    assertEmptyErr();
    resetTestSystem();
    // Test that {"core": "false"} doesn't generate core package
    outDir = getNewTmpDir();
    assertGeneratorOK(emptyLang, Map.of("path", outDir, "package", "a", "core", "false"));
    assertAttackerProfilePresent(outDir);
    assertCoreNotPresent(outDir);
    assertEmptyOut();
    assertEmptyErr();
  }

  @Test
  public void testBadLang() {
    var lang = assertGetLangClassPath("generator/bad-lang.mal");
    resetTestSystem();
    try {
      Generator.generate(lang, Map.of("path", getNewTmpDir(), "package", "a", "core", "false"));
      fail("Generator.generate should have thrown a CompilerException");
    } catch (IOException e) {
      fail("Generator.generate should have thrown a CompilerException");
    } catch (CompilerException e) {
      assertEquals("There were generator errors", e.getMessage());
      assertEmptyOut();
      String[] expectedErrors = {
        "[GENERATOR ERROR] Asset 'int' is a java keyword",
        "[GENERATOR ERROR] Attack step 'null' in asset 'int' is a java keyword",
        "[GENERATOR ERROR] Field 'static' in asset 'int' is a java keyword",
        "[GENERATOR ERROR] Field 'false' in asset 'int' is a java keyword",
        ""
      };
      assertErrLines(expectedErrors);
    }
  }

  private void assertLangGenerated(String langPath) {
    var outDir = getNewTmpDir();
    var lang = assertGetLangClassPath(langPath);
    resetTestSystem();
    try {
      Generator.generate(lang, Map.of("path", outDir, "package", "lang"));
      assertEmptyOut();
      assertEmptyErr();
      for (var asset : lang.getAssets().values()) {
        var assetFile =
            new File(String.format(String.format("%s/lang/%s.java", outDir, asset.getName())));
        assertTrue(assetFile.exists(), String.format("%s does not exist", assetFile.getPath()));
        assertTrue(assetFile.isFile(), String.format("%s is not a file", assetFile.getPath()));
      }
    } catch (IOException | CompilerException e) {
      fail(String.format("%s\n%s", e.getMessage(), getPlainErr()));
    }
  }

  @Test
  public void testAllFeaturesGenerated() {
    assertLangGenerated("all-features/all-features.mal");
  }

  @Test
  public void testComplexNotGenerated() {
    var outDir = getNewTmpDir();
    var lang = assertGetLangClassPath("analyzer/complex.mal");
    resetTestSystem();
    try {
      Generator.generate(lang, Map.of("path", outDir, "package", "lang"));
      fail("Generator.generate should have thrown a CompilerException");
    } catch (IOException e) {
      fail("Generator.generate should have thrown a CompilerException");
    } catch (CompilerException e) {
      assertEquals("There were generator errors", e.getMessage());
      assertEmptyOut();
      String[] expectedErrors = {
        "[GENERATOR ERROR] Advanced TTC, used at Computer.bypassFirewall, is not supported", ""
      };
      assertErrLines(expectedErrors);
    }
  }

  @Test
  public void testBledGenerated() {
    assertLangGenerated("bled/bled.mal");
  }

  @Test
  public void testVehicleLangGenerated() {
    assertLangGenerated("vehiclelang/vehicleLang.mal");
  }
}

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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mal_lang.compiler.test.lib.AssertLang.assertGetLangClassPath;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.lib.securicad.Generator;
import org.mal_lang.compiler.test.MalTest;

public class TestSecuricadGenerator extends MalTest {
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
  public void testBadIcons() {
    // Relative icons
    assertIconsRelative(null, Map.of("path", getNewTmpDir(), "package", "a", "icons", "a"));
    resetTestSystem();
    assertIconsRelative(null, Map.of("path", getNewTmpDir(), "package", "a", "icons", "a/b"));
    resetTestSystem();
    // File icons
    var bledFile = assertGetFileClassPath("bled/bled.mal");
    assertIconsFile(
        null, Map.of("path", getNewTmpDir(), "package", "a", "icons", bledFile.getAbsolutePath()));
  }

  @Test
  public void testGoodIcons() {
    // Missing icons
    assertGeneratorOK(emptyLang, Map.of("path", getNewTmpDir(), "package", "a"));
    resetTestSystem();
    // Empty icons
    assertGeneratorOK(emptyLang, Map.of("path", getNewTmpDir(), "package", "a", "icons", ""));
    resetTestSystem();
    // Blank icons
    assertGeneratorOK(emptyLang, Map.of("path", getNewTmpDir(), "package", "a", "icons", " \t "));
    resetTestSystem();
    // Good icons
    assertGeneratorOK(
        emptyLang, Map.of("path", getNewTmpDir(), "package", "a", "icons", getNewTmpDir()));
  }

  @Test
  public void testBadLang() {
    var lang = assertGetLangClassPath("generator/bad-lang.mal");
    resetTestSystem();
    try {
      Generator.generate(lang, Map.of("path", getNewTmpDir(), "package", "a"));
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
  public void testAllFeaturesNotGenerated() {
    var outDir = getNewTmpDir();
    var lang = assertGetLangClassPath("all-features/all-features.mal");
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
        "[GENERATOR ERROR] Category 'C1' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
        "[GENERATOR ERROR] Category 'C2' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
        "[GENERATOR ERROR] Category 'C3' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
        ""
      };
      assertErrLines(expectedErrors);
    }
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
        "[GENERATOR ERROR] Category 'Person' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
        "[GENERATOR ERROR] Category 'Hardware' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
        ""
      };
      assertErrLines(expectedErrors);
    }
  }

  @Test
  public void testBledNotGenerated() {
    var outDir = getNewTmpDir();
    var lang = assertGetLangClassPath("bled/bled.mal");
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
        "[GENERATOR ERROR] Category 'LatestAndGreatest' must be one of (Attacker, Communication, Container, Networking, Security, System, User, Zone)",
        ""
      };
      assertErrLines(expectedErrors);
    }
  }

  @Test
  public void testVehicleLangGenerated() {
    assertLangGenerated("vehiclelang/vehicleLang.mal");
  }
}

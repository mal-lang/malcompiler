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
package org.mal_lang.compiler.test.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mal_lang.compiler.test.lib.AssertLang.assertGetLangClassPath;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.ToolProvider;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.JavaGenerator;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.test.MalTest;

public abstract class JavaGeneratorTest extends MalTest {
  protected static Lang emptyLang = new Lang(Map.of(), Map.of(), Map.of(), List.of());
  protected static Class<? extends JavaGenerator> generatorClass;
  protected static Map<String, String> defaultArgs = Map.of();
  protected static Set<String> removedArgs = Set.of();

  private void generate(Lang lang, Map<String, String> args) throws IOException, CompilerException {
    assertNotNull(generatorClass, "generatorClass is null");
    try {
      var actualArgs = new HashMap<String, String>();
      actualArgs.putAll(defaultArgs);
      for (var removedArg : removedArgs) {
        actualArgs.remove(removedArg);
      }
      actualArgs.putAll(args);
      var generateMethod = generatorClass.getMethod("generate", Lang.class, Map.class);
      generateMethod.invoke(null, lang, actualArgs);
    } catch (InvocationTargetException e) {
      var target = e.getTargetException();
      if (target instanceof IOException) {
        throw (IOException) target;
      } else if (target instanceof CompilerException) {
        throw (CompilerException) target;
      } else {
        fail(String.format("%s: %s", target.getClass().getSimpleName(), target.getMessage()));
      }
    } catch (NoSuchMethodException
        | SecurityException
        | IllegalAccessException
        | IllegalArgumentException e) {
      fail(String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));
    }
  }

  protected void assertGeneratorErrors(
      Lang lang, Map<String, String> args, String[] expectedErrors) {
    try {
      generate(lang, args);
      fail("Generator.generate() should have thrown a CompilerException");
    } catch (IOException e) {
      fail("Generator.generate() should have thrown a CompilerException");
    } catch (CompilerException e) {
      assertEquals("There were generator errors", e.getMessage());
      assertEmptyOut();
      assertErrLines(expectedErrors);
    }
  }

  protected void assertGeneratorWarnings(
      Lang lang, Map<String, String> args, String[] expectedWarnings) {
    try {
      generate(lang, args);
      assertEmptyOut();
      assertErrLines(expectedWarnings);
    } catch (IOException | CompilerException e) {
      failPrintOutErr(e.getMessage());
    }
  }

  protected void assertGeneratorOK(Lang lang, Map<String, String> args) {
    try {
      generate(lang, args);
      assertEmptyOut();
      assertEmptyErr();
    } catch (IOException | CompilerException e) {
      failPrintOutErr(e.getMessage());
    }
  }

  private void generateLang(Lang lang, String sourcesDir) throws IOException, CompilerException {
    generate(lang, Map.of("path", sourcesDir));
  }

  private List<String> getJavaFilesToCompile(File sourcesDir) {
    List<String> files = new ArrayList<>();
    try {
      var canonicalSourcesDir = sourcesDir.getCanonicalFile();
      assertTrue(
          canonicalSourcesDir.exists(),
          String.format("%s does not exist", canonicalSourcesDir.getPath()));
      assertTrue(
          canonicalSourcesDir.isDirectory(),
          String.format("%s is not a directory", canonicalSourcesDir.getPath()));
      for (var file : canonicalSourcesDir.listFiles()) {
        if (file.isFile() && file.getPath().endsWith(".java")) {
          files.add(file.getCanonicalPath());
        } else if (file.isDirectory()) {
          files.addAll(getJavaFilesToCompile(file));
        }
      }
    } catch (IOException e) {
      fail(e.getMessage());
    }
    return files;
  }

  protected void assertLangGenerated(String langPath) {
    var sourcesDir = getNewTmpDir();
    var classesDir = getNewTmpDir();
    var lang = assertGetLangClassPath(langPath);
    resetTestSystem();
    try {
      generateLang(lang, sourcesDir);
      assertEmptyOut();
      assertEmptyErr();
      // Check that all assets were generated
      for (var asset : lang.getAssets().values()) {
        var assetFile =
            new File(String.format(String.format("%s/lang/%s.java", sourcesDir, asset.getName())));
        assertTrue(assetFile.exists(), String.format("%s does not exist", assetFile.getPath()));
        assertTrue(assetFile.isFile(), String.format("%s is not a file", assetFile.getPath()));
      }
      // Check that generated code compiles
      var compiler = ToolProvider.getSystemJavaCompiler();
      if (compiler == null) {
        fail("No compiler provided");
      }
      var javaFiles = getJavaFilesToCompile(new File(sourcesDir));
      List<String> arguments = new ArrayList<>();
      arguments.add("-Werror");
      arguments.add("-d");
      arguments.add(classesDir);
      arguments.addAll(javaFiles);
      String[] argumentsArray = arguments.toArray(new String[0]);
      int res = compiler.run(null, null, null, argumentsArray);
      if (res != 0) {
        failPrintOutErr("Generated code didn't compile");
      }
    } catch (IOException | CompilerException e) {
      failPrintOutErr(e.getMessage());
    }
  }

  protected void assertLangNotGenerated(String langPath, String[] expectedErrors) {
    var sourcesDir = getNewTmpDir();
    var lang = assertGetLangClassPath(langPath);
    resetTestSystem();
    try {
      generateLang(lang, sourcesDir);
      fail("Generator.generate() should have thrown a CompilerException");
    } catch (IOException e) {
      fail("Generator.generate() should have thrown a CompilerException");
    } catch (CompilerException e) {
      assertEquals("There were generator errors", e.getMessage());
      assertEmptyOut();
      assertErrLines(expectedErrors);
    }
  }
}

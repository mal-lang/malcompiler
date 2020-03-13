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
package org.mal_lang.compiler.mojo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.mal_lang.compiler.lib.Analyzer;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.LangConverter;
import org.mal_lang.compiler.lib.Parser;
import org.mal_lang.compiler.lib.securicad.Generator;

@Mojo(name = "securicad", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class SecuriCADMojo extends MalMojo {
  /** The output directory to store the generated java files in. */
  @Parameter(property = "mal.securicad.path")
  private File path;

  /** The package name to use for the generated java files. */
  @Parameter(property = "mal.securicad.package")
  private String packageName;

  /** The directory where asset icons are located. */
  @Parameter(property = "mal.securicad.icons")
  private File icons;

  /** Specifies if debug steps should be kept. */
  @Parameter(property = "mal.securicad.debug")
  private boolean keepDebugSteps;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    init();
    var log = getLog();
    var input = validateAndGetInputFile();

    // Initialize output directory
    if (path == null) {
      path = new File(getBuildDirectory(), "generated-sources");
    }
    createOrClearDirectory(path);

    // Create argument map for code generator
    var args = new HashMap<String, String>();
    args.put("path", path.getPath());

    if (packageName != null && !packageName.isBlank()) {
      args.put("package", packageName);
    }

    if (icons == null) {
      icons = new File(getResourceDirectory(), "icons");
      if (icons.exists() && icons.isDirectory()) {
        args.put("icons", icons.getPath());
      }
    } else {
      validateFileExists(icons);
      validateFileIsDirectory(icons);
      args.put("icons", icons.getPath());
    }

    args.put("debug", Boolean.toString(keepDebugSteps));

    // Generate code
    log.info(String.format("Compiling MAL specification %s", input.getPath()));
    try {
      var ast = Parser.parse(input);
      Analyzer.analyze(ast);
      var lang = LangConverter.convert(ast);
      Generator.generate(lang, args, verbose, debug);
    } catch (IOException | CompilerException e) {
      throw new MojoFailureException(e.getMessage());
    }

    // Add generated code to project's source root
    log.info(String.format("Adding compile source root %s", path.getPath()));
    project.addCompileSourceRoot(path.getPath());
  }
}

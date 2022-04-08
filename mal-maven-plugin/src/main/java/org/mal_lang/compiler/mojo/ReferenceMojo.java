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
package org.mal_lang.compiler.mojo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.mal_lang.compiler.lib.Analyzer;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.LangConverter;
import org.mal_lang.compiler.lib.Parser;
import org.mal_lang.compiler.lib.reference.Generator;

@Mojo(name = "reference", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class ReferenceMojo extends MalMojo {
  /** The output directory to store the generated java files in. */
  @Parameter(property = "mal.reference.path")
  private File path;

  /** The package name to use for the generated java files. */
  @Parameter(property = "mal.reference.package")
  private String packageName;

  /** Specifies whether the {@code core} classes should be generated. */
  @Parameter(property = "mal.reference.core", defaultValue = "true")
  private boolean core;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    init();
    var log = getLog();
    var input = validateAndGetInputFile();

    // Initialize output directory
    if (path == null) {
      path = new File(getBuildDirectory(), "generated-test-sources");
    }
    createOrClearDirectory(path);

    // Create argument map for code generator
    var args = new HashMap<String, String>();
    args.put("path", path.getPath());

    if (packageName != null && !packageName.isBlank()) {
      args.put("package", packageName);
    }

    args.put("core", String.valueOf(core));

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

    // Add generated code to project's test source root
    log.info(String.format("Adding test compile source root %s", path.getPath()));
    project.addTestCompileSourceRoot(path.getPath());

    // Add attackerProfile.ttc as a resource
    var attackerProfile = "attackerProfile.ttc";
    log.info(String.format("Adding test resource %s/%s", path.getPath(), attackerProfile));
    var resource = new Resource();
    resource.setDirectory(path.getPath());
    resource.addInclude(attackerProfile);
    project.addTestResource(resource);
  }
}

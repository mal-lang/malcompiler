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
package com.foreseeti.mal.mojo;

import com.foreseeti.mal.lib.Analyzer;
import com.foreseeti.mal.lib.CompilerException;
import com.foreseeti.mal.lib.LangConverter;
import com.foreseeti.mal.lib.Parser;
import com.foreseeti.mal.lib.generator.ReferenceGenerator;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "reference", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ReferenceMojo extends MalMojo {
  /** The output directory to store the generated java files in. */
  @Parameter(
      property = "mal.reference.path",
      defaultValue = "${project.build.directory}/generated-sources")
  private File path;

  /** The package name to use for the generated java files. */
  @Parameter(property = "mal.reference.package")
  private String packageName;

  /** Specifies whether the {@code core} classes should be generated. */
  @Parameter(property = "mal.reference.core", defaultValue = "true")
  private boolean core;

  /** The directory where MAL specifications ({@code *.mal} files) are located. */
  @Parameter(defaultValue = "${project.basedir}/src/main/mal")
  private File sourceDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    var log = getLog();

    // Check if source directory exists
    if (!sourceDirectory.exists()) {
      throw new MojoExecutionException(
          String.format("%s: No such file or directory", sourceDirectory.getPath()));
    }

    // Check if source directory is a directory
    if (!sourceDirectory.isDirectory()) {
      throw new MojoExecutionException(
          String.format("%s: Not a directory", sourceDirectory.getPath()));
    }

    // Check if file is present
    if (file == null || file.isBlank()) {
      throw new MojoExecutionException("Missing MAL specification");
    }

    var input = new File(file.trim());

    if (!input.isAbsolute()) {
      input = new File(sourceDirectory, input.getPath());
    }

    // Check if file exists
    if (!input.exists()) {
      throw new MojoExecutionException(
          String.format("%s: No such file or directory", input.getPath()));
    }

    // Check if file is a file
    if (!input.isFile()) {
      throw new MojoExecutionException(String.format("%s: Not a file", input.getPath()));
    }

    // Set up path
    if (path.exists()) {
      if (!path.isDirectory()) {
        throw new MojoExecutionException(String.format("%s: Not a directory", path.getPath()));
      }
      if (!clearDirectory(path)) {
        throw new MojoExecutionException(
            String.format("%s: Failed to clear directory", path.getPath()));
      }
    } else {
      if (!path.mkdirs()) {
        throw new MojoExecutionException(
            String.format("%s: Failed to create directories", path.getPath()));
      }
    }

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
      ReferenceGenerator.generate(lang, args, verbose, debug);
    } catch (IOException | CompilerException e) {
      throw new MojoFailureException(e.getMessage());
    }

    // Add generated code to project's source root
    log.info(String.format("Adding compile source root %s", path.getPath()));
    project.addCompileSourceRoot(path.getPath());

    // Add attackerProfile.ttc as a resource
    var attackerProfile = "attackerProfile.ttc";
    log.info(String.format("Adding resource %s/%s", path.getPath(), attackerProfile));
    var resource = new Resource();
    resource.setDirectory(path.getPath());
    resource.addInclude(attackerProfile);
    project.addResource(resource);
  }

  private boolean clearDirectory(File directory) {
    for (var file : directory.listFiles()) {
      if (file.isDirectory() && !clearDirectory(file)) {
        return false;
      }
      if (!file.delete()) {
        return false;
      }
    }
    return true;
  }
}

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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class MalMojo extends AbstractMojo {
  /** The current Maven project. */
  @Parameter(property = "project", required = true, readonly = true)
  protected MavenProject project;

  /** The current Maven project's base directory. */
  @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
  private File baseDirectory;

  /** The current Maven project's build directory. */
  @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
  private File buildDirectory;

  /** The current Maven project's resource directory. */
  private File resourceDirectory;

  /** The directory where MAL specifications ({@code *.mal} files) are located. */
  @Parameter private File sourceDirectory;

  /** The main MAL specification to compile. */
  @Parameter(property = "mal.file", required = true)
  protected String file;

  /** Specifies if the code generator should print verbose information. */
  @Parameter(property = "mal.verbose", defaultValue = "false")
  protected boolean verbose;

  /** Specifies if the code generator should print debug information. */
  @Parameter(property = "mal.debug", defaultValue = "false")
  protected boolean debug;

  protected void init() {
    if (sourceDirectory == null) {
      sourceDirectory = new File(baseDirectory, "src/main/mal");
    }
    resourceDirectory = new File(baseDirectory, "src/main/resources");
  }

  protected File getBuildDirectory() {
    return buildDirectory;
  }

  protected File getResourceDirectory() {
    return resourceDirectory;
  }

  protected void validateFileExists(File path) throws MojoExecutionException {
    if (!path.exists()) {
      throw new MojoExecutionException(
          String.format("%s: No such file or directory", path.getPath()));
    }
  }

  protected void validateFileIsFile(File path) throws MojoExecutionException {
    if (!path.isFile()) {
      throw new MojoExecutionException(String.format("%s: Not a file", path.getPath()));
    }
  }

  protected void validateFileIsDirectory(File path) throws MojoExecutionException {
    if (!path.isDirectory()) {
      throw new MojoExecutionException(String.format("%s: Not a directory", path.getPath()));
    }
  }

  protected File validateAndGetInputFile() throws MojoExecutionException {
    // Validate that input file is specified
    if (file == null || file.isBlank()) {
      throw new MojoExecutionException("Missing MAL specification");
    }

    // Create input file
    var input = new File(file.trim());

    if (!input.isAbsolute()) {
      validateFileExists(sourceDirectory);
      validateFileIsDirectory(sourceDirectory);

      // Create input file relative to source directory
      input = new File(sourceDirectory, input.getPath());
    }

    validateFileExists(input);
    validateFileIsFile(input);

    return input;
  }

  private static boolean clearDirectory(File directory) {
    for (var file : directory.listFiles()) {
      // Recursively try to clear directories before deleting them
      if (file.isDirectory() && !clearDirectory(file)) {
        return false;
      }
      if (!file.delete()) {
        return false;
      }
    }
    return true;
  }

  protected void createOrClearDirectory(File path) throws MojoExecutionException {
    if (path.exists()) {
      validateFileIsDirectory(path);

      // Try to clear path
      if (!clearDirectory(path)) {
        throw new MojoExecutionException(
            String.format("%s: Failed to clear directory", path.getPath()));
      }
    } else {
      // Try to create path
      if (!path.mkdirs()) {
        throw new MojoExecutionException(
            String.format("%s: Failed to create directories", path.getPath()));
      }
    }
  }
}

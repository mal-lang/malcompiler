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
package org.mal_lang.compiler.lib.securicad;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.lang.model.element.Modifier;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.JavaGenerator;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.lib.Lang.Asset;
import org.mal_lang.compiler.lib.Lang.Category;
import org.mal_lang.compiler.lib.Lang.Link;
import org.mal_lang.compiler.lib.MalInfo;

public class Generator extends JavaGenerator {
  private static final List<String> CATEGORIES =
      Arrays.asList(
          "Attacker",
          "Communication",
          "Container",
          "Networking",
          "Security",
          "System",
          "User",
          "Zone");
  private final String pkg;
  private final File output;
  private final Lang lang;
  private final File icons;

  public static void generate(Lang lang, Map<String, String> args)
      throws CompilerException, IOException {
    generate(lang, args, false, false);
  }

  public static void generate(Lang lang, Map<String, String> args, boolean verbose, boolean debug)
      throws CompilerException, IOException {
    new Generator(lang, args, verbose, debug)._generate();
  }

  private Generator(Lang lang, Map<String, String> args, boolean verbose, boolean debug)
      throws CompilerException {
    super(verbose, debug);
    // to not have svgSalamander flash a ghost window
    System.setProperty("java.awt.headless", "true");
    Locale.setDefault(Locale.ROOT);
    this.lang = lang;
    if (!args.containsKey("path") || args.get("path").isBlank()) {
      throw error("SecuriCAD generator requires argument 'path'");
    }
    this.output = getOutputDirectory(args.get("path"));
    if (!args.containsKey("package") || args.get("package").isBlank()) {
      LOGGER.warning("Missing optional argument 'package', using default");
      this.pkg = "auto";
    } else {
      this.pkg = args.get("package");
    }
    if (args.containsKey("icons") && !args.get("icons").isBlank()) {
      this.icons = new File(args.get("icons"));
      if (!this.icons.isAbsolute()) {
        throw error("Argument 'icons' must be an absolute path");
      } else if (icons.isFile()) {
        throw error("Argument 'icons' is a file but must be a directory");
      }
    } else {
      this.icons = null;
    }

    validateNames(this.lang, this.pkg);
    validateCategories();
  }

  private void validateCategories() throws CompilerException {
    boolean err = false;
    for (Category category : lang.getCategories().values()) {
      if (!CATEGORIES.contains(category.getName())) {
        LOGGER.error(
            String.format(
                "Category '%s' must be one of (%s)",
                category.getName(), String.join(", ", CATEGORIES)));
        err = true;
      }
    }
    if (err) {
      throw error();
    }
  }

  private void _generate() throws IOException, CompilerException {
    AssetGenerator ag = new AssetGenerator(LOGGER, pkg, output, icons);
    for (Asset asset : lang.getAssets().values()) {
      ag.generate(asset);
    }

    createAutoLangLink();
    createMetaData();
    createAttacker();

    LOGGER.info(String.format("Created %d classes", lang.getAssets().size()));
  }

  private void createAutoLangLink() throws IOException {
    LOGGER.debug("Creating 'AutoLangLink.java'");
    TypeSpec.Builder builder = TypeSpec.enumBuilder("AutoLangLink");
    builder.addModifiers(Modifier.PUBLIC);
    ClassName linkClass = ClassName.get("com.foreseeti.corelib", "Link");
    builder.addSuperinterface(linkClass);

    for (Link link : lang.getLinks()) {
      LOGGER.debug(
          String.format(
              "Adding link '%s' <- %s -> '%s'",
              link.getLeftField().getName(), link.getName(), link.getRightField().getName()));
      builder.addEnumConstant(
          String.format("%s_%s", link.getLeftField().getName(), link.getRightField().getName()),
          TypeSpec.anonymousClassBuilder("$S", link.getName()).build());
    }
    builder.addField(String.class, "name", Modifier.PRIVATE, Modifier.FINAL);

    // constructor
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    constructor.addParameter(String.class, "name");
    constructor.addStatement("this.name = name");
    builder.addMethod(constructor.build());

    MethodSpec.Builder getName = MethodSpec.methodBuilder("getName");
    getName.addAnnotation(Override.class);
    getName.addModifiers(Modifier.PUBLIC);
    getName.returns(String.class);
    getName.addStatement("return this.name");
    builder.addMethod(getName.build());

    JavaFile javaFile = JavaFile.builder(this.pkg, builder.build()).build();
    javaFile.writeTo(this.output);
  }

  private void createMetaData() throws IOException {
    LOGGER.debug("Creating 'MetaData.java'");
    TypeSpec.Builder builder = TypeSpec.classBuilder("MetaData");
    builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    ClassName hashMap = ClassName.get(HashMap.class);

    FieldSpec.Builder fb =
        FieldSpec.builder(
            String.class, "MAL_VERSION", Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
    fb.initializer("$S", MalInfo.getVersion());
    builder.addField(fb.build());

    fb = FieldSpec.builder(String.class, "ID", Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
    fb.initializer("$S", lang.getDefine("id"));
    builder.addField(fb.build());

    fb =
        FieldSpec.builder(
            String.class, "VERSION", Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
    fb.initializer("$S", lang.getDefine("version"));
    builder.addField(fb.build());

    TypeName type = ParameterizedTypeName.get(Map.class, String.class, String.class);
    builder.addField(type, "DATA", Modifier.PUBLIC, Modifier.STATIC);
    CodeBlock.Builder codeBlock = CodeBlock.builder();
    codeBlock.addStatement("DATA = new $T<>()", hashMap);
    for (Entry<String, String> entry : lang.getDefines().entrySet()) {
      LOGGER.debug(String.format("Adding define '%s' = '%s'", entry.getKey(), entry.getValue()));
      codeBlock.addStatement("DATA.put($S, $S)", entry.getKey(), entry.getValue());
    }
    builder.addStaticBlock(codeBlock.build());

    JavaFile javaFile = JavaFile.builder(this.pkg, builder.build()).build();
    javaFile.writeTo(this.output);
  }

  private void createAttacker() throws IOException, CompilerException {
    LOGGER.debug("Creating 'Attacker.java'");
    String resourcePath = "/securicad/Attacker.java";
    InputStream is = Generator.class.getResourceAsStream(resourcePath);
    if (is == null) {
      throw error(String.format("Couldn't get resource %s", resourcePath));
    }
    String code = String.format("package %s;\n\n%s", this.pkg, new String(is.readAllBytes()));
    Files.writeString(
        new File(new File(output, this.pkg.replaceAll("\\.", "/")), "Attacker.java").toPath(),
        code);
  }
}

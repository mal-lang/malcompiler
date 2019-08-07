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
package com.foreseeti.mal.lib.generator;

import com.foreseeti.mal.lib.CompilerException;
import com.foreseeti.mal.lib.Lang;
import com.foreseeti.mal.lib.Lang.Asset;
import com.foreseeti.mal.lib.Lang.AttackStep;
import com.foreseeti.mal.lib.Lang.AttackStepType;
import com.foreseeti.mal.lib.Lang.Field;
import com.foreseeti.mal.lib.Lang.StepAttackStep;
import com.foreseeti.mal.lib.Lang.StepBinOp;
import com.foreseeti.mal.lib.Lang.StepCollect;
import com.foreseeti.mal.lib.Lang.StepDifference;
import com.foreseeti.mal.lib.Lang.StepExpr;
import com.foreseeti.mal.lib.Lang.StepField;
import com.foreseeti.mal.lib.Lang.StepIntersection;
import com.foreseeti.mal.lib.Lang.StepTransitive;
import com.foreseeti.mal.lib.Lang.StepUnion;
import com.foreseeti.mal.lib.Lang.TTCExpr;
import com.foreseeti.mal.lib.Lang.TTCFunc;
import com.foreseeti.mal.lib.MalLogger;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;

public class ReferenceGenerator {
  private final String pkg;
  private final MalLogger LOGGER;
  private final File output;
  private final Lang lang;
  private final boolean core;

  public static void generate(Lang lang, Map<String, String> args)
      throws CompilerException, IOException {
    generate(lang, args, false, false);
  }

  public static void generate(Lang lang, Map<String, String> args, boolean verbose, boolean debug)
      throws CompilerException, IOException {
    new ReferenceGenerator(lang, args, verbose, debug).generateLog();
  }

  private ReferenceGenerator(Lang lang, Map<String, String> args, boolean verbose, boolean debug)
      throws CompilerException {
    Locale.setDefault(Locale.ROOT);
    LOGGER = new MalLogger("GENERATOR", verbose, debug);
    try {
      this.lang = lang;
      if (!args.containsKey("path") || args.get("path").isBlank()) {
        LOGGER.error("Reference generator requires argument 'path'");
        throw new CompilerException("There were generator errors");
      } else {
        this.output = new File(args.get("path"));
        if (!this.output.isAbsolute()) {
          LOGGER.error("Argument 'path' must be an absolute path");
          throw new CompilerException("There were generator errors");
        } else if (output.isFile()) {
          LOGGER.error("Argument 'path' is a file but must be an empty directory");
          throw new CompilerException("There were generator errors");
        } else if (output.isDirectory() && output.listFiles().length != 0) {
          LOGGER.error("Argument 'path' must be an empty directory");
          throw new CompilerException("There were generator errors");
        }
      }
      if (!args.containsKey("package") || args.get("package").isBlank()) {
        LOGGER.warning("Missing optional argument 'package', using default");
        this.pkg = "auto";
      } else {
        this.pkg = args.get("package");
      }
      if (!args.containsKey("core")) {
        this.core = true;
      } else {
        switch (args.get("core").toLowerCase().trim()) {
          case "true":
            this.core = true;
            break;
          case "false":
            this.core = false;
            break;
          default:
            LOGGER.error("Optional argument 'core' must be either 'true' or 'false'");
            throw new CompilerException("There were generator errors");
        }
      }
      checkRestricted();
    } catch (CompilerException e) {
      LOGGER.print();
      throw e;
    }
  }

  private void checkRestricted() throws CompilerException {
    boolean err = false;
    if (!SourceVersion.isName(pkg)) {
      LOGGER.error(String.format("Package '%s' is not a valid package name", pkg));
      err = true;
    }
    for (Asset asset : lang.getAssets().values()) {
      if (SourceVersion.isKeyword(asset.getName())) {
        LOGGER.error(String.format("Asset '%s' is a java keyword", asset.getName()));
        err = true;
      }
      for (AttackStep attackStep : asset.getAttackSteps().values()) {
        if (SourceVersion.isKeyword(attackStep.getName())) {
          LOGGER.error(
              String.format(
                  "Attack step '%s' in asset '%s' is a java keyword",
                  attackStep.getName(), asset.getName()));
          err = true;
        }
      }
      for (Field field : asset.getFields().values()) {
        if (SourceVersion.isKeyword(field.getName())) {
          LOGGER.error(
              String.format(
                  "Field '%s' in asset '%s' is a java keyword", field.getName(), asset.getName()));
          err = true;
        }
      }
    }
    if (err) {
      throw new CompilerException("There were generator errors");
    }
  }

  private void generateLog() throws IOException, CompilerException {
    try {
      _generate();
      LOGGER.print();
    } catch (IOException | CompilerException e) {
      LOGGER.print();
      throw e;
    }
  }

  private void _generate() throws IOException, CompilerException {
    for (Asset asset : lang.getAssets().values()) {
      JavaFile javaFile = JavaFile.builder(pkg, createAsset(asset)).build();
      javaFile.writeTo(this.output);
    }
    if (core) {
      _generateCore();
    }
    _generateProfile();
    LOGGER.info(String.format("Created %d classes", lang.getAssets().size()));
  }

  private void _generateCore() throws IOException {
    File outputFile = new File(output, "core");
    outputFile.mkdirs();

    List<String> fileNames =
        Arrays.asList(
            "Asset", "Attacker", "AttackStep", "AttackStepMax", "AttackStepMin", "Defense");
    for (String fileName : fileNames) {
      String name = String.format("%s.java", fileName);
      String resourcePath = String.format("/generator/reference/%s", name);
      InputStream is = ReferenceGenerator.class.getResourceAsStream(resourcePath);
      File destination = new File(outputFile, name);
      Files.copy(is, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private void _generateProfile() throws CompilerException, IOException {
    File out = new File(output, "attackerProfile.ttc");
    FileWriter fw = new FileWriter(out);
    for (Asset asset : lang.getAssets().values()) {
      for (AttackStep attackStep : asset.getAttackSteps().values()) {
        String dist = "Zero";
        if (attackStep.hasTTC()) {
          TTCExpr expr = attackStep.getTTC();
          if (expr instanceof TTCFunc) {
            dist = ((TTCFunc) expr).dist.toString();
          } else {
            fw.close();
            LOGGER.error(
                String.format(
                    "Advanced TTC, used at %s.%s, is not supported",
                    asset.getName(), attackStep.getName()));
            throw new CompilerException("There were generator errors");
          }
        }
        fw.write(String.format("%s.%s = %s\n", asset.getName(), attackStep.getName(), dist));
      }
    }
    fw.close();
  }

  /** Name generator to avoid variable duplication. */
  private static class Name {
    private static int value = 0;

    public static String get() {
      return String.format("_%s", Integer.toHexString(value++));
    }

    public static void reset() {
      value = 0;
    }
  }

  private static String ucFirst(String str) {
    if (str.isEmpty()) {
      return str;
    } else {
      return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
    }
  }

  private static ClassName getExtend(AttackStep as) {
    switch (as.getType()) {
      case ALL:
        return ClassName.get("core", "AttackStepMax");
      case ANY:
        return ClassName.get("core", "AttackStepMin");
      case DEFENSE:
      case EXIST:
      case NOTEXIST:
        return ClassName.get("core", "Defense");
      default:
        throw new RuntimeException(String.format("unknown attack step type '%s'", as.getType()));
    }
  }

  /**
   * Don't call this directly, see {@link #getParameters(Asset)}.
   *
   * <p>Adds the current assets defenses as parameter names. Params is set to deal with duplicates
   * since assets can override defenses.
   *
   * @param asset, current asset
   * @param params, parameter list
   * @return updated parameter list
   */
  private static LinkedHashSet<String> getParameters(Asset asset, LinkedHashSet<String> params) {
    for (AttackStep attackStep : asset.getAttackSteps().values()) {
      if (attackStep.isDefense()) {
        params.add(String.format("is%sEnabled", ucFirst(attackStep.getName())));
      }
    }
    if (asset.hasSuperAsset()) {
      return getParameters(asset.getSuperAsset(), params);
    }
    return params;
  }

  /**
   * Returns a list of defense parameter names from all parents in order.
   *
   * @param asset
   * @return
   */
  private static LinkedHashSet<String> getParameters(Asset asset) {
    if (asset.hasSuperAsset()) {
      return getParameters(asset.getSuperAsset(), new LinkedHashSet<>());
    } else {
      return new LinkedHashSet<>();
    }
  }

  private TypeSpec createAsset(Asset asset) {
    LOGGER.info(String.format("Creating '%s.java'", asset.getName()));
    TypeSpec.Builder builder = TypeSpec.classBuilder(asset.getName());
    builder.addModifiers(Modifier.PUBLIC);
    if (asset.isAbstract()) {
      builder.addModifiers(Modifier.ABSTRACT);
    }
    if (asset.hasSuperAsset()) {
      ClassName parent = ClassName.get(pkg, asset.getSuperAsset().getName());
      builder.superclass(parent);
    } else {
      ClassName parent = ClassName.get("core", "Asset");
      builder.superclass(parent);
    }

    // Normal constructor with all parameters, for normal assets this will only be (name), for
    // assets containing or inheriting defenses it will be (name, isDef1, isDef2...)
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    constructor.addParameter(String.class, "name");
    LinkedHashSet<String> params = getParameters(asset); // get parents parameters, if any
    if (params.isEmpty()) {
      constructor.addStatement("super(name)");
    } else {
      constructor.addStatement("super(name, $L)", String.join(", ", params));
    }
    constructor.addStatement("assetClassName = $S", asset.getName());
    ClassName as = ClassName.get("core", "AttackStep");
    ClassName defense = ClassName.get("core", "Defense");
    for (AttackStep attackStep : asset.getAttackSteps().values()) {
      ClassName type = ClassName.get(pkg, asset.getName(), ucFirst(attackStep.getName()));
      if (!attackStep.hasParent()) {
        // Step is not previously defined in parent, create a field for this class
        builder.addField(type, attackStep.getName(), Modifier.PUBLIC);
      }
      if (attackStep.isDefense() || attackStep.isConditionalDefense()) {
        // Is some sort of defense, remove from all defenses
        constructor.beginControlFlow("if ($N != null)", attackStep.getName());
        constructor.addStatement("$T.allAttackSteps.remove($N.disable)", as, attackStep.getName());
        constructor.endControlFlow();
        constructor.addStatement("$T.allDefenses.remove($N)", defense, attackStep.getName());
      } else {
        // Is normal attack step, remove from all attack steps
        constructor.addStatement("$T.allAttackSteps.remove($N)", as, attackStep.getName());
      }
      if (attackStep.isDefense()) {
        // Is defense that can be enabled/disabled on instantiation - add a parameter to paramlist
        // and create a new instance of the defense
        String param = String.format("is%sEnabled", ucFirst(attackStep.getName()));
        params.add(param);
        constructor.addStatement("$N = new $T(name, $N)", attackStep.getName(), type, param);
      } else {
        // Create new instance of attack step
        constructor.addStatement("$N = new $T(name)", attackStep.getName(), type);
      }
      builder.addType(createAttackStep(attackStep).build());
    }
    // Add all parameters as booleans to constructor
    for (String param : params) {
      constructor.addParameter(boolean.class, param);
    }
    builder.addMethod(constructor.build());

    if (!params.isEmpty()) {
      // Constructor for (name), we copy the original constructor but for defenses we set depending
      // on its ttc
      constructor = MethodSpec.constructorBuilder();
      constructor.addModifiers(Modifier.PUBLIC);
      constructor.addParameter(String.class, "name");
      constructor.addStatement("super(name)");
      // ### COPIED from original
      constructor.addStatement("assetClassName = $S", asset.getName());
      for (AttackStep attackStep : asset.getAttackSteps().values()) {
        ClassName type = ClassName.get(pkg, asset.getName(), ucFirst(attackStep.getName()));
        if (attackStep.isDefense() || attackStep.isConditionalDefense()) {
          // Is some sort of defense, remove from all defenses
          constructor.beginControlFlow("if ($N != null)", attackStep.getName());
          constructor.addStatement(
              "$T.allAttackSteps.remove($N.disable)", as, attackStep.getName());
          constructor.endControlFlow();
          constructor.addStatement("$T.allDefenses.remove($N)", defense, attackStep.getName());
        } else {
          // Is normal attack step, remove from all attack steps
          constructor.addStatement("$T.allAttackSteps.remove($N)", as, attackStep.getName());
        }
        if (attackStep.isDefense()) {
          if (!attackStep.hasTTC()) {
            constructor.addStatement("$N = new $T(name, false)", attackStep.getName(), type);
          } else {
            TTCFunc func = (TTCFunc) attackStep.getTTC();
            if (func.dist.getMean() < 0.5) {
              constructor.addStatement("$N = new $T(name, false)", attackStep.getName(), type);
            } else {
              constructor.addStatement("$N = new $T(name, true)", attackStep.getName(), type);
            }
          }
        } else {
          // Create new instance of attack step
          constructor.addStatement("$N = new $T(name)", attackStep.getName(), type);
        }
      }
      // ### COPIED from original
      builder.addMethod(constructor.build());

      // Constructor for only defense booleans (isDef1, isDef2...)
      constructor = MethodSpec.constructorBuilder();
      constructor.addModifiers(Modifier.PUBLIC);
      for (String param : params) {
        constructor.addParameter(boolean.class, param);
      }
      constructor.addStatement("this($S, $L)", "Anonymous", String.join(", ", params));
      builder.addMethod(constructor.build());

      // Empty constructor ()
      constructor = MethodSpec.constructorBuilder();
      constructor.addModifiers(Modifier.PUBLIC);
      constructor.addStatement("this($S)", "Anonymous");
      builder.addMethod(constructor.build());
    } else {
      // No extra params, empty constructor ()
      constructor = MethodSpec.constructorBuilder();
      constructor.addModifiers(Modifier.PUBLIC);
      constructor.addStatement("this($S)", "Anonymous");
      builder.addMethod(constructor.build());
    }

    // Instantiating fields to either null or a HashSet of correct type
    ClassName set = ClassName.get(Set.class).forceQualify(true);
    ClassName hashSet = ClassName.get(HashSet.class).forceQualify(true);
    for (Field field : asset.getFields().values()) {
      TypeName type = ClassName.get(pkg, field.getTarget().getAsset().getName());
      if (field.getMax() > 1) {
        type = ParameterizedTypeName.get(set, type);
      }
      FieldSpec.Builder fieldBuilder = FieldSpec.builder(type, field.getName());
      fieldBuilder.addModifiers(Modifier.PUBLIC);
      if (field.getMax() > 1) {
        fieldBuilder.initializer("new $T<>()", hashSet);
      } else {
        fieldBuilder.initializer("null");
      }
      builder.addField(fieldBuilder.build());
      builder.addMethod(createFieldAdder(field).build());
    }

    // Extra methods for every asset
    createExtra(builder, asset);

    return builder.build();
  }

  private MethodSpec.Builder createUpdateChildren(AttackStep attackStep) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("updateChildren");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    ClassName set = ClassName.get(Set.class).forceQualify(true);
    ClassName as = ClassName.get("core", "AttackStep");
    TypeName asSet = ParameterizedTypeName.get(set, as);
    builder.addParameter(asSet, "attackSteps");
    if (attackStep.inheritsReaches()) {
      builder.addStatement("super.updateChildren(attackSteps)");
    }
    for (StepExpr expr : attackStep.getReaches()) {
      AutoFlow af = new AutoFlow();
      AutoFlow end = createExpr(af, expr, attackStep.getAsset());
      end.addStatement("$N.updateTtc(this, ttc, attackSteps)", end.prefix);
      af.build(builder);
    }
    return builder;
  }

  private MethodSpec.Builder createSetExpectedParents(AttackStep attackStep) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("setExpectedParents");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    builder.addStatement("super.setExpectedParents()");
    for (StepExpr expr : attackStep.getParentSteps()) {
      AutoFlow af = new AutoFlow();
      AutoFlow end = createExpr(af, expr, attackStep.getAsset());
      end.addStatement("addExpectedParent($N)", end.prefix);
      af.build(builder);
    }
    return builder;
  }

  private static MethodSpec.Builder createLocalTtc(String assetName, String name) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("localTtc");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    builder.returns(double.class);
    builder.addStatement("return ttcHashMap.get($S)", String.format("%s.%s", assetName, name));
    return builder;
  }

  private static MethodSpec.Builder createFullName(String assetName, String name) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("fullName");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    builder.returns(String.class);
    builder.addStatement("return $S", String.format("%s.%s", assetName, name));
    return builder;
  }

  private TypeSpec.Builder createDisable(AttackStep attackStep) {
    TypeSpec.Builder builder = TypeSpec.classBuilder("Disable");
    builder.addModifiers(Modifier.PUBLIC);
    if (attackStep.hasParent()) {
      AttackStep parent = attackStep.getAsset().getSuperAsset().getAttackStep(attackStep.getName());
      Asset parentAsset = parent.getAsset();
      builder.superclass(
          ClassName.get(pkg, parentAsset.getName(), ucFirst(parent.getName()), "Disable"));
    } else {
      builder.superclass(ClassName.get("core", "AttackStepMin"));
    }

    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    constructor.addParameter(String.class, "name");
    constructor.addStatement("super(name)");
    builder.addMethod(constructor.build());

    if (!attackStep.getReaches().isEmpty()) {
      builder.addMethod(createUpdateChildren(attackStep).build());
    }
    if (!attackStep.getParentSteps().isEmpty()) {
      builder.addMethod(createSetExpectedParents(attackStep).build());
    }
    builder.addMethod(
        createFullName(attackStep.getAsset().getName(), attackStep.getName()).build());

    return builder;
  }

  private void createDefense(TypeSpec.Builder builder, AttackStep attackStep) {
    LOGGER.debug(String.format("Creating defense '%s'", ucFirst(attackStep.getName())));
    // Defense constructor with only (name)
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    constructor.addParameter(String.class, "name");
    if (!attackStep.hasTTC()) {
      constructor.addStatement("this(name, false)");
    } else {
      TTCFunc func = (TTCFunc) attackStep.getTTC();
      if (func.dist.getMean() < 0.5) {
        constructor.addStatement("this(name, false)");
      } else {
        constructor.addStatement("this(name, true)");
      }
    }
    builder.addMethod(constructor.build());

    // Defense constructor with both name and if it is enabled (name, isEnabled)
    constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    constructor.addParameter(String.class, "name");
    constructor.addParameter(Boolean.class, "isEnabled");
    constructor.addStatement("super(name)");
    constructor.addStatement("defaultValue = isEnabled");
    ClassName type =
        ClassName.get(
            pkg, attackStep.getAsset().getName(), ucFirst(attackStep.getName()), "Disable");
    constructor.addStatement("disable = new $T(name)", type);
    builder.addMethod(constructor.build());
  }

  private void createConditionalDefense(TypeSpec.Builder builder, AttackStep attackStep) {
    LOGGER.debug(String.format("Creating conditional defense '%s'", ucFirst(attackStep.getName())));
    // Conditional defense constructor with only (name)
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    constructor.addParameter(String.class, "name");
    constructor.addStatement("super(name)");
    ClassName type =
        ClassName.get(
            pkg, attackStep.getAsset().getName(), ucFirst(attackStep.getName()), "Disable");
    constructor.addStatement("disable = new $T(name)", type);
    builder.addMethod(constructor.build());

    // Overriding the isEnabled method, defense will be enabled if all requirements exist
    MethodSpec.Builder method = MethodSpec.methodBuilder("isEnabled");
    method.addAnnotation(Override.class);
    method.addModifiers(Modifier.PUBLIC);
    method.returns(boolean.class);
    if (attackStep.getType() == AttackStepType.EXIST) {
      for (StepExpr expr : attackStep.getRequires()) {
        AutoFlow af = new AutoFlow();
        AutoFlow end = createExpr(af, expr, attackStep.getAsset());
        end.addStatement("return false");
        af.build(method);
      }
      method.addStatement("return true");
    } else {
      method.addStatement("int count = $L", attackStep.getRequires().size());
      for (StepExpr expr : attackStep.getRequires()) {
        AutoFlow af = new AutoFlow();
        AutoFlow end = createExpr(af, expr, attackStep.getAsset());
        end.addStatement("count--");
        if (end.isLoop()) {
          end.addStatement("break");
        }
        af.build(method);
      }
      method.addStatement("return count == 0");
    }
    builder.addMethod(method.build());
  }

  private TypeSpec.Builder createAttackStep(AttackStep attackStep) {
    Name.reset();
    TypeSpec.Builder builder = TypeSpec.classBuilder(ucFirst(attackStep.getName()));
    builder.addModifiers(Modifier.PUBLIC);
    if (attackStep.hasParent()) {
      AttackStep parent = attackStep.getAsset().getSuperAsset().getAttackStep(attackStep.getName());
      Asset parentAsset = parent.getAsset();
      builder.superclass(ClassName.get(pkg, parentAsset.getName(), ucFirst(parent.getName())));
    } else {
      builder.superclass(getExtend(attackStep));
    }

    if (attackStep.isDefense()) {
      createDefense(builder, attackStep);
      builder.addType(createDisable(attackStep).build());
    } else if (attackStep.isConditionalDefense()) {
      createConditionalDefense(builder, attackStep);
      builder.addType(createDisable(attackStep).build());
    } else {
      LOGGER.debug(String.format("Creating attack step '%s'", ucFirst(attackStep.getName())));
      // Attack step constructor with only (name)
      MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
      constructor.addModifiers(Modifier.PUBLIC);
      constructor.addParameter(String.class, "name");
      constructor.addStatement("super(name)");
      builder.addMethod(constructor.build());

      if (!attackStep.getReaches().isEmpty()) {
        builder.addMethod(createUpdateChildren(attackStep).build());
      }
      if (!attackStep.getParentSteps().isEmpty()) {
        builder.addMethod(createSetExpectedParents(attackStep).build());
      }
      builder.addMethod(
          createLocalTtc(attackStep.getAsset().getName(), attackStep.getName()).build());
    }

    return builder;
  }

  /**
   * Creates helper method to add instances of assets to fields. Connects it both ways.
   *
   * @param field, field to create method for
   * @return method builder for the helper method
   */
  private MethodSpec.Builder createFieldAdder(Field field) {
    ClassName type = ClassName.get(pkg, field.getTarget().getAsset().getName());
    String name = String.format("add%s", ucFirst(field.getName()));
    MethodSpec.Builder builder = MethodSpec.methodBuilder(name);
    builder.addModifiers(Modifier.PUBLIC);
    builder.addParameter(type, field.getName());
    if (field.getMax() > 1) {
      builder.addStatement("this.$N.add($N)", field.getName(), field.getName());
    } else {
      builder.addStatement("this.$N = $N", field.getName(), field.getName());
    }
    if (field.getTarget().getMax() > 1) {
      builder.addStatement("$N.$N.add(this)", field.getName(), field.getTarget().getName());
    } else {
      builder.addStatement("$N.$N = this", field.getName(), field.getTarget().getName());
    }
    return builder;
  }

  /** Statement is an unevaluated javapoet statement. */
  private class Statement {
    public final String format;
    public final Object[] args;

    public Statement(String format, Object[] args) {
      this.format = format;
      this.args = args;
    }

    public void build(MethodSpec.Builder builder) {
      builder.addStatement(format, args);
    }
  }

  /**
   * Control flows in javapoet usually requires manual closing. AutoFlow does this automatically
   * when built. AutoFlow can store statements or other AutoFlows. Field prefix is to ease tracking
   * the variable names when nestling scopes.
   */
  private class AutoFlow extends Statement {
    private final String prefix;
    private boolean loop;
    private List<Statement> statements;

    public AutoFlow() {
      this("");
    }

    public AutoFlow(String prefix) {
      this(prefix, "", new Object[0]);
    }

    public AutoFlow(String prefix, String format, Object... args) {
      super(format, args);
      this.prefix = prefix;
      statements = new ArrayList<>();
    }

    public AutoFlow(String prefix, boolean loop, String format, Object... args) {
      this(prefix, format, args);
      this.loop = loop;
    }

    public boolean hasPrefix() {
      return !prefix.isEmpty();
    }

    public boolean isLoop() {
      return loop;
    }

    public AutoFlow addStatement(AutoFlow af) {
      if (loop) {
        af.loop = loop;
      }
      statements.add(af);
      return af;
    }

    public Statement addStatement(String format, Object... args) {
      Statement statement = new Statement(format, args);
      statements.add(statement);
      return statement;
    }

    @Override
    public void build(MethodSpec.Builder builder) {
      // Don't build autoflow without a format, this way we can have a single top-level autoflow and
      // build that when needed.
      if (!format.isEmpty()) {
        builder.beginControlFlow(format, args);
      }
      for (Statement statement : statements) {
        statement.build(builder);
      }
      if (!format.isEmpty()) {
        builder.endControlFlow();
      }
    }
  }

  private AutoFlow createStepField(AutoFlow af, StepField expr) {
    String name = expr.field.getName();
    if (af.hasPrefix()) {
      name = String.format("%s.%s", af.prefix, name);
    }
    if (expr.field.getMax() > 1) {
      // field is set
      ClassName targetType = ClassName.get(pkg, expr.field.getTarget().getAsset().getName());
      String prefix = Name.get();
      return af.addStatement(
          new AutoFlow(prefix, true, "for ($T $N : $N)", targetType, prefix, name));
    } else {
      return af.addStatement(new AutoFlow(name, "if ($L != null)", name));
    }
  }

  private AutoFlow createStepTransitive(AutoFlow af, StepTransitive expr, Asset asset) {
    ClassName targetType = ClassName.get(pkg, expr.target.getName());
    ClassName list = ClassName.get(List.class).forceQualify(true);
    ClassName arrayList = ClassName.get(ArrayList.class).forceQualify(true);
    ClassName set = ClassName.get(Set.class).forceQualify(true);
    ClassName hashSet = ClassName.get(HashSet.class).forceQualify(true);
    TypeName targetSet = ParameterizedTypeName.get(set, targetType);
    TypeName targetList = ParameterizedTypeName.get(list, targetType);
    String name1 = Name.get();
    String name2 = Name.get();
    af.addStatement("$T $N = new $T<>()", targetSet, name1, hashSet);
    af.addStatement("$T $N = new $T<>()", targetList, name2, arrayList);
    if (!expr.src.equals(expr.target)) {
      af = subType(af, expr.src, expr.target, asset);
    }
    if (af.hasPrefix()) {
      af.addStatement("$N.add($N)", name1, af.prefix);
      af.addStatement("$N.add($N)", name2, af.prefix);
    } else {
      ClassName parentType = ClassName.get(pkg, asset.getName());
      af.addStatement("$N.add($T.this)", name1, parentType);
      af.addStatement("$N.add($T.this)", name2, parentType);
    }

    String name3 = Name.get();
    AutoFlow naf = af.addStatement(new AutoFlow(name3, true, "while (!$N.isEmpty())", name2));
    naf.addStatement("$T $N = $N.remove(0)", targetType, name3, name2);
    AutoFlow deep = createExpr(naf, expr.e, asset);
    deep.addStatement("$N.add($N)", name1, deep.prefix);
    deep.addStatement("$N.add($N)", name2, deep.prefix);
    String name4 = Name.get();
    return af.addStatement(new AutoFlow(name4, true, "for ($T $N : $N)", targetType, name4, name1));
  }

  private AutoFlow createStepSet(AutoFlow af, StepExpr expr, Asset asset) {
    StepBinOp binop = (StepBinOp) expr;
    String targetName = binop.target.getName();
    ClassName targetType = ClassName.get(pkg, targetName);
    ClassName set = ClassName.get(Set.class).forceQualify(true);
    ClassName hashSet = ClassName.get(HashSet.class).forceQualify(true);
    TypeName targetSet = ParameterizedTypeName.get(set, targetType);
    String name1 = Name.get();
    String name2 = Name.get();
    af.addStatement("$T $N = new $T<>()", targetSet, name1, hashSet);
    af.addStatement("$T $N = new $T<>()", targetSet, name2, hashSet);

    AutoFlow deep1 = createExpr(af, binop.lhs, asset);
    deep1.addStatement("$N.add($N)", name1, deep1.prefix);
    AutoFlow deep2 = createExpr(af, binop.rhs, asset);
    deep2.addStatement("$N.add($N)", name2, deep2.prefix);

    if (expr instanceof StepUnion) {
      af.addStatement("$N.addAll($N)", name1, name2);
    } else if (expr instanceof StepIntersection) {
      af.addStatement("$N.retainAll($N)", name1, name2);
    } else {
      af.addStatement("$N.removeAll($N)", name1, name2);
    }
    String name3 = Name.get();
    return af.addStatement(new AutoFlow(name3, true, "for ($T $N : $N)", targetType, name3, name1));
  }

  private AutoFlow createStepAttackStep(AutoFlow af, StepAttackStep expr) {
    String name = expr.attackStep.getName();
    if (af.hasPrefix()) {
      name = String.format("%s.%s", af.prefix, name);
    }
    if (expr.attackStep.isDefense() || expr.attackStep.isConditionalDefense()) {
      name = String.format("%s.disable", name);
    }
    return af.addStatement(new AutoFlow(name));
  }

  private AutoFlow subType(AutoFlow af, Asset source, Asset subSource, Asset asset) {
    if (source != null && subSource != null && !source.equals(subSource)) {
      ClassName type = ClassName.get(pkg, subSource.getName());
      if (af.hasPrefix()) {
        String prefix = String.format("((%s) %s)", subSource.getName(), af.prefix);
        return af.addStatement(new AutoFlow(prefix, "if ($N instanceof $T)", af.prefix, type));
      } else {
        // no prefix, use CLASS.this
        String prefix = String.format("((%s) %s.this)", subSource.getName(), asset.getName());
        return af.addStatement(
            new AutoFlow(prefix, "if ($N.this instanceof $T)", asset.getName(), type));
      }
    }
    return af;
  }

  private AutoFlow createExpr(AutoFlow af, StepExpr expr, Asset asset) {
    af = subType(af, expr.src, expr.subSrc, asset);
    if (expr instanceof StepCollect) {
      af = createExpr(af, ((StepCollect) expr).lhs, asset);
      af = createExpr(af, ((StepCollect) expr).rhs, asset);
    } else if (expr instanceof StepField) {
      af = createStepField(af, (StepField) expr);
    } else if (expr instanceof StepTransitive) {
      af = createStepTransitive(af, (StepTransitive) expr, asset);
    } else if (expr instanceof StepUnion
        || expr instanceof StepIntersection
        || expr instanceof StepDifference) {
      af = createStepSet(af, expr, asset);
    } else if (expr instanceof StepAttackStep) {
      af = createStepAttackStep(af, (StepAttackStep) expr);
    } else {
      throw new RuntimeException(String.format("unknown expression '%s'", expr));
    }
    af = subType(af, expr.target, expr.subTarget, asset);
    return af;
  }

  private void createExtra(TypeSpec.Builder assetBuilder, Asset asset) {
    if (!asset.getFields().isEmpty()) {
      MethodSpec.Builder builder = MethodSpec.methodBuilder("getAssociatedAssetClassName");
      builder.addAnnotation(Override.class);
      builder.addModifiers(Modifier.PUBLIC);
      builder.returns(String.class);
      builder.addParameter(String.class, "field");
      boolean started = false;
      for (Field field : asset.getFields().values()) {
        if (!started) {
          builder.beginControlFlow("if ($N.equals($S))", "field", field.getName());
          started = true;
        } else {
          builder.nextControlFlow("else if ($N.equals($S))", "field", field.getName());
        }
        ClassName type = ClassName.get(pkg, field.getTarget().getAsset().getName());
        builder.addStatement("return $T.class.getName()", type);
      }
      builder.endControlFlow();
      builder.addStatement("return $S", "");
      assetBuilder.addMethod(builder.build());

      builder = MethodSpec.methodBuilder("getAssociatedAssets");
      builder.addAnnotation(Override.class);
      builder.addModifiers(Modifier.PUBLIC);
      ClassName set = ClassName.get(Set.class).forceQualify(true);
      ClassName hashSet = ClassName.get(HashSet.class).forceQualify(true);
      ClassName assetType = ClassName.get("core", "Asset");
      TypeName assetSet = ParameterizedTypeName.get(set, assetType);
      builder.returns(assetSet);
      builder.addParameter(String.class, "field");
      builder.addStatement("$T assets = new $T<>()", assetSet, hashSet);
      started = false;
      for (Field field : asset.getFields().values()) {
        if (!started) {
          builder.beginControlFlow("if ($N.equals($S))", "field", field.getName());
          started = true;
        } else {
          builder.nextControlFlow("else if ($N.equals($S))", "field", field.getName());
        }
        if (field.getMax() > 1) {
          builder.addStatement("$N.addAll($N)", "assets", field.getName());
        } else {
          builder.beginControlFlow("if ($N != null)", field.getName());
          builder.addStatement("$N.add($N)", "assets", field.getName());
          builder.endControlFlow();
        }
      }
      builder.endControlFlow();
      builder.addStatement("return $N", "assets");
      assetBuilder.addMethod(builder.build());

      builder = MethodSpec.methodBuilder("getAllAssociatedAssets");
      builder.addAnnotation(Override.class);
      builder.addModifiers(Modifier.PUBLIC);
      builder.returns(assetSet);
      builder.addStatement("$T assets = new $T<>()", assetSet, hashSet);
      for (Field field : asset.getFields().values()) {
        if (field.getMax() > 1) {
          builder.addStatement("$N.addAll($N)", "assets", field.getName());
        } else {
          builder.beginControlFlow("if ($N != null)", field.getName());
          builder.addStatement("$N.add($N)", "assets", field.getName());
          builder.endControlFlow();
        }
      }
      builder.addStatement("return $N", "assets");
      assetBuilder.addMethod(builder.build());
    }
  }
}

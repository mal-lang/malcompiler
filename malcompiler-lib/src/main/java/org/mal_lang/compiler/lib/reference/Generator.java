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
package org.mal_lang.compiler.lib.reference;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.JavaGenerator;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.lib.Lang.Asset;
import org.mal_lang.compiler.lib.Lang.AttackStep;
import org.mal_lang.compiler.lib.Lang.AttackStepType;
import org.mal_lang.compiler.lib.Lang.Field;
import org.mal_lang.compiler.lib.Lang.StepExpr;
import org.mal_lang.compiler.lib.Lang.TTCExpr;
import org.mal_lang.compiler.lib.Lang.TTCFunc;

public class Generator extends JavaGenerator {
  private final File output;
  private final Lang lang;
  private final boolean core;

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
    Locale.setDefault(Locale.ROOT);
    this.lang = lang;
    if (!args.containsKey("path") || args.get("path").isBlank()) {
      throw error("Reference generator requires argument 'path'");
    }
    this.output = getOutputDirectory(args.get("path"));
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
          throw error("Optional argument 'core' must be either 'true' or 'false'");
      }
    }

    validateNames(this.lang);
    checkSteps(this.lang);
    fillAlwaysQualifiedNames(this.lang);
  }

  private void _generate() throws IOException, CompilerException {
    for (Asset asset : lang.getAssets().values()) {
      var javaFile = JavaFile.builder(pkg, createAsset(asset)).build();
      javaFile.writeTo(this.output);
    }
    if (core) {
      _generateCore();
    }
    _generateProfile();
    LOGGER.info(String.format("Created %d classes", lang.getAssets().size()));
  }

  private void _generateCore() throws IOException, CompilerException {
    File outputFile = new File(output, "core");
    outputFile.mkdirs();

    List<String> fileNames =
        Arrays.asList(
            "Asset", "Attacker", "AttackStep", "AttackStepMax", "AttackStepMin", "Defense");
    for (String fileName : fileNames) {
      String name = String.format("%s.java", fileName);
      String resourcePath = String.format("/reference/%s", name);
      InputStream is = Generator.class.getResourceAsStream(resourcePath);
      if (is == null) {
        throw error(String.format("Couldn't get resource %s", resourcePath));
      }
      File destination = new File(outputFile, name);
      Files.copy(is, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private void _generateProfile() throws CompilerException, IOException {
    File out = new File(output, "attackerProfile.ttc");
    try (var fw = new FileWriter(out)) {
      for (Asset asset : lang.getAssets().values()) {
        for (AttackStep attackStep : asset.getAttackSteps().values()) {
          String dist = "Zero";
          if (attackStep.hasTTC()) {
            TTCExpr expr = attackStep.getTTC();
            if (expr instanceof TTCFunc) {
              dist = ((TTCFunc) expr).dist.toString();
            } else {
              fw.close();
              throw error(
                  String.format(
                      "Advanced TTC, used at %s.%s, is not supported",
                      asset.getName(), attackStep.getName()));
            }
          }
          fw.write(String.format("%s.%s = %s%n", asset.getName(), attackStep.getName(), dist));
        }
      }
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
    if (asset.hasSuperAsset()) {
      params = getParameters(asset.getSuperAsset(), params);
    }
    for (AttackStep attackStep : asset.getAttackSteps().values()) {
      if (attackStep.isDefense()) {
        params.add(String.format("is%sEnabled", ucFirst(attackStep.getName())));
      }
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
      return new LinkedHashSet<>(getParameters(asset.getSuperAsset(), new LinkedHashSet<>()));
    } else {
      return new LinkedHashSet<>();
    }
  }

  private TypeSpec createAsset(Asset asset) {
    LOGGER.info(String.format("Creating '%s.java'", asset.getName()));
    TypeSpec.Builder builder = TypeSpec.classBuilder(asset.getName());
    builder.alwaysQualify(this.alwaysQualifiedNames);
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
      createAttackStep(builder, attackStep);
    }

    // Create all asset variables
    for (var variable : asset.getVariables().entrySet()) {
      createVariable(builder, variable.getKey(), variable.getValue(), asset);
    }
    for (var variable : asset.getReverseVariables().entrySet()) {
      createVariable(builder, variable.getKey(), variable.getValue(), asset);
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
    ClassName set = ClassName.get(Set.class);
    ClassName hashSet = ClassName.get(HashSet.class);
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

  private void createVariable(
      TypeSpec.Builder parentBuilder, String name, StepExpr expr, Asset asset) {
    String setName = String.format("_cache%s", name);
    String methodName = String.format("_%s", name);

    MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
    builder.addModifiers(Modifier.PROTECTED);
    ClassName targetType = ClassName.get(pkg, expr.subTarget.getName());
    ClassName set = ClassName.get(Set.class);
    TypeName targetSet = ParameterizedTypeName.get(set, targetType);
    ClassName hashSet = ClassName.get(HashSet.class);
    builder.returns(targetSet);

    parentBuilder.addField(targetSet, setName, Modifier.PRIVATE);

    builder.beginControlFlow("if ($N == null)", setName);
    builder.addStatement("$N = new $T<>()", setName, hashSet);
    AutoFlow varFlow = new AutoFlow();
    AutoFlow end = generateExpr(varFlow, expr, asset);
    end.addStatement("$N.add($N)", setName, end.prefix);
    varFlow.build(builder);
    builder.endControlFlow();
    builder.addStatement("return $N", setName);

    parentBuilder.addMethod(builder.build());
  }

  private void createUpdateChildren(
      TypeSpec.Builder parentBuilder, AttackStep attackStep, String cacheName) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("updateChildren");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    ClassName set = ClassName.get(Set.class);
    ClassName as = ClassName.get("core", "AttackStep");
    TypeName asSet = ParameterizedTypeName.get(set, as);
    builder.addParameter(asSet, "attackSteps");
    if (attackStep.inheritsReaches()) {
      builder.addStatement("super.updateChildren(attackSteps)");
    }

    builder.beginControlFlow("if ($N == null)", cacheName);
    builder.addStatement("$N = new $T<>()", cacheName, HashSet.class);
    for (StepExpr expr : attackStep.getReaches()) {
      AutoFlow af = new AutoFlow();
      AutoFlow end = generateExpr(af, expr, attackStep.getAsset());
      end.addStatement("$N.add($N)", cacheName, end.prefix);
      af.build(builder);
    }
    builder.endControlFlow();

    builder.beginControlFlow("for ($T attackStep : $N)", as, cacheName);
    builder.addStatement("attackStep.updateTtc(this, ttc, attackSteps)");
    builder.endControlFlow();

    parentBuilder.addMethod(builder.build());
  }

  private MethodSpec.Builder createSetExpectedParents(AttackStep attackStep, String cacheName) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("setExpectedParents");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    builder.addStatement("super.setExpectedParents()");

    builder.beginControlFlow("if ($N == null)", cacheName);
    builder.addStatement("$N = new $T<>()", cacheName, HashSet.class);
    for (StepExpr expr : attackStep.getParentSteps()) {
      AutoFlow af = new AutoFlow();
      AutoFlow end = generateExpr(af, expr, attackStep.getAsset());
      end.addStatement("$N.add($N)", cacheName, end.prefix);
      af.build(builder);
    }
    builder.endControlFlow();

    ClassName as = ClassName.get("core", "AttackStep");
    builder.beginControlFlow("for ($T attackStep : $N)", as, cacheName);
    builder.addStatement("addExpectedParent(attackStep)");
    builder.endControlFlow();

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
      String name = String.format("_cacheChildren%s", ucFirst(attackStep.getName()));
      createSetField(builder, name);
      createUpdateChildren(builder, attackStep, name);
    }
    if (!attackStep.getParentSteps().isEmpty()) {
      String name = String.format("_cacheParent%s", ucFirst(attackStep.getName()));
      createSetField(builder, name);
      builder.addMethod(createSetExpectedParents(attackStep, name).build());
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
        AutoFlow end = generateExpr(af, expr, attackStep.getAsset());
        end.addStatement("return false");
        af.build(method);
      }
      method.addStatement("return true");
    } else {
      method.addStatement("int count = $L", attackStep.getRequires().size());
      for (StepExpr expr : attackStep.getRequires()) {
        AutoFlow af = new AutoFlow();
        AutoFlow end = generateExpr(af, expr, attackStep.getAsset());
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

  private void createAttackStep(TypeSpec.Builder parentBuilder, AttackStep attackStep) {
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
        String name = String.format("_cacheChildren%s", ucFirst(attackStep.getName()));
        createSetField(builder, name);
        createUpdateChildren(builder, attackStep, name);
      }

      if (!attackStep.getParentSteps().isEmpty()) {
        String name = String.format("_cacheParent%s", ucFirst(attackStep.getName()));
        createSetField(builder, name);
        builder.addMethod(createSetExpectedParents(attackStep, name).build());
      }
      builder.addMethod(
          createLocalTtc(attackStep.getAsset().getName(), attackStep.getName()).build());
    }

    parentBuilder.addType(builder.build());
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
      ClassName set = ClassName.get(Set.class);
      ClassName hashSet = ClassName.get(HashSet.class);
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

  private void createSetField(TypeSpec.Builder parentBuilder, String name) {
    ClassName set = ClassName.get("java.util", "Set");
    ClassName attackStep = ClassName.get("core", "AttackStep");
    TypeName type = ParameterizedTypeName.get(set, attackStep);
    FieldSpec.Builder builder = FieldSpec.builder(type, name);
    builder.addModifiers(Modifier.PRIVATE);
    parentBuilder.addField(builder.build());
  }
}

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

import com.kitfox.svg.app.beans.SVGIcon;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.lang.model.element.Modifier;
import org.mal_lang.compiler.lib.JavaGenerator;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.lib.Lang.Asset;
import org.mal_lang.compiler.lib.Lang.AttackStep;
import org.mal_lang.compiler.lib.Lang.Field;
import org.mal_lang.compiler.lib.Lang.Link;
import org.mal_lang.compiler.lib.MalLogger;

public class AssetGenerator extends JavaGenerator {
  private final File output;
  private final File icons;
  private final Lang lang;
  private final AttackStepGenerator asGen;
  private final DefenseGenerator defGen;
  private final VariableGenerator varGen;
  private final String[] alwaysQualifiedNames;

  protected AssetGenerator(
      MalLogger LOGGER,
      String pkg,
      File output,
      File icons,
      Lang lang,
      String[] alwaysQualifiedNames) {
    super(LOGGER, pkg);
    this.output = output;
    this.icons = icons;
    this.lang = lang;
    this.alwaysQualifiedNames = alwaysQualifiedNames;
    asGen = new AttackStepGenerator(LOGGER, pkg);
    defGen = new DefenseGenerator(LOGGER, pkg);
    varGen = new VariableGenerator(LOGGER, pkg);
  }

  protected void generate(Asset asset) throws IOException {
    LOGGER.info(String.format("Creating '%s.java'", asset.getName()));
    TypeSpec.Builder builder = TypeSpec.classBuilder(asset.getName());
    builder.alwaysQualify(this.alwaysQualifiedNames);

    // @annotations
    createAssetAnnotations(builder, asset);
    Generator.createMetaInfoAnnotations(builder, Generator.getMetaInfoMap(asset));

    // modifiers
    builder.addModifiers(Modifier.PUBLIC);
    if (asset.isAbstract()) {
      builder.addModifiers(Modifier.ABSTRACT);
    }

    // extends
    if (asset.hasSuperAsset()) {
      builder.superclass(ClassName.get(pkg, asset.getSuperAsset().getName()));
    } else {
      builder.superclass(ClassName.get("com.foreseeti.simulator", "MultiParentAsset"));
    }

    // class fields
    createFields(builder, asset);

    // constructors
    createEmptyConstructor(builder);
    createDefaultValueConstructor(builder);
    createCopyConstructor(builder, asset);

    // other
    createRegisterAssociations(builder, asset);
    createInitLists(builder, asset);
    createInitAttackSteps(builder, asset);

    // getters
    createGetDescription(builder, asset);
    createGetTTCColoringElements(builder, asset);
    if (!asset.hasSuperAsset()) {
      createGetAttackSteps(builder);
      createGetDefenses(builder);
    }
    if (this.icons != null && !asset.isAbstract()) {
      try {
        createGetIcon(builder, asset);
      } catch (IOException e) {
        LOGGER.error(
            String.format(
                "Failed to load icon for asset '%s', message: %s",
                asset.getName(), e.getMessage()));
      }
    }

    // local steps
    createLocalStep(builder, asset, "AttackStepMin");
    createLocalStep(builder, asset, "AttackStepMax");
    createLocalStep(builder, asset, "Defense");

    for (AttackStep attackStep : asset.getAttackSteps().values()) {
      if (attackStep.isDefense() || attackStep.isConditionalDefense()) {
        defGen.generate(builder, asset, attackStep);
      } else {
        asGen.generate(builder, asset, attackStep);
      }
    }

    // Generate variables
    for (var variable : asset.getVariables().entrySet()) {
      varGen.generate(builder, variable.getKey(), variable.getValue(), asset);
    }
    for (var variable : asset.getReverseVariables().entrySet()) {
      varGen.generate(builder, variable.getKey(), variable.getValue(), asset);
    }

    Set<String> variables = new LinkedHashSet<>();
    variables.addAll(asset.getVariables().keySet());
    variables.addAll(asset.getReverseVariables().keySet());
    if (!variables.isEmpty() || !asset.getAttackSteps().isEmpty()) {
      createClearCache(builder, asset, variables);
    }

    var file = JavaFile.builder(this.pkg, builder.build()).build();
    file.writeTo(this.output);
  }

  private void createClearCache(
      TypeSpec.Builder parentBuilder, Asset asset, Set<String> variables) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("clearGraphCache");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    if (asset.hasSuperAsset()) {
      builder.addStatement("super.clearGraphCache()");
    }
    for (var variable : variables) {
      builder.addStatement("_cache$N = null", variable);
    }
    for (var attackStep : asset.getAttackSteps().values()) {
      if (attackStep.isDefense() || attackStep.isConditionalDefense()) {
        builder.addStatement("$N.disable.clearGraphCache()", attackStep.getName());
      } else {
        builder.addStatement("$N.clearGraphCache()", attackStep.getName());
      }
    }
    parentBuilder.addMethod(builder.build());
  }

  private void createAssetAnnotations(TypeSpec.Builder parentBuilder, Asset asset) {
    ClassName displayClass = ClassName.get("com.foreseeti.corelib.FAnnotations", "DisplayClass");
    AnnotationSpec.Builder builder = AnnotationSpec.builder(displayClass);
    builder.addMember("category", "$S", asset.getCategory().getName());
    parentBuilder.addAnnotation(builder.build());

    ClassName typeName = ClassName.get("com.foreseeti.corelib.FAnnotations", "TypeName");
    builder = AnnotationSpec.builder(typeName);
    builder.addMember("name", "$S", asset.getName());
    parentBuilder.addAnnotation(builder.build());
  }

  ////////////////////
  // FIELDS

  private void createFields(TypeSpec.Builder parentBuilder, Asset asset) {
    if (!asset.hasSuperAsset()) {
      // if we extend something we will have these lists from our parent
      ClassName set = ClassName.get("java.util", "Set");
      ClassName attackStep = ClassName.get("com.foreseeti.simulator", "AttackStep");
      TypeName type = ParameterizedTypeName.get(set, attackStep);
      parentBuilder.addField(type, "attackSteps", Modifier.PROTECTED);
      ClassName defense = ClassName.get("com.foreseeti.simulator", "Defense");
      type = ParameterizedTypeName.get(set, defense);
      parentBuilder.addField(type, "defenses", Modifier.PROTECTED);
    }

    int index = 1;
    for (Field field : asset.getFields().values()) {
      createField(parentBuilder, field, index++);
    }

    for (AttackStep attackStep : asset.getAttackSteps().values()) {
      if (!attackStep.hasParent()) {
        createAttackStepField(parentBuilder, asset, attackStep, index++);
      }
    }
  }

  private void createField(TypeSpec.Builder parentBuilder, Field field, int index) {
    ClassName setType = ClassName.get("java.util", "Set");
    ClassName targetType = ClassName.get(this.pkg, field.getTarget().getAsset().getName());

    TypeName type = null;
    if (field.getMax() > 1) {
      type = ParameterizedTypeName.get(setType, targetType);
    } else {
      type = targetType;
    }

    FieldSpec.Builder builder = FieldSpec.builder(type, field.getName());

    // @annotation
    createFieldAnnotation(builder, field.getName(), index);

    // modifiers
    builder.addModifiers(Modifier.PUBLIC);

    if (field.getMax() > 1) {
      // only initialize if we are setType, otherwise null is fine
      builder.initializer("new $T<>()", HashSet.class);
    }

    parentBuilder.addField(builder.build());
  }

  private void createFieldAnnotation(FieldSpec.Builder parentBuilder, String name, int index) {
    ClassName association = ClassName.get("com.foreseeti.corelib.FAnnotations", "Association");
    AnnotationSpec.Builder builder = AnnotationSpec.builder(association);
    builder.addMember("index", "$L", index);
    builder.addMember("name", "$S", name);
    parentBuilder.addAnnotation(builder.build());
  }

  private void createAttackStepField(
      TypeSpec.Builder parentBuilder, Asset asset, AttackStep attackStep, int index) {
    ClassName type = ClassName.get(this.pkg, asset.getName(), ucFirst(attackStep.getName()));
    FieldSpec.Builder builder = FieldSpec.builder(type, attackStep.getName());

    // @annotations
    createFieldAnnotation(builder, attackStep.getName(), index);
    if (!attackStep.hasInheritedTag("hidden")) {
      ClassName display = ClassName.get("com.foreseeti.corelib.FAnnotations", "Display");
      builder.addAnnotation(display);
    }

    // modifiers
    builder.addModifiers(Modifier.PUBLIC);

    parentBuilder.addField(builder.build());
  }

  ////////////////////
  // CONSTRUCTORS

  private void createEmptyConstructor(TypeSpec.Builder parentBuilder) {
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    ClassName defaultValue = ClassName.get("com.foreseeti.corelib", "DefaultValue");
    constructor.addStatement("this($T.False)", defaultValue);
    parentBuilder.addMethod(constructor.build());
  }

  private void createDefaultValueConstructor(TypeSpec.Builder parentBuilder) {
    ClassName defaultValue = ClassName.get("com.foreseeti.corelib", "DefaultValue");
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    constructor.addParameter(defaultValue, "value");
    constructor.addStatement("initAttackSteps(value)");
    constructor.addStatement("initLists()");
    parentBuilder.addMethod(constructor.build());
  }

  private void createCopyConstructor(TypeSpec.Builder parentBuilder, Asset asset) {
    ClassName self = ClassName.get(this.pkg, asset.getName());
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    constructor.addParameter(self, "other");
    constructor.addStatement("super(other)");
    for (AttackStep attackStep : asset.getAttackSteps().values()) {
      ClassName type = ClassName.get(this.pkg, asset.getName(), ucFirst(attackStep.getName()));
      if (attackStep.hasParent()) {
        AttackStep parent =
            attackStep.getAsset().getSuperAsset().getAttackStep(attackStep.getName());
        Asset parentAsset = parent.getAsset();
        ClassName superType = ClassName.get(pkg, parentAsset.getName(), ucFirst(parent.getName()));

        constructor.addStatement(
            "$L = new $T(($T) other.$L)",
            attackStep.getName(),
            type,
            superType,
            attackStep.getName());
      } else {
        constructor.addStatement(
            "$L = new $T(other.$L)", attackStep.getName(), type, attackStep.getName());
      }
    }
    constructor.addStatement("initLists()");
    parentBuilder.addMethod(constructor.build());
  }

  ////////////////////
  // GETTERS

  private void createGetAttackSteps(TypeSpec.Builder parentBuilder) {
    ClassName set = ClassName.get("java.util", "Set");
    ClassName attackStep = ClassName.get("com.foreseeti.simulator", "AttackStep");
    TypeName type = ParameterizedTypeName.get(set, attackStep);
    MethodSpec.Builder builder = MethodSpec.methodBuilder("getAttackSteps");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    builder.returns(type);
    builder.addStatement("return this.attackSteps");
    parentBuilder.addMethod(builder.build());
  }

  private void createGetDefenses(TypeSpec.Builder parentBuilder) {
    ClassName set = ClassName.get("java.util", "Set");
    ClassName defense = ClassName.get("com.foreseeti.simulator", "Defense");
    TypeName type = ParameterizedTypeName.get(set, defense);
    MethodSpec.Builder builder = MethodSpec.methodBuilder("getDefenses");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    builder.returns(type);
    builder.addStatement("return this.defenses");
    parentBuilder.addMethod(builder.build());
  }

  private void createGetDescription(TypeSpec.Builder parentBuilder, Asset asset) {
    String description = asset.getMeta().get("user");
    if (description != null) {
      parentBuilder.addMethod(
          MethodSpec.methodBuilder("getDescription")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(String.class)
              .addStatement("return $S", description)
              .build());
    }
  }

  private File getAssetIcon(Asset asset, String type) {
    var name = String.format("%s.%s", asset.getName(), type);
    for (var file : this.icons.listFiles()) {
      if (file.getName().equals(name)) {
        return file;
      }
    }
    if (asset.hasSuperAsset()) {
      return getAssetIcon(asset.getSuperAsset(), type);
    } else {
      return null;
    }
  }

  private void createGetIcon(TypeSpec.Builder parentBuilder, Asset asset) throws IOException {
    var icon = getAssetIcon(asset, "svg");
    boolean isSvg = true;
    if (icon == null) {
      icon = getAssetIcon(asset, "png");
      isSvg = false;
      if (icon == null) {
        LOGGER.warning(String.format("No icon found for asset '%s'", asset.getName()));
        return;
      }
    }
    byte[] pngBytes = null;
    byte[] svgBytes = null;
    if (!isSvg) {
      pngBytes = Files.readAllBytes(icon.toPath());
    } else {
      SVGIcon svg = new SVGIcon();
      svg.setPreferredSize(new Dimension(48, 48));
      svg.setAutosize(SVGIcon.AUTOSIZE_BESTFIT);
      svg.setInterpolation(SVGIcon.INTERP_BICUBIC);
      svg.setAntiAlias(true);
      svg.setSvgURI(icon.toURI());

      BufferedImage img = (BufferedImage) svg.getImage();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ImageIO.write(img, "png", out);
      pngBytes = out.toByteArray();

      svgBytes = Files.readAllBytes(icon.toPath());
      MethodSpec.Builder builder = MethodSpec.methodBuilder("getIconSVG");
      builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
      builder.returns(String.class);
      builder.addStatement(
          "return $S",
          String.format(
              "data:image/svg+xml;base64,%s", new String(Base64.getEncoder().encode(svgBytes))));
      parentBuilder.addMethod(builder.build());
    }
    MethodSpec.Builder builder = MethodSpec.methodBuilder("getIconPNG");
    builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    builder.returns(String.class);
    builder.addStatement(
        "return $S",
        String.format(
            "data:image/png;base64,%s", new String(Base64.getEncoder().encode(pngBytes))));
    parentBuilder.addMethod(builder.build());
    // deprecated
    builder = MethodSpec.methodBuilder("getIcon");
    builder.addAnnotation(Deprecated.class);
    builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    builder.returns(String.class);
    builder.addStatement("return getIconPNG()");
    parentBuilder.addMethod(builder.build());
  }

  ////////////////////
  // OTHER

  private void createRegisterAssociations(TypeSpec.Builder parentBuilder, Asset asset) {
    ClassName associationManager = ClassName.get("com.foreseeti.corelib", "AssociationManager");
    ClassName autoLangLink = ClassName.get(this.pkg, "AutoLangLink");

    MethodSpec.Builder builder = MethodSpec.methodBuilder("registerAssociations");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    if (asset.hasSuperAsset()) {
      builder.addStatement("super.registerAssociations()");
    }
    for (Field field : asset.getFields().values()) {
      ClassName type = ClassName.get(this.pkg, field.getTarget().getAsset().getName());
      builder.addStatement(
          "$T.addSupportedAssociationMultiple(this.getClass(), $S, $T.class, $L, $L, $T.$L)",
          associationManager,
          field.getName(),
          type,
          field.getMin(),
          field.getMax(),
          autoLangLink,
          getLinkName(field));
    }
    parentBuilder.addMethod(builder.build());
  }

  private void createInitLists(TypeSpec.Builder parentBuilder, Asset asset) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("initLists");
    builder.addModifiers(Modifier.PROTECTED);
    List<String> attackSteps = new ArrayList<>();
    List<String> defenses = new ArrayList<>();
    for (AttackStep attackStep : asset.getAttackSteps().values()) {
      if (attackStep.isDefense() || attackStep.isConditionalDefense()) {
        defenses.add(attackStep.getName());
        attackSteps.add(String.format("%s.disable", attackStep.getName()));
      } else {
        attackSteps.add(attackStep.getName());
      }
    }
    if (asset.hasSuperAsset()) {
      builder.addStatement("super.initLists()");
    }

    ClassName set = ClassName.get("java.util", "Set");
    ClassName hashSet = ClassName.get("java.util", "HashSet");

    ClassName attackStep = ClassName.get("com.foreseeti.simulator", "AttackStep");
    builder.addStatement("$T<$T> attackSteps = new $T<>()", set, attackStep, hashSet);
    if (asset.hasSuperAsset()) {
      builder.addStatement("attackSteps.addAll(this.attackSteps)");
    }
    for (String name : attackSteps) {
      builder.addStatement("attackSteps.add($L)", name);
    }
    builder.addStatement("this.attackSteps = $T.copyOf(attackSteps)", set);

    ClassName defense = ClassName.get("com.foreseeti.simulator", "Defense");
    builder.addStatement("$T<$T> defenses = new $T<>()", set, defense, hashSet);
    if (asset.hasSuperAsset()) {
      builder.addStatement("defenses.addAll(this.defenses)");
    }
    for (String name : defenses) {
      builder.addStatement("defenses.add($L)", name);
    }
    builder.addStatement("this.defenses = $T.copyOf(defenses)", set);

    builder.addStatement("fillElementMap()");
    parentBuilder.addMethod(builder.build());
  }

  private void createInitAttackSteps(TypeSpec.Builder parentBuilder, Asset asset) {
    ClassName defaultValue = ClassName.get("com.foreseeti.corelib", "DefaultValue");
    MethodSpec.Builder builder = MethodSpec.methodBuilder("initAttackSteps");
    builder.addModifiers(Modifier.PROTECTED);
    builder.addParameter(defaultValue, "value");
    if (asset.hasSuperAsset()) {
      builder.addStatement("super.initAttackSteps(value)");
    }
    for (AttackStep attackStep : asset.getAttackSteps().values()) {
      ClassName type = ClassName.get(this.pkg, asset.getName(), ucFirst(attackStep.getName()));
      if (attackStep.isDefense()) {
        builder.addStatement("this.$L = new $T(value.get())", attackStep.getName(), type);
      } else if (attackStep.isConditionalDefense()) {
        builder.addStatement("this.$L = new $T(false)", attackStep.getName(), type);
      } else {
        builder.addStatement("this.$L = new $T()", attackStep.getName(), type);
      }
    }
    parentBuilder.addMethod(builder.build());
  }

  private void createLocalStep(TypeSpec.Builder parentBuilder, Asset asset, String name) {
    ClassName local = ClassName.get(this.pkg, asset.getName(), String.format("Local%s", name));
    ClassName step = ClassName.get("com.foreseeti.simulator", name);

    ClassName fclass = ClassName.get("com.foreseeti.corelib", "FClass");
    TypeSpec.Builder builder = TypeSpec.classBuilder(local);
    builder.addModifiers(Modifier.PUBLIC);
    builder.superclass(step);

    // empty constructor
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    builder.addMethod(constructor.build());

    // other constructor
    constructor = MethodSpec.constructorBuilder();
    constructor.addParameter(local, "other");
    constructor.addStatement("super(other)");
    builder.addMethod(constructor.build());
    if (name.equals("Defense")) {
      constructor = MethodSpec.constructorBuilder();
      constructor.addParameter(boolean.class, "other");
      constructor.addStatement("super(other)");
      builder.addMethod(constructor.build());
    }

    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getContainerFClass");
    methodBuilder.addAnnotation(Override.class);
    methodBuilder.addModifiers(Modifier.PUBLIC);
    methodBuilder.returns(fclass);
    methodBuilder.addStatement("return $L.this", asset.getName());
    builder.addMethod(methodBuilder.build());

    parentBuilder.addType(builder.build());
  }

  private void createGetTTCColoringElements(TypeSpec.Builder parentBuilder, Asset asset) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("getTTCColoringElements");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    ClassName modelElement = ClassName.get("com.foreseeti.corelib", "ModelElement");
    ClassName set = ClassName.get(Set.class);
    TypeName type = ParameterizedTypeName.get(set, modelElement);
    builder.returns(type);
    builder.addStatement("$T elements = new $T<>()", type, HashSet.class);
    for (AttackStep attackStep : asset.getAttackSteps().values()) {
      if (attackStep.hasCIA()) {
        LOGGER.debug(
            String.format("'%s$%s' set to affect color", asset.getName(), attackStep.getName()));
        builder.addStatement("elements.add($L)", attackStep.getName());
      }
    }
    builder.addStatement("return elements");
    parentBuilder.addMethod(builder.build());
  }

  ////////////////////
  // HELPERS

  private String getLinkName(Field field) {
    Link link = field.getLink();
    return String.format("%s_%s", link.getLeftField().getName(), link.getRightField().getName());
  }
}

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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.mal_lang.compiler.lib.JavaGenerator;
import org.mal_lang.compiler.lib.Lang.Asset;
import org.mal_lang.compiler.lib.Lang.AttackStep;
import org.mal_lang.compiler.lib.Lang.AttackStepType;
import org.mal_lang.compiler.lib.Lang.StepExpr;
import org.mal_lang.compiler.lib.Lang.TTCFunc;
import org.mal_lang.compiler.lib.MalLogger;

public class DefenseGenerator extends JavaGenerator {
  private final ExpressionGenerator exprGen;

  protected DefenseGenerator(MalLogger LOGGER, String pkg) {
    super(LOGGER, pkg);
    this.exprGen = new ExpressionGenerator(LOGGER, pkg);
  }

  private static String getDescription(AttackStep attackStep) {
    String userInfo = attackStep.getMeta().get("user");
    if (userInfo != null) {
      return userInfo;
    }
    if (!attackStep.hasParent()) {
      return null;
    }
    return getDescription(
        attackStep.getAsset().getSuperAsset().getAttackStep(attackStep.getName()));
  }

  protected void generate(TypeSpec.Builder parentBuilder, Asset asset, AttackStep attackStep) {
    ClassName type = ClassName.get(this.pkg, asset.getName(), ucFirst(attackStep.getName()));
    TypeSpec.Builder builder = TypeSpec.classBuilder(type);
    ClassName typeName = ClassName.get("com.foreseeti.corelib.FAnnotations", "TypeName");
    ClassName typeDescription =
        ClassName.get("com.foreseeti.corelib.FAnnotations", "TypeDescription");

    AnnotationSpec.Builder asBuilder = AnnotationSpec.builder(typeName);
    asBuilder.addMember("name", "$S", ucFirst(attackStep.getName()));
    builder.addAnnotation(asBuilder.build());

    var description = getDescription(attackStep);
    asBuilder = AnnotationSpec.builder(typeDescription);
    asBuilder.addMember(
        "text", "$S", description == null ? ucFirst(attackStep.getName()) : description);
    builder.addAnnotation(asBuilder.build());
    Generator.createMetaInfoAnnotations(builder, Generator.getMetaInfoMap(attackStep));

    builder.addModifiers(Modifier.PUBLIC);
    if (attackStep.hasParent()) {
      AttackStep parent = attackStep.getAsset().getSuperAsset().getAttackStep(attackStep.getName());
      Asset parentAsset = parent.getAsset();
      type = ClassName.get(pkg, parentAsset.getName(), ucFirst(parent.getName()));
      builder.superclass(type);
    } else {
      builder.superclass(ClassName.get(this.pkg, asset.getName(), "LocalDefense"));
    }

    // default constructor
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    constructor.addParameter(boolean.class, "enabled");
    constructor.addStatement("super(enabled)");
    ClassName disable =
        ClassName.get(this.pkg, asset.getName(), ucFirst(attackStep.getName()), "Disable");
    constructor.addStatement("this.disable = new $T()", disable);
    if (attackStep.hasTTC()) {
      ClassName fmath = ClassName.get("com.foreseeti.corelib.math", "FMath");
      // Analyzer guarantees this is a ttcfunc
      TTCFunc func = (TTCFunc) attackStep.getTTC();
      constructor.addStatement(
          "setEvidenceDistribution($T.getBernoulliDist($L))", fmath, func.dist.getMean());
    }
    builder.addMethod(constructor.build());

    // copy constructor
    constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    constructor.addParameter(type, "other");
    constructor.addStatement("super(other)");
    constructor.addStatement("this.disable = new $T()", disable);
    if (attackStep.hasTTC()) {
      ClassName fmath = ClassName.get("com.foreseeti.corelib.math", "FMath");
      // Analyzer guarantees this is a ttcfunc
      TTCFunc func = (TTCFunc) attackStep.getTTC();
      constructor.addStatement(
          "setEvidenceDistribution($T.getBernoulliDist($L))", fmath, func.dist.getMean());
    }
    builder.addMethod(constructor.build());

    if (attackStep.isConditionalDefense()) {
      createIsEnabled(builder, attackStep);
    }

    createDisable(builder, asset, attackStep);

    parentBuilder.addType(builder.build());
  }

  private void createIsEnabled(TypeSpec.Builder parentBuilder, AttackStep attackStep) {
    // Overriding the isEnabled method, defense will be enabled if all requirements exist
    MethodSpec.Builder method = MethodSpec.methodBuilder("isEnabled");
    method.addAnnotation(Override.class);
    method.addModifiers(Modifier.PUBLIC);
    method.returns(boolean.class);
    ClassName concreteSample = ClassName.get("com.foreseeti.simulator", "ConcreteSample");
    method.addParameter(concreteSample, "sample");
    if (attackStep.getType() == AttackStepType.EXIST) {
      for (StepExpr expr : attackStep.getRequires()) {
        AutoFlow af = new AutoFlow();
        AutoFlow end = exprGen.generateExpr(af, expr, attackStep.getAsset());
        end.addStatement("return false");
        af.build(method);
      }
      method.addStatement("return true");
    } else {
      method.addStatement("int count = $L", attackStep.getRequires().size());
      for (StepExpr expr : attackStep.getRequires()) {
        AutoFlow af = new AutoFlow();
        AutoFlow end = exprGen.generateExpr(af, expr, attackStep.getAsset());
        end.addStatement("count--");
        if (end.isLoop()) {
          end.addStatement("break");
        }
        af.build(method);
      }
      method.addStatement("return count == 0");
    }

    parentBuilder.addMethod(method.build());
  }

  private void createDisable(TypeSpec.Builder parentBuilder, Asset asset, AttackStep attackStep) {
    ClassName disable =
        ClassName.get(this.pkg, asset.getName(), ucFirst(attackStep.getName()), "Disable");
    TypeSpec.Builder builder = TypeSpec.classBuilder(disable);
    builder.addModifiers(Modifier.PUBLIC);
    if (attackStep.hasParent()) {
      AttackStep parent = attackStep.getAsset().getSuperAsset().getAttackStep(attackStep.getName());
      Asset parentAsset = parent.getAsset();
      ClassName type =
          ClassName.get(pkg, parentAsset.getName(), ucFirst(parent.getName()), "Disable");
      builder.superclass(type);
    } else {
      ClassName localAttackStepMin = ClassName.get(this.pkg, asset.getName(), "LocalAttackStepMin");
      builder.superclass(localAttackStepMin);
    }

    // getInfluencingDefense
    ClassName defense = ClassName.get("com.foreseeti.simulator", "Defense");
    ClassName parent = ClassName.get(this.pkg, asset.getName(), ucFirst(attackStep.getName()));
    MethodSpec.Builder method = MethodSpec.methodBuilder("getInfluencingDefense");
    method.addAnnotation(Override.class);
    method.addModifiers(Modifier.PUBLIC);
    method.returns(defense);
    method.addStatement("return $T.this", parent);
    builder.addMethod(method.build());

    AttackStepGenerator.createSteps(builder, exprGen, attackStep);

    parentBuilder.addType(builder.build());
  }
}

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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.mal_lang.compiler.lib.JavaGenerator;
import org.mal_lang.compiler.lib.Lang.Asset;
import org.mal_lang.compiler.lib.Lang.AttackStep;
import org.mal_lang.compiler.lib.Lang.StepAttackStep;
import org.mal_lang.compiler.lib.Lang.StepBinOp;
import org.mal_lang.compiler.lib.Lang.StepCall;
import org.mal_lang.compiler.lib.Lang.StepCollect;
import org.mal_lang.compiler.lib.Lang.StepDifference;
import org.mal_lang.compiler.lib.Lang.StepExpr;
import org.mal_lang.compiler.lib.Lang.StepField;
import org.mal_lang.compiler.lib.Lang.StepIntersection;
import org.mal_lang.compiler.lib.Lang.StepTransitive;
import org.mal_lang.compiler.lib.Lang.StepUnion;
import org.mal_lang.compiler.lib.MalLogger;

public class ExpressionGenerator extends JavaGenerator {
  private final String pkg;

  protected ExpressionGenerator(MalLogger LOGGER, String pkg) {
    super(LOGGER);
    this.pkg = pkg;
  }

  protected void createGetAttackStepChildren(
      TypeSpec.Builder parentBuilder, AttackStep attackStep, String cacheName) {
    Name.reset();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("getAttackStepChildren");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    ClassName set = ClassName.get(Set.class);
    ClassName as = ClassName.get("com.foreseeti.simulator", "AttackStep");
    ClassName hashSet = ClassName.get(HashSet.class);
    TypeName asSet = ParameterizedTypeName.get(set, as);
    builder.returns(asSet);

    builder.beginControlFlow("if ($N == null)", cacheName);
    if (attackStep.inheritsReaches()) {
      builder.addStatement("$T tmpCache = new $T<>(super.getAttackStepChildren())", asSet, hashSet);
    } else {
      builder.addStatement("$T tmpCache = new $T<>()", asSet, hashSet);
    }
    for (StepExpr expr : attackStep.getReaches()) {
      AutoFlow af = new AutoFlow();
      AutoFlow end = generate(af, expr, attackStep.getAsset(), "(null)");
      end.addStatement("tmpCache.add($L)", end.prefix);
      af.build(builder);
    }
    // copyOf returns an immutable set
    builder.addStatement("$N = $T.copyOf(tmpCache)", cacheName, set);
    builder.endControlFlow();

    builder.addStatement("return $N", cacheName);
    parentBuilder.addMethod(builder.build());
  }

  protected void createSetExpectedParents(
      TypeSpec.Builder parentBuilder, AttackStep attackStep, String cacheName) {
    Name.reset();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("setExpectedParents");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    ClassName concreteSample = ClassName.get("com.foreseeti.simulator", "ConcreteSample");
    builder.addParameter(concreteSample, "sample");

    builder.addStatement("super.setExpectedParents(sample)");
    builder.beginControlFlow("if ($N == null)", cacheName);
    builder.addStatement("$N = new $T<>()", cacheName, HashSet.class);
    for (StepExpr expr : attackStep.getParentSteps()) {
      AutoFlow af = new AutoFlow();
      AutoFlow end = generate(af, expr, attackStep.getAsset(), "(sample)");
      end.addStatement("$N.add($N)", cacheName, end.prefix);
      af.build(builder);
    }
    builder.endControlFlow();

    ClassName as = ClassName.get("com.foreseeti.simulator", "AttackStep");
    builder.beginControlFlow("for($T attackStep : $N)", as, cacheName);
    builder.addStatement("sample.addExpectedParent(this, attackStep)");
    builder.endControlFlow();

    parentBuilder.addMethod(builder.build());
  }

  ////////////////////
  // GENERATE

  protected AutoFlow generate(AutoFlow af, StepExpr expr, Asset asset, String nameSuffix) {
    if (!af.hasPrefix()) {
      af = subType(af, expr.src, expr.subSrc, asset);
    }
    if (expr instanceof StepCollect) {
      af = generate(af, ((StepCollect) expr).lhs, asset, nameSuffix);
      af = generate(af, ((StepCollect) expr).rhs, asset, nameSuffix);
    } else if (expr instanceof StepField) {
      af = createStepField(af, (StepField) expr, nameSuffix);
    } else if (expr instanceof StepTransitive) {
      af = createStepTransitive(af, (StepTransitive) expr, asset, nameSuffix);
    } else if (expr instanceof StepUnion
        || expr instanceof StepIntersection
        || expr instanceof StepDifference) {
      af = createStepSet(af, expr, asset, nameSuffix);
    } else if (expr instanceof StepAttackStep) {
      af = createStepAttackStep(af, (StepAttackStep) expr);
    } else if (expr instanceof StepCall) {
      af = createStepCall(af, (StepCall) expr);
    } else {
      throw new RuntimeException(String.format("unknown expression '%s'", expr));
    }
    af = subType(af, expr.target, expr.subTarget, asset);
    return af;
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

  private AutoFlow createStepField(AutoFlow af, StepField expr, String nameSuffix) {
    String name = expr.field.getName();
    if (af.hasPrefix()) {
      name = String.format("%s.%s", af.prefix, name);
    }
    if (expr.field.getMax() > 1) {
      // field is set
      ClassName targetType = ClassName.get(pkg, expr.field.getTarget().getAsset().getName());
      String prefix = Name.get();
      return af.addStatement(
          new AutoFlow(prefix, true, "for ($T $N : $N$L)", targetType, prefix, name, nameSuffix));
    } else {
      return af.addStatement(
          new AutoFlow(String.format("%s%s", name, nameSuffix), "if ($L != null)", name));
    }
  }

  private AutoFlow createStepTransitive(
      AutoFlow af, StepTransitive expr, Asset asset, String nameSuffix) {
    ClassName targetType = ClassName.get(pkg, expr.target.getName());
    ClassName list = ClassName.get(List.class);
    ClassName arrayList = ClassName.get(ArrayList.class);
    ClassName set = ClassName.get(Set.class);
    ClassName hashSet = ClassName.get(HashSet.class);
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
    AutoFlow deep = generate(naf, expr.e, asset, nameSuffix);
    deep.addStatement("$N.add($N)", name1, deep.prefix);
    deep.addStatement("$N.add($N)", name2, deep.prefix);
    String name4 = Name.get();
    return af.addStatement(new AutoFlow(name4, true, "for ($T $N : $N)", targetType, name4, name1));
  }

  private AutoFlow createStepSet(AutoFlow af, StepExpr expr, Asset asset, String nameSuffix) {
    StepBinOp binop = (StepBinOp) expr;
    String targetName = binop.target.getName();
    ClassName targetType = ClassName.get(pkg, targetName);
    ClassName set = ClassName.get(Set.class);
    ClassName hashSet = ClassName.get(HashSet.class);
    TypeName targetSet = ParameterizedTypeName.get(set, targetType);
    String name1 = Name.get();
    String name2 = Name.get();
    af.addStatement("$T $N = new $T<>()", targetSet, name1, hashSet);
    af.addStatement("$T $N = new $T<>()", targetSet, name2, hashSet);

    AutoFlow deep1 = generate(af, binop.lhs, asset, nameSuffix);
    deep1.addStatement("$N.add($N)", name1, deep1.prefix);
    AutoFlow deep2 = generate(af, binop.rhs, asset, nameSuffix);
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

  private AutoFlow createStepCall(AutoFlow af, StepCall expr) {
    String name = String.format("_%s", expr.name);
    if (af.hasPrefix()) {
      name = String.format("%s.%s", af.prefix, name);
    }

    String prefix = Name.get();
    return af.addStatement(new AutoFlow(prefix, true, "for (var $N : $N())", prefix, name));
  }
}

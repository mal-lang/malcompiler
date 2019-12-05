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
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.mal_lang.compiler.lib.JavaGenerator;
import org.mal_lang.compiler.lib.Lang.AttackStep;
import org.mal_lang.compiler.lib.Lang.StepExpr;
import org.mal_lang.compiler.lib.MalLogger;

public class ExpressionGenerator extends JavaGenerator {

  protected ExpressionGenerator(MalLogger LOGGER, String pkg) {
    super(LOGGER, pkg);
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
      AutoFlow end = generateExpr(af, expr, attackStep.getAsset());
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
      AutoFlow end = generateExpr(af, expr, attackStep.getAsset());
      end.addStatement("$N.add($N)", cacheName, end.prefix);
      af.build(builder);
    }
    builder.endControlFlow();

    builder.beginControlFlow("if (sample != null)");
    ClassName as = ClassName.get("com.foreseeti.simulator", "AttackStep");
    builder.beginControlFlow("for ($T attackStep : $N)", as, cacheName);
    builder.addStatement("sample.addExpectedParent(this, attackStep)");
    builder.endControlFlow(); // for
    builder.endControlFlow(); // if

    parentBuilder.addMethod(builder.build());
  }
}

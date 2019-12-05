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
import org.mal_lang.compiler.lib.Lang.Asset;
import org.mal_lang.compiler.lib.Lang.StepExpr;
import org.mal_lang.compiler.lib.MalLogger;

public class VariableGenerator extends JavaGenerator {

  private final String pkg;
  private final ExpressionGenerator exprGen;

  protected VariableGenerator(MalLogger LOGGER, String pkg) {
    super(LOGGER);
    this.pkg = pkg;
    this.exprGen = new ExpressionGenerator(LOGGER, pkg);
  }

  protected void generate(TypeSpec.Builder parentBuilder, String name, StepExpr expr, Asset asset) {
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
    builder.addStatement("$T tmpCache = new $T<>()", targetSet, hashSet);
    AutoFlow varFlow = new AutoFlow();
    AutoFlow end = this.exprGen.generateExpr(pkg, varFlow, expr, asset);
    end.addStatement("tmpCache.add($N)", end.prefix);
    varFlow.build(builder);
    // copyOf returns an immutable set
    builder.addStatement("$N = $T.copyOf(tmpCache)", setName, set);
    builder.endControlFlow();

    builder.addStatement("return $N", setName);

    parentBuilder.addMethod(builder.build());
  }
}

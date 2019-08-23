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
package org.mal_lang.mal.lib.securicad;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.mal_lang.mal.lib.Distributions;
import org.mal_lang.mal.lib.JavaGenerator;
import org.mal_lang.mal.lib.Lang.Asset;
import org.mal_lang.mal.lib.Lang.AttackStep;
import org.mal_lang.mal.lib.Lang.TTCAdd;
import org.mal_lang.mal.lib.Lang.TTCBinOp;
import org.mal_lang.mal.lib.Lang.TTCDiv;
import org.mal_lang.mal.lib.Lang.TTCExpr;
import org.mal_lang.mal.lib.Lang.TTCFunc;
import org.mal_lang.mal.lib.Lang.TTCMul;
import org.mal_lang.mal.lib.Lang.TTCNum;
import org.mal_lang.mal.lib.Lang.TTCPow;
import org.mal_lang.mal.lib.Lang.TTCSub;
import org.mal_lang.mal.lib.MalLogger;

public class AttackStepGenerator extends JavaGenerator {
  private final String pkg;
  private final ExpressionGenerator exprGen;

  protected AttackStepGenerator(MalLogger LOGGER, String pkg) {
    super(LOGGER);
    this.pkg = pkg;
    this.exprGen = new ExpressionGenerator(LOGGER, pkg);
  }

  protected void generate(TypeSpec.Builder parentBuilder, Asset asset, AttackStep attackStep) {
    ClassName type = ClassName.get(this.pkg, asset.getName(), ucFirst(attackStep.getName()));
    TypeSpec.Builder builder = TypeSpec.classBuilder(type);
    builder.addModifiers(Modifier.PUBLIC);
    if (attackStep.hasParent()) {
      AttackStep parent = attackStep.getAsset().getSuperAsset().getAttackStep(attackStep.getName());
      Asset parentAsset = parent.getAsset();
      type = ClassName.get(pkg, parentAsset.getName(), ucFirst(parent.getName()));
      builder.superclass(type);
    } else {
      builder.superclass(getExtend(asset, attackStep));
    }

    // empty constructor
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    builder.addMethod(constructor.build());

    // copy constructor
    constructor = MethodSpec.constructorBuilder();
    constructor.addModifiers(Modifier.PUBLIC);
    constructor.addParameter(type, "other");
    constructor.addStatement("super(other)");
    builder.addMethod(constructor.build());

    if (attackStep.hasTTC()) {
      createDefaultLocalTtc(builder, attackStep);
    }

    if (!attackStep.getReaches().isEmpty()) {
      exprGen.createGetAttackStepChildren(builder, attackStep);
    }

    if (!attackStep.getParentSteps().isEmpty()) {
      exprGen.createSetExpectedParents(builder, attackStep);
    }

    parentBuilder.addType(builder.build());
  }

  ////////////////////
  // TTC METHODS

  private void createDefaultLocalTtc(TypeSpec.Builder parentBuilder, AttackStep attackStep) {
    ClassName baseSample = ClassName.get("com.foreseeti.corelib", "BaseSample");
    ClassName as = ClassName.get("com.foreseeti.simulator", "AttackStep");
    MethodSpec.Builder builder = MethodSpec.methodBuilder("defaultLocalTtc");
    builder.addAnnotation(Override.class);
    builder.addModifiers(Modifier.PUBLIC);
    builder.returns(double.class);
    builder.addParameter(baseSample, "sample");
    builder.addParameter(as, "caller");

    builder.addCode("return ");
    builder.addStatement(createTTC(attackStep.getTTC()));

    parentBuilder.addMethod(builder.build());
  }

  private String getOperator(TTCBinOp expr) {
    if (expr instanceof TTCAdd) {
      return "+";
    } else if (expr instanceof TTCSub) {
      return "-";
    } else if (expr instanceof TTCDiv) {
      return "/";
    } else if (expr instanceof TTCMul) {
      return "*";
    } else {
      throw new RuntimeException(
          String.format(
              "TTC binary operation not reckognized '%s'", expr.getClass().getSimpleName()));
    }
  }

  private CodeBlock createTTCFunc(Distributions.Distribution dist) {
    CodeBlock.Builder builder = CodeBlock.builder();
    ClassName fmath = ClassName.get("com.foreseeti.corelib.math", "FMath");
    if (dist instanceof Distributions.Bernoulli) {
      ClassName dbl = ClassName.get("java.lang", "Double");
      builder.add(
          "($T.getBernoulliDist($L).sample() ? 0 : $T.MAX_VALUE)",
          fmath,
          ((Distributions.Bernoulli) dist).probability,
          dbl);
    } else if (dist instanceof Distributions.Binomial) {
      // trials, p
      builder.add(
          "$T.getBinomialDist($L, $L).sample()",
          fmath,
          ((Distributions.Binomial) dist).trials,
          ((Distributions.Binomial) dist).probability);
    } else if (dist instanceof Distributions.Exponential) {
      // intensity
      builder.add(
          "$T.getExponentialDist($L).sample()",
          fmath,
          1 / ((Distributions.Exponential) dist).lambda);
    } else if (dist instanceof Distributions.Gamma) {
      // shape, scale
      builder.add(
          "$T.getGammaDist($L, $L).sample()",
          fmath,
          ((Distributions.Gamma) dist).shape,
          ((Distributions.Gamma) dist).scale);
    } else if (dist instanceof Distributions.LogNormal) {
      // scale, shape
      builder.add(
          "$T.getLogNormalDist($L, $L).sample()",
          fmath,
          ((Distributions.LogNormal) dist).mean,
          ((Distributions.LogNormal) dist).standardDeviation);
    } else if (dist instanceof Distributions.Pareto) {
      // scale, shape
      builder.add(
          "$T.getParetoDist($L, $L).sample()",
          fmath,
          ((Distributions.Pareto) dist).min,
          ((Distributions.Pareto) dist).shape);
    } else if (dist instanceof Distributions.TruncatedNormal) {
      // same as lognormal
      builder.add(
          "$T.getTruncatedNormalDist($L, $L).sample()",
          fmath,
          ((Distributions.TruncatedNormal) dist).mean,
          ((Distributions.TruncatedNormal) dist).standardDeviation);
    } else if (dist instanceof Distributions.Uniform) {
      // min, max
      builder.add(
          "$T.getUniformDist($L, $L).sample()",
          fmath,
          ((Distributions.Uniform) dist).min,
          ((Distributions.Uniform) dist).max);
    } else if (dist instanceof Distributions.EasyAndCertain) {
      return createTTCFunc(Distributions.EasyAndCertain.exponential);
    } else if (dist instanceof Distributions.EasyAndUncertain) {
      return createTTCFunc(Distributions.EasyAndUncertain.bernoulli);
    } else if (dist instanceof Distributions.HardAndCertain) {
      return createTTCFunc(Distributions.HardAndCertain.exponential);
    } else if (dist instanceof Distributions.HardAndUncertain) {
      CodeBlock bernoulli = createTTCFunc(Distributions.HardAndUncertain.bernoulli);
      CodeBlock exponential = createTTCFunc(Distributions.HardAndUncertain.exponential);
      builder.add(bernoulli).add(" * ").add(exponential);
    } else if (dist instanceof Distributions.VeryHardAndCertain) {
      return createTTCFunc(Distributions.VeryHardAndCertain.exponential);
    } else if (dist instanceof Distributions.VeryHardAndUncertain) {
      CodeBlock bernoulli = createTTCFunc(Distributions.VeryHardAndUncertain.bernoulli);
      CodeBlock exponential = createTTCFunc(Distributions.VeryHardAndUncertain.exponential);
      builder.add(bernoulli).add(" * ").add(exponential);
    } else if (dist instanceof Distributions.Infinity) {
      ClassName dbl = ClassName.get("java.lang", "Double");
      builder.add("$T.MAX_VALUE", dbl);
    } else if (dist instanceof Distributions.Zero) {
      builder.add("0");
    } else {
      throw new RuntimeException("unknown distribution");
    }
    return builder.build();
  }

  private CodeBlock createTTC(TTCExpr expr) {
    if (expr instanceof TTCPow) {
      ClassName math = ClassName.get("java.lang", "Math");
      CodeBlock left = createTTC(((TTCPow) expr).lhs);
      CodeBlock right = createTTC(((TTCPow) expr).rhs);
      return CodeBlock.builder()
          .add("$T.pow(", math)
          .add(left)
          .add(", ")
          .add(right)
          .add(")")
          .build();
    } else if (expr instanceof TTCBinOp) {
      CodeBlock left = createTTC(((TTCBinOp) expr).lhs);
      CodeBlock right = createTTC(((TTCBinOp) expr).rhs);
      return CodeBlock.builder()
          .add(left)
          .add(String.format(" %s ", getOperator((TTCBinOp) expr)))
          .add(right)
          .build();
    } else if (expr instanceof TTCFunc) {
      return createTTCFunc(((TTCFunc) expr).dist);
    } else if (expr instanceof TTCNum) {
      return CodeBlock.builder().add("$L", ((TTCNum) expr).value).build();
    } else {
      throw new RuntimeException("err");
    }
  }

  ////////////////////
  // HELPERS

  private ClassName getExtend(Asset asset, AttackStep attackStep) {
    switch (attackStep.getType()) {
      case ALL:
        return ClassName.get(this.pkg, asset.getName(), "LocalAttackStepMax");
      case ANY:
        return ClassName.get(this.pkg, asset.getName(), "LocalAttackStepMin");
      default:
        throw new RuntimeException(
            String.format("unknown attack step type '%s'", attackStep.getType()));
    }
  }
}

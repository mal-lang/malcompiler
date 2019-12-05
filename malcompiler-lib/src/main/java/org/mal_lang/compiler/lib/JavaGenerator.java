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
package org.mal_lang.compiler.lib;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import org.mal_lang.compiler.lib.Lang.Asset;
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

public abstract class JavaGenerator extends Generator {

  protected String pkg;

  protected JavaGenerator(boolean verbose, boolean debug) {
    this(verbose, debug, "");
  }

  protected JavaGenerator(boolean verbose, boolean debug, String pkg) {
    super(verbose, debug);
    this.pkg = pkg;
  }

  protected JavaGenerator(MalLogger LOGGER, String pkg) {
    super(LOGGER);
    this.pkg = pkg;
  }

  protected static String ucFirst(String str) {
    if (str.isEmpty()) {
      return str;
    } else {
      return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
    }
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
  protected class AutoFlow extends Statement {
    public final String prefix;
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

  protected void validateNames(Lang lang) throws CompilerException {
    boolean err = false;
    if (!SourceVersion.isName(pkg)) {
      LOGGER.error(String.format("Package '%s' is not a valid package name", pkg));
      err = true;
    }
    for (var asset : lang.getAssets().values()) {
      if (SourceVersion.isKeyword(asset.getName())) {
        LOGGER.error(String.format("Asset '%s' is a java keyword", asset.getName()));
        err = true;
      }
      for (var attackStep : asset.getAttackSteps().values()) {
        if (SourceVersion.isKeyword(attackStep.getName())) {
          LOGGER.error(
              String.format(
                  "Attack step '%s' in asset '%s' is a java keyword",
                  attackStep.getName(), asset.getName()));
          err = true;
        }
      }
      for (var field : asset.getFields().values()) {
        if (SourceVersion.isKeyword(field.getName())) {
          LOGGER.error(
              String.format(
                  "Field '%s' in asset '%s' is a java keyword", field.getName(), asset.getName()));
          err = true;
        }
      }
    }
    if (err) {
      throw error();
    }
  }

  protected void checkSteps(Lang lang) throws CompilerException {
    boolean err = false;
    for (var asset : lang.getAssets().values()) {
      for (var attackStep : asset.getAttackSteps().values()) {
        if (attackStep.getName().equalsIgnoreCase(asset.getName())) {
          LOGGER.error(
              String.format(
                  "Attack step '%s' shares name with its asset '%s'",
                  attackStep.getName(), asset.getName()));
          err = true;
        }
      }
    }
    if (err) {
      throw error();
    }
  }

  public AutoFlow generateExpr(AutoFlow af, StepExpr expr, Asset asset) {
    if (!af.hasPrefix()) {
      af = subType(af, expr.src, expr.subSrc, asset);
    }
    if (expr instanceof StepCollect) {
      af = generateExpr(af, ((StepCollect) expr).lhs, asset);
      af = generateExpr(af, ((StepCollect) expr).rhs, asset);
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
    } else if (expr instanceof StepCall) {
      af = createStepCall(af, (StepCall) expr);
    } else {
      throw new RuntimeException(String.format("unknown expression '%s'", expr));
    }
    af = subType(af, expr.target, expr.subTarget, asset);
    return af;
  }

  private AutoFlow createStepTransitive(AutoFlow af, StepTransitive expr, Asset asset) {
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
    AutoFlow deep = generateExpr(naf, expr.e, asset);
    deep.addStatement("$N.add($N)", name1, deep.prefix);
    deep.addStatement("$N.add($N)", name2, deep.prefix);
    String name4 = Name.get();
    return af.addStatement(new AutoFlow(name4, true, "for ($T $N : $N)", targetType, name4, name1));
  }

  private AutoFlow createStepSet(AutoFlow af, StepExpr expr, Asset asset) {
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

    AutoFlow deep1 = generateExpr(af, binop.lhs, asset);
    deep1.addStatement("$N.add($N)", name1, deep1.prefix);
    AutoFlow deep2 = generateExpr(af, binop.rhs, asset);
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

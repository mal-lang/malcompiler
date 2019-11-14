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
    String setName = String.format("_cache%sAsset", name);
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
    AutoFlow end = this.exprGen.generate(varFlow, expr, asset, "(null)");
    end.addStatement("tmpCache.add($N)", end.prefix);
    varFlow.build(builder);
    // copyOf returns an immutable set
    builder.addStatement("$N = $T.copyOf(tmpCache)", setName, set);
    builder.endControlFlow();

    builder.addStatement("return $N", setName);

    parentBuilder.addMethod(builder.build());
  }
}

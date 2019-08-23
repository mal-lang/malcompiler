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
package org.mal_lang.mal.lib;

import com.squareup.javapoet.MethodSpec;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.SourceVersion;

public abstract class JavaGenerator extends Generator {
  protected JavaGenerator(boolean verbose, boolean debug) {
    super(verbose, debug);
  }

  protected JavaGenerator(MalLogger LOGGER) {
    super(LOGGER);
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

  protected void validateNames(Lang lang, String pkg) throws CompilerException {
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
}

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
package com.foreseeti.mal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AST {
  private List<Category> categories = new ArrayList<>();
  private List<Association> associations = new ArrayList<>();
  private List<Define> defines = new ArrayList<>();

  @Override
  public String toString() {
    var sb = new StringBuilder();
    for (var define : defines) {
      sb.append(define.toString());
      sb.append('\n');
    }
    for (var category : categories) {
      sb.append(category.toString());
      sb.append('\n');
    }
    sb.append("associations {\n");
    for (var association : associations) {
      sb.append(association.toString());
    }
    sb.append("}\n");
    return sb.toString();
  }

  public void include(AST other) {
    this.categories.addAll(other.categories);
    this.associations.addAll(other.associations);
    this.defines.addAll(other.defines);
  }

  public List<Category> getCategories() {
    var categories = new ArrayList<Category>();
    categories.addAll(this.categories);
    return categories;
  }

  public void addCategory(Category category) {
    this.categories.add(category);
  }

  public List<Association> getAssociations() {
    var associations = new ArrayList<Association>();
    associations.addAll(this.associations);
    return associations;
  }

  public void addAssociations(List<Association> associations) {
    this.associations.addAll(associations);
  }

  public List<Define> getDefines() {
    var defines = new ArrayList<Define>();
    defines.addAll(this.defines);
    return defines;
  }

  public void addDefine(Define define) {
    this.defines.add(define);
  }

  public static class ID extends Position {
    public final String id;

    public ID(Position pos, String id) {
      super(pos);
      this.id = id;
    }

    @Override
    public String toString() {
      return String.format("<%s:%d:%d>ID(%s)", filename, line, col, id);
    }
  }

  public static class Define extends Position {
    public final ID key;
    public final String value;

    public Define(Position pos, ID key, String value) {
      super(pos);
      this.key = key;
      this.value = value;
    }

    @Override
    public String toString() {
      return String.format("<%s:%d:%d>Define(%s, \"%s\")", filename, line, col, key.toString(), value);
    }
  }

  public enum MetaType {
    INFO("'info'"),
    ASSUMPTIONS("'assumptions'"),
    RATIONALE("'rationale'");

    String string;

    private MetaType(String string) {
      this.string = string;
    }

    @Override
    public String toString() {
      return string;
    }
  }

  public static class Meta extends Position {
    public final MetaType type;
    public final String string;

    public Meta(Position pos, MetaType type, String string) {
      super(pos);
      this.type = type;
      this.string = string;
    }

    @Override
    public String toString() {
      return String.format("<%s:%d:%d>Meta(%s, \"%s\")", filename, line, col, type.toString(), string);
    }
  }

  public static class Category extends Position {
    public final ID name;
    public final List<Meta> meta;
    public final List<Asset> assets;

    public Category(Position pos, ID name, List<Meta> meta, List<Asset> assets) {
      super(pos);
      this.name = name;
      this.meta = meta;
      this.assets = assets;
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      sb.append(String.format("<%s:%d:%d>Category(%s)\n", filename, line, col, name.toString()));
      for (var m : meta) {
        sb.append(String.format("  %s\n", m.toString()));
      }
      for (var a : assets) {
        sb.append(a.toString());
      }
      return sb.toString();
    }
  }

  public static class Asset extends Position {
    public final boolean isAbstract;
    public final ID name;
    public final Optional<ID> parent;
    public final List<Meta> meta;
    public final List<AttackStep> attackSteps;
    public final List<Variable> variables;

    public Asset(Position pos, boolean isAbstract, ID name, Optional<ID> parent, List<Meta> meta, List<AttackStep> attackSteps, List<Variable> variables) {
      super(pos);
      this.isAbstract = isAbstract;
      this.name = name;
      this.parent = parent;
      this.meta = meta;
      this.attackSteps = attackSteps;
      this.variables = variables;
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      sb.append(String.format("  <%s:%d:%d>Asset(%s, %s, %s)\n", filename, line, col, isAbstract ? "abstract" : "not abstract", name.toString(), parent.isEmpty() ? "no parent" : parent.get().toString()));
      for (var m : meta) {
        sb.append(String.format("    %s\n", m.toString()));
      }
      for (var a : attackSteps) {
        sb.append(a.toString());
      }
      for (var v : variables) {
        sb.append(String.format("    %s\n", v.toString()));
      }
      return sb.toString();
    }
  }

  public enum AttackStepType {
    ALL,
    ANY,
    DEFENSE,
    EXIST,
    NOTEXIST
  }

  public static class AttackStep extends Position {
    public final AttackStepType type;
    public final ID name;
    public final Optional<TTCExpr> ttc;
    public final List<Meta> meta;
    public final Optional<Requires> requires;
    public final Optional<Reaches> reaches;

    public AttackStep(Position pos, AttackStepType type, ID name, Optional<TTCExpr> ttc, List<Meta> meta, Optional<Requires> requires, Optional<Reaches> reaches) {
      super(pos);
      this.type = type;
      this.name = name;
      this.ttc = ttc;
      this.meta = meta;
      this.requires = requires;
      this.reaches = reaches;
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      sb.append(String.format("    <%s:%d:%d>AttackStep(%s, %s)\n", filename, line, col, type.name(), name.toString()));
      if (!ttc.isEmpty()) {
        sb.append(String.format("      [%s]\n", ttc.get().toString()));
      }
      for (var m : meta) {
        sb.append(String.format("      %s\n", m.toString()));
      }
      if (!requires.isEmpty()) {
        sb.append(requires.get().toString());
      }
      if (!reaches.isEmpty()) {
        sb.append(reaches.get().toString());
      }
      return sb.toString();
    }
  }

  public static abstract class TTCExpr extends Position {
    public TTCExpr(Position pos) {
      super(pos);
    }
  }

  public static abstract class TTCBinaryExpr extends TTCExpr {
    public final TTCExpr lhs;
    public final TTCExpr rhs;

    public TTCBinaryExpr(Position pos, TTCExpr lhs, TTCExpr rhs) {
      super(pos);
      this.lhs = lhs;
      this.rhs = rhs;
    }
  }

  public static class TTCAddExpr extends TTCBinaryExpr {
    public TTCAddExpr(Position pos, TTCExpr lhs, TTCExpr rhs) {
      super(pos, lhs, rhs);
    }

    @Override
    public String toString() {
      return String.format("(%s) + (%s)", lhs.toString(), rhs.toString());
    }
  }

  public static class TTCSubExpr extends TTCBinaryExpr {
    public TTCSubExpr(Position pos, TTCExpr lhs, TTCExpr rhs) {
      super(pos, lhs, rhs);
    }

    @Override
    public String toString() {
      return String.format("(%s) - (%s)", lhs.toString(), rhs.toString());
    }
  }

  public static class TTCMulExpr extends TTCBinaryExpr {
    public TTCMulExpr(Position pos, TTCExpr lhs, TTCExpr rhs) {
      super(pos, lhs, rhs);
    }

    @Override
    public String toString() {
      return String.format("(%s) * (%s)", lhs.toString(), rhs.toString());
    }
  }

  public static class TTCDivExpr extends TTCBinaryExpr {
    public TTCDivExpr(Position pos, TTCExpr lhs, TTCExpr rhs) {
      super(pos, lhs, rhs);
    }

    @Override
    public String toString() {
      return String.format("(%s) / (%s)", lhs.toString(), rhs.toString());
    }
  }

  public static class TTCPowExpr extends TTCBinaryExpr {
    public TTCPowExpr(Position pos, TTCExpr lhs, TTCExpr rhs) {
      super(pos, lhs, rhs);
    }

    @Override
    public String toString() {
      return String.format("(%s) ^ (%s)", lhs.toString(), rhs.toString());
    }
  }

  public static class TTCFuncExpr extends TTCExpr {
    public final ID name;
    public final List<Double> params;

    public TTCFuncExpr(Position pos, ID name, List<Double> params) {
      super(pos);
      this.name = name;
      this.params = params;
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      sb.append(String.format("%s(", name.toString()));
      for (int i = 0; i < params.size(); ++i) {
        if (i == 0) {
          sb.append(params.get(i));
        } else {
          sb.append(String.format(", %f", params.get(i)));
        }
      }
      sb.append(')');
      return sb.toString();
    }
  }

  public static class Requires extends Position {
    public final List<Statement> requires;

    public Requires(Position pos, List<Statement> requires) {
      super(pos);
      this.requires = requires;
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      sb.append("      <- ");
      for (int i = 0; i < requires.size(); ++i) {
        if (i == 0) {
          sb.append(requires.get(i).toString());
        } else {
          sb.append(String.format(", %s", requires.get(i).toString()));
        }
      }
      sb.append('\n');
      return sb.toString();
    }
  }

  public static class Reaches extends Position {
    public final boolean inherits;
    public final List<Statement> statements;

    public Reaches(Position pos, boolean inherits, List<Statement> statements) {
      super(pos);
      this.inherits = inherits;
      this.statements = statements;
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      if (inherits) {
        sb.append("      +>\n");
      } else {
        sb.append("      ->\n");
      }
      for (int i = 0; i < statements.size(); ++i) {
        if (i + 1 < statements.size()) {
          sb.append(String.format("        %s,\n", statements.get(i).toString()));
        } else {
          sb.append(String.format("        %s\n", statements.get(i).toString()));
        }
      }
      return sb.toString();
    }
  }

  public static abstract class Statement extends Position {
    public Statement(Position pos) {
      super(pos);
    }
  }

  public static class Variable extends Statement {
    public final ID name;
    public final Expr expr;

    public Variable(Position pos, ID name, Expr expr) {
      super(pos);
      this.name = name;
      this.expr = expr;
    }

    @Override
    public String toString() {
      return String.format("<%s:%d:%d>Variable(%s, %s)", filename, line, col, name.toString(), expr.toString());
    }
  }

  public static abstract class Expr extends Statement {
    public Expr(Position pos) {
      super(pos);
    }
  }

  public static abstract class BinaryExpr extends Expr {
    public final Expr lhs;
    public final Expr rhs;

    public BinaryExpr(Position pos, Expr lhs, Expr rhs) {
      super(pos);
      this.lhs = lhs;
      this.rhs = rhs;
    }
  }

  public static class UnionExpr extends BinaryExpr {
    public UnionExpr(Position pos, Expr lhs, Expr rhs) {
      super(pos, lhs, rhs);
    }

    @Override
    public String toString() {
      return String.format("(%s) \\/ (%s)", lhs.toString(), rhs.toString());
    }
  }

  public static class IntersectionExpr extends BinaryExpr {
    public IntersectionExpr(Position pos, Expr lhs, Expr rhs) {
      super(pos, lhs, rhs);
    }

    @Override
    public String toString() {
      return String.format("(%s) /\\ (%s)", lhs.toString(), rhs.toString());
    }
  }

  public static class StepExpr extends BinaryExpr {
    public StepExpr(Position pos, Expr lhs, Expr rhs) {
      super(pos, lhs, rhs);
    }

    @Override
    public String toString() {
      return String.format("(%s).(%s)", lhs.toString(), rhs.toString());
    }
  }

  public static abstract class UnaryExpr extends Expr {
    public final Expr e;

    public UnaryExpr(Position pos, Expr e) {
      super(pos);
      this.e = e;
    }
  }

  public static class TransitiveExpr extends UnaryExpr {
    public TransitiveExpr(Position pos, Expr e) {
      super(pos, e);
    }

    @Override
    public String toString() {
      return String.format("(%s)*", e.toString());
    }
  }

  public static class SubTypeExpr extends UnaryExpr {
    public final ID subType;

    public SubTypeExpr(Position pos, Expr e, ID subType) {
      super(pos, e);
      this.subType = subType;
    }

    @Override
    public String toString() {
      return String.format("(%s)[%s]", e.toString(), subType.toString());
    }
  }

  public static class IDExpr extends Expr {
    public final ID id;

    public IDExpr(Position pos, ID id) {
      super(pos);
      this.id = id;
    }

    @Override
    public String toString() {
      return id.toString();
    }
  }

  public static class Association extends Position {
    public final ID leftAsset;
    public final ID leftField;
    public final Multiplicity leftMult;
    public final ID linkName;
    public final Multiplicity rightMult;
    public final ID rightField;
    public final ID rightAsset;
    public final List<Meta> meta;

    public Association(Position pos, ID leftAsset, ID leftField, Multiplicity leftMult, ID linkName, Multiplicity rightMult, ID rightField, ID rightAsset, List<Meta> meta) {
      super(pos);
      this.leftAsset = leftAsset;
      this.leftField = leftField;
      this.leftMult = leftMult;
      this.linkName = linkName;
      this.rightMult = rightMult;
      this.rightField = rightField;
      this.rightAsset = rightAsset;
      this.meta = meta;
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      sb.append(String.format("  <%s:%d:%d>Association(%s, %s, %s, %s, %s, %s, %s)\n", filename, line, col, leftAsset.toString(), leftField.toString(), leftMult.toString(), linkName.toString(), rightMult.toString(), rightField.toString(), rightAsset.toString()));
      for (var m : meta) {
        sb.append(String.format("    %s\n", m.toString()));
      }
      return sb.toString();
    }
  }

  public enum Multiplicity {
    ZERO_OR_ONE("0..1"),
    ZERO_OR_MORE("*"),
    ONE("1"),
    ONE_OR_MORE("1..*");

    private String string;

    private Multiplicity(String string) {
      this.string = string;
    }

    @Override
    public String toString() {
      return string;
    }
  }
}

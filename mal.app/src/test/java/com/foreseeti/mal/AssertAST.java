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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class AssertAST {
  // Prevent instantiation
  private AssertAST() {}

  private static File getFileClassPath(String filename) throws IOException {
    var resource = AssertAST.class.getClassLoader().getResource(filename);
    if (resource == null) {
      throw new IOException(String.format("%s: No such file or directory", filename));
    }
    return new File(resource.getFile());
  }

  public static String readFileClassPath(String filename) throws IOException {
    return Files.readString(getFileClassPath(filename).toPath());
  }

  public static String assertReadFileClassPath(String filename) {
    try {
      return readFileClassPath(filename);
    } catch (IOException e) {
      fail(e.getMessage());
    }
    fail("This should be unreachable");
    return null;
  }

  public static AST getASTClassPath(String filename) throws IOException, CompilerException {
    return Parser.parse(getFileClassPath(filename));
  }

  public static AST assertGetASTClassPath(String filename) {
    try {
      return getASTClassPath(filename);
    } catch (IOException | CompilerException e) {
      fail(e.getMessage());
    }
    fail("This should be unreachable");
    return null;
  }

  private static void assertEqualsPos(boolean expected, boolean actual, Position pos) {
    assertEquals(expected, actual, String.format("Failure at: %s", pos.toString()));
  }

  private static void assertEqualsPos(int expected, int actual, Position pos) {
    assertEquals(expected, actual, String.format("Failure at: %s", pos.toString()));
  }

  private static void assertEqualsPos(Object expected, Object actual, Position pos) {
    assertEquals(expected, actual, String.format("Failure at: %s", pos.toString()));
  }

  private static void assertTruePos(boolean condition, Position pos) {
    assertTrue(condition, String.format("Failure at: %s", pos.toString()));
  }

  private static void failPos(String message, Position pos) {
    fail(String.format("Failure at: %s: %s", pos.toString(), message));
  }

  public static void assertEmptyAST(AST ast) {
    assertEquals(0, ast.getCategories().size());
    assertEquals(0, ast.getAssociations().size());
    assertEquals(0, ast.getDefines().size());
  }

  private static void assertPosition(Position expected, Position actual) {
    assertEqualsPos(expected.filename, actual.filename, actual);
    assertEqualsPos(expected.line, actual.line, actual);
    assertEqualsPos(expected.col, actual.col, actual);
  }

  private static void assertID(AST.ID expected, AST.ID actual) {
    assertPosition(expected, actual);
    assertEqualsPos(expected.id, actual.id, actual);
  }

  public static void assertDefine(AST.Define expected, AST.Define actual) {
    assertPosition(expected, actual);
    assertID(expected.key, actual.key);
    assertEqualsPos(expected.value, actual.value, actual);
  }

  private static void assertMeta(AST.Meta expected, AST.Meta actual) {
    assertPosition(expected, actual);
    assertEqualsPos(expected.type, actual.type, actual);
    assertEqualsPos(expected.string, actual.string, actual);
  }

  private static void assertMetaList(List<AST.Meta> expected, List<AST.Meta> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertMeta(expected.get(i), actual.get(i));
    }
  }

  public static void assertCategory(AST.Category expected, AST.Category actual) {
    assertPosition(expected, actual);
    assertID(expected.name, actual.name);
    assertMetaList(expected.meta, actual.meta);
    assertAssetList(expected.assets, actual.assets);
  }

  private static void assertAsset(AST.Asset expected, AST.Asset actual) {
    assertPosition(expected, actual);
    assertEqualsPos(expected.isAbstract, actual.isAbstract, actual);
    assertID(expected.name, actual.name);
    if (expected.parent.isEmpty()) {
      assertTruePos(actual.parent.isEmpty(), actual);
    } else {
      assertID(expected.parent.get(), actual.parent.get());
    }
    assertMetaList(expected.meta, actual.meta);
    assertAttackStepList(expected.attackSteps, actual.attackSteps);
    assertVariableList(expected.variables, actual.variables);
  }

  private static void assertAssetList(List<AST.Asset> expected, List<AST.Asset> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertAsset(expected.get(i), actual.get(i));
    }
  }

  private static void assertAttackStep(AST.AttackStep expected, AST.AttackStep actual) {
    assertPosition(expected, actual);
    assertEqualsPos(expected.type, actual.type, actual);
    assertID(expected.name, actual.name);
    if (expected.ttc.isEmpty()) {
      assertTruePos(actual.ttc.isEmpty(), actual);
    } else {
      assertTTCExpr(expected.ttc.get(), actual.ttc.get());
    }
    assertMetaList(expected.meta, actual.meta);
    if (expected.requires.isEmpty()) {
      assertTruePos(actual.requires.isEmpty(), actual);
    } else {
      assertRequires(expected.requires.get(), actual.requires.get());
    }
    if (expected.reaches.isEmpty()) {
      assertTruePos(actual.reaches.isEmpty(), actual);
    } else {
      assertReaches(expected.reaches.get(), actual.reaches.get());
    }
  }

  private static void assertAttackStepList(
      List<AST.AttackStep> expected, List<AST.AttackStep> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertAttackStep(expected.get(i), actual.get(i));
    }
  }

  private static void assertTTCExpr(AST.TTCExpr expected, AST.TTCExpr actual) {
    assertPosition(expected, actual);
    if (expected instanceof AST.TTCAddExpr) {
      assertTruePos(actual instanceof AST.TTCAddExpr, actual);
      assertTTCBinaryExpr((AST.TTCBinaryExpr) expected, (AST.TTCBinaryExpr) actual);
    } else if (expected instanceof AST.TTCSubExpr) {
      assertTruePos(actual instanceof AST.TTCSubExpr, actual);
      assertTTCBinaryExpr((AST.TTCBinaryExpr) expected, (AST.TTCBinaryExpr) actual);
    } else if (expected instanceof AST.TTCMulExpr) {
      assertTruePos(actual instanceof AST.TTCMulExpr, actual);
      assertTTCBinaryExpr((AST.TTCBinaryExpr) expected, (AST.TTCBinaryExpr) actual);
    } else if (expected instanceof AST.TTCDivExpr) {
      assertTruePos(actual instanceof AST.TTCDivExpr, actual);
      assertTTCBinaryExpr((AST.TTCBinaryExpr) expected, (AST.TTCBinaryExpr) actual);
    } else if (expected instanceof AST.TTCPowExpr) {
      assertTruePos(actual instanceof AST.TTCPowExpr, actual);
      assertTTCBinaryExpr((AST.TTCBinaryExpr) expected, (AST.TTCBinaryExpr) actual);
    } else if (expected instanceof AST.TTCFuncExpr) {
      assertTruePos(actual instanceof AST.TTCFuncExpr, actual);
      assertTTCFuncExpr((AST.TTCFuncExpr) expected, (AST.TTCFuncExpr) actual);
    } else {
      failPos("Invalid expected subtype of TTCExpr", expected);
    }
  }

  private static void assertTTCBinaryExpr(AST.TTCBinaryExpr expected, AST.TTCBinaryExpr actual) {
    assertTTCExpr(expected.lhs, actual.lhs);
    assertTTCExpr(expected.rhs, actual.rhs);
  }

  private static void assertTTCFuncExpr(AST.TTCFuncExpr expected, AST.TTCFuncExpr actual) {
    assertID(expected.name, actual.name);
    assertDoubleList(expected.params, actual.params, actual);
  }

  private static void assertDoubleList(List<Double> expected, List<Double> actual, Position pos) {
    assertEqualsPos(expected.size(), actual.size(), pos);
    for (int i = 0; i < expected.size(); i++) {
      assertEqualsPos(expected.get(i), actual.get(i), pos);
    }
  }

  private static void assertRequires(AST.Requires expected, AST.Requires actual) {
    assertPosition(expected, actual);
    assertVariableList(expected.variables, actual.variables);
    assertExprList(expected.requires, actual.requires);
  }

  private static void assertReaches(AST.Reaches expected, AST.Reaches actual) {
    assertPosition(expected, actual);
    assertEqualsPos(expected.inherits, actual.inherits, actual);
    assertVariableList(expected.variables, actual.variables);
    assertExprList(expected.reaches, actual.reaches);
  }

  private static void assertVariable(AST.Variable expected, AST.Variable actual) {
    assertPosition(expected, actual);
    assertID(expected.name, actual.name);
    assertExpr(expected.expr, actual.expr);
  }

  private static void assertVariableList(List<AST.Variable> expected, List<AST.Variable> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertVariable(expected.get(i), actual.get(i));
    }
  }

  private static void assertExpr(AST.Expr expected, AST.Expr actual) {
    assertPosition(expected, actual);
    if (expected instanceof AST.UnionExpr) {
      assertTruePos(actual instanceof AST.UnionExpr, actual);
      assertBinaryExpr((AST.BinaryExpr) expected, (AST.BinaryExpr) actual);
    } else if (expected instanceof AST.IntersectionExpr) {
      assertTruePos(actual instanceof AST.IntersectionExpr, actual);
      assertBinaryExpr((AST.BinaryExpr) expected, (AST.BinaryExpr) actual);
    } else if (expected instanceof AST.StepExpr) {
      assertTruePos(actual instanceof AST.StepExpr, actual);
      assertBinaryExpr((AST.BinaryExpr) expected, (AST.BinaryExpr) actual);
    } else if (expected instanceof AST.TransitiveExpr) {
      assertTruePos(actual instanceof AST.TransitiveExpr, actual);
      assertTransitiveExpr((AST.TransitiveExpr) expected, (AST.TransitiveExpr) actual);
    } else if (expected instanceof AST.SubTypeExpr) {
      assertTruePos(actual instanceof AST.SubTypeExpr, actual);
      assertSubTypeExpr((AST.SubTypeExpr) expected, (AST.SubTypeExpr) actual);
    } else if (expected instanceof AST.IDExpr) {
      assertTruePos(actual instanceof AST.IDExpr, actual);
      assertIDExpr((AST.IDExpr) expected, (AST.IDExpr) actual);
    } else {
      failPos("Invalid expected subtype of Expr", expected);
    }
  }

  private static void assertExprList(List<AST.Expr> expected, List<AST.Expr> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertExpr(expected.get(i), actual.get(i));
    }
  }

  private static void assertBinaryExpr(AST.BinaryExpr expected, AST.BinaryExpr actual) {
    assertExpr(expected.lhs, actual.lhs);
    assertExpr(expected.rhs, actual.rhs);
  }

  private static void assertTransitiveExpr(AST.TransitiveExpr expected, AST.TransitiveExpr actual) {
    assertExpr(expected.e, actual.e);
  }

  private static void assertSubTypeExpr(AST.SubTypeExpr expected, AST.SubTypeExpr actual) {
    assertExpr(expected.e, actual.e);
    assertID(expected.subType, actual.subType);
  }

  private static void assertIDExpr(AST.IDExpr expected, AST.IDExpr actual) {
    assertID(expected.id, actual.id);
  }

  public static void assertAssociation(AST.Association expected, AST.Association actual) {
    assertPosition(expected, actual);
    assertID(expected.leftAsset, actual.leftAsset);
    assertID(expected.leftField, actual.leftField);
    assertEqualsPos(expected.leftMult, actual.leftMult, actual);
    assertID(expected.linkName, actual.linkName);
    assertEqualsPos(expected.rightMult, actual.rightMult, actual);
    assertID(expected.rightField, actual.rightField);
    assertID(expected.rightAsset, actual.rightAsset);
    assertMetaList(expected.meta, actual.meta);
  }
}

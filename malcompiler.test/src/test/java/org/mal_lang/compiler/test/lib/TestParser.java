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
package org.mal_lang.compiler.test.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mal_lang.compiler.test.lib.AssertAST.assertAssociation;
import static org.mal_lang.compiler.test.lib.AssertAST.assertCategory;
import static org.mal_lang.compiler.test.lib.AssertAST.assertDefine;
import static org.mal_lang.compiler.test.lib.AssertAST.assertEmptyAST;
import static org.mal_lang.compiler.test.lib.AssertAST.assertGetASTClassPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.lib.AST;
import org.mal_lang.compiler.lib.Position;
import org.mal_lang.compiler.test.MalTest;

public class TestParser extends MalTest {
  @Test
  public void testEmpty() {
    var ast = assertGetASTClassPath("parser/empty.mal");
    assertEmptyAST(ast);
  }

  @Test
  public void testWhitespace() {
    var ast = assertGetASTClassPath("parser/whitespace.mal");
    assertEmptyAST(ast);
  }

  @Test
  public void testDefines() {
    var ast = assertGetASTClassPath("parser/defines.mal");
    assertEquals(0, ast.getCategories().size());
    assertEquals(0, ast.getAssociations().size());
    var defines = ast.getDefines();
    assertEquals(3, defines.size());
    assertDefine(
        new AST.Define(
            new Position("defines.mal", 1, 1),
            new AST.ID(new Position("defines.mal", 1, 2), "mal"),
            "MAL"),
        defines.get(0));
    assertDefine(
        new AST.Define(
            new Position("defines.mal", 3, 5),
            new AST.ID(new Position("defines.mal", 3, 7), "hello"),
            "Hello!"),
        defines.get(1));
    assertDefine(
        new AST.Define(
            new Position("defines.mal", 5, 1),
            new AST.ID(new Position("defines.mal", 6, 1), "def"),
            "String\nLine"),
        defines.get(2));
  }

  @Test
  public void testCategories() {
    var ast = assertGetASTClassPath("parser/categories.mal");
    var categories = ast.getCategories();
    assertEquals(5, categories.size());
    assertCategory(
        new AST.Category(
            new Position("categories.mal", 1, 1),
            new AST.ID(new Position("categories.mal", 1, 10), "C1"),
            new ArrayList<AST.Meta>(),
            Arrays.asList(
                new AST.Asset(
                    new Position("categories.mal", 2, 3),
                    false,
                    new AST.ID(new Position("categories.mal", 2, 9), "A1"),
                    Optional.empty(),
                    new ArrayList<AST.Meta>(),
                    new ArrayList<AST.AttackStep>(),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("categories.mal", 3, 3),
                    true,
                    new AST.ID(new Position("categories.mal", 3, 18), "A2"),
                    Optional.empty(),
                    new ArrayList<AST.Meta>(),
                    new ArrayList<AST.AttackStep>(),
                    new ArrayList<AST.Variable>()))),
        categories.get(0));
    assertCategory(
        new AST.Category(
            new Position("categories.mal", 6, 1),
            new AST.ID(new Position("categories.mal", 6, 10), "C2"),
            Arrays.asList(
                new AST.Meta(
                    new Position("categories.mal", 7, 3), AST.MetaType.ASSUMPTIONS, "none")),
            Arrays.asList(
                new AST.Asset(
                    new Position("categories.mal", 9, 3),
                    false,
                    new AST.ID(new Position("categories.mal", 9, 9), "A3"),
                    Optional.of(new AST.ID(new Position("categories.mal", 9, 20), "A1")),
                    new ArrayList<AST.Meta>(),
                    new ArrayList<AST.AttackStep>(),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("categories.mal", 10, 3),
                    true,
                    new AST.ID(new Position("categories.mal", 10, 18), "A4"),
                    Optional.of(new AST.ID(new Position("categories.mal", 10, 29), "A2")),
                    new ArrayList<AST.Meta>(),
                    new ArrayList<AST.AttackStep>(),
                    new ArrayList<AST.Variable>()))),
        categories.get(1));
    assertCategory(
        new AST.Category(
            new Position("categories.mal", 13, 1),
            new AST.ID(new Position("categories.mal", 13, 10), "C3"),
            Arrays.asList(
                new AST.Meta(
                    new Position("categories.mal", 14, 3), AST.MetaType.INFO, "this is first C3"),
                new AST.Meta(
                    new Position("categories.mal", 15, 3), AST.MetaType.INFO, "another info"),
                new AST.Meta(
                    new Position("categories.mal", 16, 3), AST.MetaType.RATIONALE, "just to test"),
                new AST.Meta(
                    new Position("categories.mal", 17, 3),
                    AST.MetaType.ASSUMPTIONS,
                    "will not run through the analyzer")),
            new ArrayList<AST.Asset>()),
        categories.get(2));
    assertCategory(
        new AST.Category(
            new Position("categories.mal", 21, 1),
            new AST.ID(new Position("categories.mal", 21, 10), "C2"),
            new ArrayList<AST.Meta>(),
            new ArrayList<AST.Asset>()),
        categories.get(3));
    assertCategory(
        new AST.Category(
            new Position("categories.mal", 25, 1),
            new AST.ID(new Position("categories.mal", 25, 10), "C3"),
            new ArrayList<AST.Meta>(),
            new ArrayList<AST.Asset>()),
        categories.get(4));
    assertEquals(0, ast.getAssociations().size());
    assertEquals(0, ast.getDefines().size());
  }

  @Test
  public void testAssociations() {
    var ast = assertGetASTClassPath("parser/associations.mal");
    assertEquals(0, ast.getCategories().size());
    var associations = ast.getAssociations();
    assertEquals(37, associations.size());
    var mults =
        new AST.Multiplicity[] {
          AST.Multiplicity.ONE,
          AST.Multiplicity.ZERO_OR_MORE,
          AST.Multiplicity.ZERO_OR_ONE,
          AST.Multiplicity.ZERO_OR_MORE,
          AST.Multiplicity.ONE,
          AST.Multiplicity.ONE_OR_MORE
        };
    for (int idx = 0, i = 0; i < mults.length; i++) {
      for (int j = 0; j < mults.length; j++) {
        assertAssociation(
            new AST.Association(
                new Position("associations.mal", 3 + idx, 3),
                new AST.ID(new Position("associations.mal", 3 + idx, 3), "A1"),
                new AST.ID(new Position("associations.mal", 3 + idx, 7), "b"),
                mults[i],
                new AST.ID(new Position("associations.mal", 3 + idx, 19), "L"),
                mults[j],
                new AST.ID(new Position("associations.mal", 3 + idx, 31), "a"),
                new AST.ID(new Position("associations.mal", 3 + idx, 34), "A2"),
                new ArrayList<AST.Meta>()),
            associations.get(idx++));
      }
    }
    assertAssociation(
        new AST.Association(
            new Position("associations.mal", 47, 3),
            new AST.ID(new Position("associations.mal", 47, 3), "A1"),
            new AST.ID(new Position("associations.mal", 47, 7), "b"),
            AST.Multiplicity.ONE,
            new AST.ID(new Position("associations.mal", 47, 19), "L"),
            AST.Multiplicity.ONE,
            new AST.ID(new Position("associations.mal", 47, 31), "a"),
            new AST.ID(new Position("associations.mal", 47, 34), "A2"),
            Arrays.asList(
                new AST.Meta(new Position("associations.mal", 48, 6), AST.MetaType.INFO, "testing"),
                new AST.Meta(
                    new Position("associations.mal", 49, 6), AST.MetaType.RATIONALE, "hej"),
                new AST.Meta(
                    new Position("associations.mal", 50, 6),
                    AST.MetaType.ASSUMPTIONS,
                    "\"!\"!\"!\""))),
        associations.get(36));
    assertEquals(0, ast.getDefines().size());
  }

  @Test
  public void testInclude() {
    var ast = assertGetASTClassPath("parser/include.mal");
    assertEquals(0, ast.getCategories().size());
    assertEquals(0, ast.getAssociations().size());
    var defines = ast.getDefines();
    assertEquals(5, defines.size());
    assertDefine(
        new AST.Define(
            new Position("include.mal", 1, 1),
            new AST.ID(new Position("include.mal", 1, 2), "a"),
            "b"),
        defines.get(0));
    assertDefine(
        new AST.Define(
            new Position("included1.mal", 1, 1),
            new AST.ID(new Position("included1.mal", 1, 2), "c"),
            "d"),
        defines.get(1));
    assertDefine(
        new AST.Define(
            new Position("subDir/subIncluded1.mal", 1, 1),
            new AST.ID(new Position("subDir/subIncluded1.mal", 1, 2), "d"),
            "e"),
        defines.get(2));
    assertDefine(
        new AST.Define(
            new Position("included2.mal", 1, 1),
            new AST.ID(new Position("included2.mal", 1, 2), "e"),
            "f"),
        defines.get(3));
    assertDefine(
        new AST.Define(
            new Position("include.mal", 5, 1),
            new AST.ID(new Position("include.mal", 5, 2), "b"),
            "c"),
        defines.get(4));
  }

  @Test
  public void testAssets() {
    var ast = assertGetASTClassPath("parser/assets.mal");
    var categories = ast.getCategories();
    assertEquals(1, categories.size());
    assertCategory(
        new AST.Category(
            new Position("assets.mal", 1, 1),
            new AST.ID(new Position("assets.mal", 1, 10), "Cat"),
            new ArrayList<AST.Meta>(),
            Arrays.asList(
                new AST.Asset(
                    new Position("assets.mal", 2, 3),
                    false,
                    new AST.ID(new Position("assets.mal", 2, 9), "A1"),
                    Optional.empty(),
                    Arrays.asList(
                        new AST.Meta(new Position("assets.mal", 3, 5), AST.MetaType.INFO, "Info1"),
                        new AST.Meta(
                            new Position("assets.mal", 4, 5), AST.MetaType.RATIONALE, "Reason1"),
                        new AST.Meta(
                            new Position("assets.mal", 5, 5), AST.MetaType.ASSUMPTIONS, "None1")),
                    new ArrayList<AST.AttackStep>(),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("assets.mal", 9, 3),
                    true,
                    new AST.ID(new Position("assets.mal", 9, 18), "A2"),
                    Optional.empty(),
                    Arrays.asList(
                        new AST.Meta(new Position("assets.mal", 10, 5), AST.MetaType.INFO, "Info2"),
                        new AST.Meta(
                            new Position("assets.mal", 11, 5), AST.MetaType.RATIONALE, "Reason2"),
                        new AST.Meta(
                            new Position("assets.mal", 12, 5), AST.MetaType.ASSUMPTIONS, "None2")),
                    new ArrayList<AST.AttackStep>(),
                    Arrays.asList(
                        new AST.Variable(
                            new Position("assets.mal", 14, 5),
                            new AST.ID(new Position("assets.mal", 14, 9), "x"),
                            new AST.IDExpr(
                                new Position("assets.mal", 14, 13),
                                new AST.ID(new Position("assets.mal", 14, 13), "y"))))),
                new AST.Asset(
                    new Position("assets.mal", 17, 3),
                    false,
                    new AST.ID(new Position("assets.mal", 17, 9), "A3"),
                    Optional.of(new AST.ID(new Position("assets.mal", 17, 20), "A1")),
                    Arrays.asList(
                        new AST.Meta(new Position("assets.mal", 18, 5), AST.MetaType.INFO, "Info3"),
                        new AST.Meta(
                            new Position("assets.mal", 19, 5), AST.MetaType.RATIONALE, "Reason3"),
                        new AST.Meta(
                            new Position("assets.mal", 20, 5), AST.MetaType.ASSUMPTIONS, "None3")),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("assets.mal", 22, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("assets.mal", 22, 7), "a"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("assets.mal", 23, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("assets.mal", 23, 7), "a"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty())),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("assets.mal", 26, 3),
                    true,
                    new AST.ID(new Position("assets.mal", 26, 18), "A4"),
                    Optional.of(new AST.ID(new Position("assets.mal", 26, 29), "A2")),
                    Arrays.asList(
                        new AST.Meta(new Position("assets.mal", 27, 5), AST.MetaType.INFO, "Info4"),
                        new AST.Meta(
                            new Position("assets.mal", 28, 5), AST.MetaType.RATIONALE, "Reason4"),
                        new AST.Meta(
                            new Position("assets.mal", 29, 5), AST.MetaType.ASSUMPTIONS, "None4")),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("assets.mal", 31, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position("assets.mal", 31, 7), "a"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("assets.mal", 33, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position("assets.mal", 33, 7), "b"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("assets.mal", 34, 5),
                            AST.AttackStepType.NOTEXIST,
                            new AST.ID(new Position("assets.mal", 34, 8), "c"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty())),
                    Arrays.asList(
                        new AST.Variable(
                            new Position("assets.mal", 32, 5),
                            new AST.ID(new Position("assets.mal", 32, 9), "x"),
                            new AST.IDExpr(
                                new Position("assets.mal", 32, 13),
                                new AST.ID(new Position("assets.mal", 32, 13), "y"))))))),
        categories.get(0));
    assertEquals(0, ast.getAssociations().size());
    assertEquals(0, ast.getDefines().size());
  }

  @Test
  public void testAttackSteps() {
    var ast = assertGetASTClassPath("parser/attacksteps.mal");
    var categories = ast.getCategories();
    assertEquals(1, categories.size());
    assertCategory(
        new AST.Category(
            new Position("attacksteps.mal", 1, 1),
            new AST.ID(new Position("attacksteps.mal", 1, 10), "Cat"),
            new ArrayList<AST.Meta>(),
            Arrays.asList(
                new AST.Asset(
                    new Position("attacksteps.mal", 3, 3),
                    false,
                    new AST.ID(new Position("attacksteps.mal", 3, 9), "A1"),
                    Optional.empty(),
                    new ArrayList<AST.Meta>(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 4, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("attacksteps.mal", 4, 8), "a"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCAddExpr(
                                    new Position("attacksteps.mal", 4, 11),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 4, 11),
                                        new AST.ID(new Position("attacksteps.mal", 4, 11), "a"),
                                        new ArrayList<Double>()),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 4, 13),
                                        new AST.ID(new Position("attacksteps.mal", 4, 13), "b"),
                                        new ArrayList<Double>()))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 5, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 5, 8), "b"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCSubExpr(
                                    new Position("attacksteps.mal", 5, 11),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 5, 11),
                                        new AST.ID(new Position("attacksteps.mal", 5, 11), "a"),
                                        new ArrayList<Double>()),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 5, 13),
                                        new AST.ID(new Position("attacksteps.mal", 5, 13), "b"),
                                        new ArrayList<Double>()))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 6, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position("attacksteps.mal", 6, 8), "c"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCMulExpr(
                                    new Position("attacksteps.mal", 6, 11),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 6, 11),
                                        new AST.ID(new Position("attacksteps.mal", 6, 11), "a"),
                                        new ArrayList<Double>()),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 6, 13),
                                        new AST.ID(new Position("attacksteps.mal", 6, 13), "b"),
                                        new ArrayList<Double>()))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 7, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position("attacksteps.mal", 7, 8), "d"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCDivExpr(
                                    new Position("attacksteps.mal", 7, 11),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 7, 11),
                                        new AST.ID(new Position("attacksteps.mal", 7, 11), "a"),
                                        new ArrayList<Double>()),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 7, 13),
                                        new AST.ID(new Position("attacksteps.mal", 7, 13), "b"),
                                        new ArrayList<Double>()))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 8, 5),
                            AST.AttackStepType.NOTEXIST,
                            new AST.ID(new Position("attacksteps.mal", 8, 8), "e"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCPowExpr(
                                    new Position("attacksteps.mal", 8, 11),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 8, 11),
                                        new AST.ID(new Position("attacksteps.mal", 8, 11), "a"),
                                        new ArrayList<Double>()),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 8, 13),
                                        new AST.ID(new Position("attacksteps.mal", 8, 13), "b"),
                                        new ArrayList<Double>()))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 9, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("attacksteps.mal", 9, 8), "a"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCAddExpr(
                                    new Position("attacksteps.mal", 9, 11),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 9, 11),
                                        new AST.ID(new Position("attacksteps.mal", 9, 11), "a"),
                                        new ArrayList<Double>()),
                                    new AST.TTCMulExpr(
                                        new Position("attacksteps.mal", 9, 13),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 9, 13),
                                            new AST.ID(new Position("attacksteps.mal", 9, 13), "b"),
                                            new ArrayList<Double>()),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 9, 15),
                                            new AST.ID(new Position("attacksteps.mal", 9, 15), "c"),
                                            new ArrayList<Double>())))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 10, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 10, 8), "b"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCAddExpr(
                                    new Position("attacksteps.mal", 10, 11),
                                    new AST.TTCMulExpr(
                                        new Position("attacksteps.mal", 10, 11),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 10, 11),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 10, 11), "a"),
                                            new ArrayList<Double>()),
                                        new AST.TTCPowExpr(
                                            new Position("attacksteps.mal", 10, 13),
                                            new AST.TTCFuncExpr(
                                                new Position("attacksteps.mal", 10, 13),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 10, 13), "b"),
                                                new ArrayList<Double>()),
                                            new AST.TTCFuncExpr(
                                                new Position("attacksteps.mal", 10, 15),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 10, 15), "e"),
                                                new ArrayList<Double>()))),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 10, 17),
                                        new AST.ID(new Position("attacksteps.mal", 10, 17), "c"),
                                        new ArrayList<Double>()))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 11, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position("attacksteps.mal", 11, 8), "c"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCMulExpr(
                                    new Position("attacksteps.mal", 11, 11),
                                    new AST.TTCAddExpr(
                                        new Position("attacksteps.mal", 11, 12),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 11, 12),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 11, 12), "a"),
                                            new ArrayList<Double>()),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 11, 14),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 11, 14), "b"),
                                            new ArrayList<Double>())),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 11, 17),
                                        new AST.ID(new Position("attacksteps.mal", 11, 17), "c"),
                                        new ArrayList<Double>()))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 12, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position("attacksteps.mal", 12, 8), "d"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCAddExpr(
                                    new Position("attacksteps.mal", 12, 11),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 12, 11),
                                        new AST.ID(new Position("attacksteps.mal", 12, 11), "a"),
                                        new ArrayList<Double>()),
                                    new AST.TTCMulExpr(
                                        new Position("attacksteps.mal", 12, 14),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 12, 14),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 12, 14), "b"),
                                            new ArrayList<Double>()),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 12, 16),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 12, 16), "c"),
                                            new ArrayList<Double>())))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 13, 5),
                            AST.AttackStepType.NOTEXIST,
                            new AST.ID(new Position("attacksteps.mal", 13, 8), "e"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCAddExpr(
                                    new Position("attacksteps.mal", 13, 11),
                                    new AST.TTCMulExpr(
                                        new Position("attacksteps.mal", 13, 12),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 13, 12),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 13, 12), "a"),
                                            new ArrayList<Double>()),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 13, 14),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 13, 14), "b"),
                                            new ArrayList<Double>())),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 13, 17),
                                        new AST.ID(new Position("attacksteps.mal", 13, 17), "c"),
                                        new ArrayList<Double>()))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 14, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("attacksteps.mal", 14, 8), "a"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCMulExpr(
                                    new Position("attacksteps.mal", 14, 11),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 14, 11),
                                        new AST.ID(new Position("attacksteps.mal", 14, 11), "a"),
                                        new ArrayList<Double>()),
                                    new AST.TTCAddExpr(
                                        new Position("attacksteps.mal", 14, 14),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 14, 14),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 14, 14), "b"),
                                            new ArrayList<Double>()),
                                        new AST.TTCFuncExpr(
                                            new Position("attacksteps.mal", 14, 16),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 14, 16), "c"),
                                            new ArrayList<Double>())))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 15, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 15, 8), "b"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("attacksteps.mal", 15, 11),
                                    new AST.ID(new Position("attacksteps.mal", 15, 11), "a"),
                                    new ArrayList<Double>())),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 16, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position("attacksteps.mal", 16, 8), "c"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("attacksteps.mal", 16, 11),
                                    new AST.ID(new Position("attacksteps.mal", 16, 11), "a"),
                                    Arrays.asList(Double.valueOf(0.0)))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 17, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position("attacksteps.mal", 17, 8), "d"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("attacksteps.mal", 17, 11),
                                    new AST.ID(new Position("attacksteps.mal", 17, 11), "a"),
                                    Arrays.asList(Double.valueOf(13.0)))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 18, 5),
                            AST.AttackStepType.NOTEXIST,
                            new AST.ID(new Position("attacksteps.mal", 18, 8), "e"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("attacksteps.mal", 18, 11),
                                    new AST.ID(new Position("attacksteps.mal", 18, 11), "a"),
                                    Arrays.asList(Double.valueOf(3.14)))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 19, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("attacksteps.mal", 19, 8), "a"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("attacksteps.mal", 19, 11),
                                    new AST.ID(new Position("attacksteps.mal", 19, 11), "a"),
                                    Arrays.asList(Double.valueOf(1.0), Double.valueOf(1.1)))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 20, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 20, 8), "b"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCAddExpr(
                                    new Position("attacksteps.mal", 20, 11),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 20, 11),
                                        new AST.ID(new Position("attacksteps.mal", 20, 11), "a"),
                                        Arrays.asList(
                                            Double.valueOf(1.0),
                                            Double.valueOf(2.3),
                                            Double.valueOf(3.0))),
                                    new AST.TTCFuncExpr(
                                        new Position("attacksteps.mal", 20, 26),
                                        new AST.ID(new Position("attacksteps.mal", 20, 26), "b"),
                                        new ArrayList<Double>()))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty())),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("attacksteps.mal", 24, 3),
                    false,
                    new AST.ID(new Position("attacksteps.mal", 24, 9), "A2"),
                    Optional.empty(),
                    new ArrayList<AST.Meta>(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 25, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 25, 7), "a1"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 26, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 26, 7), "a2"),
                            Optional.of(new ArrayList<AST.CIA>()),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 27, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 27, 7), "a3"),
                            Optional.of(Arrays.asList(AST.CIA.C)),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 28, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 28, 7), "a4"),
                            Optional.of(Arrays.asList(AST.CIA.C, AST.CIA.C)),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 29, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 29, 7), "a5"),
                            Optional.of(Arrays.asList(AST.CIA.C, AST.CIA.I, AST.CIA.A)),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 30, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 30, 7), "a6"),
                            Optional.of(Arrays.asList(AST.CIA.A, AST.CIA.C, AST.CIA.I)),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 31, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 31, 7), "a7"),
                            Optional.of(
                                Arrays.asList(
                                    AST.CIA.C, AST.CIA.C, AST.CIA.I, AST.CIA.I, AST.CIA.A,
                                    AST.CIA.A)),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty())),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("attacksteps.mal", 35, 3),
                    false,
                    new AST.ID(new Position("attacksteps.mal", 35, 9), "A3"),
                    Optional.empty(),
                    new ArrayList<AST.Meta>(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 36, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("attacksteps.mal", 36, 7), "a"),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position("attacksteps.mal", 37, 7),
                                    AST.MetaType.INFO,
                                    "Info")),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 38, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 38, 7), "b"),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position("attacksteps.mal", 39, 7),
                                    AST.MetaType.INFO,
                                    "Info"),
                                new AST.Meta(
                                    new Position("attacksteps.mal", 40, 7),
                                    AST.MetaType.RATIONALE,
                                    "Reason")),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 41, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position("attacksteps.mal", 41, 7), "c"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("attacksteps.mal", 41, 10),
                                    new AST.ID(new Position("attacksteps.mal", 41, 10), "TTC"),
                                    new ArrayList<Double>())),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position("attacksteps.mal", 42, 7),
                                    AST.MetaType.INFO,
                                    "Info")),
                            Optional.empty(),
                            Optional.empty())),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("attacksteps.mal", 46, 3),
                    false,
                    new AST.ID(new Position("attacksteps.mal", 46, 9), "A4"),
                    Optional.empty(),
                    new ArrayList<AST.Meta>(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 47, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position("attacksteps.mal", 47, 7), "a"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.of(
                                new AST.Requires(
                                    new Position("attacksteps.mal", 48, 7),
                                    Arrays.asList(
                                        new AST.Variable(
                                            new Position("attacksteps.mal", 48, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 48, 14), "x"),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 48, 18),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 48, 18), "y"))),
                                        new AST.Variable(
                                            new Position("attacksteps.mal", 50, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 50, 14), "a"),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 50, 18),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 50, 18),
                                                    "b")))),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("attacksteps.mal", 49, 10),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 49, 10),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 49, 10), "a")),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 49, 12),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 49, 12), "y"))),
                                        new AST.IDExpr(
                                            new Position("attacksteps.mal", 51, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 51, 10), "b")),
                                        new AST.UnionExpr(
                                            new Position("attacksteps.mal", 52, 10),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 52, 10),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 52, 10), "c")),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 52, 15),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 52, 15), "d"))),
                                        new AST.StepExpr(
                                            new Position("attacksteps.mal", 53, 10),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 53, 10),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 53, 10), "a")),
                                            new AST.TransitiveExpr(
                                                new Position("attacksteps.mal", 53, 12),
                                                new AST.SubTypeExpr(
                                                    new Position("attacksteps.mal", 53, 13),
                                                    new AST.IDExpr(
                                                        new Position("attacksteps.mal", 53, 13),
                                                        new AST.ID(
                                                            new Position("attacksteps.mal", 53, 13),
                                                            "b")),
                                                    new AST.ID(
                                                        new Position("attacksteps.mal", 53, 15),
                                                        "D"))))))),
                            Optional.empty())),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("attacksteps.mal", 57, 3),
                    false,
                    new AST.ID(new Position("attacksteps.mal", 57, 9), "A5"),
                    Optional.empty(),
                    new ArrayList<AST.Meta>(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 58, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("attacksteps.mal", 58, 7), "a"),
                            Optional.of(Arrays.asList(AST.CIA.I)),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("attacksteps.mal", 59, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.Variable(
                                            new Position("attacksteps.mal", 59, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 59, 14), "x"),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 59, 18),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 59, 18), "y"))),
                                        new AST.Variable(
                                            new Position("attacksteps.mal", 61, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 61, 14), "a"),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 61, 18),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 61, 18),
                                                    "b")))),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("attacksteps.mal", 60, 10),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 60, 10),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 60, 10), "a")),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 60, 12),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 60, 12), "y"))),
                                        new AST.IDExpr(
                                            new Position("attacksteps.mal", 62, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 62, 10), "b")),
                                        new AST.UnionExpr(
                                            new Position("attacksteps.mal", 63, 10),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 63, 10),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 63, 10), "c")),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 63, 15),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 63, 15), "d"))),
                                        new AST.StepExpr(
                                            new Position("attacksteps.mal", 64, 10),
                                            new AST.IDExpr(
                                                new Position("attacksteps.mal", 64, 10),
                                                new AST.ID(
                                                    new Position("attacksteps.mal", 64, 10), "a")),
                                            new AST.TransitiveExpr(
                                                new Position("attacksteps.mal", 64, 12),
                                                new AST.SubTypeExpr(
                                                    new Position("attacksteps.mal", 64, 13),
                                                    new AST.IDExpr(
                                                        new Position("attacksteps.mal", 64, 13),
                                                        new AST.ID(
                                                            new Position("attacksteps.mal", 64, 13),
                                                            "b")),
                                                    new AST.ID(
                                                        new Position("attacksteps.mal", 64, 15),
                                                        "D")))))))),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 65, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("attacksteps.mal", 65, 7), "b"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.of(
                                new AST.Requires(
                                    new Position("attacksteps.mal", 66, 7),
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("attacksteps.mal", 66, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 66, 10), "x"))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("attacksteps.mal", 67, 7),
                                    false,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("attacksteps.mal", 67, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 67, 10), "y")))))),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 68, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position("attacksteps.mal", 68, 7), "c"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("attacksteps.mal", 68, 10),
                                    new AST.ID(new Position("attacksteps.mal", 68, 10), "t"),
                                    new ArrayList<Double>())),
                            new ArrayList<AST.Meta>(),
                            Optional.of(
                                new AST.Requires(
                                    new Position("attacksteps.mal", 69, 7),
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("attacksteps.mal", 69, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 69, 10), "x"))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("attacksteps.mal", 70, 7),
                                    true,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("attacksteps.mal", 70, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 70, 10), "y")))))),
                        new AST.AttackStep(
                            new Position("attacksteps.mal", 71, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position("attacksteps.mal", 71, 7), "d"),
                            Optional.of(Arrays.asList(AST.CIA.C)),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("attacksteps.mal", 71, 14),
                                    new AST.ID(new Position("attacksteps.mal", 71, 14), "t"),
                                    new ArrayList<Double>())),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position("attacksteps.mal", 72, 7),
                                    AST.MetaType.INFO,
                                    "Info")),
                            Optional.of(
                                new AST.Requires(
                                    new Position("attacksteps.mal", 73, 7),
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("attacksteps.mal", 73, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 73, 10), "x"))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("attacksteps.mal", 74, 7),
                                    false,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("attacksteps.mal", 74, 10),
                                            new AST.ID(
                                                new Position("attacksteps.mal", 74, 10), "y"))))))),
                    new ArrayList<AST.Variable>()))),
        categories.get(0));
    assertEquals(0, ast.getAssociations().size());
    assertEquals(0, ast.getDefines().size());
  }

  @Test
  public void testToString() {
    var ast = assertGetASTClassPath("parser/to-string.mal");
    String ans = assertReadFileClassPath("parser/to-string.ans");
    assertEquals(ans, ast.toString());
  }
}

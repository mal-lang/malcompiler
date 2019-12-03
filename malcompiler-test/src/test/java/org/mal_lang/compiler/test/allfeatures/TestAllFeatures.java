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
package org.mal_lang.compiler.test.allfeatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mal_lang.compiler.test.lib.AssertAST.assertAnalyzeClassPath;
import static org.mal_lang.compiler.test.lib.AssertAST.assertAssociation;
import static org.mal_lang.compiler.test.lib.AssertAST.assertCategory;
import static org.mal_lang.compiler.test.lib.AssertAST.assertDefine;
import static org.mal_lang.compiler.test.lib.AssertAST.assertGetASTClassPath;
import static org.mal_lang.compiler.test.lib.AssertLang.assertGetLangAsset;
import static org.mal_lang.compiler.test.lib.AssertLang.assertGetLangAttackStep;
import static org.mal_lang.compiler.test.lib.AssertLang.assertGetLangClassPath;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangCIA;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangCategory;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangDefines;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangField;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangLink;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangStepExpr;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangTTC;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangTags;
import static org.mal_lang.compiler.test.lib.AssertToken.assertTokens;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.lib.AST;
import org.mal_lang.compiler.lib.AST.ID;
import org.mal_lang.compiler.lib.Distributions;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.lib.Position;
import org.mal_lang.compiler.lib.Token;
import org.mal_lang.compiler.lib.TokenType;
import org.mal_lang.compiler.test.MalTest;

public class TestAllFeatures extends MalTest {
  private static final String ALL_FEATURES_MAL = "all-features.mal";
  private static final String CORE_MAL = "core.mal";
  private static final String SUBINCLUDED_MAL = "subincluded.mal";
  private static final String INCLUDED_MAL = "included.mal";
  private static final String SUBDIR_SUBINCLUDED_MAL =
      String.format("subdir%ssubincluded.mal", fileSep);

  @Test
  public void testLexerAllFeatures() {
    Token[] tokens = {
      new Token(TokenType.HASH, ALL_FEATURES_MAL, 6, 1),
      new Token(TokenType.ID, ALL_FEATURES_MAL, 6, 2, "id"),
      new Token(TokenType.COLON, ALL_FEATURES_MAL, 6, 4),
      new Token(TokenType.STRING, ALL_FEATURES_MAL, 6, 6, "all-features"),
      new Token(TokenType.HASH, ALL_FEATURES_MAL, 7, 1),
      new Token(TokenType.ID, ALL_FEATURES_MAL, 7, 2, "version"),
      new Token(TokenType.COLON, ALL_FEATURES_MAL, 7, 9),
      new Token(TokenType.STRING, ALL_FEATURES_MAL, 7, 11, "0.0.1"),
      new Token(TokenType.INCLUDE, ALL_FEATURES_MAL, 9, 1),
      new Token(TokenType.STRING, ALL_FEATURES_MAL, 9, 9, "core.mal"),
      new Token(TokenType.INCLUDE, ALL_FEATURES_MAL, 10, 1),
      new Token(TokenType.STRING, ALL_FEATURES_MAL, 10, 9, "subdir/subincluded.mal"),
      new Token(TokenType.EOF, ALL_FEATURES_MAL, 11, 1)
    };
    assertTokens(tokens, "all-features/all-features.mal");
  }

  @Test
  public void testLexerCore() {
    Token[] tokens = {
      new Token(TokenType.CATEGORY, CORE_MAL, 1, 1),
      new Token(TokenType.ID, CORE_MAL, 1, 10, "C1"),
      new Token(TokenType.ID, CORE_MAL, 2, 3, "user"),
      new Token(TokenType.INFO, CORE_MAL, 2, 8),
      new Token(TokenType.COLON, CORE_MAL, 2, 12),
      new Token(TokenType.STRING, CORE_MAL, 2, 14, "This is C1"),
      new Token(TokenType.ID, CORE_MAL, 3, 3, "modeler"),
      new Token(TokenType.INFO, CORE_MAL, 3, 11),
      new Token(TokenType.COLON, CORE_MAL, 3, 15),
      new Token(TokenType.STRING, CORE_MAL, 3, 17, "None for C1"),
      new Token(TokenType.ID, CORE_MAL, 4, 3, "developer"),
      new Token(TokenType.INFO, CORE_MAL, 4, 13),
      new Token(TokenType.COLON, CORE_MAL, 4, 17),
      new Token(TokenType.STRING, CORE_MAL, 4, 19, "Reasoning for C1"),
      new Token(TokenType.LCURLY, CORE_MAL, 5, 1),
      new Token(TokenType.RCURLY, CORE_MAL, 7, 1),
      new Token(TokenType.CATEGORY, CORE_MAL, 9, 1),
      new Token(TokenType.ID, CORE_MAL, 9, 10, "C1"),
      new Token(TokenType.LCURLY, CORE_MAL, 9, 13),
      new Token(TokenType.ASSET, CORE_MAL, 10, 3),
      new Token(TokenType.ID, CORE_MAL, 10, 9, "A1"),
      new Token(TokenType.LCURLY, CORE_MAL, 11, 3),
      new Token(TokenType.ANY, CORE_MAL, 12, 5),
      new Token(TokenType.ID, CORE_MAL, 12, 7, "a1Attack1"),
      new Token(TokenType.LCURLY, CORE_MAL, 12, 17),
      new Token(TokenType.C, CORE_MAL, 12, 18),
      new Token(TokenType.RCURLY, CORE_MAL, 12, 19),
      new Token(TokenType.ID, CORE_MAL, 13, 7, "user"),
      new Token(TokenType.INFO, CORE_MAL, 13, 12),
      new Token(TokenType.COLON, CORE_MAL, 13, 16),
      new Token(TokenType.STRING, CORE_MAL, 13, 18, "This is a1Attack1"),
      new Token(TokenType.ID, CORE_MAL, 14, 7, "modeler"),
      new Token(TokenType.INFO, CORE_MAL, 14, 15),
      new Token(TokenType.COLON, CORE_MAL, 14, 19),
      new Token(TokenType.STRING, CORE_MAL, 14, 21, "None for a1Attack1"),
      new Token(TokenType.ID, CORE_MAL, 15, 7, "developer"),
      new Token(TokenType.INFO, CORE_MAL, 15, 17),
      new Token(TokenType.COLON, CORE_MAL, 15, 21),
      new Token(TokenType.STRING, CORE_MAL, 15, 23, "Reasoning for a1Attack1"),
      new Token(TokenType.ALL, CORE_MAL, 17, 5),
      new Token(TokenType.ID, CORE_MAL, 17, 7, "a1Attack2"),
      new Token(TokenType.LCURLY, CORE_MAL, 17, 17),
      new Token(TokenType.I, CORE_MAL, 17, 18),
      new Token(TokenType.COMMA, CORE_MAL, 17, 19),
      new Token(TokenType.C, CORE_MAL, 17, 21),
      new Token(TokenType.RCURLY, CORE_MAL, 17, 22),
      new Token(TokenType.LBRACKET, CORE_MAL, 17, 24),
      new Token(TokenType.RBRACKET, CORE_MAL, 17, 25),
      new Token(TokenType.ID, CORE_MAL, 18, 7, "user"),
      new Token(TokenType.INFO, CORE_MAL, 18, 12),
      new Token(TokenType.COLON, CORE_MAL, 18, 16),
      new Token(TokenType.STRING, CORE_MAL, 18, 18, "This is a1Attack2"),
      new Token(TokenType.OVERRIDE, CORE_MAL, 19, 7),
      new Token(TokenType.ID, CORE_MAL, 19, 10, "a1Sub"),
      new Token(TokenType.LBRACKET, CORE_MAL, 19, 15),
      new Token(TokenType.ID, CORE_MAL, 19, 16, "A1"),
      new Token(TokenType.RBRACKET, CORE_MAL, 19, 18),
      new Token(TokenType.STAR, CORE_MAL, 19, 19),
      new Token(TokenType.DOT, CORE_MAL, 19, 20),
      new Token(TokenType.ID, CORE_MAL, 19, 21, "a1Attack1"),
      new Token(TokenType.HASH, CORE_MAL, 20, 5),
      new Token(TokenType.ID, CORE_MAL, 20, 7, "a1Defense1"),
      new Token(TokenType.LBRACKET, CORE_MAL, 20, 18),
      new Token(TokenType.ID, CORE_MAL, 20, 19, "Bernoulli"),
      new Token(TokenType.LPAREN, CORE_MAL, 20, 28),
      new Token(TokenType.FLOAT, CORE_MAL, 20, 29, 0.5),
      new Token(TokenType.RPAREN, CORE_MAL, 20, 32),
      new Token(TokenType.RBRACKET, CORE_MAL, 20, 33),
      new Token(TokenType.ID, CORE_MAL, 21, 7, "developer"),
      new Token(TokenType.INFO, CORE_MAL, 21, 17),
      new Token(TokenType.COLON, CORE_MAL, 21, 21),
      new Token(TokenType.STRING, CORE_MAL, 21, 23, "Reasoning for a1Defense"),
      new Token(TokenType.HASH, CORE_MAL, 23, 5),
      new Token(TokenType.ID, CORE_MAL, 23, 7, "a1Defense2"),
      new Token(TokenType.LBRACKET, CORE_MAL, 23, 18),
      new Token(TokenType.ID, CORE_MAL, 23, 19, "Disabled"),
      new Token(TokenType.RBRACKET, CORE_MAL, 23, 27),
      new Token(TokenType.OVERRIDE, CORE_MAL, 24, 7),
      new Token(TokenType.ID, CORE_MAL, 24, 10, "a1Attack2"),
      new Token(TokenType.EXIST, CORE_MAL, 25, 5),
      new Token(TokenType.ID, CORE_MAL, 25, 7, "a1Exist1"),
      new Token(TokenType.REQUIRE, CORE_MAL, 26, 7),
      new Token(TokenType.ID, CORE_MAL, 26, 10, "a1Sub"),
      new Token(TokenType.EXIST, CORE_MAL, 28, 5),
      new Token(TokenType.ID, CORE_MAL, 28, 7, "a1Exist2"),
      new Token(TokenType.ID, CORE_MAL, 29, 7, "modeler"),
      new Token(TokenType.INFO, CORE_MAL, 29, 15),
      new Token(TokenType.COLON, CORE_MAL, 29, 19),
      new Token(TokenType.STRING, CORE_MAL, 29, 21, "None for a1Exist2"),
      new Token(TokenType.REQUIRE, CORE_MAL, 30, 7),
      new Token(TokenType.ID, CORE_MAL, 30, 10, "a7"),
      new Token(TokenType.COMMA, CORE_MAL, 30, 12),
      new Token(TokenType.ID, CORE_MAL, 30, 14, "a1Super"),
      new Token(TokenType.DOT, CORE_MAL, 30, 21),
      new Token(TokenType.ID, CORE_MAL, 30, 22, "a1Super"),
      new Token(TokenType.DOT, CORE_MAL, 30, 29),
      new Token(TokenType.ID, CORE_MAL, 30, 30, "a7"),
      new Token(TokenType.OVERRIDE, CORE_MAL, 31, 7),
      new Token(TokenType.LPAREN, CORE_MAL, 31, 10),
      new Token(TokenType.ID, CORE_MAL, 31, 11, "a1Super"),
      new Token(TokenType.STAR, CORE_MAL, 31, 18),
      new Token(TokenType.RPAREN, CORE_MAL, 31, 19),
      new Token(TokenType.LBRACKET, CORE_MAL, 31, 20),
      new Token(TokenType.ID, CORE_MAL, 31, 21, "A2"),
      new Token(TokenType.RBRACKET, CORE_MAL, 31, 23),
      new Token(TokenType.DOT, CORE_MAL, 31, 24),
      new Token(TokenType.LPAREN, CORE_MAL, 31, 25),
      new Token(TokenType.ID, CORE_MAL, 31, 26, "a8"),
      new Token(TokenType.DOT, CORE_MAL, 31, 28),
      new Token(TokenType.ID, CORE_MAL, 31, 29, "destroy"),
      new Token(TokenType.RPAREN, CORE_MAL, 31, 36),
      new Token(TokenType.NOTEXIST, CORE_MAL, 32, 5),
      new Token(TokenType.ID, CORE_MAL, 32, 8, "a1NotExist1"),
      new Token(TokenType.REQUIRE, CORE_MAL, 33, 7),
      new Token(TokenType.ID, CORE_MAL, 33, 10, "a8"),
      new Token(TokenType.DOT, CORE_MAL, 33, 12),
      new Token(TokenType.ID, CORE_MAL, 33, 13, "a8Sub"),
      new Token(TokenType.STAR, CORE_MAL, 33, 18),
      new Token(TokenType.DOT, CORE_MAL, 33, 19),
      new Token(TokenType.ID, CORE_MAL, 33, 20, "a1"),
      new Token(TokenType.LBRACKET, CORE_MAL, 33, 22),
      new Token(TokenType.ID, CORE_MAL, 33, 23, "A7"),
      new Token(TokenType.RBRACKET, CORE_MAL, 33, 25),
      new Token(TokenType.NOTEXIST, CORE_MAL, 35, 5),
      new Token(TokenType.ID, CORE_MAL, 35, 8, "a1NotExist2"),
      new Token(TokenType.REQUIRE, CORE_MAL, 36, 7),
      new Token(TokenType.ID, CORE_MAL, 36, 10, "a8"),
      new Token(TokenType.DOT, CORE_MAL, 36, 12),
      new Token(TokenType.ID, CORE_MAL, 36, 13, "a4"),
      new Token(TokenType.INTERSECT, CORE_MAL, 36, 16),
      new Token(TokenType.ID, CORE_MAL, 36, 19, "a4"),
      new Token(TokenType.OVERRIDE, CORE_MAL, 37, 7),
      new Token(TokenType.ID, CORE_MAL, 37, 10, "a4"),
      new Token(TokenType.DOT, CORE_MAL, 37, 12),
      new Token(TokenType.ID, CORE_MAL, 37, 13, "a"),
      new Token(TokenType.RCURLY, CORE_MAL, 38, 3),
      new Token(TokenType.ASSET, CORE_MAL, 40, 3),
      new Token(TokenType.ID, CORE_MAL, 40, 9, "A2"),
      new Token(TokenType.EXTENDS, CORE_MAL, 40, 12),
      new Token(TokenType.ID, CORE_MAL, 40, 20, "A1"),
      new Token(TokenType.LCURLY, CORE_MAL, 40, 23),
      new Token(TokenType.ANY, CORE_MAL, 42, 5),
      new Token(TokenType.ID, CORE_MAL, 42, 7, "a1Attack1"),
      new Token(TokenType.LCURLY, CORE_MAL, 42, 17),
      new Token(TokenType.RCURLY, CORE_MAL, 42, 18),
      new Token(TokenType.OVERRIDE, CORE_MAL, 43, 7),
      new Token(TokenType.ID, CORE_MAL, 43, 10, "a1Super"),
      new Token(TokenType.DOT, CORE_MAL, 43, 17),
      new Token(TokenType.ID, CORE_MAL, 43, 18, "a1Attack1"),
      new Token(TokenType.ALL, CORE_MAL, 45, 5),
      new Token(TokenType.ID, CORE_MAL, 45, 7, "a1Attack2"),
      new Token(TokenType.LCURLY, CORE_MAL, 45, 17),
      new Token(TokenType.C, CORE_MAL, 45, 18),
      new Token(TokenType.COMMA, CORE_MAL, 45, 19),
      new Token(TokenType.I, CORE_MAL, 45, 21),
      new Token(TokenType.COMMA, CORE_MAL, 45, 22),
      new Token(TokenType.A, CORE_MAL, 45, 24),
      new Token(TokenType.RCURLY, CORE_MAL, 45, 25),
      new Token(TokenType.INHERIT, CORE_MAL, 46, 7),
      new Token(TokenType.ID, CORE_MAL, 46, 10, "a1Super"),
      new Token(TokenType.DOT, CORE_MAL, 46, 17),
      new Token(TokenType.ID, CORE_MAL, 46, 18, "a1Attack1"),
      new Token(TokenType.HASH, CORE_MAL, 48, 5),
      new Token(TokenType.ID, CORE_MAL, 48, 7, "a1Defense1"),
      new Token(TokenType.LBRACKET, CORE_MAL, 48, 18),
      new Token(TokenType.ID, CORE_MAL, 48, 19, "Enabled"),
      new Token(TokenType.RBRACKET, CORE_MAL, 48, 26),
      new Token(TokenType.OVERRIDE, CORE_MAL, 49, 7),
      new Token(TokenType.ID, CORE_MAL, 49, 10, "a1Sub"),
      new Token(TokenType.DOT, CORE_MAL, 49, 15),
      new Token(TokenType.ID, CORE_MAL, 49, 16, "a1Sub"),
      new Token(TokenType.DOT, CORE_MAL, 49, 21),
      new Token(TokenType.ID, CORE_MAL, 49, 22, "a1Attack2"),
      new Token(TokenType.HASH, CORE_MAL, 51, 5),
      new Token(TokenType.ID, CORE_MAL, 51, 7, "a1Defense2"),
      new Token(TokenType.INHERIT, CORE_MAL, 52, 7),
      new Token(TokenType.ID, CORE_MAL, 52, 10, "a1Attack2"),
      new Token(TokenType.EXIST, CORE_MAL, 54, 5),
      new Token(TokenType.ID, CORE_MAL, 54, 7, "a1Exist2"),
      new Token(TokenType.REQUIRE, CORE_MAL, 55, 7),
      new Token(TokenType.ID, CORE_MAL, 55, 10, "a8"),
      new Token(TokenType.OVERRIDE, CORE_MAL, 56, 7),
      new Token(TokenType.ID, CORE_MAL, 56, 10, "a1Defense2"),
      new Token(TokenType.NOTEXIST, CORE_MAL, 58, 5),
      new Token(TokenType.ID, CORE_MAL, 58, 8, "a1NotExist1"),
      new Token(TokenType.REQUIRE, CORE_MAL, 59, 8),
      new Token(TokenType.ID, CORE_MAL, 59, 11, "a8"),
      new Token(TokenType.INHERIT, CORE_MAL, 60, 8),
      new Token(TokenType.ID, CORE_MAL, 60, 11, "a1Defense1"),
      new Token(TokenType.RCURLY, CORE_MAL, 61, 3),
      new Token(TokenType.ABSTRACT, CORE_MAL, 63, 3),
      new Token(TokenType.ASSET, CORE_MAL, 63, 12),
      new Token(TokenType.ID, CORE_MAL, 63, 18, "A3"),
      new Token(TokenType.EXTENDS, CORE_MAL, 63, 21),
      new Token(TokenType.ID, CORE_MAL, 63, 29, "A1"),
      new Token(TokenType.ID, CORE_MAL, 64, 5, "user"),
      new Token(TokenType.INFO, CORE_MAL, 64, 10),
      new Token(TokenType.COLON, CORE_MAL, 64, 14),
      new Token(TokenType.STRING, CORE_MAL, 64, 16, "This is A3"),
      new Token(TokenType.LCURLY, CORE_MAL, 65, 3),
      new Token(TokenType.LET, CORE_MAL, 66, 5),
      new Token(TokenType.ID, CORE_MAL, 66, 9, "unused"),
      new Token(TokenType.ASSIGN, CORE_MAL, 66, 16),
      new Token(TokenType.ID, CORE_MAL, 66, 18, "a1Sub"),
      new Token(TokenType.ANY, CORE_MAL, 67, 5),
      new Token(TokenType.ID, CORE_MAL, 67, 7, "a3Attack"),
      new Token(TokenType.LCURLY, CORE_MAL, 67, 16),
      new Token(TokenType.A, CORE_MAL, 67, 17),
      new Token(TokenType.COMMA, CORE_MAL, 67, 18),
      new Token(TokenType.I, CORE_MAL, 67, 20),
      new Token(TokenType.COMMA, CORE_MAL, 67, 21),
      new Token(TokenType.C, CORE_MAL, 67, 23),
      new Token(TokenType.RCURLY, CORE_MAL, 67, 24),
      new Token(TokenType.LET, CORE_MAL, 68, 5),
      new Token(TokenType.ID, CORE_MAL, 68, 9, "used"),
      new Token(TokenType.ASSIGN, CORE_MAL, 68, 14),
      new Token(TokenType.ID, CORE_MAL, 68, 16, "a1Sub"),
      new Token(TokenType.LBRACKET, CORE_MAL, 68, 21),
      new Token(TokenType.ID, CORE_MAL, 68, 22, "A2"),
      new Token(TokenType.RBRACKET, CORE_MAL, 68, 24),
      new Token(TokenType.LET, CORE_MAL, 70, 5),
      new Token(TokenType.ID, CORE_MAL, 70, 9, "V1"),
      new Token(TokenType.ASSIGN, CORE_MAL, 70, 12),
      new Token(TokenType.ID, CORE_MAL, 70, 14, "used"),
      new Token(TokenType.LPAREN, CORE_MAL, 70, 18),
      new Token(TokenType.RPAREN, CORE_MAL, 70, 19),
      new Token(TokenType.ALL, CORE_MAL, 71, 5),
      new Token(TokenType.ID, CORE_MAL, 71, 7, "AT"),
      new Token(TokenType.OVERRIDE, CORE_MAL, 72, 7),
      new Token(TokenType.ID, CORE_MAL, 72, 10, "V1"),
      new Token(TokenType.LPAREN, CORE_MAL, 72, 12),
      new Token(TokenType.RPAREN, CORE_MAL, 72, 13),
      new Token(TokenType.DOT, CORE_MAL, 72, 14),
      new Token(TokenType.ID, CORE_MAL, 72, 15, "a1Attack1"),
      new Token(TokenType.RCURLY, CORE_MAL, 73, 3),
      new Token(TokenType.RCURLY, CORE_MAL, 74, 1),
      new Token(TokenType.CATEGORY, CORE_MAL, 76, 1),
      new Token(TokenType.ID, CORE_MAL, 76, 10, "C2"),
      new Token(TokenType.LCURLY, CORE_MAL, 76, 13),
      new Token(TokenType.RCURLY, CORE_MAL, 76, 44),
      new Token(TokenType.CATEGORY, CORE_MAL, 78, 1),
      new Token(TokenType.ID, CORE_MAL, 78, 10, "C2"),
      new Token(TokenType.ID, CORE_MAL, 79, 3, "modeler"),
      new Token(TokenType.INFO, CORE_MAL, 79, 11),
      new Token(TokenType.COLON, CORE_MAL, 79, 15),
      new Token(TokenType.STRING, CORE_MAL, 79, 17, "None for C2"),
      new Token(TokenType.LCURLY, CORE_MAL, 80, 1),
      new Token(TokenType.ABSTRACT, CORE_MAL, 81, 3),
      new Token(TokenType.ASSET, CORE_MAL, 81, 12),
      new Token(TokenType.ID, CORE_MAL, 81, 18, "A4"),
      new Token(TokenType.ID, CORE_MAL, 82, 5, "modeler"),
      new Token(TokenType.INFO, CORE_MAL, 82, 13),
      new Token(TokenType.COLON, CORE_MAL, 82, 17),
      new Token(TokenType.STRING, CORE_MAL, 82, 19, "None for A4"),
      new Token(TokenType.ID, CORE_MAL, 83, 5, "developer"),
      new Token(TokenType.INFO, CORE_MAL, 83, 15),
      new Token(TokenType.COLON, CORE_MAL, 83, 19),
      new Token(TokenType.STRING, CORE_MAL, 83, 21, "Reasoning for A4"),
      new Token(TokenType.ID, CORE_MAL, 84, 5, "user"),
      new Token(TokenType.INFO, CORE_MAL, 84, 10),
      new Token(TokenType.COLON, CORE_MAL, 84, 14),
      new Token(TokenType.STRING, CORE_MAL, 84, 16, "This is A4"),
      new Token(TokenType.LCURLY, CORE_MAL, 85, 3),
      new Token(TokenType.LET, CORE_MAL, 86, 5),
      new Token(TokenType.ID, CORE_MAL, 86, 9, "var"),
      new Token(TokenType.ASSIGN, CORE_MAL, 86, 13),
      new Token(TokenType.ID, CORE_MAL, 86, 15, "a1"),
      new Token(TokenType.DOT, CORE_MAL, 86, 17),
      new Token(TokenType.ID, CORE_MAL, 86, 18, "a1Sub"),
      new Token(TokenType.STAR, CORE_MAL, 86, 23),
      new Token(TokenType.ANY, CORE_MAL, 87, 5),
      new Token(TokenType.ID, CORE_MAL, 87, 7, "a"),
      new Token(TokenType.OVERRIDE, CORE_MAL, 88, 7),
      new Token(TokenType.LPAREN, CORE_MAL, 88, 10),
      new Token(TokenType.ID, CORE_MAL, 88, 11, "var"),
      new Token(TokenType.LPAREN, CORE_MAL, 88, 14),
      new Token(TokenType.RPAREN, CORE_MAL, 88, 15),
      new Token(TokenType.UNION, CORE_MAL, 88, 17),
      new Token(TokenType.ID, CORE_MAL, 88, 20, "var2"),
      new Token(TokenType.LPAREN, CORE_MAL, 88, 24),
      new Token(TokenType.RPAREN, CORE_MAL, 88, 25),
      new Token(TokenType.RPAREN, CORE_MAL, 88, 26),
      new Token(TokenType.DOT, CORE_MAL, 88, 27),
      new Token(TokenType.ID, CORE_MAL, 88, 28, "a1Attack1"),
      new Token(TokenType.LET, CORE_MAL, 89, 5),
      new Token(TokenType.ID, CORE_MAL, 89, 9, "var2"),
      new Token(TokenType.ASSIGN, CORE_MAL, 89, 14),
      new Token(TokenType.ID, CORE_MAL, 89, 16, "var"),
      new Token(TokenType.LPAREN, CORE_MAL, 89, 19),
      new Token(TokenType.RPAREN, CORE_MAL, 89, 20),
      new Token(TokenType.LBRACKET, CORE_MAL, 89, 21),
      new Token(TokenType.ID, CORE_MAL, 89, 22, "A3"),
      new Token(TokenType.RBRACKET, CORE_MAL, 89, 24),
      new Token(TokenType.RCURLY, CORE_MAL, 90, 3),
      new Token(TokenType.RCURLY, CORE_MAL, 91, 1),
      new Token(TokenType.CATEGORY, CORE_MAL, 93, 1),
      new Token(TokenType.ID, CORE_MAL, 93, 10, "C2"),
      new Token(TokenType.ID, CORE_MAL, 94, 3, "developer"),
      new Token(TokenType.INFO, CORE_MAL, 94, 13),
      new Token(TokenType.COLON, CORE_MAL, 94, 17),
      new Token(TokenType.STRING, CORE_MAL, 94, 19, "Reasoning for C2"),
      new Token(TokenType.LCURLY, CORE_MAL, 95, 1),
      new Token(TokenType.ASSET, CORE_MAL, 96, 3),
      new Token(TokenType.ID, CORE_MAL, 96, 9, "A5"),
      new Token(TokenType.EXTENDS, CORE_MAL, 96, 12),
      new Token(TokenType.ID, CORE_MAL, 96, 20, "A4"),
      new Token(TokenType.ID, CORE_MAL, 97, 5, "modeler"),
      new Token(TokenType.INFO, CORE_MAL, 97, 13),
      new Token(TokenType.COLON, CORE_MAL, 97, 17),
      new Token(TokenType.STRING, CORE_MAL, 97, 19, "None for A5"),
      new Token(TokenType.LCURLY, CORE_MAL, 98, 3),
      new Token(TokenType.RCURLY, CORE_MAL, 98, 31),
      new Token(TokenType.ABSTRACT, CORE_MAL, 100, 3),
      new Token(TokenType.ASSET, CORE_MAL, 100, 12),
      new Token(TokenType.ID, CORE_MAL, 100, 18, "A6"),
      new Token(TokenType.EXTENDS, CORE_MAL, 100, 21),
      new Token(TokenType.ID, CORE_MAL, 100, 29, "A4"),
      new Token(TokenType.ID, CORE_MAL, 101, 5, "developer"),
      new Token(TokenType.INFO, CORE_MAL, 101, 15),
      new Token(TokenType.COLON, CORE_MAL, 101, 19),
      new Token(TokenType.STRING, CORE_MAL, 101, 21, "Reasoning for A6"),
      new Token(TokenType.LCURLY, CORE_MAL, 102, 3),
      new Token(TokenType.RCURLY, CORE_MAL, 104, 3),
      new Token(TokenType.RCURLY, CORE_MAL, 105, 1),
      new Token(TokenType.CATEGORY, CORE_MAL, 107, 1),
      new Token(TokenType.ID, CORE_MAL, 107, 10, "C3"),
      new Token(TokenType.LCURLY, CORE_MAL, 107, 13),
      new Token(TokenType.ASSET, CORE_MAL, 108, 3),
      new Token(TokenType.ID, CORE_MAL, 108, 9, "A7"),
      new Token(TokenType.EXTENDS, CORE_MAL, 108, 12),
      new Token(TokenType.ID, CORE_MAL, 108, 20, "A3"),
      new Token(TokenType.LCURLY, CORE_MAL, 108, 23),
      new Token(TokenType.ANY, CORE_MAL, 109, 5),
      new Token(TokenType.ID, CORE_MAL, 109, 7, "a7Attack"),
      new Token(TokenType.OVERRIDE, CORE_MAL, 110, 7),
      new Token(TokenType.ID, CORE_MAL, 110, 10, "a1"),
      new Token(TokenType.LPAREN, CORE_MAL, 110, 12),
      new Token(TokenType.RPAREN, CORE_MAL, 110, 13),
      new Token(TokenType.DOT, CORE_MAL, 110, 14),
      new Token(TokenType.ID, CORE_MAL, 110, 15, "destroy"),
      new Token(TokenType.LET, CORE_MAL, 111, 5),
      new Token(TokenType.ID, CORE_MAL, 111, 9, "a6"),
      new Token(TokenType.ASSIGN, CORE_MAL, 111, 12),
      new Token(TokenType.ID, CORE_MAL, 111, 14, "a1Super"),
      new Token(TokenType.DOT, CORE_MAL, 111, 21),
      new Token(TokenType.ID, CORE_MAL, 111, 22, "a8"),
      new Token(TokenType.LET, CORE_MAL, 112, 5),
      new Token(TokenType.ID, CORE_MAL, 112, 9, "a1"),
      new Token(TokenType.ASSIGN, CORE_MAL, 112, 12),
      new Token(TokenType.ID, CORE_MAL, 112, 14, "a6"),
      new Token(TokenType.LPAREN, CORE_MAL, 112, 16),
      new Token(TokenType.RPAREN, CORE_MAL, 112, 17),
      new Token(TokenType.DOT, CORE_MAL, 112, 18),
      new Token(TokenType.ID, CORE_MAL, 112, 19, "a8Super"),
      new Token(TokenType.RCURLY, CORE_MAL, 113, 3),
      new Token(TokenType.ASSET, CORE_MAL, 115, 3),
      new Token(TokenType.ID, CORE_MAL, 115, 9, "A8"),
      new Token(TokenType.LCURLY, CORE_MAL, 115, 12),
      new Token(TokenType.ALL, CORE_MAL, 116, 5),
      new Token(TokenType.ID, CORE_MAL, 116, 7, "destroy"),
      new Token(TokenType.LCURLY, CORE_MAL, 116, 15),
      new Token(TokenType.C, CORE_MAL, 116, 16),
      new Token(TokenType.COMMA, CORE_MAL, 116, 17),
      new Token(TokenType.I, CORE_MAL, 116, 19),
      new Token(TokenType.COMMA, CORE_MAL, 116, 20),
      new Token(TokenType.A, CORE_MAL, 116, 22),
      new Token(TokenType.RCURLY, CORE_MAL, 116, 23),
      new Token(TokenType.LBRACKET, CORE_MAL, 116, 25),
      new Token(TokenType.ID, CORE_MAL, 116, 26, "Exponential"),
      new Token(TokenType.LPAREN, CORE_MAL, 116, 37),
      new Token(TokenType.INT, CORE_MAL, 116, 38, 5),
      new Token(TokenType.RPAREN, CORE_MAL, 116, 39),
      new Token(TokenType.RBRACKET, CORE_MAL, 116, 40),
      new Token(TokenType.RCURLY, CORE_MAL, 117, 3),
      new Token(TokenType.ASSET, CORE_MAL, 119, 3),
      new Token(TokenType.ID, CORE_MAL, 119, 9, "A9"),
      new Token(TokenType.LCURLY, CORE_MAL, 119, 12),
      new Token(TokenType.RCURLY, CORE_MAL, 122, 3),
      new Token(TokenType.RCURLY, CORE_MAL, 123, 1),
      new Token(TokenType.ASSOCIATIONS, CORE_MAL, 125, 1),
      new Token(TokenType.LCURLY, CORE_MAL, 125, 14),
      new Token(TokenType.RCURLY, CORE_MAL, 125, 43),
      new Token(TokenType.ASSOCIATIONS, CORE_MAL, 127, 1),
      new Token(TokenType.LCURLY, CORE_MAL, 127, 14),
      new Token(TokenType.ID, CORE_MAL, 129, 3, "A1"),
      new Token(TokenType.LBRACKET, CORE_MAL, 129, 6),
      new Token(TokenType.ID, CORE_MAL, 129, 7, "a1"),
      new Token(TokenType.RBRACKET, CORE_MAL, 129, 9),
      new Token(TokenType.INT, CORE_MAL, 129, 16, 1),
      new Token(TokenType.LARROW, CORE_MAL, 129, 21),
      new Token(TokenType.ID, CORE_MAL, 129, 25, "L1"),
      new Token(TokenType.RARROW, CORE_MAL, 129, 28),
      new Token(TokenType.INT, CORE_MAL, 129, 32, 1),
      new Token(TokenType.RANGE, CORE_MAL, 129, 33),
      new Token(TokenType.STAR, CORE_MAL, 129, 35),
      new Token(TokenType.LBRACKET, CORE_MAL, 129, 37),
      new Token(TokenType.ID, CORE_MAL, 129, 38, "a4"),
      new Token(TokenType.RBRACKET, CORE_MAL, 129, 40),
      new Token(TokenType.ID, CORE_MAL, 129, 45, "A4"),
      new Token(TokenType.ID, CORE_MAL, 130, 3, "A5"),
      new Token(TokenType.LBRACKET, CORE_MAL, 130, 6),
      new Token(TokenType.ID, CORE_MAL, 130, 7, "a5"),
      new Token(TokenType.RBRACKET, CORE_MAL, 130, 9),
      new Token(TokenType.INT, CORE_MAL, 130, 16, 1),
      new Token(TokenType.RANGE, CORE_MAL, 130, 17),
      new Token(TokenType.INT, CORE_MAL, 130, 19, 1),
      new Token(TokenType.LARROW, CORE_MAL, 130, 21),
      new Token(TokenType.ID, CORE_MAL, 130, 25, "L2"),
      new Token(TokenType.RARROW, CORE_MAL, 130, 28),
      new Token(TokenType.INT, CORE_MAL, 130, 32, 0),
      new Token(TokenType.RANGE, CORE_MAL, 130, 33),
      new Token(TokenType.STAR, CORE_MAL, 130, 35),
      new Token(TokenType.LBRACKET, CORE_MAL, 130, 37),
      new Token(TokenType.ID, CORE_MAL, 130, 38, "a6"),
      new Token(TokenType.RBRACKET, CORE_MAL, 130, 40),
      new Token(TokenType.ID, CORE_MAL, 130, 45, "A6"),
      new Token(TokenType.RCURLY, CORE_MAL, 131, 1),
      new Token(TokenType.ASSOCIATIONS, CORE_MAL, 133, 1),
      new Token(TokenType.LCURLY, CORE_MAL, 133, 14),
      new Token(TokenType.ID, CORE_MAL, 135, 3, "A1"),
      new Token(TokenType.LBRACKET, CORE_MAL, 135, 6),
      new Token(TokenType.ID, CORE_MAL, 135, 7, "a1Super"),
      new Token(TokenType.RBRACKET, CORE_MAL, 135, 14),
      new Token(TokenType.INT, CORE_MAL, 135, 16, 0),
      new Token(TokenType.RANGE, CORE_MAL, 135, 17),
      new Token(TokenType.INT, CORE_MAL, 135, 19, 1),
      new Token(TokenType.LARROW, CORE_MAL, 135, 21),
      new Token(TokenType.ID, CORE_MAL, 135, 25, "L3"),
      new Token(TokenType.RARROW, CORE_MAL, 135, 28),
      new Token(TokenType.STAR, CORE_MAL, 135, 32),
      new Token(TokenType.LBRACKET, CORE_MAL, 135, 37),
      new Token(TokenType.ID, CORE_MAL, 135, 38, "a1Sub"),
      new Token(TokenType.RBRACKET, CORE_MAL, 135, 43),
      new Token(TokenType.ID, CORE_MAL, 135, 47, "A1"),
      new Token(TokenType.ID, CORE_MAL, 136, 3, "A3"),
      new Token(TokenType.LBRACKET, CORE_MAL, 136, 6),
      new Token(TokenType.ID, CORE_MAL, 136, 7, "a3"),
      new Token(TokenType.RBRACKET, CORE_MAL, 136, 9),
      new Token(TokenType.STAR, CORE_MAL, 136, 16),
      new Token(TokenType.LARROW, CORE_MAL, 136, 21),
      new Token(TokenType.ID, CORE_MAL, 136, 25, "L3"),
      new Token(TokenType.RARROW, CORE_MAL, 136, 28),
      new Token(TokenType.STAR, CORE_MAL, 136, 32),
      new Token(TokenType.LBRACKET, CORE_MAL, 136, 37),
      new Token(TokenType.ID, CORE_MAL, 136, 38, "a6"),
      new Token(TokenType.RBRACKET, CORE_MAL, 136, 40),
      new Token(TokenType.ID, CORE_MAL, 136, 47, "A6"),
      new Token(TokenType.ID, CORE_MAL, 137, 3, "A7"),
      new Token(TokenType.LBRACKET, CORE_MAL, 137, 6),
      new Token(TokenType.ID, CORE_MAL, 137, 7, "a7"),
      new Token(TokenType.RBRACKET, CORE_MAL, 137, 9),
      new Token(TokenType.INT, CORE_MAL, 137, 16, 0),
      new Token(TokenType.RANGE, CORE_MAL, 137, 17),
      new Token(TokenType.INT, CORE_MAL, 137, 19, 1),
      new Token(TokenType.LARROW, CORE_MAL, 137, 21),
      new Token(TokenType.ID, CORE_MAL, 137, 25, "L3"),
      new Token(TokenType.RARROW, CORE_MAL, 137, 28),
      new Token(TokenType.INT, CORE_MAL, 137, 32, 1),
      new Token(TokenType.LBRACKET, CORE_MAL, 137, 37),
      new Token(TokenType.ID, CORE_MAL, 137, 38, "a1"),
      new Token(TokenType.RBRACKET, CORE_MAL, 137, 40),
      new Token(TokenType.ID, CORE_MAL, 137, 47, "A1"),
      new Token(TokenType.ID, CORE_MAL, 138, 3, "A8"),
      new Token(TokenType.LBRACKET, CORE_MAL, 138, 6),
      new Token(TokenType.ID, CORE_MAL, 138, 7, "a8"),
      new Token(TokenType.RBRACKET, CORE_MAL, 138, 9),
      new Token(TokenType.STAR, CORE_MAL, 138, 16),
      new Token(TokenType.LARROW, CORE_MAL, 138, 21),
      new Token(TokenType.ID, CORE_MAL, 138, 25, "L4"),
      new Token(TokenType.RARROW, CORE_MAL, 138, 28),
      new Token(TokenType.STAR, CORE_MAL, 138, 32),
      new Token(TokenType.LBRACKET, CORE_MAL, 138, 37),
      new Token(TokenType.ID, CORE_MAL, 138, 38, "a1"),
      new Token(TokenType.RBRACKET, CORE_MAL, 138, 40),
      new Token(TokenType.ID, CORE_MAL, 138, 47, "A1"),
      new Token(TokenType.ID, CORE_MAL, 139, 3, "A8"),
      new Token(TokenType.LBRACKET, CORE_MAL, 139, 6),
      new Token(TokenType.ID, CORE_MAL, 139, 7, "a8"),
      new Token(TokenType.RBRACKET, CORE_MAL, 139, 9),
      new Token(TokenType.STAR, CORE_MAL, 139, 16),
      new Token(TokenType.LARROW, CORE_MAL, 139, 21),
      new Token(TokenType.ID, CORE_MAL, 139, 25, "L4"),
      new Token(TokenType.RARROW, CORE_MAL, 139, 28),
      new Token(TokenType.STAR, CORE_MAL, 139, 32),
      new Token(TokenType.LBRACKET, CORE_MAL, 139, 37),
      new Token(TokenType.ID, CORE_MAL, 139, 38, "a4"),
      new Token(TokenType.RBRACKET, CORE_MAL, 139, 40),
      new Token(TokenType.ID, CORE_MAL, 139, 47, "A4"),
      new Token(TokenType.ID, CORE_MAL, 140, 3, "A8"),
      new Token(TokenType.LBRACKET, CORE_MAL, 140, 6),
      new Token(TokenType.ID, CORE_MAL, 140, 7, "a8Sub"),
      new Token(TokenType.RBRACKET, CORE_MAL, 140, 12),
      new Token(TokenType.STAR, CORE_MAL, 140, 16),
      new Token(TokenType.LARROW, CORE_MAL, 140, 21),
      new Token(TokenType.ID, CORE_MAL, 140, 25, "L4"),
      new Token(TokenType.RARROW, CORE_MAL, 140, 28),
      new Token(TokenType.STAR, CORE_MAL, 140, 32),
      new Token(TokenType.LBRACKET, CORE_MAL, 140, 37),
      new Token(TokenType.ID, CORE_MAL, 140, 38, "a8Super"),
      new Token(TokenType.RBRACKET, CORE_MAL, 140, 45),
      new Token(TokenType.ID, CORE_MAL, 140, 47, "A8"),
      new Token(TokenType.RCURLY, CORE_MAL, 141, 1),
      new Token(TokenType.EOF, CORE_MAL, 142, 1)
    };
    assertTokens(tokens, "all-features/core.mal");
  }

  @Test
  public void testLexerSubIncluded() {
    Token[] tokens = {
      new Token(TokenType.HASH, SUBINCLUDED_MAL, 2, 2),
      new Token(TokenType.ID, SUBINCLUDED_MAL, 3, 4, "empty"),
      new Token(TokenType.COLON, SUBINCLUDED_MAL, 4, 10),
      new Token(TokenType.STRING, SUBINCLUDED_MAL, 5, 12, ""),
      new Token(TokenType.INCLUDE, SUBINCLUDED_MAL, 7, 2),
      new Token(TokenType.STRING, SUBINCLUDED_MAL, 8, 10, "../included.mal"),
      new Token(TokenType.EOF, SUBINCLUDED_MAL, 9, 1)
    };
    assertTokens(tokens, "all-features/subdir/subincluded.mal");
  }

  @Test
  public void testLexerIncluded() {
    Token[] tokens = {
      new Token(TokenType.HASH, INCLUDED_MAL, 2, 2),
      new Token(TokenType.ID, INCLUDED_MAL, 2, 4, "other"),
      new Token(TokenType.COLON, INCLUDED_MAL, 2, 10),
      new Token(TokenType.STRING, INCLUDED_MAL, 2, 12, "other"),
      new Token(TokenType.INCLUDE, INCLUDED_MAL, 4, 2),
      new Token(TokenType.STRING, INCLUDED_MAL, 4, 10, "all-features.mal"),
      new Token(TokenType.EOF, INCLUDED_MAL, 6, 1)
    };
    assertTokens(tokens, "all-features/included.mal");
  }

  @Test
  public void testParser() {
    var ast = assertGetASTClassPath("all-features/all-features.mal");

    var categories = ast.getCategories();
    assertEquals(6, categories.size());
    assertCategory(
        new AST.Category(
            new Position(CORE_MAL, 1, 1),
            new AST.ID(new Position(CORE_MAL, 1, 10), "C1"),
            Arrays.asList(
                new AST.Meta(
                    new Position(CORE_MAL, 2, 3),
                    new ID(new Position(CORE_MAL, 2, 3), "user"),
                    "This is C1"),
                new AST.Meta(
                    new Position(CORE_MAL, 3, 3),
                    new ID(new Position(CORE_MAL, 3, 3), "modeler"),
                    "None for C1"),
                new AST.Meta(
                    new Position(CORE_MAL, 4, 3),
                    new ID(new Position(CORE_MAL, 4, 3), "developer"),
                    "Reasoning for C1")),
            List.of()),
        categories.get(0));
    assertCategory(
        new AST.Category(
            new Position(CORE_MAL, 9, 1),
            new AST.ID(new Position(CORE_MAL, 9, 10), "C1"),
            Arrays.asList(),
            Arrays.asList(
                new AST.Asset(
                    new Position(CORE_MAL, 10, 3),
                    false,
                    new AST.ID(new Position(CORE_MAL, 10, 9), "A1"),
                    Optional.empty(),
                    Arrays.asList(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position(CORE_MAL, 12, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position(CORE_MAL, 12, 7), "a1Attack1"),
                            List.of(),
                            Optional.of(Arrays.asList(AST.CIA.C)),
                            Optional.empty(),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position(CORE_MAL, 13, 7),
                                    new ID(new Position(CORE_MAL, 13, 7), "user"),
                                    "This is a1Attack1"),
                                new AST.Meta(
                                    new Position(CORE_MAL, 14, 7),
                                    new ID(new Position(CORE_MAL, 14, 7), "modeler"),
                                    "None for a1Attack1"),
                                new AST.Meta(
                                    new Position(CORE_MAL, 15, 7),
                                    new ID(new Position(CORE_MAL, 15, 7), "developer"),
                                    "Reasoning for a1Attack1")),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 17, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position(CORE_MAL, 17, 7), "a1Attack2"),
                            List.of(),
                            Optional.of(Arrays.asList(AST.CIA.I, AST.CIA.C)),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position(CORE_MAL, 17, 25),
                                    new AST.ID(new Position(CORE_MAL, 17, 25), "Zero"),
                                    Arrays.asList())),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position(CORE_MAL, 18, 7),
                                    new ID(new Position(CORE_MAL, 18, 7), "user"),
                                    "This is a1Attack2")),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 19, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 19, 10),
                                            new AST.TransitiveExpr(
                                                new Position(CORE_MAL, 19, 10),
                                                new AST.SubTypeExpr(
                                                    new Position(CORE_MAL, 19, 10),
                                                    new AST.IDExpr(
                                                        new Position(CORE_MAL, 19, 10),
                                                        new AST.ID(
                                                            new Position(CORE_MAL, 19, 10),
                                                            "a1Sub")),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 19, 16), "A1"))),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 19, 21),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 19, 21),
                                                    "a1Attack1"))))))),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 20, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position(CORE_MAL, 20, 7), "a1Defense1"),
                            List.of(),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position(CORE_MAL, 20, 19),
                                    new AST.ID(new Position(CORE_MAL, 20, 19), "Bernoulli"),
                                    Arrays.asList(0.5))),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position(CORE_MAL, 21, 7),
                                    new ID(new Position(CORE_MAL, 21, 7), "developer"),
                                    "Reasoning for a1Defense")),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 23, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position(CORE_MAL, 23, 7), "a1Defense2"),
                            List.of(),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position(CORE_MAL, 23, 19),
                                    new AST.ID(new Position(CORE_MAL, 23, 19), "Disabled"),
                                    Arrays.asList())),
                            Arrays.asList(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 24, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position(CORE_MAL, 24, 10),
                                            new AST.ID(
                                                new Position(CORE_MAL, 24, 10), "a1Attack2")))))),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 25, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position(CORE_MAL, 25, 7), "a1Exist1"),
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.of(
                                new AST.Requires(
                                    new Position(CORE_MAL, 26, 7),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position(CORE_MAL, 26, 10),
                                            new AST.ID(new Position(CORE_MAL, 26, 10), "a1Sub"))))),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 28, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position(CORE_MAL, 28, 7), "a1Exist2"),
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position(CORE_MAL, 29, 7),
                                    new ID(new Position(CORE_MAL, 29, 7), "modeler"),
                                    "None for a1Exist2")),
                            Optional.of(
                                new AST.Requires(
                                    new Position(CORE_MAL, 30, 7),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position(CORE_MAL, 30, 10),
                                            new AST.ID(new Position(CORE_MAL, 30, 10), "a7")),
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 30, 14),
                                            new AST.StepExpr(
                                                new Position(CORE_MAL, 30, 14),
                                                new AST.IDExpr(
                                                    new Position(CORE_MAL, 30, 14),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 30, 14), "a1Super")),
                                                new AST.IDExpr(
                                                    new Position(CORE_MAL, 30, 22),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 30, 22),
                                                        "a1Super"))),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 30, 30),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 30, 30), "a7")))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 31, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 31, 10),
                                            new AST.SubTypeExpr(
                                                new Position(CORE_MAL, 31, 10),
                                                new AST.TransitiveExpr(
                                                    new Position(CORE_MAL, 31, 11),
                                                    new AST.IDExpr(
                                                        new Position(CORE_MAL, 31, 11),
                                                        new AST.ID(
                                                            new Position(CORE_MAL, 31, 11),
                                                            "a1Super"))),
                                                new AST.ID(new Position(CORE_MAL, 31, 21), "A2")),
                                            new AST.StepExpr(
                                                new Position(CORE_MAL, 31, 26),
                                                new AST.IDExpr(
                                                    new Position(CORE_MAL, 31, 26),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 31, 26), "a8")),
                                                new AST.IDExpr(
                                                    new Position(CORE_MAL, 31, 29),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 31, 29),
                                                        "destroy")))))))),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 32, 5),
                            AST.AttackStepType.NOTEXIST,
                            new AST.ID(new Position(CORE_MAL, 32, 8), "a1NotExist1"),
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.of(
                                new AST.Requires(
                                    new Position(CORE_MAL, 33, 7),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 33, 10),
                                            new AST.StepExpr(
                                                new Position(CORE_MAL, 33, 10),
                                                new AST.IDExpr(
                                                    new Position(CORE_MAL, 33, 10),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 33, 10), "a8")),
                                                new AST.TransitiveExpr(
                                                    new Position(CORE_MAL, 33, 13),
                                                    new AST.IDExpr(
                                                        new Position(CORE_MAL, 33, 13),
                                                        new AST.ID(
                                                            new Position(CORE_MAL, 33, 13),
                                                            "a8Sub")))),
                                            new AST.SubTypeExpr(
                                                new Position(CORE_MAL, 33, 20),
                                                new AST.IDExpr(
                                                    new Position(CORE_MAL, 33, 20),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 33, 20), "a1")),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 33, 23), "A7")))))),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 35, 5),
                            AST.AttackStepType.NOTEXIST,
                            new AST.ID(new Position(CORE_MAL, 35, 8), "a1NotExist2"),
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.of(
                                new AST.Requires(
                                    new Position(CORE_MAL, 36, 7),
                                    Arrays.asList(
                                        new AST.IntersectionExpr(
                                            new Position(CORE_MAL, 36, 10),
                                            new AST.StepExpr(
                                                new Position(CORE_MAL, 36, 10),
                                                new AST.IDExpr(
                                                    new Position(CORE_MAL, 36, 10),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 36, 10), "a8")),
                                                new AST.IDExpr(
                                                    new Position(CORE_MAL, 36, 13),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 36, 13), "a4"))),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 36, 19),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 36, 19), "a4")))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 37, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 37, 10),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 37, 10),
                                                new AST.ID(new Position(CORE_MAL, 37, 10), "a4")),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 37, 13),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 37, 13), "a")))))))),
                    Arrays.asList()),
                new AST.Asset(
                    new Position(CORE_MAL, 40, 3),
                    false,
                    new AST.ID(new Position(CORE_MAL, 40, 9), "A2"),
                    Optional.of(new AST.ID(new Position(CORE_MAL, 40, 20), "A1")),
                    Arrays.asList(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position(CORE_MAL, 42, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position(CORE_MAL, 42, 7), "a1Attack1"),
                            List.of(),
                            Optional.of(Arrays.asList()),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 43, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 43, 10),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 43, 10),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 43, 10), "a1Super")),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 43, 18),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 43, 18),
                                                    "a1Attack1"))))))),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 45, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position(CORE_MAL, 45, 7), "a1Attack2"),
                            List.of(),
                            Optional.of(Arrays.asList(AST.CIA.C, AST.CIA.I, AST.CIA.A)),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 46, 7),
                                    true,
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 46, 10),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 46, 10),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 46, 10), "a1Super")),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 46, 18),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 46, 18),
                                                    "a1Attack1"))))))),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 48, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position(CORE_MAL, 48, 7), "a1Defense1"),
                            List.of(),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position(CORE_MAL, 48, 19),
                                    new AST.ID(new Position(CORE_MAL, 48, 19), "Enabled"),
                                    Arrays.asList())),
                            Arrays.asList(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 49, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 49, 10),
                                            new AST.StepExpr(
                                                new Position(CORE_MAL, 49, 10),
                                                new AST.IDExpr(
                                                    new Position(CORE_MAL, 49, 10),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 49, 10), "a1Sub")),
                                                new AST.IDExpr(
                                                    new Position(CORE_MAL, 49, 16),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 49, 16), "a1Sub"))),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 49, 22),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 49, 22),
                                                    "a1Attack2"))))))),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 51, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position(CORE_MAL, 51, 7), "a1Defense2"),
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 52, 7),
                                    true,
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position(CORE_MAL, 52, 10),
                                            new AST.ID(
                                                new Position(CORE_MAL, 52, 10), "a1Attack2")))))),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 54, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position(CORE_MAL, 54, 7), "a1Exist2"),
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.of(
                                new AST.Requires(
                                    new Position(CORE_MAL, 55, 7),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position(CORE_MAL, 55, 10),
                                            new AST.ID(new Position(CORE_MAL, 55, 10), "a8"))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 56, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position(CORE_MAL, 56, 10),
                                            new AST.ID(
                                                new Position(CORE_MAL, 56, 10), "a1Defense2")))))),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 58, 5),
                            AST.AttackStepType.NOTEXIST,
                            new AST.ID(new Position(CORE_MAL, 58, 8), "a1NotExist1"),
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.of(
                                new AST.Requires(
                                    new Position(CORE_MAL, 59, 8),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position(CORE_MAL, 59, 11),
                                            new AST.ID(new Position(CORE_MAL, 59, 11), "a8"))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 60, 8),
                                    true,
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position(CORE_MAL, 60, 11),
                                            new AST.ID(
                                                new Position(CORE_MAL, 60, 11), "a1Defense1"))))))),
                    Arrays.asList()),
                new AST.Asset(
                    new Position(CORE_MAL, 63, 3),
                    true,
                    new AST.ID(new Position(CORE_MAL, 63, 18), "A3"),
                    Optional.of(new AST.ID(new Position(CORE_MAL, 63, 29), "A1")),
                    Arrays.asList(
                        new AST.Meta(
                            new Position(CORE_MAL, 64, 5),
                            new ID(new Position(CORE_MAL, 64, 5), "user"),
                            "This is A3")),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position(CORE_MAL, 67, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position(CORE_MAL, 67, 7), "a3Attack"),
                            List.of(),
                            Optional.of(Arrays.asList(AST.CIA.A, AST.CIA.I, AST.CIA.C)),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position(CORE_MAL, 71, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position(CORE_MAL, 71, 7), "AT"),
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 72, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 72, 10),
                                            new AST.CallExpr(
                                                new Position(CORE_MAL, 72, 10),
                                                new AST.ID(new Position(CORE_MAL, 72, 10), "V1")),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 72, 15),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 72, 15),
                                                    "a1Attack1")))))))),
                    Arrays.asList(
                        new AST.Variable(
                            new Position(CORE_MAL, 66, 5),
                            new AST.ID(new Position(CORE_MAL, 66, 9), "unused"),
                            new AST.IDExpr(
                                new Position(CORE_MAL, 66, 18),
                                new AST.ID(new Position(CORE_MAL, 66, 18), "a1Sub"))),
                        new AST.Variable(
                            new Position(CORE_MAL, 68, 5),
                            new AST.ID(new Position(CORE_MAL, 68, 9), "used"),
                            new AST.SubTypeExpr(
                                new Position(CORE_MAL, 68, 16),
                                new AST.IDExpr(
                                    new Position(CORE_MAL, 68, 16),
                                    new AST.ID(new Position(CORE_MAL, 68, 16), "a1Sub")),
                                new AST.ID(new Position(CORE_MAL, 68, 22), "A2"))),
                        new AST.Variable(
                            new Position(CORE_MAL, 70, 5),
                            new AST.ID(new Position(CORE_MAL, 70, 9), "V1"),
                            new AST.CallExpr(
                                new Position(CORE_MAL, 70, 14),
                                new AST.ID(new Position(CORE_MAL, 70, 14), "used"))))))),
        categories.get(1));
    assertCategory(
        new AST.Category(
            new Position(CORE_MAL, 76, 1),
            new AST.ID(new Position(CORE_MAL, 76, 10), "C2"),
            Arrays.asList(),
            Arrays.asList()),
        categories.get(2));
    assertCategory(
        new AST.Category(
            new Position(CORE_MAL, 78, 1),
            new AST.ID(new Position(CORE_MAL, 78, 10), "C2"),
            Arrays.asList(
                new AST.Meta(
                    new Position(CORE_MAL, 79, 3),
                    new ID(new Position(CORE_MAL, 79, 3), "modeler"),
                    "None for C2")),
            Arrays.asList(
                new AST.Asset(
                    new Position(CORE_MAL, 81, 3),
                    true,
                    new AST.ID(new Position(CORE_MAL, 81, 18), "A4"),
                    Optional.empty(),
                    Arrays.asList(
                        new AST.Meta(
                            new Position(CORE_MAL, 82, 5),
                            new ID(new Position(CORE_MAL, 82, 5), "modeler"),
                            "None for A4"),
                        new AST.Meta(
                            new Position(CORE_MAL, 83, 5),
                            new ID(new Position(CORE_MAL, 83, 5), "developer"),
                            "Reasoning for A4"),
                        new AST.Meta(
                            new Position(CORE_MAL, 84, 5),
                            new ID(new Position(CORE_MAL, 84, 5), "user"),
                            "This is A4")),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position(CORE_MAL, 87, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position(CORE_MAL, 87, 7), "a"),
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 88, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 88, 10),
                                            new AST.UnionExpr(
                                                new Position(CORE_MAL, 88, 11),
                                                new AST.CallExpr(
                                                    new Position(CORE_MAL, 88, 11),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 88, 11), "var")),
                                                new AST.CallExpr(
                                                    new Position(CORE_MAL, 88, 20),
                                                    new AST.ID(
                                                        new Position(CORE_MAL, 88, 20), "var2"))),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 88, 28),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 88, 28),
                                                    "a1Attack1")))))))),
                    Arrays.asList(
                        new AST.Variable(
                            new Position(CORE_MAL, 86, 5),
                            new AST.ID(new Position(CORE_MAL, 86, 9), "var"),
                            new AST.StepExpr(
                                new Position(CORE_MAL, 86, 15),
                                new AST.IDExpr(
                                    new Position(CORE_MAL, 86, 15),
                                    new AST.ID(new Position(CORE_MAL, 86, 15), "a1")),
                                new AST.TransitiveExpr(
                                    new Position(CORE_MAL, 86, 18),
                                    new AST.IDExpr(
                                        new Position(CORE_MAL, 86, 18),
                                        new AST.ID(new Position(CORE_MAL, 86, 18), "a1Sub"))))),
                        new AST.Variable(
                            new Position(CORE_MAL, 89, 5),
                            new AST.ID(new Position(CORE_MAL, 89, 9), "var2"),
                            new AST.SubTypeExpr(
                                new Position(CORE_MAL, 89, 16),
                                new AST.CallExpr(
                                    new Position(CORE_MAL, 89, 16),
                                    new AST.ID(new Position(CORE_MAL, 89, 16), "var")),
                                new AST.ID(new Position(CORE_MAL, 89, 22), "A3"))))))),
        categories.get(3));
    assertCategory(
        new AST.Category(
            new Position(CORE_MAL, 93, 1),
            new AST.ID(new Position(CORE_MAL, 93, 10), "C2"),
            Arrays.asList(
                new AST.Meta(
                    new Position(CORE_MAL, 94, 3),
                    new ID(new Position(CORE_MAL, 94, 3), "developer"),
                    "Reasoning for C2")),
            Arrays.asList(
                new AST.Asset(
                    new Position(CORE_MAL, 96, 3),
                    false,
                    new AST.ID(new Position(CORE_MAL, 96, 9), "A5"),
                    Optional.of(new AST.ID(new Position(CORE_MAL, 96, 20), "A4")),
                    Arrays.asList(
                        new AST.Meta(
                            new Position(CORE_MAL, 97, 5),
                            new ID(new Position(CORE_MAL, 97, 5), "modeler"),
                            "None for A5")),
                    Arrays.asList(),
                    Arrays.asList()),
                new AST.Asset(
                    new Position(CORE_MAL, 100, 3),
                    true,
                    new AST.ID(new Position(CORE_MAL, 100, 18), "A6"),
                    Optional.of(new AST.ID(new Position(CORE_MAL, 100, 29), "A4")),
                    Arrays.asList(
                        new AST.Meta(
                            new Position(CORE_MAL, 101, 5),
                            new ID(new Position(CORE_MAL, 101, 5), "developer"),
                            "Reasoning for A6")),
                    Arrays.asList(),
                    Arrays.asList()))),
        categories.get(4));
    assertCategory(
        new AST.Category(
            new Position(CORE_MAL, 107, 1),
            new AST.ID(new Position(CORE_MAL, 107, 10), "C3"),
            Arrays.asList(),
            Arrays.asList(
                new AST.Asset(
                    new Position(CORE_MAL, 108, 3),
                    false,
                    new AST.ID(new Position(CORE_MAL, 108, 9), "A7"),
                    Optional.of(new AST.ID(new Position(CORE_MAL, 108, 20), "A3")),
                    Arrays.asList(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position(CORE_MAL, 109, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position(CORE_MAL, 109, 7), "a7Attack"),
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position(CORE_MAL, 110, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position(CORE_MAL, 110, 10),
                                            new AST.CallExpr(
                                                new Position(CORE_MAL, 110, 10),
                                                new AST.ID(new Position(CORE_MAL, 110, 10), "a1")),
                                            new AST.IDExpr(
                                                new Position(CORE_MAL, 110, 15),
                                                new AST.ID(
                                                    new Position(CORE_MAL, 110, 15),
                                                    "destroy")))))))),
                    Arrays.asList(
                        new AST.Variable(
                            new Position(CORE_MAL, 111, 5),
                            new AST.ID(new Position(CORE_MAL, 111, 9), "a6"),
                            new AST.StepExpr(
                                new Position(CORE_MAL, 111, 14),
                                new AST.IDExpr(
                                    new Position(CORE_MAL, 111, 14),
                                    new AST.ID(new Position(CORE_MAL, 111, 14), "a1Super")),
                                new AST.IDExpr(
                                    new Position(CORE_MAL, 111, 22),
                                    new AST.ID(new Position(CORE_MAL, 111, 22), "a8")))),
                        new AST.Variable(
                            new Position(CORE_MAL, 112, 5),
                            new AST.ID(new Position(CORE_MAL, 112, 9), "a1"),
                            new AST.StepExpr(
                                new Position(CORE_MAL, 112, 14),
                                new AST.CallExpr(
                                    new Position(CORE_MAL, 112, 14),
                                    new AST.ID(new Position(CORE_MAL, 112, 14), "a6")),
                                new AST.IDExpr(
                                    new Position(CORE_MAL, 112, 19),
                                    new AST.ID(new Position(CORE_MAL, 112, 19), "a8Super")))))),
                new AST.Asset(
                    new Position(CORE_MAL, 115, 3),
                    false,
                    new AST.ID(new Position(CORE_MAL, 115, 9), "A8"),
                    Optional.empty(),
                    Arrays.asList(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position(CORE_MAL, 116, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position(CORE_MAL, 116, 7), "destroy"),
                            List.of(),
                            Optional.of(Arrays.asList(AST.CIA.C, AST.CIA.I, AST.CIA.A)),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position(CORE_MAL, 116, 26),
                                    new AST.ID(new Position(CORE_MAL, 116, 26), "Exponential"),
                                    Arrays.asList(5.0))),
                            Arrays.asList(),
                            Optional.empty(),
                            Optional.empty())),
                    Arrays.asList()),
                new AST.Asset(
                    new Position(CORE_MAL, 119, 3),
                    false,
                    new AST.ID(new Position(CORE_MAL, 119, 9), "A9"),
                    Optional.empty(),
                    Arrays.asList(),
                    Arrays.asList(),
                    Arrays.asList()))),
        categories.get(5));
    var associations = ast.getAssociations();
    assertEquals(8, associations.size());
    assertAssociation(
        new AST.Association(
            new Position(CORE_MAL, 129, 3),
            new AST.ID(new Position(CORE_MAL, 129, 3), "A1"),
            new AST.ID(new Position(CORE_MAL, 129, 7), "a1"),
            AST.Multiplicity.ONE,
            new AST.ID(new Position(CORE_MAL, 129, 25), "L1"),
            AST.Multiplicity.ONE_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 129, 38), "a4"),
            new AST.ID(new Position(CORE_MAL, 129, 45), "A4"),
            Arrays.asList()),
        associations.get(0));
    assertAssociation(
        new AST.Association(
            new Position(CORE_MAL, 130, 3),
            new AST.ID(new Position(CORE_MAL, 130, 3), "A5"),
            new AST.ID(new Position(CORE_MAL, 130, 7), "a5"),
            AST.Multiplicity.ONE,
            new AST.ID(new Position(CORE_MAL, 130, 25), "L2"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 130, 38), "a6"),
            new AST.ID(new Position(CORE_MAL, 130, 45), "A6"),
            Arrays.asList()),
        associations.get(1));
    assertAssociation(
        new AST.Association(
            new Position(CORE_MAL, 135, 3),
            new AST.ID(new Position(CORE_MAL, 135, 3), "A1"),
            new AST.ID(new Position(CORE_MAL, 135, 7), "a1Super"),
            AST.Multiplicity.ZERO_OR_ONE,
            new AST.ID(new Position(CORE_MAL, 135, 25), "L3"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 135, 38), "a1Sub"),
            new AST.ID(new Position(CORE_MAL, 135, 47), "A1"),
            Arrays.asList()),
        associations.get(2));
    assertAssociation(
        new AST.Association(
            new Position(CORE_MAL, 136, 3),
            new AST.ID(new Position(CORE_MAL, 136, 3), "A3"),
            new AST.ID(new Position(CORE_MAL, 136, 7), "a3"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 136, 25), "L3"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 136, 38), "a6"),
            new AST.ID(new Position(CORE_MAL, 136, 47), "A6"),
            Arrays.asList()),
        associations.get(3));
    assertAssociation(
        new AST.Association(
            new Position(CORE_MAL, 137, 3),
            new AST.ID(new Position(CORE_MAL, 137, 3), "A7"),
            new AST.ID(new Position(CORE_MAL, 137, 7), "a7"),
            AST.Multiplicity.ZERO_OR_ONE,
            new AST.ID(new Position(CORE_MAL, 137, 25), "L3"),
            AST.Multiplicity.ONE,
            new AST.ID(new Position(CORE_MAL, 137, 38), "a1"),
            new AST.ID(new Position(CORE_MAL, 137, 47), "A1"),
            Arrays.asList()),
        associations.get(4));
    assertAssociation(
        new AST.Association(
            new Position(CORE_MAL, 138, 3),
            new AST.ID(new Position(CORE_MAL, 138, 3), "A8"),
            new AST.ID(new Position(CORE_MAL, 138, 7), "a8"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 138, 25), "L4"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 138, 38), "a1"),
            new AST.ID(new Position(CORE_MAL, 138, 47), "A1"),
            Arrays.asList()),
        associations.get(5));
    assertAssociation(
        new AST.Association(
            new Position(CORE_MAL, 139, 3),
            new AST.ID(new Position(CORE_MAL, 139, 3), "A8"),
            new AST.ID(new Position(CORE_MAL, 139, 7), "a8"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 139, 25), "L4"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 139, 38), "a4"),
            new AST.ID(new Position(CORE_MAL, 139, 47), "A4"),
            Arrays.asList()),
        associations.get(6));
    assertAssociation(
        new AST.Association(
            new Position(CORE_MAL, 140, 3),
            new AST.ID(new Position(CORE_MAL, 140, 3), "A8"),
            new AST.ID(new Position(CORE_MAL, 140, 7), "a8Sub"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 140, 25), "L4"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position(CORE_MAL, 140, 38), "a8Super"),
            new AST.ID(new Position(CORE_MAL, 140, 47), "A8"),
            Arrays.asList()),
        associations.get(7));
    var defines = ast.getDefines();
    assertEquals(4, defines.size());
    assertDefine(
        new AST.Define(
            new Position(ALL_FEATURES_MAL, 6, 1),
            new AST.ID(new Position(ALL_FEATURES_MAL, 6, 2), "id"),
            "all-features"),
        defines.get(0));
    assertDefine(
        new AST.Define(
            new Position(ALL_FEATURES_MAL, 7, 1),
            new AST.ID(new Position(ALL_FEATURES_MAL, 7, 2), "version"),
            "0.0.1"),
        defines.get(1));
    assertDefine(
        new AST.Define(
            new Position(SUBDIR_SUBINCLUDED_MAL, 2, 2),
            new AST.ID(new Position(SUBDIR_SUBINCLUDED_MAL, 3, 4), "empty"),
            ""),
        defines.get(2));
    assertDefine(
        new AST.Define(
            new Position(INCLUDED_MAL, 2, 2),
            new AST.ID(new Position(INCLUDED_MAL, 2, 4), "other"),
            "other"),
        defines.get(3));
  }

  @Test
  public void testAnalyzer() {
    assertAnalyzeClassPath("all-features/all-features.mal");
    assertEmptyOut();
    String[] lines = {
      "[ANALYZER WARNING] <core.mal:66:9> Variable 'unused' is never used",
      "[ANALYZER WARNING] <core.mal:76:10> Category 'C2' contains no assets or metadata",
      "[ANALYZER WARNING] <core.mal:100:18> Asset 'A6' is abstract but never extended to",
      "[ANALYZER WARNING] <core.mal:130:3> Association 'A5 [a5] <-- L2 --> A6 [a6]' is never used",
      "[ANALYZER WARNING] <core.mal:136:3> Association 'A3 [a3] <-- L3 --> A6 [a6]' is never used",
      ""
    };
    assertErrLines(lines);
  }

  @Test
  public void testLangConverter() {
    var lang = assertGetLangClassPath("all-features/all-features.mal");
    assertDefines(lang);
    assertCategories(lang);
    assertAssets(lang);
    assertLinks(lang);
  }

  private static void assertDefines(Lang lang) {
    assertLangDefines(
        Map.ofEntries(
            Map.entry("id", "all-features"),
            Map.entry("version", "0.0.1"),
            Map.entry("empty", ""),
            Map.entry("other", "other")),
        lang.getDefines());
  }

  private static void assertCategories(Lang lang) {
    assertEquals(3, lang.getCategories().size());
    assertLangCategory(
        lang,
        "C1",
        new String[] {"A1", "A2", "A3"},
        Map.ofEntries(
            Map.entry("user", "This is C1"),
            Map.entry("modeler", "None for C1"),
            Map.entry("developer", "Reasoning for C1")));
    assertLangCategory(
        lang,
        "C2",
        new String[] {"A4", "A5", "A6"},
        Map.ofEntries(
            Map.entry("modeler", "None for C2"), Map.entry("developer", "Reasoning for C2")));
    assertLangCategory(lang, "C3", new String[] {"A7", "A8", "A9"}, Map.of());
  }

  private static void assertAssets(Lang lang) {
    assertEquals(9, lang.getAssets().size());
    assertAssetA1(lang);
    assertAssetA2(lang);
    assertAssetA3(lang);
    assertAssetA4(lang);
    assertAssetA5(lang);
    assertAssetA6(lang);
    assertAssetA7(lang);
    assertAssetA8(lang);
    assertAssetA9(lang);
  }

  private static void assertAssetA1(Lang lang) {
    var asset = assertGetLangAsset(lang, "A1", false, "C1", null, Map.of());
    // Check fields
    assertEquals(5, asset.getFields().size());
    assertLangField(asset, "a4", 1, Integer.MAX_VALUE);
    assertLangField(asset, "a1Sub", 0, Integer.MAX_VALUE);
    assertLangField(asset, "a1Super", 0, 1);
    assertLangField(asset, "a7", 0, 1);
    assertLangField(asset, "a8", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(8, asset.getAttackSteps().size());

    // Check attack step "a1Attack1"
    var attackStep =
        assertGetLangAttackStep(
            asset,
            "a1Attack1",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            Map.ofEntries(
                Map.entry("user", "This is a1Attack1"),
                Map.entry("modeler", "None for a1Attack1"),
                Map.entry("developer", "Reasoning for a1Attack1")));
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, new Lang.CIA(true, false, false));
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(0, reaches.size());
    assertEquals(4, parentSteps.size());

    // Auto-generated parent step 1
    Lang.StepExpr step =
        new Lang.StepCollect(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            null,
            null,
            new Lang.StepTransitive(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                new Lang.StepField(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1").getField("a1Super"))),
            new Lang.StepAttackStep(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1").getAttackStep("a1Attack2")));
    assertLangStepExpr(step, parentSteps.get(0));

    // Auto-generated parent step 2
    step =
        new Lang.StepCollect(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            null,
            null,
            new Lang.StepField(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A2"),
                lang.getAsset("A1").getField("a1Sub")),
            new Lang.StepAttackStep(
                lang.getAsset("A2"),
                lang.getAsset("A2"),
                lang.getAsset("A2").getAttackStep("a1Attack1")));
    assertLangStepExpr(step, parentSteps.get(1));

    // Auto-generated parent step 3
    step =
        new Lang.StepCollect(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            null,
            null,
            new Lang.StepField(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A2"),
                lang.getAsset("A1").getField("a1Sub")),
            new Lang.StepAttackStep(
                lang.getAsset("A2"),
                lang.getAsset("A2"),
                lang.getAsset("A2").getAttackStep("a1Attack2")));
    assertLangStepExpr(step, parentSteps.get(2));

    // Auto-generated parent step 4
    step =
        new Lang.StepCollect(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            null,
            null,
            new Lang.StepUnion(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A4"),
                lang.getAsset("A4"),
                new Lang.StepCall(
                    lang.getAsset("A3"),
                    lang.getAsset("A1"),
                    lang.getAsset("A4"),
                    lang.getAsset("A4"),
                    "reversevar2A4"),
                new Lang.StepCall(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A4"),
                    lang.getAsset("A4"),
                    "reversevarA4")),
            new Lang.StepAttackStep(
                lang.getAsset("A4"), lang.getAsset("A4"), lang.getAsset("A4").getAttackStep("a")));
    assertLangStepExpr(step, parentSteps.get(3));

    // Check attack step "a1Attack2"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "a1Attack2",
            Lang.AttackStepType.ALL,
            false,
            false,
            false,
            false,
            Map.ofEntries(Map.entry("user", "This is a1Attack2")));
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, new Lang.CIA(true, true, false));
    assertLangTTC(attackStep, new Lang.TTCFunc(new Distributions.Zero()));
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(2, parentSteps.size());

    // Auto-generated reaches step 1
    step =
        new Lang.StepCollect(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            null,
            null,
            new Lang.StepTransitive(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                new Lang.StepField(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1").getField("a1Sub"))),
            new Lang.StepAttackStep(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1").getAttackStep("a1Attack1")));
    assertLangStepExpr(step, reaches.get(0));

    // Auto-generated parent step 1
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            lang.getAsset("A1").getAttackStep("a1Defense2"));
    assertLangStepExpr(step, parentSteps.get(0));

    // Auto-generated parent step 2
    step =
        new Lang.StepCollect(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            null,
            null,
            new Lang.StepCollect(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A2"),
                lang.getAsset("A2"),
                new Lang.StepField(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1").getField("a1Super")),
                new Lang.StepField(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A2"),
                    lang.getAsset("A1").getField("a1Super"))),
            new Lang.StepAttackStep(
                lang.getAsset("A2"),
                lang.getAsset("A2"),
                lang.getAsset("A2").getAttackStep("a1Defense1")));
    assertLangStepExpr(step, parentSteps.get(1));

    // Check attack step "a1Defense1"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "a1Defense1",
            Lang.AttackStepType.DEFENSE,
            false,
            true,
            false,
            false,
            Map.ofEntries(Map.entry("developer", "Reasoning for a1Defense")));

    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, new Lang.TTCFunc(new Distributions.Bernoulli(0.5)));
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(0, reaches.size());
    assertEquals(0, parentSteps.size());

    // Check attack step "a1Defense2"
    attackStep =
        assertGetLangAttackStep(
            asset, "a1Defense2", Lang.AttackStepType.DEFENSE, false, true, false, false, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, new Lang.TTCFunc(new Distributions.Disabled()));
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // Auto-generated reaches step 1
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            lang.getAsset("A1").getAttackStep("a1Attack2"));
    assertLangStepExpr(step, reaches.get(0));

    // Check attack step "a1Exist1"
    attackStep =
        assertGetLangAttackStep(
            asset, "a1Exist1", Lang.AttackStepType.EXIST, false, false, true, false, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(1, requires.size());
    assertEquals(0, reaches.size());
    assertEquals(0, parentSteps.size());

    // Auto-generated requires step 1
    step =
        new Lang.StepField(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            lang.getAsset("A1").getField("a1Sub"));
    assertLangStepExpr(step, requires.get(0));

    // Check attack step "a1Exist2"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "a1Exist2",
            Lang.AttackStepType.EXIST,
            false,
            false,
            true,
            false,
            Map.ofEntries(Map.entry("modeler", "None for a1Exist2")));
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(2, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // Auto-generated requires step 1
    step =
        new Lang.StepField(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            lang.getAsset("A7"),
            lang.getAsset("A7"),
            lang.getAsset("A1").getField("a7"));
    assertLangStepExpr(step, requires.get(0));

    // Auto-generated requires step 2
    step =
        new Lang.StepCollect(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            lang.getAsset("A7"),
            lang.getAsset("A7"),
            new Lang.StepCollect(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                new Lang.StepField(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1").getField("a1Super")),
                new Lang.StepField(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1").getField("a1Super"))),
            new Lang.StepField(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A7"),
                lang.getAsset("A7"),
                lang.getAsset("A1").getField("a7")));
    assertLangStepExpr(step, requires.get(1));

    // Auto-generated reaches step 1
    step =
        new Lang.StepCollect(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            null,
            null,
            new Lang.StepTransitive(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A2"),
                new Lang.StepField(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1").getField("a1Super"))),
            new Lang.StepCollect(
                lang.getAsset("A2"),
                lang.getAsset("A2"),
                null,
                null,
                new Lang.StepField(
                    lang.getAsset("A2"),
                    lang.getAsset("A1"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A1").getField("a8")),
                new Lang.StepAttackStep(
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8").getAttackStep("destroy"))));
    assertLangStepExpr(step, reaches.get(0));

    // Check attack step "a1NotExist1"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "a1NotExist1",
            Lang.AttackStepType.NOTEXIST,
            false,
            false,
            true,
            false,
            Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(1, requires.size());
    assertEquals(0, reaches.size());
    assertEquals(0, parentSteps.size());

    // Auto-generated requires step 1
    step =
        new Lang.StepCollect(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            lang.getAsset("A7"),
            lang.getAsset("A7"),
            new Lang.StepCollect(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A8"),
                lang.getAsset("A8"),
                new Lang.StepField(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A1").getField("a8")),
                new Lang.StepTransitive(
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    new Lang.StepField(
                        lang.getAsset("A8"),
                        lang.getAsset("A8"),
                        lang.getAsset("A8"),
                        lang.getAsset("A8"),
                        lang.getAsset("A8").getField("a8Sub")))),
            new Lang.StepField(
                lang.getAsset("A8"),
                lang.getAsset("A8"),
                lang.getAsset("A1"),
                lang.getAsset("A7"),
                lang.getAsset("A8").getField("a1")));
    assertLangStepExpr(step, requires.get(0));

    // Check attack step "a1NotExist2"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "a1NotExist2",
            Lang.AttackStepType.NOTEXIST,
            false,
            false,
            true,
            false,
            Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(1, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // Auto-generated requires step 1
    step =
        new Lang.StepIntersection(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            lang.getAsset("A4"),
            lang.getAsset("A4"),
            new Lang.StepCollect(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A4"),
                lang.getAsset("A4"),
                new Lang.StepField(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A1").getField("a8")),
                new Lang.StepField(
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A4"),
                    lang.getAsset("A4"),
                    lang.getAsset("A8").getField("a4"))),
            new Lang.StepField(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A4"),
                lang.getAsset("A4"),
                lang.getAsset("A1").getField("a4")));
    assertLangStepExpr(step, requires.get(0));

    // Auto-generated reaches step 1
    step =
        new Lang.StepCollect(
            lang.getAsset("A1"),
            lang.getAsset("A1"),
            null,
            null,
            new Lang.StepField(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A4"),
                lang.getAsset("A4"),
                lang.getAsset("A1").getField("a4")),
            new Lang.StepAttackStep(
                lang.getAsset("A4"), lang.getAsset("A4"), lang.getAsset("A4").getAttackStep("a")));
    assertLangStepExpr(step, reaches.get(0));
  }

  private static void assertAssetA2(Lang lang) {
    var asset = assertGetLangAsset(lang, "A2", false, "C1", "A1", Map.of());
    // Check fields
    assertEquals(0, asset.getFields().size());
    // Check attack steps
    assertEquals(6, asset.getAttackSteps().size());

    // Check attack step "a1Attack1"
    var attackStep =
        assertGetLangAttackStep(
            asset, "a1Attack1", Lang.AttackStepType.ANY, false, false, false, true, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, new Lang.CIA(false, false, false));
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(1, parentSteps.size());

    // Auto-generated reaches step 1
    Lang.StepExpr step =
        new Lang.StepCollect(
            lang.getAsset("A2"),
            lang.getAsset("A2"),
            null,
            null,
            new Lang.StepField(
                lang.getAsset("A2"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1").getField("a1Super")),
            new Lang.StepAttackStep(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1").getAttackStep("a1Attack1")));
    assertLangStepExpr(step, reaches.get(0));

    // Auto-generated parent step 1
    step =
        new Lang.StepCollect(
            lang.getAsset("A2"),
            lang.getAsset("A2"),
            null,
            null,
            new Lang.StepCall(
                lang.getAsset("A2"),
                lang.getAsset("A2"),
                lang.getAsset("A1"),
                lang.getAsset("A3"),
                "reverseV1A3"),
            new Lang.StepAttackStep(
                lang.getAsset("A3"), lang.getAsset("A3"), lang.getAsset("A3").getAttackStep("AT")));
    assertLangStepExpr(step, parentSteps.get(0));

    // Check attack step "a1Attack2"
    attackStep =
        assertGetLangAttackStep(
            asset, "a1Attack2", Lang.AttackStepType.ALL, true, false, false, true, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, new Lang.CIA(true, true, true));
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(1, parentSteps.size());

    // Auto-generated reaches step 1
    step =
        new Lang.StepCollect(
            lang.getAsset("A2"),
            lang.getAsset("A2"),
            null,
            null,
            new Lang.StepField(
                lang.getAsset("A2"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1").getField("a1Super")),
            new Lang.StepAttackStep(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1").getAttackStep("a1Attack1")));
    assertLangStepExpr(step, reaches.get(0));

    // Auto-generated parent step 1
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A2"),
            lang.getAsset("A2"),
            lang.getAsset("A2").getAttackStep("a1Defense2"));
    assertLangStepExpr(step, parentSteps.get(0));

    // Check attack step "a1Defense1"
    attackStep =
        assertGetLangAttackStep(
            asset, "a1Defense1", Lang.AttackStepType.DEFENSE, false, true, false, true, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, new Lang.TTCFunc(new Distributions.Enabled()));
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(1, parentSteps.size());

    // Auto-generated reaches step 1
    step =
        new Lang.StepCollect(
            lang.getAsset("A2"),
            lang.getAsset("A2"),
            null,
            null,
            new Lang.StepCollect(
                lang.getAsset("A2"),
                lang.getAsset("A2"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                new Lang.StepField(
                    lang.getAsset("A2"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1").getField("a1Sub")),
                new Lang.StepField(
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1").getField("a1Sub"))),
            new Lang.StepAttackStep(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1").getAttackStep("a1Attack2")));
    assertLangStepExpr(step, reaches.get(0));

    // Auto-generated parent step 1
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A2"),
            lang.getAsset("A2"),
            lang.getAsset("A2").getAttackStep("a1NotExist1"));
    assertLangStepExpr(step, parentSteps.get(0));

    // Check attack step "a1Defense2"
    attackStep =
        assertGetLangAttackStep(
            asset, "a1Defense2", Lang.AttackStepType.DEFENSE, true, true, false, true, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(1, parentSteps.size());

    // Auto-generated reaches step 1
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A2"),
            lang.getAsset("A2"),
            lang.getAsset("A2").getAttackStep("a1Attack2"));
    assertLangStepExpr(step, reaches.get(0));

    // Auto-generated parent step 1
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A2"),
            lang.getAsset("A2"),
            lang.getAsset("A2").getAttackStep("a1Exist2"));
    assertLangStepExpr(step, parentSteps.get(0));

    // Check attack step "a1Exist2"
    attackStep =
        assertGetLangAttackStep(
            asset, "a1Exist2", Lang.AttackStepType.EXIST, false, false, true, true, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(1, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // Auto-generated requires step 1
    step =
        new Lang.StepField(
            lang.getAsset("A2"),
            lang.getAsset("A1"),
            lang.getAsset("A8"),
            lang.getAsset("A8"),
            lang.getAsset("A1").getField("a8"));
    assertLangStepExpr(step, requires.get(0));

    // Auto-generated reaches step 1
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A2"),
            lang.getAsset("A2"),
            lang.getAsset("A2").getAttackStep("a1Defense2"));
    assertLangStepExpr(step, reaches.get(0));

    // Check attack step "a1NotExist1"
    attackStep =
        assertGetLangAttackStep(
            asset, "a1NotExist1", Lang.AttackStepType.NOTEXIST, true, false, true, true, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(1, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // Auto-generated requires step 1
    step =
        new Lang.StepField(
            lang.getAsset("A2"),
            lang.getAsset("A1"),
            lang.getAsset("A8"),
            lang.getAsset("A8"),
            lang.getAsset("A1").getField("a8"));
    assertLangStepExpr(step, requires.get(0));

    // Auto-generated reaches step 1
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A2"),
            lang.getAsset("A2"),
            lang.getAsset("A2").getAttackStep("a1Defense1"));
    assertLangStepExpr(step, reaches.get(0));
  }

  private static void assertAssetA3(Lang lang) {
    var asset =
        assertGetLangAsset(
            lang, "A3", true, "C1", "A1", Map.ofEntries(Map.entry("user", "This is A3")));
    // Check fields
    assertEquals(1, asset.getFields().size());
    assertLangField(asset, "a6", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(2, asset.getAttackSteps().size());

    // Check attack step "a3Attack"
    var attackStep =
        assertGetLangAttackStep(
            asset, "a3Attack", Lang.AttackStepType.ANY, false, false, false, false, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, new Lang.CIA(true, true, true));
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(0, reaches.size());
    assertEquals(0, parentSteps.size());

    // Check attack step "AT"
    attackStep =
        assertGetLangAttackStep(
            asset, "AT", Lang.AttackStepType.ALL, false, false, false, false, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // Auto-generated reaches step 1
    Lang.StepExpr step =
        new Lang.StepCollect(
            lang.getAsset("A3"),
            lang.getAsset("A3"),
            null,
            null,
            new Lang.StepCall(
                lang.getAsset("A3"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A2"),
                "V1A3"),
            new Lang.StepAttackStep(
                lang.getAsset("A2"),
                lang.getAsset("A2"),
                lang.getAsset("A2").getAttackStep("a1Attack1")));
    assertLangStepExpr(step, reaches.get(0));
  }

  private static void assertAssetA4(Lang lang) {
    var asset =
        assertGetLangAsset(
            lang,
            "A4",
            true,
            "C2",
            null,
            Map.ofEntries(
                Map.entry("user", "This is A4"),
                Map.entry("developer", "Reasoning for A4"),
                Map.entry("modeler", "None for A4")));
    // Check fields
    assertEquals(2, asset.getFields().size());
    assertLangField(asset, "a1", 1, 1);
    assertLangField(asset, "a8", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(1, asset.getAttackSteps().size());

    // Check attack step "a"
    var attackStep =
        assertGetLangAttackStep(
            asset, "a", Lang.AttackStepType.ANY, false, false, false, false, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(1, parentSteps.size());

    // Auto-generated reaches step 1
    Lang.StepExpr step =
        new Lang.StepCollect(
            lang.getAsset("A4"),
            lang.getAsset("A4"),
            null,
            null,
            new Lang.StepUnion(
                lang.getAsset("A4"),
                lang.getAsset("A4"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                new Lang.StepCall(
                    lang.getAsset("A4"),
                    lang.getAsset("A4"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    "varA4"),
                new Lang.StepCall(
                    lang.getAsset("A4"),
                    lang.getAsset("A4"),
                    lang.getAsset("A1"),
                    lang.getAsset("A3"),
                    "var2A4")),
            new Lang.StepAttackStep(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1").getAttackStep("a1Attack1")));
    assertLangStepExpr(step, reaches.get(0));

    // Auto-generated parent step 1
    step =
        new Lang.StepCollect(
            lang.getAsset("A4"),
            lang.getAsset("A4"),
            null,
            null,
            new Lang.StepField(
                lang.getAsset("A4"),
                lang.getAsset("A4"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A4").getField("a1")),
            new Lang.StepAttackStep(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1").getAttackStep("a1NotExist2")));
    assertLangStepExpr(step, parentSteps.get(0));
  }

  private static void assertAssetA5(Lang lang) {
    var asset =
        assertGetLangAsset(
            lang, "A5", false, "C2", "A4", Map.ofEntries(Map.entry("modeler", "None for A5")));
    // Check fields
    assertEquals(1, asset.getFields().size());
    assertLangField(asset, "a6", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(0, asset.getAttackSteps().size());
  }

  private static void assertAssetA6(Lang lang) {
    var asset =
        assertGetLangAsset(
            lang,
            "A6",
            true,
            "C2",
            "A4",
            Map.ofEntries(Map.entry("developer", "Reasoning for A6")));
    // Check fields
    assertEquals(2, asset.getFields().size());
    assertLangField(asset, "a5", 1, 1);
    assertLangField(asset, "a3", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(0, asset.getAttackSteps().size());
  }

  private static void assertAssetA7(Lang lang) {
    var asset = assertGetLangAsset(lang, "A7", false, "C3", "A3", Map.of());
    // Check fields
    assertEquals(1, asset.getFields().size());
    assertLangField(asset, "a1", 1, 1);
    // Check attack steps
    assertEquals(1, asset.getAttackSteps().size());

    // Check attack step "a7Attack"
    var attackStep =
        assertGetLangAttackStep(
            asset, "a7Attack", Lang.AttackStepType.ANY, false, false, false, false, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // Auto-generated reaches step 1
    Lang.StepExpr step =
        new Lang.StepCollect(
            lang.getAsset("A7"),
            lang.getAsset("A7"),
            null,
            null,
            new Lang.StepCall(
                lang.getAsset("A7"),
                lang.getAsset("A7"),
                lang.getAsset("A8"),
                lang.getAsset("A8"),
                "a1A7"),
            new Lang.StepAttackStep(
                lang.getAsset("A8"),
                lang.getAsset("A8"),
                lang.getAsset("A8").getAttackStep("destroy")));
    assertLangStepExpr(step, reaches.get(0));
  }

  private static void assertAssetA8(Lang lang) {
    var asset = assertGetLangAsset(lang, "A8", false, "C3", null, Map.of());
    // Check fields
    assertEquals(4, asset.getFields().size());
    assertLangField(asset, "a1", 0, Integer.MAX_VALUE);
    assertLangField(asset, "a4", 0, Integer.MAX_VALUE);
    assertLangField(asset, "a8Super", 0, Integer.MAX_VALUE);
    assertLangField(asset, "a8Sub", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(1, asset.getAttackSteps().size());

    // Check attack step "destroy"
    var attackStep =
        assertGetLangAttackStep(
            asset, "destroy", Lang.AttackStepType.ALL, false, false, false, false, Map.of());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, new Lang.CIA(true, true, true));
    assertLangTTC(attackStep, new Lang.TTCFunc(new Distributions.Exponential(5)));
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(0, reaches.size());
    assertEquals(2, parentSteps.size());

    // Auto-generated parent step 1
    Lang.StepExpr step =
        new Lang.StepCollect(
            lang.getAsset("A8"),
            lang.getAsset("A8"),
            null,
            null,
            new Lang.StepCollect(
                lang.getAsset("A8"),
                lang.getAsset("A8"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                new Lang.StepField(
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A1"),
                    lang.getAsset("A2"),
                    lang.getAsset("A8").getField("a1")),
                new Lang.StepTransitive(
                    lang.getAsset("A2"),
                    lang.getAsset("A2"),
                    lang.getAsset("A1"),
                    lang.getAsset("A1"),
                    new Lang.StepField(
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A1").getField("a1Sub")))),
            new Lang.StepAttackStep(
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A1").getAttackStep("a1Exist2")));
    assertLangStepExpr(step, parentSteps.get(0));

    // Auto-generated parent step 2
    step =
        new Lang.StepCollect(
            lang.getAsset("A8"),
            lang.getAsset("A8"),
            null,
            null,
            new Lang.StepCall(
                lang.getAsset("A8"),
                lang.getAsset("A8"),
                lang.getAsset("A7"),
                lang.getAsset("A7"),
                "reversea1A7"),
            new Lang.StepAttackStep(
                lang.getAsset("A7"),
                lang.getAsset("A7"),
                lang.getAsset("A7").getAttackStep("a7Attack")));
    assertLangStepExpr(step, parentSteps.get(1));
  }

  private static void assertAssetA9(Lang lang) {
    var asset = assertGetLangAsset(lang, "A9", false, "C3", null, Map.of());
    // Check fields
    assertEquals(0, asset.getFields().size());
    // Check attack steps
    assertEquals(0, asset.getAttackSteps().size());
  }

  private static void assertLinks(Lang lang) {
    assertEquals(8, lang.getLinks().size());
    assertLangLink(lang, "A1", "a4", "A4", "a1", 0, "L1", Map.of());
    assertLangLink(lang, "A5", "a6", "A6", "a5", 1, "L2", Map.of());
    assertLangLink(lang, "A1", "a1Sub", "A1", "a1Super", 2, "L3", Map.of());
    assertLangLink(lang, "A3", "a6", "A6", "a3", 3, "L3", Map.of());
    assertLangLink(lang, "A7", "a1", "A1", "a7", 4, "L3", Map.of());
    assertLangLink(lang, "A8", "a1", "A1", "a8", 5, "L4", Map.of());
    assertLangLink(lang, "A8", "a4", "A4", "a8", 6, "L4", Map.of());
    assertLangLink(lang, "A8", "a8Super", "A8", "a8Sub", 7, "L4", Map.of());
  }
}

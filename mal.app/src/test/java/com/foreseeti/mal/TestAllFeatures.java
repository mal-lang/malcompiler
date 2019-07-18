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

import static com.foreseeti.mal.AssertAST.assertAnalyzeClassPath;
import static com.foreseeti.mal.AssertAST.assertAssociation;
import static com.foreseeti.mal.AssertAST.assertCategory;
import static com.foreseeti.mal.AssertAST.assertDefine;
import static com.foreseeti.mal.AssertAST.assertGetASTClassPath;
import static com.foreseeti.mal.AssertLang.assertGetLangAsset;
import static com.foreseeti.mal.AssertLang.assertGetLangAttackStep;
import static com.foreseeti.mal.AssertLang.assertGetLangClassPath;
import static com.foreseeti.mal.AssertLang.assertLangCIA;
import static com.foreseeti.mal.AssertLang.assertLangCategory;
import static com.foreseeti.mal.AssertLang.assertLangDefines;
import static com.foreseeti.mal.AssertLang.assertLangField;
import static com.foreseeti.mal.AssertLang.assertLangLink;
import static com.foreseeti.mal.AssertLang.assertLangStepExpr;
import static com.foreseeti.mal.AssertLang.assertLangTTC;
import static com.foreseeti.mal.AssertToken.assertTokens;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class TestAllFeatures extends MalTest {
  @Test
  public void testLexerAllFeatures() {
    Token[] tokens = {
      new Token(TokenType.HASH, "all-features.mal", 6, 1),
      new Token(TokenType.ID, "all-features.mal", 6, 2, "id"),
      new Token(TokenType.COLON, "all-features.mal", 6, 4),
      new Token(TokenType.STRING, "all-features.mal", 6, 6, "all-features"),
      new Token(TokenType.HASH, "all-features.mal", 7, 1),
      new Token(TokenType.ID, "all-features.mal", 7, 2, "version"),
      new Token(TokenType.COLON, "all-features.mal", 7, 9),
      new Token(TokenType.STRING, "all-features.mal", 7, 11, "0.0.1"),
      new Token(TokenType.INCLUDE, "all-features.mal", 9, 1),
      new Token(TokenType.STRING, "all-features.mal", 9, 9, "core.mal"),
      new Token(TokenType.INCLUDE, "all-features.mal", 10, 1),
      new Token(TokenType.STRING, "all-features.mal", 10, 9, "subdir/subincluded.mal"),
      new Token(TokenType.EOF, "all-features.mal", 11, 1)
    };
    assertTokens(tokens, "all-features/all-features.mal");
  }

  @Test
  public void testLexerCore() {
    Token[] tokens = {
      new Token(TokenType.CATEGORY, "core.mal", 1, 1),
      new Token(TokenType.ID, "core.mal", 1, 10, "C1"),
      new Token(TokenType.INFO, "core.mal", 2, 3),
      new Token(TokenType.COLON, "core.mal", 2, 7),
      new Token(TokenType.STRING, "core.mal", 2, 9, "This is C1"),
      new Token(TokenType.ASSUMPTIONS, "core.mal", 3, 3),
      new Token(TokenType.COLON, "core.mal", 3, 14),
      new Token(TokenType.STRING, "core.mal", 3, 16, "None for C1"),
      new Token(TokenType.RATIONALE, "core.mal", 4, 3),
      new Token(TokenType.COLON, "core.mal", 4, 12),
      new Token(TokenType.STRING, "core.mal", 4, 14, "Reasoning for C1"),
      new Token(TokenType.LCURLY, "core.mal", 5, 1),
      new Token(TokenType.RCURLY, "core.mal", 7, 1),
      new Token(TokenType.CATEGORY, "core.mal", 9, 1),
      new Token(TokenType.ID, "core.mal", 9, 10, "C1"),
      new Token(TokenType.LCURLY, "core.mal", 9, 13),
      new Token(TokenType.ASSET, "core.mal", 10, 3),
      new Token(TokenType.ID, "core.mal", 10, 9, "A1"),
      new Token(TokenType.LCURLY, "core.mal", 11, 3),
      new Token(TokenType.ANY, "core.mal", 12, 5),
      new Token(TokenType.ID, "core.mal", 12, 7, "a1Attack1"),
      new Token(TokenType.LCURLY, "core.mal", 12, 17),
      new Token(TokenType.C, "core.mal", 12, 18),
      new Token(TokenType.RCURLY, "core.mal", 12, 19),
      new Token(TokenType.INFO, "core.mal", 13, 7),
      new Token(TokenType.COLON, "core.mal", 13, 11),
      new Token(TokenType.STRING, "core.mal", 13, 13, "This is a1Attack1"),
      new Token(TokenType.ASSUMPTIONS, "core.mal", 14, 7),
      new Token(TokenType.COLON, "core.mal", 14, 18),
      new Token(TokenType.STRING, "core.mal", 14, 20, "None for a1Attack1"),
      new Token(TokenType.RATIONALE, "core.mal", 15, 7),
      new Token(TokenType.COLON, "core.mal", 15, 16),
      new Token(TokenType.STRING, "core.mal", 15, 18, "Reasoning for a1Attack1"),
      new Token(TokenType.ALL, "core.mal", 17, 5),
      new Token(TokenType.ID, "core.mal", 17, 7, "a1Attack2"),
      new Token(TokenType.LCURLY, "core.mal", 17, 17),
      new Token(TokenType.I, "core.mal", 17, 18),
      new Token(TokenType.COMMA, "core.mal", 17, 19),
      new Token(TokenType.C, "core.mal", 17, 21),
      new Token(TokenType.RCURLY, "core.mal", 17, 22),
      new Token(TokenType.LBRACKET, "core.mal", 17, 24),
      new Token(TokenType.RBRACKET, "core.mal", 17, 25),
      new Token(TokenType.INFO, "core.mal", 18, 7),
      new Token(TokenType.COLON, "core.mal", 18, 11),
      new Token(TokenType.STRING, "core.mal", 18, 13, "This is a1Attack2"),
      new Token(TokenType.OVERRIDE, "core.mal", 19, 7),
      new Token(TokenType.ID, "core.mal", 19, 10, "a1Sub"),
      new Token(TokenType.LBRACKET, "core.mal", 19, 15),
      new Token(TokenType.ID, "core.mal", 19, 16, "A1"),
      new Token(TokenType.RBRACKET, "core.mal", 19, 18),
      new Token(TokenType.STAR, "core.mal", 19, 19),
      new Token(TokenType.DOT, "core.mal", 19, 20),
      new Token(TokenType.ID, "core.mal", 19, 21, "a1Attack1"),
      new Token(TokenType.HASH, "core.mal", 20, 5),
      new Token(TokenType.ID, "core.mal", 20, 7, "a1Defense1"),
      new Token(TokenType.LBRACKET, "core.mal", 20, 18),
      new Token(TokenType.ID, "core.mal", 20, 19, "Bernoulli"),
      new Token(TokenType.LPAREN, "core.mal", 20, 28),
      new Token(TokenType.FLOAT, "core.mal", 20, 29, 0.5),
      new Token(TokenType.RPAREN, "core.mal", 20, 32),
      new Token(TokenType.RBRACKET, "core.mal", 20, 33),
      new Token(TokenType.RATIONALE, "core.mal", 21, 7),
      new Token(TokenType.COLON, "core.mal", 21, 16),
      new Token(TokenType.STRING, "core.mal", 21, 18, "Reasoning for a1Defense"),
      new Token(TokenType.HASH, "core.mal", 23, 5),
      new Token(TokenType.ID, "core.mal", 23, 7, "a1Defense2"),
      new Token(TokenType.LBRACKET, "core.mal", 23, 18),
      new Token(TokenType.ID, "core.mal", 23, 19, "Disabled"),
      new Token(TokenType.RBRACKET, "core.mal", 23, 27),
      new Token(TokenType.OVERRIDE, "core.mal", 24, 7),
      new Token(TokenType.ID, "core.mal", 24, 10, "a1Attack2"),
      new Token(TokenType.EXIST, "core.mal", 25, 5),
      new Token(TokenType.ID, "core.mal", 25, 7, "a1Exist1"),
      new Token(TokenType.REQUIRE, "core.mal", 26, 7),
      new Token(TokenType.ID, "core.mal", 26, 10, "a1Sub"),
      new Token(TokenType.EXIST, "core.mal", 28, 5),
      new Token(TokenType.ID, "core.mal", 28, 7, "a1Exist2"),
      new Token(TokenType.ASSUMPTIONS, "core.mal", 29, 7),
      new Token(TokenType.COLON, "core.mal", 29, 18),
      new Token(TokenType.STRING, "core.mal", 29, 20, "None for a1Exist2"),
      new Token(TokenType.REQUIRE, "core.mal", 30, 7),
      new Token(TokenType.ID, "core.mal", 30, 10, "a7"),
      new Token(TokenType.COMMA, "core.mal", 30, 12),
      new Token(TokenType.ID, "core.mal", 30, 14, "a1Super"),
      new Token(TokenType.DOT, "core.mal", 30, 21),
      new Token(TokenType.ID, "core.mal", 30, 22, "a1Super"),
      new Token(TokenType.DOT, "core.mal", 30, 29),
      new Token(TokenType.ID, "core.mal", 30, 30, "a7"),
      new Token(TokenType.OVERRIDE, "core.mal", 31, 7),
      new Token(TokenType.LPAREN, "core.mal", 31, 10),
      new Token(TokenType.ID, "core.mal", 31, 11, "a1Super"),
      new Token(TokenType.STAR, "core.mal", 31, 18),
      new Token(TokenType.RPAREN, "core.mal", 31, 19),
      new Token(TokenType.LBRACKET, "core.mal", 31, 20),
      new Token(TokenType.ID, "core.mal", 31, 21, "A2"),
      new Token(TokenType.RBRACKET, "core.mal", 31, 23),
      new Token(TokenType.DOT, "core.mal", 31, 24),
      new Token(TokenType.LPAREN, "core.mal", 31, 25),
      new Token(TokenType.ID, "core.mal", 31, 26, "a8"),
      new Token(TokenType.DOT, "core.mal", 31, 28),
      new Token(TokenType.ID, "core.mal", 31, 29, "destroy"),
      new Token(TokenType.RPAREN, "core.mal", 31, 36),
      new Token(TokenType.NOTEXIST, "core.mal", 32, 5),
      new Token(TokenType.ID, "core.mal", 32, 8, "a1NotExist1"),
      new Token(TokenType.REQUIRE, "core.mal", 33, 7),
      new Token(TokenType.ID, "core.mal", 33, 10, "a8"),
      new Token(TokenType.DOT, "core.mal", 33, 12),
      new Token(TokenType.ID, "core.mal", 33, 13, "a8Sub"),
      new Token(TokenType.STAR, "core.mal", 33, 18),
      new Token(TokenType.DOT, "core.mal", 33, 19),
      new Token(TokenType.ID, "core.mal", 33, 20, "a1"),
      new Token(TokenType.LBRACKET, "core.mal", 33, 22),
      new Token(TokenType.ID, "core.mal", 33, 23, "A7"),
      new Token(TokenType.RBRACKET, "core.mal", 33, 25),
      new Token(TokenType.NOTEXIST, "core.mal", 35, 5),
      new Token(TokenType.ID, "core.mal", 35, 8, "a1NotExist2"),
      new Token(TokenType.REQUIRE, "core.mal", 36, 7),
      new Token(TokenType.ID, "core.mal", 36, 10, "a8"),
      new Token(TokenType.DOT, "core.mal", 36, 12),
      new Token(TokenType.ID, "core.mal", 36, 13, "a4"),
      new Token(TokenType.INTERSECT, "core.mal", 36, 16),
      new Token(TokenType.ID, "core.mal", 36, 19, "a4"),
      new Token(TokenType.OVERRIDE, "core.mal", 37, 7),
      new Token(TokenType.ID, "core.mal", 37, 10, "a4"),
      new Token(TokenType.DOT, "core.mal", 37, 12),
      new Token(TokenType.ID, "core.mal", 37, 13, "a"),
      new Token(TokenType.RCURLY, "core.mal", 38, 3),
      new Token(TokenType.ASSET, "core.mal", 40, 3),
      new Token(TokenType.ID, "core.mal", 40, 9, "A2"),
      new Token(TokenType.EXTENDS, "core.mal", 40, 12),
      new Token(TokenType.ID, "core.mal", 40, 20, "A1"),
      new Token(TokenType.LCURLY, "core.mal", 40, 23),
      new Token(TokenType.ANY, "core.mal", 42, 5),
      new Token(TokenType.ID, "core.mal", 42, 7, "a1Attack1"),
      new Token(TokenType.LCURLY, "core.mal", 42, 17),
      new Token(TokenType.RCURLY, "core.mal", 42, 18),
      new Token(TokenType.OVERRIDE, "core.mal", 43, 7),
      new Token(TokenType.ID, "core.mal", 43, 10, "a1Super"),
      new Token(TokenType.DOT, "core.mal", 43, 17),
      new Token(TokenType.ID, "core.mal", 43, 18, "a1Attack1"),
      new Token(TokenType.ALL, "core.mal", 45, 5),
      new Token(TokenType.ID, "core.mal", 45, 7, "a1Attack2"),
      new Token(TokenType.LCURLY, "core.mal", 45, 17),
      new Token(TokenType.C, "core.mal", 45, 18),
      new Token(TokenType.COMMA, "core.mal", 45, 19),
      new Token(TokenType.I, "core.mal", 45, 21),
      new Token(TokenType.COMMA, "core.mal", 45, 22),
      new Token(TokenType.A, "core.mal", 45, 24),
      new Token(TokenType.RCURLY, "core.mal", 45, 25),
      new Token(TokenType.INHERIT, "core.mal", 46, 7),
      new Token(TokenType.ID, "core.mal", 46, 10, "a1Super"),
      new Token(TokenType.DOT, "core.mal", 46, 17),
      new Token(TokenType.ID, "core.mal", 46, 18, "a1Attack1"),
      new Token(TokenType.HASH, "core.mal", 48, 5),
      new Token(TokenType.ID, "core.mal", 48, 7, "a1Defense1"),
      new Token(TokenType.LBRACKET, "core.mal", 48, 18),
      new Token(TokenType.ID, "core.mal", 48, 19, "Enabled"),
      new Token(TokenType.RBRACKET, "core.mal", 48, 26),
      new Token(TokenType.OVERRIDE, "core.mal", 49, 7),
      new Token(TokenType.ID, "core.mal", 49, 10, "a1Sub"),
      new Token(TokenType.DOT, "core.mal", 49, 15),
      new Token(TokenType.ID, "core.mal", 49, 16, "a1Sub"),
      new Token(TokenType.DOT, "core.mal", 49, 21),
      new Token(TokenType.ID, "core.mal", 49, 22, "a1Attack2"),
      new Token(TokenType.HASH, "core.mal", 51, 5),
      new Token(TokenType.ID, "core.mal", 51, 7, "a1Defense2"),
      new Token(TokenType.INHERIT, "core.mal", 52, 7),
      new Token(TokenType.ID, "core.mal", 52, 10, "a1Attack2"),
      new Token(TokenType.EXIST, "core.mal", 54, 5),
      new Token(TokenType.ID, "core.mal", 54, 7, "a1Exist2"),
      new Token(TokenType.REQUIRE, "core.mal", 55, 7),
      new Token(TokenType.ID, "core.mal", 55, 10, "a8"),
      new Token(TokenType.OVERRIDE, "core.mal", 56, 7),
      new Token(TokenType.ID, "core.mal", 56, 10, "a1Defense2"),
      new Token(TokenType.NOTEXIST, "core.mal", 58, 5),
      new Token(TokenType.ID, "core.mal", 58, 8, "a1NotExist1"),
      new Token(TokenType.REQUIRE, "core.mal", 59, 8),
      new Token(TokenType.ID, "core.mal", 59, 11, "a8"),
      new Token(TokenType.INHERIT, "core.mal", 60, 8),
      new Token(TokenType.ID, "core.mal", 60, 11, "a1Defense1"),
      new Token(TokenType.RCURLY, "core.mal", 61, 3),
      new Token(TokenType.ABSTRACT, "core.mal", 63, 3),
      new Token(TokenType.ASSET, "core.mal", 63, 12),
      new Token(TokenType.ID, "core.mal", 63, 18, "A3"),
      new Token(TokenType.EXTENDS, "core.mal", 63, 21),
      new Token(TokenType.ID, "core.mal", 63, 29, "A1"),
      new Token(TokenType.INFO, "core.mal", 64, 5),
      new Token(TokenType.COLON, "core.mal", 64, 9),
      new Token(TokenType.STRING, "core.mal", 64, 11, "This is A3"),
      new Token(TokenType.LCURLY, "core.mal", 65, 3),
      new Token(TokenType.LET, "core.mal", 66, 5),
      new Token(TokenType.ID, "core.mal", 66, 9, "unused"),
      new Token(TokenType.ASSIGN, "core.mal", 66, 16),
      new Token(TokenType.ID, "core.mal", 66, 18, "a1Sub"),
      new Token(TokenType.ANY, "core.mal", 67, 5),
      new Token(TokenType.ID, "core.mal", 67, 7, "a3Attack"),
      new Token(TokenType.LCURLY, "core.mal", 67, 16),
      new Token(TokenType.A, "core.mal", 67, 17),
      new Token(TokenType.COMMA, "core.mal", 67, 18),
      new Token(TokenType.I, "core.mal", 67, 20),
      new Token(TokenType.COMMA, "core.mal", 67, 21),
      new Token(TokenType.C, "core.mal", 67, 23),
      new Token(TokenType.RCURLY, "core.mal", 67, 24),
      new Token(TokenType.LET, "core.mal", 68, 5),
      new Token(TokenType.ID, "core.mal", 68, 9, "used"),
      new Token(TokenType.ASSIGN, "core.mal", 68, 14),
      new Token(TokenType.ID, "core.mal", 68, 16, "a1Sub"),
      new Token(TokenType.LBRACKET, "core.mal", 68, 21),
      new Token(TokenType.ID, "core.mal", 68, 22, "A2"),
      new Token(TokenType.RBRACKET, "core.mal", 68, 24),
      new Token(TokenType.LET, "core.mal", 70, 5),
      new Token(TokenType.ID, "core.mal", 70, 9, "V1"),
      new Token(TokenType.ASSIGN, "core.mal", 70, 12),
      new Token(TokenType.ID, "core.mal", 70, 14, "used"),
      new Token(TokenType.LET, "core.mal", 71, 5),
      new Token(TokenType.ID, "core.mal", 71, 9, "V2"),
      new Token(TokenType.ASSIGN, "core.mal", 71, 12),
      new Token(TokenType.ID, "core.mal", 71, 14, "V1"),
      new Token(TokenType.ALL, "core.mal", 72, 5),
      new Token(TokenType.ID, "core.mal", 72, 7, "AT"),
      new Token(TokenType.OVERRIDE, "core.mal", 73, 7),
      new Token(TokenType.LET, "core.mal", 73, 10),
      new Token(TokenType.ID, "core.mal", 73, 14, "V1"),
      new Token(TokenType.ASSIGN, "core.mal", 73, 17),
      new Token(TokenType.ID, "core.mal", 73, 19, "V2"),
      new Token(TokenType.COMMA, "core.mal", 73, 21),
      new Token(TokenType.ID, "core.mal", 74, 10, "V1"),
      new Token(TokenType.DOT, "core.mal", 74, 12),
      new Token(TokenType.ID, "core.mal", 74, 13, "a1Attack1"),
      new Token(TokenType.RCURLY, "core.mal", 75, 3),
      new Token(TokenType.RCURLY, "core.mal", 76, 1),
      new Token(TokenType.CATEGORY, "core.mal", 78, 1),
      new Token(TokenType.ID, "core.mal", 78, 10, "C2"),
      new Token(TokenType.LCURLY, "core.mal", 78, 13),
      new Token(TokenType.RCURLY, "core.mal", 78, 44),
      new Token(TokenType.CATEGORY, "core.mal", 80, 1),
      new Token(TokenType.ID, "core.mal", 80, 10, "C2"),
      new Token(TokenType.ASSUMPTIONS, "core.mal", 81, 3),
      new Token(TokenType.COLON, "core.mal", 81, 14),
      new Token(TokenType.STRING, "core.mal", 81, 16, "None for C2"),
      new Token(TokenType.LCURLY, "core.mal", 82, 1),
      new Token(TokenType.ABSTRACT, "core.mal", 83, 3),
      new Token(TokenType.ASSET, "core.mal", 83, 12),
      new Token(TokenType.ID, "core.mal", 83, 18, "A4"),
      new Token(TokenType.ASSUMPTIONS, "core.mal", 84, 5),
      new Token(TokenType.COLON, "core.mal", 84, 16),
      new Token(TokenType.STRING, "core.mal", 84, 18, "None for A4"),
      new Token(TokenType.RATIONALE, "core.mal", 85, 5),
      new Token(TokenType.COLON, "core.mal", 85, 14),
      new Token(TokenType.STRING, "core.mal", 85, 16, "Reasoning for A4"),
      new Token(TokenType.INFO, "core.mal", 86, 5),
      new Token(TokenType.COLON, "core.mal", 86, 9),
      new Token(TokenType.STRING, "core.mal", 86, 11, "This is A4"),
      new Token(TokenType.LCURLY, "core.mal", 87, 3),
      new Token(TokenType.LET, "core.mal", 88, 5),
      new Token(TokenType.ID, "core.mal", 88, 9, "var"),
      new Token(TokenType.ASSIGN, "core.mal", 88, 13),
      new Token(TokenType.ID, "core.mal", 88, 15, "a1"),
      new Token(TokenType.DOT, "core.mal", 88, 17),
      new Token(TokenType.ID, "core.mal", 88, 18, "a1Sub"),
      new Token(TokenType.STAR, "core.mal", 88, 23),
      new Token(TokenType.ANY, "core.mal", 89, 5),
      new Token(TokenType.ID, "core.mal", 89, 7, "a"),
      new Token(TokenType.OVERRIDE, "core.mal", 90, 7),
      new Token(TokenType.LPAREN, "core.mal", 90, 10),
      new Token(TokenType.ID, "core.mal", 90, 11, "var"),
      new Token(TokenType.UNION, "core.mal", 90, 15),
      new Token(TokenType.ID, "core.mal", 90, 18, "var2"),
      new Token(TokenType.RPAREN, "core.mal", 90, 22),
      new Token(TokenType.DOT, "core.mal", 90, 23),
      new Token(TokenType.ID, "core.mal", 90, 24, "a1Attack1"),
      new Token(TokenType.COMMA, "core.mal", 90, 33),
      new Token(TokenType.LET, "core.mal", 92, 10),
      new Token(TokenType.ID, "core.mal", 92, 14, "var"),
      new Token(TokenType.ASSIGN, "core.mal", 92, 18),
      new Token(TokenType.ID, "core.mal", 92, 20, "a1"),
      new Token(TokenType.LBRACKET, "core.mal", 92, 22),
      new Token(TokenType.ID, "core.mal", 92, 23, "A2"),
      new Token(TokenType.RBRACKET, "core.mal", 92, 25),
      new Token(TokenType.LET, "core.mal", 94, 5),
      new Token(TokenType.ID, "core.mal", 94, 9, "var2"),
      new Token(TokenType.ASSIGN, "core.mal", 94, 14),
      new Token(TokenType.ID, "core.mal", 94, 16, "var"),
      new Token(TokenType.LBRACKET, "core.mal", 94, 19),
      new Token(TokenType.ID, "core.mal", 94, 20, "A3"),
      new Token(TokenType.RBRACKET, "core.mal", 94, 22),
      new Token(TokenType.RCURLY, "core.mal", 95, 3),
      new Token(TokenType.RCURLY, "core.mal", 96, 1),
      new Token(TokenType.CATEGORY, "core.mal", 98, 1),
      new Token(TokenType.ID, "core.mal", 98, 10, "C2"),
      new Token(TokenType.RATIONALE, "core.mal", 99, 3),
      new Token(TokenType.COLON, "core.mal", 99, 12),
      new Token(TokenType.STRING, "core.mal", 99, 14, "Reasoning for C2"),
      new Token(TokenType.LCURLY, "core.mal", 100, 1),
      new Token(TokenType.ASSET, "core.mal", 101, 3),
      new Token(TokenType.ID, "core.mal", 101, 9, "A5"),
      new Token(TokenType.EXTENDS, "core.mal", 101, 12),
      new Token(TokenType.ID, "core.mal", 101, 20, "A4"),
      new Token(TokenType.ASSUMPTIONS, "core.mal", 102, 5),
      new Token(TokenType.COLON, "core.mal", 102, 16),
      new Token(TokenType.STRING, "core.mal", 102, 18, "None for A5"),
      new Token(TokenType.LCURLY, "core.mal", 103, 3),
      new Token(TokenType.RCURLY, "core.mal", 103, 31),
      new Token(TokenType.ABSTRACT, "core.mal", 105, 3),
      new Token(TokenType.ASSET, "core.mal", 105, 12),
      new Token(TokenType.ID, "core.mal", 105, 18, "A6"),
      new Token(TokenType.EXTENDS, "core.mal", 105, 21),
      new Token(TokenType.ID, "core.mal", 105, 29, "A4"),
      new Token(TokenType.RATIONALE, "core.mal", 106, 5),
      new Token(TokenType.COLON, "core.mal", 106, 14),
      new Token(TokenType.STRING, "core.mal", 106, 16, "Reasoning for A6"),
      new Token(TokenType.LCURLY, "core.mal", 107, 3),
      new Token(TokenType.RCURLY, "core.mal", 109, 3),
      new Token(TokenType.RCURLY, "core.mal", 110, 1),
      new Token(TokenType.CATEGORY, "core.mal", 112, 1),
      new Token(TokenType.ID, "core.mal", 112, 10, "C3"),
      new Token(TokenType.LCURLY, "core.mal", 112, 13),
      new Token(TokenType.ASSET, "core.mal", 113, 3),
      new Token(TokenType.ID, "core.mal", 113, 9, "A7"),
      new Token(TokenType.EXTENDS, "core.mal", 113, 12),
      new Token(TokenType.ID, "core.mal", 113, 20, "A3"),
      new Token(TokenType.LCURLY, "core.mal", 113, 23),
      new Token(TokenType.ANY, "core.mal", 114, 5),
      new Token(TokenType.ID, "core.mal", 114, 7, "a7Attack"),
      new Token(TokenType.OVERRIDE, "core.mal", 115, 7),
      new Token(TokenType.ID, "core.mal", 115, 10, "a3Attack"),
      new Token(TokenType.COMMA, "core.mal", 115, 18),
      new Token(TokenType.ID, "core.mal", 116, 10, "otherAttack"),
      new Token(TokenType.COMMA, "core.mal", 116, 21),
      new Token(TokenType.ID, "core.mal", 117, 10, "a1"),
      new Token(TokenType.DOT, "core.mal", 117, 12),
      new Token(TokenType.ID, "core.mal", 117, 13, "destroy"),
      new Token(TokenType.ANY, "core.mal", 118, 5),
      new Token(TokenType.ID, "core.mal", 118, 7, "otherAttack"),
      new Token(TokenType.LET, "core.mal", 120, 5),
      new Token(TokenType.ID, "core.mal", 120, 9, "a3Attack"),
      new Token(TokenType.ASSIGN, "core.mal", 120, 18),
      new Token(TokenType.ID, "core.mal", 120, 20, "a1Attack1"),
      new Token(TokenType.LET, "core.mal", 122, 5),
      new Token(TokenType.ID, "core.mal", 122, 9, "otherAttack"),
      new Token(TokenType.ASSIGN, "core.mal", 122, 21),
      new Token(TokenType.ID, "core.mal", 122, 23, "a7Attack"),
      new Token(TokenType.LET, "core.mal", 124, 5),
      new Token(TokenType.ID, "core.mal", 124, 9, "a6"),
      new Token(TokenType.ASSIGN, "core.mal", 124, 12),
      new Token(TokenType.ID, "core.mal", 124, 14, "a1Super"),
      new Token(TokenType.DOT, "core.mal", 124, 21),
      new Token(TokenType.ID, "core.mal", 124, 22, "a8"),
      new Token(TokenType.LET, "core.mal", 126, 5),
      new Token(TokenType.ID, "core.mal", 126, 9, "a1"),
      new Token(TokenType.ASSIGN, "core.mal", 126, 12),
      new Token(TokenType.ID, "core.mal", 126, 14, "a6"),
      new Token(TokenType.DOT, "core.mal", 126, 16),
      new Token(TokenType.ID, "core.mal", 126, 17, "a8Super"),
      new Token(TokenType.RCURLY, "core.mal", 127, 3),
      new Token(TokenType.ASSET, "core.mal", 129, 3),
      new Token(TokenType.ID, "core.mal", 129, 9, "A8"),
      new Token(TokenType.LCURLY, "core.mal", 129, 12),
      new Token(TokenType.ALL, "core.mal", 130, 5),
      new Token(TokenType.ID, "core.mal", 130, 7, "destroy"),
      new Token(TokenType.LCURLY, "core.mal", 130, 15),
      new Token(TokenType.C, "core.mal", 130, 16),
      new Token(TokenType.COMMA, "core.mal", 130, 17),
      new Token(TokenType.I, "core.mal", 130, 19),
      new Token(TokenType.COMMA, "core.mal", 130, 20),
      new Token(TokenType.A, "core.mal", 130, 22),
      new Token(TokenType.RCURLY, "core.mal", 130, 23),
      new Token(TokenType.LBRACKET, "core.mal", 130, 25),
      new Token(TokenType.ID, "core.mal", 130, 26, "Exponential"),
      new Token(TokenType.LPAREN, "core.mal", 130, 37),
      new Token(TokenType.INT, "core.mal", 130, 38, 5),
      new Token(TokenType.RPAREN, "core.mal", 130, 39),
      new Token(TokenType.RBRACKET, "core.mal", 130, 40),
      new Token(TokenType.RCURLY, "core.mal", 131, 3),
      new Token(TokenType.ASSET, "core.mal", 133, 3),
      new Token(TokenType.ID, "core.mal", 133, 9, "A9"),
      new Token(TokenType.LCURLY, "core.mal", 133, 12),
      new Token(TokenType.RCURLY, "core.mal", 136, 3),
      new Token(TokenType.RCURLY, "core.mal", 137, 1),

      // 139: associations { /* No associations here */ }
      new Token(TokenType.ASSOCIATIONS, "core.mal", 139, 1),
      new Token(TokenType.LCURLY, "core.mal", 139, 14),
      new Token(TokenType.RCURLY, "core.mal", 139, 43),

      // 141: associations {
      new Token(TokenType.ASSOCIATIONS, "core.mal", 141, 1),
      new Token(TokenType.LCURLY, "core.mal", 141, 14),

      // 143:   A1 [a1]      1    <-- L1 --> 1..* [a4]    A4
      new Token(TokenType.ID, "core.mal", 143, 3, "A1"),
      new Token(TokenType.LBRACKET, "core.mal", 143, 6),
      new Token(TokenType.ID, "core.mal", 143, 7, "a1"),
      new Token(TokenType.RBRACKET, "core.mal", 143, 9),
      new Token(TokenType.INT, "core.mal", 143, 16, 1),
      new Token(TokenType.LARROW, "core.mal", 143, 21),
      new Token(TokenType.ID, "core.mal", 143, 25, "L1"),
      new Token(TokenType.RARROW, "core.mal", 143, 28),
      new Token(TokenType.INT, "core.mal", 143, 32, 1),
      new Token(TokenType.RANGE, "core.mal", 143, 33),
      new Token(TokenType.STAR, "core.mal", 143, 35),
      new Token(TokenType.LBRACKET, "core.mal", 143, 37),
      new Token(TokenType.ID, "core.mal", 143, 38, "a4"),
      new Token(TokenType.RBRACKET, "core.mal", 143, 40),
      new Token(TokenType.ID, "core.mal", 143, 45, "A4"),

      // 144:   A5 [a5]      1..1 <-- L2 --> 0..* [a6]    A6
      new Token(TokenType.ID, "core.mal", 144, 3, "A5"),
      new Token(TokenType.LBRACKET, "core.mal", 144, 6),
      new Token(TokenType.ID, "core.mal", 144, 7, "a5"),
      new Token(TokenType.RBRACKET, "core.mal", 144, 9),
      new Token(TokenType.INT, "core.mal", 144, 16, 1),
      new Token(TokenType.RANGE, "core.mal", 144, 17),
      new Token(TokenType.INT, "core.mal", 144, 19, 1),
      new Token(TokenType.LARROW, "core.mal", 144, 21),
      new Token(TokenType.ID, "core.mal", 144, 25, "L2"),
      new Token(TokenType.RARROW, "core.mal", 144, 28),
      new Token(TokenType.INT, "core.mal", 144, 32, 0),
      new Token(TokenType.RANGE, "core.mal", 144, 33),
      new Token(TokenType.STAR, "core.mal", 144, 35),
      new Token(TokenType.LBRACKET, "core.mal", 144, 37),
      new Token(TokenType.ID, "core.mal", 144, 38, "a6"),
      new Token(TokenType.RBRACKET, "core.mal", 144, 40),
      new Token(TokenType.ID, "core.mal", 144, 45, "A6"),

      // 145: }
      new Token(TokenType.RCURLY, "core.mal", 145, 1),

      // 147: associations {
      new Token(TokenType.ASSOCIATIONS, "core.mal", 147, 1),
      new Token(TokenType.LCURLY, "core.mal", 147, 14),

      // 149:   A1 [a1Super] 0..1 <-- L3 --> *    [a1Sub]   A1
      new Token(TokenType.ID, "core.mal", 149, 3, "A1"),
      new Token(TokenType.LBRACKET, "core.mal", 149, 6),
      new Token(TokenType.ID, "core.mal", 149, 7, "a1Super"),
      new Token(TokenType.RBRACKET, "core.mal", 149, 14),
      new Token(TokenType.INT, "core.mal", 149, 16, 0),
      new Token(TokenType.RANGE, "core.mal", 149, 17),
      new Token(TokenType.INT, "core.mal", 149, 19, 1),
      new Token(TokenType.LARROW, "core.mal", 149, 21),
      new Token(TokenType.ID, "core.mal", 149, 25, "L3"),
      new Token(TokenType.RARROW, "core.mal", 149, 28),
      new Token(TokenType.STAR, "core.mal", 149, 32),
      new Token(TokenType.LBRACKET, "core.mal", 149, 37),
      new Token(TokenType.ID, "core.mal", 149, 38, "a1Sub"),
      new Token(TokenType.RBRACKET, "core.mal", 149, 43),
      new Token(TokenType.ID, "core.mal", 149, 47, "A1"),

      // 150:   A3 [a3]      *    <-- L3 --> *    [a6]      A6
      new Token(TokenType.ID, "core.mal", 150, 3, "A3"),
      new Token(TokenType.LBRACKET, "core.mal", 150, 6),
      new Token(TokenType.ID, "core.mal", 150, 7, "a3"),
      new Token(TokenType.RBRACKET, "core.mal", 150, 9),
      new Token(TokenType.STAR, "core.mal", 150, 16),
      new Token(TokenType.LARROW, "core.mal", 150, 21),
      new Token(TokenType.ID, "core.mal", 150, 25, "L3"),
      new Token(TokenType.RARROW, "core.mal", 150, 28),
      new Token(TokenType.STAR, "core.mal", 150, 32),
      new Token(TokenType.LBRACKET, "core.mal", 150, 37),
      new Token(TokenType.ID, "core.mal", 150, 38, "a6"),
      new Token(TokenType.RBRACKET, "core.mal", 150, 40),
      new Token(TokenType.ID, "core.mal", 150, 47, "A6"),

      // 151:   A7 [a7]      0..1 <-- L3 --> 1    [a1]      A1
      new Token(TokenType.ID, "core.mal", 151, 3, "A7"),
      new Token(TokenType.LBRACKET, "core.mal", 151, 6),
      new Token(TokenType.ID, "core.mal", 151, 7, "a7"),
      new Token(TokenType.RBRACKET, "core.mal", 151, 9),
      new Token(TokenType.INT, "core.mal", 151, 16, 0),
      new Token(TokenType.RANGE, "core.mal", 151, 17),
      new Token(TokenType.INT, "core.mal", 151, 19, 1),
      new Token(TokenType.LARROW, "core.mal", 151, 21),
      new Token(TokenType.ID, "core.mal", 151, 25, "L3"),
      new Token(TokenType.RARROW, "core.mal", 151, 28),
      new Token(TokenType.INT, "core.mal", 151, 32, 1),
      new Token(TokenType.LBRACKET, "core.mal", 151, 37),
      new Token(TokenType.ID, "core.mal", 151, 38, "a1"),
      new Token(TokenType.RBRACKET, "core.mal", 151, 40),
      new Token(TokenType.ID, "core.mal", 151, 47, "A1"),

      // 152:   A8 [a8]      *    <-- L4 --> *    [a1]      A1
      new Token(TokenType.ID, "core.mal", 152, 3, "A8"),
      new Token(TokenType.LBRACKET, "core.mal", 152, 6),
      new Token(TokenType.ID, "core.mal", 152, 7, "a8"),
      new Token(TokenType.RBRACKET, "core.mal", 152, 9),
      new Token(TokenType.STAR, "core.mal", 152, 16),
      new Token(TokenType.LARROW, "core.mal", 152, 21),
      new Token(TokenType.ID, "core.mal", 152, 25, "L4"),
      new Token(TokenType.RARROW, "core.mal", 152, 28),
      new Token(TokenType.STAR, "core.mal", 152, 32),
      new Token(TokenType.LBRACKET, "core.mal", 152, 37),
      new Token(TokenType.ID, "core.mal", 152, 38, "a1"),
      new Token(TokenType.RBRACKET, "core.mal", 152, 40),
      new Token(TokenType.ID, "core.mal", 152, 47, "A1"),

      // 153:   A8 [a8]      *    <-- L4 --> *    [a4]      A4
      new Token(TokenType.ID, "core.mal", 153, 3, "A8"),
      new Token(TokenType.LBRACKET, "core.mal", 153, 6),
      new Token(TokenType.ID, "core.mal", 153, 7, "a8"),
      new Token(TokenType.RBRACKET, "core.mal", 153, 9),
      new Token(TokenType.STAR, "core.mal", 153, 16),
      new Token(TokenType.LARROW, "core.mal", 153, 21),
      new Token(TokenType.ID, "core.mal", 153, 25, "L4"),
      new Token(TokenType.RARROW, "core.mal", 153, 28),
      new Token(TokenType.STAR, "core.mal", 153, 32),
      new Token(TokenType.LBRACKET, "core.mal", 153, 37),
      new Token(TokenType.ID, "core.mal", 153, 38, "a4"),
      new Token(TokenType.RBRACKET, "core.mal", 153, 40),
      new Token(TokenType.ID, "core.mal", 153, 47, "A4"),

      // 154:   A8 [a8Sub]   *    <-- L4 --> *    [a8Super] A8
      new Token(TokenType.ID, "core.mal", 154, 3, "A8"),
      new Token(TokenType.LBRACKET, "core.mal", 154, 6),
      new Token(TokenType.ID, "core.mal", 154, 7, "a8Sub"),
      new Token(TokenType.RBRACKET, "core.mal", 154, 12),
      new Token(TokenType.STAR, "core.mal", 154, 16),
      new Token(TokenType.LARROW, "core.mal", 154, 21),
      new Token(TokenType.ID, "core.mal", 154, 25, "L4"),
      new Token(TokenType.RARROW, "core.mal", 154, 28),
      new Token(TokenType.STAR, "core.mal", 154, 32),
      new Token(TokenType.LBRACKET, "core.mal", 154, 37),
      new Token(TokenType.ID, "core.mal", 154, 38, "a8Super"),
      new Token(TokenType.RBRACKET, "core.mal", 154, 45),
      new Token(TokenType.ID, "core.mal", 154, 47, "A8"),

      // 155: }
      new Token(TokenType.RCURLY, "core.mal", 155, 1),
      new Token(TokenType.EOF, "core.mal", 156, 1)
    };
    assertTokens(tokens, "all-features/core.mal");
  }

  @Test
  public void testLexerSubIncluded() {
    Token[] tokens = {
      new Token(TokenType.HASH, "subincluded.mal", 2, 2),
      new Token(TokenType.ID, "subincluded.mal", 3, 4, "empty"),
      new Token(TokenType.COLON, "subincluded.mal", 4, 10),
      new Token(TokenType.STRING, "subincluded.mal", 5, 12, ""),
      new Token(TokenType.INCLUDE, "subincluded.mal", 7, 2),
      new Token(TokenType.STRING, "subincluded.mal", 8, 10, "../included.mal"),
      new Token(TokenType.EOF, "subincluded.mal", 9, 1)
    };
    assertTokens(tokens, "all-features/subdir/subincluded.mal");
  }

  @Test
  public void testLexerIncluded() {
    Token[] tokens = {
      new Token(TokenType.HASH, "included.mal", 2, 2),
      new Token(TokenType.ID, "included.mal", 2, 4, "other"),
      new Token(TokenType.COLON, "included.mal", 2, 10),
      new Token(TokenType.STRING, "included.mal", 2, 12, "other"),
      new Token(TokenType.INCLUDE, "included.mal", 4, 2),
      new Token(TokenType.STRING, "included.mal", 4, 10, "all-features.mal"),
      new Token(TokenType.EOF, "included.mal", 6, 1)
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
            new Position("core.mal", 1, 1),
            new AST.ID(new Position("core.mal", 1, 10), "C1"),
            Arrays.asList(
                new AST.Meta(new Position("core.mal", 2, 3), AST.MetaType.INFO, "This is C1"),
                new AST.Meta(
                    new Position("core.mal", 3, 3), AST.MetaType.ASSUMPTIONS, "None for C1"),
                new AST.Meta(
                    new Position("core.mal", 4, 3), AST.MetaType.RATIONALE, "Reasoning for C1")),
            new ArrayList<AST.Asset>()),
        categories.get(0));
    assertCategory(
        new AST.Category(
            new Position("core.mal", 9, 1),
            new AST.ID(new Position("core.mal", 9, 10), "C1"),
            new ArrayList<AST.Meta>(),
            Arrays.asList(
                new AST.Asset(
                    new Position("core.mal", 10, 3),
                    false,
                    new AST.ID(new Position("core.mal", 10, 9), "A1"),
                    Optional.empty(),
                    new ArrayList<AST.Meta>(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("core.mal", 12, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("core.mal", 12, 7), "a1Attack1"),
                            Optional.of(Arrays.asList(AST.CIA.C)),
                            Optional.empty(),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position("core.mal", 13, 7),
                                    AST.MetaType.INFO,
                                    "This is a1Attack1"),
                                new AST.Meta(
                                    new Position("core.mal", 14, 7),
                                    AST.MetaType.ASSUMPTIONS,
                                    "None for a1Attack1"),
                                new AST.Meta(
                                    new Position("core.mal", 15, 7),
                                    AST.MetaType.RATIONALE,
                                    "Reasoning for a1Attack1")),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("core.mal", 17, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("core.mal", 17, 7), "a1Attack2"),
                            Optional.of(Arrays.asList(AST.CIA.I, AST.CIA.C)),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("core.mal", 17, 25),
                                    new AST.ID(new Position("core.mal", 17, 25), "Zero"),
                                    new ArrayList<>())),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position("core.mal", 18, 7),
                                    AST.MetaType.INFO,
                                    "This is a1Attack2")),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 19, 7),
                                    false,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("core.mal", 19, 10),
                                            new AST.TransitiveExpr(
                                                new Position("core.mal", 19, 10),
                                                new AST.SubTypeExpr(
                                                    new Position("core.mal", 19, 10),
                                                    new AST.IDExpr(
                                                        new Position("core.mal", 19, 10),
                                                        new AST.ID(
                                                            new Position("core.mal", 19, 10),
                                                            "a1Sub")),
                                                    new AST.ID(
                                                        new Position("core.mal", 19, 16), "A1"))),
                                            new AST.IDExpr(
                                                new Position("core.mal", 19, 21),
                                                new AST.ID(
                                                    new Position("core.mal", 19, 21),
                                                    "a1Attack1"))))))),
                        new AST.AttackStep(
                            new Position("core.mal", 20, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position("core.mal", 20, 7), "a1Defense1"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("core.mal", 20, 19),
                                    new AST.ID(new Position("core.mal", 20, 19), "Bernoulli"),
                                    Arrays.asList(Double.valueOf(0.5)))),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position("core.mal", 21, 7),
                                    AST.MetaType.RATIONALE,
                                    "Reasoning for a1Defense")),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("core.mal", 23, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position("core.mal", 23, 7), "a1Defense2"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("core.mal", 23, 19),
                                    new AST.ID(new Position("core.mal", 23, 19), "Disabled"),
                                    List.of())),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 24, 7),
                                    false,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("core.mal", 24, 10),
                                            new AST.ID(
                                                new Position("core.mal", 24, 10), "a1Attack2")))))),
                        new AST.AttackStep(
                            new Position("core.mal", 25, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position("core.mal", 25, 7), "a1Exist1"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.of(
                                new AST.Requires(
                                    new Position("core.mal", 26, 7),
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("core.mal", 26, 10),
                                            new AST.ID(
                                                new Position("core.mal", 26, 10), "a1Sub"))))),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("core.mal", 28, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position("core.mal", 28, 7), "a1Exist2"),
                            Optional.empty(),
                            Optional.empty(),
                            Arrays.asList(
                                new AST.Meta(
                                    new Position("core.mal", 29, 7),
                                    AST.MetaType.ASSUMPTIONS,
                                    "None for a1Exist2")),
                            Optional.of(
                                new AST.Requires(
                                    new Position("core.mal", 30, 7),
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("core.mal", 30, 10),
                                            new AST.ID(new Position("core.mal", 30, 10), "a7")),
                                        new AST.StepExpr(
                                            new Position("core.mal", 30, 14),
                                            new AST.StepExpr(
                                                new Position("core.mal", 30, 14),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 30, 14),
                                                    new AST.ID(
                                                        new Position("core.mal", 30, 14),
                                                        "a1Super")),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 30, 22),
                                                    new AST.ID(
                                                        new Position("core.mal", 30, 22),
                                                        "a1Super"))),
                                            new AST.IDExpr(
                                                new Position("core.mal", 30, 30),
                                                new AST.ID(
                                                    new Position("core.mal", 30, 30), "a7")))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 31, 7),
                                    false,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("core.mal", 31, 10),
                                            new AST.SubTypeExpr(
                                                new Position("core.mal", 31, 10),
                                                new AST.TransitiveExpr(
                                                    new Position("core.mal", 31, 11),
                                                    new AST.IDExpr(
                                                        new Position("core.mal", 31, 11),
                                                        new AST.ID(
                                                            new Position("core.mal", 31, 11),
                                                            "a1Super"))),
                                                new AST.ID(new Position("core.mal", 31, 21), "A2")),
                                            new AST.StepExpr(
                                                new Position("core.mal", 31, 26),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 31, 26),
                                                    new AST.ID(
                                                        new Position("core.mal", 31, 26), "a8")),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 31, 29),
                                                    new AST.ID(
                                                        new Position("core.mal", 31, 29),
                                                        "destroy")))))))),
                        new AST.AttackStep(
                            new Position("core.mal", 32, 5),
                            AST.AttackStepType.NOTEXIST,
                            new AST.ID(new Position("core.mal", 32, 8), "a1NotExist1"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.of(
                                new AST.Requires(
                                    new Position("core.mal", 33, 7),
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("core.mal", 33, 10),
                                            new AST.StepExpr(
                                                new Position("core.mal", 33, 10),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 33, 10),
                                                    new AST.ID(
                                                        new Position("core.mal", 33, 10), "a8")),
                                                new AST.TransitiveExpr(
                                                    new Position("core.mal", 33, 13),
                                                    new AST.IDExpr(
                                                        new Position("core.mal", 33, 13),
                                                        new AST.ID(
                                                            new Position("core.mal", 33, 13),
                                                            "a8Sub")))),
                                            new AST.SubTypeExpr(
                                                new Position("core.mal", 33, 20),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 33, 20),
                                                    new AST.ID(
                                                        new Position("core.mal", 33, 20), "a1")),
                                                new AST.ID(
                                                    new Position("core.mal", 33, 23), "A7")))))),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("core.mal", 35, 5),
                            AST.AttackStepType.NOTEXIST,
                            new AST.ID(new Position("core.mal", 35, 8), "a1NotExist2"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.of(
                                new AST.Requires(
                                    new Position("core.mal", 36, 7),
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IntersectionExpr(
                                            new Position("core.mal", 36, 10),
                                            new AST.StepExpr(
                                                new Position("core.mal", 36, 10),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 36, 10),
                                                    new AST.ID(
                                                        new Position("core.mal", 36, 10), "a8")),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 36, 13),
                                                    new AST.ID(
                                                        new Position("core.mal", 36, 13), "a4"))),
                                            new AST.IDExpr(
                                                new Position("core.mal", 36, 19),
                                                new AST.ID(
                                                    new Position("core.mal", 36, 19), "a4")))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 37, 7),
                                    false,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("core.mal", 37, 10),
                                            new AST.IDExpr(
                                                new Position("core.mal", 37, 10),
                                                new AST.ID(new Position("core.mal", 37, 10), "a4")),
                                            new AST.IDExpr(
                                                new Position("core.mal", 37, 13),
                                                new AST.ID(
                                                    new Position("core.mal", 37, 13), "a")))))))),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("core.mal", 40, 3),
                    false,
                    new AST.ID(new Position("core.mal", 40, 9), "A2"),
                    Optional.of(new AST.ID(new Position("core.mal", 40, 20), "A1")),
                    new ArrayList<AST.Meta>(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("core.mal", 42, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("core.mal", 42, 7), "a1Attack1"),
                            Optional.of(new ArrayList<AST.CIA>()),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 43, 7),
                                    false,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("core.mal", 43, 10),
                                            new AST.IDExpr(
                                                new Position("core.mal", 43, 10),
                                                new AST.ID(
                                                    new Position("core.mal", 43, 10), "a1Super")),
                                            new AST.IDExpr(
                                                new Position("core.mal", 43, 18),
                                                new AST.ID(
                                                    new Position("core.mal", 43, 18),
                                                    "a1Attack1"))))))),
                        new AST.AttackStep(
                            new Position("core.mal", 45, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("core.mal", 45, 7), "a1Attack2"),
                            Optional.of(Arrays.asList(AST.CIA.C, AST.CIA.I, AST.CIA.A)),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 46, 7),
                                    true,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("core.mal", 46, 10),
                                            new AST.IDExpr(
                                                new Position("core.mal", 46, 10),
                                                new AST.ID(
                                                    new Position("core.mal", 46, 10), "a1Super")),
                                            new AST.IDExpr(
                                                new Position("core.mal", 46, 18),
                                                new AST.ID(
                                                    new Position("core.mal", 46, 18),
                                                    "a1Attack1"))))))),
                        new AST.AttackStep(
                            new Position("core.mal", 48, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position("core.mal", 48, 7), "a1Defense1"),
                            Optional.empty(),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("core.mal", 48, 19),
                                    new AST.ID(new Position("core.mal", 48, 19), "Enabled"),
                                    List.of())),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 49, 7),
                                    false,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("core.mal", 49, 10),
                                            new AST.StepExpr(
                                                new Position("core.mal", 49, 10),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 49, 10),
                                                    new AST.ID(
                                                        new Position("core.mal", 49, 10), "a1Sub")),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 49, 16),
                                                    new AST.ID(
                                                        new Position("core.mal", 49, 16),
                                                        "a1Sub"))),
                                            new AST.IDExpr(
                                                new Position("core.mal", 49, 22),
                                                new AST.ID(
                                                    new Position("core.mal", 49, 22),
                                                    "a1Attack2"))))))),
                        new AST.AttackStep(
                            new Position("core.mal", 51, 5),
                            AST.AttackStepType.DEFENSE,
                            new AST.ID(new Position("core.mal", 51, 7), "a1Defense2"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 52, 7),
                                    true,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("core.mal", 52, 10),
                                            new AST.ID(
                                                new Position("core.mal", 52, 10), "a1Attack2")))))),
                        new AST.AttackStep(
                            new Position("core.mal", 54, 5),
                            AST.AttackStepType.EXIST,
                            new AST.ID(new Position("core.mal", 54, 7), "a1Exist2"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.of(
                                new AST.Requires(
                                    new Position("core.mal", 55, 7),
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("core.mal", 55, 10),
                                            new AST.ID(new Position("core.mal", 55, 10), "a8"))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 56, 7),
                                    false,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("core.mal", 56, 10),
                                            new AST.ID(
                                                new Position("core.mal", 56, 10),
                                                "a1Defense2")))))),
                        new AST.AttackStep(
                            new Position("core.mal", 58, 5),
                            AST.AttackStepType.NOTEXIST,
                            new AST.ID(new Position("core.mal", 58, 8), "a1NotExist1"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.of(
                                new AST.Requires(
                                    new Position("core.mal", 59, 8),
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("core.mal", 59, 11),
                                            new AST.ID(new Position("core.mal", 59, 11), "a8"))))),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 60, 8),
                                    true,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("core.mal", 60, 11),
                                            new AST.ID(
                                                new Position("core.mal", 60, 11),
                                                "a1Defense1"))))))),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("core.mal", 63, 3),
                    true,
                    new AST.ID(new Position("core.mal", 63, 18), "A3"),
                    Optional.of(new AST.ID(new Position("core.mal", 63, 29), "A1")),
                    Arrays.asList(
                        new AST.Meta(
                            new Position("core.mal", 64, 5), AST.MetaType.INFO, "This is A3")),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("core.mal", 67, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("core.mal", 67, 7), "a3Attack"),
                            Optional.of(Arrays.asList(AST.CIA.A, AST.CIA.I, AST.CIA.C)),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty()),
                        new AST.AttackStep(
                            new Position("core.mal", 72, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("core.mal", 72, 7), "AT"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 73, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.Variable(
                                            new Position("core.mal", 73, 10),
                                            new AST.ID(new Position("core.mal", 73, 14), "V1"),
                                            new AST.IDExpr(
                                                new Position("core.mal", 73, 19),
                                                new AST.ID(
                                                    new Position("core.mal", 73, 19), "V2")))),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("core.mal", 74, 10),
                                            new AST.IDExpr(
                                                new Position("core.mal", 74, 10),
                                                new AST.ID(new Position("core.mal", 74, 10), "V1")),
                                            new AST.IDExpr(
                                                new Position("core.mal", 74, 13),
                                                new AST.ID(
                                                    new Position("core.mal", 74, 13),
                                                    "a1Attack1")))))))),
                    Arrays.asList(
                        new AST.Variable(
                            new Position("core.mal", 66, 5),
                            new AST.ID(new Position("core.mal", 66, 9), "unused"),
                            new AST.IDExpr(
                                new Position("core.mal", 66, 18),
                                new AST.ID(new Position("core.mal", 66, 18), "a1Sub"))),
                        new AST.Variable(
                            new Position("core.mal", 68, 5),
                            new AST.ID(new Position("core.mal", 68, 9), "used"),
                            new AST.SubTypeExpr(
                                new Position("core.mal", 68, 16),
                                new AST.IDExpr(
                                    new Position("core.mal", 68, 16),
                                    new AST.ID(new Position("core.mal", 68, 16), "a1Sub")),
                                new AST.ID(new Position("core.mal", 68, 22), "A2"))),
                        new AST.Variable(
                            new Position("core.mal", 70, 5),
                            new AST.ID(new Position("core.mal", 70, 9), "V1"),
                            new AST.IDExpr(
                                new Position("core.mal", 70, 14),
                                new AST.ID(new Position("core.mal", 70, 14), "used"))),
                        new AST.Variable(
                            new Position("core.mal", 71, 5),
                            new AST.ID(new Position("core.mal", 71, 9), "V2"),
                            new AST.IDExpr(
                                new Position("core.mal", 71, 14),
                                new AST.ID(new Position("core.mal", 71, 14), "V1"))))))),
        categories.get(1));
    assertCategory(
        new AST.Category(
            new Position("core.mal", 78, 1),
            new AST.ID(new Position("core.mal", 78, 10), "C2"),
            new ArrayList<AST.Meta>(),
            new ArrayList<AST.Asset>()),
        categories.get(2));
    assertCategory(
        new AST.Category(
            new Position("core.mal", 80, 1),
            new AST.ID(new Position("core.mal", 80, 10), "C2"),
            Arrays.asList(
                new AST.Meta(
                    new Position("core.mal", 81, 3), AST.MetaType.ASSUMPTIONS, "None for C2")),
            Arrays.asList(
                new AST.Asset(
                    new Position("core.mal", 83, 3),
                    true,
                    new AST.ID(new Position("core.mal", 83, 18), "A4"),
                    Optional.empty(),
                    Arrays.asList(
                        new AST.Meta(
                            new Position("core.mal", 84, 5),
                            AST.MetaType.ASSUMPTIONS,
                            "None for A4"),
                        new AST.Meta(
                            new Position("core.mal", 85, 5),
                            AST.MetaType.RATIONALE,
                            "Reasoning for A4"),
                        new AST.Meta(
                            new Position("core.mal", 86, 5), AST.MetaType.INFO, "This is A4")),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("core.mal", 89, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("core.mal", 89, 7), "a"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 90, 7),
                                    false,
                                    Arrays.asList(
                                        new AST.Variable(
                                            new Position("core.mal", 92, 10),
                                            new AST.ID(new Position("core.mal", 92, 14), "var"),
                                            new AST.SubTypeExpr(
                                                new Position("core.mal", 92, 20),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 92, 20),
                                                    new AST.ID(
                                                        new Position("core.mal", 92, 20), "a1")),
                                                new AST.ID(
                                                    new Position("core.mal", 92, 23), "A2")))),
                                    Arrays.asList(
                                        new AST.StepExpr(
                                            new Position("core.mal", 90, 10),
                                            new AST.UnionExpr(
                                                new Position("core.mal", 90, 11),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 90, 11),
                                                    new AST.ID(
                                                        new Position("core.mal", 90, 11), "var")),
                                                new AST.IDExpr(
                                                    new Position("core.mal", 90, 18),
                                                    new AST.ID(
                                                        new Position("core.mal", 90, 18), "var2"))),
                                            new AST.IDExpr(
                                                new Position("core.mal", 90, 24),
                                                new AST.ID(
                                                    new Position("core.mal", 90, 24),
                                                    "a1Attack1")))))))),
                    Arrays.asList(
                        new AST.Variable(
                            new Position("core.mal", 88, 5),
                            new AST.ID(new Position("core.mal", 88, 9), "var"),
                            new AST.StepExpr(
                                new Position("core.mal", 88, 15),
                                new AST.IDExpr(
                                    new Position("core.mal", 88, 15),
                                    new AST.ID(new Position("core.mal", 88, 15), "a1")),
                                new AST.TransitiveExpr(
                                    new Position("core.mal", 88, 18),
                                    new AST.IDExpr(
                                        new Position("core.mal", 88, 18),
                                        new AST.ID(new Position("core.mal", 88, 18), "a1Sub"))))),
                        new AST.Variable(
                            new Position("core.mal", 94, 5),
                            new AST.ID(new Position("core.mal", 94, 9), "var2"),
                            new AST.SubTypeExpr(
                                new Position("core.mal", 94, 16),
                                new AST.IDExpr(
                                    new Position("core.mal", 94, 16),
                                    new AST.ID(new Position("core.mal", 94, 16), "var")),
                                new AST.ID(new Position("core.mal", 94, 20), "A3"))))))),
        categories.get(3));
    assertCategory(
        new AST.Category(
            new Position("core.mal", 98, 1),
            new AST.ID(new Position("core.mal", 98, 10), "C2"),
            Arrays.asList(
                new AST.Meta(
                    new Position("core.mal", 99, 3), AST.MetaType.RATIONALE, "Reasoning for C2")),
            Arrays.asList(
                new AST.Asset(
                    new Position("core.mal", 101, 3),
                    false,
                    new AST.ID(new Position("core.mal", 101, 9), "A5"),
                    Optional.of(new AST.ID(new Position("core.mal", 101, 20), "A4")),
                    Arrays.asList(
                        new AST.Meta(
                            new Position("core.mal", 102, 5),
                            AST.MetaType.ASSUMPTIONS,
                            "None for A5")),
                    new ArrayList<AST.AttackStep>(),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("core.mal", 105, 3),
                    true,
                    new AST.ID(new Position("core.mal", 105, 18), "A6"),
                    Optional.of(new AST.ID(new Position("core.mal", 105, 29), "A4")),
                    Arrays.asList(
                        new AST.Meta(
                            new Position("core.mal", 106, 5),
                            AST.MetaType.RATIONALE,
                            "Reasoning for A6")),
                    new ArrayList<AST.AttackStep>(),
                    new ArrayList<AST.Variable>()))),
        categories.get(4));
    assertCategory(
        new AST.Category(
            new Position("core.mal", 112, 1),
            new AST.ID(new Position("core.mal", 112, 10), "C3"),
            new ArrayList<AST.Meta>(),
            Arrays.asList(
                new AST.Asset(
                    new Position("core.mal", 113, 3),
                    false,
                    new AST.ID(new Position("core.mal", 113, 9), "A7"),
                    Optional.of(new AST.ID(new Position("core.mal", 113, 20), "A3")),
                    new ArrayList<AST.Meta>(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("core.mal", 114, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("core.mal", 114, 7), "a7Attack"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.of(
                                new AST.Reaches(
                                    new Position("core.mal", 115, 7),
                                    false,
                                    new ArrayList<AST.Variable>(),
                                    Arrays.asList(
                                        new AST.IDExpr(
                                            new Position("core.mal", 115, 10),
                                            new AST.ID(
                                                new Position("core.mal", 115, 10), "a3Attack")),
                                        new AST.IDExpr(
                                            new Position("core.mal", 116, 10),
                                            new AST.ID(
                                                new Position("core.mal", 116, 10), "otherAttack")),
                                        new AST.StepExpr(
                                            new Position("core.mal", 117, 10),
                                            new AST.IDExpr(
                                                new Position("core.mal", 117, 10),
                                                new AST.ID(
                                                    new Position("core.mal", 117, 10), "a1")),
                                            new AST.IDExpr(
                                                new Position("core.mal", 117, 13),
                                                new AST.ID(
                                                    new Position("core.mal", 117, 13),
                                                    "destroy"))))))),
                        new AST.AttackStep(
                            new Position("core.mal", 118, 5),
                            AST.AttackStepType.ANY,
                            new AST.ID(new Position("core.mal", 118, 7), "otherAttack"),
                            Optional.empty(),
                            Optional.empty(),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty())),
                    Arrays.asList(
                        new AST.Variable(
                            new Position("core.mal", 120, 5),
                            new AST.ID(new Position("core.mal", 120, 9), "a3Attack"),
                            new AST.IDExpr(
                                new Position("core.mal", 120, 20),
                                new AST.ID(new Position("core.mal", 120, 20), "a1Attack1"))),
                        new AST.Variable(
                            new Position("core.mal", 122, 5),
                            new AST.ID(new Position("core.mal", 122, 9), "otherAttack"),
                            new AST.IDExpr(
                                new Position("core.mal", 122, 23),
                                new AST.ID(new Position("core.mal", 122, 23), "a7Attack"))),
                        new AST.Variable(
                            new Position("core.mal", 124, 5),
                            new AST.ID(new Position("core.mal", 124, 9), "a6"),
                            new AST.StepExpr(
                                new Position("core.mal", 124, 14),
                                new AST.IDExpr(
                                    new Position("core.mal", 124, 14),
                                    new AST.ID(new Position("core.mal", 124, 14), "a1Super")),
                                new AST.IDExpr(
                                    new Position("core.mal", 124, 22),
                                    new AST.ID(new Position("core.mal", 124, 22), "a8")))),
                        new AST.Variable(
                            new Position("core.mal", 126, 5),
                            new AST.ID(new Position("core.mal", 126, 9), "a1"),
                            new AST.StepExpr(
                                new Position("core.mal", 126, 14),
                                new AST.IDExpr(
                                    new Position("core.mal", 126, 14),
                                    new AST.ID(new Position("core.mal", 126, 14), "a6")),
                                new AST.IDExpr(
                                    new Position("core.mal", 126, 17),
                                    new AST.ID(new Position("core.mal", 126, 17), "a8Super")))))),
                new AST.Asset(
                    new Position("core.mal", 129, 3),
                    false,
                    new AST.ID(new Position("core.mal", 129, 9), "A8"),
                    Optional.empty(),
                    new ArrayList<AST.Meta>(),
                    Arrays.asList(
                        new AST.AttackStep(
                            new Position("core.mal", 130, 5),
                            AST.AttackStepType.ALL,
                            new AST.ID(new Position("core.mal", 130, 7), "destroy"),
                            Optional.of(Arrays.asList(AST.CIA.C, AST.CIA.I, AST.CIA.A)),
                            Optional.of(
                                new AST.TTCFuncExpr(
                                    new Position("core.mal", 130, 26),
                                    new AST.ID(new Position("core.mal", 130, 26), "Exponential"),
                                    Arrays.asList(Double.valueOf(5)))),
                            new ArrayList<AST.Meta>(),
                            Optional.empty(),
                            Optional.empty())),
                    new ArrayList<AST.Variable>()),
                new AST.Asset(
                    new Position("core.mal", 133, 3),
                    false,
                    new AST.ID(new Position("core.mal", 133, 9), "A9"),
                    Optional.empty(),
                    new ArrayList<AST.Meta>(),
                    new ArrayList<AST.AttackStep>(),
                    new ArrayList<AST.Variable>()))),
        categories.get(5));
    var associations = ast.getAssociations();
    assertEquals(8, associations.size());
    assertAssociation(
        new AST.Association(
            new Position("core.mal", 143, 3),
            new AST.ID(new Position("core.mal", 143, 3), "A1"),
            new AST.ID(new Position("core.mal", 143, 7), "a1"),
            AST.Multiplicity.ONE,
            new AST.ID(new Position("core.mal", 143, 25), "L1"),
            AST.Multiplicity.ONE_OR_MORE,
            new AST.ID(new Position("core.mal", 143, 38), "a4"),
            new AST.ID(new Position("core.mal", 143, 45), "A4"),
            new ArrayList<AST.Meta>()),
        associations.get(0));
    assertAssociation(
        new AST.Association(
            new Position("core.mal", 144, 3),
            new AST.ID(new Position("core.mal", 144, 3), "A5"),
            new AST.ID(new Position("core.mal", 144, 7), "a5"),
            AST.Multiplicity.ONE,
            new AST.ID(new Position("core.mal", 144, 25), "L2"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position("core.mal", 144, 38), "a6"),
            new AST.ID(new Position("core.mal", 144, 45), "A6"),
            new ArrayList<AST.Meta>()),
        associations.get(1));
    assertAssociation(
        new AST.Association(
            new Position("core.mal", 149, 3),
            new AST.ID(new Position("core.mal", 149, 3), "A1"),
            new AST.ID(new Position("core.mal", 149, 7), "a1Super"),
            AST.Multiplicity.ZERO_OR_ONE,
            new AST.ID(new Position("core.mal", 149, 25), "L3"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position("core.mal", 149, 38), "a1Sub"),
            new AST.ID(new Position("core.mal", 149, 47), "A1"),
            new ArrayList<AST.Meta>()),
        associations.get(2));
    assertAssociation(
        new AST.Association(
            new Position("core.mal", 150, 3),
            new AST.ID(new Position("core.mal", 150, 3), "A3"),
            new AST.ID(new Position("core.mal", 150, 7), "a3"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position("core.mal", 150, 25), "L3"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position("core.mal", 150, 38), "a6"),
            new AST.ID(new Position("core.mal", 150, 47), "A6"),
            new ArrayList<AST.Meta>()),
        associations.get(3));
    assertAssociation(
        new AST.Association(
            new Position("core.mal", 151, 3),
            new AST.ID(new Position("core.mal", 151, 3), "A7"),
            new AST.ID(new Position("core.mal", 151, 7), "a7"),
            AST.Multiplicity.ZERO_OR_ONE,
            new AST.ID(new Position("core.mal", 151, 25), "L3"),
            AST.Multiplicity.ONE,
            new AST.ID(new Position("core.mal", 151, 38), "a1"),
            new AST.ID(new Position("core.mal", 151, 47), "A1"),
            new ArrayList<AST.Meta>()),
        associations.get(4));
    assertAssociation(
        new AST.Association(
            new Position("core.mal", 152, 3),
            new AST.ID(new Position("core.mal", 152, 3), "A8"),
            new AST.ID(new Position("core.mal", 152, 7), "a8"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position("core.mal", 152, 25), "L4"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position("core.mal", 152, 38), "a1"),
            new AST.ID(new Position("core.mal", 152, 47), "A1"),
            new ArrayList<AST.Meta>()),
        associations.get(5));
    assertAssociation(
        new AST.Association(
            new Position("core.mal", 153, 3),
            new AST.ID(new Position("core.mal", 153, 3), "A8"),
            new AST.ID(new Position("core.mal", 153, 7), "a8"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position("core.mal", 153, 25), "L4"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position("core.mal", 153, 38), "a4"),
            new AST.ID(new Position("core.mal", 153, 47), "A4"),
            new ArrayList<AST.Meta>()),
        associations.get(6));
    assertAssociation(
        new AST.Association(
            new Position("core.mal", 154, 3),
            new AST.ID(new Position("core.mal", 154, 3), "A8"),
            new AST.ID(new Position("core.mal", 154, 7), "a8Sub"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position("core.mal", 154, 25), "L4"),
            AST.Multiplicity.ZERO_OR_MORE,
            new AST.ID(new Position("core.mal", 154, 38), "a8Super"),
            new AST.ID(new Position("core.mal", 154, 47), "A8"),
            new ArrayList<AST.Meta>()),
        associations.get(7));
    var defines = ast.getDefines();
    assertEquals(4, defines.size());
    assertDefine(
        new AST.Define(
            new Position("all-features.mal", 6, 1),
            new AST.ID(new Position("all-features.mal", 6, 2), "id"),
            "all-features"),
        defines.get(0));
    assertDefine(
        new AST.Define(
            new Position("all-features.mal", 7, 1),
            new AST.ID(new Position("all-features.mal", 7, 2), "version"),
            "0.0.1"),
        defines.get(1));
    assertDefine(
        new AST.Define(
            new Position("subdir/subincluded.mal", 2, 2),
            new AST.ID(new Position("subdir/subincluded.mal", 3, 4), "empty"),
            ""),
        defines.get(2));
    assertDefine(
        new AST.Define(
            new Position("included.mal", 2, 2),
            new AST.ID(new Position("included.mal", 2, 4), "other"),
            "other"),
        defines.get(3));
  }

  @Test
  public void testAnalyzer() {
    assertAnalyzeClassPath("all-features/all-features.mal");
    assertEmptyOut();
    String[] lines = {
      "[ANALYZER WARNING] <core.mal:78:10> Category 'C2' contains no assets or metadata",
      "[ANALYZER WARNING] <core.mal:105:18> Asset 'A6' is abstract but never extended to",
      "[ANALYZER WARNING] <core.mal:115:10> Step 'a3Attack' defined as variable at <core.mal:120:9> and attack step at <core.mal:67:7>",
      "[ANALYZER WARNING] <core.mal:116:10> Step 'otherAttack' defined as variable at <core.mal:122:9> and attack step at <core.mal:118:7>",
      "[ANALYZER WARNING] <core.mal:117:10> Step 'a1' defined as variable at <core.mal:126:9> and field at <core.mal:151:38>",
      "[ANALYZER WARNING] <core.mal:126:14> Step 'a6' defined as variable at <core.mal:124:9> and field at <core.mal:150:38>",
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
        new Lang.Meta()
            .setInfo("This is C1")
            .setAssumptions("None for C1")
            .setRationale("Reasoning for C1"));
    assertLangCategory(
        lang,
        "C2",
        new String[] {"A4", "A5", "A6"},
        new Lang.Meta().setAssumptions("None for C2").setRationale("Reasoning for C2"));
    assertLangCategory(lang, "C3", new String[] {"A7", "A8", "A9"}, new Lang.Meta());
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
    var asset = assertGetLangAsset(lang, "A1", false, "C1", null, new Lang.Meta());
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
            new Lang.Meta()
                .setInfo("This is a1Attack1")
                .setAssumptions("None for a1Attack1")
                .setRationale("Reasoning for a1Attack1"));
    assertLangCIA(attackStep, new Lang.CIA(true, false, false));
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(0, reaches.size());
    assertEquals(5, parentSteps.size());

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
                new Lang.StepCollect(
                    lang.getAsset("A3"),
                    lang.getAsset("A1"),
                    lang.getAsset("A4"),
                    lang.getAsset("A4"),
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
                    new Lang.StepField(
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A4"),
                        lang.getAsset("A4"),
                        lang.getAsset("A1").getField("a4"))),
                new Lang.StepField(
                    lang.getAsset("A2"),
                    lang.getAsset("A1"),
                    lang.getAsset("A4"),
                    lang.getAsset("A4"),
                    lang.getAsset("A1").getField("a4"))),
            new Lang.StepAttackStep(
                lang.getAsset("A4"), lang.getAsset("A4"), lang.getAsset("A4").getAttackStep("a")));
    assertLangStepExpr(step, parentSteps.get(3));

    // Auto-generated parent step 5
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A7"),
            lang.getAsset("A1"),
            lang.getAsset("A7").getAttackStep("a7Attack"));
    assertLangStepExpr(step, parentSteps.get(4));

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
            new Lang.Meta().setInfo("This is a1Attack2"));
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
            new Lang.Meta().setRationale("Reasoning for a1Defense"));
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
            asset,
            "a1Defense2",
            Lang.AttackStepType.DEFENSE,
            false,
            true,
            false,
            false,
            new Lang.Meta());
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
            asset,
            "a1Exist1",
            Lang.AttackStepType.EXIST,
            false,
            false,
            true,
            false,
            new Lang.Meta());
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
            new Lang.Meta().setAssumptions("None for a1Exist2"));
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
            new Lang.Meta());
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
            new Lang.Meta());
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
    var asset = assertGetLangAsset(lang, "A2", false, "C1", "A1", new Lang.Meta());
    // Check fields
    assertEquals(0, asset.getFields().size());
    // Check attack steps
    assertEquals(6, asset.getAttackSteps().size());

    // Check attack step "a1Attack1"
    var attackStep =
        assertGetLangAttackStep(
            asset,
            "a1Attack1",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            true,
            new Lang.Meta());
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
            new Lang.StepField(
                lang.getAsset("A2"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A3"),
                lang.getAsset("A1").getField("a1Super")),
            new Lang.StepAttackStep(
                lang.getAsset("A3"), lang.getAsset("A3"), lang.getAsset("A3").getAttackStep("AT")));
    assertLangStepExpr(step, parentSteps.get(0));

    // Check attack step "a1Attack2"
    attackStep =
        assertGetLangAttackStep(
            asset, "a1Attack2", Lang.AttackStepType.ALL, true, false, false, true, new Lang.Meta());
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
            asset,
            "a1Defense1",
            Lang.AttackStepType.DEFENSE,
            false,
            true,
            false,
            true,
            new Lang.Meta());
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
            asset,
            "a1Defense2",
            Lang.AttackStepType.DEFENSE,
            true,
            true,
            false,
            true,
            new Lang.Meta());
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
            asset,
            "a1Exist2",
            Lang.AttackStepType.EXIST,
            false,
            false,
            true,
            true,
            new Lang.Meta());
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
            asset,
            "a1NotExist1",
            Lang.AttackStepType.NOTEXIST,
            true,
            false,
            true,
            true,
            new Lang.Meta());
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
        assertGetLangAsset(lang, "A3", true, "C1", "A1", new Lang.Meta().setInfo("This is A3"));
    // Check fields
    assertEquals(1, asset.getFields().size());
    assertLangField(asset, "a6", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(2, asset.getAttackSteps().size());

    // Check attack step "a3Attack"
    var attackStep =
        assertGetLangAttackStep(
            asset,
            "a3Attack",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
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
            asset, "AT", Lang.AttackStepType.ALL, false, false, false, false, new Lang.Meta());
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
            new Lang.StepField(
                lang.getAsset("A3"),
                lang.getAsset("A1"),
                lang.getAsset("A1"),
                lang.getAsset("A2"),
                lang.getAsset("A1").getField("a1Sub")),
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
            new Lang.Meta()
                .setAssumptions("None for A4")
                .setRationale("Reasoning for A4")
                .setInfo("This is A4"));
    // Check fields
    assertEquals(2, asset.getFields().size());
    assertLangField(asset, "a1", 1, 1);
    assertLangField(asset, "a8", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(1, asset.getAttackSteps().size());

    // Check attack step "a"
    var attackStep =
        assertGetLangAttackStep(
            asset, "a", Lang.AttackStepType.ANY, false, false, false, false, new Lang.Meta());
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
                new Lang.StepField(
                    lang.getAsset("A4"),
                    lang.getAsset("A4"),
                    lang.getAsset("A1"),
                    lang.getAsset("A2"),
                    lang.getAsset("A4").getField("a1")),
                new Lang.StepCollect(
                    lang.getAsset("A4"),
                    lang.getAsset("A4"),
                    lang.getAsset("A1"),
                    lang.getAsset("A3"),
                    new Lang.StepField(
                        lang.getAsset("A4"),
                        lang.getAsset("A4"),
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A4").getField("a1")),
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
                            lang.getAsset("A1").getField("a1Sub"))))),
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
            lang, "A5", false, "C2", "A4", new Lang.Meta().setAssumptions("None for A5"));
    // Check fields
    assertEquals(1, asset.getFields().size());
    assertLangField(asset, "a6", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(0, asset.getAttackSteps().size());
  }

  private static void assertAssetA6(Lang lang) {
    var asset =
        assertGetLangAsset(
            lang, "A6", true, "C2", "A4", new Lang.Meta().setRationale("Reasoning for A6"));
    // Check fields
    assertEquals(2, asset.getFields().size());
    assertLangField(asset, "a5", 1, 1);
    assertLangField(asset, "a3", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(0, asset.getAttackSteps().size());
  }

  private static void assertAssetA7(Lang lang) {
    var asset = assertGetLangAsset(lang, "A7", false, "C3", "A3", new Lang.Meta());
    // Check fields
    assertEquals(1, asset.getFields().size());
    assertLangField(asset, "a1", 1, 1);
    // Check attack steps
    assertEquals(2, asset.getAttackSteps().size());

    // Check attack step "a7Attack"
    var attackStep =
        assertGetLangAttackStep(
            asset,
            "a7Attack",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(3, reaches.size());
    assertEquals(1, parentSteps.size());

    // Auto-generated reaches step 1
    Lang.StepExpr step =
        new Lang.StepAttackStep(
            lang.getAsset("A7"),
            lang.getAsset("A1"),
            lang.getAsset("A1").getAttackStep("a1Attack1"));
    assertLangStepExpr(step, reaches.get(0));

    // Auto-generated reaches step 2
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A7"),
            lang.getAsset("A7"),
            lang.getAsset("A7").getAttackStep("a7Attack"));
    assertLangStepExpr(step, reaches.get(1));

    // Auto-generated reaches step 3
    step =
        new Lang.StepCollect(
            lang.getAsset("A7"),
            lang.getAsset("A7"),
            null,
            null,
            new Lang.StepCollect(
                lang.getAsset("A7"),
                lang.getAsset("A7"),
                lang.getAsset("A8"),
                lang.getAsset("A8"),
                new Lang.StepCollect(
                    lang.getAsset("A7"),
                    lang.getAsset("A7"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    new Lang.StepField(
                        lang.getAsset("A7"),
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A1").getField("a1Super")),
                    new Lang.StepField(
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A8"),
                        lang.getAsset("A8"),
                        lang.getAsset("A1").getField("a8"))),
                new Lang.StepField(
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8").getField("a8Super"))),
            new Lang.StepAttackStep(
                lang.getAsset("A8"),
                lang.getAsset("A8"),
                lang.getAsset("A8").getAttackStep("destroy")));
    assertLangStepExpr(step, reaches.get(2));

    // Auto-generated parent step 1
    step =
        new Lang.StepAttackStep(
            lang.getAsset("A7"),
            lang.getAsset("A7"),
            lang.getAsset("A7").getAttackStep("a7Attack"));
    assertLangStepExpr(step, parentSteps.get(0));

    // Check attack step "otherAttack"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "otherAttack",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(0, reaches.size());
    assertEquals(0, parentSteps.size());
  }

  private static void assertAssetA8(Lang lang) {
    var asset = assertGetLangAsset(lang, "A8", false, "C3", null, new Lang.Meta());
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
            asset, "destroy", Lang.AttackStepType.ALL, false, false, false, false, new Lang.Meta());
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
            new Lang.StepCollect(
                lang.getAsset("A8"),
                lang.getAsset("A8"),
                lang.getAsset("A7"),
                lang.getAsset("A7"),
                new Lang.StepField(
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A8").getField("a8Sub")),
                new Lang.StepCollect(
                    lang.getAsset("A8"),
                    lang.getAsset("A8"),
                    lang.getAsset("A7"),
                    lang.getAsset("A7"),
                    new Lang.StepField(
                        lang.getAsset("A8"),
                        lang.getAsset("A8"),
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A8").getField("a1")),
                    new Lang.StepField(
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A1"),
                        lang.getAsset("A7"),
                        lang.getAsset("A1").getField("a1Sub")))),
            new Lang.StepAttackStep(
                lang.getAsset("A7"),
                lang.getAsset("A7"),
                lang.getAsset("A7").getAttackStep("a7Attack")));
    assertLangStepExpr(step, parentSteps.get(1));
  }

  private static void assertAssetA9(Lang lang) {
    var asset = assertGetLangAsset(lang, "A9", false, "C3", null, new Lang.Meta());
    // Check fields
    assertEquals(0, asset.getFields().size());
    // Check attack steps
    assertEquals(0, asset.getAttackSteps().size());
  }

  private static void assertLinks(Lang lang) {
    assertEquals(8, lang.getLinks().size());
    assertLangLink(lang, "A1", "a4", "A4", "a1", 0, "L1", new Lang.Meta());
    assertLangLink(lang, "A5", "a6", "A6", "a5", 1, "L2", new Lang.Meta());
    assertLangLink(lang, "A1", "a1Sub", "A1", "a1Super", 2, "L3", new Lang.Meta());
    assertLangLink(lang, "A3", "a6", "A6", "a3", 3, "L3", new Lang.Meta());
    assertLangLink(lang, "A7", "a1", "A1", "a7", 4, "L3", new Lang.Meta());
    assertLangLink(lang, "A8", "a1", "A1", "a8", 5, "L4", new Lang.Meta());
    assertLangLink(lang, "A8", "a4", "A4", "a8", 6, "L4", new Lang.Meta());
    assertLangLink(lang, "A8", "a8Super", "A8", "a8Sub", 7, "L4", new Lang.Meta());
  }
}

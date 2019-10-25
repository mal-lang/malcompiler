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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mal_lang.compiler.test.lib.AssertAST.getASTClassPath;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.Parser;
import org.mal_lang.compiler.lib.Position;
import org.mal_lang.compiler.test.MalTest;

public class TestParserFail extends MalTest {
  private static final String NON_EXISTANT_MAL = "non-existant.mal";
  private static final String BAD_DEFINE1_MAL = "bad-define1.mal";
  private static final String BAD_DEFINE2_MAL = "bad-define2.mal";
  private static final String BAD_META1_MAL = "bad-meta1.mal";
  private static final String BAD_META2_MAL = "bad-meta2.mal";
  private static final String BAD_META3_MAL = "bad-meta3.mal";
  private static final String BAD_INCLUDE1_MAL = "bad-include1.mal";
  private static final String BAD_INCLUDE2_MAL = "bad-include2.mal";
  private static final String SUBDIR_NON_EXISTANT_MAL = String.format("subDir%snon-existant.mal", fileSep);
  private static final String SUBDIR_BAD_INCLUDED1_MAL = String.format("subDir%sbad-included1.mal", fileSep);
  private static final String BAD_CATEGORY1_MAL = "bad-category1.mal";
  private static final String BAD_ASSET1_MAL = "bad-asset1.mal";
  private static final String BAD_ASSET2_MAL = "bad-asset2.mal";
  private static final String BAD_ASSET3_MAL = "bad-asset3.mal";
  private static final String BAD_ASSET4_MAL = "bad-asset4.mal";
  private static final String BAD_ATTACKSTEP1_MAL = "bad-attackstep1.mal";
  private static final String BAD_ATTACKSTEP2_MAL = "bad-attackstep2.mal";
  private static final String BAD_ATTACKSTEP3_MAL = "bad-attackstep3.mal";
  private static final String BAD_ATTACKSTEP4_MAL = "bad-attackstep4.mal";
  private static final String BAD_ATTACKSTEP5_MAL = "bad-attackstep5.mal";
  private static final String BAD_ATTACKSTEP6_MAL = "bad-attackstep6.mal";
  private static final String BAD_ATTACKSTEP7_MAL = "bad-attackstep7.mal";
  private static final String BAD_ASSOCIATION1_MAL = "bad-association1.mal";
  private static final String BAD_ASSOCIATION2_MAL = "bad-association2.mal";
  private static final String BAD_ASSOCIATION3_MAL = "bad-association3.mal";
  private static final String BAD_ASSOCIATION4_MAL = "bad-association4.mal";
  private static final String BAD_ASSOCIATION5_MAL = "bad-association5.mal";

  private static void assertIOException(String filename, String error) {
    try {
      Parser.parse(new File(filename));
      fail(String.format("File \"%s\" should cause an IOException"));
    } catch (Exception e) {
      assertTrue(e instanceof IOException);
      assertEquals(error, e.getMessage());
    }
  }

  @Test
  public void testBadFile() {
    assertIOException("parser/non-existant.mal", String.format("%s: No such file or directory", NON_EXISTANT_MAL));
    assertIOException("parser/\u0000non-existant.mal", "Invalid file path");
  }

  private void assertSyntaxError(String filename, Position pos, String error) {
    try {
      resetTestSystem();
      getASTClassPath(filename);
      fail(String.format("File \"%s\" should have syntax errors", filename));
    } catch (Exception e) {
      assertTrue(e instanceof CompilerException);
      assertEquals("There were syntax errors", e.getMessage());
      assertEmptyOut();
      assertEquals(String.format("[PARSER ERROR] %s %s%n", pos.posString(), error), getPlainErr());
    }
  }

  @Test
  public void testBadDefine() {
    assertSyntaxError(
        "parser/bad-define1.mal", new Position(BAD_DEFINE1_MAL, 1, 4), "expected ':', found '='");
    assertSyntaxError(
        "parser/bad-define2.mal",
        new Position(BAD_DEFINE2_MAL, 1, 5),
        "expected string literal, found identifier");
  }

  @Test
  public void testBadMeta() {
    assertSyntaxError(
        "parser/bad-meta1.mal",
        new Position(BAD_META1_MAL, 2, 3),
        "expected 'info', 'assumptions', 'rationale', or '{', found identifier");
    assertSyntaxError(
        "parser/bad-meta2.mal", new Position(BAD_META2_MAL, 2, 8), "expected ':', found '='");
    assertSyntaxError(
        "parser/bad-meta3.mal",
        new Position(BAD_META3_MAL, 2, 9),
        "expected string literal, found identifier");
  }

  @Test
  public void testBadInclude() {
    assertSyntaxError(
        "parser/bad-include1.mal",
        new Position(BAD_INCLUDE1_MAL, 1, 9),
        "expected string literal, found identifier");
    assertSyntaxError(
        "parser/bad-include2.mal",
        new Position(BAD_INCLUDE2_MAL, 1, 9),
        String.format("%s: No such file or directory", SUBDIR_NON_EXISTANT_MAL));
    assertSyntaxError(
        "parser/bad-include3.mal",
        new Position(SUBDIR_BAD_INCLUDED1_MAL, 1, 1),
        "expected 'category', 'associations', 'include', or '#', found identifier");
  }

  @Test
  public void testBadCategory() {
    assertSyntaxError(
        "parser/bad-category1.mal",
        new Position(BAD_CATEGORY1_MAL, 2, 3),
        "expected 'abstract', 'asset', or '}', found 'info'");
  }

  @Test
  public void testBadAsset() {
    assertSyntaxError(
        "parser/bad-asset1.mal",
        new Position(BAD_ASSET1_MAL, 2, 3),
        "expected 'abstract', 'asset', or '}', found identifier");
    assertSyntaxError(
        "parser/bad-asset2.mal",
        new Position(BAD_ASSET2_MAL, 2, 12),
        "expected 'asset', found identifier");
    assertSyntaxError(
        "parser/bad-asset3.mal",
        new Position(BAD_ASSET3_MAL, 2, 20),
        "expected identifier, found '{'");
    assertSyntaxError(
        "parser/bad-asset4.mal",
        new Position(BAD_ASSET4_MAL, 3, 5),
        "expected '&', '|', '#', 'E', '!E', 'let', or '}', found 'info'");
  }

  @Test
  public void testBadAttackStep() {
    assertSyntaxError(
        "parser/bad-attackstep1.mal",
        new Position(BAD_ATTACKSTEP1_MAL, 3, 12),
        "expected '&', '|', '#', 'E', '!E', 'let', or '}', found '{'");
    assertSyntaxError(
        "parser/bad-attackstep2.mal",
        new Position(BAD_ATTACKSTEP2_MAL, 3, 12),
        "expected ')', found ']'");
    assertSyntaxError(
        "parser/bad-attackstep3.mal",
        new Position(BAD_ATTACKSTEP3_MAL, 3, 11),
        "expected ']', found ')'");
    assertSyntaxError(
        "parser/bad-attackstep4.mal",
        new Position(BAD_ATTACKSTEP4_MAL, 5, 9),
        "expected identifier or '(', found 'info'");
    assertSyntaxError(
        "parser/bad-attackstep5.mal",
        new Position(BAD_ATTACKSTEP5_MAL, 5, 9),
        "expected identifier or '(', found 'info'");
    assertSyntaxError(
        "parser/bad-attackstep6.mal",
        new Position(BAD_ATTACKSTEP6_MAL, 3, 11),
        "expected 'C', 'I', 'A', or '}', found identifier");
    assertSyntaxError(
        "parser/bad-attackstep7.mal",
        new Position(BAD_ATTACKSTEP7_MAL, 3, 14),
        "expected '&', '|', '#', 'E', '!E', 'let', or '}', found '{'");
  }

  @Test
  public void testBadAssociation() {
    assertSyntaxError(
        "parser/bad-association1.mal",
        new Position(BAD_ASSOCIATION1_MAL, 2, 12),
        "expected '<--', found '<-'");
    assertSyntaxError(
        "parser/bad-association2.mal",
        new Position(BAD_ASSOCIATION2_MAL, 2, 7),
        "expected identifier, found integer literal");
    assertSyntaxError(
        "parser/bad-association3.mal",
        new Position(BAD_ASSOCIATION3_MAL, 2, 10),
        "expected '0', '1', or '*', found integer literal");
    assertSyntaxError(
        "parser/bad-association4.mal",
        new Position(BAD_ASSOCIATION4_MAL, 2, 22),
        "Invalid multiplicity '0'");
    assertSyntaxError(
        "parser/bad-association5.mal",
        new Position(BAD_ASSOCIATION5_MAL, 2, 22),
        "Invalid multiplicity '1..0'");
  }
}

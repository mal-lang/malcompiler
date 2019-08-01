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
package com.foreseeti.mal.test;

import static com.foreseeti.mal.test.AssertAST.getASTClassPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.foreseeti.mal.lib.CompilerException;
import com.foreseeti.mal.lib.Parser;
import com.foreseeti.mal.lib.Position;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class TestParserFail extends MalTest {
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
    assertIOException("parser/non-existant.mal", "non-existant.mal: No such file or directory");
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
      assertEquals(String.format("[PARSER ERROR] %s %s\n", pos.posString(), error), getPlainErr());
    }
  }

  @Test
  public void testBadDefine() {
    assertSyntaxError(
        "parser/bad-define1.mal", new Position("bad-define1.mal", 1, 4), "expected ':', found '='");
    assertSyntaxError(
        "parser/bad-define2.mal",
        new Position("bad-define2.mal", 1, 5),
        "expected string literal, found identifier");
  }

  @Test
  public void testBadMeta() {
    assertSyntaxError(
        "parser/bad-meta1.mal",
        new Position("bad-meta1.mal", 2, 3),
        "expected 'info', 'assumptions', 'rationale', or '{', found identifier");
    assertSyntaxError(
        "parser/bad-meta2.mal", new Position("bad-meta2.mal", 2, 8), "expected ':', found '='");
    assertSyntaxError(
        "parser/bad-meta3.mal",
        new Position("bad-meta3.mal", 2, 9),
        "expected string literal, found identifier");
  }

  @Test
  public void testBadInclude() {
    assertSyntaxError(
        "parser/bad-include1.mal",
        new Position("bad-include1.mal", 1, 9),
        "expected string literal, found identifier");
    assertSyntaxError(
        "parser/bad-include2.mal",
        new Position("bad-include2.mal", 1, 9),
        "subDir/non-existant.mal: No such file or directory");
    assertSyntaxError(
        "parser/bad-include3.mal",
        new Position("subDir/bad-included1.mal", 1, 1),
        "expected 'category', 'associations', 'include', or '#', found identifier");
  }

  @Test
  public void testBadCategory() {
    assertSyntaxError(
        "parser/bad-category1.mal",
        new Position("bad-category1.mal", 2, 3),
        "expected 'abstract', 'asset', or '}', found 'info'");
  }

  @Test
  public void testBadAsset() {
    assertSyntaxError(
        "parser/bad-asset1.mal",
        new Position("bad-asset1.mal", 2, 3),
        "expected 'abstract', 'asset', or '}', found identifier");
    assertSyntaxError(
        "parser/bad-asset2.mal",
        new Position("bad-asset2.mal", 2, 12),
        "expected 'asset', found identifier");
    assertSyntaxError(
        "parser/bad-asset3.mal",
        new Position("bad-asset3.mal", 2, 20),
        "expected identifier, found '{'");
    assertSyntaxError(
        "parser/bad-asset4.mal",
        new Position("bad-asset4.mal", 3, 5),
        "expected '&', '|', '#', 'E', '!E', 'let', or '}', found 'info'");
  }

  @Test
  public void testBadAttackStep() {
    assertSyntaxError(
        "parser/bad-attackstep1.mal",
        new Position("bad-attackstep1.mal", 3, 12),
        "expected '&', '|', '#', 'E', '!E', 'let', or '}', found '{'");
    assertSyntaxError(
        "parser/bad-attackstep2.mal",
        new Position("bad-attackstep2.mal", 3, 12),
        "expected ')', found ']'");
    assertSyntaxError(
        "parser/bad-attackstep3.mal",
        new Position("bad-attackstep3.mal", 3, 11),
        "expected ']', found ')'");
    assertSyntaxError(
        "parser/bad-attackstep4.mal",
        new Position("bad-attackstep4.mal", 5, 9),
        "expected identifier or '(', found 'info'");
    assertSyntaxError(
        "parser/bad-attackstep5.mal",
        new Position("bad-attackstep5.mal", 5, 9),
        "expected identifier or '(', found 'info'");
    assertSyntaxError(
        "parser/bad-attackstep6.mal",
        new Position("bad-attackstep6.mal", 3, 11),
        "expected 'C', 'I', 'A', or '}', found identifier");
    assertSyntaxError(
        "parser/bad-attackstep7.mal",
        new Position("bad-attackstep7.mal", 3, 14),
        "expected '&', '|', '#', 'E', '!E', 'let', or '}', found '{'");
  }

  @Test
  public void testBadAssociation() {
    assertSyntaxError(
        "parser/bad-association1.mal",
        new Position("bad-association1.mal", 2, 12),
        "expected '<--', found '<-'");
    assertSyntaxError(
        "parser/bad-association2.mal",
        new Position("bad-association2.mal", 2, 7),
        "expected identifier, found integer literal");
    assertSyntaxError(
        "parser/bad-association3.mal",
        new Position("bad-association3.mal", 2, 10),
        "expected '0', '1', or '*', found integer literal");
    assertSyntaxError(
        "parser/bad-association4.mal",
        new Position("bad-association4.mal", 2, 22),
        "Invalid multiplicity '0'");
    assertSyntaxError(
        "parser/bad-association5.mal",
        new Position("bad-association5.mal", 2, 22),
        "Invalid multiplicity '1..0'");
  }
}

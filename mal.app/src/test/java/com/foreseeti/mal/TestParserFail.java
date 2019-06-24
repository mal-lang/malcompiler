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

import static com.foreseeti.mal.AssertAST.assertGetParser;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class TestParserFail {
  private static void assertIOException(String filename, String error) {
    try {
      var parser = new Parser(new File(filename));
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

  private static void assertSyntaxError(String filename, String error) {
    var parser = assertGetParser(filename);
    try {
      var ast = parser.parse();
      fail(String.format("File \"%s\" should have syntax errors", filename));
    } catch (Exception e) {
      assertTrue(e instanceof SyntaxError);
      assertEquals(error, e.getMessage());
    }
  }

  @Test
  public void testBadDefine() {
    assertSyntaxError("parser/bad-define1.mal", "bad-define1.mal:1:4:syntax error: expected ':', found '='");
    assertSyntaxError("parser/bad-define2.mal", "bad-define2.mal:1:5:syntax error: expected string literal, found identifier");
  }

  @Test
  public void testBadMeta() {
    assertSyntaxError("parser/bad-meta1.mal", "bad-meta1.mal:2:3:syntax error: expected '{', found identifier");
    assertSyntaxError("parser/bad-meta2.mal", "bad-meta2.mal:2:8:syntax error: expected ':', found '='");
    assertSyntaxError("parser/bad-meta3.mal", "bad-meta3.mal:2:9:syntax error: expected string literal, found identifier");
  }

  @Test
  public void testBadInclude() {
    assertSyntaxError("parser/bad-include1.mal", "bad-include1.mal:1:9:syntax error: expected string literal, found identifier");
    assertSyntaxError("parser/bad-include2.mal", "bad-include2.mal:1:9:syntax error: subDir/non-existant.mal: No such file or directory");
    assertSyntaxError("parser/bad-include3.mal", "subDir/bad-included1.mal:1:1:syntax error: expected 'category', 'associations', 'include', '#', or end-of-file, found identifier");
  }

  @Test
  public void testBadCategory() {
    assertSyntaxError("parser/bad-category1.mal", "bad-category1.mal:2:3:syntax error: expected '}', found 'info'");
  }

  @Test
  public void testBadAsset() {
    assertSyntaxError("parser/bad-asset1.mal", "bad-asset1.mal:2:3:syntax error: expected '}', found identifier");
    assertSyntaxError("parser/bad-asset2.mal", "bad-asset2.mal:2:12:syntax error: expected 'asset', found identifier");
    assertSyntaxError("parser/bad-asset3.mal", "bad-asset3.mal:2:19:syntax error: expected identifier, found '{'");
    assertSyntaxError("parser/bad-asset4.mal", "bad-asset4.mal:3:5:syntax error: expected '&', '|', '#', 'E', or '!E', found 'info'");
  }

  @Test
  public void testBadAttackStep() {
    assertSyntaxError("parser/bad-attackstep1.mal", "bad-attackstep1.mal:3:9:syntax error: expected '&', '|', '#', 'E', or '!E', found '{'");
    assertSyntaxError("parser/bad-attackstep2.mal", "bad-attackstep2.mal:3:12:syntax error: expected ')', found ']'");
    assertSyntaxError("parser/bad-attackstep3.mal", "bad-attackstep3.mal:3:11:syntax error: expected ']', found ')'");
    assertSyntaxError("parser/bad-attackstep4.mal", "bad-attackstep4.mal:5:9:syntax error: expected identifier or '(', found 'info'");
    assertSyntaxError("parser/bad-attackstep5.mal", "bad-attackstep5.mal:5:9:syntax error: expected identifier or '(', found 'info'");
  }

  @Test
  public void testBadAssociation() {
    assertSyntaxError("parser/bad-association1.mal", "bad-association1.mal:2:11:syntax error: expected '<--', found '<-'");
    assertSyntaxError("parser/bad-association2.mal", "bad-association2.mal:2:6:syntax error: expected identifier, found integer literal");
    assertSyntaxError("parser/bad-association3.mal", "bad-association3.mal:2:9:syntax error: expected '0', '1', or '*', found integer literal");
    assertSyntaxError("parser/bad-association4.mal", "bad-association4.mal:2:21:syntax error: Invalid multiplicity '0'");
    assertSyntaxError("parser/bad-association5.mal", "bad-association5.mal:2:21:syntax error: Invalid multiplicity '1..0'");
  }
}

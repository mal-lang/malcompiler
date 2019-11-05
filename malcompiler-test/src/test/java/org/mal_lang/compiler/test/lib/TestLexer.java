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
import static org.mal_lang.compiler.test.lib.AssertToken.assertGetLexerClassPath;
import static org.mal_lang.compiler.test.lib.AssertToken.assertTokenTypes;
import static org.mal_lang.compiler.test.lib.AssertToken.assertTokens;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.Lexer;
import org.mal_lang.compiler.lib.Position;
import org.mal_lang.compiler.lib.Token;
import org.mal_lang.compiler.lib.TokenType;
import org.mal_lang.compiler.test.MalTest;

public class TestLexer extends MalTest {
  @Test
  public void testLexerFileNotExist() {
    try {
      new Lexer(new File("nothing"));
      fail("File \"nothing\" should cause an IOException");
    } catch (Exception e) {
      assertTrue(e instanceof IOException);
      assertEquals("nothing: No such file or directory", e.getMessage());
    }
  }

  @Test
  public void testLexerFileExist() throws IOException {
    assertGetLexerClassPath("lexer/tokens.txt");
  }

  @Test
  public void testLexerTokens() throws IOException, CompilerException {
    TokenType[] tokenTypes = {
      TokenType.HASH,
      TokenType.COLON,
      TokenType.LCURLY,
      TokenType.RCURLY,
      TokenType.INHERIT,
      TokenType.OVERRIDE,
      TokenType.ALL,
      TokenType.ANY,
      TokenType.NOTEXIST,
      TokenType.AT,
      TokenType.LBRACKET,
      TokenType.RBRACKET,
      TokenType.LPAREN,
      TokenType.RPAREN,
      TokenType.COMMA,
      TokenType.REQUIRE,
      TokenType.ASSIGN,
      TokenType.UNION,
      TokenType.INTERSECT,
      TokenType.DOT,
      TokenType.RANGE,
      TokenType.STAR,
      TokenType.PLUS,
      TokenType.MINUS,
      TokenType.DIVIDE,
      TokenType.POWER,
      TokenType.LARROW,
      TokenType.RARROW,
      TokenType.EOF
    };
    assertTokenTypes(tokenTypes, "lexer/tokens.txt");
  }

  @Test
  public void testLexerKeywords() throws CompilerException {
    TokenType[] tokenTypes = {
      TokenType.INCLUDE,
      TokenType.INFO,
      TokenType.CATEGORY,
      TokenType.ABSTRACT,
      TokenType.ASSET,
      TokenType.EXTENDS,
      TokenType.ASSOCIATIONS,
      TokenType.LET,
      TokenType.EXIST,
      TokenType.C,
      TokenType.I,
      TokenType.A,
      TokenType.EOF
    };
    assertTokenTypes(tokenTypes, "lexer/keywords.txt");
  }

  @Test
  public void testLexerLexemes() throws CompilerException {
    TokenType[] tokenTypes = {
      TokenType.STRING,
      TokenType.STRING,
      TokenType.INT,
      TokenType.FLOAT,
      TokenType.ID,
      TokenType.EOF
    };
    assertTokenTypes(tokenTypes, "lexer/lexemes.txt");
  }

  private void assertSyntaxError(String filename, Position pos, String error) {
    try {
      assertGetLexerClassPath(filename).next();
      fail(String.format("File \"%s\" should have syntax errors", filename));
    } catch (Exception e) {
      assertTrue(e instanceof CompilerException);
      assertEquals("There were syntax errors", e.getMessage());
      assertEmptyOut();
      assertEquals(String.format("[LEXER ERROR] %s %s%n", pos.posString(), error), getPlainErr());
    }
  }

  @Test
  public void testLexerInvalid() {
    assertSyntaxError(
        "lexer/invalid.txt", new Position("invalid.txt", 1, 1), "Expected '-' or '--'");
  }

  @Test
  public void testLexerUnterminatedString() {
    assertSyntaxError(
        "lexer/unterminated_string.txt",
        new Position("unterminated_string.txt", 1, 1),
        "Unterminated string starting at <unterminated_string.txt:1:1>");
  }

  @Test
  public void testLexerUnterminatedComment() {
    assertSyntaxError(
        "lexer/unterminated_comment.txt",
        new Position("unterminated_comment.txt", 2, 1),
        "Unterminated comment starting at <unterminated_comment.txt:1:1>");
  }

  @Test
  public void testLexerInvalidEscapeSequence() {
    assertSyntaxError(
        "lexer/invalid_escape_sequence.txt",
        new Position("invalid_escape_sequence.txt", 1, 1),
        "Invalid escape sequence '\\a'");
  }

  @Test
  public void testLexerUnicode() {
    Token[] tokens = {
      new Token(TokenType.STRING, "unicode.txt", 1, 1, "Unicode character: ä"),
      new Token(TokenType.EOF, "unicode.txt", 2, 1)
    };
    assertTokens(tokens, "lexer/unicode.txt");
  }

  @Test
  public void testLexerBadUnicode1() {
    assertSyntaxError(
        "lexer/bad-unicode-1.txt",
        new Position("bad-unicode-1.txt", 1, 1),
        "Invalid escape byte 0xC3");
  }

  @Test
  public void testLexerBadUnicode2() {
    assertSyntaxError(
        "lexer/bad-unicode-2.txt",
        new Position("bad-unicode-2.txt", 1, 1),
        "Unexpected token 0xC3");
  }
}

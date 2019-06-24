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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TestLexer {

  @Test
  public void testLexerFileNotExist() {
    try {
      new Lexer(new File("nothing"));
      fail();
    } catch (IOException e) {
      return;
    }
  }

  @Test
  public void testLexerFileExist() throws IOException {
    File input = new File(getClass().getClassLoader().getResource("lexer/tokens.txt").getFile());
    new Lexer(input);
  }

  @Test
  public void testLexerTokens() throws IOException, SyntaxError {
    File input = new File(getClass().getClassLoader().getResource("lexer/tokens.txt").getFile());
    Lexer lexer = new Lexer(input);
    List<TokenType> tokenTypes = Arrays.asList(TokenType.HASH, TokenType.COLON, TokenType.LCURLY, TokenType.RCURLY, TokenType.INHERIT, TokenType.OVERRIDE, TokenType.ALL, TokenType.ANY, TokenType.NOTEXIST, TokenType.LBRACKET, TokenType.RBRACKET, TokenType.LPAREN, TokenType.RPAREN, TokenType.COMMA, TokenType.REQUIRE, TokenType.ASSIGN, TokenType.UNION, TokenType.INTERSECT, TokenType.DOT, TokenType.RANGE, TokenType.STAR, TokenType.PLUS, TokenType.MINUS, TokenType.DIVIDE, TokenType.POWER, TokenType.LARROW, TokenType.RARROW, TokenType.EOF);
    for(TokenType tokenType : tokenTypes) {
      assertEquals(tokenType, lexer.next().type);
    }
  }

  @Test
  public void testLexerKeywords() throws IOException, SyntaxError {
    File input = new File(getClass().getClassLoader().getResource("lexer/keywords.txt").getFile());
    Lexer lexer = new Lexer(input);
    List<TokenType> tokenTypes = Arrays.asList(TokenType.INCLUDE, TokenType.INFO, TokenType.ASSUMPTIONS, TokenType.RATIONALE, TokenType.CATEGORY, TokenType.ABSTRACT, TokenType.ASSET, TokenType.EXTENDS, TokenType.ASSOCIATIONS, TokenType.LET, TokenType.EXIST, TokenType.EOF);
    for(TokenType tokenType : tokenTypes) {
      assertEquals(tokenType, lexer.next().type);
    }
  }

  @Test
  public void testLexerLexemes() throws IOException, SyntaxError {
    File input = new File(getClass().getClassLoader().getResource("lexer/lexemes.txt").getFile());
    Lexer lexer = new Lexer(input);
    List<TokenType> tokenTypes = Arrays.asList(TokenType.STRING, TokenType.STRING, TokenType.INT, TokenType.FLOAT, TokenType.ID, TokenType.EOF);
    for(TokenType tokenType : tokenTypes) {
      assertEquals(tokenType, lexer.next().type);
    }
  }

  @Test
  public void testLexerInvalid() throws IOException {
    File input = new File(getClass().getClassLoader().getResource("lexer/invalid.txt").getFile());
    Lexer lexer = new Lexer(input);
    try {
      lexer.next();
      fail();
    } catch (SyntaxError e) {
      return;
    }
  }

  @Test
  public void testLexerUnterminatedString() throws IOException {
    File input = new File(getClass().getClassLoader().getResource("lexer/unterminated_string.txt").getFile());
    Lexer lexer = new Lexer(input);
    try {
      lexer.next();
      fail();
    } catch (SyntaxError e) {
      return;
    }
  }

  @Test
  public void testLexerUnterminatedComment() throws IOException {
    File input = new File(getClass().getClassLoader().getResource("lexer/unterminated_comment.txt").getFile());
    Lexer lexer = new Lexer(input);
    try {
      lexer.next();
      fail();
    } catch (SyntaxError e) {
      return;
    }
  }

  @Test
  public void testLexerInvalidEscapeSequence() throws IOException {
    File input = new File(getClass().getClassLoader().getResource("lexer/invalid_escape_sequence.txt").getFile());
    Lexer lexer = new Lexer(input);
    try {
      lexer.next();
      fail();
    } catch (SyntaxError e) {
      return;
    }
  }
}

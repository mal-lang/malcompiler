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
package org.mal_lang.mal.test.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mal_lang.mal.test.MalTest.getFileClassPath;

import java.io.IOException;
import org.mal_lang.mal.lib.CompilerException;
import org.mal_lang.mal.lib.Lexer;
import org.mal_lang.mal.lib.Token;
import org.mal_lang.mal.lib.TokenType;

public final class AssertToken {
  // Prevent instantiation
  private AssertToken() {}

  public static Lexer getLexerClassPath(String filename) throws IOException {
    return new Lexer(getFileClassPath(filename));
  }

  public static Lexer assertGetLexerClassPath(String filename) {
    try {
      return getLexerClassPath(filename);
    } catch (IOException e) {
      fail(e.getMessage());
    }
    throw new RuntimeException("This should be unreachable");
  }

  public static void assertTokenTypes(TokenType[] tokenTypes, String filename) {
    try {
      var lex = assertGetLexerClassPath(filename);
      for (var tokenType : tokenTypes) {
        assertEquals(tokenType, lex.next().type);
      }
    } catch (CompilerException e) {
      fail(e.getMessage());
    }
  }

  private static void assertToken(Token expected, Token actual) {
    assertEquals(expected.filename, actual.filename, actual.posString());
    assertEquals(expected.line, actual.line, actual.posString());
    assertEquals(expected.col, actual.col, actual.posString());
    assertEquals(expected.type, actual.type, actual.posString());
    assertEquals(expected.stringValue, actual.stringValue, actual.posString());
    assertEquals(expected.doubleValue, actual.doubleValue, actual.posString());
    assertEquals(expected.intValue, actual.intValue, actual.posString());
  }

  public static void assertTokens(Token[] tokens, String filename) {
    try {
      var lex = assertGetLexerClassPath(filename);
      for (var token : tokens) {
        assertToken(token, lex.next());
      }
    } catch (CompilerException e) {
      fail(e.getMessage());
    }
  }
}

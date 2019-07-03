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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Lexer {
  private MalLogger LOGGER;
  private String filename;
  private byte[] input;
  private int index;
  private int col;
  private int line;
  private String lexeme;

  private static Map<String, TokenType> keywords;
  static {
    keywords = new HashMap<>();
    keywords.put("include", TokenType.INCLUDE);
    keywords.put("info", TokenType.INFO);
    keywords.put("assumptions", TokenType.ASSUMPTIONS);
    keywords.put("rationale", TokenType.RATIONALE);
    keywords.put("category", TokenType.CATEGORY);
    keywords.put("abstract", TokenType.ABSTRACT);
    keywords.put("asset", TokenType.ASSET);
    keywords.put("extends", TokenType.EXTENDS);
    keywords.put("associations", TokenType.ASSOCIATIONS);
    keywords.put("let", TokenType.LET);
    keywords.put("E", TokenType.EXIST);
  }

  private static Map<String, Character> escapeSequences;
  static {
    escapeSequences = new HashMap<>();
    escapeSequences.put("\\b", '\b');
    escapeSequences.put("\\n", '\n');
    escapeSequences.put("\\t", '\t');
    escapeSequences.put("\\r", '\r');
    escapeSequences.put("\\f", '\f');
    escapeSequences.put("\\\"", '"');
    escapeSequences.put("\\\\", '\\');
  }

  public Lexer(File file) throws IOException {
    this(file, file.getName(), false, false);
  }

  public Lexer(File file, boolean verbose, boolean debug) throws IOException {
    this(file, file.getName(), verbose, debug);
  }

  public Lexer(File file, String relativeName) throws IOException {
    this(file, relativeName, false, false);
  }

  public Lexer(File file, String relativeName, boolean verbose, boolean debug) throws IOException {
    LOGGER = new MalLogger("LEXER", verbose, debug);
    LOGGER.debug(String.format("Creating lexer with file '%s'", relativeName));
    if (!file.exists()) {
      throw new IOException(String.format("%s: No such file or directory", relativeName));
    }
    filename = relativeName;
    input = Files.readAllBytes(file.toPath());
    line = 1;
    col = 1;
    index = 0;
  }

  public Token next() throws CompilerException {
    lexeme = "";
    char c = consume();
    switch (c) {
      case '\0':
        return createToken(TokenType.EOF);
      case ' ':
      case '\t':
      case '\r':
      case '\n':
        return next();
      case '#':
        return createToken(TokenType.HASH);
      case ':':
        return createToken(TokenType.COLON);
      case '{':
        return createToken(TokenType.LCURLY);
      case '}':
        return createToken(TokenType.RCURLY);
      case '+':
        if (peek() == '>') {
          consume();
          return createToken(TokenType.INHERIT);
        } else {
          return createToken(TokenType.PLUS);
        }
      case '-':
        if (peek() == '>') {
          consume();
          return createToken(TokenType.OVERRIDE);
        } else if (peek(2).equals("->")) {
          consume(2);
          return createToken(TokenType.RARROW);
        } else {
          return createToken(TokenType.MINUS);
        }
      case '&':
        return createToken(TokenType.ALL);
      case '|':
        return createToken(TokenType.ANY);
      case '!':
        if (peek() == 'E') {
          consume();
          return createToken(TokenType.NOTEXIST);
        } else {
          throw exception("Expected 'E'");
        }
      case '[':
        return createToken(TokenType.LBRACKET);
      case ']':
        return createToken(TokenType.RBRACKET);
      case '(':
        return createToken(TokenType.LPAREN);
      case ')':
        return createToken(TokenType.RPAREN);
      case ',':
        return createToken(TokenType.COMMA);
      case '<':
        if (peek(2).equals("--")) {
          consume(2);
          return createToken(TokenType.LARROW);
        } else if (peek() == '-') {
          consume();
          return createToken(TokenType.REQUIRE);
        } else {
          throw exception("Expected '-' or '--'");
        }
      case '=':
        return createToken(TokenType.ASSIGN);
      case '\\':
        if (peek() == '/') {
          consume();
          return createToken(TokenType.UNION);
        } else {
          throw exception("Expected '/'");
        }
      case '/':
        if (peek() == '\\') {
          consume();
          return createToken(TokenType.INTERSECT);
        } else if (peek() == '/') {
          while (peek() != '\n' && peek() != '\0') {
            consume();
          }
          return next();
        } else if (peek() == '*') {
          int startline = line;
          int startcol = col;
          consume();
          while (!peek(2).equals("*/")) {
            consume();
            if (peek() == '\0') {
              throw exception(
                  String.format("Unterminated comment starting at %s", new Position(filename, startline, startcol)));
            }
          }
          consume(2);
          return next();
        } else {
          return createToken(TokenType.DIVIDE);
        }
      case '.':
        if (peek() == '.') {
          consume();
          return createToken(TokenType.RANGE);
        } else {
          return createToken(TokenType.DOT);
        }
      case '*':
        return createToken(TokenType.STAR);
      case '^':
        return createToken(TokenType.POWER);
      case '"':
        int startline = line;
        int startcol = col;
        while (peek() != '"') {
          if (peek() == '\\') {
            String escapeSequence = peek(2);
            if (!escapeSequences.containsKey(escapeSequence)) {
              throw exception(String.format("Invalid escape sequence '%s'", escapeSequence));
            }
            lexeme += escapeSequences.get(escapeSequence);
            index += 2;
          } else if (peek() == '\0' || peek() == '\n') {
            throw exception(
                String.format("Unterminated string starting at %s", new Position(filename, startline, startcol)));
          } else {
            consume();
          }
        }
        index++;
        return new Token(TokenType.STRING, filename, startline, startcol, lexeme.substring(1));
      default:
        if (isAlpha(c)) {
          while (isAlphaNumeric(peek())) {
            consume();
          }
          if (keywords.containsKey(lexeme)) {
            return createToken(keywords.get(lexeme));
          } else {
            return createToken(TokenType.ID);
          }
        } else if (isDigit(c)) {
          while (isDigit(peek())) {
            consume();
          }
          if (peek(2).equals("..") || peek() != '.') {
            return createToken(TokenType.INT);
          } else if (peek() == '.') {
            consume();
            while (isDigit(peek())) {
              consume();
            }
            return createToken(TokenType.FLOAT);
          }
        }
        throw exception(String.format("Unexpected token '%s'", lexeme));
    }
  }

  private String consume(int n) {
    String s = "";
    while (n-- > 0) {
      s += consume();
    }
    return s;
  }

  private char consume() {
    if (index > 0 && index - 1 < input.length) {
      if ((char) input[index - 1] == '\n') {
        line++;
        col = 1;
      } else {
        col++;
      }
    }
    if (index < input.length) {
      char c = (char) input[index++];
      lexeme += c;
      return c;
    } else {
      return '\0';
    }
  }

  private String peek(int n) {
    String s = "";
    for (int i = 0; i < n && index + i < input.length; i++) {
      s += (char) input[index + i];
    }
    return s;
  }

  private char peek() {
    if (index < input.length) {
      return (char) input[index];
    } else {
      return '\0';
    }
  }

  private Token createToken(TokenType type) {
    int startcol = col - (lexeme.length() - 1);
    switch (type) {
      case INT:
        return new Token(type, filename, line, startcol, Integer.parseInt(lexeme));
      case FLOAT:
        return new Token(type, filename, line, startcol, Double.parseDouble(lexeme));
      case ID:
        return new Token(type, filename, line, startcol, lexeme);
      default:
        return new Token(type, filename, line, startcol);
    }
  }

  private CompilerException exception(String msg) {
    LOGGER.error(new Position(filename, line, col), msg);
    return new CompilerException("There were syntax errors");
  }

  private static boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }

  private static boolean isAlpha(char c) {
    return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || c == '_';
  }

  private static boolean isAlphaNumeric(char c) {
    return isDigit(c) || isAlpha(c);
  }

}

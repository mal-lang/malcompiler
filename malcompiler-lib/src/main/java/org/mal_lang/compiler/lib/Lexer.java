/*
 * Copyright 2019-2022 Foreseeti AB
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
package org.mal_lang.compiler.lib;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Lexer {
  private MalLogger LOGGER;
  private String filename;
  private byte[] input;
  private int index;
  private int line;
  private int col;
  private int startLine;
  private int startCol;
  private List<Byte> lexeme;
  private List<Token> comments = new ArrayList<>();
  private boolean eof;

  private static Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("include", TokenType.INCLUDE);
    keywords.put("info", TokenType.INFO);
    keywords.put("category", TokenType.CATEGORY);
    keywords.put("abstract", TokenType.ABSTRACT);
    keywords.put("asset", TokenType.ASSET);
    keywords.put("extends", TokenType.EXTENDS);
    keywords.put("associations", TokenType.ASSOCIATIONS);
    keywords.put("let", TokenType.LET);
    keywords.put("E", TokenType.EXIST);
    keywords.put("C", TokenType.C);
    keywords.put("I", TokenType.I);
    keywords.put("A", TokenType.A);
  }

  private static Map<String, Byte> escapeSequences;

  static {
    escapeSequences = new HashMap<>();
    escapeSequences.put("\\b", (byte) '\b');
    escapeSequences.put("\\n", (byte) '\n');
    escapeSequences.put("\\t", (byte) '\t');
    escapeSequences.put("\\r", (byte) '\r');
    escapeSequences.put("\\f", (byte) '\f');
    escapeSequences.put("\\\"", (byte) '"');
    escapeSequences.put("\\\\", (byte) '\\');
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
    Locale.setDefault(Locale.ROOT);
    LOGGER = new MalLogger("LEXER", verbose, debug);
    try {
      LOGGER.debug(String.format("Creating lexer with file '%s'", relativeName));
      if (!file.exists()) {
        throw new IOException(String.format("%s: No such file or directory", relativeName));
      }
      this.filename = relativeName;
      this.input = Files.readAllBytes(file.toPath());
      this.index = 0;
      this.line = 1;
      this.col = 1;
      this.eof = input.length == 0;
    } catch (IOException e) {
      LOGGER.print();
      throw e;
    }
  }

  public static boolean syntacticallyEqual(Lexer l1, Lexer l2) {
    try {
      var tok1 = l1.next();
      var tok2 = l2.next();
      while (tok1.type != TokenType.EOF && tok2.type != TokenType.EOF) {
        if (tok1.type != tok2.type
            || !tok1.stringValue.equals(tok2.stringValue)
            || tok1.intValue != tok2.intValue
            || tok1.doubleValue != tok2.doubleValue) {
          return false;
        }
        tok1 = l1.next();
        tok2 = l2.next();
      }
      return tok1.type == TokenType.EOF && tok2.type == TokenType.EOF;
    } catch (CompilerException e) {
      return false;
    }
  }

  private String getLexemeString() {
    byte[] byteArray = new byte[lexeme.size()];
    for (int i = 0; i < lexeme.size(); i++) {
      byteArray[i] = lexeme.get(i).byteValue();
    }
    return new String(byteArray, StandardCharsets.UTF_8);
  }

  public Token next() throws CompilerException {
    startLine = line;
    startCol = col;
    lexeme = new ArrayList<>();
    if (eof) {
      LOGGER.print();
      return createToken(TokenType.EOF);
    }
    byte c = consume();
    switch (c) {
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
        if (peek('>')) {
          consume();
          return createToken(TokenType.INHERIT);
        } else {
          return createToken(TokenType.PLUS);
        }
      case '-':
        if (peek('>')) {
          consume();
          return createToken(TokenType.OVERRIDE);
        } else if (peek("->")) {
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
        if (peek('E')) {
          consume();
          return createToken(TokenType.NOTEXIST);
        } else {
          throw exception("Expected 'E'");
        }
      case '@':
        return createToken(TokenType.AT);
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
        if (peek("--")) {
          consume(2);
          return createToken(TokenType.LARROW);
        } else if (peek('-')) {
          consume();
          return createToken(TokenType.REQUIRE);
        } else {
          throw exception("Expected '-' or '--'");
        }
      case '=':
        return createToken(TokenType.ASSIGN);
      case '\\':
        if (peek('/')) {
          consume();
          return createToken(TokenType.UNION);
        } else {
          throw exception("Expected '/'");
        }
      case '/':
        if (peek('\\')) {
          consume();
          return createToken(TokenType.INTERSECT);
        } else if (peek('/')) {
          while (!eof && !peek('\n') && !peek('\r')) {
            consume();
          }
          createComment(TokenType.SINGLECOMMENT);
          return next();
        } else if (peek('*')) {
          consume();
          while (!peek("*/")) {
            if (eof) {
              throw exception(
                  String.format(
                      "Unterminated comment starting at %s",
                      new Position(filename, startLine, startCol)));
            }
            consume();
          }
          consume(2);
          createComment(TokenType.MULTICOMMENT);
          return next();
        } else {
          return createToken(TokenType.DIVIDE);
        }
      case '.':
        if (peek('.')) {
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
        if (peek("\"\"")) {
          consume(2);
          while (peek(' ') || peek('\t')) {
            consume();
          }
          if (peek('\r')) {
            consume();
            if (peek('\n')) {
              consume();
            }
          } else if (peek('\n')) {
            consume();
          } else {
            throw exception("Expected line terminator");
          }
          lexeme = new ArrayList<>();
          while (!peek("\"\"\"")) {
            if (eof) {
              throw exception(
                  String.format(
                      "Unterminated multi-line string starting at %s",
                      new Position(filename, startLine, startCol)));
            } else if (peek('\r')) {
              consume();
              lexeme = lexeme.subList(0, lexeme.size() - 1);
              lexeme.add((byte) '\n');
              if (peek('\n')) {
                consume();
                lexeme = lexeme.subList(0, lexeme.size() - 1);
              }
            } else if (peek('\\')) {
              consume();
              if (input[index] < 32 || input[index] > 126) {
                throw exception(String.format("Invalid escape byte 0x%02X", input[index]));
              }
              consume();
              var lexemeString = getLexemeString();
              String escapeSequence = lexemeString.substring(lexemeString.length() - 2);
              lexeme = lexeme.subList(0, lexeme.size() - 2);
              if (!escapeSequences.containsKey(escapeSequence)) {
                throw exception(String.format("Invalid escape sequence '%s'", escapeSequence));
              }
              lexeme.add(escapeSequences.get(escapeSequence));
            } else {
              consume();
            }
          }
          consume(3);
          lexeme = lexeme.subList(0, lexeme.size() - 3);
          return createToken(TokenType.MULTI_STRING);
        }
        while (!peek('"')) {
          if (peek('\\')) {
            consume();
            if (eof || peek('\n')) {
              throw exception(
                  String.format(
                      "Unterminated string starting at %s",
                      new Position(filename, startLine, startCol)));
            }
            if (input[index] < 32 || input[index] > 126) {
              throw exception(String.format("Invalid escape byte 0x%02X", input[index]));
            }
            consume();
            var lexemeString = getLexemeString();
            String escapeSequence = lexemeString.substring(lexemeString.length() - 2);
            lexeme = lexeme.subList(0, lexeme.size() - 2);
            if (!escapeSequences.containsKey(escapeSequence)) {
              throw exception(String.format("Invalid escape sequence '%s'", escapeSequence));
            }
            lexeme.add(escapeSequences.get(escapeSequence));
          } else if (eof || peek('\n')) {
            throw exception(
                String.format(
                    "Unterminated string starting at %s",
                    new Position(filename, startLine, startCol)));
          } else {
            consume();
          }
        }
        consume();
        return createToken(TokenType.STRING);
      default:
        if (isAlpha(c)) {
          while (isAlphaNumeric()) {
            consume();
          }
          var lexemeString = getLexemeString();
          if (keywords.containsKey(lexemeString)) {
            return createToken(keywords.get(lexemeString));
          } else {
            return createToken(TokenType.ID);
          }
        } else if (isDigit(c)) {
          while (isDigit()) {
            consume();
          }
          if (peek("..") || !peek('.')) {
            return createToken(TokenType.INT);
          } else if (peek('.')) {
            consume();
            while (isDigit()) {
              consume();
            }
            return createToken(TokenType.FLOAT);
          }
        }
        if (c < 0) {
          throw exception(String.format("Unexpected token 0x%02X", c));
        } else {
          throw exception(String.format("Unexpected token '%c'", (char) c));
        }
    }
  }

  private void consume(int n) {
    for (int i = 0; i < n; i++) {
      consume();
    }
  }

  private byte consume() {
    if (eof) {
      throw new RuntimeException("Consuming past end-of-file");
    }
    if (input[index] == (byte) '\n') {
      line++;
      col = 1;
    } else {
      col++;
    }
    var c = input[index++];
    lexeme.add(c);
    if (index == input.length) {
      eof = true;
    }
    return c;
  }

  private boolean peek(String s) {
    var bytes = s.getBytes();
    if (input.length - index < bytes.length) {
      return false;
    }
    for (int i = 0; i < bytes.length; i++) {
      if (bytes[i] != input[index + i]) {
        return false;
      }
    }
    return true;
  }

  private boolean peek(char c) {
    return peek((byte) c);
  }

  private boolean peek(byte c) {
    if (eof) {
      return false;
    } else {
      return c == input[index];
    }
  }

  private void createComment(TokenType type) {
    var lexemeString = getLexemeString();
    lexemeString = lexemeString.substring(2, lexemeString.length());
    if (type == TokenType.MULTICOMMENT) {
      lexemeString = lexemeString.substring(0, lexemeString.length() - 2);
    }
    comments.add(new Token(type, filename, startLine, startCol, lexemeString));
  }

  private Token createRawToken(TokenType type) {
    switch (type) {
      case INT:
        return new Token(type, filename, startLine, startCol, Integer.parseInt(getLexemeString()));
      case FLOAT:
        return new Token(
            type, filename, startLine, startCol, Double.parseDouble(getLexemeString()));
      case ID:
        return new Token(type, filename, startLine, startCol, getLexemeString());
      case STRING:
        {
          var lexemeString = getLexemeString();
          return new Token(
              type,
              filename,
              startLine,
              startCol,
              lexemeString.substring(1, lexemeString.length() - 1));
        }
      case MULTI_STRING:
        {
          var lexemeString = getLexemeString();
          var lines = lexemeString.split("\\R");

          // Find minIndent
          int minIndent = -1;
          for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            if (!line.isBlank() || i + 1 == lines.length) {
              var indent = 0;
              for (int j = 0; j < line.length(); j++) {
                if (!Character.isWhitespace(line.charAt(j))) {
                  break;
                }
                indent += 1;
              }
              if (minIndent == -1 || indent < minIndent) {
                minIndent = indent;
              }
            }
          }

          // Strip lines
          var newLines = new String[lines.length];
          for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            if (line.isBlank()) {
              newLines[i] = "";
            } else {
              if (minIndent != -1) {
                line = line.substring(minIndent);
              }
              newLines[i] = line.stripTrailing();
            }
          }

          return new Token(
              TokenType.STRING, filename, startLine, startCol, String.join("\n", newLines));
        }
      default:
        return new Token(type, filename, startLine, startCol);
    }
  }

  private void readTrailingComments() throws CompilerException {
    // Trailing comments are all comments followed on the same line as the previous
    // token, including comments that follow previous trailing comments by exactly 1
    // line.
    startLine = line;
    startCol = col;
    lexeme = new ArrayList<>();
    if (eof || peek('\n')) {
      return;
    }
    byte c = consume();
    switch (c) {
      case ' ':
      case '\t':
        readTrailingComments();
        return;
      case '/':
        if (peek('/')) {
          while (!eof && !peek('\n') && !peek('\r')) {
            consume();
          }
          createComment(TokenType.SINGLECOMMENT);
          if (peek("\r\n")) {
            consume(2);
            readTrailingComments();
          } else if (peek('\n')) {
            consume();
            readTrailingComments();
          }
          return;
        } else if (peek('*')) {
          consume();
          while (!peek("*/")) {
            if (eof) {
              throw exception(
                  String.format(
                      "Unterminated comment starting at %s",
                      new Position(filename, startLine, startCol)));
            }
            consume();
          }
          consume(2);
          createComment(TokenType.MULTICOMMENT);
          readTrailingComments();
          return;
        }
        // Not a comment, we want to fall-through
      default:
        index--;
        col--;
        eof = false;
        return;
    }
  }

  private Token createToken(TokenType type) throws CompilerException {
    var token = createRawToken(type);
    var preComments = List.copyOf(comments);
    comments.clear();
    readTrailingComments();
    var postComments = List.copyOf(comments);
    comments.clear();
    return new Token(token, preComments, postComments);
  }

  private CompilerException exception(String msg) {
    Position pos = null;
    if (eof) {
      pos = new Position(filename, line, col);
    } else {
      pos = new Position(filename, startLine, startCol);
    }
    LOGGER.error(pos, msg);
    LOGGER.print();
    return new CompilerException("There were syntax errors");
  }

  private boolean isDigit() {
    if (eof) {
      return false;
    }
    return isDigit(input[index]);
  }

  private boolean isDigit(byte c) {
    return '0' <= c && c <= '9';
  }

  private boolean isAlpha(byte c) {
    return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || c == '_';
  }

  private boolean isAlphaNumeric() {
    if (eof) {
      return false;
    }
    return isAlphaNumeric(input[index]);
  }

  private boolean isAlphaNumeric(byte c) {
    return isDigit(c) || isAlpha(c);
  }
}

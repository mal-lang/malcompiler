/*
 * Copyright 2020 Foreseeti AB
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
package org.mal_lang.formatter;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.Lexer;
import org.mal_lang.compiler.lib.TokenType;

public class Parser {
  private Lexer lex;
  private org.mal_lang.compiler.lib.Token tok;
  private Deque<Token.Base> tokens;
  private int defaultIndent;

  public Parser(File file, Deque<Token.Base> tokens, int indent) throws IOException {
    var canonicalFile = file.getCanonicalFile();
    this.lex = new Lexer(canonicalFile);
    this.tokens = tokens;
    this.defaultIndent = indent;
  }

  private void next() {
    try {
      tok = lex.next();
    } catch (CompilerException e) {
      throw new RuntimeException(e);
    }
  }

  public void parse() {
    createBegin(Token.BlockBreakType.ALWAYS, 0);
    next();
    boolean first = true;
    loop:
    while (true) {
      switch (tok.type) {
        case CATEGORY:
          parseCategory(first);
          first = false;
          break;
        case ASSOCIATIONS:
          parseAssociations();
          first = false;
          break;
        case INCLUDE:
          parseInclude();
          first = false;
          break;
        case HASH:
          parseDefine();
          first = false;
          break;
        case EOF:
          break loop;
        default:
          throw new RuntimeException(tok.type + "");
      }
    }
    createEnd();
  }

  private void parseAssociations() {
    createBreak("", 0);
    createBegin(Token.BlockBreakType.ALWAYS, defaultIndent);

    createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    consumeToken("associations");
    createEnd();
    createBreak("", -defaultIndent);
    consumeToken("{");
    if (tok.type == TokenType.ID) {
      parseAsssociations1();
    }
    createBreak("", -defaultIndent);
    consumeToken("}");
    createEnd();
    createBreak("", 0);
  }

  private void parseAsssociations1() {
    var id = tok;
    next();
    parseAssociation(id);
    while (tok.type == TokenType.ID) {
      id = tok;
      next();
      if (tok.type == TokenType.INFO) {
        parseMeta2(id);
      } else {
        createEnd(); // closing association block
        parseAssociation(id);
      }
    }
    createEnd(); // Closing association block
  }

  private void parseAssociation(org.mal_lang.compiler.lib.Token prev) {
    createBreak("", 0);
    createBegin(Token.BlockBreakType.ALWAYS, defaultIndent);
    createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    createToken(prev, prev.stringValue);
    createBreak(" ", 0);
    consumeToken("[");
    consumeToken(tok.stringValue);
    consumeToken("]");
    createBreak(" ", 0);
    parseMult();
    createBreak(" ", 0);
    consumeToken("<--");
    createBreak(" ", 0);
    consumeToken(tok.stringValue);
    createBreak(" ", 0);
    consumeToken("-->");
    createBreak(" ", 0);
    parseMult();
    createBreak(" ", 0);
    consumeToken("[");
    consumeToken(tok.stringValue);
    consumeToken("]");
    createBreak(" ", 0);
    consumeToken(tok.stringValue);
    createEnd();
    // createEnd();
    // block will be closed above
  }

  private void parseMult() {
    parseMultUnit();
    if (tok.type == TokenType.RANGE) {
      consumeToken("..");
      parseMultUnit();
    }
  }

  private void parseMultUnit() {
    if (tok.type == TokenType.INT) {
      consumeToken(Integer.toString(tok.intValue));
    } else {
      consumeToken("*");
    }
  }

  private void parseDefine() {
    createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    consumeToken("#");
    consumeToken(tok.stringValue);
    consumeToken(":");
    createBreak(" ", 0);
    consumeToken("\"" + tok.stringValue + "\"");
    createEnd();
    createBreak("", 0);
  }

  private void parseInclude() {
    createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    consumeToken("include");
    createBreak(" ", 0);
    consumeToken("\"" + tok.stringValue + "\"");
    createEnd();
    createBreak("", 0);
  }

  private void createToken(org.mal_lang.compiler.lib.Token token, String value) {
    for (int i = 0; i < token.preComments.size(); i++) {
      var comment = token.preComments.get(i);
      if (i < token.preComments.size() - 1) {
        createComment(comment, false, false);
      } else {
        int endLine = comment.line + comment.stringValue.split("\n").length - 1;
        createComment(comment, false, endLine != token.line);
      }
    }
    if (!token.postComments.isEmpty()) {
      // we have at least 1 trailing comment
      createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
      createString(value);
      for (var comment : token.postComments) {
        createComment(comment, true, false);
      }
      createEnd();
    } else {
      createString(value);
    }
  }

  private void consumeToken(String value) {
    createToken(tok, value);
    next();
  }

  private void createString(String value) {
    tokens.push(new Token.String(value));
  }

  private void createComment(
      org.mal_lang.compiler.lib.Token comment, boolean space, boolean newline) {
    var singleComment = comment.type == TokenType.SINGLECOMMENT;
    if (newline || singleComment) {
      createBegin(Token.BlockBreakType.ALWAYS, 0);
    }
    if (comment.type == TokenType.SINGLECOMMENT) {
      createString(String.format("%s//%s", space ? " " : "", comment.stringValue));
    } else {
      createString(String.format("%s/*%s*/", space ? " " : "", comment.stringValue));
    }
    if (newline || singleComment) {
      createBreak("", 0);
      createEnd();
    }
  }

  private Token.Begin createBegin(Token.BlockBreakType type, int indent) {
    var block = new Token.Begin(type, indent);
    tokens.push(block);
    return block;
  }

  private void createEnd() {
    tokens.push(new Token.End());
  }

  private void createBreak(String value, int indent) {
    tokens.push(new Token.Break(value, indent));
  }

  private void parseCategory(boolean first) {
    if (!first) {
      // Extra break if we are following a previous asset
      createBreak("", 0);
    }
    createBegin(Token.BlockBreakType.ALWAYS, defaultIndent);

    createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    consumeToken("category");
    createBreak(" ", defaultIndent);
    consumeToken(tok.stringValue);
    createEnd();

    parseMeta();

    createBreak("", -defaultIndent);
    consumeToken("{");
    parseAssets();
    createBreak("", -defaultIndent);
    consumeToken("}");
    createEnd();
    createBreak("", 0);
  }

  private void parseAssets() {
    boolean first = true;
    while (true) {
      switch (tok.type) {
        case ABSTRACT:
        case ASSET:
          parseAsset(first);
          first = false;
          break;
        default:
          return;
      }
    }
  }

  private void parseAsset(boolean first) {
    if (!first) {
      // Extra break if we are following a previous asset
      createBreak("", 0);
    }
    createBreak("", 0);
    createBegin(Token.BlockBreakType.ALWAYS, defaultIndent);
    createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    if (tok.type == TokenType.ABSTRACT) {
      consumeToken("abstract");
      createBreak(" ", 0);
    }
    consumeToken("asset");
    createBreak(" ", 0);
    consumeToken(tok.stringValue);
    if (tok.type == TokenType.EXTENDS) {
      createBreak(" ", 0);
      consumeToken("extends");
      createBreak(" ", 0);
      consumeToken(tok.stringValue);
    }
    createEnd();
    parseMeta();

    createBreak("", -defaultIndent);
    consumeToken("{");
    // ATTACKSTEPS
    first = true;
    loop:
    while (true) {
      switch (tok.type) {
        case LET:
          parseVariable();
          first = false;
          break;
        case RCURLY:
          break loop;
        default:
          parseAttackStep(first);
          first = false;
          break;
      }
    }
    createBreak("", -defaultIndent);
    consumeToken("}");
    createEnd();
  }

  private void parseVariable() {
    createBreak("", 0);
    createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    consumeToken("let");
    createBreak(" ", 0);
    consumeToken(tok.stringValue);
    createBreak(" ", 0);
    consumeToken("=");
    createBreak(" ", 0);
    parseExpr();
    createEnd();
  }

  private void parseAttackStep(boolean first) {
    // If we must break attackstep definition we want it all on the same line
    if (!first) {
      // Extra break if we are following a previous def
      createBreak("", 0);
    }
    createBreak("", 0);
    createBegin(Token.BlockBreakType.ALWAYS, defaultIndent);

    createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    {
      var value = tok.type.toString();
      consumeToken(value.substring(1, value.length() - 1));
      createBreak(" ", 0);
      consumeToken(tok.stringValue);

      while (tok.type == TokenType.AT) {
        createBreak(" ", 0);
        consumeToken("@");
        consumeToken(tok.stringValue);
      }

      if (tok.type == TokenType.LCURLY) {
        createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
        createBreak(" ", 0);
        consumeToken("{");
        if (tok.type != TokenType.RCURLY) {
          value = tok.type.toString();
          consumeToken(value.substring(1, value.length() - 1));
          while (tok.type == TokenType.COMMA) {
            consumeToken(",");
            createBreak(" ", 0);
            value = tok.type.toString();
            consumeToken(value.substring(1, value.length() - 1));
          }
        }
        consumeToken("}");
        createEnd();
      }
      if (tok.type == TokenType.LBRACKET) {
        createBreak(" ", 0);
        createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
        consumeToken("[");
        if (tok.type != TokenType.RBRACKET) {
          parseTTCExpr();
        }
        consumeToken("]");
        createEnd();
      }
      createEnd();
    }
    parseMeta();
    if (tok.type == TokenType.REQUIRE) {
      parseReachesOrRequires();
    }
    if (tok.type == TokenType.INHERIT || tok.type == TokenType.OVERRIDE) {
      parseReachesOrRequires();
    }
    createEnd();
  }

  private void parseTTCExpr() {
    parseTTCTerm();
    while (tok.type == TokenType.PLUS || tok.type == TokenType.MINUS) {
      createBreak(" ", 0);
      var value = tok.type.toString();
      consumeToken(value.substring(1, value.length() - 1));
      createBreak(" ", 0);
      parseTTCTerm();
    }
  }

  private void parseTTCTerm() {
    parseTTCFact();
    while (tok.type == TokenType.STAR || tok.type == TokenType.DIVIDE) {
      createBreak(" ", 0);
      var value = tok.type.toString();
      consumeToken(value.substring(1, value.length() - 1));
      createBreak(" ", 0);
      parseTTCFact();
    }
  }

  private void parseTTCFact() {
    parseTTCPrim();
    if (tok.type == TokenType.POWER) {
      createBreak(" ", 0);
      consumeToken("^");
      createBreak(" ", 0);
      parseTTCFact();
    }
  }

  private void parseTTCPrim() {
    if (tok.type == TokenType.ID) {
      consumeToken(tok.stringValue);
      if (tok.type == TokenType.LPAREN) {
        consumeToken("(");
        if (tok.type == TokenType.INT || tok.type == TokenType.FLOAT) {
          parseNumber();
          while (tok.type == TokenType.COMMA) {
            consumeToken(",");
            createBreak(" ", 0);
            parseNumber();
          }
        }
        consumeToken(")");
      }
    } else if (tok.type == TokenType.LPAREN) {
      consumeToken("(");
      parseTTCExpr();
      consumeToken(")");
    } else if (tok.type == TokenType.INT || tok.type == TokenType.FLOAT) {
      parseNumber();
    }
  }

  private void parseNumber() {
    if (tok.type == TokenType.INT) {
      consumeToken(Integer.toString(tok.intValue));
    } else {
      consumeToken(Double.toString(tok.doubleValue));
    }
  }

  private void parseReachesOrRequires() {
    createBreak("", 0);
    createBegin(Token.BlockBreakType.ALWAYS, 3);
    var value = tok.type.toString();
    consumeToken(value.substring(1, value.length() - 1));
    createString(" ");
    parseExpr();
    while (tok.type == TokenType.COMMA) {
      consumeToken(",");
      createBreak("", 0);
      parseExpr();
    }
    createEnd();
  }

  private void parseExpr() {
    var block = createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    parseSteps();
    while (tok.type == TokenType.UNION
        || tok.type == TokenType.INTERSECT
        || tok.type == TokenType.MINUS) {
      block.type = Token.BlockBreakType.CONSISTENT;
      createBreak(" ", 0);
      var value = tok.type.toString();
      consumeToken(value.substring(1, value.length() - 1));
      createBreak(" ", 0);
      parseSteps();
    }
    createEnd();
  }

  private void parseSteps() {
    createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    parseStep();
    while (tok.type == TokenType.DOT) {
      createBreak("", 0);
      consumeToken(".");
      parseStep();
    }
    createEnd();
  }

  private void parseStep() {
    if (tok.type == TokenType.LPAREN) {
      consumeToken("(");
      parseExpr();
      consumeToken(")");
    } else if (tok.type == TokenType.ID) {
      consumeToken(tok.stringValue);
      if (tok.type == TokenType.LPAREN) {
        consumeToken("(");
        consumeToken(")");
      }
    }
    while (tok.type == TokenType.STAR || tok.type == TokenType.LBRACKET) {
      if (tok.type == TokenType.STAR) {
        consumeToken("*");
      } else if (tok.type == TokenType.LBRACKET) {
        consumeToken("[");
        consumeToken(tok.stringValue);
        consumeToken("]");
      }
    }
  }

  private void parseMeta2(org.mal_lang.compiler.lib.Token prev) {
    createBreak("", 0);
    createBegin(Token.BlockBreakType.INCONSISTENT, defaultIndent);
    createToken(prev, prev.stringValue);
    createBreak(" ", 0);
    consumeToken("info");
    consumeToken(":");
    createBreak(" ", 0);
    consumeToken("\"" + tok.stringValue + "\"");
    createEnd();
  }

  private void parseMeta() {
    while (tok.type == TokenType.ID) {
      var prev = tok;
      next();
      parseMeta2(prev);
    }
  }
}

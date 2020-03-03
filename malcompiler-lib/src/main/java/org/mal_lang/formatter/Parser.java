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
  private static final int INDENT = 2;
  private Lexer lex;
  private org.mal_lang.compiler.lib.Token tok;
  private Deque<Token.Base> tokens;
  private int lastLine = 1;
  private boolean allowBreak = false;

  public Parser(File file, Deque<Token.Base> tokens) throws IOException {
    var canonicalFile = file.getCanonicalFile();
    this.lex = new Lexer(canonicalFile);
    this.tokens = tokens;
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
          createEnd();
          return;
        default:
          throw new RuntimeException(tok.type + "");
      }
    }
  }

  private void parseAssociations() {
    createBreak("", 0);
    createBegin(Token.BlockBreakType.ALWAYS, INDENT);

    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    consumeToken("associations");
    createEnd();
    createBreak("", -INDENT);
    consumeToken("{");
    if (tok.type == TokenType.ID) {
      parseAsssociations1();
    }
    createBreak("", -INDENT);
    consumeToken("}");
    createEnd();
    createBreak("", 0);
  }

  private void parseAsssociations1() {
    var id = tok;
    next();
    allowBreak = true;
    parseAssociation(id);
    while (tok.type == TokenType.ID) {
      id = tok;
      next();
      if (tok.type == TokenType.INFO) {
        parseMeta2(id);
      } else {
        createEnd(); // closing association block
        allowBreak = true;
        parseAssociation(id);
      }
    }
    createEnd(); // Closing association block
  }

  private void parseAssociation(org.mal_lang.compiler.lib.Token prev) {
    createBreak("", 0);
    createBegin(Token.BlockBreakType.ALWAYS, INDENT);
    createBegin(Token.BlockBreakType.CONSISTENT, 2 * INDENT);
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    createToken(prev, prev.stringValue);
    createBreak(" ", 0);
    consumeToken("[");
    consumeToken(tok.stringValue);
    consumeToken("]");
    createEnd();
    createBreak(" ", 0);
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    parseMult();
    createBreak(" ", 0);
    consumeToken("<--");
    createBreak(" ", 0);
    consumeToken(tok.stringValue);
    createBreak(" ", 0);
    consumeToken("-->");
    createBreak(" ", 0);
    parseMult();
    createEnd();
    createBreak(" ", 0);
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    consumeToken("[");
    consumeToken(tok.stringValue);
    consumeToken("]");
    createBreak(" ", 0);
    consumeToken(tok.stringValue);
    createEnd();
    createEnd();
    // Block left open will be closed above
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
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    consumeToken("#");
    consumeToken(tok.stringValue);
    consumeToken(":");
    createBreak(" ", 0);
    consumeToken("\"" + tok.stringValue + "\"");
    createEnd();
    createBreak("", 0);
  }

  private void parseInclude() {
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
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
    if (allowBreak && token.line - lastLine >= 2) {
      // allow blanklines
      createBegin(Token.BlockBreakType.ALWAYS, 0);
      tokens.push(new Token.Break("", 0)); // dont consume comment break
      createEnd();
    }
    allowBreak = false;
    if (!token.postComments.isEmpty()) {
      // we have at least 1 trailing comment
      createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
      createString(value);
      for (var comment : token.postComments) {
        createComment(comment, true, false);
      }
      createEnd();
    } else {
      createString(value);
    }
    // postComments are always on the same line as the token
    lastLine = token.line;
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
    if (allowBreak && comment.line - lastLine >= 2) {
      // allow blanklines
      createBegin(Token.BlockBreakType.ALWAYS, 0);
      createBreak("", 0);
      createEnd();
    }
    allowBreak = false;
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
      tokens.push(new Token.CommentBreak("", 0));
      createEnd();
      allowBreak = true;
    }
    lastLine = comment.line;
  }

  private Token.Begin createBegin(Token.BlockBreakType type, int indent) {
    var block = new Token.Begin(type, indent);
    tokens.push(block);
    return block;
  }

  private void createEnd() {
    tokens.push(new Token.End());
  }

  private void removeLastCommentBreak() {
    var it = tokens.iterator();
    while (it.hasNext()) {
      var next = it.next();
      if (!(next instanceof Token.Begin || next instanceof Token.End)) {
        if (next instanceof Token.CommentBreak) {
          it.remove();
        }
        break;
      }
    }
  }

  private void createBreak(String value, int indent) {
    if (value.isEmpty()) {
      removeLastCommentBreak();
    }
    tokens.push(new Token.Break(value, indent));
  }

  private void parseCategory(boolean first) {
    if (!first) {
      // Extra break if we are following a previous asset
      createBreak("", 0);
    }
    createBegin(Token.BlockBreakType.ALWAYS, INDENT);

    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    consumeToken("category");
    createBreak(" ", 0);
    consumeToken(tok.stringValue);
    createEnd();

    parseMeta();

    createBreak("", -INDENT);
    consumeToken("{");
    parseAssets();
    createBreak("", -INDENT);
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
    createBegin(Token.BlockBreakType.ALWAYS, INDENT);
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
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

    createBreak("", -INDENT);
    consumeToken("{");
    // ATTACKSTEPS
    first = true;
    boolean isMore = true;
    while (isMore) {
      switch (tok.type) {
        case LET:
          allowBreak = true;
          parseVariable();
          first = false;
          break;
        case RCURLY:
          isMore = false;
          break;
        default:
          parseAttackStep(first);
          first = false;
          break;
      }
    }
    createBreak("", -INDENT);
    consumeToken("}");
    createEnd();
  }

  private void parseVariable() {
    createBreak("", 0);
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
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
    createBegin(Token.BlockBreakType.ALWAYS, INDENT);

    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);

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
      createBegin(Token.BlockBreakType.INCONSISTENT, INDENT);
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
      createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
      consumeToken("[");
      if (tok.type != TokenType.RBRACKET) {
        parseTTCExpr();
      }
      consumeToken("]");
      createEnd();
    }
    createEnd();

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
      createBegin(Token.BlockBreakType.NEVER, 0);
      consumeToken(value.substring(1, value.length() - 1));
      createBreak(" ", 0);
      parseTTCTerm();
      createEnd();
    }
  }

  private void parseTTCTerm() {
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    parseTTCFact();
    while (tok.type == TokenType.STAR || tok.type == TokenType.DIVIDE) {
      createBreak(" ", 0);
      var value = tok.type.toString();
      createBegin(Token.BlockBreakType.NEVER, 0);
      consumeToken(value.substring(1, value.length() - 1));
      createBreak(" ", 0);
      parseTTCFact();
      createEnd();
    }
    createEnd();
  }

  private void parseTTCFact() {
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    parseTTCPrim();
    if (tok.type == TokenType.POWER) {
      createBreak(" ", 0);
      createBegin(Token.BlockBreakType.NEVER, 0);
      consumeToken("^");
      createBreak(" ", 0);
      parseTTCFact();
      createEnd();
    }
    createEnd();
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
      createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
      parseTTCExpr();
      createEnd();
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
    allowBreak = true;
    parseExpr();
    while (tok.type == TokenType.COMMA) {
      consumeToken(",");
      createBreak("", 0);
      allowBreak = true;
      parseExpr();
    }
    createEnd();
  }

  private void parseExpr() {
    var block = createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    parseSteps();
    while (tok.type == TokenType.UNION
        || tok.type == TokenType.INTERSECT
        || tok.type == TokenType.MINUS) {
      block.type = Token.BlockBreakType.CONSISTENT;
      createBreak(" ", 0);
      var value = tok.type.toString();
      createBegin(Token.BlockBreakType.NEVER, 0);
      consumeToken(value.substring(1, value.length() - 1));
      createBreak(" ", 0);
      parseSteps();
      createEnd();
    }
    createEnd();
  }

  private void parseSteps() {
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
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
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
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

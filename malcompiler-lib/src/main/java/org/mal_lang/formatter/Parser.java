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
  private int currentLine = 1;

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

  public void checkFirst(boolean first) {
    if (!first) {
      createBreak("", 0);
      currentLine++;
    }
  }

  public void parse() {
    createBegin(Token.BlockBreakType.ALWAYS, 0);
    next();
    boolean first = true;
    while (true) {
      // Category and Associations are separated by a blank line if they are not the
      // first top-level element
      switch (tok.type) {
        case CATEGORY:
          checkFirst(first);
          parseCategory();
          break;
        case ASSOCIATIONS:
          checkFirst(first);
          parseAssociations();
          break;
        case INCLUDE:
          parseInclude();
          break;
        case HASH:
          parseDefine();
          break;
        case EOF:
          for (var comment : tok.preComments) {
            allowUserBreak(comment);
            createComment(comment, tok);
          }
          createEnd();
          return;
        default:
          throw new RuntimeException(tok.type + "");
      }
      first = false;
    }
  }

  private void parseAssociations() {
    createBegin(Token.BlockBreakType.ALWAYS, INDENT);

    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    consumeToken("associations");
    createEnd();
    createBreak("", -INDENT);
    currentLine++;
    consumeToken("{");
    if (tok.type == TokenType.ID) {
      parseAsssociations1();
    }
    createBreak("", -INDENT);
    currentLine++;
    consumeToken("}");
    createEnd();
    createBreak("", 0);
    currentLine++;
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
    currentLine++;
    createBegin(Token.BlockBreakType.ALWAYS, INDENT);
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    // Allow user blank lines between associations
    createToken(prev, prev.stringValue, true);
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
    currentLine++;
  }

  private void parseInclude() {
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    consumeToken("include");
    createBreak(" ", 0);
    consumeToken("\"" + tok.stringValue + "\"");
    createEnd();
    createBreak("", 0);
    currentLine++;
  }

  private void createComment(
      org.mal_lang.compiler.lib.Token comment, org.mal_lang.compiler.lib.Token token) {
    switch (comment.type) {
      case SINGLECOMMENT:
        // Never include single comment length
        createString(String.format("//%s", comment.stringValue), false);
        createCommentBreak();
        currentLine = comment.line + 1;
        break;
      case MULTICOMMENT:
        var rows = comment.stringValue.split("\\n", -1);
        int startLine = comment.line;
        int endLine = startLine + rows.length - 1;

        createString("/");
        createBegin(Token.BlockBreakType.ALWAYS, 0);
        createString("*");
        for (int i = 0; i < rows.length; i++) {
          // Only count length if we are on the same line as our token
          createString(rows[i].strip(), startLine + i == token.line);
          if (i != rows.length - 1) {
            createCommentBreak();
            currentLine++;
          }
        }
        createString("*/");
        createEnd();
        currentLine = endLine;
        break;
      default:
        break;
    }
  }

  private void removeLastCommentBreak() {
    var it = tokens.iterator();
    while (it.hasNext()) {
      var token = it.next();
      if (token instanceof Token.CommentBreak || token instanceof Token.String) {
        if (token instanceof Token.CommentBreak) {
          it.remove();
        }
        break;
      }
    }
  }

  private void allowUserBreak(org.mal_lang.compiler.lib.Token token) {
    if (token.line - currentLine > 0) {
      // Token is ahead of expected line
      tokens.push(new Token.Break("", 0));
    }
  }

  private void createToken(
      org.mal_lang.compiler.lib.Token token, String value, boolean allowBreaks) {
    if (currentLine > token.line) {
      // We are in front of our token, this is okay except if this was caused by a
      // previous trailing comment.
      removeLastCommentBreak();
    }

    createBegin(Token.BlockBreakType.ALWAYS, 0);
    for (var comment : token.preComments) {
      allowUserBreak(comment);
      createComment(comment, token);
    }

    if (allowBreaks || !token.preComments.isEmpty()) {
      allowUserBreak(token);
    }
    createEnd();
    createString(value);
    currentLine = token.line;
    if (!token.postComments.isEmpty()) {
      createString(" ");
      createBegin(Token.BlockBreakType.ALWAYS, 0);
      for (var comment : token.postComments) {
        allowUserBreak(comment);
        createComment(comment, token);
      }
      createCommentBreak();
      currentLine++;
      createEnd();
    }
  }

  private void consumeToken(String value) {
    consumeToken(value, false);
  }

  private void consumeToken(String value, boolean allowBreak) {
    createToken(tok, value, allowBreak);
    next();
  }

  private void createString(String value, boolean includeLength) {
    tokens.push(new Token.String(value, includeLength));
  }

  private void createString(String value) {
    createString(value, true);
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
    removeLastCommentBreak();
    tokens.push(new Token.Break(value, indent));
  }

  private void createCommentBreak() {
    tokens.push(new Token.CommentBreak("", 0));
  }

  private void parseCategory() {
    createBegin(Token.BlockBreakType.ALWAYS, INDENT);

    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    consumeToken("category");
    createBreak(" ", 0);
    consumeToken(tok.stringValue);
    createEnd();

    parseMeta();

    createBreak("", -INDENT);
    currentLine++;
    consumeToken("{");
    parseAssets();
    createBreak("", -INDENT);
    currentLine++;
    consumeToken("}");
    createEnd();
    createBreak("", 0);
    currentLine++;
  }

  private void parseAssets() {
    boolean first = true;
    while (true) {
      // Assets are separated by a blank line if they are not the first asset in a
      // category
      switch (tok.type) {
        case ABSTRACT:
        case ASSET:
          checkFirst(first);
          parseAsset();
          break;
        default:
          return;
      }
      first = false;
    }
  }

  private void parseAsset() {
    createBreak("", 0);
    currentLine++;
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
    currentLine++;
    consumeToken("{");
    // ATTACKSTEPS
    boolean first = true;
    boolean isMore = true;
    while (isMore) {
      // Only attacksteps are separated by a blank line if they are not the first
      // top-level element inside Asset
      switch (tok.type) {
        case LET:
          parseVariable();
          break;
        case RCURLY:
          isMore = false;
          break;
        default:
          checkFirst(first);
          parseAttackStep();
          break;
      }
      first = false;
    }
    createBreak("", -INDENT);
    consumeToken("}");
    createEnd();
  }

  private void parseVariable() {
    createBreak("", 0);
    currentLine++;
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    // Allow user blank lines between variable definitions
    consumeToken("let", true);
    createBreak(" ", 0);
    consumeToken(tok.stringValue);
    createBreak(" ", 0);
    consumeToken("=");
    createBreak(" ", 0);
    parseExpr(false);
    createEnd();
  }

  private void parseAttackStep() {
    createBreak("", 0);
    currentLine++;
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
    currentLine++;
    createBegin(Token.BlockBreakType.ALWAYS, 3);
    var value = tok.type.toString();
    consumeToken(value.substring(1, value.length() - 1) + (tok.postComments.isEmpty() ? " " : ""));
    // Only allow user blank lines after the first expression
    parseExpr(false);
    while (tok.type == TokenType.COMMA) {
      consumeToken(",");
      createBreak("", 0);
      currentLine++;
      parseExpr(true);
    }
    createEnd();
  }

  private void parseExpr(boolean allowBreaks) {
    var block = createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    // allowBreaks is passed to when the first token is read
    parseSteps(allowBreaks);
    while (tok.type == TokenType.UNION
        || tok.type == TokenType.INTERSECT
        || tok.type == TokenType.MINUS) {
      block.type = Token.BlockBreakType.CONSISTENT;
      createBreak(" ", 0);
      var value = tok.type.toString();
      createBegin(Token.BlockBreakType.NEVER, 0);
      consumeToken(value.substring(1, value.length() - 1));
      createBreak(" ", 0);
      parseSteps(false);
      createEnd();
    }
    createEnd();
  }

  private void parseSteps(boolean allowBreaks) {
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    // allowBreaks is passed to when the first token is read
    parseStep(allowBreaks);
    while (tok.type == TokenType.DOT) {
      createBreak("", 0);
      consumeToken(".");
      parseStep(false);
    }
    createEnd();
  }

  private void parseStep(boolean allowBreaks) {
    // Only allow user blank lines on first token read
    if (tok.type == TokenType.LPAREN) {
      consumeToken("(", allowBreaks);
      parseExpr(false);
      consumeToken(")");
    } else if (tok.type == TokenType.ID) {
      consumeToken(tok.stringValue, allowBreaks);
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
    currentLine++;
    createBegin(Token.BlockBreakType.INCONSISTENT, 2 * INDENT);
    createToken(prev, prev.stringValue, false);
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

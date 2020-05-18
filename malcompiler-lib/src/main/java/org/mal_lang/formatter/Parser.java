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
import java.util.ArrayList;
import java.util.List;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.Lexer;
import org.mal_lang.compiler.lib.Token;
import org.mal_lang.compiler.lib.TokenType;
import org.mal_lang.formatter.blocks.Block;
import org.mal_lang.formatter.blocks.IndentBlock;
import org.mal_lang.formatter.blocks.LineBlock;
import org.mal_lang.formatter.blocks.MultiBlock;
import org.mal_lang.formatter.blocks.StackBlock;
import org.mal_lang.formatter.blocks.TextBlock;
import org.mal_lang.formatter.blocks.WrapBlock;

public class Parser {
  private Lexer lex;
  private Token tok;
  private StackBlock block;
  private int currentLine;
  private boolean allowBreaks = true;

  public Parser(File file) throws IOException {
    var canonicalFile = file.getCanonicalFile();
    this.lex = new Lexer(canonicalFile);
  }

  public String getOutput(int margin) {
    block.update(0, margin);
    return block.getOutput();
  }

  private void next() {
    try {
      tok = lex.next();
    } catch (CompilerException e) {
      throw new RuntimeException(e);
    }
  }

  public void parse() {
    currentLine = 1;
    block = new StackBlock();
    next();
    while (true) {
      switch (tok.type) {
        case INCLUDE:
          parseInclude(block);
          break;
        case CATEGORY:
          parseCategory(block);
          break;
        case HASH:
          parseDefine(block);
          break;
        case ASSOCIATIONS:
          parseAssociations(block);
          break;
        case EOF:
          block.add(blockify(""));
          return;
        default:
          throw new RuntimeException(tok.type + "");
      }
    }
  }

  private Block blockify() {
    return blockify(false);
  }

  private Block blockify(String string) {
    return blockify(string, false);
  }

  private Block blockify(boolean stackBlockLast) {
    var value = tok.type.toString();
    return blockify(value.substring(1, value.length() - 1), stackBlockLast);
  }

  private Block multilineComment(String value) {
    var lines = value.split("\\n", -1);
    if (lines.length == 1) {
      return new TextBlock("/*" + value + "*/");
    } else {
      var line = new LineBlock(new TextBlock("/"));
      var block = new StackBlock(new TextBlock("*" + lines[0].stripTrailing()));
      for (int i = 1; i < lines.length - 1; i++) {
        block.add(new TextBlock(lines[i].strip()));
      }
      block.add(new TextBlock(lines[lines.length - 1].stripLeading() + "*/"));
      line.add(block);
      return line;
    }
  }

  private Block blockify(String string, boolean stackBlockLast) {
    var pre = new StackBlock();
    var line = new LineBlock();

    for (var comment : tok.preComments) {
      if (currentLine + 1 < comment.line) {
        pre.add(new TextBlock(""));
        currentLine = comment.line;
      }
      if (comment.type == TokenType.SINGLECOMMENT) {
        pre.add(new TextBlock("//" + comment.stringValue));
        currentLine = comment.line;
      } else {
        int end = comment.line + comment.stringValue.split("\\n", -1).length - 1;
        if (end == tok.line) {
          line.add(multilineComment(comment.stringValue));
        } else {
          pre.add(multilineComment(comment.stringValue));
        }
        currentLine = end;
      }
    }
    if (currentLine + 1 < tok.line && allowBreaks) {
      pre.add(new TextBlock(""));
      currentLine = tok.line;
    }
    line.add(new TextBlock(string));
    pre.add(line);
    currentLine = tok.line;

    var post = new StackBlock();
    boolean lastSingle = false;
    for (var comment : tok.postComments) {
      if (comment.type == TokenType.SINGLECOMMENT) {
        // Cost is free to allow comments to span across margin
        post.add(new TextBlock("//" + comment.stringValue, true));
        lastSingle = true;
        currentLine = comment.line;
      } else {
        post.add(multilineComment(comment.stringValue));
        lastSingle = false;
        currentLine = comment.line + comment.stringValue.split("\\n", -1).length - 1;
      }
    }
    if (lastSingle && !stackBlockLast) {
      post.add(new TextBlock(""));
    }
    next();
    if (post.getSize() > 0) {
      return new LineBlock(pre, new TextBlock(" "), post);
    } else {
      return pre;
    }
  }

  private void parseInclude(MultiBlock block) {
    var include = blockify();
    allowBreaks = false;
    var filePath = blockify("\"" + tok.stringValue + "\"");
    block.add(new WrapBlock(" ", 4, include, filePath));
    allowBreaks = true;
  }

  private void parseDefine(MultiBlock block) {
    var hash = blockify();
    allowBreaks = false;
    var hashKeyColon = new LineBlock(hash, blockify(tok.stringValue), blockify());
    var stringValue = blockify("\"" + tok.stringValue + "\"");
    block.add(new WrapBlock(" ", 4, hashKeyColon, stringValue));
    allowBreaks = true;
  }

  private void parseCategory(MultiBlock block) {
    var category = new StackBlock();
    var categoryToken = blockify();
    allowBreaks = false;
    var name = blockify(tok.stringValue, true);
    category.add(new WrapBlock(" ", 4, categoryToken, name));
    allowBreaks = true;
    var metas = parseMetas();
    if (!metas.isEmpty()) {
      category.add(new IndentBlock(2, new StackBlock(metas)));
    }
    var open = blockify(true);
    var assets = new StackBlock();
    parseAssets(assets);
    var close = blockify(true);
    if (assets.getSize() > 0) {
      category.add(open, new IndentBlock(2, assets), close);
    } else {
      category.add(open, close);
    }
    block.add(category);
  }

  private void parseAssets(StackBlock block) {
    boolean more = true;
    while (more) {
      switch (tok.type) {
        case RCURLY:
          more = false;
          break;
        default:
          parseAsset(block);
          break;
      }
    }
  }

  private void parseAsset(MultiBlock block) {
    var asset = new StackBlock();
    var def = new ArrayList<Block>();
    if (tok.type == TokenType.ABSTRACT) {
      def.add(blockify());
      allowBreaks = false;
    }
    def.add(blockify());
    allowBreaks = false;
    def.add(blockify(tok.stringValue));
    if (tok.type == TokenType.EXTENDS) {
      def.add(blockify());
      def.add(blockify(tok.stringValue));
    }
    asset.add(new WrapBlock(" ", 4, def));
    allowBreaks = true;
    var metas = parseMetas();
    if (!metas.isEmpty()) {
      asset.add(new IndentBlock(2, new StackBlock(metas)));
    }
    var open = blockify(true);
    var content = new StackBlock();
    boolean more = true;
    while (more) {
      switch (tok.type) {
        case LET:
          parseVariable(content);
          break;
        case RCURLY:
          more = false;
          break;
        default:
          parseAttackStep(content);
          break;
      }
    }
    var close = blockify(true);
    if (content.getSize() > 0) {
      asset.add(open, new IndentBlock(2, content), close);
    } else {
      asset.add(open, close);
    }
    block.add(asset);
  }

  private void parseVariable(StackBlock block) {
    var let = blockify();
    allowBreaks = false;
    var def =
        new LineBlock(
            let, new TextBlock(" "), blockify(tok.stringValue), new TextBlock(" "), blockify());
    block.add(new WrapBlock(" ", 4, def, parseExpr()));
    allowBreaks = true;
  }

  private void parseAttackStep(MultiBlock block) {
    var attackStep = new StackBlock();
    var def = new ArrayList<Block>();
    var type = blockify();
    allowBreaks = false;
    def.add(new LineBlock(type, new TextBlock(" "), blockify(tok.stringValue)));
    def.addAll(parseTags());
    if (tok.type == TokenType.LCURLY) {
      def.add(parseCIA());
    }
    if (tok.type == TokenType.LBRACKET) {
      def.add(parseTTC());
    }
    attackStep.add(new WrapBlock(" ", 4, def));
    allowBreaks = true;

    var metas = parseMetas();
    if (!metas.isEmpty()) {
      attackStep.add(new IndentBlock(2, new StackBlock(metas)));
    }

    if (tok.type == TokenType.REQUIRE) {
      parseExprList(attackStep);
    }
    if (tok.type == TokenType.INHERIT || tok.type == TokenType.OVERRIDE) {
      parseExprList(attackStep);
    }
    block.add(attackStep);
  }

  private List<Block> parseTags() {
    var tags = new ArrayList<Block>();
    while (tok.type == TokenType.AT) {
      tags.add(new LineBlock(blockify(), blockify(tok.stringValue)));
    }
    return tags;
  }

  private LineBlock parseCIA() {
    var cia = new LineBlock(blockify());
    if (tok.type != TokenType.RCURLY) {
      cia.add(blockify());
      while (tok.type == TokenType.COMMA) {
        cia.add(blockify());
        cia.add(new TextBlock(" "));
        cia.add(blockify());
      }
    }
    cia.add(blockify());
    return cia;
  }

  private LineBlock parseTTC() {
    var ttc = new LineBlock();
    ttc.add(blockify());
    allowBreaks = false;
    if (tok.type != TokenType.RBRACKET) {
      ttc.add(parseTTCExpr());
    }
    ttc.add(blockify());
    allowBreaks = true;
    return ttc;
  }

  private LineBlock parseTTCExpr() {
    Block steps = parseTTCTerm();
    while (tok.type == TokenType.PLUS || tok.type == TokenType.MINUS) {
      var left = steps;
      var operand = blockify();
      var right = parseTTCTerm();
      steps = new WrapBlock(" ", 4, left, operand, right);
    }
    return new LineBlock(steps);
  }

  private LineBlock parseTTCTerm() {
    Block steps = parseTTCFact();
    while (tok.type == TokenType.STAR || tok.type == TokenType.DIVIDE) {
      var left = steps;
      var operand = blockify();
      var right = parseTTCFact();
      steps = new WrapBlock(" ", 4, left, operand, right);
    }
    return new LineBlock(steps);
  }

  private LineBlock parseTTCFact() {
    Block steps = parseTTCPrim();
    if (tok.type == TokenType.POWER) {
      var left = steps;
      var operand = blockify();
      var right = parseTTCFact();
      steps = new WrapBlock(" ", 4, left, operand, right);
    }
    return new LineBlock(steps);
  }

  private Block parseTTCPrim() {
    var prim = new LineBlock();
    if (tok.type == TokenType.ID) {
      prim.add(blockify(tok.stringValue));
      if (tok.type == TokenType.LPAREN) {
        prim.add(blockify());
        if (tok.type == TokenType.INT || tok.type == TokenType.FLOAT) {
          prim.add(parseNumber());
          while (tok.type == TokenType.COMMA) {
            prim.add(blockify());
            prim.add(new TextBlock(" "));
            prim.add(parseNumber());
          }
        }
        prim.add(blockify());
      }
    } else if (tok.type == TokenType.LPAREN) {
      prim.add(blockify());
      prim.add(parseTTCExpr());
      prim.add(blockify());
    } else {
      prim.add(parseNumber());
    }
    return prim;
  }

  private Block parseNumber() {
    if (tok.type == TokenType.INT) {
      return blockify(Integer.toString(tok.intValue));
    } else {
      return blockify(Double.toString(tok.doubleValue));
    }
  }

  private void parseExprList(MultiBlock block) {
    // Transfer arrows post comment to contents precomments to avoid shifting future stackblock
    var comments = new ArrayList<>(tok.postComments);
    tok = new Token(tok, tok.preComments, List.of());
    var arrow = blockify();
    comments.addAll(tok.preComments);
    tok = new Token(tok, comments, tok.postComments);
    var content = new StackBlock();
    var expr = parseExpr();
    while (tok.type == TokenType.COMMA) {
      expr.add(blockify(true));
      content.add(expr);
      expr = parseExpr();
    }
    content.add(expr);
    block.add(new IndentBlock(2, new LineBlock(arrow, new TextBlock(" "), content)));
  }

  private LineBlock parseExpr() {
    var steps = parseSteps();
    while (tok.type == TokenType.UNION
        || tok.type == TokenType.INTERSECT
        || tok.type == TokenType.MINUS) {
      var left = steps;
      var operand = blockify();
      var right = parseSteps();
      steps = new WrapBlock(" ", 4, left, operand, right);
    }
    return new LineBlock(steps);
  }

  private MultiBlock parseSteps() {
    var steps = new ArrayList<Block>();
    // Special wrapblock for when separators could have comments attached.
    var separators = new ArrayList<Block>();
    steps.add(parseStep());
    while (tok.type == TokenType.DOT) {
      separators.add(blockify());
      steps.add(parseStep());
    }
    return new WrapBlock(separators, 4, steps);
  }

  private Block parseStep() {
    var step = new LineBlock();
    if (tok.type == TokenType.LPAREN) {
      step.add(blockify(), parseExpr(), blockify());
    } else if (tok.type == TokenType.ID) {
      step.add(blockify(tok.stringValue));
      if (tok.type == TokenType.LPAREN) {
        step.add(blockify(), blockify());
      }
    }
    while (tok.type == TokenType.STAR || tok.type == TokenType.LBRACKET) {
      if (tok.type == TokenType.STAR) {
        step.add(blockify());
      } else {
        step.add(blockify(), blockify(tok.stringValue), blockify());
      }
    }
    return step;
  }

  private void parseAssociations(StackBlock block) {
    var associations = new StackBlock(blockify());
    var open = blockify(true);
    if (tok.type == TokenType.ID) {
      associations.add(open);
      var assocs = new StackBlock();
      parseAssociations1(assocs);
      associations.add(new IndentBlock(2, assocs), blockify(true));
    } else {
      associations.add(open, blockify(true));
    }
    block.add(associations);
  }

  private void parseAssociations1(StackBlock block) {
    var saved = blockify(tok.stringValue);
    parseAssociation(saved, block);
    var metas = new ArrayList<Block>();
    while (tok.type == TokenType.ID) {
      saved = blockify(tok.stringValue);
      if (tok.type == TokenType.INFO) {
        metas.add(parseMeta2(saved));
      } else {
        if (!metas.isEmpty()) {
          block.add(new IndentBlock(2, new StackBlock(metas)));
          metas.clear();
        }
        parseAssociation(saved, block);
      }
    }
    if (!metas.isEmpty()) {
      block.add(new IndentBlock(2, new StackBlock(metas)));
    }
  }

  private void parseAssociation(Block saved, StackBlock block) {
    allowBreaks = false;
    var type1 = new LineBlock(blockify(), blockify(tok.stringValue), blockify());
    var mult1 = parseMult();
    var arr1 = blockify();
    var name = blockify(tok.stringValue);
    var arr2 = blockify();
    var mult2 = parseMult();
    var type2 = new LineBlock(blockify(), blockify(tok.stringValue), blockify());
    var field = blockify(tok.stringValue);
    block.add(new WrapBlock(" ", 4, saved, type1, mult1, arr1, name, arr2, mult2, type2, field));
    allowBreaks = true;
  }

  private LineBlock parseMult() {
    var mult = parseMultUnit();
    if (tok.type == TokenType.RANGE) {
      mult.add(blockify());
      mult.add(parseMultUnit());
    }
    return mult;
  }

  private LineBlock parseMultUnit() {
    var mult = new LineBlock();
    if (tok.type == TokenType.INT) {
      mult.add(blockify(Integer.toString(tok.intValue)));
    } else {
      mult.add(blockify());
    }
    return mult;
  }

  private LineBlock parseMeta2(Block saved) {
    allowBreaks = false;
    var block =
        new LineBlock(
            saved, // info type
            new TextBlock(" "),
            blockify(), // info
            blockify(), // :
            new TextBlock(" "),
            blockify("\"" + tok.stringValue + "\"", true));
    allowBreaks = true;
    return block;
  }

  private List<Block> parseMetas() {
    var metas = new ArrayList<Block>();
    while (tok.type == TokenType.ID) {
      var type = blockify(tok.stringValue);
      metas.add(parseMeta2(type));
    }
    return metas;
  }
}

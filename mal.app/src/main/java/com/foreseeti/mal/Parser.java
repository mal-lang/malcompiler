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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Parser {
  private Lexer lex;
  private Token tok;
  private Set<File> included;
  private File currentFile;
  private Path originPath;

  public Parser(File file) throws IOException {
    var canonicalFile = file.getCanonicalFile();
    this.lex = new Lexer(canonicalFile);
    this.included = new HashSet<File>();
    this.included.add(canonicalFile);
    this.currentFile = canonicalFile;
    this.originPath = Path.of(canonicalFile.getParent());
  }

  private Parser(File file, Path originPath, Set<File> included) throws IOException {
    this.lex = new Lexer(file, originPath.relativize(Path.of(file.getPath())).toString());
    this.included = included;
    this.included.add(file);
    this.currentFile = file;
    this.originPath = originPath;
  }

  private void next() throws SyntaxError {
    tok = lex.next();
  }

  private void expect(TokenType type) throws SyntaxError {
    if (tok.type != type) {
      throw syntaxErrorExpectedTok(type);
    }
    next();
  }

  // The first set of <mal>
  private static List<TokenType> malFirst = new ArrayList<>();
  static {
    malFirst.add(TokenType.CATEGORY);
    malFirst.add(TokenType.ASSOCIATIONS);
    malFirst.add(TokenType.INCLUDE);
    malFirst.add(TokenType.HASH);
    malFirst.add(TokenType.EOF);
  }

  // The first set of <meta>
  private static List<TokenType> metaFirst = new ArrayList<>();
  static {
    metaFirst.add(TokenType.INFO);
    metaFirst.add(TokenType.ASSUMPTIONS);
    metaFirst.add(TokenType.RATIONALE);
  }

  // The first set of <asset>
  private static List<TokenType> assetFirst = new ArrayList<>();
  static {
    assetFirst.add(TokenType.ABSTRACT);
    assetFirst.add(TokenType.ASSET);
  }

  // The first set of <attackstep>
  private static List<TokenType> attackStepFirst = new ArrayList<>();
  static {
    attackStepFirst.add(TokenType.ALL);
    attackStepFirst.add(TokenType.ANY);
    attackStepFirst.add(TokenType.HASH);
    attackStepFirst.add(TokenType.EXIST);
    attackStepFirst.add(TokenType.NOTEXIST);
  }

  // <mal> ::= (<category> | <associations> | <include> | <define>)* EOF
  public AST parse() throws SyntaxError {
    var ast = new AST();
    next();

    while (true) {
      switch (tok.type) {
        case CATEGORY:
          var category = parseCategory();
          ast.addCategory(category);
          break;
        case ASSOCIATIONS:
          var associations = parseAssociations();
          ast.addAssociations(associations);
          break;
        case INCLUDE:
          var include = parseInclude();
          ast.include(include);
          break;
        case HASH:
          var define = parseDefine();
          ast.addDefine(define);
          break;
        case EOF:
          return ast;
        default:
          throw syntaxErrorExpectedTok(malFirst.toArray(new TokenType[malFirst.size()]));
      }
    }
  }

  // ID
  private AST.ID parseID() throws SyntaxError {
    if (tok.type != TokenType.ID) {
      throw syntaxErrorExpectedTok(TokenType.ID);
    }

    var id = new AST.ID(tok, tok.stringValue);
    next();

    return id;
  }

  // STRING
  private String parseString() throws SyntaxError {
    if (tok.type != TokenType.STRING) {
      throw syntaxErrorExpectedTok(TokenType.STRING);
    }

    var str = tok.stringValue;
    next();

    return str;
  }

  // <define> ::= HASH ID COLON STRING
  private AST.Define parseDefine() throws SyntaxError {
    var firstToken = tok;

    expect(TokenType.HASH);
    var key = parseID();
    expect(TokenType.COLON);
    var value = parseString();
    return new AST.Define(firstToken, key, value);
  }

  // <meta> ::= <meta-type> COLON STRING
  private AST.Meta parseMeta() throws SyntaxError {
    var firstToken = tok;

    var type = parseMetaType();
    expect(TokenType.COLON);
    var value = parseString();
    return new AST.Meta(firstToken, type, value);
  }

  // <meta-type> ::= INFO | ASSUMPTIONS | RATIONALE
  private AST.MetaType parseMetaType() throws SyntaxError {
    switch (tok.type) {
      case INFO:
        next();
        return AST.MetaType.INFO;
      case ASSUMPTIONS:
        next();
        return AST.MetaType.ASSUMPTIONS;
      case RATIONALE:
        next();
        return AST.MetaType.RATIONALE;
      default:
        throw syntaxErrorExpectedTok(metaFirst.toArray(new TokenType[metaFirst.size()]));
    }
  }

  // <meta>*
  private List<AST.Meta> parseMetaList() throws SyntaxError {
    var meta = new ArrayList<AST.Meta>();
    while (metaFirst.contains(tok.type)) {
      meta.add(parseMeta());
    }
    return meta;
  }

  // <include> ::= INCLUDE STRING
  private AST parseInclude() throws SyntaxError {
    expect(TokenType.INCLUDE);
    var line = tok.line;
    var col = tok.col;
    var filename = parseString();
    var file = new File(filename);

    if (!file.isAbsolute()) {
      var currentDir = currentFile.getParent();
      file = new File(String.format("%s/%s", currentDir, filename));
    }

    if (included.contains(file)) {
      return new AST();
    } else {
      try {
        var parser = new Parser(file.getCanonicalFile(), originPath, included);
        return parser.parse();
      } catch (IOException e) {
        throw syntaxError(line, col, e.getMessage());
      }
    }
  }

  // <number> ::= INT | FLOAT
  private double parseNumber() throws SyntaxError {
    if (tok.type != TokenType.INT && tok.type != TokenType.FLOAT) {
      throw syntaxErrorExpectedTok(TokenType.INT, TokenType.FLOAT);
    }

    if (tok.type == TokenType.INT) {
      double val = tok.intValue;
      next();
      return val;
    } else {
      double val = tok.doubleValue;
      next();
      return val;
    }
  }

  // <category> ::= CATEGORY ID <meta>* LCURLY <asset>* RCURLY
  private AST.Category parseCategory() throws SyntaxError {
    var firstToken = tok;

    expect(TokenType.CATEGORY);
    var name = parseID();
    var meta = parseMetaList();
    expect(TokenType.LCURLY);
    var assets = parseAssetList();
    expect(TokenType.RCURLY);
    return new AST.Category(firstToken, name, meta, assets);
  }

  // <asset> ::= ABSTRACT? ASSET ID (EXTENDS ID)? <meta>* LCURLY (<attackstep> | <variable>)* RCURLY
  private AST.Asset parseAsset() throws SyntaxError {
    var firstToken = tok;

    var isAbstract = false;
    if (tok.type == TokenType.ABSTRACT) {
      isAbstract = true;
      next();
    }
    expect(TokenType.ASSET);
    var name = parseID();
    Optional<AST.ID> parent = Optional.empty();
    if (tok.type == TokenType.EXTENDS) {
      next();
      parent = Optional.of(parseID());
    }
    var meta = parseMetaList();
    expect(TokenType.LCURLY);
    var attackSteps = new ArrayList<AST.AttackStep>();
    var variables = new ArrayList<AST.Variable>();
    while (tok.type != TokenType.RCURLY) {
      if (tok.type == TokenType.LET) {
        variables.add(parseVariable());
      } else {
        attackSteps.add(parseAttackStep());
      }
    }
    expect(TokenType.RCURLY);
    return new AST.Asset(firstToken, isAbstract, name, parent, meta, attackSteps, variables);
  }

  // <asset>*
  private List<AST.Asset> parseAssetList() throws SyntaxError {
    var assets = new ArrayList<AST.Asset>();
    while (assetFirst.contains(tok.type)) {
      assets.add(parseAsset());
    }
    return assets;
  }

  // <attackstep> ::= <astype> ID <ttc>? <meta>* <existence>? <reaches>?
  private AST.AttackStep parseAttackStep() throws SyntaxError {
    var firstToken = tok;

    var asType = parseAttackStepType();
    var name = parseID();
    Optional<AST.TTCExpr> ttc = Optional.empty();
    if (tok.type == TokenType.LBRACKET) {
      ttc = parseTTC();
    }
    var meta = parseMetaList();
    Optional<AST.Requires> requires = Optional.empty();
    if (tok.type == TokenType.REQUIRE) {
      requires = Optional.of(parseExistence());
    }
    Optional<AST.Reaches> reaches = Optional.empty();
    if (tok.type == TokenType.INHERIT || tok.type == TokenType.OVERRIDE) {
      reaches = Optional.of(parseReaches());
    }
    return new AST.AttackStep(firstToken, asType, name, ttc, meta, requires, reaches);
  }

  // <astype> ::= ALL | ANY | HASH | EXIST | NOTEXIST
  private AST.AttackStepType parseAttackStepType() throws SyntaxError {
    switch (tok.type) {
      case ALL:
        next();
        return AST.AttackStepType.ALL;
      case ANY:
        next();
        return AST.AttackStepType.ANY;
      case HASH:
        next();
        return AST.AttackStepType.DEFENSE;
      case EXIST:
        next();
        return AST.AttackStepType.EXIST;
      case NOTEXIST:
        next();
        return AST.AttackStepType.NOTEXIST;
      default:
        throw syntaxErrorExpectedTok(attackStepFirst.toArray(new TokenType[attackStepFirst.size()]));
    }
  }

  // <ttc> ::= LBRACKET <ttc-expr>? RBRACKET
  private Optional<AST.TTCExpr> parseTTC() throws SyntaxError {
    expect(TokenType.LBRACKET);
    Optional<AST.TTCExpr> expr = Optional.empty();
    if (tok.type != TokenType.RBRACKET) {
      expr = Optional.of(parseTTCExpr());
    }
    expect(TokenType.RBRACKET);
    return expr;
  }

  // <ttc-expr> ::= <ttc-term> ((PLUS | MINUS) <ttc-term>)*
  private AST.TTCExpr parseTTCExpr() throws SyntaxError {
    var firstToken = tok;

    var lhs = parseTTCTerm();
    while (tok.type == TokenType.PLUS || tok.type == TokenType.MINUS) {
      var addType = tok.type;
      next();
      var rhs = parseTTCTerm();
      if (addType == TokenType.PLUS) {
        lhs = new AST.TTCAddExpr(firstToken, lhs, rhs);
      } else {
        lhs = new AST.TTCSubExpr(firstToken, lhs, rhs);
      }
    }
    return lhs;
  }

  // <ttc-term> ::= <ttc-fact> ((STAR | DIVIDE) <ttc-fact>)*
  private AST.TTCExpr parseTTCTerm() throws SyntaxError {
    var firstToken = tok;

    var lhs = parseTTCFact();
    while (tok.type == TokenType.STAR || tok.type == TokenType.DIVIDE) {
      var mulType = tok.type;
      next();
      var rhs = parseTTCFact();
      if (mulType == TokenType.STAR) {
        lhs = new AST.TTCMulExpr(firstToken, lhs, rhs);
      } else {
        lhs = new AST.TTCDivExpr(firstToken, lhs, rhs);
      }
    }
    return lhs;
  }

  // <ttc-fact> ::= <ttc-prim> (POWER <ttc-fact>)?
  private AST.TTCExpr parseTTCFact() throws SyntaxError {
    var firstToken = tok;

    var e = parseTTCPrim();
    if (tok.type == TokenType.POWER) {
      next();
      e = new AST.TTCPowExpr(firstToken, e, parseTTCFact());
    }
    return e;
  }

  // <ttc-prim> ::= ID (LPAREN (<number> (COMMA <number>)*)? RPAREN)?
  //              | LPAREN <ttc-expr> RPAREN
  private AST.TTCExpr parseTTCPrim() throws SyntaxError {
    if (tok.type == TokenType.ID) {
      var firstToken = tok;

      var function = parseID();
      var params = new ArrayList<Double>();
      if (tok.type == TokenType.LPAREN) {
        next();
        if (tok.type == TokenType.INT || tok.type == TokenType.FLOAT) {
          params.add(parseNumber());
          while (tok.type == TokenType.COMMA) {
            next();
            params.add(parseNumber());
          }
        }
        expect(TokenType.RPAREN);
      }
      return new AST.TTCFuncExpr(firstToken, function, params);
    } else if (tok.type == TokenType.LPAREN) {
      next();
      var e = parseTTCExpr();
      expect(TokenType.RPAREN);
      return e;
    } else {
      throw syntaxErrorExpectedTok(TokenType.ID, TokenType.LPAREN);
    }
  }

  // <existence> ::= REQUIRE (<variable> | <expr>) (COMMA (<variable> | <expr>))*
  private AST.Requires parseExistence() throws SyntaxError {
    var firstToken = tok;

    expect(TokenType.REQUIRE);
    var variables = new ArrayList<AST.Variable>();
    var requires = new ArrayList<AST.Expr>();
    if (tok.type == TokenType.LET) {
      variables.add(parseVariable());
    } else {
      requires.add(parseExpr());
    }
    while (tok.type == TokenType.COMMA) {
      next();
      if (tok.type == TokenType.LET) {
        variables.add(parseVariable());
      } else {
        requires.add(parseExpr());
      }
    }
    return new AST.Requires(firstToken, variables, requires);
  }

  // <reaches> ::= (INHERIT | OVERRIDE) (<variable> | <expr>) (COMMA (<variable> | <expr>))*
  private AST.Reaches parseReaches() throws SyntaxError {
    var firstToken = tok;

    var inherits = false;
    if (tok.type == TokenType.INHERIT) {
      inherits = true;
    } else if (tok.type == TokenType.OVERRIDE) {
      inherits = false;
    } else {
      throw syntaxErrorExpectedTok(TokenType.INHERIT, TokenType.OVERRIDE);
    }
    next();
    var variables = new ArrayList<AST.Variable>();
    var reaches = new ArrayList<AST.Expr>();
    if (tok.type == TokenType.LET) {
      variables.add(parseVariable());
    } else {
      reaches.add(parseExpr());
    }
    while (tok.type == TokenType.COMMA) {
      next();
      if (tok.type == TokenType.LET) {
        variables.add(parseVariable());
      } else {
        reaches.add(parseExpr());
      }
    }
    return new AST.Reaches(firstToken, inherits, variables, reaches);
  }

  // <variable> ::= LET ID ASSIGN <expr>
  private AST.Variable parseVariable() throws SyntaxError {
    var firstToken = tok;

    expect(TokenType.LET);
    var id = parseID();
    expect(TokenType.ASSIGN);
    var e = parseExpr();
    return new AST.Variable(firstToken, id, e);
  }

  // <expr> ::= <step> ((UNION | INTERSECT) <step>)*
  private AST.Expr parseExpr() throws SyntaxError {
    var firstToken = tok;

    var lhs = parseStep();
    while (tok.type == TokenType.UNION || tok.type == TokenType.INTERSECT) {
      var setType = tok.type;
      next();
      var rhs = parseStep();
      if (setType == TokenType.UNION) {
        lhs = new AST.UnionExpr(firstToken, lhs, rhs);
      } else {
        lhs = new AST.IntersectionExpr(firstToken, lhs, rhs);
      }
    }
    return lhs;
  }

  // <step> ::= <transitive> (DOT <transitive>)*
  private AST.Expr parseStep() throws SyntaxError {
    var firstToken = tok;

    var lhs = parseTransitive();
    while (tok.type == TokenType.DOT) {
      next();
      var rhs = parseTransitive();
      lhs = new AST.StepExpr(firstToken, lhs, rhs);
    }
    return lhs;
  }

  // <transitive> ::= <subtype> STAR?
  private AST.Expr parseTransitive() throws SyntaxError {
    var firstToken = tok;

    var e = parseSubType();
    if (tok.type == TokenType.STAR) {
      next();
      e = new AST.TransitiveExpr(firstToken, e);
    }
    return e;
  }

  // <subtype> ::= <prim> <type>?
  private AST.Expr parseSubType() throws SyntaxError {
    var firstToken = tok;

    var e = parsePrim();
    if (tok.type == TokenType.LBRACKET) {
      var id = parseType();
      e = new AST.SubTypeExpr(firstToken, e, id);
    }
    return e;
  }

  // <prim> ::= ID | LPAREN <expr> RPAREN
  private AST.Expr parsePrim() throws SyntaxError {
    var firstToken = tok;

    if (tok.type == TokenType.ID) {
      var id = parseID();
      return new AST.IDExpr(firstToken, id);
    } else if (tok.type == TokenType.LPAREN) {
      next();
      var e = parseExpr();
      expect(TokenType.RPAREN);
      return e;
    } else {
      throw syntaxErrorExpectedTok(TokenType.ID, TokenType.LPAREN);
    }
  }

  // <associations> ::= ASSOCIATIONS LCURLY <association>* RCURLY
  private List<AST.Association> parseAssociations() throws SyntaxError {
    expect(TokenType.ASSOCIATIONS);
    expect(TokenType.LCURLY);
    var assocs = parseAssociationList();
    expect(TokenType.RCURLY);
    return assocs;
  }

  // <association> ::= ID <type> <mult> LARROW ID RARROW <mult> <type> ID <meta>*
  private AST.Association parseAssociation() throws SyntaxError {
    var firstToken = tok;

    var leftAsset = parseID();
    var leftField = parseType();
    var leftMult = parseMultiplicity();
    expect(TokenType.LARROW);
    var linkName = parseID();
    expect(TokenType.RARROW);
    var rightMult = parseMultiplicity();
    var rightField = parseType();
    var rightAsset = parseID();
    var meta = parseMetaList();
    return new AST.Association(firstToken, leftAsset, leftField, leftMult, linkName, rightMult, rightField, rightAsset, meta);
  }

  // <association>*
  private List<AST.Association> parseAssociationList() throws SyntaxError {
    var assocs = new ArrayList<AST.Association>();
    while (tok.type == TokenType.ID) {
      assocs.add(parseAssociation());
    }
    return assocs;
  }

  // <mult> ::= <mult-unit> (RANGE <mult-unit>)?
  private AST.Multiplicity parseMultiplicity() throws SyntaxError {
    var line = tok.line;
    var col = tok.col;

    var min = parseMultiplicityUnit();
    if (tok.type == TokenType.RANGE) {
      next();
      var max = parseMultiplicityUnit();
      if (min == 0 && max == 1) {
        return AST.Multiplicity.ZERO_OR_ONE;
      } else if (min == 0 && max == 2) {
        return AST.Multiplicity.ZERO_OR_MORE;
      } else if (min == 1 && max == 1) {
        return AST.Multiplicity.ONE;
      } else if (min == 1 && max == 2) {
        return AST.Multiplicity.ONE_OR_MORE;
      } else {
        throw syntaxError(line, col, String.format("Invalid multiplicity '%c..%c'", intToMult(min), intToMult(max)));
      }
    } else {
      if (min == 0) {
        throw syntaxError(line, col, "Invalid multiplicity '0'");
      } else if (min == 1) {
        return AST.Multiplicity.ONE;
      } else {
        return AST.Multiplicity.ZERO_OR_MORE;
      }
    }
  }

  private static char intToMult(int n) {
    switch (n) {
      case 0:
        return '0';
      case 1:
        return '1';
      default:
        return '*';
    }
  }

  // <mult-unit> ::= INT | STAR
  // 0 | 1 | *
  private int parseMultiplicityUnit() throws SyntaxError {
    if (tok.type == TokenType.INT) {
      var n = tok.intValue;
      if (n == 0 || n == 1) {
        next();
        return n;
      }
    } else if (tok.type == TokenType.STAR) {
      next();
      return 2;
    }
    throw syntaxErrorExpected("'0', '1', or '*'");
  }

  // <type> ::= LBRACKET ID RBRACKET
  private AST.ID parseType() throws SyntaxError {
    expect(TokenType.LBRACKET);
    var id = parseID();
    expect(TokenType.RBRACKET);
    return id;
  }

  /*
   * SyntaxError helper functions
   */

  private SyntaxError syntaxError(int line, int col, String message) {
    return new SyntaxError(tok.filename, line, col, message);
  }

  private SyntaxError syntaxError(String message) {
    return syntaxError(tok.line, tok.col, message);
  }

  private SyntaxError syntaxErrorExpected(String expected) {
    return syntaxError(String.format("expected %s, found %s", expected, tok.type.toString()));
  }

  private SyntaxError syntaxErrorExpectedTok(TokenType... types) {
    if (types.length == 0) {
      return syntaxErrorExpected("(null)");
    } else {
      var sb = new StringBuilder();
      for (int i = 0; i < types.length; ++i) {
        if (i == 0) {
          sb.append(types[i].toString());
        } else if (i == types.length - 1) {
          if (types.length == 2) {
            sb.append(String.format(" or %s", types[i].toString()));
          } else {
            sb.append(String.format(", or %s", types[i].toString()));
          }
        } else {
          sb.append(String.format(", %s", types[i].toString()));
        }
      }
      return syntaxErrorExpected(sb.toString());
    }
  }
}

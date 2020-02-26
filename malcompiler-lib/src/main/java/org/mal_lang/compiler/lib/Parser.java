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
package org.mal_lang.compiler.lib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class Parser {
  private MalLogger LOGGER;
  private Lexer lex;
  private Token tok;
  private Set<File> included;
  private File currentFile;
  private Path originPath;
  public boolean includeImports = true;

  private Parser(File file, boolean verbose, boolean debug) throws IOException {
    Locale.setDefault(Locale.ROOT);
    LOGGER = new MalLogger("PARSER", verbose, debug);
    var canonicalFile = file.getCanonicalFile();
    this.lex = new Lexer(canonicalFile);
    this.included = new HashSet<File>();
    this.included.add(canonicalFile);
    this.currentFile = canonicalFile;
    this.originPath = Path.of(canonicalFile.getParent());
  }

  private Parser(File file, Path originPath, Set<File> included, boolean verbose, boolean debug)
      throws IOException {
    Locale.setDefault(Locale.ROOT);
    LOGGER = new MalLogger("PARSER", verbose, debug);
    this.lex = new Lexer(file, originPath.relativize(Path.of(file.getPath())).toString());
    this.included = included;
    this.included.add(file);
    this.currentFile = file;
    this.originPath = originPath;
  }

  public static AST parse(File file) throws IOException, CompilerException {
    return parse(file, false, false);
  }

  public static AST parse(File file, boolean verbose, boolean debug)
      throws IOException, CompilerException {
    return new Parser(file, verbose, debug).parseLog();
  }

  public static AST parse(File file, boolean readImports) throws IOException, CompilerException {
    var parser = new Parser(file, false, false);
    parser.includeImports = readImports;
    return parser.parseLog();
  }

  private static AST parse(
      File file, Path originPath, Set<File> included, boolean verbose, boolean debug)
      throws IOException, CompilerException {
    return new Parser(file, originPath, included, verbose, debug).parseLog();
  }

  private AST parseLog() throws CompilerException {
    try {
      var ast = _parse();
      LOGGER.print();
      return ast;
    } catch (CompilerException e) {
      LOGGER.print();
      throw e;
    }
  }

  // The first set of <mal>
  private static TokenType[] malFirst = {
    TokenType.CATEGORY, TokenType.ASSOCIATIONS, TokenType.INCLUDE, TokenType.HASH
  };

  // The first set of <asset>
  private static TokenType[] assetFirst = {TokenType.ABSTRACT, TokenType.ASSET};

  // The first set of <attackstep>
  private static TokenType[] attackStepFirst = {
    TokenType.ALL, TokenType.ANY, TokenType.HASH, TokenType.EXIST, TokenType.NOTEXIST
  };

  private void _next() throws CompilerException {
    tok = lex.next();
  }

  private void _expect(TokenType type) throws CompilerException {
    if (tok.type != type) {
      throw exception(type);
    }
    _next();
  }

  // <mal> ::= (<category> | <associations> | <include> | <define>)* EOF
  private AST _parse() throws CompilerException {
    var ast = new AST();
    _next();

    while (true) {
      switch (tok.type) {
        case CATEGORY:
          var category = _parseCategory();
          ast.addCategory(category);
          break;
        case ASSOCIATIONS:
          var associations = _parseAssociations();
          ast.addAssociations(associations);
          break;
        case INCLUDE:
          var include = _parseInclude();
          ast.include(include);
          break;
        case HASH:
          var define = _parseDefine();
          ast.addDefine(define);
          break;
        case EOF:
          return ast;
        default:
          throw exception(malFirst);
      }
    }
  }

  // ID
  private AST.ID _parseID() throws CompilerException {
    switch (tok.type) {
      case ID:
        var id = new AST.ID(tok, tok.stringValue);
        _next();
        return id;
      default:
        throw exception(TokenType.ID);
    }
  }

  // STRING
  private String _parseString() throws CompilerException {
    switch (tok.type) {
      case STRING:
        var str = tok.stringValue;
        _next();
        return str;
      default:
        throw exception(TokenType.STRING);
    }
  }

  // <define> ::= HASH ID COLON STRING
  private AST.Define _parseDefine() throws CompilerException {
    var firstToken = tok;

    _expect(TokenType.HASH);
    var key = _parseID();
    _expect(TokenType.COLON);
    var value = _parseString();
    return new AST.Define(firstToken, key, value);
  }

  // <meta1> ::= ID <meta2>
  private AST.Meta _parseMeta1() throws CompilerException {
    var type = _parseID();
    return _parseMeta2(type);
  }

  // <meta2> ::= INFO COLON STRING
  private AST.Meta _parseMeta2(AST.ID type) throws CompilerException {
    _expect(TokenType.INFO);
    _expect(TokenType.COLON);
    var value = _parseString();
    return new AST.Meta(type, type, value);
  }

  // <meta1>*
  private List<AST.Meta> _parseMeta1List() throws CompilerException {
    var meta = new ArrayList<AST.Meta>();
    while (tok.type == TokenType.ID) {
      meta.add(_parseMeta1());
    }
    return meta;
  }

  // <include> ::= INCLUDE STRING
  private AST _parseInclude() throws CompilerException {
    _expect(TokenType.INCLUDE);
    var firstTok = tok;
    var filename = _parseString();
    if (!includeImports) {
      return new AST();
    }
    var file = new File(filename);

    if (!file.isAbsolute()) {
      var currentDir = currentFile.getParent();
      file = new File(String.format("%s/%s", currentDir, filename));
    }

    try {
      file = file.getCanonicalFile();
    } catch (IOException e) {
      throw exception(firstTok, e.getMessage());
    }

    if (included.contains(file)) {
      return new AST();
    } else {
      try {
        return Parser.parse(file, originPath, included, LOGGER.isVerbose(), LOGGER.isDebug());
      } catch (IOException e) {
        throw exception(firstTok, e.getMessage());
      }
    }
  }

  // <number> ::= INT | FLOAT
  private double _parseNumber() throws CompilerException {
    double val = 0.0;
    switch (tok.type) {
      case INT:
        val = tok.intValue;
        _next();
        return val;
      case FLOAT:
        val = tok.doubleValue;
        _next();
        return val;
      default:
        throw exception(TokenType.INT, TokenType.FLOAT);
    }
  }

  // <category> ::= CATEGORY ID <meta1>* LCURLY <asset>* RCURLY
  private AST.Category _parseCategory() throws CompilerException {
    var firstToken = tok;

    _expect(TokenType.CATEGORY);
    var name = _parseID();
    var meta = _parseMeta1List();
    if (tok.type == TokenType.LCURLY) {
      _next();
    } else {
      throw exception(TokenType.ID, TokenType.LCURLY);
    }
    var assets = _parseAssetList();
    if (tok.type == TokenType.RCURLY) {
      _next();
    } else {
      throw exception(assetFirst, TokenType.RCURLY);
    }
    return new AST.Category(firstToken, name, meta, assets);
  }

  // <asset> ::=
  //           ABSTRACT? ASSET ID (EXTENDS ID)? <meta1>* LCURLY (<attackstep> | <variable>)* RCURLY
  private AST.Asset _parseAsset() throws CompilerException {
    var firstToken = tok;

    var isAbstract = false;
    if (tok.type == TokenType.ABSTRACT) {
      isAbstract = true;
      _next();
    }
    _expect(TokenType.ASSET);
    var name = _parseID();
    Optional<AST.ID> parent = Optional.empty();
    if (tok.type == TokenType.EXTENDS) {
      _next();
      parent = Optional.of(_parseID());
    }
    var meta = _parseMeta1List();
    if (tok.type == TokenType.LCURLY) {
      _next();
    } else {
      throw exception(TokenType.ID, TokenType.LCURLY);
    }
    var attackSteps = new ArrayList<AST.AttackStep>();
    var variables = new ArrayList<AST.Variable>();
    loop:
    while (true) {
      switch (tok.type) {
        case LET:
          variables.add(_parseVariable());
          break;
        case ALL:
        case ANY:
        case HASH:
        case EXIST:
        case NOTEXIST:
          attackSteps.add(_parseAttackStep());
          break;
        case RCURLY:
          _next();
          break loop;
        default:
          throw exception(attackStepFirst, TokenType.LET, TokenType.RCURLY);
      }
    }
    return new AST.Asset(firstToken, isAbstract, name, parent, meta, attackSteps, variables);
  }

  // <asset>*
  private List<AST.Asset> _parseAssetList() throws CompilerException {
    var assets = new ArrayList<AST.Asset>();
    while (true) {
      switch (tok.type) {
        case ABSTRACT:
        case ASSET:
          assets.add(_parseAsset());
          break;
        default:
          return assets;
      }
    }
  }

  // <attackstep> ::= <astype> ID <tag>* <cia>? <ttc>? <meta1>* <existence>? <reaches>?
  private AST.AttackStep _parseAttackStep() throws CompilerException {
    var firstToken = tok;

    var asType = _parseAttackStepType();
    var name = _parseID();
    List<AST.ID> tags = new ArrayList<>();
    while (tok.type == TokenType.AT) {
      tags.add(_parseTag());
    }
    Optional<List<AST.CIA>> cia = Optional.empty();
    if (tok.type == TokenType.LCURLY) {
      cia = Optional.of(_parseCIA());
    }
    Optional<AST.TTCExpr> ttc = Optional.empty();
    if (tok.type == TokenType.LBRACKET) {
      ttc = _parseTTC();
    }
    var meta = _parseMeta1List();
    Optional<AST.Requires> requires = Optional.empty();
    if (tok.type == TokenType.REQUIRE) {
      requires = Optional.of(_parseExistence());
    }
    Optional<AST.Reaches> reaches = Optional.empty();
    if (tok.type == TokenType.INHERIT || tok.type == TokenType.OVERRIDE) {
      reaches = Optional.of(_parseReaches());
    }
    return new AST.AttackStep(firstToken, asType, name, tags, cia, ttc, meta, requires, reaches);
  }

  // <astype> ::= ALL | ANY | HASH | EXIST | NOTEXIST
  private AST.AttackStepType _parseAttackStepType() throws CompilerException {
    switch (tok.type) {
      case ALL:
        _next();
        return AST.AttackStepType.ALL;
      case ANY:
        _next();
        return AST.AttackStepType.ANY;
      case HASH:
        _next();
        return AST.AttackStepType.DEFENSE;
      case EXIST:
        _next();
        return AST.AttackStepType.EXIST;
      case NOTEXIST:
        _next();
        return AST.AttackStepType.NOTEXIST;
      default:
        throw exception(attackStepFirst);
    }
  }

  // <tag> ::= AT ID
  private AST.ID _parseTag() throws CompilerException {
    _expect(TokenType.AT);
    return _parseID();
  }

  // <cia> ::= LCURLY <cia-list>? RCURLY
  private List<AST.CIA> _parseCIA() throws CompilerException {
    _expect(TokenType.LCURLY);
    List<AST.CIA> cia = new ArrayList<AST.CIA>();
    if (tok.type != TokenType.RCURLY) {
      _parseCIAList(cia);
    }
    _expect(TokenType.RCURLY);
    return cia;
  }

  // <cia-list> ::= <cia-class> (COMMA <cia-class>)*
  private void _parseCIAList(List<AST.CIA> cia) throws CompilerException {
    cia.add(_parseCIAClass());
    while (tok.type == TokenType.COMMA) {
      _next();
      cia.add(_parseCIAClass());
    }
  }

  // <cia-class> ::= C | I | A
  private AST.CIA _parseCIAClass() throws CompilerException {
    switch (tok.type) {
      case C:
        _next();
        return AST.CIA.C;
      case I:
        _next();
        return AST.CIA.I;
      case A:
        _next();
        return AST.CIA.A;
      default:
        throw exception(TokenType.C, TokenType.I, TokenType.A, TokenType.RCURLY);
    }
  }

  // <ttc> ::= LBRACKET <ttc-expr>? RBRACKET
  private Optional<AST.TTCExpr> _parseTTC() throws CompilerException {
    _expect(TokenType.LBRACKET);
    Optional<AST.TTCExpr> expr = Optional.empty();
    if (tok.type != TokenType.RBRACKET) {
      expr = Optional.of(_parseTTCExpr());
    } else {
      // empty brackets [] = 0
      expr = Optional.of(new AST.TTCFuncExpr(tok, new AST.ID(tok, "Zero"), new ArrayList<>()));
    }
    _expect(TokenType.RBRACKET);
    return expr;
  }

  // <ttc-expr> ::= <ttc-term> ((PLUS | MINUS) <ttc-term>)*
  private AST.TTCExpr _parseTTCExpr() throws CompilerException {
    var firstToken = tok;

    var lhs = _parseTTCTerm();
    while (tok.type == TokenType.PLUS || tok.type == TokenType.MINUS) {
      var addType = tok.type;
      _next();
      var rhs = _parseTTCTerm();
      if (addType == TokenType.PLUS) {
        lhs = new AST.TTCAddExpr(firstToken, lhs, rhs);
      } else {
        lhs = new AST.TTCSubExpr(firstToken, lhs, rhs);
      }
    }
    return lhs;
  }

  // <ttc-term> ::= <ttc-fact> ((STAR | DIVIDE) <ttc-fact>)*
  private AST.TTCExpr _parseTTCTerm() throws CompilerException {
    var firstToken = tok;

    var lhs = _parseTTCFact();
    while (tok.type == TokenType.STAR || tok.type == TokenType.DIVIDE) {
      var mulType = tok.type;
      _next();
      var rhs = _parseTTCFact();
      if (mulType == TokenType.STAR) {
        lhs = new AST.TTCMulExpr(firstToken, lhs, rhs);
      } else {
        lhs = new AST.TTCDivExpr(firstToken, lhs, rhs);
      }
    }
    return lhs;
  }

  // <ttc-fact> ::= <ttc-prim> (POWER <ttc-fact>)?
  private AST.TTCExpr _parseTTCFact() throws CompilerException {
    var firstToken = tok;

    var e = _parseTTCPrim();
    if (tok.type == TokenType.POWER) {
      _next();
      e = new AST.TTCPowExpr(firstToken, e, _parseTTCFact());
    }
    return e;
  }

  // <ttc-prim> ::= ID (LPAREN (<number> (COMMA <number>)*)? RPAREN)?
  //              | LPAREN <ttc-expr> RPAREN | <number>
  private AST.TTCExpr _parseTTCPrim() throws CompilerException {
    var firstToken = tok;
    if (tok.type == TokenType.ID) {
      var function = _parseID();
      var params = new ArrayList<Double>();
      if (tok.type == TokenType.LPAREN) {
        _next();
        if (tok.type == TokenType.INT || tok.type == TokenType.FLOAT) {
          params.add(_parseNumber());
          while (tok.type == TokenType.COMMA) {
            _next();
            params.add(_parseNumber());
          }
        }
        _expect(TokenType.RPAREN);
      }
      return new AST.TTCFuncExpr(firstToken, function, params);
    } else if (tok.type == TokenType.LPAREN) {
      _next();
      var e = _parseTTCExpr();
      _expect(TokenType.RPAREN);
      return e;
    } else if (tok.type == TokenType.INT || tok.type == TokenType.FLOAT) {
      double num = _parseNumber();
      return new AST.TTCNumExpr(firstToken, num);
    } else {
      throw exception(TokenType.ID, TokenType.LPAREN, TokenType.INT, TokenType.FLOAT);
    }
  }

  // <existence> ::= REQUIRE <expr> (COMMA <expr>)*
  private AST.Requires _parseExistence() throws CompilerException {
    var firstToken = tok;

    _expect(TokenType.REQUIRE);
    var requires = new ArrayList<AST.Expr>();
    requires.add(_parseExpr());
    while (tok.type == TokenType.COMMA) {
      _next();
      requires.add(_parseExpr());
    }
    return new AST.Requires(firstToken, requires);
  }

  // <reaches> ::= (INHERIT | OVERRIDE) <expr> (COMMA <expr>)*
  private AST.Reaches _parseReaches() throws CompilerException {
    var firstToken = tok;

    var inherits = false;
    if (tok.type == TokenType.INHERIT) {
      inherits = true;
    } else if (tok.type == TokenType.OVERRIDE) {
      inherits = false;
    } else {
      throw exception(TokenType.INHERIT, TokenType.OVERRIDE);
    }
    _next();
    var reaches = new ArrayList<AST.Expr>();
    reaches.add(_parseExpr());
    while (tok.type == TokenType.COMMA) {
      _next();
      reaches.add(_parseExpr());
    }
    return new AST.Reaches(firstToken, inherits, reaches);
  }

  // <variable> ::= LET ID ASSIGN <expr>
  private AST.Variable _parseVariable() throws CompilerException {
    var firstToken = tok;

    _expect(TokenType.LET);
    var id = _parseID();
    _expect(TokenType.ASSIGN);
    var e = _parseExpr();
    return new AST.Variable(firstToken, id, e);
  }

  // <expr> ::= <steps> ((UNION | INTERSECT | MINUS) <steps>)*
  private AST.Expr _parseExpr() throws CompilerException {
    var firstToken = tok;

    var lhs = _parseSteps();
    while (tok.type == TokenType.UNION
        || tok.type == TokenType.INTERSECT
        || tok.type == TokenType.MINUS) {
      var setType = tok.type;
      _next();
      var rhs = _parseSteps();
      if (setType == TokenType.UNION) {
        lhs = new AST.UnionExpr(firstToken, lhs, rhs);
      } else if (setType == TokenType.INTERSECT) {
        lhs = new AST.IntersectionExpr(firstToken, lhs, rhs);
      } else {
        lhs = new AST.DifferenceExpr(firstToken, lhs, rhs);
      }
    }
    return lhs;
  }

  // <steps> ::= <step> (DOT <step>)*
  private AST.Expr _parseSteps() throws CompilerException {
    var firstToken = tok;

    var lhs = _parseStep();
    while (tok.type == TokenType.DOT) {
      _next();
      var rhs = _parseStep();
      lhs = new AST.StepExpr(firstToken, lhs, rhs);
    }
    return lhs;
  }

  // <step> ::= (LPAREN <expr> RPAREN | ID (LPAREN RPAREN)?) (STAR | <type>)*
  private AST.Expr _parseStep() throws CompilerException {
    var firstToken = tok;

    AST.Expr e = null;
    if (tok.type == TokenType.LPAREN) {
      _next();
      e = _parseExpr();
      _expect(TokenType.RPAREN);
    } else if (tok.type == TokenType.ID) {
      var id = _parseID();
      e = new AST.IDExpr(firstToken, id);
      if (tok.type == TokenType.LPAREN) {
        _next();
        _expect(TokenType.RPAREN);
        e = new AST.CallExpr(firstToken, id);
      }
    } else {
      throw exception(TokenType.LPAREN, TokenType.ID);
    }
    while (tok.type == TokenType.STAR || tok.type == TokenType.LBRACKET) {
      if (tok.type == TokenType.STAR) {
        _next();
        e = new AST.TransitiveExpr(firstToken, e);
      } else if (tok.type == TokenType.LBRACKET) {
        e = new AST.SubTypeExpr(firstToken, e, _parseType());
      }
    }
    return e;
  }

  // <associations> ::= ASSOCIATIONS LCURLY <associations1>? RCURLY
  private List<AST.Association> _parseAssociations() throws CompilerException {
    _expect(TokenType.ASSOCIATIONS);
    _expect(TokenType.LCURLY);
    List<AST.Association> assocs = new ArrayList<>();
    if (tok.type == TokenType.ID) {
      assocs = _parseAssociations1();
    }
    _expect(TokenType.RCURLY);
    return assocs;
  }

  // <associations1> ::= ID <association> (ID (<meta2> | <association>))*
  private List<AST.Association> _parseAssociations1() throws CompilerException {
    var assocs = new ArrayList<AST.Association>();
    var leftAsset = _parseID();
    var assoc = _parseAssociation(leftAsset);
    while (tok.type == TokenType.ID) {
      var id = _parseID();
      if (tok.type == TokenType.INFO) {
        assoc.meta.add(_parseMeta2(id));
      } else if (tok.type == TokenType.LBRACKET) {
        assocs.add(assoc);
        assoc = _parseAssociation(id);
      } else {
        throw exception(TokenType.INFO, TokenType.LBRACKET);
      }
    }
    assocs.add(assoc);
    return assocs;
  }

  // <association> ::= <type> <mult> LARROW ID RARROW <mult> <type> ID
  private AST.Association _parseAssociation(AST.ID leftAsset) throws CompilerException {
    var leftField = _parseType();
    var leftMult = _parseMultiplicity();
    _expect(TokenType.LARROW);
    var linkName = _parseID();
    _expect(TokenType.RARROW);
    var rightMult = _parseMultiplicity();
    var rightField = _parseType();
    var rightAsset = _parseID();
    return new AST.Association(
        leftAsset,
        leftAsset,
        leftField,
        leftMult,
        linkName,
        rightMult,
        rightField,
        rightAsset,
        new ArrayList<>());
  }

  // <mult> ::= <mult-unit> (RANGE <mult-unit>)?
  private AST.Multiplicity _parseMultiplicity() throws CompilerException {
    var firstTok = tok;

    var min = _parseMultiplicityUnit();
    if (tok.type == TokenType.RANGE) {
      _next();
      var max = _parseMultiplicityUnit();
      if (min == 0 && max == 1) {
        return AST.Multiplicity.ZERO_OR_ONE;
      } else if (min == 0 && max == 2) {
        return AST.Multiplicity.ZERO_OR_MORE;
      } else if (min == 1 && max == 1) {
        return AST.Multiplicity.ONE;
      } else if (min == 1 && max == 2) {
        return AST.Multiplicity.ONE_OR_MORE;
      } else {
        throw exception(
            firstTok,
            String.format("Invalid multiplicity '%c..%c'", intToMult(min), intToMult(max)));
      }
    } else {
      if (min == 0) {
        throw exception(firstTok, "Invalid multiplicity '0'");
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
  private int _parseMultiplicityUnit() throws CompilerException {
    if (tok.type == TokenType.INT) {
      var n = tok.intValue;
      if (n == 0 || n == 1) {
        _next();
        return n;
      }
    } else if (tok.type == TokenType.STAR) {
      _next();
      return 2;
    }
    throw expectedException("'0', '1', or '*'");
  }

  // <type> ::= LBRACKET ID RBRACKET
  private AST.ID _parseType() throws CompilerException {
    _expect(TokenType.LBRACKET);
    var id = _parseID();
    _expect(TokenType.RBRACKET);
    return id;
  }

  /*
   * CompilerException helper functions
   */

  private CompilerException expectedException(String expected) {
    return exception(String.format("expected %s, found %s", expected, tok.type.toString()));
  }

  private CompilerException exception(String msg) {
    return exception(tok, msg);
  }

  private CompilerException exception(Position pos, String msg) {
    LOGGER.error(pos, msg);
    return new CompilerException("There were syntax errors");
  }

  private CompilerException exception(TokenType... types) {
    return exception(new TokenType[0], types);
  }

  private CompilerException exception(TokenType[] firstTypes, TokenType... followingTypes) {
    if (firstTypes.length == 0 && followingTypes.length == 0) {
      return expectedException("(null)");
    } else {
      var sb = new StringBuilder();
      var totalLength = firstTypes.length + followingTypes.length;
      for (int i = 0; i < totalLength; ++i) {
        TokenType type = null;
        if (i < firstTypes.length) {
          type = firstTypes[i];
        } else {
          type = followingTypes[i - firstTypes.length];
        }
        if (i == 0) {
          sb.append(type.toString());
        } else if (i == totalLength - 1) {
          if (totalLength == 2) {
            sb.append(String.format(" or %s", type.toString()));
          } else {
            sb.append(String.format(", or %s", type.toString()));
          }
        } else {
          sb.append(String.format(", %s", type.toString()));
        }
      }
      return expectedException(sb.toString());
    }
  }
}

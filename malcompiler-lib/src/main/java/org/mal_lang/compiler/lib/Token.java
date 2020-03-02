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

import java.util.List;

public class Token extends Position {
  public final TokenType type;
  public final String stringValue;
  public final double doubleValue;
  public final int intValue;
  public final List<Token> preComments;
  public final List<Token> postComments;

  public Token(TokenType type, String filename, int line, int col) {
    super(filename, line, col);
    this.type = type;
    this.stringValue = "";
    this.doubleValue = 0.0;
    this.intValue = 0;
    preComments = List.of();
    postComments = List.of();
  }

  public Token(TokenType type, String filename, int line, int col, String stringValue) {
    super(filename, line, col);
    this.type = type;
    this.stringValue = stringValue;
    this.doubleValue = 0.0;
    this.intValue = 0;
    preComments = List.of();
    postComments = List.of();
  }

  public Token(Token tok, List<Token> preComments, List<Token> postComments) {
    super(tok.filename, tok.line, tok.col);
    type = tok.type;
    stringValue = tok.stringValue;
    doubleValue = tok.doubleValue;
    intValue = tok.intValue;
    this.preComments = preComments;
    this.postComments = postComments;
  }

  public Token(TokenType type, String filename, int line, int col, double doubleValue) {
    super(filename, line, col);
    this.type = type;
    this.stringValue = "";
    this.doubleValue = doubleValue;
    this.intValue = 0;
    preComments = List.of();
    postComments = List.of();
  }

  public Token(TokenType type, String filename, int line, int col, int intValue) {
    super(filename, line, col);
    this.type = type;
    this.stringValue = "";
    this.doubleValue = 0.0;
    this.intValue = intValue;
    preComments = List.of();
    postComments = List.of();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(type);
    sb.append(", ");
    sb.append(posString());
    switch (type) {
      case FLOAT:
        sb.append(", ");
        sb.append(doubleValue);
        break;
      case INT:
        sb.append(", ");
        sb.append(intValue);
        break;
      case ID:
      case STRING:
        sb.append(", ");
        sb.append(stringValue);
        break;
      default:
        break;
    }
    return sb.toString();
  }
}

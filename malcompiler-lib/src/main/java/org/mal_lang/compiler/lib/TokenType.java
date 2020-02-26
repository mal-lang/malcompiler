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

public enum TokenType {
  SINGLECOMMENT("single line comment"),
  MULTICOMMENT("multi line comment"),
  EOF("end-of-file"),

  INCLUDE("'include'"),
  INFO("'info'"),
  CATEGORY("'category'"),
  ABSTRACT("'abstract'"),
  ASSET("'asset'"),
  EXTENDS("'extends'"),
  ASSOCIATIONS("'associations'"),
  LET("'let'"),
  EXIST("'E'"),
  C("'C'"),
  I("'I'"),
  A("'A'"),

  STRING("string literal"),
  ID("identifier"),
  INT("integer literal"),
  FLOAT("floating point literal"),
  HASH("'#'"),
  COLON("':'"),
  LCURLY("'{'"),
  RCURLY("'}'"),
  INHERIT("'+>'"),
  OVERRIDE("'->'"),
  ALL("'&'"),
  ANY("'|'"),
  NOTEXIST("'!E'"),
  AT("'@'"),
  LBRACKET("'['"),
  RBRACKET("']'"),
  LPAREN("'('"),
  RPAREN("')'"),
  COMMA("','"),
  REQUIRE("'<-'"),
  ASSIGN("'='"),
  UNION("'\\/'"),
  INTERSECT("'/\\'"),
  DOT("'.'"),
  RANGE("'..'"),
  STAR("'*'"),
  PLUS("'+'"),
  MINUS("'-'"),
  DIVIDE("'/'"),
  POWER("'^'"),
  LARROW("'<--'"),
  RARROW("'-->'");

  private final String string;

  private TokenType(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }
}

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

public class Token {
  private TokenType type;
  private String filename;
  private String stringValue;
  private double doubleValue;
  private int intValue;
  private int line;
  private int col;

  public Token(TokenType type, String filename, int line, int col) {
    this.type = type;
    this.filename = filename;
    this.line = line;
    this.col = col;
  }

  public Token(TokenType type, String filename, int line, int col, String stringValue) {
    this(type, filename, line, col);
    this.stringValue = stringValue;
  }

  public Token(TokenType type, String filename, int line, int col, double doubleValue) {
    this(type, filename, line, col);
    this.doubleValue = doubleValue;
  }

  public Token(TokenType type, String filename, int line, int col, int intValue) {
    this(type, filename, line, col);
    this.intValue = intValue;
  }

  public TokenType getType() {
    return type;
  }

  public String getFilename() {
    return filename;
  }

  public int getLine() {
    return line;
  }

  public int getCol() {
    return col;
  }

  public String getStringValue() {
    return stringValue;
  }

  public double getDoubleValue() {
    return doubleValue;
  }

  public int getIntValue() {
    return intValue;
  }
}

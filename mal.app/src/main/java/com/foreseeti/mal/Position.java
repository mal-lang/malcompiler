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

public class Position {
  public final String filename;
  public final int line;
  public final int col;

  public Position(String filename, int line, int col) {
    this.filename = filename;
    this.line = line;
    this.col = col;
  }

  public Position(Position pos) {
    this.filename = pos.filename;
    this.line = pos.line;
    this.col = pos.col;
  }

  public String posString() {
    return String.format("<%s:%d:%d>", filename, line, col);
  }

  @Override
  public String toString() {
    return posString();
  }
}

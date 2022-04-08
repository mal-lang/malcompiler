/*
 * Copyright 2019-2022 Foreseeti AB
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

public class Position implements Comparable<Position> {
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

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Position)) {
      return false;
    }
    var other = (Position) obj;
    return this.filename.equals(other.filename) && this.line == other.line && this.col == other.col;
  }

  @Override
  public int compareTo(Position o) {
    int cmp = this.filename.compareTo(o.filename);
    if (cmp != 0) {
      return cmp;
    }
    cmp = Integer.compare(this.line, o.line);
    if (cmp != 0) {
      return cmp;
    }
    return Integer.compare(this.col, o.col);
  }
}

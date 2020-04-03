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

public class Token {
  private Token() {}

  public static interface Base {}

  public static class String implements Base {
    public final java.lang.String value;
    public final int length;

    public String(java.lang.String value) {
      this(value, true);
    }

    public String(java.lang.String value, boolean includeLength) {
      this.value = value;
      this.length = includeLength ? value.length() : 0;
    }
  }

  public static class Break implements Base {
    public final java.lang.String value;
    public final int indent;

    public Break(java.lang.String value, int indent) {
      this.value = value;
      this.indent = indent;
    }
  }

  public static class CommentBreak extends Break {
    public CommentBreak(java.lang.String value, int indent) {
      super(value, indent);
    }
  }

  public enum BlockBreakType {
    ALWAYS, // always break children, even if they fit line
    CONSISTENT, // break if children doesn't fit (and do so for every child and align start of rows)
    INCONSISTENT, // default, break if children doesn't fit and indent breaks
    NEVER // never break
  }

  public static class Begin implements Base {
    public final int indent;
    protected BlockBreakType type;

    public Begin(BlockBreakType type, int indent) {
      this.indent = indent;
      this.type = type;
    }
  }

  public static class End implements Base {}
}
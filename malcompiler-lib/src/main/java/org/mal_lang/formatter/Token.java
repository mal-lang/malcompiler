package org.mal_lang.formatter;

public class Token {
  private static final int DEFAULT_INDENT = 2;

  public static interface Base {}

  public static class String implements Base {
    public final java.lang.String value;

    public String(java.lang.String value) {
      this.value = value;
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

  public static enum BlockBreakType {
    ALWAYS, // always break children, even if they fit line
    CONSISTENT, // break if children doesn't fit (and do so for every child and align start of rows)
    INCONSISTENT, // default, break if children doesn't fit and indent breaks
    FIT // Not used here
  }

  public static class Begin implements Base {
    public final int indent;
    public BlockBreakType type;

    public Begin(BlockBreakType type, int indent) {
      this.indent = indent;
      this.type = type;
    }
  }

  public static class End implements Base {}
}

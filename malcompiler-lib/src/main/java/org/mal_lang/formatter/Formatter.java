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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.Lexer;
import org.mal_lang.compiler.lib.MalLogger;

/**
 * This formatter is based on the algorithm described in "Prettyprinting" by Derek C. Oppen.
 *
 * <p>Oppen, Dereck C. "Prettyprinting." ACM Transactions on Programming Languages and Systems
 * (TOPLAS) 2, no. 4 (1980): 465-483.
 */
public class Formatter {

  public static byte[] prettyPrint(File file, int lineWidth, int indent)
      throws IOException, CompilerException {
    return new Formatter(file, lineWidth, indent).scan();
  }

  public static void prettyPrint(File file, Map<String, String> opts)
      throws IOException, CompilerException {
    int indent = opts.containsKey("indent") ? Integer.parseInt(opts.get("indent")) : 2;
    boolean inplace =
        opts.containsKey("inplace") ? Boolean.parseBoolean(opts.get("inplace")) : false;
    int margin = opts.containsKey("margin") ? Integer.parseInt(opts.get("margin")) : 2;
    var bytes = prettyPrint(file, margin, indent);
    if (inplace) {
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(file, false);
        fos.write(bytes);
      } finally {
        if (fos != null) {
          fos.close();
        }
      }
    } else {
      System.out.print(new String(bytes));
    }
  }

  private class Block {
    public final int indent;
    public final Token.BlockBreakType type;

    public Block(int indent, Token.BlockBreakType type) {
      this.indent = indent;
      this.type = type;
    }
  }

  private Deque<Block> spaceStack = new ArrayDeque<>();
  private int margin;
  private int space;
  private File file;
  private MalLogger LOGGER;
  private ByteArrayOutputStream out = new ByteArrayOutputStream();
  private int indent;

  private Formatter(File file, int lineWidth, int indent) throws IOException, CompilerException {
    Locale.setDefault(Locale.ROOT);
    LOGGER = new MalLogger("FORMATTER", false, false);
    margin = lineWidth;
    space = margin;
    this.indent = indent;
    this.file = file;
    try {
      org.mal_lang.compiler.lib.Parser.parse(file);
    } catch (IOException e) {
      throw e;
    } catch (CompilerException e) {
      LOGGER.error("Code to be formatted must be syntactically valid");
      LOGGER.print();
      throw e;
    }
  }

  private void output(String s) throws IOException {
    out.write(s.getBytes());
  }

  private void indent(int n) throws IOException {
    output(String.format("%s", " ".repeat(n)));
  }

  private void newLine(int n) throws IOException {
    output("\n");
    indent(n);
  }

  private void print(Token.Base x, int l) throws IOException {
    if (x instanceof Token.String) {
      var tok = (Token.String) x;
      output(tok.value);
      space -= l;
    } else if (x instanceof Token.Begin) {
      var tok = (Token.Begin) x;
      if (tok.type == Token.BlockBreakType.ALWAYS) {
        spaceStack.push(new Block(space - tok.indent, Token.BlockBreakType.CONSISTENT));
      } else if (l > space) {
        spaceStack.push(new Block(space - tok.indent, tok.type));
      } else {
        spaceStack.push(new Block(0, Token.BlockBreakType.FIT));
      }
    } else if (x instanceof Token.End) {
      spaceStack.pop();
    } else if (x instanceof Token.Break) {
      var tok = (Token.Break) x;
      var block = spaceStack.peek();
      if (block.type == Token.BlockBreakType.FIT) {
        space -= tok.value.length();
        output(tok.value);
      } else if (block.type == Token.BlockBreakType.CONSISTENT) {
        space = block.indent - tok.indent;
        newLine(margin - space);
      } else if (block.type == Token.BlockBreakType.INCONSISTENT) {
        if (l > space) {
          space = block.indent - tok.indent;
          newLine(margin - space);
        } else {
          space -= tok.value.length();
          output(tok.value);
        }
      }
    }
  }

  private Deque<Token.Base> tokens = new ArrayDeque<>();
  private Deque<Integer> blockStartIndex = new ArrayDeque<>();
  private List<Integer> size = new ArrayList<>();
  private List<Token.Base> stream = new ArrayList<>();
  private int total = 1;

  private byte[] scan() throws IOException, CompilerException {
    var parser = new Parser(file, tokens, indent);
    parser.parse();
    while (!tokens.isEmpty()) {
      var token = tokens.pollLast();
      if (token instanceof Token.Begin) {
        stream.add(token);
        size.add(-total);
        blockStartIndex.push(size.size() - 1);
      } else if (token instanceof Token.End) {
        stream.add(token);
        size.add(0);
        var x = blockStartIndex.pop();
        size.set(x, size.get(x) + total);
        if (stream.get(x) instanceof Token.Break) {
          x = blockStartIndex.pop();
          size.set(x, size.get(x) + total);
        }
        if (blockStartIndex.isEmpty()) {
          for (int i = 0; i < stream.size(); i++) {
            print(stream.get(i), size.get(i));
          }
        }
      } else if (token instanceof Token.Break) {
        var x = blockStartIndex.peek();
        if (stream.get(x) instanceof Token.Break) {
          size.set(blockStartIndex.pop(), size.get(x) + total);
        }
        stream.add(token);
        size.add(-total);
        blockStartIndex.push(size.size() - 1);
        total += ((Token.Break) token).value.length();
      } else if (token instanceof Token.String) {
        var tok = (Token.String) token;
        if (blockStartIndex.isEmpty()) {
          print(tok, tok.value.length());
        } else {
          stream.add(tok);
          size.add(tok.value.length());
          total += tok.value.length();
        }
      }
    }
    // We might produce empty lines which only have whitespace
    String trimmed = out.toString().replaceAll("(?m) +$", "");
    var bytes = trimmed.getBytes();
    var tempFile = File.createTempFile("formatted", ".tmp");
    tempFile.deleteOnExit();
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(tempFile);
      fos.write(bytes);
      if (!Lexer.syntacticallyEqual(new Lexer(file), new Lexer(tempFile))) {
        throw new CompilerException(
            "The formatter has produced an AST that differs from the input.");
      }
      return bytes;
    } catch (Exception e) {
      LOGGER.error("The formatter has produced an invalid AST. Please report this as a bug.");
      LOGGER.print();
      throw e;
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }
}

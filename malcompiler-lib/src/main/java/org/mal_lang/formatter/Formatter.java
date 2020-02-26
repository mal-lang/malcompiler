package org.mal_lang.formatter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Stack;
import org.mal_lang.compiler.lib.AST;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.MalLogger;

public class Formatter {

  public static byte[] prettyPrint(File file, int lineWidth) throws IOException, CompilerException {
    return new Formatter(file, lineWidth).scan();
  }

  private class Block {
    public final int indent;
    public final Token.BlockBreakType type;

    public Block(int indent, Token.BlockBreakType type) {
      this.indent = indent;
      this.type = type;
    }
  }

  private Stack<Block> spaceStack = new Stack<>();
  private int margin;
  private int space;
  private File file;
  private MalLogger LOGGER;
  private ByteArrayOutputStream out = new ByteArrayOutputStream();
  private AST ast;

  private Formatter(File file, int lineWidth) throws IOException, CompilerException {
    Locale.setDefault(Locale.ROOT);
    LOGGER = new MalLogger("FORMATTER", false, false);
    margin = lineWidth;
    space = margin;
    this.file = file;
    try {
      ast = org.mal_lang.compiler.lib.Parser.parse(file, false);
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
      // output("/*B*/");
      var tok = (Token.Begin) x;
      if (tok.type == Token.BlockBreakType.ALWAYS) {
        spaceStack.push(new Block(space - tok.indent, Token.BlockBreakType.CONSISTENT));
      } else if (l > space) {
        spaceStack.push(new Block(space - tok.indent, tok.type));
      } else {
        spaceStack.push(new Block(0, Token.BlockBreakType.FIT));
      }
    } else if (x instanceof Token.End) {
      // output("/*E*/");
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
  private Stack<Integer> blockStartIndex = new Stack<>();
  private Stack<Integer> size = new Stack<>();
  private Stack<Token.Base> stream = new Stack<>();
  private int total = 1;

  private byte[] scan() throws IOException, CompilerException {
    var parser = new Parser(file, tokens);
    parser.parse();
    while (!tokens.isEmpty()) {
      var token = tokens.pollLast();
      if (token instanceof Token.Begin) {
        stream.push(token);
        size.push(-total);
        blockStartIndex.push(size.size() - 1);
      } else if (token instanceof Token.End) {
        stream.push(token);
        size.push(0);
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
        stream.push(token);
        size.push(-total);
        blockStartIndex.push(size.size() - 1);
        total += ((Token.Break) token).value.length();
      } else if (token instanceof Token.String) {
        var tok = (Token.String) token;
        if (blockStartIndex.isEmpty()) {
          print(tok, tok.value.length());
        } else {
          stream.push(tok);
          size.push(tok.value.length());
          total += tok.value.length();
        }
      }
    }
    var tempFile = File.createTempFile("formatted", ".tmp");
    tempFile.deleteOnExit();
    var outputStream = new FileOutputStream(tempFile);
    try {
      outputStream.write(out.toByteArray());
      var newAst = org.mal_lang.compiler.lib.Parser.parse(tempFile, false);
      if (!ast.syntacticallyEqual(newAst)) {
        throw new CompilerException(
            "The formatter has produced an AST that differs from the input.");
      }
      return out.toByteArray();
    } catch (CompilerException e) {
      LOGGER.error("The formatter has produced an invalid AST. Please report this as a bug.");
      LOGGER.print();
      throw e;
    } finally {
      outputStream.close();
    }
  }
}

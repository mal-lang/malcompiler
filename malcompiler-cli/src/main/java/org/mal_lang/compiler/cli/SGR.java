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
package org.mal_lang.compiler.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * This class follows parts of ECMA-48 5th edition (June 1991). Some terminals might not support
 * every control function below.
 */
public class SGR {
  private List<SGR> children;
  private String text;

  // SGR 1 bold or increased intensity
  private boolean isBold = false;
  // SGR 2 faint, decreased intensity or second colour
  private boolean isFaint = false;
  // SGR 3 italicized
  private boolean isItalicized = false;
  // SGR 4 singly underlined
  private boolean isSinglyUnderlined = false;
  // SGR 5 slowly blinking (less then 150 per minute)
  private boolean isSlowlyBlinking = false;
  // SGR 6 rapidly blinking (150 per minute or more)
  private boolean isRapidlyBlinking = false;
  // SGR 7 negative image
  private boolean isNegativeImage = false;
  // SGR 8 concealed characters
  private boolean isConcealedCharacters = false;
  // SGR 9 crossed-out (characters still legible but marked as to be deleted)
  private boolean isCrossedOut = false;
  // SGR 10-19 alternative fonts [not included]
  // SGR 20 Fraktur (Gothic)
  private boolean isFraktur = false;
  // SGR 21 doubly underlined
  private boolean isDoublyUnderlined = false;
  // SGR 22 normal colour or normal intensity (neither bold nor faint) [not included]
  // SGR 23 not italicized, not fraktur [not included]
  // SGR 24 not underlined (neither singly nor doubly) [not included]
  // SGR 25 steady (not blinking) [not included]
  // SGR 26 (reserved for proportional spacing as specified in CCITT Recommendation T.61)
  // [not included]
  // SGR 27 positive image [not included]
  // SGR 28 revealed characters [not included]
  // SGR 29 not crossed out [not included]
  // SGR 30 black display
  private boolean isFgBlack = false;
  // SGR 31 red display
  private boolean isFgRed = false;
  // SGR 32 green display
  private boolean isFgGreen = false;
  // SGR 33 yellow display
  private boolean isFgYellow = false;
  // SGR 34 blue display
  private boolean isFgBlue = false;
  // SGR 35 magenta display
  private boolean isFgMagenta = false;
  // SGR 36 cyan display
  private boolean isFgCyan = false;
  // SGR 37 white display
  private boolean isFgWhite = false;
  // SGR 38 (reserved for future standardization; intended for setting character foreground colour
  // as specified in ISO 8613-6 [CCITT Recommendation T.416])
  private boolean isFg8Bit = false;
  private int fg8Bit = 0;
  private boolean isFg24Bit = false;
  private int fg24BitR = 0;
  private int fg24BitG = 0;
  private int fg24BitB = 0;
  // SGR 39 default display colour (implementation-defined) [not included]
  // SGR 40 black background
  private boolean isBgBlack = false;
  // SGR 41 red background
  private boolean isBgRed = false;
  // SGR 42 green background
  private boolean isBgGreen = false;
  // SGR 43 yellow background
  private boolean isBgYellow = false;
  // SGR 44 blue background
  private boolean isBgBlue = false;
  // SGR 45 magenta background
  private boolean isBgMagenta = false;
  // SGR 46 cyan background
  private boolean isBgCyan = false;
  // SGR 47 white background
  private boolean isBgWhite = false;
  // SGR 48 (reserved for future standardization; intended for setting character background colour
  // as specified in ISO 8613-6 [CCITT Recommendation T.416])
  private boolean isBg8Bit = false;
  private int bg8Bit = 0;
  private boolean isBg24Bit = false;
  private int bg24BitR = 0;
  private int bg24BitG = 0;
  private int bg24BitB = 0;
  // SGR 49 default background colour (implementation-defined) [not included]
  // SGR 50 (reserved for cancelling the effect of the rendering aspect established by parameter
  // value 26) [not included]
  // SGR 51 framed [not included]
  // SGR 52 encircled [not included]
  // SGR 53 overline [not included]
  // SGR 54 not framed, not encircled [not included]
  // SGR 55 not overlined [not included]
  // SGR 56 (reserved for future standardization) [not included]
  // SGR 57 (reserved for future standardization) [not included]
  // SGR 58 (reserved for future standardization) [not included]
  // SGR 59 (reserved for future standardization) [not included]
  // SGR 60 ideogram underline or right side line [not included]
  // SGR 61 ideogram double underline or double line on the right side [not included]
  // SGR 62 ideogram overline or left side line [not included]
  // SGR 63 ideogram double overline or double line on the left side [not included]
  // SGR 64 ideogram stress marking [not included]
  // SGR 65 cancels the effect of the rendition aspects established by parameter values 60 to 64
  // [not included]

  private SGR() {
    this.children = null;
    this.text = null;
  }

  private SGR(String text) {
    this.children = null;
    this.text = text;
  }

  private SGR(SGR other) {
    this.isBold = other.isBold;
    this.isFaint = other.isFaint;
    this.isItalicized = other.isItalicized;
    this.isSinglyUnderlined = other.isSinglyUnderlined;
    this.isSlowlyBlinking = other.isSlowlyBlinking;
    this.isRapidlyBlinking = other.isRapidlyBlinking;
    this.isNegativeImage = other.isNegativeImage;
    this.isConcealedCharacters = other.isConcealedCharacters;
    this.isCrossedOut = other.isCrossedOut;
    this.isFraktur = other.isFraktur;
    this.isDoublyUnderlined = other.isDoublyUnderlined;
    this.isFgBlack = other.isFgBlack;
    this.isFgRed = other.isFgRed;
    this.isFgGreen = other.isFgGreen;
    this.isFgYellow = other.isFgYellow;
    this.isFgBlue = other.isFgBlue;
    this.isFgMagenta = other.isFgMagenta;
    this.isFgCyan = other.isFgCyan;
    this.isFgWhite = other.isFgWhite;
    this.isFg8Bit = other.isFg8Bit;
    this.fg8Bit = other.fg8Bit;
    this.isFg24Bit = other.isFg24Bit;
    this.fg24BitR = other.fg24BitR;
    this.fg24BitG = other.fg24BitG;
    this.fg24BitB = other.fg24BitB;
    this.isBgBlack = other.isBgBlack;
    this.isBgRed = other.isBgRed;
    this.isBgGreen = other.isBgGreen;
    this.isBgYellow = other.isBgYellow;
    this.isBgBlue = other.isBgBlue;
    this.isBgMagenta = other.isBgMagenta;
    this.isBgCyan = other.isBgCyan;
    this.isBgWhite = other.isBgWhite;
    this.isBg8Bit = other.isBg8Bit;
    this.bg8Bit = other.bg8Bit;
    this.isBg24Bit = other.isBg24Bit;
    this.bg24BitR = other.bg24BitR;
    this.bg24BitG = other.bg24BitG;
    this.bg24BitB = other.bg24BitB;
  }

  public static SGR of(Object... children) {
    var sgr = new SGR();
    for (var child : children) {
      sgr.add(child);
    }
    return sgr;
  }

  public void add(Object child) {
    if (this.text != null) {
      throw new UnsupportedOperationException("Cannot add child to an SGR text node");
    }
    if (this.children == null) {
      this.children = new ArrayList<>();
    }
    if (child instanceof SGR) {
      this.children.add((SGR) child);
    } else {
      this.children.add(new SGR(child.toString()));
    }
  }

  private void update(SGR other) {
    if (other.isBold) {
      bold(this);
    }
    if (other.isFaint) {
      faint(this);
    }
    if (other.isItalicized) {
      italicized(this);
    }
    if (other.isSinglyUnderlined) {
      singlyUnderlined(this);
    }
    if (other.isSlowlyBlinking) {
      slowlyBlinking(this);
    }
    if (other.isRapidlyBlinking) {
      rapidlyBlinking(this);
    }
    if (other.isNegativeImage) {
      negativeImage(this);
    }
    if (other.isConcealedCharacters) {
      concealedCharacters(this);
    }
    if (other.isCrossedOut) {
      crossedOut(this);
    }
    if (other.isFraktur) {
      fraktur(this);
    }
    if (other.isDoublyUnderlined) {
      doublyUnderlined(this);
    }
    if (other.isFgBlack) {
      fgBlack(this);
    }
    if (other.isFgRed) {
      fgRed(this);
    }
    if (other.isFgGreen) {
      fgGreen(this);
    }
    if (other.isFgYellow) {
      fgYellow(this);
    }
    if (other.isFgBlue) {
      fgBlue(this);
    }
    if (other.isFgMagenta) {
      fgMagenta(this);
    }
    if (other.isFgCyan) {
      fgCyan(this);
    }
    if (other.isFgWhite) {
      fgWhite(this);
    }
    if (other.isFg8Bit) {
      fg8Bit(other.fg8Bit, this);
    }
    if (other.isFg24Bit) {
      fgRGB(other.fg24BitR, other.fg24BitG, other.fg24BitB, this);
    }
    if (other.isBgBlack) {
      bgBlack(this);
    }
    if (other.isBgRed) {
      bgRed(this);
    }
    if (other.isBgGreen) {
      bgGreen(this);
    }
    if (other.isBgYellow) {
      bgYellow(this);
    }
    if (other.isBgBlue) {
      bgBlue(this);
    }
    if (other.isBgMagenta) {
      bgMagenta(this);
    }
    if (other.isBgCyan) {
      bgCyan(this);
    }
    if (other.isBgWhite) {
      bgWhite(this);
    }
    if (other.isBg8Bit) {
      bg8Bit(other.bg8Bit, this);
    }
    if (other.isBg24Bit) {
      bgRGB(other.bg24BitR, other.bg24BitG, other.bg24BitB, this);
    }
  }

  private String getSGRPrefix() {
    var sb = new StringBuilder();
    if (isBold) {
      sb.append(";1");
    }
    if (isFaint) {
      sb.append(";2");
    }
    if (isItalicized) {
      sb.append(";3");
    }
    if (isSinglyUnderlined) {
      sb.append(";4");
    }
    if (isSlowlyBlinking) {
      sb.append(";5");
    }
    if (isRapidlyBlinking) {
      sb.append(";6");
    }
    if (isNegativeImage) {
      sb.append(";7");
    }
    if (isConcealedCharacters) {
      sb.append(";8");
    }
    if (isCrossedOut) {
      sb.append(";9");
    }
    if (isFraktur) {
      sb.append(";20");
    }
    if (isDoublyUnderlined) {
      sb.append(";21");
    }
    if (isFgBlack) {
      sb.append(";30");
    }
    if (isFgRed) {
      sb.append(";31");
    }
    if (isFgGreen) {
      sb.append(";32");
    }
    if (isFgYellow) {
      sb.append(";33");
    }
    if (isFgBlue) {
      sb.append(";34");
    }
    if (isFgMagenta) {
      sb.append(";35");
    }
    if (isFgCyan) {
      sb.append(";36");
    }
    if (isFgWhite) {
      sb.append(";37");
    }
    if (isFg8Bit) {
      sb.append(String.format(";38;5;%d", fg8Bit));
    }
    if (isFg24Bit) {
      sb.append(String.format(";38;2;%d;%d;%d", fg24BitR, fg24BitG, fg24BitB));
    }
    if (isBgBlack) {
      sb.append(";40");
    }
    if (isBgRed) {
      sb.append(";41");
    }
    if (isBgGreen) {
      sb.append(";42");
    }
    if (isBgYellow) {
      sb.append(";43");
    }
    if (isBgBlue) {
      sb.append(";44");
    }
    if (isBgMagenta) {
      sb.append(";45");
    }
    if (isBgCyan) {
      sb.append(";46");
    }
    if (isBgWhite) {
      sb.append(";47");
    }
    if (isBg8Bit) {
      sb.append(String.format(";48;5;%d", bg8Bit));
    }
    if (isBg24Bit) {
      sb.append(String.format(";48;2;%d;%d;%d", bg24BitR, bg24BitG, bg24BitB));
    }
    if (sb.length() == 0) {
      return null;
    }
    sb.deleteCharAt(0);
    return String.format("\u001B[%sm", sb.toString());
  }

  public static SGR bold(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isFaint = false;
      ((SGR) child).isBold = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBold = true;
      return sgr;
    }
  }

  public static SGR faint(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isFaint = true;
      ((SGR) child).isBold = false;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFaint = true;
      return sgr;
    }
  }

  public static SGR italicized(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isItalicized = true;
      ((SGR) child).isFraktur = false;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isItalicized = true;
      return sgr;
    }
  }

  public static SGR fraktur(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isItalicized = false;
      ((SGR) child).isFraktur = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFraktur = true;
      return sgr;
    }
  }

  public static SGR singlyUnderlined(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isSinglyUnderlined = true;
      ((SGR) child).isDoublyUnderlined = false;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isSinglyUnderlined = true;
      return sgr;
    }
  }

  public static SGR doublyUnderlined(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isSinglyUnderlined = false;
      ((SGR) child).isDoublyUnderlined = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isDoublyUnderlined = true;
      return sgr;
    }
  }

  public static SGR slowlyBlinking(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isSlowlyBlinking = true;
      ((SGR) child).isRapidlyBlinking = false;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isSlowlyBlinking = true;
      return sgr;
    }
  }

  public static SGR rapidlyBlinking(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isSlowlyBlinking = false;
      ((SGR) child).isRapidlyBlinking = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isRapidlyBlinking = true;
      return sgr;
    }
  }

  public static SGR negativeImage(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isNegativeImage = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isNegativeImage = true;
      return sgr;
    }
  }

  public static SGR concealedCharacters(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isConcealedCharacters = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isConcealedCharacters = true;
      return sgr;
    }
  }

  public static SGR crossedOut(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).isCrossedOut = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isCrossedOut = true;
      return sgr;
    }
  }

  private void resetFgColors() {
    isFgBlack = false;
    isFgRed = false;
    isFgGreen = false;
    isFgYellow = false;
    isFgBlue = false;
    isFgMagenta = false;
    isFgCyan = false;
    isFgWhite = false;
    isFg8Bit = false;
    fg8Bit = 0;
    isFg24Bit = false;
    fg24BitR = 0;
    fg24BitG = 0;
    fg24BitB = 0;
  }

  public static SGR fgBlack(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetFgColors();
      ((SGR) child).isFgBlack = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFgBlack = true;
      return sgr;
    }
  }

  public static SGR fgRed(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetFgColors();
      ((SGR) child).isFgRed = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFgRed = true;
      return sgr;
    }
  }

  public static SGR fgGreen(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetFgColors();
      ((SGR) child).isFgGreen = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFgGreen = true;
      return sgr;
    }
  }

  public static SGR fgYellow(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetFgColors();
      ((SGR) child).isFgYellow = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFgYellow = true;
      return sgr;
    }
  }

  public static SGR fgBlue(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetFgColors();
      ((SGR) child).isFgBlue = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFgBlue = true;
      return sgr;
    }
  }

  public static SGR fgMagenta(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetFgColors();
      ((SGR) child).isFgMagenta = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFgMagenta = true;
      return sgr;
    }
  }

  public static SGR fgCyan(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetFgColors();
      ((SGR) child).isFgCyan = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFgCyan = true;
      return sgr;
    }
  }

  public static SGR fgWhite(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetFgColors();
      ((SGR) child).isFgWhite = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFgWhite = true;
      return sgr;
    }
  }

  public static SGR fg8Bit(int n, Object child) {
    if (!isByte(n)) {
      throw new IllegalArgumentException("Invalid 8-bit color");
    }
    if (child instanceof SGR) {
      ((SGR) child).resetFgColors();
      ((SGR) child).isFg8Bit = true;
      ((SGR) child).fg8Bit = n;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFg8Bit = true;
      sgr.fg8Bit = n;
      return sgr;
    }
  }

  public static SGR fgRGB(int r, int g, int b, Object child) {
    if (!isByte(r) || !isByte(g) || !isByte(b)) {
      throw new IllegalArgumentException("Invalid RGB color");
    }
    if (child instanceof SGR) {
      ((SGR) child).resetFgColors();
      ((SGR) child).isFg24Bit = true;
      ((SGR) child).fg24BitR = r;
      ((SGR) child).fg24BitG = g;
      ((SGR) child).fg24BitB = b;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isFg24Bit = true;
      sgr.fg24BitR = r;
      sgr.fg24BitG = g;
      sgr.fg24BitB = b;
      return sgr;
    }
  }

  private void resetBgColors() {
    isBgBlack = false;
    isBgRed = false;
    isBgGreen = false;
    isBgYellow = false;
    isBgBlue = false;
    isBgMagenta = false;
    isBgCyan = false;
    isBgWhite = false;
    isBg8Bit = false;
    bg8Bit = 0;
    isBg24Bit = false;
    bg24BitR = 0;
    bg24BitG = 0;
    bg24BitB = 0;
  }

  public static SGR bgBlack(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetBgColors();
      ((SGR) child).isBgBlack = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBgBlack = true;
      return sgr;
    }
  }

  public static SGR bgRed(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetBgColors();
      ((SGR) child).isBgRed = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBgRed = true;
      return sgr;
    }
  }

  public static SGR bgGreen(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetBgColors();
      ((SGR) child).isBgGreen = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBgGreen = true;
      return sgr;
    }
  }

  public static SGR bgYellow(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetBgColors();
      ((SGR) child).isBgYellow = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBgYellow = true;
      return sgr;
    }
  }

  public static SGR bgBlue(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetBgColors();
      ((SGR) child).isBgBlue = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBgBlue = true;
      return sgr;
    }
  }

  public static SGR bgMagenta(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetBgColors();
      ((SGR) child).isBgMagenta = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBgMagenta = true;
      return sgr;
    }
  }

  public static SGR bgCyan(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetBgColors();
      ((SGR) child).isBgCyan = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBgCyan = true;
      return sgr;
    }
  }

  public static SGR bgWhite(Object child) {
    if (child instanceof SGR) {
      ((SGR) child).resetBgColors();
      ((SGR) child).isBgWhite = true;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBgWhite = true;
      return sgr;
    }
  }

  public static SGR bg8Bit(int n, Object child) {
    if (!isByte(n)) {
      throw new IllegalArgumentException("Invalid 8-bit color");
    }
    if (child instanceof SGR) {
      ((SGR) child).resetBgColors();
      ((SGR) child).isBg8Bit = true;
      ((SGR) child).bg8Bit = n;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBg8Bit = true;
      sgr.bg8Bit = n;
      return sgr;
    }
  }

  public static SGR bgRGB(int r, int g, int b, Object child) {
    if (!isByte(r) || !isByte(g) || !isByte(b)) {
      throw new IllegalArgumentException("Invalid RGB color");
    }
    if (child instanceof SGR) {
      ((SGR) child).resetBgColors();
      ((SGR) child).isBg24Bit = true;
      ((SGR) child).bg24BitR = r;
      ((SGR) child).bg24BitG = g;
      ((SGR) child).bg24BitB = b;
      return (SGR) child;
    } else {
      var sgr = new SGR(child.toString());
      sgr.isBg24Bit = true;
      sgr.bg24BitR = r;
      sgr.bg24BitG = g;
      sgr.bg24BitB = b;
      return sgr;
    }
  }

  public String getPlainString() {
    if (text != null) {
      return text;
    }
    if (children == null) {
      return "";
    }
    var sb = new StringBuilder();
    for (var child : children) {
      sb.append(child.getPlainString());
    }
    return sb.toString();
  }

  private String getSGRString(SGR ctx) {
    ctx.update(this);
    if (text != null) {
      var prefix = ctx.getSGRPrefix();
      if (prefix == null) {
        return text;
      }
      return String.format("%s%s\u001B[m", prefix, text);
    }
    if (children == null) {
      return "";
    }
    var sb = new StringBuilder();
    for (var child : children) {
      sb.append(child.getSGRString(new SGR(ctx)));
    }
    return sb.toString();
  }

  public String getSGRString() {
    return getSGRString(new SGR());
  }

  public int length() {
    if (text != null) {
      return text.length();
    }
    if (children == null) {
      return 0;
    }
    var length = 0;
    for (var child : children) {
      length += child.length();
    }
    return length;
  }

  private static boolean isByte(int n) {
    return n >= 0 && n <= 255;
  }
}

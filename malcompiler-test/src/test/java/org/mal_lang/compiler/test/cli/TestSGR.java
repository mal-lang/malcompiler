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
package org.mal_lang.compiler.test.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.cli.SGR;
import org.mal_lang.compiler.test.MalTest;

public class TestSGR extends MalTest {
  @Test
  public void testPlainString() {
    assertEquals("", SGR.of().getPlainString());
    assertEquals("string1", SGR.bold("string1").getPlainString());
    assertEquals("string2", SGR.faint("string2").getPlainString());
    assertEquals("string3", SGR.italicized("string3").getPlainString());
    assertEquals("string4", SGR.fraktur("string4").getPlainString());
    assertEquals("string5", SGR.singlyUnderlined("string5").getPlainString());
    assertEquals("string6", SGR.doublyUnderlined("string6").getPlainString());
    assertEquals("string7", SGR.slowlyBlinking("string7").getPlainString());
    assertEquals("string8", SGR.rapidlyBlinking("string8").getPlainString());
    assertEquals("string9", SGR.negativeImage("string9").getPlainString());
    assertEquals("string10", SGR.concealedCharacters("string10").getPlainString());
    assertEquals("string11", SGR.crossedOut("string11").getPlainString());
    assertEquals("string12", SGR.fgBlack("string12").getPlainString());
    assertEquals("string13", SGR.fgRed("string13").getPlainString());
    assertEquals("string14", SGR.fgGreen("string14").getPlainString());
    assertEquals("string15", SGR.fgYellow("string15").getPlainString());
    assertEquals("string16", SGR.fgBlue("string16").getPlainString());
    assertEquals("string17", SGR.fgMagenta("string17").getPlainString());
    assertEquals("string18", SGR.fgCyan("string18").getPlainString());
    assertEquals("string19", SGR.fgWhite("string19").getPlainString());
    assertEquals("string20", SGR.fg8Bit(128, "string20").getPlainString());
    assertEquals("string21", SGR.fgRGB(64, 128, 192, "string21").getPlainString());
    assertEquals("string22", SGR.bgBlack("string22").getPlainString());
    assertEquals("string23", SGR.bgRed("string23").getPlainString());
    assertEquals("string24", SGR.bgGreen("string24").getPlainString());
    assertEquals("string25", SGR.bgYellow("string25").getPlainString());
    assertEquals("string26", SGR.bgBlue("string26").getPlainString());
    assertEquals("string27", SGR.bgMagenta("string27").getPlainString());
    assertEquals("string28", SGR.bgCyan("string28").getPlainString());
    assertEquals("string29", SGR.bgWhite("string29").getPlainString());
    assertEquals("string30", SGR.bg8Bit(128, "string30").getPlainString());
    assertEquals("string31", SGR.bgRGB(64, 128, 192, "string31").getPlainString());
    assertEquals(
        "a b c",
        SGR.of(SGR.bold("a"), " ", SGR.fgRed("b"), " ", SGR.bgGreen("c")).getPlainString());
  }

  @Test
  public void testSGRString() {
    assertEquals("", SGR.of().getSGRString());
    assertEquals("\u001B[1mstring1\u001B[m", SGR.bold("string1").getSGRString());
    assertEquals("\u001B[2mstring2\u001B[m", SGR.faint("string2").getSGRString());
    assertEquals("\u001B[3mstring3\u001B[m", SGR.italicized("string3").getSGRString());
    assertEquals("\u001B[20mstring4\u001B[m", SGR.fraktur("string4").getSGRString());
    assertEquals("\u001B[4mstring5\u001B[m", SGR.singlyUnderlined("string5").getSGRString());
    assertEquals("\u001B[21mstring6\u001B[m", SGR.doublyUnderlined("string6").getSGRString());
    assertEquals("\u001B[5mstring7\u001B[m", SGR.slowlyBlinking("string7").getSGRString());
    assertEquals("\u001B[6mstring8\u001B[m", SGR.rapidlyBlinking("string8").getSGRString());
    assertEquals("\u001B[7mstring9\u001B[m", SGR.negativeImage("string9").getSGRString());
    assertEquals("\u001B[8mstring10\u001B[m", SGR.concealedCharacters("string10").getSGRString());
    assertEquals("\u001B[9mstring11\u001B[m", SGR.crossedOut("string11").getSGRString());
    assertEquals("\u001B[30mstring12\u001B[m", SGR.fgBlack("string12").getSGRString());
    assertEquals("\u001B[31mstring13\u001B[m", SGR.fgRed("string13").getSGRString());
    assertEquals("\u001B[32mstring14\u001B[m", SGR.fgGreen("string14").getSGRString());
    assertEquals("\u001B[33mstring15\u001B[m", SGR.fgYellow("string15").getSGRString());
    assertEquals("\u001B[34mstring16\u001B[m", SGR.fgBlue("string16").getSGRString());
    assertEquals("\u001B[35mstring17\u001B[m", SGR.fgMagenta("string17").getSGRString());
    assertEquals("\u001B[36mstring18\u001B[m", SGR.fgCyan("string18").getSGRString());
    assertEquals("\u001B[37mstring19\u001B[m", SGR.fgWhite("string19").getSGRString());
    assertEquals("\u001B[38;5;128mstring20\u001B[m", SGR.fg8Bit(128, "string20").getSGRString());
    assertEquals(
        "\u001B[38;2;64;128;192mstring21\u001B[m",
        SGR.fgRGB(64, 128, 192, "string21").getSGRString());
    assertEquals("\u001B[40mstring22\u001B[m", SGR.bgBlack("string22").getSGRString());
    assertEquals("\u001B[41mstring23\u001B[m", SGR.bgRed("string23").getSGRString());
    assertEquals("\u001B[42mstring24\u001B[m", SGR.bgGreen("string24").getSGRString());
    assertEquals("\u001B[43mstring25\u001B[m", SGR.bgYellow("string25").getSGRString());
    assertEquals("\u001B[44mstring26\u001B[m", SGR.bgBlue("string26").getSGRString());
    assertEquals("\u001B[45mstring27\u001B[m", SGR.bgMagenta("string27").getSGRString());
    assertEquals("\u001B[46mstring28\u001B[m", SGR.bgCyan("string28").getSGRString());
    assertEquals("\u001B[47mstring29\u001B[m", SGR.bgWhite("string29").getSGRString());
    assertEquals("\u001B[48;5;128mstring30\u001B[m", SGR.bg8Bit(128, "string30").getSGRString());
    assertEquals(
        "\u001B[48;2;64;128;192mstring31\u001B[m",
        SGR.bgRGB(64, 128, 192, "string31").getSGRString());
    assertEquals(
        "\u001B[1ma\u001B[m \u001B[31mb\u001B[m \u001B[42mc\u001B[m",
        SGR.of(SGR.bold("a"), " ", SGR.fgRed("b"), " ", SGR.bgGreen("c")).getSGRString());
  }

  private void assertInvalidFg8Bit(int n, String s) {
    try {
      SGR.fg8Bit(n, s);
      fail("SGR.fg8Bit should have thrown an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid 8-bit color", e.getMessage());
    }
  }

  private void assertInvalidBg8Bit(int n, String s) {
    try {
      SGR.bg8Bit(n, s);
      fail("SGR.bg8Bit should have thrown an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid 8-bit color", e.getMessage());
    }
  }

  private void assertInvalidFgRGB(int r, int g, int b, String s) {
    try {
      SGR.fgRGB(r, g, b, s);
      fail("SGR.fgRGB should have thrown an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid RGB color", e.getMessage());
    }
  }

  private void assertInvalidBgRGB(int r, int g, int b, String s) {
    try {
      SGR.bgRGB(r, g, b, s);
      fail("SGR.bgRGB should have thrown an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid RGB color", e.getMessage());
    }
  }

  @Test
  public void testBadParams() {
    assertInvalidFg8Bit(-1, "string");
    assertInvalidFg8Bit(256, "string");
    assertInvalidBg8Bit(-1, "string");
    assertInvalidBg8Bit(256, "string");
    assertInvalidFgRGB(-1, 128, 128, "string");
    assertInvalidFgRGB(256, 128, 128, "string");
    assertInvalidFgRGB(128, -1, 128, "string");
    assertInvalidFgRGB(128, 256, 128, "string");
    assertInvalidFgRGB(128, 128, -1, "string");
    assertInvalidFgRGB(128, 128, 256, "string");
    assertInvalidBgRGB(-1, 128, 128, "string");
    assertInvalidBgRGB(256, 128, 128, "string");
    assertInvalidBgRGB(128, -1, 128, "string");
    assertInvalidBgRGB(128, 256, 128, "string");
    assertInvalidBgRGB(128, 128, -1, "string");
    assertInvalidBgRGB(128, 128, 256, "string");
  }

  @Test
  public void testAdd() {
    var sgr = SGR.of();
    sgr.add("text1");
    sgr.add("text2");
    assertEquals("text1text2", sgr.getPlainString());
    assertEquals("text1text2", sgr.getSGRString());
    sgr.add(SGR.bold("text3"));
    assertEquals("text1text2text3", sgr.getPlainString());
    assertEquals("text1text2\u001B[1mtext3\u001B[m", sgr.getSGRString());
    try {
      SGR.bold("text1").add("text2");
      fail("SGR.add should have thrown an UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      assertEquals("Cannot add child to an SGR text node", e.getMessage());
    }
  }

  @Test
  public void testStacking() {
    assertEquals("\u001B[1;3mtext\u001B[m", SGR.bold(SGR.italicized("text")).getSGRString());
  }

  @Test
  public void testNesting() {
    assertEquals(
        "\u001B[1mtext1 \u001B[m\u001B[1;3mtext2\u001B[m\u001B[1m text3\u001B[m",
        SGR.bold(SGR.of("text1 ", SGR.italicized("text2"), " text3")).getSGRString());
  }
}

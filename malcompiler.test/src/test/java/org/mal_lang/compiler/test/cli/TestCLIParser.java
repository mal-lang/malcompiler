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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mal_lang.compiler.cli.CLIParser.HasArgument.NO_ARGUMENT;
import static org.mal_lang.compiler.cli.CLIParser.HasArgument.OPTIONAL_ARGUMENT;
import static org.mal_lang.compiler.cli.CLIParser.HasArgument.REQUIRED_ARGUMENT;

import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.cli.CLIArguments;
import org.mal_lang.compiler.cli.CLIParser;
import org.mal_lang.compiler.test.MalTest;

public class TestCLIParser extends MalTest {
  private static final String FAIL_ILLEGAL_ARGUMENT =
      "CLIParser.addOption should have thrown an IllegalArgumentException";

  private static void assertInvalidShortOption(IllegalArgumentException e, String shortOpt) {
    assertEquals(
        String.format(
            "Invalid short option %s: Each short option name should be a single alphanumeric character",
            shortOpt),
        e.getMessage());
  }

  private static void assertDuplicateShortOption(IllegalArgumentException e, String shortOpt) {
    assertEquals(String.format("Short option %s has already been added", shortOpt), e.getMessage());
  }

  private static void assertInvalidLongOption(IllegalArgumentException e, String longOpt) {
    assertEquals(
        String.format(
            "Invalid long option \"%s\": Each long option name should be made of alphanumeric characters and dashes",
            longOpt),
        e.getMessage());
  }

  private static void assertDuplicateLongOption(IllegalArgumentException e, String longOpt) {
    assertEquals(
        String.format("Long option \"%s\" has already been added", longOpt), e.getMessage());
  }

  @Test
  public void testInvalidShortOption() {
    // Invalid short option
    try {
      var cli = new CLIParser();
      cli.addOption('-', NO_ARGUMENT);
      fail(FAIL_ILLEGAL_ARGUMENT);
    } catch (IllegalArgumentException e) {
      assertInvalidShortOption(e, "'-'");
    }
    // Invalid short option + valid long option
    try {
      var cli = new CLIParser();
      cli.addOption('\n', "-", NO_ARGUMENT);
      fail(FAIL_ILLEGAL_ARGUMENT);
    } catch (IllegalArgumentException e) {
      assertInvalidShortOption(e, "0x000A");
    }
    // Invalid short option + invalid long option
    try {
      var cli = new CLIParser();
      cli.addOption('ä', "\n", NO_ARGUMENT);
      fail(FAIL_ILLEGAL_ARGUMENT);
    } catch (IllegalArgumentException e) {
      assertInvalidShortOption(e, "0x00E4");
    }
  }

  @Test
  public void testDuplicateShortOption() {
    // Duplicate short option
    try {
      var cli = new CLIParser();
      cli.addOption('0', NO_ARGUMENT);
      try {
        cli.addOption('0', NO_ARGUMENT);
        fail(FAIL_ILLEGAL_ARGUMENT);
      } catch (IllegalArgumentException e) {
        assertDuplicateShortOption(e, "'0'");
      }
    } catch (IllegalArgumentException e) {
      fail(e.getMessage());
    }
    // Duplicate short option + non-duplicate long option
    try {
      var cli = new CLIParser();
      cli.addOption('a', "a", NO_ARGUMENT);
      try {
        cli.addOption('a', "b", NO_ARGUMENT);
        fail(FAIL_ILLEGAL_ARGUMENT);
      } catch (IllegalArgumentException e) {
        assertDuplicateShortOption(e, "'a'");
      }
    } catch (IllegalArgumentException e) {
      fail(e.getMessage());
    }
    // Duplicate short option + duplicate long option
    try {
      var cli = new CLIParser();
      cli.addOption('A', "a", NO_ARGUMENT);
      try {
        cli.addOption('A', "a", NO_ARGUMENT);
        fail(FAIL_ILLEGAL_ARGUMENT);
      } catch (IllegalArgumentException e) {
        assertDuplicateShortOption(e, "'A'");
      }
    } catch (IllegalArgumentException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testInvalidLongOption() {
    // Invalid long option
    try {
      var cli = new CLIParser();
      cli.addOption("/", NO_ARGUMENT);
      fail(FAIL_ILLEGAL_ARGUMENT);
    } catch (IllegalArgumentException e) {
      assertInvalidLongOption(e, "/");
    }
    // Valid short option + invalid long option
    try {
      var cli = new CLIParser();
      cli.addOption('a', "\n", NO_ARGUMENT);
      fail(FAIL_ILLEGAL_ARGUMENT);
    } catch (IllegalArgumentException e) {
      assertInvalidLongOption(e, "\n");
    }
  }

  @Test
  public void testDuplicateLongOption() {
    // Duplicate long option
    try {
      var cli = new CLIParser();
      cli.addOption("a-a", NO_ARGUMENT);
      try {
        cli.addOption("a-a", NO_ARGUMENT);
        fail(FAIL_ILLEGAL_ARGUMENT);
      } catch (IllegalArgumentException e) {
        assertDuplicateLongOption(e, "a-a");
      }
    } catch (IllegalArgumentException e) {
      fail(e.getMessage());
    }
    // Non-duplicate short option + duplicate long option
    try {
      var cli = new CLIParser();
      cli.addOption('a', "a-0", NO_ARGUMENT);
      try {
        cli.addOption('b', "a-0", NO_ARGUMENT);
        fail(FAIL_ILLEGAL_ARGUMENT);
      } catch (IllegalArgumentException e) {
        assertDuplicateLongOption(e, "a-0");
      }
    } catch (IllegalArgumentException e) {
      fail(e.getMessage());
    }
  }

  private static void assertInvalidOption(CLIArguments.Option option, String error) {
    assertTrue(option instanceof CLIArguments.InvalidOption);
    var invalidOption = (CLIArguments.InvalidOption) option;
    assertFalse(invalidOption.hasArgument());
    assertNull(invalidOption.getArgument());
    assertEquals(-1, invalidOption.getValue());
    assertEquals(error, invalidOption.getError());
  }

  private static void assertLongOption(
      CLIArguments.Option option, String argument, int value, String optionString) {
    assertTrue(option instanceof CLIArguments.LongOption);
    var longOption = (CLIArguments.LongOption) option;
    if (argument == null) {
      assertFalse(longOption.hasArgument());
      assertNull(longOption.getArgument());
    } else {
      assertTrue(longOption.hasArgument());
      assertEquals(argument, longOption.getArgument());
    }
    assertEquals(value, longOption.getValue());
    assertEquals(optionString, longOption.getOption());
  }

  private static void assertShortOption(
      CLIArguments.Option option, String argument, int value, char optionChar) {
    assertTrue(option instanceof CLIArguments.ShortOption);
    var shortOption = (CLIArguments.ShortOption) option;
    if (argument == null) {
      assertFalse(shortOption.hasArgument());
      assertNull(shortOption.getArgument());
    } else {
      assertTrue(shortOption.hasArgument());
      assertEquals(argument, shortOption.getArgument());
    }
    assertEquals(value, shortOption.getValue());
    assertEquals(optionChar, shortOption.getOption());
  }

  @Test
  public void testEndOfOptions() {
    CLIParser cli = null;
    try {
      cli = new CLIParser();
      cli.addOption('a', "a", NO_ARGUMENT);
    } catch (IllegalArgumentException e) {
      fail(e.getMessage());
    }

    // Option parsing ending at "--"
    var cliArgs = cli.parse(new String[] {"a", "-a", "--a", "--", "a", "-a", "--a"});
    var options = cliArgs.getOptions();
    var operands = cliArgs.getOperands();
    // Options "-a", "--a"
    assertEquals(2, options.size());
    assertShortOption(options.get(0), null, 0, 'a');
    assertLongOption(options.get(1), null, 0, "a");
    // Operands "a", "a", "-a", "--a"
    assertEquals(4, operands.size());
    assertEquals("a", operands.get(0));
    assertEquals("a", operands.get(1));
    assertEquals("-a", operands.get(2));
    assertEquals("--a", operands.get(3));
  }

  @Test
  public void testDashOption() {
    CLIParser cli = null;
    try {
      cli = new CLIParser();
      cli.addOption('a', "a", NO_ARGUMENT);
    } catch (IllegalArgumentException e) {
      fail(e.getMessage());
    }

    // Operand only containing one dash
    var cliArgs = cli.parse(new String[] {"a", "-a", "--a", "-"});
    var options = cliArgs.getOptions();
    var operands = cliArgs.getOperands();
    // Options "-a", "--a"
    assertEquals(2, options.size());
    assertShortOption(options.get(0), null, 0, 'a');
    assertLongOption(options.get(1), null, 0, "a");
    // Operands "a", "-"
    assertEquals(2, operands.size());
    assertEquals("a", operands.get(0));
    assertEquals("-", operands.get(1));
  }

  @Test
  public void testLongOptions() {
    CLIParser cli = null;
    try {
      cli = new CLIParser();
      cli.addOption("a", NO_ARGUMENT);
      cli.addOption("b", REQUIRED_ARGUMENT);
      cli.addOption("c", OPTIONAL_ARGUMENT);
    } catch (IllegalArgumentException e) {
      fail(e.getMessage());
    }

    // Invalid long option
    var cliArgs = cli.parse(new String[] {"--0", "--0=arg", "--0", "arg", "--0="});
    var options = cliArgs.getOptions();
    var operands = cliArgs.getOperands();
    // Options "--0", "--0=arg", "--0", "--0="
    assertEquals(4, options.size());
    assertInvalidOption(options.get(0), "Invalid option --0");
    assertInvalidOption(options.get(1), "Invalid option --0");
    assertInvalidOption(options.get(2), "Invalid option --0");
    assertInvalidOption(options.get(3), "Invalid option --0");
    // Operands "arg"
    assertEquals(1, operands.size());
    assertEquals("arg", operands.get(0));

    // Long option without argument
    cliArgs = cli.parse(new String[] {"--a", "--a=arg", "--a", "arg", "--a="});
    options = cliArgs.getOptions();
    operands = cliArgs.getOperands();
    // Options "--a", "--a=arg", "--a", "--a="
    assertEquals(4, options.size());
    assertLongOption(options.get(0), null, 0, "a");
    assertInvalidOption(options.get(1), "Option --a doesn't allow an argument");
    assertLongOption(options.get(2), null, 0, "a");
    assertInvalidOption(options.get(3), "Option --a doesn't allow an argument");
    // Operands "arg"
    assertEquals(1, operands.size());
    assertEquals("arg", operands.get(0));

    // Long option with required argument
    cliArgs = cli.parse(new String[] {"--b", "--b=arg", "--b=arg", "--b", "arg", "--b=", "--b"});
    options = cliArgs.getOptions();
    operands = cliArgs.getOperands();
    // Options "--b --b=arg", "--b=arg", "--b arg", "--b=", "--b"
    assertEquals(5, options.size());
    assertLongOption(options.get(0), "--b=arg", 1, "b");
    assertLongOption(options.get(1), "arg", 1, "b");
    assertLongOption(options.get(2), "arg", 1, "b");
    assertLongOption(options.get(3), "", 1, "b");
    assertInvalidOption(options.get(4), "Option --b requires an argument");
    // Operands
    assertEquals(0, operands.size());

    // Long option with optional argument
    cliArgs = cli.parse(new String[] {"--c", "--c=arg", "--c", "arg", "--c="});
    options = cliArgs.getOptions();
    operands = cliArgs.getOperands();
    // Options "--c", "--c=arg", "--c", "--c="
    assertEquals(4, options.size());
    assertLongOption(options.get(0), null, 2, "c");
    assertLongOption(options.get(1), "arg", 2, "c");
    assertLongOption(options.get(2), null, 2, "c");
    assertLongOption(options.get(3), "", 2, "c");
    // Operands "arg"
    assertEquals(1, operands.size());
    assertEquals("arg", operands.get(0));
  }

  @Test
  public void testShortOptions() {
    CLIParser cli = null;
    try {
      cli = new CLIParser();
      cli.addOption('a', NO_ARGUMENT);
      cli.addOption('b', REQUIRED_ARGUMENT);
      cli.addOption('c', OPTIONAL_ARGUMENT);
    } catch (IllegalArgumentException e) {
      fail(e.getMessage());
    }

    // Invalid short option
    var cliArgs = cli.parse(new String[] {"-0", "-0arg", "-0", "arg", "-0"});
    var options = cliArgs.getOptions();
    var operands = cliArgs.getOperands();
    // Options "-0", "-0", "-a", "-r", "-g", "-0", "-0"
    assertEquals(7, options.size());
    assertInvalidOption(options.get(0), "Invalid option -0");
    assertInvalidOption(options.get(1), "Invalid option -0");
    assertShortOption(options.get(2), null, 0, 'a');
    assertInvalidOption(options.get(3), "Invalid option -r");
    assertInvalidOption(options.get(4), "Invalid option -g");
    assertInvalidOption(options.get(5), "Invalid option -0");
    assertInvalidOption(options.get(6), "Invalid option -0");
    // Operands "arg"
    assertEquals(1, operands.size());
    assertEquals("arg", operands.get(0));

    // Short option without argument
    cliArgs = cli.parse(new String[] {"-a", "-aarg", "-a", "arg", "-a"});
    options = cliArgs.getOptions();
    operands = cliArgs.getOperands();
    // Options "-a", "-a", "-a", "-r", "-g", "-a", "-a"
    assertEquals(7, options.size());
    assertShortOption(options.get(0), null, 0, 'a');
    assertShortOption(options.get(1), null, 0, 'a');
    assertShortOption(options.get(2), null, 0, 'a');
    assertInvalidOption(options.get(3), "Invalid option -r");
    assertInvalidOption(options.get(4), "Invalid option -g");
    assertShortOption(options.get(5), null, 0, 'a');
    assertShortOption(options.get(6), null, 0, 'a');
    // Operands "arg"
    assertEquals(1, operands.size());
    assertEquals("arg", operands.get(0));

    // Short option with required argument
    cliArgs = cli.parse(new String[] {"-b", "-barg", "-barg", "-b", "arg", "-b"});
    options = cliArgs.getOptions();
    operands = cliArgs.getOperands();
    // Options "-b -barg", "-barg", "-b arg", "-b"
    assertEquals(4, options.size());
    assertShortOption(options.get(0), "-barg", 1, 'b');
    assertShortOption(options.get(1), "arg", 1, 'b');
    assertShortOption(options.get(2), "arg", 1, 'b');
    assertInvalidOption(options.get(3), "Option -b requires an argument");
    // Operands
    assertEquals(0, operands.size());

    // Short option with optional argument
    cliArgs = cli.parse(new String[] {"-c", "-carg", "-c", "arg", "-c"});
    options = cliArgs.getOptions();
    operands = cliArgs.getOperands();
    // Options "-c", "-carg", "-c", "-c"
    assertEquals(4, options.size());
    assertShortOption(options.get(0), null, 2, 'c');
    assertShortOption(options.get(1), "arg", 2, 'c');
    assertShortOption(options.get(2), null, 2, 'c');
    assertShortOption(options.get(3), null, 2, 'c');
    // Operands "arg"
    assertEquals(1, operands.size());
    assertEquals("arg", operands.get(0));
  }

  @Test
  public void testGetOptionDescriptions() {
    var cli = new CLIParser();
    cli.addOption('a', NO_ARGUMENT);
    cli.addOption("b", NO_ARGUMENT);
    cli.addOption('c', "c", NO_ARGUMENT);
    cli.addOption('d', NO_ARGUMENT, "description for d");
    cli.addOption("e", NO_ARGUMENT, "description for e\nit is long");
    cli.addOption('f', "f", NO_ARGUMENT, "description for f\nit is really\nlong");
    cli.addOption('g', REQUIRED_ARGUMENT);
    cli.addOption("h", REQUIRED_ARGUMENT);
    cli.addOption('i', "i", REQUIRED_ARGUMENT);
    cli.addOption('j', REQUIRED_ARGUMENT, "ARGUMENT1", null);
    cli.addOption("k", REQUIRED_ARGUMENT, "ARGUMENT2", null);
    cli.addOption('l', "l", REQUIRED_ARGUMENT, "VERY_SPECIFIC_ARGUMENT", null);
    cli.addOption('m', REQUIRED_ARGUMENT, "description for m");
    cli.addOption("n", REQUIRED_ARGUMENT, "description for n");
    cli.addOption('o', "o", REQUIRED_ARGUMENT, "description for o\nsome extra info");
    cli.addOption('p', REQUIRED_ARGUMENT, "ARGUMENT1", "description for p");
    cli.addOption("q", REQUIRED_ARGUMENT, "ARGUMENT2", "description for q");
    cli.addOption(
        'r',
        "r",
        REQUIRED_ARGUMENT,
        "VERY_SPECIFIC_ARGUMENT",
        "description for r\nsome extra info");
    cli.addOption('s', OPTIONAL_ARGUMENT, "");
    cli.addOption("t", OPTIONAL_ARGUMENT, "");
    cli.addOption('u', "u", OPTIONAL_ARGUMENT, "");
    cli.addOption('v', OPTIONAL_ARGUMENT, "ARGUMENT1", "\n");
    cli.addOption("w", OPTIONAL_ARGUMENT, "ARGUMENT2", "\n\n");
    cli.addOption('x', "x", OPTIONAL_ARGUMENT, "VERY_SPECIFIC_ARGUMENT", "\n\n\n");
    cli.addOption('y', OPTIONAL_ARGUMENT, "description for y");
    cli.addOption("z", OPTIONAL_ARGUMENT, "description for z");
    cli.addOption('0', "0", OPTIONAL_ARGUMENT, "description for 0\nsome extra info");
    cli.addOption('1', OPTIONAL_ARGUMENT, "ARGUMENT1", "description for 1");
    cli.addOption("2", OPTIONAL_ARGUMENT, "ARGUMENT2", "description for 2");
    cli.addOption(
        '3',
        "3",
        OPTIONAL_ARGUMENT,
        "VERY_SPECIFIC_ARGUMENT",
        "description for 3\nsome extra info");
    cli.addOption("border-line-option-x", NO_ARGUMENT, "description 1");
    cli.addOption("border-line-option-xx", NO_ARGUMENT, "description 2");
    String[] expectedLines = {
      "  -a",
      "      --b",
      "  -c, --c",
      "  -d                         description for d",
      "      --e                    description for e",
      "                               it is long",
      "  -f, --f                    description for f",
      "                               it is really",
      "                               long",
      "  -gARGUMENT",
      "      --h=ARGUMENT",
      "  -i, --i=ARGUMENT",
      "  -jARGUMENT1",
      "      --k=ARGUMENT2",
      "  -l, --l=VERY_SPECIFIC_ARGUMENT",
      "  -mARGUMENT                 description for m",
      "      --n=ARGUMENT           description for n",
      "  -o, --o=ARGUMENT           description for o",
      "                               some extra info",
      "  -pARGUMENT1                description for p",
      "      --q=ARGUMENT2          description for q",
      "  -r, --r=VERY_SPECIFIC_ARGUMENT",
      "                             description for r",
      "                               some extra info",
      "  -s[ARGUMENT]",
      "      --t[=ARGUMENT]",
      "  -u, --u[=ARGUMENT]",
      "  -v[ARGUMENT1]              ",
      "                               ",
      "      --w[=ARGUMENT2]        ",
      "                               ",
      "                               ",
      "  -x, --x[=VERY_SPECIFIC_ARGUMENT]",
      "                             ",
      "                               ",
      "                               ",
      "                               ",
      "  -y[ARGUMENT]               description for y",
      "      --z[=ARGUMENT]         description for z",
      "  -0, --0[=ARGUMENT]         description for 0",
      "                               some extra info",
      "  -1[ARGUMENT1]              description for 1",
      "      --2[=ARGUMENT2]        description for 2",
      "  -3, --3[=VERY_SPECIFIC_ARGUMENT]",
      "                             description for 3",
      "                               some extra info",
      "      --border-line-option-x description 1",
      "      --border-line-option-xx",
      "                             description 2",
      ""
    };
    var actualLines = cli.getSGROptionLines();
    var expected = String.join("\n", expectedLines);
    var actualBuffer = new StringBuffer();
    for (var actualLine : actualLines) {
      actualBuffer.append(actualLine.getPlainString());
      actualBuffer.append("\n");
    }
    var actual = actualBuffer.toString();
    assertEquals(expected, actual);
  }
}

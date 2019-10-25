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
package org.mal_lang.compiler.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CLIParser {
  private static final String lineSep = System.getProperty("line.separator");
  private static final int DESCRIPTION_XPOS = 29;
  private static final String DESCRIPTION_INDENT;

  static {
    var sb = new StringBuilder();
    sb.append(lineSep);
    for (int i = 0; i < DESCRIPTION_XPOS; i++) {
      sb.append(' ');
    }
    DESCRIPTION_INDENT = sb.toString();
  }

  private int nextValue = 0;
  private List<Option> options = new ArrayList<>();
  private Map<Character, Option> shortOptions = new HashMap<>();
  private Map<String, Option> longOptions = new HashMap<>();

  public enum HasArgument {
    NO_ARGUMENT,
    REQUIRED_ARGUMENT,
    OPTIONAL_ARGUMENT
  }

  private class Option {
    public final Optional<Character> shortOption;
    public final Optional<String> longOption;
    public final HasArgument hasArgument;
    public final String argumentName;
    public final int value;
    public final String description;

    public Option(
        Optional<Character> shortOption,
        Optional<String> longOption,
        HasArgument hasArgument,
        String argumentName,
        String description) {
      this.shortOption = shortOption;
      this.longOption = longOption;
      this.hasArgument = hasArgument;
      this.argumentName = argumentName;
      this.value = nextValue++;
      this.description = description;
    }
  }

  private static String charToString(char c) {
    if (c >= 0x20 && c <= 0x7E) {
      return String.format("'%c'", c);
    } else {
      return String.format("0x%04X", (int) c);
    }
  }

  private static boolean validShortOption(char shortOption) {
    return (shortOption >= '0' && shortOption <= '9')
        || (shortOption >= 'a' && shortOption <= 'z')
        || (shortOption >= 'A' && shortOption <= 'Z');
  }

  private static boolean validLongOption(String longOption) {
    for (int i = 0; i < longOption.length(); i++) {
      char c = longOption.charAt(i);
      if (!validShortOption(c) && c != '-') {
        return false;
      }
    }
    return true;
  }

  private void validateShortOption(char shortOption) {
    if (!validShortOption(shortOption)) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid short option %s: Each short option name should be a single alphanumeric character",
              charToString(shortOption)));
    }
    if (shortOptions.containsKey(Character.valueOf(shortOption))) {
      throw new IllegalArgumentException(
          String.format("Short option %s has already been added", charToString(shortOption)));
    }
  }

  private void validateLongOption(String longOption) {
    if (!validLongOption(longOption)) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid long option \"%s\": Each long option name should be made of alphanumeric characters and dashes",
              longOption));
    }
    if (longOptions.containsKey(longOption)) {
      throw new IllegalArgumentException(
          String.format("Long option \"%s\" has already been added", longOption));
    }
  }

  private int addOption(
      Optional<Character> shortOption,
      Optional<String> longOption,
      HasArgument hasArgument,
      String argumentName,
      String description) {
    if (shortOption.isPresent()) {
      validateShortOption(shortOption.get().charValue());
    }
    if (longOption.isPresent()) {
      validateLongOption(longOption.get());
    }
    var option = new Option(shortOption, longOption, hasArgument, argumentName, description);
    options.add(option);
    if (shortOption.isPresent()) {
      shortOptions.put(shortOption.get(), option);
    }
    if (longOption.isPresent()) {
      longOptions.put(longOption.get(), option);
    }
    return option.value;
  }

  public int addOption(char shortOption, HasArgument hasArgument) {
    return addOption(shortOption, hasArgument, null, null);
  }

  public int addOption(char shortOption, HasArgument hasArgument, String description) {
    return addOption(shortOption, hasArgument, null, description);
  }

  public int addOption(
      char shortOption, HasArgument hasArgument, String argumentName, String description) {
    return addOption(
        Optional.of(Character.valueOf(shortOption)),
        Optional.empty(),
        hasArgument,
        argumentName,
        description);
  }

  public int addOption(String longOption, HasArgument hasArgument) {
    return addOption(longOption, hasArgument, null, null);
  }

  public int addOption(String longOption, HasArgument hasArgument, String description) {
    return addOption(longOption, hasArgument, null, description);
  }

  public int addOption(
      String longOption, HasArgument hasArgument, String argumentName, String description) {
    return addOption(
        Optional.empty(), Optional.of(longOption), hasArgument, argumentName, description);
  }

  public int addOption(char shortOption, String longOption, HasArgument hasArgument) {
    return addOption(shortOption, longOption, hasArgument, null, null);
  }

  public int addOption(
      char shortOption, String longOption, HasArgument hasArgument, String description) {
    return addOption(shortOption, longOption, hasArgument, null, description);
  }

  public int addOption(
      char shortOption,
      String longOption,
      HasArgument hasArgument,
      String argumentName,
      String description) {
    return addOption(
        Optional.of(Character.valueOf(shortOption)),
        Optional.of(longOption),
        hasArgument,
        argumentName,
        description);
  }

  public CLIArguments parse(String[] args) {
    var cliArgs = new CLIArguments();
    boolean endOfOptions = false;
    for (int i = 0; i < args.length; i++) {
      var arg = args[i];
      if (endOfOptions) {
        cliArgs.addOperand(arg);
      } else if (arg.equals("-")) {
        cliArgs.addOperand(arg);
      } else if (arg.equals("--")) {
        endOfOptions = true;
      } else if (arg.startsWith("--")) {
        var longOption = arg.substring(2);
        var equalsIndex = longOption.indexOf('=');
        if (equalsIndex == -1) {
          // Long option without inline argument
          if (!longOptions.containsKey(longOption)) {
            // Invalid long option
            cliArgs.addInvalidOption(String.format("Invalid option --%s", longOption));
          } else {
            var option = longOptions.get(longOption);
            switch (option.hasArgument) {
              case NO_ARGUMENT:
              case OPTIONAL_ARGUMENT:
                // Long option without argument
                cliArgs.addLongOption(longOption, option.value);
                break;
              case REQUIRED_ARGUMENT:
                if (i + 1 == args.length) {
                  // Long option with missing argument
                  cliArgs.addInvalidOption(
                      String.format("Option --%s requires an argument", longOption));
                } else {
                  // Long option with separate argument
                  cliArgs.addLongOption(longOption, args[++i], option.value);
                }
                break;
            }
          }
        } else {
          // Long option with inline argument
          var argument = longOption.substring(equalsIndex + 1);
          longOption = longOption.substring(0, equalsIndex);
          if (!longOptions.containsKey(longOption)) {
            // Invalid long option
            cliArgs.addInvalidOption(String.format("Invalid option --%s", longOption));
          } else {
            var option = longOptions.get(longOption);
            switch (option.hasArgument) {
              case NO_ARGUMENT:
                // Long option with unexpected argument
                cliArgs.addInvalidOption(
                    String.format("Option --%s doesn't allow an argument", longOption));
                break;
              case REQUIRED_ARGUMENT:
              case OPTIONAL_ARGUMENT:
                // Long option with inline argument
                cliArgs.addLongOption(longOption, argument, option.value);
                break;
            }
          }
        }
      } else if (arg.startsWith("-")) {
        var shortOptionString = arg.substring(1);
        for (int j = 0; j < shortOptionString.length(); j++) {
          var shortOption = shortOptionString.charAt(j);
          var argument = shortOptionString.substring(j + 1);
          if (!shortOptions.containsKey(Character.valueOf(shortOption))) {
            // Invalid short option
            cliArgs.addInvalidOption(String.format("Invalid option -%c", shortOption));
          } else {
            var option = shortOptions.get(Character.valueOf(shortOption));
            switch (option.hasArgument) {
              case NO_ARGUMENT:
                // Short option without argument
                cliArgs.addShortOption(shortOption, option.value);
                break;
              case REQUIRED_ARGUMENT:
                if (argument.isEmpty()) {
                  if (i + 1 == args.length) {
                    // Short option with missing argument
                    cliArgs.addInvalidOption(
                        String.format("Option -%c requires an argument", shortOption));
                  } else {
                    // Short option with separate argument
                    cliArgs.addShortOption(shortOption, args[++i], option.value);
                  }
                } else {
                  // Short option with inline argument
                  cliArgs.addShortOption(shortOption, argument, option.value);
                  j = shortOptionString.length() - 1;
                }
                break;
              case OPTIONAL_ARGUMENT:
                if (argument.isEmpty()) {
                  // Short option without argument
                  cliArgs.addShortOption(shortOption, option.value);
                } else {
                  // Short option with inline argument
                  cliArgs.addShortOption(shortOption, argument, option.value);
                  j = shortOptionString.length() - 1;
                }
                break;
            }
          }
        }
      } else {
        cliArgs.addOperand(arg);
      }
    }
    return cliArgs;
  }

  private static List<String> getDescriptionLines(String description) {
    if (description == null || description.isEmpty()) {
      // No description is present, return the empty list
      return List.of();
    }
    List<String> descriptionLines = new ArrayList<>();
    String descriptionLine = null;
    String remainder = description;
    while (remainder != null) {
      // Get the first line in the remainder
      var idx = remainder.indexOf(lineSep);
      if (idx == -1) {
        // This is the last line
        descriptionLine = remainder;
        remainder = null;
      } else {
        // There are more lines
        descriptionLine = remainder.substring(0, idx);
        remainder = remainder.substring(idx + lineSep.length());
      }
      descriptionLines.add(descriptionLine);
    }
    return descriptionLines;
  }

  public static SGR getSGROptionLine(SGR sgrOption, String description) {
    // Indent the option with two spaces
    var sgrOptionLine = SGR.of("  ", sgrOption);
    var descriptionLines = getDescriptionLines(description);
    if (descriptionLines.isEmpty()) {
      // No description is present, return the indented option
      return sgrOptionLine;
    }
    // Indent the first description line
    if (sgrOptionLine.length() >= DESCRIPTION_XPOS) {
      sgrOptionLine.add(DESCRIPTION_INDENT);
    } else {
      var sb = new StringBuilder();
      for (int i = sgrOptionLine.length(); i < DESCRIPTION_XPOS; i++) {
        sb.append(' ');
      }
      sgrOptionLine.add(sb.toString());
    }
    // Append the first description line
    sgrOptionLine.add(descriptionLines.get(0));
    // Indent and append the following description lines
    for (int i = 1; i < descriptionLines.size(); i++) {
      sgrOptionLine.add(DESCRIPTION_INDENT);
      sgrOptionLine.add("  ");
      sgrOptionLine.add(descriptionLines.get(i));
    }
    return sgrOptionLine;
  }

  private static SGR getSGROptionName(Option option) {
    SGR sgrShortOption = null;
    SGR sgrLongOption = null;
    if (option.shortOption.isPresent()) {
      var shortOption = option.shortOption.get().charValue();
      sgrShortOption = SGR.fgRGB(135, 206, 235, String.format("-%c", shortOption));
    }
    if (option.longOption.isPresent()) {
      var longOption = option.longOption.get();
      sgrLongOption = SGR.fgRGB(135, 206, 235, String.format("--%s", longOption));
    }
    if (sgrShortOption == null) {
      if (sgrLongOption != null) {
        return SGR.of("    ", sgrLongOption);
      } else {
        return SGR.of();
      }
    } else {
      if (sgrLongOption != null) {
        return SGR.of(sgrShortOption, ", ", sgrLongOption);
      } else {
        return sgrShortOption;
      }
    }
  }

  private SGR getSGROptionArgument(Option option) {
    var argumentName = option.argumentName == null ? "ARGUMENT" : option.argumentName;
    switch (option.hasArgument) {
      case REQUIRED_ARGUMENT:
        if (option.longOption.isPresent()) {
          return SGR.italicized(String.format("=%s", argumentName));
        } else {
          return SGR.italicized(String.format("%s", argumentName));
        }
      case OPTIONAL_ARGUMENT:
        if (option.longOption.isPresent()) {
          return SGR.of("[", SGR.italicized(String.format("=%s", argumentName)), "]");
        } else {
          return SGR.of("[", SGR.italicized(String.format("%s", argumentName)), "]");
        }
      default:
        return SGR.of();
    }
  }

  private SGR getSGROption(Option option) {
    return SGR.of(getSGROptionName(option), getSGROptionArgument(option));
  }

  public List<SGR> getSGROptionLines() {
    List<SGR> sgrOptionLines = new ArrayList<>();
    for (var option : options) {
      var sgrOption = getSGROption(option);
      var sgrOptionLine = getSGROptionLine(sgrOption, option.description);
      sgrOptionLines.add(sgrOptionLine);
    }
    return sgrOptionLines;
  }
}

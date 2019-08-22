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
package org.mal_lang.mal.cli;

import java.util.ArrayList;
import java.util.List;

public class CLIArguments {
  private List<Option> options = new ArrayList<>();
  private List<String> operands = new ArrayList<>();

  public abstract static class Option {
    private String argument;
    private int value;

    public Option(String argument, int value) {
      this.argument = argument;
      this.value = value;
    }

    public boolean hasArgument() {
      return argument != null;
    }

    public String getArgument() {
      return argument;
    }

    public int getValue() {
      return value;
    }
  }

  public static class ShortOption extends Option {
    private char option;

    public ShortOption(char option, String argument, int value) {
      super(argument, value);
      this.option = option;
    }

    public char getOption() {
      return option;
    }
  }

  public static class LongOption extends Option {
    private String option;

    public LongOption(String option, String argument, int value) {
      super(argument, value);
      this.option = option;
    }

    public String getOption() {
      return option;
    }
  }

  public static class InvalidOption extends Option {
    private String error;

    public InvalidOption(String error) {
      super(null, -1);
      this.error = error;
    }

    public String getError() {
      return error;
    }
  }

  public void addOption(Option option) {
    options.add(option);
  }

  public void addShortOption(char shortOption, int value) {
    addShortOption(shortOption, null, value);
  }

  public void addShortOption(char shortOption, String argument, int value) {
    addOption(new ShortOption(shortOption, argument, value));
  }

  public void addLongOption(String longOption, int value) {
    addLongOption(longOption, null, value);
  }

  public void addLongOption(String longOption, String argument, int value) {
    addOption(new LongOption(longOption, argument, value));
  }

  public void addInvalidOption(String error) {
    addOption(new InvalidOption(error));
  }

  public void addOperand(String operand) {
    operands.add(operand);
  }

  public List<Option> getOptions() {
    return List.copyOf(options);
  }

  public List<String> getOperands() {
    return List.copyOf(operands);
  }
}

package com.foreseeti.corelib;

public enum DefaultValue {
  True(true),
  False(false);

  private final boolean value;

  DefaultValue(boolean value) {
    this.value = value;
  }

  public boolean get() {
    return value;
  }
}

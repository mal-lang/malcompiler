package com.foreseeti.simulator;

import com.foreseeti.corelib.Link;

public enum BaseLangLink implements Link {
  Attacker_AttackStep("Attacks");

  protected final String name;

  BaseLangLink(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}

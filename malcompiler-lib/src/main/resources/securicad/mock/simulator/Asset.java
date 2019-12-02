package com.foreseeti.simulator;

import java.util.Set;

public interface Asset {
  public abstract Set<AttackStep> getAttackSteps();

  public abstract Set<Defense> getDefenses();

  public void fillElementMap();

  public default void clearGraphCache() {}
}

package com.foreseeti.simulator;

import com.foreseeti.corelib.DefaultValue;
import com.foreseeti.corelib.ModelElement;
import com.foreseeti.corelib.math.FDistribution;

public abstract class Defense implements ModelElement {
  public AttackStep disable;

  public Defense() {}

  public Defense(Boolean b) {}

  public Defense(DefaultValue val) {}

  public Defense(Defense other) {}

  public boolean isEnabled(ConcreteSample sample) {
    return false;
  }

  @Override
  public void setEvidenceDistribution(FDistribution<?> evidence) {}
}

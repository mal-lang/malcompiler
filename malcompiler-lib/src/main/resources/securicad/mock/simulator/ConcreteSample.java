package com.foreseeti.simulator;

import com.foreseeti.corelib.AbstractSample;
import com.foreseeti.corelib.ModelElement;
import java.util.List;

public class ConcreteSample extends AbstractSample {
  public void addExpectedParent(AttackStep as, AttackStep expectedParent) {}

  @Override
  public List<ModelElement> getAttackSteps() {
    return List.of();
  }
}

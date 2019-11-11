package com.foreseeti.simulator;

import com.foreseeti.corelib.BaseSample;
import com.foreseeti.corelib.FAnnotations.TypeName;
import com.foreseeti.corelib.FClass;
import com.foreseeti.corelib.ModelElement;
import com.foreseeti.corelib.math.FDistribution;
import java.util.Set;

@TypeName(name = "AttackStep")
public abstract class AttackStep extends FClass implements ModelElement {
  public AttackStep() {}

  public AttackStep(AttackStep other) {}

  @Override
  protected void registerAssociations() {}

  protected void setExpectedParents(ConcreteSample sample) {}

  public Set<AttackStep> getAttackStepChildren() {
    return Set.of();
  }

  public double defaultLocalTtc(BaseSample sample, AttackStep caller) {
    return 0.0;
  }

  public void clearGraphCache() {}

  @Override
  public void setEvidenceDistribution(FDistribution<?> evidence) {}

  public Defense getInfluencingDefense() {
    return null;
  }
}

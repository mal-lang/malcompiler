package core;

import java.util.Set;

public class AttackStepMax extends AttackStep {
  public AttackStepMax(String name) {
    super(name);
  }

  @Override
  public void updateTtc(AttackStep parent, double parentTtc, Set<AttackStep> activeAttackSteps) {
    expectedParents.remove(parent);
    visitedParents.add(parent);
    if (expectedParents.isEmpty()) {
      if (parentTtc + localTtc() < ttc) {
        ttc = parentTtc + localTtc();
        activeAttackSteps.add(this);
      }
    }
  }
}

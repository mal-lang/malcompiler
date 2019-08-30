package com.foreseeti.simulator;

import com.foreseeti.corelib.DefaultValue;
import com.foreseeti.corelib.FAnnotations.Association;
import com.foreseeti.corelib.FAnnotations.Display;
import com.foreseeti.corelib.FAnnotations.TypeName;
import com.foreseeti.corelib.FClass;
import com.foreseeti.corelib.util.FProbSet;
import java.util.Set;

public abstract class AbstractAttacker extends SingleParentAsset {
  @Association(index = 1, name = "firstSteps")
  public FProbSet<AttackStep> firstSteps;

  @Display
  @Association(index = 2, name = "entryPoint")
  public EntryPoint entryPoint = new EntryPoint();

  public AbstractAttacker() {}

  public AbstractAttacker(DefaultValue val) {}

  public AbstractAttacker(AbstractAttacker other) {}

  @TypeName(name = "EntryPoint")
  public class EntryPoint extends AttackStepMin {
    public EntryPoint() {}

    public EntryPoint(EntryPoint other) {
      super(other);
    }

    @Override
    public FClass getContainerFClass() {
      return AbstractAttacker.this;
    }

    @Override
    public Set<AttackStep> getAttackStepChildren() {
      return FClass.toSampleSet(((AbstractAttacker) getContainerFClass()).firstSteps, null);
    }
  }

  @Override
  public boolean areAssociationsPublic() {
    return false;
  }

  @Override
  public boolean areModelElementsPublic() {
    return false;
  }

  @Override
  public boolean isAttacker() {
    return true;
  }
}

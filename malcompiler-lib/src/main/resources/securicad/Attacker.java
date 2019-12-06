import com.foreseeti.corelib.AssociationManager;
import com.foreseeti.corelib.DefaultValue;
import com.foreseeti.corelib.FAnnotations.DisplayClass;
import com.foreseeti.corelib.FAnnotations.TypeName;
import com.foreseeti.corelib.FClass;
import com.foreseeti.corelib.util.FProbSet;
import com.foreseeti.simulator.AbstractAttacker;
import com.foreseeti.simulator.AttackStep;
import com.foreseeti.simulator.BaseLangLink;
import com.foreseeti.simulator.Defense;
import java.util.Set;

@DisplayClass(supportCapexOpex = false, category = "Attacker")
@TypeName(name = "Attacker")
public class Attacker extends AbstractAttacker {
  public Attacker() {
    this(DefaultValue.False);
  }

  public Attacker(DefaultValue val) {
    firstSteps = new FProbSet<>();
    fillElementMap();
  }

  public Attacker(Attacker other) {
    super(other);
    firstSteps = new FProbSet<>();
    entryPoint = new EntryPoint(other.entryPoint);
    fillElementMap();
  }

  @Override
  public String getConnectionValidationErrors(
      String sourceFieldName, FClass target, String targetFieldName) {
    if (Attacker.class.isAssignableFrom(target.getClass())) {
      return "Attacker can not be connected to other Attackers";
    }
    return getConnectionValidationErrors(target.getClass());
  }

  @Override
  public void registerAssociations() {
    AssociationManager.addSupportedAssociationMultiple(
        this.getClass(),
        getName(1),
        AttackStep.class,
        0,
        AssociationManager.NO_LIMIT,
        BaseLangLink.Attacker_AttackStep);
  }

  @Override
  public Set<AttackStep> getAttackSteps() {
    return Set.of(entryPoint);
  }

  @Override
  public Set<Defense> getDefenses() {
    return Set.of();
  }

  @TypeName(name = "EntryPoint")
  public class EntryPoint extends AbstractAttacker.EntryPoint {
    public EntryPoint() {}

    public EntryPoint(AbstractAttacker.EntryPoint other) {
      super(other);
    }

    @Override
    public FClass getContainerFClass() {
      return Attacker.this;
    }

    @Override
    public java.util.Set<AttackStep> getAttackStepChildren() {
      return FClass.toSampleSet(((Attacker) getContainerFClass()).firstSteps, null);
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

package com.foreseeti.corelib;

import com.foreseeti.corelib.util.FProb;
import com.foreseeti.corelib.util.FProbSet;
import java.util.Set;

public abstract class FClass {
  public FClass() {}

  public FClass(FClass other) {}

  public Set<ModelElement> getTTCColoringElements() {
    return Set.of();
  }

  public static <T extends FClass> T toSample(FProb<T> probObj, BaseSample sample) {
    return null;
  }

  public static <T extends FClass> Set<T> toSampleSet(FProbSet<T> probSet, BaseSample sample) {
    return Set.of();
  }

  protected String getName(int index) {
    return null;
  }

  protected static String getName(Class<? extends FClass> clazz, int index) {
    return null;
  }

  public String getDescription() {
    return "";
  }

  public String getConnectionValidationErrors(
      String sourceFieldName, FClass target, String targetFieldName) {
    return null;
  }

  public String getConnectionValidationErrors(Class<? extends FClass> type) {
    return null;
  }

  protected abstract void registerAssociations();

  public boolean areAssociationsPublic() {
    return true;
  }

  public boolean areModelElementsPublic() {
    return true;
  }

  public boolean isAttacker() {
    return false;
  }
}

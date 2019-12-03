package com.foreseeti.corelib.util;

import com.foreseeti.corelib.FClass;
import java.util.HashSet;
import java.util.Set;

public class FProbSet<T extends FClass> extends HashSet<FProb<T>> {
  public Set<T> getNonSampled() {
    return null;
  }
}

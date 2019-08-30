package com.foreseeti.corelib.math;

public class FBernoulliDistribution implements FDistribution<Boolean> {
  public static FBernoulliDistribution getDist(double p) {
    return new FBernoulliDistribution();
  }

  public Boolean sample() {
    return Boolean.FALSE;
  }
}

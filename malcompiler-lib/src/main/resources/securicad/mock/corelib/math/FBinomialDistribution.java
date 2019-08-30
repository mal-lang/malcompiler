package com.foreseeti.corelib.math;

public class FBinomialDistribution implements FDistribution<Integer> {
  public static FBinomialDistribution getDist(int trials, double p) {
    return new FBinomialDistribution();
  }

  public Integer sample() {
    return Integer.valueOf(0);
  }
}

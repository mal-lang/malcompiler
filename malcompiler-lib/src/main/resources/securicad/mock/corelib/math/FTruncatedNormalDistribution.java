package com.foreseeti.corelib.math;

public class FTruncatedNormalDistribution implements FDistribution<Double> {
  public static FDistribution<Double> getDist(double m, double sd) {
    return new FTruncatedNormalDistribution();
  }

  public Double sample() {
    return Double.valueOf(0.0);
  }
}

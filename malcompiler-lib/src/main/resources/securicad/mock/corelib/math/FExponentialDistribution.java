package com.foreseeti.corelib.math;

public class FExponentialDistribution implements FDistribution<Double> {
  public static FDistribution<Double> getDist(double m) {
    return new FExponentialDistribution();
  }

  public Double sample() {
    return Double.valueOf(0.0);
  }
}

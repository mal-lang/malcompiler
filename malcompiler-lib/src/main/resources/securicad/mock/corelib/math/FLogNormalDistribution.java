package com.foreseeti.corelib.math;

public class FLogNormalDistribution implements FDistribution<Double> {
  public static FLogNormalDistribution getDist(double shape, double scale) {
    return new FLogNormalDistribution();
  }

  public Double sample() {
    return Double.valueOf(0.0);
  }
}

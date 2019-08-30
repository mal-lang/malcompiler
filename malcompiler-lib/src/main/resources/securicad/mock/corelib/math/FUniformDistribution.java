package com.foreseeti.corelib.math;

public class FUniformDistribution implements FDistribution<Double> {
  public static FUniformDistribution getDist(double lower, double upper) {
    return new FUniformDistribution();
  }

  public Double sample() {
    return Double.valueOf(0.0);
  }
}

package com.foreseeti.corelib.math;

public class FParetoDistribution implements FDistribution<Double> {
  public static FParetoDistribution getDist(double shape, double scale) {
    return new FParetoDistribution();
  }

  public Double sample() {
    return Double.valueOf(0.0);
  }
}

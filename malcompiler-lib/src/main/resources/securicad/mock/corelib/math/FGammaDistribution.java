package com.foreseeti.corelib.math;

public class FGammaDistribution implements FDistribution<Double> {
  public static FGammaDistribution getDist(double shape, double scale) {
    return new FGammaDistribution();
  }

  public Double sample() {
    return Double.valueOf(0.0);
  }
}

package com.foreseeti.corelib.math;

public abstract class FMath {
  public static FDistribution<Boolean> getBernoulliDist(double p) {
    return FBernoulliDistribution.getDist(p);
  }

  public static FDistribution<Integer> getBinomialDist(int trials, double p) {
    return FBinomialDistribution.getDist(trials, p);
  }

  public static FDistribution<Double> getExponentialDist(double m) {
    return FExponentialDistribution.getDist(m);
  }

  public static FDistribution<Double> getGammaDist(double shape, double scale) {
    return FGammaDistribution.getDist(shape, scale);
  }

  public static FDistribution<Double> getLogNormalDist(double shape, double scale) {
    return FLogNormalDistribution.getDist(shape, scale);
  }

  public static FDistribution<Double> getParetoDist(double scale, double shape) {
    return FParetoDistribution.getDist(scale, shape);
  }

  public static FDistribution<Double> getTruncatedNormalDist(double m, double sd) {
    return FTruncatedNormalDistribution.getDist(m, sd);
  }

  public static FDistribution<Double> getUniformDist(double lower, double upper) {
    return FUniformDistribution.getDist(lower, upper);
  }
}

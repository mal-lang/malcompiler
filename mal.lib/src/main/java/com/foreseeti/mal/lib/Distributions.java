/*
 * Copyright 2019 Foreseeti AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreseeti.mal.lib;

import java.util.List;

public class Distributions {

  public static void validate(String name, List<Double> params) throws CompilerException {
    switch (name) {
      case "Bernoulli":
        Bernoulli.validate(params);
        break;
      case "Binomial":
        Binomial.validate(params);
        break;
      case "Exponential":
        Exponential.validate(params);
        break;
      case "Gamma":
        Gamma.validate(params);
        break;
      case "LogNormal":
        LogNormal.validate(params);
        break;
      case "Pareto":
        Pareto.validate(params);
        break;
      case "TruncatedNormal":
        TruncatedNormal.validate(params);
        break;
      case "Uniform":
        Uniform.validate(params);
        break;
      case "EasyAndCertain":
      case "EasyButUncertain":
      case "HardButCertain":
      case "HardAndUncertain":
      case "VeryHardButCertain":
      case "VeryHardAndUncertain":
      case "Infinity":
      case "Zero":
      case "Enabled":
      case "Disabled":
        Combination.validate(params);
        break;
      default:
        throw new CompilerException(String.format("Distribution '%s' is not supported", name));
    }
  }

  public static Distribution getDistribution(String name, List<Double> params) {
    switch (name) {
      case "Bernoulli":
        return new Bernoulli(params);
      case "Binomial":
        return new Binomial(params);
      case "Exponential":
        return new Exponential(params);
      case "Gamma":
        return new Gamma(params);
      case "LogNormal":
        return new LogNormal(params);
      case "Pareto":
        return new Pareto(params);
      case "TruncatedNormal":
        return new TruncatedNormal(params);
      case "Uniform":
        return new Uniform(params);
      case "EasyAndCertain":
        return new EasyAndCertain();
      case "EasyButUncertain":
        return new EasyButUncertain();
      case "HardButCertain":
        return new HardButCertain();
      case "HardAndUncertain":
        return new HardAndUncertain();
      case "VeryHardButCertain":
        return new VeryHardButCertain();
      case "VeryHardAndUncertain":
        return new VeryHardAndUncertain();
      case "Infinity":
        return new Infinity();
      case "Zero":
        return new Zero();
      case "Enabled":
        return new Enabled();
      case "Disabled":
        return new Disabled();
      default:
        throw new RuntimeException(String.format("Distribution '%s' is not supported", name));
    }
  }

  public interface Distribution {
    double getMean();
  }

  public static class Bernoulli implements Distribution {
    public final double probability;

    public Bernoulli(double probability) {
      this.probability = probability;
    }

    public Bernoulli(List<Double> params) {
      this(params.get(0));
    }

    public static void validate(List<Double> params) throws CompilerException {
      if (params == null || params.size() != 1) {
        throw new CompilerException(
            "Expected exactly one parameter (probability), for Bernoulli distribution");
      } else if (params.get(0) > 1) {
        throw new CompilerException(
            String.format(
                "%s is not in valid range '0 <= probability <= 1', for Bernoulli distribution",
                params.get(0)));
      }
    }

    @Override
    public double getMean() {
      return probability;
    }

    @Override
    public String toString() {
      return String.format("Bernoulli(%f)", probability);
    }
  }

  public static class Binomial implements Distribution {
    public final int trials;
    public final double probability;

    public Binomial(int trials, double probability) {
      this.trials = trials;
      this.probability = probability;
    }

    public Binomial(List<Double> params) {
      this((int) Math.round(params.get(0)), params.get(1));
    }

    public static void validate(List<Double> params) throws CompilerException {
      if (params == null || params.size() != 2) {
        throw new CompilerException(
            "Expected exactly two parameters (trials, probability), for Binomial distribution");
      } else if (params.get(1) > 1) {
        throw new CompilerException(
            String.format(
                "%s is not in valid range '0 <= probability <= 1', for Binomial distribution",
                params.get(1)));
      }
    }

    @Override
    public double getMean() {
      return trials * probability;
    }

    @Override
    public String toString() {
      return String.format("Binomial(%d, %f)", trials, probability);
    }
  }

  public static class Exponential implements Distribution {
    public final double lambda;

    public Exponential(double lambda) {
      this.lambda = lambda;
    }

    public Exponential(List<Double> params) {
      this(params.get(0));
    }

    public static void validate(List<Double> params) throws CompilerException {
      if (params == null || params.size() != 1) {
        throw new CompilerException(
            "Expected exactly one parameter (lambda), for Exponential distribution");
      } else if (params.get(0) <= 0) {
        throw new CompilerException(
            String.format(
                "%s is not in valid range 'lambda > 0', for Exponential distribution",
                params.get(0)));
      }
    }

    @Override
    public double getMean() {
      return 1 / lambda;
    }

    @Override
    public String toString() {
      return String.format("Exponential(%f)", lambda);
    }
  }

  public static class Gamma implements Distribution {
    public final double shape;
    public final double scale;

    public Gamma(double shape, double scale) {
      this.shape = shape;
      this.scale = scale;
    }

    public Gamma(List<Double> params) {
      this(params.get(0), params.get(1));
    }

    public static void validate(List<Double> params) throws CompilerException {
      if (params == null || params.size() != 2) {
        throw new CompilerException(
            "Expected exactly two parameters (shape, scale), for Gamma distribution");
      } else if (params.get(0) <= 0) {
        throw new CompilerException(
            String.format(
                "%s is not in valid range 'shape > 0', for Gamma distribution", params.get(0)));
      } else if (params.get(1) <= 0) {
        throw new CompilerException(
            String.format(
                "%s is not in valid range 'scale > 0', for Gamma distribution", params.get(1)));
      }
    }

    @Override
    public double getMean() {
      return shape * scale;
    }

    @Override
    public String toString() {
      return String.format("Gamma(%f, %f)", shape, scale);
    }
  }

  public static class LogNormal implements Distribution {
    public final double mean;
    public final double standardDeviation;

    public LogNormal(double mean, double standardDeviation) {
      this.mean = mean;
      this.standardDeviation = standardDeviation;
    }

    public LogNormal(List<Double> params) {
      this(params.get(0), params.get(1));
    }

    public static void validate(List<Double> params) throws CompilerException {
      if (params == null || params.size() != 2) {
        throw new CompilerException(
            "Expected exactly two parameters (mean, standardDeviation), for LogNormal distribution");
      } else if (params.get(1) <= 0) {
        throw new CompilerException(
            String.format(
                "%s is not in valid range 'standardDeviation > 0', for LogNormal distribution",
                params.get(1)));
      }
    }

    @Override
    public double getMean() {
      return Math.exp(mean + Math.pow(standardDeviation, 2) / 2);
    }

    @Override
    public String toString() {
      return String.format("LogNormal(%f, %f)", mean, standardDeviation);
    }
  }

  public static class Pareto implements Distribution {
    public final double min;
    public final double shape;

    public Pareto(double min, double shape) {
      this.min = min;
      this.shape = shape;
    }

    public Pareto(List<Double> params) {
      this(params.get(0), params.get(1));
    }

    public static void validate(List<Double> params) throws CompilerException {
      if (params == null || params.size() != 2) {
        throw new CompilerException(
            "Expected exactly two parameters (min, shape), for Pareto distribution");
      } else if (params.get(0) <= 0) {
        throw new CompilerException(
            String.format(
                "%s is not in valid range 'min > 0', for Pareto distribution", params.get(0)));
      } else if (params.get(1) <= 0) {
        throw new CompilerException(
            String.format(
                "%s is not in valid range 'shape > 0', for Pareto distribution", params.get(1)));
      }
    }

    @Override
    public double getMean() {
      if (min <= 1) {
        return Double.MAX_VALUE;
      } else {
        return (min * shape) / (min - 1);
      }
    }

    @Override
    public String toString() {
      return String.format("Pareto(%f, %f)", min, shape);
    }
  }

  public static class TruncatedNormal implements Distribution {
    public final double mean;
    public final double standardDeviation;

    public TruncatedNormal(double mean, double standardDeviation) {
      this.mean = mean;
      this.standardDeviation = standardDeviation;
    }

    public TruncatedNormal(List<Double> params) {
      this(params.get(0), params.get(1));
    }

    public static void validate(List<Double> params) throws CompilerException {
      if (params == null || params.size() != 2) {
        throw new CompilerException(
            "Expected exactly two parameters (mean, standardDeviation), for TruncatedNormal distribution");
      } else if (params.get(1) <= 0) {
        throw new CompilerException(
            String.format(
                "%s is not in valid range 'standardDeviation > 0', for TruncatedNormal distribution",
                params.get(1)));
      }
    }

    @Override
    public double getMean() {
      return mean;
    }

    @Override
    public String toString() {
      return String.format("TruncatedNormal(%f, %f)", mean, standardDeviation);
    }
  }

  public static class Uniform implements Distribution {
    public final double min;
    public final double max;

    public Uniform(double min, double max) {
      this.min = min;
      this.max = max;
    }

    public Uniform(List<Double> params) {
      this(params.get(0), params.get(1));
    }

    public static void validate(List<Double> params) throws CompilerException {
      if (params == null || params.size() != 2) {
        throw new CompilerException(
            "Expected exactly two parameters (min, max), for Uniform distribution");
      } else if (params.get(0) > params.get(1)) {
        throw new CompilerException(
            String.format(
                "(%s, %s) does not meet requirement 'min <= max', for Uniform distribution",
                params.get(0), params.get(1)));
      }
    }

    @Override
    public double getMean() {
      return (min + max) / 2;
    }

    @Override
    public String toString() {
      return String.format("Uniform(%f, %f)", min, max);
    }
  }

  /*Custom, combinations of above defined distributions*/

  private abstract static class Combination implements Distribution {
    public static void validate(List<Double> params) throws CompilerException {
      if (params != null && params.size() != 0) {
        throw new CompilerException(
            String.format("Expected exactly zero parameters, for combination distributions"));
      }
    }
  }

  public static class EasyAndCertain extends Combination {
    private static Exponential exponential = new Exponential(1);

    @Override
    public double getMean() {
      return exponential.getMean();
    }

    @Override
    public String toString() {
      return "EasyAndCertain";
    }
  }

  public static class EasyButUncertain extends Combination {
    private static Bernoulli bernoulli = new Bernoulli(0.5);

    @Override
    public double getMean() {
      return bernoulli.getMean();
    }

    @Override
    public String toString() {
      return "EasyButUncertain";
    }
  }

  public static class HardButCertain extends Combination {
    private static Exponential exponential = new Exponential(0.1);

    @Override
    public double getMean() {
      return exponential.getMean();
    }

    @Override
    public String toString() {
      return "HardButCertain";
    }
  }

  public static class HardAndUncertain extends Combination {
    private static Bernoulli bernoulli = new Bernoulli(0.5);
    private static Exponential exponential = new Exponential(0.1);

    @Override
    public double getMean() {
      return bernoulli.getMean() * exponential.getMean();
    }

    @Override
    public String toString() {
      return "HardAndUncertain";
    }
  }

  public static class VeryHardButCertain extends Combination {
    private static Exponential exponential = new Exponential(0.01);

    @Override
    public double getMean() {
      return exponential.getMean();
    }

    @Override
    public String toString() {
      return "VeryHardButCertain";
    }
  }

  public static class VeryHardAndUncertain extends Combination {
    private static Bernoulli bernoulli = new Bernoulli(0.5);
    private static Exponential exponential = new Exponential(0.01);

    @Override
    public double getMean() {
      return bernoulli.getMean() * exponential.getMean();
    }

    @Override
    public String toString() {
      return "VeryHardAndUncertain";
    }
  }

  public static class Infinity extends Combination {
    public Infinity() {}

    @Override
    public double getMean() {
      return Double.MAX_VALUE;
    }

    @Override
    public String toString() {
      return "Infinity";
    }
  }

  public static class Zero extends Combination {
    @Override
    public double getMean() {
      return 0;
    }

    @Override
    public String toString() {
      return "Zero";
    }
  }

  public static class Enabled extends Combination {
    @Override
    public double getMean() {
      return 1;
    }

    @Override
    public String toString() {
      return "Enabled";
    }
  }

  public static class Disabled extends Combination {
    @Override
    public double getMean() {
      return 0;
    }

    @Override
    public String toString() {
      return "Disabled";
    }
  }
}

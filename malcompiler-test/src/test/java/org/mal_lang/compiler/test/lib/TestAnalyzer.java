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
package org.mal_lang.compiler.test.lib;

import static org.mal_lang.compiler.test.lib.AssertAST.assertAnalyzeClassPath;
import static org.mal_lang.compiler.test.lib.AssertAST.assertAnalyzeClassPathError;

import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.test.MalTest;

public class TestAnalyzer extends MalTest {
  @Test
  public void testBad1() {
    assertAnalyzeClassPathError("analyzer/bad1.mal");
    assertEmptyOut();
    String[] expected = {
      "[ANALYZER ERROR] Missing required define '#id: \"\"'",
      "[ANALYZER ERROR] Missing required define '#version: \"\"'",
      "[ANALYZER ERROR] <bad1.mal:4:1> Define 'custom' previously defined at <bad1.mal:3:1>",
      "[ANALYZER WARNING] <bad1.mal:5:10> Category 'emp' contains no assets or metadata",
      "[ANALYZER ERROR] <bad1.mal:11:3> Metadata 'info' previously defined at <bad1.mal:10:3>",
      "[ANALYZER ERROR] <bad1.mal:12:3> Metadata 'rationale' previously defined at <bad1.mal:7:3>",
      "[ANALYZER ERROR] <bad1.mal:16:5> Metadata 'rationale' previously defined at <bad1.mal:15:5>",
      "[ANALYZER ERROR] <bad1.mal:19:8> Last step is not attack step",
      "[ANALYZER ERROR] <bad1.mal:21:7> Attack step 'compromise' previously defined at <bad1.mal:18:7>",
      "[ANALYZER ERROR] <bad1.mal:21:18> Require '<-' may only be defined for attack step type exist 'E' or not-exist '!E'",
      "[ANALYZER ERROR] <bad1.mal:23:8> Types '_C' and '_A' have no common ancestor",
      "[ANALYZER ERROR] <bad1.mal:24:7> Asset '_C' cannot be of type '_A'",
      "[ANALYZER WARNING] <bad1.mal:26:18> Asset 'AA' is abstract but never extended to",
      "[ANALYZER ERROR] <bad1.mal:27:7> Cannot override attack step 'compromise' previously defined at <bad1.mal:18:7> with different type 'ALL' =/= 'ANY'",
      "[ANALYZER ERROR] <bad1.mal:28:14> Cannot inherit attack step 'access' without previous definition",
      "[ANALYZER ERROR] <bad1.mal:29:7> Attack step 'authorize' not defined for asset 'AA'",
      "[ANALYZER ERROR] <bad1.mal:30:7> Field 'b' not defined for asset 'AA'",
      "[ANALYZER ERROR] <bad1.mal:32:9> Asset 'AA' previously defined at <bad1.mal:26:18>",
      "[ANALYZER ERROR] <bad1.mal:35:9> Variable 'var1' previously defined at <bad1.mal:34:9>",
      "[ANALYZER WARNING] <bad1.mal:37:7> Attack step _C.compromise contains duplicate classification {C}",
      "[ANALYZER ERROR] <bad1.mal:38:7> Attack step 'var1' not defined for asset '_C'",
      "[ANALYZER ERROR] <bad1.mal:39:7> Previous asset '_C' is not of type '_A'",
      "[ANALYZER ERROR] <bad1.mal:40:7> Defenses cannot have CIA classifications",
      "[ANALYZER ERROR] <bad1.mal:46:7> Field _A.a previously defined for asset at <bad1.mal:46:26>",
      "[ANALYZER ERROR] <bad1.mal:48:6> Metadata 'assumptions' previously defined at <bad1.mal:47:6>",
      ""
    };
    assertErrLines(expected);
  }

  @Test
  public void testBad2() {
    assertAnalyzeClassPathError("analyzer/bad2.mal");
    assertEmptyOut();
    String[] expected = {
      "[ANALYZER ERROR] <bad2.mal:1:1> Define 'id' cannot be empty",
      "[ANALYZER ERROR] <bad2.mal:2:1> Define 'version' must be valid semantic versioning without pre-release identifier and build metadata",
      "[ANALYZER ERROR] <bad2.mal:4:9> Asset '_A' extends in loop '_A -> _B -> _C -> _D -> _A'",
      "[ANALYZER ERROR] <bad2.mal:5:9> Asset '_B' extends in loop '_B -> _C -> _D -> _A -> _B'",
      "[ANALYZER ERROR] <bad2.mal:6:9> Asset '_C' extends in loop '_C -> _D -> _A -> _B -> _C'",
      "[ANALYZER ERROR] <bad2.mal:7:9> Asset '_D' extends in loop '_D -> _A -> _B -> _C -> _D'",
      ""
    };
    assertErrLines(expected);
  }

  @Test
  public void testBad3() {
    assertAnalyzeClassPathError("analyzer/bad3.mal");
    assertEmptyOut();
    String[] expected = {
      "[ANALYZER ERROR] <bad3.mal:10:9> Variable 'VAR' previously defined at <bad3.mal:5:8>",
      "[ANALYZER ERROR] <bad3.mal:16:10> Field 'VAR1' not defined for asset 'Bravo', did you mean the variable 'VAR1()' defined at <bad3.mal:14:5>",
      ""
    };
    assertErrLines(expected);
  }

  @Test
  public void testComplex() {
    assertAnalyzeClassPath("analyzer/complex.mal");
    assertEmptyOut();
    String[] expected = {
      "[ANALYZER WARNING] <complex.mal:69:3> Association 'Computer [studentComputer] <-- Use --> Student [student]' is never used",
      "[ANALYZER WARNING] <complex.mal:70:3> Association 'Computer [teacherComputer] <-- Use --> Teacher [teacher]' is never used",
      ""
    };
    assertErrLines(expected);
  }

  @Test
  public void testDistributions() {
    assertAnalyzeClassPathError("analyzer/distributions.mal");
    assertEmptyOut();
    String[] expected = {
      "[ANALYZER ERROR] <distributions.mal:6:7> Expected exactly one parameter (probability), for Bernoulli distribution",
      "[ANALYZER ERROR] <distributions.mal:7:7> Expected exactly one parameter (probability), for Bernoulli distribution",
      "[ANALYZER ERROR] <distributions.mal:8:7> Expected exactly one parameter (probability), for Bernoulli distribution",
      "[ANALYZER ERROR] <distributions.mal:9:7> 1.1 is not in valid range '0 <= probability <= 1', for Bernoulli distribution",
      "[ANALYZER ERROR] <distributions.mal:13:7> Expected exactly two parameters (trials, probability), for Binomial distribution",
      "[ANALYZER ERROR] <distributions.mal:14:7> Expected exactly two parameters (trials, probability), for Binomial distribution",
      "[ANALYZER ERROR] <distributions.mal:15:7> Expected exactly two parameters (trials, probability), for Binomial distribution",
      "[ANALYZER ERROR] <distributions.mal:16:7> Expected exactly two parameters (trials, probability), for Binomial distribution",
      "[ANALYZER ERROR] <distributions.mal:17:7> 1.1 is not in valid range '0 <= probability <= 1', for Binomial distribution",
      "[ANALYZER ERROR] <distributions.mal:21:7> Expected exactly one parameter (lambda), for Exponential distribution",
      "[ANALYZER ERROR] <distributions.mal:22:7> Expected exactly one parameter (lambda), for Exponential distribution",
      "[ANALYZER ERROR] <distributions.mal:23:7> Expected exactly one parameter (lambda), for Exponential distribution",
      "[ANALYZER ERROR] <distributions.mal:24:7> 0.0 is not in valid range 'lambda > 0', for Exponential distribution",
      "[ANALYZER ERROR] <distributions.mal:28:7> Expected exactly two parameters (shape, scale), for Gamma distribution",
      "[ANALYZER ERROR] <distributions.mal:29:7> Expected exactly two parameters (shape, scale), for Gamma distribution",
      "[ANALYZER ERROR] <distributions.mal:30:7> Expected exactly two parameters (shape, scale), for Gamma distribution",
      "[ANALYZER ERROR] <distributions.mal:31:7> Expected exactly two parameters (shape, scale), for Gamma distribution",
      "[ANALYZER ERROR] <distributions.mal:32:7> 0.0 is not in valid range 'shape > 0', for Gamma distribution",
      "[ANALYZER ERROR] <distributions.mal:33:7> 0.0 is not in valid range 'scale > 0', for Gamma distribution",
      "[ANALYZER ERROR] <distributions.mal:37:7> Expected exactly two parameters (mean, standardDeviation), for LogNormal distribution",
      "[ANALYZER ERROR] <distributions.mal:38:7> Expected exactly two parameters (mean, standardDeviation), for LogNormal distribution",
      "[ANALYZER ERROR] <distributions.mal:39:7> Expected exactly two parameters (mean, standardDeviation), for LogNormal distribution",
      "[ANALYZER ERROR] <distributions.mal:40:7> Expected exactly two parameters (mean, standardDeviation), for LogNormal distribution",
      "[ANALYZER ERROR] <distributions.mal:41:7> 0.0 is not in valid range 'standardDeviation > 0', for LogNormal distribution",
      "[ANALYZER ERROR] <distributions.mal:45:7> Expected exactly two parameters (min, shape), for Pareto distribution",
      "[ANALYZER ERROR] <distributions.mal:46:7> Expected exactly two parameters (min, shape), for Pareto distribution",
      "[ANALYZER ERROR] <distributions.mal:47:7> Expected exactly two parameters (min, shape), for Pareto distribution",
      "[ANALYZER ERROR] <distributions.mal:48:7> Expected exactly two parameters (min, shape), for Pareto distribution",
      "[ANALYZER ERROR] <distributions.mal:49:7> 0.0 is not in valid range 'min > 0', for Pareto distribution",
      "[ANALYZER ERROR] <distributions.mal:50:7> 0.0 is not in valid range 'shape > 0', for Pareto distribution",
      "[ANALYZER ERROR] <distributions.mal:54:7> Expected exactly two parameters (mean, standardDeviation), for TruncatedNormal distribution",
      "[ANALYZER ERROR] <distributions.mal:55:7> Expected exactly two parameters (mean, standardDeviation), for TruncatedNormal distribution",
      "[ANALYZER ERROR] <distributions.mal:56:7> Expected exactly two parameters (mean, standardDeviation), for TruncatedNormal distribution",
      "[ANALYZER ERROR] <distributions.mal:57:7> Expected exactly two parameters (mean, standardDeviation), for TruncatedNormal distribution",
      "[ANALYZER ERROR] <distributions.mal:58:7> 0.0 is not in valid range 'standardDeviation > 0', for TruncatedNormal distribution",
      "[ANALYZER ERROR] <distributions.mal:62:7> Expected exactly two parameters (min, max), for Uniform distribution",
      "[ANALYZER ERROR] <distributions.mal:63:7> Expected exactly two parameters (min, max), for Uniform distribution",
      "[ANALYZER ERROR] <distributions.mal:64:7> Expected exactly two parameters (min, max), for Uniform distribution",
      "[ANALYZER ERROR] <distributions.mal:65:7> Expected exactly two parameters (min, max), for Uniform distribution",
      "[ANALYZER ERROR] <distributions.mal:66:7> (1.0, 0.0) does not meet requirement 'min <= max', for Uniform distribution",
      "[ANALYZER ERROR] <distributions.mal:70:7> Expected exactly zero parameters, for combination distributions",
      "[ANALYZER ERROR] <distributions.mal:75:7> Expected exactly zero parameters, for combination distributions",
      "[ANALYZER ERROR] <distributions.mal:80:7> Expected exactly zero parameters, for combination distributions",
      "[ANALYZER ERROR] <distributions.mal:85:7> Expected exactly zero parameters, for combination distributions",
      "[ANALYZER ERROR] <distributions.mal:90:7> Expected exactly zero parameters, for combination distributions",
      "[ANALYZER ERROR] <distributions.mal:95:7> Expected exactly zero parameters, for combination distributions",
      "[ANALYZER ERROR] <distributions.mal:100:7> Expected exactly zero parameters, for combination distributions",
      "[ANALYZER ERROR] <distributions.mal:105:7> Expected exactly zero parameters, for combination distributions",
      "[ANALYZER ERROR] <distributions.mal:109:5> Defense Distribution.enabled1 may not have advanced TTC expressions",
      "[ANALYZER ERROR] <distributions.mal:110:17> Expected exactly zero parameters, for combination distributions",
      "[ANALYZER ERROR] <distributions.mal:113:5> Defense Distribution.disabled1 may not have advanced TTC expressions",
      "[ANALYZER ERROR] <distributions.mal:114:18> Expected exactly zero parameters, for combination distributions",
      "[ANALYZER ERROR] <distributions.mal:118:5> Defense Distribution.nobern may only have 'Enabled', 'Disabled', or 'Bernoulli(p)' as TTC",
      "[ANALYZER ERROR] <distributions.mal:119:13> Distributions 'Enabled' or 'Disabled' may not be used as TTC values in '&' and '|' attack steps",
      "[ANALYZER ERROR] <distributions.mal:120:14> Distributions 'Enabled' or 'Disabled' may not be used as TTC values in '&' and '|' attack steps",
      "[ANALYZER ERROR] <distributions.mal:121:18> Distribution 'BestTTC' is not supported",
      ""
    };
    assertErrLines(expected);
  }

  @Test
  public void testAssoc() {
    assertAnalyzeClassPathError("analyzer/invalid-assoc.mal");
    assertEmptyOut();
    String[] expected = {
      "[ANALYZER ERROR] <invalid-assoc.mal:13:48> Right asset 'Group' is not defined", ""
    };
    assertErrLines(expected);
  }

  @Test
  public void testDist() {
    assertAnalyzeClassPathError("analyzer/dist-fail.mal");
    assertEmptyOut();
    String[] expected = {
      "[ANALYZER ERROR] <dist-fail.mal:5:55> TTC distribution 'Bernoulli' is not available in subtraction, division or exponential expressions.",
      ""
    };
    assertErrLines(expected);
  }
}

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
package com.foreseeti.mal;

import static com.foreseeti.mal.AssertAST.assertAnalyzeClassPath;
import static com.foreseeti.mal.AssertAST.assertAnalyzeClassPathError;

import org.junit.jupiter.api.Test;

public class TestAnalyzer extends MalTest {
  @Test
  public void testBad1() {
    assertAnalyzeClassPathError("analyzer/bad1.mal");
    assertEmptyOut();
    String[] expected = {
      "[ANALYZER ERROR] <bad1.mal:4:1> Define 'custom' previously defined at <bad1.mal:3:1>",
      "[ANALYZER ERROR] Missing required define '#id: \"\"'",
      "[ANALYZER ERROR] Missing required define '#version: \"\"'",
      "[ANALYZER WARNING] <bad1.mal:5:10> Category 'emp' contains no assets or metadata",
      "[ANALYZER ERROR] <bad1.mal:35:9> Asset 'AA' previously defined at <bad1.mal:29:18>",
      "[ANALYZER ERROR] <bad1.mal:11:3> Metadata 'info' previously defined at <bad1.mal:10:3>",
      "[ANALYZER ERROR] <bad1.mal:12:3> Metadata 'rationale' previously defined at <bad1.mal:7:3>",
      "[ANALYZER ERROR] <bad1.mal:16:5> Metadata 'rationale' previously defined at <bad1.mal:15:5>",
      "[ANALYZER ERROR] <bad1.mal:52:6> Metadata 'assumptions' previously defined at <bad1.mal:51:6>",
      "[ANALYZER WARNING] <bad1.mal:29:18> Asset 'AA' is abstract but never extended to",
      "[ANALYZER ERROR] <bad1.mal:22:7> Attack step 'compromise' previously defined at <bad1.mal:18:7>",
      "[ANALYZER ERROR] <bad1.mal:30:7> Cannot override attack step 'compromise' previously defined at <bad1.mal:18:7> with different type 'ALL' =/= 'ANY'",
      "[ANALYZER ERROR] <bad1.mal:31:14> Cannot inherit attack step 'access' without previous definition",
      "[ANALYZER WARNING] <bad1.mal:39:7> Attack step _C.compromise contains duplicate classification {C}",
      "[ANALYZER ERROR] <bad1.mal:44:7> Defenses cannot have CIA classifications",
      "[ANALYZER ERROR] <bad1.mal:50:7> Field _A.a previously defined at <bad1.mal:50:26>",
      "[ANALYZER ERROR] <bad1.mal:20:8> Last step is not attack step",
      "[ANALYZER WARNING] <bad1.mal:21:7> Step 'c' defined as variable at <bad1.mal:19:11> and field at <bad1.mal:49:26>",
      "[ANALYZER ERROR] <bad1.mal:22:18> Require '<-' may only be defined for attack step type exist 'E' or not-exist '!E'",
      "[ANALYZER ERROR] <bad1.mal:25:8> Types '_C' and '_A' have no common ancestor",
      "[ANALYZER ERROR] <bad1.mal:26:7> Asset '_C' cannot be of type '_A'",
      "[ANALYZER WARNING] <bad1.mal:27:7> Step 'invalidate' defined as variable at <bad1.mal:24:11> and attack step at <bad1.mal:23:7>",
      "[ANALYZER ERROR] <bad1.mal:32:7> Attack step 'authorize' not defined for asset 'AA'",
      "[ANALYZER ERROR] <bad1.mal:33:7> Field 'b' not defined for asset 'AA'",
      "[ANALYZER ERROR] <bad1.mal:38:9> Variable 'var1' previously defined at <bad1.mal:37:9>",
      "[ANALYZER ERROR] <bad1.mal:41:11> Variable 'aaa' previously defined at <bad1.mal:40:11>",
      "[ANALYZER ERROR] <bad1.mal:37:9> Variable 'var1' contains cycle 'var1 -> var1'",
      "[ANALYZER ERROR] <bad1.mal:43:7> Previous asset '_C' is not of type '_A'",
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
  public void testScope() {
    assertAnalyzeClassPath("analyzer/scope.mal");
    assertEmptyOut();
    assertEmptyErr();
  }

  @Test
  public void testComplex() {
    assertAnalyzeClassPath("analyzer/complex.mal");
    assertEmptyOut();
    assertEmptyErr();
  }
}

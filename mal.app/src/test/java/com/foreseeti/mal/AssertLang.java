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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

public class AssertLang {
  // Prevent instantiation
  private AssertLang() {}

  private static String classToString(Class c) {
    if (c == null) {
      return "(null)";
    } else {
      return String.format("\"%s\"", c.getSimpleName());
    }
  }

  private static String assetToString(Lang.Asset asset) {
    if (asset == null) {
      return "(null)";
    } else {
      return String.format("\"%s\"", asset.getName());
    }
  }

  private static String fieldToString(Lang.Field field) {
    if (field == null) {
      return "(null)";
    } else {
      return String.format("\"%s\"", field.getName());
    }
  }

  private static String attackStepToString(Lang.AttackStep attackStep) {
    if (attackStep == null) {
      return "(null)";
    } else {
      return String.format("\"%s\"", attackStep.getName());
    }
  }

  private static void assertLangStepClass(Lang.StepExpr expected, Lang.StepExpr actual) {
    assertSame(
        expected.getClass(),
        actual.getClass(),
        String.format(
            "expected class %s, found %s",
            classToString(expected.getClass()), classToString(actual.getClass())));
  }

  private static void assertLangStepSrcTarget(Lang.StepExpr expected, Lang.StepExpr actual) {
    if (expected.subSrc == null) {
      assertNull(
          actual.subSrc,
          String.format("Expected subSrc (null), found %s", assetToString(actual.subSrc)));
    } else {
      assertSame(
          expected.subSrc,
          actual.subSrc,
          String.format(
              "Expected subSrc %s, found %s",
              assetToString(expected.subSrc), assetToString(actual.subSrc)));
    }
    if (expected.src == null) {
      assertNull(
          actual.src, String.format("Expected src (null), found %s", assetToString(actual.src)));
    } else {
      assertSame(
          expected.src,
          actual.src,
          String.format(
              "Expected src %s, found %s", assetToString(expected.src), assetToString(actual.src)));
    }
    if (expected.target == null) {
      assertNull(
          actual.target,
          String.format("Expected target (null), found %s", assetToString(actual.target)));
    } else {
      assertSame(
          expected.target,
          actual.target,
          String.format(
              "Expected target %s, found %s",
              assetToString(expected.target), assetToString(actual.target)));
    }
    if (expected.subTarget == null) {
      assertNull(
          actual.subTarget,
          String.format("Expected subTarget (null), found %s", assetToString(actual.subTarget)));
    } else {
      assertSame(
          expected.subTarget,
          actual.subTarget,
          String.format(
              "Expected subTarget %s, found %s",
              assetToString(expected.subTarget), assetToString(actual.subTarget)));
    }
  }

  private static void assertLangStepField(Lang.StepField expected, Lang.StepField actual) {
    assertSame(
        expected.field,
        actual.field,
        String.format(
            "expected field %s, found %s",
            fieldToString(expected.field), fieldToString(actual.field)));
  }

  private static void assertLangStepAttackStep(
      Lang.StepAttackStep expected, Lang.StepAttackStep actual) {
    assertSame(
        expected.attackStep,
        actual.attackStep,
        String.format(
            "expected attackStep %s, found %s",
            attackStepToString(expected.attackStep), attackStepToString(actual.attackStep)));
  }

  public static void assertLangStepExpr(Lang.StepExpr expected, Lang.StepExpr actual) {
    assertLangStepClass(expected, actual);
    assertLangStepSrcTarget(expected, actual);
    if (expected instanceof Lang.StepUnion) {
      assertLangStepExpr(((Lang.StepUnion) expected).lhs, ((Lang.StepUnion) actual).lhs);
      assertLangStepExpr(((Lang.StepUnion) expected).rhs, ((Lang.StepUnion) actual).rhs);
    } else if (expected instanceof Lang.StepIntersection) {
      assertLangStepExpr(
          ((Lang.StepIntersection) expected).lhs, ((Lang.StepIntersection) actual).lhs);
      assertLangStepExpr(
          ((Lang.StepIntersection) expected).rhs, ((Lang.StepIntersection) actual).rhs);
    } else if (expected instanceof Lang.StepCollect) {
      assertLangStepExpr(((Lang.StepCollect) expected).lhs, ((Lang.StepCollect) actual).lhs);
      assertLangStepExpr(((Lang.StepCollect) expected).rhs, ((Lang.StepCollect) actual).rhs);
    } else if (expected instanceof Lang.StepTransitive) {
      assertLangStepExpr(((Lang.StepTransitive) expected).e, ((Lang.StepTransitive) actual).e);
    } else if (expected instanceof Lang.StepField) {
      assertLangStepField((Lang.StepField) expected, (Lang.StepField) actual);
    } else if (expected instanceof Lang.StepAttackStep) {
      assertLangStepAttackStep((Lang.StepAttackStep) expected, (Lang.StepAttackStep) actual);
    } else {
      fail(
          String.format(
              "Invalid expected subtype \"%s\" of \"StepExpr\"",
              expected.getClass().getSimpleName()));
    }
  }
}

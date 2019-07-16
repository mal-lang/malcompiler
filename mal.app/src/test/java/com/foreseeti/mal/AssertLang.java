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

import static com.foreseeti.mal.AssertAST.assertGetASTClassPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

public final class AssertLang {
  // Prevent instantiation
  private AssertLang() {}

  public static Lang assertGetLangClassPath(String filename) {
    var ast = assertGetASTClassPath(filename);
    try {
      Analyzer.analyze(ast);
    } catch (CompilerException e) {
      fail(e.getMessage());
    }
    return LangConverter.convert(ast);
  }

  public static void assertLangMeta(Lang.Meta expected, Lang.Meta actual, String location) {
    assertEquals(
        expected.getInfo(), actual.getInfo(), String.format("Wrong info string at %s", location));
    assertEquals(
        expected.getAssumptions(),
        actual.getAssumptions(),
        String.format("Wrong assumptions string at %s", location));
    assertEquals(
        expected.getRationale(),
        actual.getRationale(),
        String.format("Wrong rationale string at %s", location));
  }

  public static void assertLangDefines(Map<String, String> expected, Map<String, String> actual) {
    assertEquals(expected.size(), actual.size(), "Wrong number of defines");
    for (var define : expected.entrySet()) {
      assertTrue(
          actual.containsKey(define.getKey()),
          String.format("Expected define #%s", define.getKey()));
      assertEquals(
          define.getValue(),
          actual.get(define.getKey()),
          String.format("Wrong value for define #%s", define.getKey()));
    }
  }

  public static void assertLangCategory(
      Lang lang, String name, String[] expectedAssets, Lang.Meta meta) {
    var categories = lang.getCategories();
    assertTrue(categories.containsKey(name), String.format("Expected category %s", name));
    var category = categories.get(name);
    assertSame(
        category,
        lang.getCategory(name),
        String.format("Different references for category %s", name));
    assertEquals(
        name,
        category.getName(),
        String.format("Wrong category name, should be %s but was %s", name, category.getName()));
    assertLangMeta(meta, category.getMeta(), String.format("category %s", name));
    var actualAssets = category.getAssets();
    assertEquals(
        expectedAssets.length,
        actualAssets.size(),
        String.format("Wrong number of assets in category %s", name));
    for (var expectedAsset : expectedAssets) {
      assertTrue(
          actualAssets.containsKey(expectedAsset),
          String.format("Expected asset %s in category %s", expectedAsset, name));
      assertSame(
          actualAssets.get(expectedAsset),
          category.getAsset(expectedAsset),
          String.format("Different references for asset %s is category %s", expectedAsset, name));
    }
  }

  public static Lang.Asset assertGetLangAsset(
      Lang lang,
      String name,
      boolean isAbstract,
      String category,
      String superAsset,
      Lang.Meta meta) {
    var assets = lang.getAssets();
    assertTrue(assets.containsKey(name), String.format("Expected asset %s", name));
    var asset = assets.get(name);
    assertSame(asset, lang.getAsset(name), String.format("Different references to asset %s", name));
    assertEquals(name, asset.getName(), "Wrong asset name");
    assertEquals(
        isAbstract,
        asset.isAbstract(),
        String.format("Asset %s should%s be abstract", name, isAbstract ? "" : " not"));
    assertSame(
        lang.getCategory(category),
        asset.getCategory(),
        String.format(
            "Wrong category for asset %s, should be %s but was %s",
            name, category, asset.getCategory().getName()));
    if (superAsset == null) {
      assertFalse(
          asset.hasSuperAsset(), String.format("Asset %s shouldn't extend any other asset", name));
      assertNull(
          asset.getSuperAsset(),
          "Asset.hasSuperAsset() returned false but Asset.getSuperAsset() didn't return null");
    } else {
      assertTrue(
          asset.hasSuperAsset(),
          String.format("Asset %s should extend asset %s", name, superAsset));
      assertSame(
          lang.getAsset(superAsset),
          asset.getSuperAsset(),
          String.format(
              "Wrong super asset for asset %s, should be %s but was %s",
              name, superAsset, asset.getSuperAsset().getName()));
    }
    assertLangMeta(meta, asset.getMeta(), String.format("asset %s", name));
    return asset;
  }

  public static void assertLangField(Lang.Asset asset, String name, int min, int max) {
    var fields = asset.getFields();
    assertTrue(
        fields.containsKey(name),
        String.format("Expected field %s in asset %s", name, asset.getName()));
    var field = fields.get(name);
    assertSame(
        field,
        asset.getField(name),
        String.format("Different references to field %s in asset %s", name, asset.getName()));
    assertEquals(
        name, field.getName(), String.format("Wrong field name in asset %s", asset.getName()));
    assertSame(
        asset,
        field.getAsset(),
        String.format(
            "Wrong asset for field %s, should be %s but was %s",
            name, asset.getName(), field.getAsset().getName()));
    assertEquals(
        min, field.getMin(), String.format("Wrong min for field %s", assetFieldToString(field)));
    assertEquals(
        max, field.getMax(), String.format("Wrong max for field %s", assetFieldToString(field)));
  }

  public static Lang.AttackStep assertGetLangAttackStep(
      Lang.Asset asset,
      String name,
      Lang.AttackStepType type,
      boolean inheritsReaches,
      boolean isDefense,
      boolean isConditionalDefense,
      boolean hasParent,
      Lang.Meta meta) {
    var attackSteps = asset.getAttackSteps();
    assertTrue(
        attackSteps.containsKey(name),
        String.format("Expected attack step %s in asset %s", name, asset.getName()));
    var attackStep = attackSteps.get(name);
    assertSame(
        attackStep,
        asset.getAttackStep(name),
        String.format("Different references to attack step %s in asset %s", name, asset.getName()));
    assertEquals(
        name,
        attackStep.getName(),
        String.format("Wrong attack step name in asset %s", asset.getName()));
    assertEquals(
        type,
        attackStep.getType(),
        String.format("Wrong type for attack step %s in asset %s", name, asset.getName()));
    assertSame(
        asset,
        attackStep.getAsset(),
        String.format(
            "Wrong asset for attack step %s, should be %s but was %s",
            name, asset.getName(), attackStep.getAsset().getName()));
    assertEquals(
        inheritsReaches,
        attackStep.inheritsReaches(),
        String.format(
            "Attack step %s should%s inherit reaches steps", name, inheritsReaches ? "" : " not"));
    assertEquals(
        isDefense,
        attackStep.isDefense(),
        String.format("Attack step %s should%s be a defense", name, isDefense ? "" : " not"));
    assertEquals(
        isConditionalDefense,
        attackStep.isConditionalDefense(),
        String.format(
            "Attack step %s should%s be a conditional defense",
            name, isConditionalDefense ? "" : " not"));
    assertEquals(
        hasParent,
        attackStep.hasParent(),
        String.format("Attack step %s should%s have a parent", name, hasParent ? "" : " not"));
    assertLangMeta(
        meta, attackStep.getMeta(), String.format("attack step %s.%s", asset.getName(), name));
    return attackStep;
  }

  public static void assertLangCIA(Lang.AttackStep attackStep, Lang.CIA cia) {
    if (cia == null) {
      assertFalse(
          attackStep.hasCIA(),
          String.format("Attack step %s should not have CIA", assetAttackStepToString(attackStep)));
      assertNull(
          attackStep.getCIA(),
          "AttackStep.hasCIA() returned false but AttackStep.getCIA() didn't return null");
    } else {
      assertTrue(
          attackStep.hasCIA(),
          String.format("Attack step %s should have CIA", assetAttackStepToString(attackStep)));
      assertEquals(
          cia.C,
          attackStep.getCIA().C,
          String.format(
              "Attack step %s should%s have {C}",
              assetAttackStepToString(attackStep), cia.C ? "" : " not"));
      assertEquals(
          cia.I,
          attackStep.getCIA().I,
          String.format(
              "Attack step %s should%s have {I}",
              assetAttackStepToString(attackStep), cia.I ? "" : " not"));
      assertEquals(
          cia.A,
          attackStep.getCIA().A,
          String.format(
              "Attack step %s should%s have {A}",
              assetAttackStepToString(attackStep), cia.A ? "" : " not"));
    }
  }

  public static void assertLangTTC(Lang.AttackStep attackStep, Lang.TTCExpr ttc) {
    if (ttc == null) {
      assertFalse(
          attackStep.hasTTC(),
          String.format("Attack step %s should not have TTC", assetAttackStepToString(attackStep)));
      assertNull(
          attackStep.getTTC(),
          "AttackStep.hasTTC() returned false but AttackStep.getTTC() didn't return null");
    } else {
      assertTrue(
          attackStep.hasTTC(),
          String.format("Attack step %s should have TTC", assetAttackStepToString(attackStep)));
      assertTTCExpr(ttc, attackStep.getTTC());
    }
  }

  private static void assertTTCExpr(Lang.TTCExpr expected, Lang.TTCExpr actual) {
    assertSameClass(expected, actual);
    if (expected instanceof Lang.TTCAdd) {
      assertTTCExpr(((Lang.TTCAdd) expected).lhs, ((Lang.TTCAdd) actual).lhs);
      assertTTCExpr(((Lang.TTCAdd) expected).rhs, ((Lang.TTCAdd) actual).rhs);
    } else if (expected instanceof Lang.TTCSub) {
      assertTTCExpr(((Lang.TTCSub) expected).lhs, ((Lang.TTCSub) actual).lhs);
      assertTTCExpr(((Lang.TTCSub) expected).rhs, ((Lang.TTCSub) actual).rhs);
    } else if (expected instanceof Lang.TTCMul) {
      assertTTCExpr(((Lang.TTCMul) expected).lhs, ((Lang.TTCMul) actual).lhs);
      assertTTCExpr(((Lang.TTCMul) expected).rhs, ((Lang.TTCMul) actual).rhs);
    } else if (expected instanceof Lang.TTCDiv) {
      assertTTCExpr(((Lang.TTCDiv) expected).lhs, ((Lang.TTCDiv) actual).lhs);
      assertTTCExpr(((Lang.TTCDiv) expected).rhs, ((Lang.TTCDiv) actual).rhs);
    } else if (expected instanceof Lang.TTCPow) {
      assertTTCExpr(((Lang.TTCPow) expected).lhs, ((Lang.TTCPow) actual).lhs);
      assertTTCExpr(((Lang.TTCPow) expected).rhs, ((Lang.TTCPow) actual).rhs);
    } else if (expected instanceof Lang.TTCFunc) {
      assertDistribution(((Lang.TTCFunc) expected).dist, ((Lang.TTCFunc) actual).dist);
    }
  }

  private static void assertDistribution(
      Distributions.Distribution expected, Distributions.Distribution actual) {
    assertSameClass(expected, actual);
    if (expected instanceof Distributions.Bernoulli) {
      assertEquals(
          ((Distributions.Bernoulli) expected).probability,
          ((Distributions.Bernoulli) actual).probability);
    } else if (expected instanceof Distributions.Binomial) {
      assertEquals(
          ((Distributions.Binomial) expected).trials, ((Distributions.Binomial) actual).trials);
      assertEquals(
          ((Distributions.Binomial) expected).probability,
          ((Distributions.Binomial) actual).probability);
    } else if (expected instanceof Distributions.Exponential) {
      assertEquals(
          ((Distributions.Exponential) expected).lambda,
          ((Distributions.Exponential) actual).lambda);
    } else if (expected instanceof Distributions.Gamma) {
      assertEquals(((Distributions.Gamma) expected).shape, ((Distributions.Gamma) actual).shape);
      assertEquals(((Distributions.Gamma) expected).scale, ((Distributions.Gamma) actual).scale);
    } else if (expected instanceof Distributions.LogNormal) {
      assertEquals(
          ((Distributions.LogNormal) expected).mean, ((Distributions.LogNormal) actual).mean);
      assertEquals(
          ((Distributions.LogNormal) expected).standardDeviation,
          ((Distributions.LogNormal) actual).standardDeviation);
    } else if (expected instanceof Distributions.Pareto) {
      assertEquals(((Distributions.Pareto) expected).min, ((Distributions.Pareto) actual).min);
      assertEquals(((Distributions.Pareto) expected).shape, ((Distributions.Pareto) actual).shape);
    } else if (expected instanceof Distributions.TruncatedNormal) {
      assertEquals(
          ((Distributions.TruncatedNormal) expected).mean,
          ((Distributions.TruncatedNormal) actual).mean);
      assertEquals(
          ((Distributions.TruncatedNormal) expected).standardDeviation,
          ((Distributions.TruncatedNormal) actual).standardDeviation);
    } else if (expected instanceof Distributions.Uniform) {
      assertEquals(((Distributions.Uniform) expected).min, ((Distributions.Uniform) actual).min);
      assertEquals(((Distributions.Uniform) expected).max, ((Distributions.Uniform) actual).max);
    }
  }

  private static void assertSameClass(Object expected, Object actual) {
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
    assertSameClass(expected, actual);
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

  public static void assertLangLink(
      Lang lang,
      String asset1,
      String asset1Field,
      String asset2,
      String asset2Field,
      int idx,
      String name,
      Lang.Meta meta) {
    var link = lang.getLinks().get(idx);
    var leftField = lang.getAsset(asset1).getField(asset1Field);
    var rightField = lang.getAsset(asset2).getField(asset2Field);
    assertEquals(name, link.getName(), String.format("Wrong name for association %d", idx));
    assertLangMeta(meta, link.getMeta(), linkToString(idx, name));
    assertSame(
        leftField,
        link.getLeftField(),
        String.format(
            "Wrong left field for %s, should be %s but was %s",
            linkToString(idx, name),
            assetFieldToString(leftField),
            assetFieldToString(link.getLeftField())));
    assertSame(
        rightField,
        link.getRightField(),
        String.format(
            "Wrong right field for %s, should be %s but was %s",
            linkToString(idx, name),
            assetFieldToString(rightField),
            assetFieldToString(link.getRightField())));
    assertSame(
        link,
        leftField.getLink(),
        String.format(
            "Wrong link for field %s, should be %s but was %s",
            assetFieldToString(leftField), linkToString(idx, name), leftField.getLink().getName()));
    assertSame(
        link,
        rightField.getLink(),
        String.format(
            "Wrong link for field %s, should be %s but was %s",
            assetFieldToString(rightField),
            linkToString(idx, name),
            rightField.getLink().getName()));
    assertSame(
        rightField,
        leftField.getTarget(),
        String.format(
            "Wrong target for field %s, should be %s but was %s",
            assetFieldToString(leftField),
            assetFieldToString(rightField),
            assetFieldToString(leftField.getTarget())));
    assertSame(
        leftField,
        rightField.getTarget(),
        String.format(
            "Wrong target for field %s, should be %s but was %s",
            assetFieldToString(rightField),
            assetFieldToString(leftField),
            assetFieldToString(rightField.getTarget())));
  }

  private static String classToString(Class<?> c) {
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

  private static String assetFieldToString(Lang.Field field) {
    if (field == null) {
      return "(null)";
    } else {
      return String.format("\"%s.%s\"", field.getAsset().getName(), field.getName());
    }
  }

  private static String attackStepToString(Lang.AttackStep attackStep) {
    if (attackStep == null) {
      return "(null)";
    } else {
      return String.format("\"%s\"", attackStep.getName());
    }
  }

  private static String assetAttackStepToString(Lang.AttackStep attackStep) {
    if (attackStep == null) {
      return "(null)";
    } else {
      return String.format("\"%s.%s\"", attackStep.getAsset().getName(), attackStep.getName());
    }
  }

  private static String linkToString(int idx, String name) {
    return String.format("association %d, %s", idx, name);
  }
}

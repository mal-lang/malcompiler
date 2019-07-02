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
import static com.foreseeti.mal.AssertLang.assertLangStepExpr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestLangConverter {
  @BeforeEach
  public void init() {
    TestUtils.initTestSystem();
  }

  @AfterEach
  public void tearDown() {
    TestUtils.clearTestSystem();
  }

  @Test
  public void testComplexModel() {
    try {
      var ast = assertGetASTClassPath("analyzer/complex.mal");
      Analyzer.analyze(ast);
      var lang = LangConverter.convert(ast);
      assertLangComplex(lang);
    } catch (CompilerException e) {
      fail(e.getMessage());
    }
  }

  private static void assertMeta(
      Lang.Meta meta, String info, String assumptions, String rationale) {
    assertEquals(info, meta.getInfo());
    assertEquals(assumptions, meta.getAssumptions());
    assertEquals(rationale, meta.getRationale());
  }

  private static void assertDefines(Lang lang) {
    var defines = lang.getDefines();
    assertEquals(2, defines.size());
    assertTrue(defines.containsKey("id"));
    assertEquals("complex", defines.get("id"));
    assertEquals("complex", lang.getDefine("id"));
    assertTrue(defines.containsKey("version"));
    assertEquals("1.0.0", defines.get("version"));
    assertEquals("1.0.0", lang.getDefine("version"));
  }

  private static void assertCategory(Lang lang, String categoryName, String[] expectedAssets) {
    var categories = lang.getCategories();
    assertTrue(categories.containsKey(categoryName));
    var category = categories.get(categoryName);
    assertSame(category, lang.getCategory(categoryName));
    assertEquals(categoryName, category.getName());
    assertMeta(category.getMeta(), null, null, null);
    var assets = category.getAssets();
    assertEquals(expectedAssets.length, assets.size());
    for (var asset : expectedAssets) {
      assertTrue(assets.containsKey(asset));
      assertSame(assets.get(asset), category.getAsset(asset));
    }
  }

  private static void assertCategoryPerson(Lang lang) {
    String categoryName = "Person";
    String[] expectedAssets = {"User", "Student", "Teacher"};
    assertCategory(lang, categoryName, expectedAssets);
  }

  private static void assertCategoryHardware(Lang lang) {
    String categoryName = "Hardware";
    String[] expectedAssets = {"Computer", "Firewall", "Harddrive", "SecretFolder"};
    assertCategory(lang, categoryName, expectedAssets);
  }

  private static void assertCategories(Lang lang) {
    assertEquals(2, lang.getCategories().size());
    assertCategoryPerson(lang);
    assertCategoryHardware(lang);
  }

  private static Lang.Asset assertAsset(
      Lang lang, String name, boolean isAbstract, String category, String superAsset) {
    var assets = lang.getAssets();
    assertTrue(assets.containsKey(name));
    var asset = assets.get(name);
    assertSame(asset, lang.getAsset(name));
    assertEquals(name, asset.getName());
    assertEquals(isAbstract, asset.isAbstract());
    assertSame(lang.getCategory(category), asset.getCategory());
    if (superAsset == null) {
      assertFalse(asset.hasSuperAsset());
      assertNull(asset.getSuperAsset());
    } else {
      assertTrue(asset.hasSuperAsset());
      assertSame(lang.getAsset(superAsset), asset.getSuperAsset());
    }
    return asset;
  }

  private static void assertField(Lang.Asset asset, String name, int min, int max) {
    var fields = asset.getFields();
    assertTrue(fields.containsKey(name));
    var field = fields.get(name);
    assertSame(field, asset.getField(name));
    assertEquals(name, field.getName());
    assertSame(asset, field.getAsset());
    assertEquals(min, field.getMin());
    assertEquals(max, field.getMax());
  }

  private static Lang.AttackStep assertAttackStep(
      Lang.Asset asset,
      String name,
      Lang.AttackStepType type,
      boolean inheritsReaches,
      boolean isDefense,
      boolean hasParent) {
    var attackSteps = asset.getAttackSteps();
    assertTrue(attackSteps.containsKey(name));
    var attackStep = attackSteps.get(name);
    assertSame(attackStep, asset.getAttackStep(name));
    assertEquals(name, attackStep.getName());
    assertEquals(type, attackStep.getType());
    assertSame(asset, attackStep.getAsset());
    assertEquals(inheritsReaches, attackStep.inheritsReaches());
    assertEquals(isDefense, attackStep.isDefense());
    assertEquals(hasParent, attackStep.hasParent());
    return attackStep;
  }

  private static void assertAssetUser(Lang lang) {
    var asset = assertAsset(lang, "User", true, "Person", null);
    assertMeta(asset.getMeta(), null, null, null);
    // Check fields
    assertEquals(2, asset.getFields().size());
    assertField(asset, "firewall", 1, 1);
    assertField(asset, "computer", 1, 1);
    // Check attack steps
    assertEquals(4, asset.getAttackSteps().size());

    // Check attack step "impersonate"
    var attackStep =
        assertAttackStep(asset, "impersonate", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    var reaches = attackStep.getReaches();
    assertEquals(2, reaches.size());
    // -> compromise
    Lang.StepExpr step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("compromise"));
    assertLangStepExpr(step, reaches.get(0));
    // -> stealInformation
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("stealInformation"));
    assertLangStepExpr(step, reaches.get(1));

    // Check parent steps for attack step "impersonate"
    var parentSteps = attackStep.getParentSteps();
    assertEquals(2, parentSteps.size());
    // -> computer.retrievePassword
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                asset.getField("computer")),
            new Lang.StepAttackStep(
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                lang.getAsset("Computer").getAttackStep("retrievePassword")));
    assertLangStepExpr(step, parentSteps.get(0));
    // -> [Teacher]firewall.bypassFirewall
    step =
        new Lang.StepCollect(
            lang.getAsset("Teacher"),
            asset,
            null,
            null,
            new Lang.StepField(
                lang.getAsset("Teacher"),
                asset,
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall"),
                asset.getField("firewall")),
            new Lang.StepAttackStep(
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall").getAttackStep("bypassFirewall")));
    assertLangStepExpr(step, parentSteps.get(1));

    // Check attack step "compromise"
    attackStep =
        assertAttackStep(asset, "compromise", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> computer.stealSecret
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                asset.getField("computer")),
            new Lang.StepAttackStep(
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                lang.getAsset("Computer").getAttackStep("stealSecret")));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "compromise"
    parentSteps = attackStep.getParentSteps();
    assertEquals(2, parentSteps.size());
    // -> impersonate
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("impersonate"));
    assertLangStepExpr(step, parentSteps.get(0));
    // -> [Teacher]accessSchoolComputer
    step =
        new Lang.StepAttackStep(
            lang.getAsset("Teacher"),
            asset,
            lang.getAsset("Teacher").getAttackStep("accessSchoolComputer"));
    assertLangStepExpr(step, parentSteps.get(1));

    // Check attack step "stealInformation"
    attackStep =
        assertAttackStep(asset, "stealInformation", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> computer.internalHD.stealHDSecrets
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepCollect(
                asset,
                asset,
                lang.getAsset("Harddrive"),
                lang.getAsset("Harddrive"),
                new Lang.StepField(
                    asset,
                    asset,
                    lang.getAsset("Computer"),
                    lang.getAsset("Computer"),
                    asset.getField("computer")),
                new Lang.StepField(
                    lang.getAsset("Computer"),
                    lang.getAsset("Computer"),
                    lang.getAsset("Harddrive"),
                    lang.getAsset("Harddrive"),
                    lang.getAsset("Computer").getField("internalHD"))),
            new Lang.StepAttackStep(
                lang.getAsset("Harddrive"),
                lang.getAsset("Harddrive"),
                lang.getAsset("Harddrive").getAttackStep("stealHDSecrets")));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "stealInformation"
    parentSteps = attackStep.getParentSteps();
    assertEquals(1, parentSteps.size());
    // -> impersonate
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("impersonate"));
    assertLangStepExpr(step, parentSteps.get(0));

    // Check attack step "stealFolder"
    attackStep =
        assertAttackStep(asset, "stealFolder", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> computer.(externalHD.stealFolder)
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                asset.getField("computer")),
            new Lang.StepCollect(
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                null,
                null,
                new Lang.StepField(
                    lang.getAsset("Computer"),
                    lang.getAsset("Computer"),
                    lang.getAsset("Harddrive"),
                    lang.getAsset("Harddrive"),
                    lang.getAsset("Computer").getField("externalHD")),
                new Lang.StepAttackStep(
                    lang.getAsset("Harddrive"),
                    lang.getAsset("Harddrive"),
                    lang.getAsset("Harddrive").getAttackStep("stealFolder"))));

    // Check parent steps for attack step "stealFolder"
    assertEquals(0, attackStep.getParentSteps().size());
  }

  private static void assertAssetStudent(Lang lang) {
    var asset = assertAsset(lang, "Student", false, "Person", "User");
    assertMeta(asset.getMeta(), null, null, null);
    // Check fields
    assertEquals(1, asset.getFields().size());
    assertField(asset, "studentComputer", 1, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(0, asset.getAttackSteps().size());
  }

  private static void assertAssetTeacher(Lang lang) {
    var asset = assertAsset(lang, "Teacher", false, "Person", "User");
    assertMeta(asset.getMeta(), null, null, null);
    // Check fields
    assertEquals(1, asset.getFields().size());
    assertField(asset, "teacherComputer", 1, 1);
    // Check attack steps
    assertEquals(1, asset.getAttackSteps().size());

    // Check attack step "accessSchoolComputer"
    var attackStep =
        assertAttackStep(
            asset, "accessSchoolComputer", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(
        attackStep.getMeta(),
        "An extra level of protection, their school computer must be used to impersonate them.",
        null,
        null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    var reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> compromise
    Lang.StepExpr step =
        new Lang.StepAttackStep(asset, lang.getAsset("User"), asset.getAttackStep("compromise"));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "accessSchoolComputer"
    assertEquals(0, attackStep.getParentSteps().size());
  }

  private static void assertAssetComputer(Lang lang) {
    var asset = assertAsset(lang, "Computer", false, "Hardware", null);
    assertMeta(asset.getMeta(), null, null, null);
    // Check fields
    assertEquals(6, asset.getFields().size());
    assertField(asset, "student", 1, 1);
    assertField(asset, "teacher", 1, 1);
    assertField(asset, "firewall", 1, 1);
    assertField(asset, "user", 1, 1);
    assertField(asset, "externalHD", 1, 1);
    assertField(asset, "internalHD", 1, 1);
    // Check attack steps
    assertEquals(7, asset.getAttackSteps().size());

    // Check attack step "malwareInfection"
    var attackStep =
        assertAttackStep(asset, "malwareInfection", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    var reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> interceptTraffic
    Lang.StepExpr step =
        new Lang.StepAttackStep(asset, asset, asset.getAttackStep("interceptTraffic"));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "malwareInfection"
    assertEquals(0, attackStep.getParentSteps().size());

    // Check attack step "interceptTraffic"
    attackStep =
        assertAttackStep(asset, "interceptTraffic", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> retrievePassword
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("retrievePassword"));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "interceptTraffic"
    var parentSteps = attackStep.getParentSteps();
    assertEquals(2, parentSteps.size());
    // -> malwareInfection
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("malwareInfection"));
    assertLangStepExpr(step, parentSteps.get(0));
    // -> firewall.bypassFirewall
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall"),
                asset.getField("firewall")),
            new Lang.StepAttackStep(
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall").getAttackStep("bypassFirewall")));
    assertLangStepExpr(step, parentSteps.get(1));

    // Check attack step "retrievePassword"
    attackStep =
        assertAttackStep(asset, "retrievePassword", Lang.AttackStepType.ALL, false, false, false);
    assertMeta(
        attackStep.getMeta(),
        "Retrieval of password is only possible if password is unencrypted",
        null,
        null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> user.impersonate
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset, asset, lang.getAsset("User"), lang.getAsset("User"), asset.getField("user")),
            new Lang.StepAttackStep(
                lang.getAsset("User"),
                lang.getAsset("User"),
                lang.getAsset("User").getAttackStep("impersonate")));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "retrievePassword"
    parentSteps = attackStep.getParentSteps();
    assertEquals(3, parentSteps.size());
    // -> interceptTraffic
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("interceptTraffic"));
    assertLangStepExpr(step, parentSteps.get(0));
    // -> passwordEncrypted
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("passwordEncrypted"));
    assertLangStepExpr(step, parentSteps.get(1));
    // -> firewall.bypassFirewall
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall"),
                asset.getField("firewall")),
            new Lang.StepAttackStep(
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall").getAttackStep("bypassFirewall")));
    assertLangStepExpr(step, parentSteps.get(2));

    // Check attack step "bypassFirewall"
    attackStep =
        assertAttackStep(asset, "bypassFirewall", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertTrue(attackStep.hasTTC());
    // ExponentialDistribution(0.05) * GammaDistribution(1.2, 1.7)
    var ttc = attackStep.getTTC();
    assertTrue(ttc instanceof Lang.TTCMul);
    // ExponentialDistribution(0.05)
    var subTTC = ((Lang.TTCMul) ttc).lhs;
    assertTrue(subTTC instanceof Lang.TTCFunc);
    var ttcFunc = (Lang.TTCFunc) subTTC;
    assertEquals("ExponentialDistribution", ttcFunc.name);
    assertEquals(1, ttcFunc.params.size());
    assertEquals(0.05, ttcFunc.params.get(0));
    // GammaDistribution(1.2, 1.7)
    subTTC = ((Lang.TTCMul) ttc).rhs;
    assertTrue(subTTC instanceof Lang.TTCFunc);
    ttcFunc = (Lang.TTCFunc) subTTC;
    assertEquals("GammaDistribution", ttcFunc.name);
    assertEquals(2, ttcFunc.params.size());
    assertEquals(1.2, ttcFunc.params.get(0));
    assertEquals(1.7, ttcFunc.params.get(1));
    assertEquals(0, attackStep.getRequires().size());
    reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> firewall.bypassFirewall
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall"),
                asset.getField("firewall")),
            new Lang.StepAttackStep(
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall").getAttackStep("bypassFirewall")));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "bypassFirewall"
    assertEquals(0, attackStep.getParentSteps().size());

    // Check attack step "stealSecret"
    attackStep =
        assertAttackStep(asset, "stealSecret", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> (externalHD \/ internalHD).stealHDSecrets
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepUnion(
                asset,
                asset,
                lang.getAsset("Harddrive"),
                lang.getAsset("Harddrive"),
                new Lang.StepField(
                    asset,
                    asset,
                    lang.getAsset("Harddrive"),
                    lang.getAsset("Harddrive"),
                    asset.getField("externalHD")),
                new Lang.StepField(
                    asset,
                    asset,
                    lang.getAsset("Harddrive"),
                    lang.getAsset("Harddrive"),
                    asset.getField("internalHD"))),
            new Lang.StepAttackStep(
                lang.getAsset("Harddrive"),
                lang.getAsset("Harddrive"),
                lang.getAsset("Harddrive").getAttackStep("stealHDSecrets")));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "stealSecret"
    parentSteps = attackStep.getParentSteps();
    assertEquals(1, parentSteps.size());
    // -> user.compromise
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset, asset, lang.getAsset("User"), lang.getAsset("User"), asset.getField("user")),
            new Lang.StepAttackStep(
                lang.getAsset("User"),
                lang.getAsset("User"),
                lang.getAsset("User").getAttackStep("compromise")));
    assertLangStepExpr(step, parentSteps.get(0));

    // Check attack step "passwordEncrypted"
    attackStep =
        assertAttackStep(
            asset, "passwordEncrypted", Lang.AttackStepType.DEFENSE, false, true, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> retrievePassword
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("retrievePassword"));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "passwordEncrypted"
    assertEquals(0, attackStep.getParentSteps().size());

    // Check attack step "firewallProtected"
    attackStep =
        assertAttackStep(asset, "firewallProtected", Lang.AttackStepType.EXIST, false, true, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    var requires = attackStep.getRequires();
    assertEquals(1, requires.size());
    reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // <- firewall
    step =
        new Lang.StepField(
            asset,
            asset,
            lang.getAsset("Firewall"),
            lang.getAsset("Firewall"),
            asset.getField("firewall"));
    assertLangStepExpr(step, requires.get(0));
    // -> firewall.bypassFirewall
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall"),
                asset.getField("firewall")),
            new Lang.StepAttackStep(
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall"),
                lang.getAsset("Firewall").getAttackStep("bypassFirewall")));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "firewallProtected"
    assertEquals(0, attackStep.getParentSteps().size());
  }

  private static void assertAssetFirewall(Lang lang) {
    var asset = assertAsset(lang, "Firewall", false, "Hardware", null);
    assertMeta(asset.getMeta(), null, null, null);
    // Check fields
    assertEquals(2, asset.getFields().size());
    assertField(asset, "computer", 0, Integer.MAX_VALUE);
    assertField(asset, "user", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(1, asset.getAttackSteps().size());

    // Check attack step "bypassFirewall"
    var attackStep =
        assertAttackStep(asset, "bypassFirewall", Lang.AttackStepType.ALL, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    var reaches = attackStep.getReaches();
    assertEquals(3, reaches.size());
    // -> computer.retrievePassword
    Lang.StepExpr step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                asset.getField("computer")),
            new Lang.StepAttackStep(
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                lang.getAsset("Computer").getAttackStep("retrievePassword")));
    assertLangStepExpr(step, reaches.get(0));
    // -> computer.interceptTraffic
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                asset.getField("computer")),
            new Lang.StepAttackStep(
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                lang.getAsset("Computer").getAttackStep("interceptTraffic")));
    assertLangStepExpr(step, reaches.get(1));
    // -> user[Teacher].impersonate
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("User"),
                lang.getAsset("Teacher"),
                asset.getField("user")),
            new Lang.StepAttackStep(
                lang.getAsset("Teacher"),
                lang.getAsset("User"),
                lang.getAsset("User").getAttackStep("impersonate")));
    assertLangStepExpr(step, reaches.get(2));

    // Check parent steps for attack step "bypassFirewall"
    var parentSteps = attackStep.getParentSteps();
    assertEquals(2, parentSteps.size());
    // -> computer.bypassFirewall
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                asset.getField("computer")),
            new Lang.StepAttackStep(
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                lang.getAsset("Computer").getAttackStep("bypassFirewall")));
    assertLangStepExpr(step, parentSteps.get(0));
    // -> computer.firewallProtected
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepField(
                asset,
                asset,
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                asset.getField("computer")),
            new Lang.StepAttackStep(
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                lang.getAsset("Computer").getAttackStep("firewallProtected")));
    assertLangStepExpr(step, parentSteps.get(1));
  }

  private static void assertAssetHarddrive(Lang lang) {
    var asset = assertAsset(lang, "Harddrive", false, "Hardware", null);
    assertMeta(asset.getMeta(), null, null, null);
    // Check fields
    assertEquals(3, asset.getFields().size());
    assertField(asset, "extHDComputer", 1, 1);
    assertField(asset, "intHDComputer", 1, 1);
    assertField(asset, "folder", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(2, asset.getAttackSteps().size());

    // Check attack step "stealHDSecrets"
    var attackStep =
        assertAttackStep(asset, "stealHDSecrets", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    assertEquals(0, attackStep.getReaches().size());

    // Check parent steps for attack step "stealHDSecrets"
    var parentSteps = attackStep.getParentSteps();
    assertEquals(2, parentSteps.size());
    // -> intHDComputer.user.stealInformation
    Lang.StepExpr step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepCollect(
                asset,
                asset,
                lang.getAsset("User"),
                lang.getAsset("User"),
                new Lang.StepField(
                    asset,
                    asset,
                    lang.getAsset("Computer"),
                    lang.getAsset("Computer"),
                    asset.getField("intHDComputer")),
                new Lang.StepField(
                    lang.getAsset("Computer"),
                    lang.getAsset("Computer"),
                    lang.getAsset("User"),
                    lang.getAsset("User"),
                    lang.getAsset("Computer").getField("user"))),
            new Lang.StepAttackStep(
                lang.getAsset("User"),
                lang.getAsset("User"),
                lang.getAsset("User").getAttackStep("stealInformation")));
    assertLangStepExpr(step, parentSteps.get(0));
    // -> (intHDComputer \/ extHDComputer).stealSecret
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepUnion(
                asset,
                asset,
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                new Lang.StepField(
                    asset,
                    asset,
                    lang.getAsset("Computer"),
                    lang.getAsset("Computer"),
                    asset.getField("intHDComputer")),
                new Lang.StepField(
                    asset,
                    asset,
                    lang.getAsset("Computer"),
                    lang.getAsset("Computer"),
                    asset.getField("extHDComputer"))),
            new Lang.StepAttackStep(
                lang.getAsset("Computer"),
                lang.getAsset("Computer"),
                lang.getAsset("Computer").getAttackStep("stealSecret")));
    assertLangStepExpr(step, parentSteps.get(1));

    // Check attack step "stealFolder"
    attackStep =
        assertAttackStep(asset, "stealFolder", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    var reaches = attackStep.getReaches();
    assertEquals(1, reaches.size());
    // -> ((folder.(subFolder)*).accessFolder)
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepCollect(
                asset,
                asset,
                lang.getAsset("SecretFolder"),
                lang.getAsset("SecretFolder"),
                new Lang.StepField(
                    asset,
                    asset,
                    lang.getAsset("SecretFolder"),
                    lang.getAsset("SecretFolder"),
                    asset.getField("folder")),
                new Lang.StepTransitive(
                    lang.getAsset("SecretFolder"),
                    lang.getAsset("SecretFolder"),
                    lang.getAsset("SecretFolder"),
                    lang.getAsset("SecretFolder"),
                    new Lang.StepField(
                        lang.getAsset("SecretFolder"),
                        lang.getAsset("SecretFolder"),
                        lang.getAsset("SecretFolder"),
                        lang.getAsset("SecretFolder"),
                        lang.getAsset("SecretFolder").getField("subFolder")))),
            new Lang.StepAttackStep(
                lang.getAsset("SecretFolder"),
                lang.getAsset("SecretFolder"),
                lang.getAsset("SecretFolder").getAttackStep("accessFolder")));
    assertLangStepExpr(step, reaches.get(0));

    // Check parent steps for attack step "stealFolder"
    parentSteps = attackStep.getParentSteps();
    assertEquals(1, parentSteps.size());
    // -> extHDComputer.user.stealFolder
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepCollect(
                asset,
                asset,
                lang.getAsset("User"),
                lang.getAsset("User"),
                new Lang.StepField(
                    asset,
                    asset,
                    lang.getAsset("Computer"),
                    lang.getAsset("Computer"),
                    asset.getField("extHDComputer")),
                new Lang.StepField(
                    lang.getAsset("Computer"),
                    lang.getAsset("Computer"),
                    lang.getAsset("User"),
                    lang.getAsset("User"),
                    lang.getAsset("Computer").getField("user"))),
            new Lang.StepAttackStep(
                lang.getAsset("User"),
                lang.getAsset("User"),
                lang.getAsset("User").getAttackStep("stealFolder")));
    assertLangStepExpr(step, parentSteps.get(0));
  }

  private static void assertAssetSecretFolder(Lang lang) {
    var asset = assertAsset(lang, "SecretFolder", false, "Hardware", null);
    assertMeta(asset.getMeta(), null, null, null);
    // Check fields
    assertEquals(3, asset.getFields().size());
    assertField(asset, "internalHD", 1, 1);
    assertField(asset, "subFolder", 0, Integer.MAX_VALUE);
    assertField(asset, "folder", 1, 1);
    // Check attack steps
    assertEquals(1, asset.getAttackSteps().size());

    // Check attack step "accessFolder"
    var attackStep =
        assertAttackStep(asset, "accessFolder", Lang.AttackStepType.ANY, false, false, false);
    assertMeta(attackStep.getMeta(), null, null, null);
    assertFalse(attackStep.hasTTC());
    assertNull(attackStep.getTTC());
    assertEquals(0, attackStep.getRequires().size());
    assertEquals(0, attackStep.getReaches().size());

    // Check parent steps for attack step "accessFolder"
    var parentSteps = attackStep.getParentSteps();
    assertEquals(1, parentSteps.size());
    // -> folder*.internalHD.stealFolder
    var step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepCollect(
                asset,
                asset,
                lang.getAsset("Harddrive"),
                lang.getAsset("Harddrive"),
                new Lang.StepTransitive(
                    asset,
                    asset,
                    asset,
                    asset,
                    new Lang.StepField(asset, asset, asset, asset, asset.getField("folder"))),
                new Lang.StepField(
                    asset,
                    asset,
                    lang.getAsset("Harddrive"),
                    lang.getAsset("Harddrive"),
                    asset.getField("internalHD"))),
            new Lang.StepAttackStep(
                lang.getAsset("Harddrive"),
                lang.getAsset("Harddrive"),
                lang.getAsset("Harddrive").getAttackStep("stealFolder")));
    assertLangStepExpr(step, parentSteps.get(0));
  }

  private static void assertAssets(Lang lang) {
    assertEquals(7, lang.getAssets().size());
    assertAssetUser(lang);
    assertAssetStudent(lang);
    assertAssetTeacher(lang);
    assertAssetComputer(lang);
    assertAssetFirewall(lang);
    assertAssetHarddrive(lang);
    assertAssetSecretFolder(lang);
  }

  private static void assertLink(
      Lang lang, String a1, String a1Field, String a2, String a2Field, int linkIdx, String name) {
    var link = lang.getLinks().get(linkIdx);
    var leftField = lang.getAsset(a1).getField(a1Field);
    var rightField = lang.getAsset(a2).getField(a2Field);
    assertEquals(name, link.getName());
    assertMeta(link.getMeta(), null, null, null);
    assertSame(leftField, link.getLeftField());
    assertSame(rightField, link.getRightField());
    assertSame(link, leftField.getLink());
    assertSame(link, rightField.getLink());
    assertSame(rightField, leftField.getTarget());
    assertSame(leftField, rightField.getTarget());
  }

  private static void assertLinks(Lang lang) {
    assertEquals(9, lang.getLinks().size());
    assertLink(lang, "Computer", "student", "Student", "studentComputer", 0, "Use");
    assertLink(lang, "Computer", "teacher", "Teacher", "teacherComputer", 1, "Use");
    assertLink(lang, "Computer", "firewall", "Firewall", "computer", 2, "Protect");
    assertLink(lang, "Firewall", "user", "User", "firewall", 3, "Protect");
    assertLink(lang, "Computer", "user", "User", "computer", 4, "Storage");
    assertLink(lang, "Computer", "externalHD", "Harddrive", "extHDComputer", 5, "Use");
    assertLink(lang, "Computer", "internalHD", "Harddrive", "intHDComputer", 6, "Contain");
    assertLink(lang, "Harddrive", "folder", "SecretFolder", "internalHD", 7, "Contain");
    assertLink(lang, "SecretFolder", "subFolder", "SecretFolder", "folder", 8, "Contain");
  }

  private static void assertLangComplex(Lang lang) {
    assertDefines(lang);
    assertCategories(lang);
    assertAssets(lang);
    assertLinks(lang);
  }
}

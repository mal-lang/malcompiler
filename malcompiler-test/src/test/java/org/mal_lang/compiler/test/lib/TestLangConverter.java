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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mal_lang.compiler.test.lib.AssertLang.assertGetLangAsset;
import static org.mal_lang.compiler.test.lib.AssertLang.assertGetLangAttackStep;
import static org.mal_lang.compiler.test.lib.AssertLang.assertGetLangClassPath;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangCIA;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangCategory;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangDefines;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangField;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangLink;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangStepExpr;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangTTC;
import static org.mal_lang.compiler.test.lib.AssertLang.assertLangTags;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.lib.Distributions;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.test.MalTest;

public class TestLangConverter extends MalTest {
  @Test
  public void testReverse() {
    var lang = assertGetLangClassPath("lang-converter/reverse.mal");
    // localaction
    var asset = assertGetLangAsset(lang, "LocalAction", false, "CAT", "Action", new Lang.Meta());
    // compromise
    var attackstep =
        assertGetLangAttackStep(
            asset,
            "compromise",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            true,
            new Lang.Meta());
    // -> (a.ga /\ a.la).compromise
    var step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepIntersection(
                lang.getAsset("LocalAction"),
                lang.getAsset("LocalAction"),
                lang.getAsset("Action"),
                lang.getAsset("Action"),
                new Lang.StepCollect(
                    lang.getAsset("LocalAction"),
                    lang.getAsset("LocalAction"),
                    lang.getAsset("GlobalAction"),
                    lang.getAsset("GlobalAction"),
                    new Lang.StepField(
                        lang.getAsset("LocalAction"),
                        lang.getAsset("LocalAction"),
                        lang.getAsset("Alpha"),
                        lang.getAsset("Alpha"),
                        asset.getField("a")),
                    new Lang.StepField(
                        lang.getAsset("Alpha"),
                        lang.getAsset("Alpha"),
                        lang.getAsset("GlobalAction"),
                        lang.getAsset("GlobalAction"),
                        lang.getAsset("Alpha").getField("ga"))),
                new Lang.StepCollect(
                    lang.getAsset("LocalAction"),
                    lang.getAsset("LocalAction"),
                    lang.getAsset("LocalAction"),
                    lang.getAsset("LocalAction"),
                    new Lang.StepField(
                        lang.getAsset("LocalAction"),
                        lang.getAsset("LocalAction"),
                        lang.getAsset("Alpha"),
                        lang.getAsset("Alpha"),
                        asset.getField("a")),
                    new Lang.StepField(
                        lang.getAsset("Alpha"),
                        lang.getAsset("Alpha"),
                        lang.getAsset("LocalAction"),
                        lang.getAsset("LocalAction"),
                        lang.getAsset("Alpha").getField("la")))),
            new Lang.StepAttackStep(
                lang.getAsset("Action"),
                lang.getAsset("Action"),
                lang.getAsset("Action").getAttackStep("compromise")));
    assertLangStepExpr(step, attackstep.getReaches().get(0));

    // action
    asset = assertGetLangAsset(lang, "Action", false, "CAT", null, new Lang.Meta());
    // compromise
    attackstep =
        assertGetLangAttackStep(
            asset,
            "compromise",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    step =
        new Lang.StepCollect(
            asset,
            asset,
            null,
            null,
            new Lang.StepIntersection(
                lang.getAsset("Action"),
                lang.getAsset("Action"),
                lang.getAsset("LocalAction"),
                lang.getAsset("LocalAction"),
                new Lang.StepCollect(
                    lang.getAsset("LocalAction"),
                    lang.getAsset("Action"),
                    lang.getAsset("LocalAction"),
                    lang.getAsset("LocalAction"),
                    new Lang.StepField(
                        lang.getAsset("LocalAction"),
                        lang.getAsset("LocalAction"),
                        lang.getAsset("Alpha"),
                        lang.getAsset("Alpha"),
                        lang.getAsset("LocalAction").getField("a")),
                    new Lang.StepField(
                        lang.getAsset("Alpha"),
                        lang.getAsset("Alpha"),
                        lang.getAsset("LocalAction"),
                        lang.getAsset("LocalAction"),
                        lang.getAsset("Alpha").getField("la"))),
                new Lang.StepCollect(
                    lang.getAsset("GlobalAction"),
                    lang.getAsset("Action"),
                    lang.getAsset("LocalAction"),
                    lang.getAsset("LocalAction"),
                    new Lang.StepField(
                        lang.getAsset("GlobalAction"),
                        lang.getAsset("GlobalAction"),
                        lang.getAsset("Alpha"),
                        lang.getAsset("Alpha"),
                        lang.getAsset("GlobalAction").getField("a")),
                    new Lang.StepField(
                        lang.getAsset("Alpha"),
                        lang.getAsset("Alpha"),
                        lang.getAsset("LocalAction"),
                        lang.getAsset("LocalAction"),
                        lang.getAsset("Alpha").getField("la")))),
            new Lang.StepAttackStep(
                lang.getAsset("LocalAction"),
                lang.getAsset("LocalAction"),
                lang.getAsset("LocalAction").getAttackStep("compromise")));
    assertLangStepExpr(step, attackstep.getParentSteps().get(0));
  }

  @Test
  public void testComplexModel() {
    var lang = assertGetLangClassPath("analyzer/complex.mal");
    assertDefines(lang);
    assertCategories(lang);
    assertAssets(lang);
    assertLinks(lang);
  }

  private static void assertDefines(Lang lang) {
    assertLangDefines(
        Map.ofEntries(Map.entry("id", "complex"), Map.entry("version", "1.0.0")),
        lang.getDefines());
  }

  private static void assertCategories(Lang lang) {
    assertEquals(2, lang.getCategories().size());
    assertLangCategory(
        lang, "Person", new String[] {"User", "Student", "Teacher"}, new Lang.Meta());
    assertLangCategory(
        lang,
        "Hardware",
        new String[] {"Computer", "Firewall", "Harddrive", "SecretFolder"},
        new Lang.Meta());
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

  private static void assertAssetUser(Lang lang) {
    var asset = assertGetLangAsset(lang, "User", true, "Person", null, new Lang.Meta());
    // Check fields
    assertEquals(2, asset.getFields().size());
    assertLangField(asset, "firewall", 1, 1);
    assertLangField(asset, "computer", 1, 1);
    // Check attack steps
    assertEquals(4, asset.getAttackSteps().size());

    // Check attack step "impersonate"
    var attackStep =
        assertGetLangAttackStep(
            asset,
            "impersonate",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of("hidden"));
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(2, reaches.size());
    assertEquals(2, parentSteps.size());

    // (Reaches) -> compromise
    Lang.StepExpr step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("compromise"));
    assertLangStepExpr(step, reaches.get(0));

    // (Reaches) -> stealInformation
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("stealInformation"));
    assertLangStepExpr(step, reaches.get(1));

    // (Parent) -> computer.retrievePassword
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

    // (Parent) -> [Teacher]firewall.bypassFirewall
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
        assertGetLangAttackStep(
            asset,
            "compromise",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(2, parentSteps.size());

    // (Reaches) -> computer.stealSecret
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

    // (Parent) -> impersonate
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("impersonate"));
    assertLangStepExpr(step, parentSteps.get(0));

    // (Parent) -> [Teacher]accessSchoolComputer
    step =
        new Lang.StepAttackStep(
            lang.getAsset("Teacher"),
            asset,
            lang.getAsset("Teacher").getAttackStep("accessSchoolComputer"));
    assertLangStepExpr(step, parentSteps.get(1));

    // Check attack step "stealInformation"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "stealInformation",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(1, parentSteps.size());

    // (Reaches) -> computer.internalHD.stealHDSecrets
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

    // (Parent) -> impersonate
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("impersonate"));
    assertLangStepExpr(step, parentSteps.get(0));

    // Check attack step "stealFolder"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "stealFolder",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // (Reaches) -> computer.(externalHD.stealFolder)
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
  }

  private static void assertAssetStudent(Lang lang) {
    var asset = assertGetLangAsset(lang, "Student", false, "Person", "User", new Lang.Meta());
    // Check fields
    assertEquals(1, asset.getFields().size());
    assertLangField(asset, "studentComputer", 1, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(0, asset.getAttackSteps().size());
  }

  private static void assertAssetTeacher(Lang lang) {
    var asset = assertGetLangAsset(lang, "Teacher", false, "Person", "User", new Lang.Meta());
    // Check fields
    assertEquals(1, asset.getFields().size());
    assertLangField(asset, "teacherComputer", 1, 1);
    // Check attack steps
    assertEquals(1, asset.getAttackSteps().size());

    // Check attack step "accessSchoolComputer"
    var attackStep =
        assertGetLangAttackStep(
            asset,
            "accessSchoolComputer",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta()
                .setInfo(
                    "An extra level of protection, their school computer must be used to impersonate them."));
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // (Reaches) -> compromise
    Lang.StepExpr step =
        new Lang.StepAttackStep(asset, lang.getAsset("User"), asset.getAttackStep("compromise"));
    assertLangStepExpr(step, reaches.get(0));
  }

  private static void assertAssetComputer(Lang lang) {
    var asset = assertGetLangAsset(lang, "Computer", false, "Hardware", null, new Lang.Meta());
    // Check fields
    assertEquals(6, asset.getFields().size());
    assertLangField(asset, "student", 1, 1);
    assertLangField(asset, "teacher", 1, 1);
    assertLangField(asset, "firewall", 1, 1);
    assertLangField(asset, "user", 1, 1);
    assertLangField(asset, "externalHD", 1, 1);
    assertLangField(asset, "internalHD", 1, 1);
    // Check attack steps
    assertEquals(7, asset.getAttackSteps().size());

    // Check attack step "malwareInfection"
    var attackStep =
        assertGetLangAttackStep(
            asset,
            "malwareInfection",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // (Reaches) -> interceptTraffic
    Lang.StepExpr step =
        new Lang.StepAttackStep(asset, asset, asset.getAttackStep("interceptTraffic"));
    assertLangStepExpr(step, reaches.get(0));

    // Check attack step "interceptTraffic"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "interceptTraffic",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(2, parentSteps.size());

    // (Reaches) -> retrievePassword
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("retrievePassword"));
    assertLangStepExpr(step, reaches.get(0));

    // (Parent) -> malwareInfection
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("malwareInfection"));
    assertLangStepExpr(step, parentSteps.get(0));

    // (Parent) -> firewall.bypassFirewall
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
        assertGetLangAttackStep(
            asset,
            "retrievePassword",
            Lang.AttackStepType.ALL,
            false,
            false,
            false,
            false,
            new Lang.Meta()
                .setInfo("Retrieval of password is only possible if password is unencrypted"));
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(3, parentSteps.size());

    // (Reaches) -> user.impersonate
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

    // (Parent) -> interceptTraffic
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("interceptTraffic"));
    assertLangStepExpr(step, parentSteps.get(0));

    // (Parent) -> passwordEncrypted
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("passwordEncrypted"));
    assertLangStepExpr(step, parentSteps.get(1));

    // (Parent) -> firewall.bypassFirewall
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
        assertGetLangAttackStep(
            asset,
            "bypassFirewall",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    // ExponentialDistribution(0.05) * GammaDistribution(1.2, 1.7)
    assertLangTTC(
        attackStep,
        new Lang.TTCMul(
            new Lang.TTCFunc(new Distributions.Exponential(0.05)),
            new Lang.TTCFunc(new Distributions.Gamma(1.2, 1.7))));

    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // (Reaches) -> firewall.bypassFirewall
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

    // Check attack step "stealSecret"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "stealSecret",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(1, parentSteps.size());

    // (Reaches) -> (externalHD \/ internalHD).stealHDSecrets
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

    // (Parent) -> user.compromise
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
        assertGetLangAttackStep(
            asset,
            "passwordEncrypted",
            Lang.AttackStepType.DEFENSE,
            false,
            true,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // (Reaches) -> retrievePassword
    step = new Lang.StepAttackStep(asset, asset, asset.getAttackStep("retrievePassword"));
    assertLangStepExpr(step, reaches.get(0));

    // Check attack step "firewallProtected"
    attackStep =
        assertGetLangAttackStep(
            asset,
            "firewallProtected",
            Lang.AttackStepType.EXIST,
            false,
            false,
            true,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(1, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(0, parentSteps.size());

    // (Requires) <- firewall
    step =
        new Lang.StepField(
            asset,
            asset,
            lang.getAsset("Firewall"),
            lang.getAsset("Firewall"),
            asset.getField("firewall"));
    assertLangStepExpr(step, requires.get(0));

    // (Reaches) -> firewall.bypassFirewall
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
  }

  private static void assertAssetFirewall(Lang lang) {
    var asset = assertGetLangAsset(lang, "Firewall", false, "Hardware", null, new Lang.Meta());
    // Check fields
    assertEquals(2, asset.getFields().size());
    assertLangField(asset, "computer", 0, Integer.MAX_VALUE);
    assertLangField(asset, "user", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(1, asset.getAttackSteps().size());

    // Check attack step "bypassFirewall"
    var attackStep =
        assertGetLangAttackStep(
            asset,
            "bypassFirewall",
            Lang.AttackStepType.ALL,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(3, reaches.size());
    assertEquals(2, parentSteps.size());

    // (Reaches) -> computer.retrievePassword
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

    // (Reaches) -> computer.interceptTraffic
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

    // (Reaches) -> user[Teacher].impersonate
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

    // (Parent) -> computer.bypassFirewall
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

    // (Parent) -> computer.firewallProtected
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
    var asset = assertGetLangAsset(lang, "Harddrive", false, "Hardware", null, new Lang.Meta());
    // Check fields
    assertEquals(3, asset.getFields().size());
    assertLangField(asset, "extHDComputer", 1, 1);
    assertLangField(asset, "intHDComputer", 1, 1);
    assertLangField(asset, "folder", 0, Integer.MAX_VALUE);
    // Check attack steps
    assertEquals(2, asset.getAttackSteps().size());

    // Check attack step "stealHDSecrets"
    var attackStep =
        assertGetLangAttackStep(
            asset,
            "stealHDSecrets",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(0, reaches.size());
    assertEquals(2, parentSteps.size());

    // (Parent) -> intHDComputer.user.stealInformation
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

    // (Parent) -> (intHDComputer \/ extHDComputer).stealSecret
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
        assertGetLangAttackStep(
            asset,
            "stealFolder",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    requires = attackStep.getRequires();
    reaches = attackStep.getReaches();
    parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(1, reaches.size());
    assertEquals(1, parentSteps.size());

    // (Reaches) -> ((folder.(subFolder)*).accessFolder)
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

    // (Parent) -> extHDComputer.user.stealFolder
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
    var asset = assertGetLangAsset(lang, "SecretFolder", false, "Hardware", null, new Lang.Meta());
    // Check fields
    assertEquals(3, asset.getFields().size());
    assertLangField(asset, "internalHD", 1, 1);
    assertLangField(asset, "subFolder", 0, Integer.MAX_VALUE);
    assertLangField(asset, "folder", 1, 1);
    // Check attack steps
    assertEquals(1, asset.getAttackSteps().size());

    // Check attack step "accessFolder"
    var attackStep =
        assertGetLangAttackStep(
            asset,
            "accessFolder",
            Lang.AttackStepType.ANY,
            false,
            false,
            false,
            false,
            new Lang.Meta());
    assertLangTags(attackStep, List.of());
    assertLangCIA(attackStep, null);
    assertLangTTC(attackStep, null);
    var requires = attackStep.getRequires();
    var reaches = attackStep.getReaches();
    var parentSteps = attackStep.getParentSteps();
    assertEquals(0, requires.size());
    assertEquals(0, reaches.size());
    assertEquals(1, parentSteps.size());

    // (Parent) -> folder*.internalHD.stealFolder
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

  private static void assertLinks(Lang lang) {
    assertEquals(9, lang.getLinks().size());
    assertLangLink(
        lang, "Computer", "student", "Student", "studentComputer", 0, "Use", new Lang.Meta());
    assertLangLink(
        lang, "Computer", "teacher", "Teacher", "teacherComputer", 1, "Use", new Lang.Meta());
    assertLangLink(
        lang, "Computer", "firewall", "Firewall", "computer", 2, "Protect", new Lang.Meta());
    assertLangLink(lang, "Firewall", "user", "User", "firewall", 3, "Protect", new Lang.Meta());
    assertLangLink(lang, "Computer", "user", "User", "computer", 4, "Storage", new Lang.Meta());
    assertLangLink(
        lang, "Computer", "externalHD", "Harddrive", "extHDComputer", 5, "Use", new Lang.Meta());
    assertLangLink(
        lang,
        "Computer",
        "internalHD",
        "Harddrive",
        "intHDComputer",
        6,
        "Contain",
        new Lang.Meta());
    assertLangLink(
        lang, "Harddrive", "folder", "SecretFolder", "internalHD", 7, "Contain", new Lang.Meta());
    assertLangLink(
        lang, "SecretFolder", "subFolder", "SecretFolder", "folder", 8, "Contain", new Lang.Meta());
  }
}

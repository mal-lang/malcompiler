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
package org.mal_lang.compiler.lib;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class LangConverter {
  private MalLogger LOGGER;
  private Map<String, List<AST.Category>> astCategories = new LinkedHashMap<>();
  private List<AST.Association> astAssociations = new ArrayList<>();
  private Map<String, String> astDefines = new LinkedHashMap<>();

  private LangConverter(AST ast, boolean verbose, boolean debug) {
    Locale.setDefault(Locale.ROOT);
    LOGGER = new MalLogger("LANG_CONVERTER", verbose, debug);
    // Collect categories
    var allAstCategories = ast.getCategories();
    for (var astCategory : allAstCategories) {
      this.astCategories.put(astCategory.name.id, new ArrayList<>());
    }
    for (var astCategory : allAstCategories) {
      this.astCategories.get(astCategory.name.id).add(astCategory);
    }
    // Collect associations
    for (var astAssociation : ast.getAssociations()) {
      this.astAssociations.add(astAssociation);
    }
    // Collect defines
    for (var astDefine : ast.getDefines()) {
      this.astDefines.put(astDefine.key.id, astDefine.value);
    }
  }

  public static Lang convert(AST ast) {
    return convert(ast, false, false);
  }

  public static Lang convert(AST ast, boolean verbose, boolean debug) {
    return new LangConverter(ast, verbose, debug).convertLog();
  }

  private Lang convertLog() {
    var lang = _convert();
    LOGGER.print();
    return lang;
  }

  private Lang _convert() {
    // Define structures for lang
    var langDefines = this.astDefines;
    var langCategories = new LinkedHashMap<String, Lang.Category>();
    var langAssets = new LinkedHashMap<String, Lang.Asset>();
    var langLinks = new ArrayList<Lang.Link>();

    // Add categories to lang
    for (var categoryName : this.astCategories.keySet()) {
      var langCategory = new Lang.Category(categoryName);
      langCategories.put(categoryName, langCategory);
      for (var astCategory : this.astCategories.get(categoryName)) {
        _convertMetaList(langCategory.getMeta(), astCategory.meta);
      }
    }

    // Add assets to lang and categories
    for (var categoryName : this.astCategories.keySet()) {
      var langCategory = langCategories.get(categoryName);
      for (var astCategory : this.astCategories.get(categoryName)) {
        for (var astAsset : astCategory.assets) {
          var assetName = astAsset.name.id;
          var langAsset = new Lang.Asset(assetName, astAsset.isAbstract, langCategory);
          _convertMetaList(langAsset.getMeta(), astAsset.meta);
          langCategory.addAsset(langAsset);
          langAssets.put(assetName, langAsset);
        }
      }
    }

    // Add super assets to assets
    for (var categoryName : this.astCategories.keySet()) {
      for (var astCategory : this.astCategories.get(categoryName)) {
        for (var astAsset : astCategory.assets) {
          var assetName = astAsset.name.id;
          var langAsset = langAssets.get(assetName);
          if (astAsset.parent.isPresent()) {
            var superAssetName = astAsset.parent.get().id;
            langAsset.setSuperAsset(langAssets.get(superAssetName));
          }
        }
      }
    }

    // Add links to lang and fields to assets
    for (var astAssociation : this.astAssociations) {
      // Create link
      var langLink = new Lang.Link(astAssociation.linkName.id);
      _convertMetaList(langLink.getMeta(), astAssociation.meta);
      // Create left field
      var leftAsset = langAssets.get(astAssociation.leftAsset.id);
      var leftField = new Lang.Field(astAssociation.rightField.id, leftAsset, langLink);
      _convertMultiplicity(leftField, astAssociation.rightMult);
      leftAsset.addField(leftField);
      // Create right field
      var rightAsset = langAssets.get(astAssociation.rightAsset.id);
      var rightField = new Lang.Field(astAssociation.leftField.id, rightAsset, langLink);
      _convertMultiplicity(rightField, astAssociation.leftMult);
      rightAsset.addField(rightField);
      // Create references
      leftField.setTarget(rightField);
      rightField.setTarget(leftField);
      langLink.setLeftField(leftField);
      langLink.setRightField(rightField);
      langLinks.add(langLink);
    }

    // Add attack steps to assets
    for (var categoryName : this.astCategories.keySet()) {
      for (var astCategory : this.astCategories.get(categoryName)) {
        for (var astAsset : astCategory.assets) {
          var assetName = astAsset.name.id;
          var langAsset = langAssets.get(assetName);
          for (var astAttackStep : astAsset.attackSteps) {
            var langAttackStepType = _convertAttackStepType(astAttackStep.type);
            var inheritsReaches = _convertInheritsReaches(astAttackStep);
            var langAttackStep =
                new Lang.AttackStep(
                    astAttackStep.name.id,
                    langAttackStepType,
                    langAsset,
                    inheritsReaches,
                    _convertCIA(astAttackStep.cia));
            _convertMetaList(langAttackStep.getMeta(), astAttackStep.meta);
            for (var tag : astAttackStep.tags) {
              langAttackStep.addTag(tag.id);
            }
            if (astAttackStep.ttc.isPresent()) {
              langAttackStep.setTTC(_convertTTC(astAttackStep.ttc.get()));
            }
            langAsset.addAttackStep(langAttackStep);
          }
        }
      }
    }

    // Create asset var hashmap
    var assetVars = new LinkedHashMap<String, Map<String, AST.Variable>>();
    for (var categoryName : this.astCategories.keySet()) {
      for (var astCategory : this.astCategories.get(categoryName)) {
        for (var astAsset : astCategory.assets) {
          var variables = new LinkedHashMap<String, AST.Variable>();
          for (var astVariable : astAsset.variables) {
            variables.put(astVariable.name.id, astVariable);
          }
          assetVars.put(astAsset.name.id, variables);
        }
      }
    }

    // Add requires and reaches to attack steps
    for (var categoryName : this.astCategories.keySet()) {
      for (var astCategory : this.astCategories.get(categoryName)) {
        for (var astAsset : astCategory.assets) {
          var assetName = astAsset.name.id;
          var langAsset = langAssets.get(assetName);
          for (var astAttackStep : astAsset.attackSteps) {
            var attackStepName = astAttackStep.name.id;
            var langAttackStep = langAsset.getAttackStep(attackStepName);
            if (astAttackStep.requires.isPresent()) {
              _convertRequires(langAttackStep, langAssets, astAttackStep.requires.get(), assetVars);
            }
            if (astAttackStep.reaches.isPresent()) {
              _convertReaches(langAttackStep, langAssets, astAttackStep.reaches.get(), assetVars);
            }
          }
        }
      }
    }

    // Add parent steps to attack steps
    for (var asset : langAssets.values()) {
      for (var attackStep : asset.getAttackSteps().values()) {
        for (var reaches : attackStep.getReaches()) {
          _convertReverseStep(attackStep, reaches);
        }
      }
    }

    return new Lang(langDefines, langCategories, langAssets, langLinks);
  }

  private void _convertMetaList(Map<String, String> meta, List<AST.Meta> astMetaList) {
    for (var astMeta : astMetaList) {
      meta.put(astMeta.type.id, astMeta.string);
    }
  }

  private void _convertMultiplicity(Lang.Field field, AST.Multiplicity astMultiplicity) {
    switch (astMultiplicity) {
      case ZERO_OR_ONE:
        field.setMin(0);
        field.setMax(1);
        break;
      case ZERO_OR_MORE:
        field.setMin(0);
        field.setMax(Integer.MAX_VALUE);
        break;
      case ONE:
        field.setMin(1);
        field.setMax(1);
        break;
      case ONE_OR_MORE:
        field.setMin(1);
        field.setMax(Integer.MAX_VALUE);
        break;
    }
  }

  private Lang.AttackStepType _convertAttackStepType(AST.AttackStepType astType) {
    switch (astType) {
      case ALL:
        return Lang.AttackStepType.ALL;
      case ANY:
        return Lang.AttackStepType.ANY;
      case DEFENSE:
        return Lang.AttackStepType.DEFENSE;
      case EXIST:
        return Lang.AttackStepType.EXIST;
      case NOTEXIST:
        return Lang.AttackStepType.NOTEXIST;
    }
    throw new RuntimeException("Invalid AttackStepType");
  }

  private boolean _convertInheritsReaches(AST.AttackStep astAttackStep) {
    if (astAttackStep.reaches.isPresent()) {
      return astAttackStep.reaches.get().inherits;
    } else {
      return false;
    }
  }

  private Lang.CIA _convertCIA(Optional<List<AST.CIA>> astCIA) {
    if (astCIA.isEmpty()) {
      return null;
    } else {
      boolean C = false;
      boolean I = false;
      boolean A = false;
      for (var cia : astCIA.get()) {
        switch (cia) {
          case C:
            C = true;
            break;
          case I:
            I = true;
            break;
          case A:
            A = true;
            break;
        }
      }
      return new Lang.CIA(C, I, A);
    }
  }

  private Lang.TTCExpr _convertTTC(AST.TTCExpr astTTC) {
    if (astTTC instanceof AST.TTCAddExpr) {
      var astAdd = (AST.TTCAddExpr) astTTC;
      return new Lang.TTCAdd(_convertTTC(astAdd.lhs), _convertTTC(astAdd.rhs));
    } else if (astTTC instanceof AST.TTCSubExpr) {
      var astSub = (AST.TTCSubExpr) astTTC;
      return new Lang.TTCSub(_convertTTC(astSub.lhs), _convertTTC(astSub.rhs));
    } else if (astTTC instanceof AST.TTCMulExpr) {
      var astMul = (AST.TTCMulExpr) astTTC;
      return new Lang.TTCMul(_convertTTC(astMul.lhs), _convertTTC(astMul.rhs));
    } else if (astTTC instanceof AST.TTCDivExpr) {
      var astDiv = (AST.TTCDivExpr) astTTC;
      return new Lang.TTCDiv(_convertTTC(astDiv.lhs), _convertTTC(astDiv.rhs));
    } else if (astTTC instanceof AST.TTCPowExpr) {
      var astPow = (AST.TTCPowExpr) astTTC;
      return new Lang.TTCPow(_convertTTC(astPow.lhs), _convertTTC(astPow.rhs));
    } else if (astTTC instanceof AST.TTCNumExpr) {
      var astNum = (AST.TTCNumExpr) astTTC;
      return new Lang.TTCNum(astNum.value);
    } else if (astTTC instanceof AST.TTCFuncExpr) {
      var astFunc = (AST.TTCFuncExpr) astTTC;
      var dist = Distributions.getDistribution(astFunc.name.id, astFunc.params);
      return new Lang.TTCFunc(dist);
    }
    throw new RuntimeException("_convertTTC: Invalid AST.TTCExpr subtype");
  }

  private void _convertRequires(
      Lang.AttackStep langAttackStep,
      Map<String, Lang.Asset> assets,
      AST.Requires astRequires,
      Map<String, Map<String, AST.Variable>> assetVars) {
    for (var astExpr : astRequires.requires) {
      langAttackStep.addRequires(
          _convertExprToAsset(astExpr, langAttackStep.getAsset(), assets, assetVars));
    }
  }

  private void _convertReaches(
      Lang.AttackStep langAttackStep,
      Map<String, Lang.Asset> assets,
      AST.Reaches astReaches,
      Map<String, Map<String, AST.Variable>> assetVars) {
    for (var astExpr : astReaches.reaches) {
      langAttackStep.addReaches(
          _convertExprToAttackStep(astExpr, langAttackStep.getAsset(), assets, assetVars));
    }
  }

  private static boolean isSubTypeOf(Lang.Asset a1, Lang.Asset a2) {
    if (a1 == a2) {
      return true;
    } else if (!a1.hasSuperAsset()) {
      return false;
    } else {
      return isSubTypeOf(a1.getSuperAsset(), a2);
    }
  }

  private static Lang.Asset leastUpperBound(Lang.Asset a1, Lang.Asset a2) {
    if (isSubTypeOf(a1, a2)) {
      return a2;
    } else if (isSubTypeOf(a2, a1)) {
      return a1;
    } else if (!a1.hasSuperAsset() && !a2.hasSuperAsset()) {
      return null;
    } else {
      var a1Next = a1.hasSuperAsset() ? a1.getSuperAsset() : a1;
      var a2Next = a2.hasSuperAsset() ? a2.getSuperAsset() : a2;
      return leastUpperBound(a1Next, a2Next);
    }
  }

  private Lang.StepExpr _convertExprToAsset(
      AST.Expr expr,
      Lang.Asset asset,
      Map<String, Lang.Asset> assets,
      Map<String, Map<String, AST.Variable>> assetVars) {
    return _convertExprToAsset(expr, asset, assets, null, assetVars);
  }

  private Lang.StepExpr _convertExprToAsset(
      AST.Expr expr,
      Lang.Asset asset,
      Map<String, Lang.Asset> assets,
      Lang.Asset subTarget,
      Map<String, Map<String, AST.Variable>> assetVars) {
    if (expr instanceof AST.UnionExpr) {
      var unionExpr = (AST.UnionExpr) expr;
      var lhs = _convertExprToAsset(unionExpr.lhs, asset, assets, assetVars);
      var rhs = _convertExprToAsset(unionExpr.rhs, asset, assets, assetVars);
      var target = leastUpperBound(lhs.subTarget, rhs.subTarget);
      return new Lang.StepUnion(
          asset, asset, target, subTarget == null ? target : subTarget, lhs, rhs);
    } else if (expr instanceof AST.IntersectionExpr) {
      var intersectionExpr = (AST.IntersectionExpr) expr;
      var lhs = _convertExprToAsset(intersectionExpr.lhs, asset, assets, assetVars);
      var rhs = _convertExprToAsset(intersectionExpr.rhs, asset, assets, assetVars);
      var target = leastUpperBound(lhs.subTarget, rhs.subTarget);
      return new Lang.StepIntersection(
          asset, asset, target, subTarget == null ? target : subTarget, lhs, rhs);
    } else if (expr instanceof AST.DifferenceExpr) {
      var differenceExpr = (AST.DifferenceExpr) expr;
      var lhs = _convertExprToAsset(differenceExpr.lhs, asset, assets, assetVars);
      var rhs = _convertExprToAsset(differenceExpr.rhs, asset, assets, assetVars);
      var target = leastUpperBound(lhs.subTarget, rhs.subTarget);
      return new Lang.StepDifference(
          asset, asset, target, subTarget == null ? target : subTarget, lhs, rhs);
    } else if (expr instanceof AST.StepExpr) {
      var stepExpr = (AST.StepExpr) expr;
      var lhs = _convertExprToAsset(stepExpr.lhs, asset, assets, assetVars);
      var rhs = _convertExprToAsset(stepExpr.rhs, lhs.subTarget, assets, assetVars);
      return new Lang.StepCollect(
          asset, asset, rhs.subTarget, subTarget == null ? rhs.subTarget : subTarget, lhs, rhs);
    } else if (expr instanceof AST.TransitiveExpr) {
      var transitiveExpr = (AST.TransitiveExpr) expr;
      var e = _convertExprToAsset(transitiveExpr.e, asset, assets, assetVars);
      return new Lang.StepTransitive(
          asset, asset, e.subTarget, subTarget == null ? e.subTarget : subTarget, e);
    } else if (expr instanceof AST.SubTypeExpr) {
      var subTypeExpr = (AST.SubTypeExpr) expr;
      var subType = assets.get(subTypeExpr.subType.id);
      return _convertExprToAsset(subTypeExpr.e, asset, assets, subType, assetVars);
    } else if (expr instanceof AST.IDExpr) {
      var idExpr = (AST.IDExpr) expr;
      var field = asset.getField(idExpr.id.id);
      var target = field.getTarget().getAsset();
      return new Lang.StepField(
          asset, field.getAsset(), target, subTarget == null ? target : subTarget, field);
    } else if (expr instanceof AST.CallExpr) {
      var varExpr = (AST.CallExpr) expr;
      String callName = String.format("%s%s", varExpr.id.id, asset.getName());
      var expression = asset.getVariables().get(varExpr.id.id);
      if (expression == null) {
        AST.Variable astVar = assetVars.get(asset.getName()).get(varExpr.id.id);
        var parent = asset;
        while (astVar == null) {
          parent = parent.getSuperAsset();
          astVar = assetVars.get(parent.getName()).get(varExpr.id.id);
        }
        expression = parent.getVariables().get(varExpr.id.id);
        if (expression == null) {
          expression = _convertExprToAsset(astVar.expr, parent, assets, assetVars);
          parent.addVariable(callName, expression);
          var reverse = reverseStep(expression, expression.subTarget);
          expression.subTarget.addReverseVariable(String.format("reverse%s", callName), reverse);
        }
      }

      return new Lang.StepCall(
          asset,
          expression.src,
          expression.target,
          subTarget == null ? expression.subTarget : subTarget,
          callName);
    }
    throw new RuntimeException("_convertExprToAsset: Invalid AST.Expr subtype");
  }

  private Lang.StepExpr _convertExprToAttackStep(
      AST.Expr expr,
      Lang.Asset asset,
      Map<String, Lang.Asset> assets,
      Map<String, Map<String, AST.Variable>> assetVars) {
    if (expr instanceof AST.StepExpr) {
      var stepExpr = (AST.StepExpr) expr;
      var lhs = _convertExprToAsset(stepExpr.lhs, asset, assets, assetVars);
      var rhs = _convertExprToAttackStep(stepExpr.rhs, lhs.subTarget, assets, assetVars);
      return new Lang.StepCollect(asset, asset, null, null, lhs, rhs);
    } else if (expr instanceof AST.IDExpr) {
      var idExpr = (AST.IDExpr) expr;
      var attStep = asset.getAttackStep(idExpr.id.id);
      return new Lang.StepAttackStep(asset, attStep.getAsset(), attStep);
    }
    throw new RuntimeException("_convertExprToAttackStep: Invalid AST.Expr subtype");
  }

  /**
   * Returns the final attack step of a step expression.
   *
   * @param step A 'reaches' step expression
   * @return the final attack step of the input step expression
   */
  private static Lang.StepAttackStep getTargetStepAttackStep(Lang.StepExpr step) {
    if (step instanceof Lang.StepCollect) {
      return getTargetStepAttackStep(((Lang.StepCollect) step).rhs);
    } else if (step instanceof Lang.StepAttackStep) {
      return (Lang.StepAttackStep) step;
    }
    throw new RuntimeException("getTargetAttackStep: Invalid Lang.StepExpr subtype");
  }

  /**
   * Removes the final attack step from a step expression.
   *
   * @param step A 'reaches' step expression (that is not a Lang.StepAttackStep)
   * @return the input step expression with the final attack step removed
   */
  private static Lang.StepExpr removeStepAttackStep(Lang.StepExpr step) {
    if (step instanceof Lang.StepCollect) {
      var stepCollect = (Lang.StepCollect) step;
      if (stepCollect.rhs instanceof Lang.StepAttackStep) {
        return stepCollect.lhs;
      } else {
        var newRhs = removeStepAttackStep(stepCollect.rhs);
        return new Lang.StepCollect(
            step.subSrc, step.src, newRhs.target, newRhs.subTarget, stepCollect.lhs, newRhs);
      }
    }
    throw new RuntimeException("removeStepAttackStep: Invalid Lang.StepExpr subtype");
  }

  /**
   * Reverses a step expression.
   *
   * @param step A 'reaches' step expression with the final attack step removed
   * @return the input step expression reversed
   */
  private static Lang.StepExpr reverseStep(Lang.StepExpr step, Lang.Asset src) {
    if (step instanceof Lang.StepUnion) {
      var stepUnion = (Lang.StepUnion) step;
      return new Lang.StepUnion(
          step.subTarget,
          src == null ? step.target : src,
          step.src,
          step.subSrc,
          reverseStep(stepUnion.rhs, step.subTarget),
          reverseStep(stepUnion.lhs, step.subTarget));
    } else if (step instanceof Lang.StepIntersection) {
      var stepIntersection = (Lang.StepIntersection) step;
      return new Lang.StepIntersection(
          step.subTarget,
          src == null ? step.target : src,
          step.src,
          step.subSrc,
          reverseStep(stepIntersection.rhs, step.subTarget),
          reverseStep(stepIntersection.lhs, step.subTarget));
    } else if (step instanceof Lang.StepDifference) {
      var stepDifference = (Lang.StepDifference) step;
      return new Lang.StepDifference(
          step.subTarget,
          src == null ? step.target : src,
          step.src,
          step.subSrc,
          reverseStep(stepDifference.rhs, step.subTarget),
          reverseStep(stepDifference.lhs, step.subTarget));
    } else if (step instanceof Lang.StepCollect) {
      var stepCollect = (Lang.StepCollect) step;
      return new Lang.StepCollect(
          step.subTarget,
          src == null ? step.target : src,
          step.src,
          step.subSrc,
          reverseStep(stepCollect.rhs, null),
          reverseStep(stepCollect.lhs, null));
    } else if (step instanceof Lang.StepTransitive) {
      var stepTransitive = (Lang.StepTransitive) step;
      return new Lang.StepTransitive(
          step.subTarget,
          src == null ? step.subTarget : src,
          step.src,
          step.subSrc,
          reverseStep(stepTransitive.e, null));
    } else if (step instanceof Lang.StepField) {
      var stepField = (Lang.StepField) step;
      return new Lang.StepField(
          step.subTarget,
          src == null ? step.target : src,
          step.src,
          step.subSrc,
          stepField.field.getTarget());
    } else if (step instanceof Lang.StepCall) {
      var stepVar = (Lang.StepCall) step;
      return new Lang.StepCall(
          step.subTarget,
          src == null ? step.subTarget : src,
          step.src,
          step.subSrc,
          String.format("reverse%s", stepVar.name));
    }
    throw new RuntimeException("reverseStep: Invalid Lang.StepExpr subtype");
  }

  /**
   * Reverses a 'reaches' step expression and adds it as a parent step to its target attack step.
   *
   * @param attackStep the attack step containing the 'reaches' attack step
   * @param step the 'reaches' attack step to reverse
   */
  private void _convertReverseStep(Lang.AttackStep attackStep, Lang.StepExpr step) {
    var targetStepAttackStep = getTargetStepAttackStep(step);
    var targetAttackStep = targetStepAttackStep.attackStep;
    if (step instanceof Lang.StepAttackStep) {
      targetAttackStep.addParentStep(
          new Lang.StepAttackStep(
              targetStepAttackStep.subSrc, targetAttackStep.getAsset(), attackStep));
    } else {
      var strippedStep = removeStepAttackStep(step);
      var reversedStep = reverseStep(strippedStep, null);
      var newStep =
          new Lang.StepCollect(
              targetStepAttackStep.subSrc,
              targetAttackStep.getAsset(),
              null,
              null,
              reversedStep,
              new Lang.StepAttackStep(reversedStep.subTarget, reversedStep.subTarget, attackStep));
      targetAttackStep.addParentStep(newStep);
    }
  }
}

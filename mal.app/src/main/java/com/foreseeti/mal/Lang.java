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

import com.foreseeti.mal.Distributions.Distribution;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Lang {
  private Map<String, String> defines;
  private Map<String, Category> categories;
  private Map<String, Asset> assets;
  private List<Link> links;

  public Lang(
      Map<String, String> defines,
      Map<String, Category> categories,
      Map<String, Asset> assets,
      List<Link> links) {
    this.defines = defines;
    this.categories = categories;
    this.assets = assets;
    this.links = links;
  }

  public Map<String, String> getDefines() {
    var copy = new LinkedHashMap<String, String>();
    for (var entry : this.defines.entrySet()) {
      copy.put(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  public String getDefine(String key) {
    return this.defines.get(key);
  }

  public Map<String, Category> getCategories() {
    var copy = new LinkedHashMap<String, Category>();
    for (var entry : this.categories.entrySet()) {
      copy.put(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  public Category getCategory(String name) {
    return this.categories.get(name);
  }

  public Map<String, Asset> getAssets() {
    var copy = new LinkedHashMap<String, Asset>();
    for (var entry : this.assets.entrySet()) {
      copy.put(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  public Asset getAsset(String name) {
    return this.assets.get(name);
  }

  public List<Link> getLinks() {
    return List.copyOf(this.links);
  }

  public static class Meta {
    private String info;
    private String assumptions;
    private String rationale;

    public String getInfo() {
      return this.info;
    }

    public Meta setInfo(String info) {
      this.info = info;
      return this;
    }

    public String getAssumptions() {
      return this.assumptions;
    }

    public Meta setAssumptions(String assumptions) {
      this.assumptions = assumptions;
      return this;
    }

    public String getRationale() {
      return this.rationale;
    }

    public Meta setRationale(String rationale) {
      this.rationale = rationale;
      return this;
    }
  }

  public static class Category {
    private String name;
    private Meta meta;
    private Map<String, Asset> assets;

    public Category(String name) {
      this.name = name;
      this.meta = new Meta();
      this.assets = new LinkedHashMap<>();
    }

    public String getName() {
      return this.name;
    }

    public Meta getMeta() {
      return this.meta;
    }

    public Map<String, Asset> getAssets() {
      var copy = new LinkedHashMap<String, Asset>();
      for (var entry : this.assets.entrySet()) {
        copy.put(entry.getKey(), entry.getValue());
      }
      return copy;
    }

    public Asset getAsset(String name) {
      return this.assets.get(name);
    }

    public void addAsset(Asset asset) {
      this.assets.put(asset.getName(), asset);
    }
  }

  public static class Asset {
    private String name;
    private boolean isAbstract;
    private Category category;
    private Meta meta;
    private Asset superAsset;
    private Map<String, Field> fields;
    private Map<String, AttackStep> attackSteps;

    public Asset(String name, boolean isAbstract, Category category) {
      this.name = name;
      this.isAbstract = isAbstract;
      this.category = category;
      this.meta = new Meta();
      this.fields = new LinkedHashMap<>();
      this.attackSteps = new LinkedHashMap<>();
    }

    public String getName() {
      return this.name;
    }

    public boolean isAbstract() {
      return this.isAbstract;
    }

    public Category getCategory() {
      return this.category;
    }

    public Meta getMeta() {
      return this.meta;
    }

    public boolean hasSuperAsset() {
      return this.superAsset != null;
    }

    public Asset getSuperAsset() {
      return this.superAsset;
    }

    public void setSuperAsset(Asset superAsset) {
      this.superAsset = superAsset;
    }

    public Map<String, Field> getFields() {
      var copy = new LinkedHashMap<String, Field>();
      for (var entry : this.fields.entrySet()) {
        copy.put(entry.getKey(), entry.getValue());
      }
      return copy;
    }

    public Field getField(String name) {
      if (this.fields.containsKey(name)) {
        return this.fields.get(name);
      }
      if (this.superAsset != null) {
        return this.superAsset.getField(name);
      }
      return null;
    }

    public void addField(Field field) {
      this.fields.put(field.getName(), field);
    }

    public Map<String, AttackStep> getAttackSteps() {
      var copy = new LinkedHashMap<String, AttackStep>();
      for (var entry : this.attackSteps.entrySet()) {
        copy.put(entry.getKey(), entry.getValue());
      }
      return copy;
    }

    public AttackStep getAttackStep(String name) {
      if (this.attackSteps.containsKey(name)) {
        return this.attackSteps.get(name);
      }
      if (this.superAsset != null) {
        return this.superAsset.getAttackStep(name);
      }
      return null;
    }

    public void addAttackStep(AttackStep attackStep) {
      this.attackSteps.put(attackStep.getName(), attackStep);
    }
  }

  public static class Link {
    private String name;
    private Meta meta;
    private Field leftField;
    private Field rightField;

    public Link(String name) {
      this.name = name;
      this.meta = new Meta();
    }

    public String getName() {
      return this.name;
    }

    public Meta getMeta() {
      return this.meta;
    }

    public Field getLeftField() {
      return this.leftField;
    }

    public void setLeftField(Field leftField) {
      this.leftField = leftField;
    }

    public Field getRightField() {
      return this.rightField;
    }

    public void setRightField(Field rightField) {
      this.rightField = rightField;
    }
  }

  public static class Field {
    private String name;
    private Asset asset;
    private Link link;
    private int min;
    private int max;
    private Field target;

    public Field(String name, Asset asset, Link link) {
      this.name = name;
      this.asset = asset;
      this.link = link;
    }

    public String getName() {
      return this.name;
    }

    public Asset getAsset() {
      return this.asset;
    }

    public Link getLink() {
      return this.link;
    }

    public int getMin() {
      return this.min;
    }

    public void setMin(int min) {
      this.min = min;
    }

    public int getMax() {
      return this.max;
    }

    public void setMax(int max) {
      this.max = max;
    }

    public Field getTarget() {
      return this.target;
    }

    public void setTarget(Field target) {
      this.target = target;
    }
  }

  public enum AttackStepType {
    ALL,
    ANY,
    DEFENSE,
    EXIST,
    NOTEXIST
  }

  public static class AttackStep {
    private String name;
    private AttackStepType type;
    private Asset asset;
    private boolean inheritsReaches;
    private CIA cia;
    private Meta meta;
    private TTCExpr ttc;
    private List<StepExpr> requires;
    private List<StepExpr> reaches;
    private List<StepExpr> parentSteps;

    public AttackStep(
        String name, AttackStepType type, Asset asset, boolean inheritsReaches, CIA cia) {
      this.name = name;
      this.type = type;
      this.asset = asset;
      this.inheritsReaches = inheritsReaches;
      this.cia = cia;
      this.meta = new Meta();
      this.requires = new ArrayList<>();
      this.reaches = new ArrayList<>();
      this.parentSteps = new ArrayList<>();
    }

    public String getName() {
      return this.name;
    }

    public AttackStepType getType() {
      return this.type;
    }

    public Asset getAsset() {
      return this.asset;
    }

    public boolean inheritsReaches() {
      return this.inheritsReaches;
    }

    public Meta getMeta() {
      return this.meta;
    }

    public boolean hasCIA() {
      return this.cia != null;
    }

    public CIA getCIA() {
      return this.cia;
    }

    public boolean hasTTC() {
      return this.ttc != null;
    }

    public TTCExpr getTTC() {
      return this.ttc;
    }

    public void setTTC(TTCExpr ttc) {
      this.ttc = ttc;
    }

    public List<StepExpr> getRequires() {
      return List.copyOf(this.requires);
    }

    public void addRequires(StepExpr expr) {
      this.requires.add(expr);
    }

    public List<StepExpr> getReaches() {
      return List.copyOf(this.reaches);
    }

    public void addReaches(StepExpr expr) {
      this.reaches.add(expr);
    }

    public List<StepExpr> getParentSteps() {
      return List.copyOf(this.parentSteps);
    }

    public void addParentStep(StepExpr expr) {
      this.parentSteps.add(expr);
    }

    public boolean isDefense() {
      return this.type == AttackStepType.DEFENSE;
    }

    public boolean isConditionalDefense() {
      return this.type == AttackStepType.EXIST || this.type == AttackStepType.NOTEXIST;
    }

    public boolean hasParent() {
      return this.asset.hasSuperAsset()
          && this.asset.getSuperAsset().getAttackStep(this.name) != null;
    }
  }

  public static class CIA {
    public final boolean C;
    public final boolean I;
    public final boolean A;

    public CIA(boolean C, boolean I, boolean A) {
      this.C = C;
      this.I = I;
      this.A = A;
    }
  }

  public abstract static class TTCExpr {}

  public abstract static class TTCBinOp extends TTCExpr {
    public final TTCExpr lhs;
    public final TTCExpr rhs;

    public TTCBinOp(TTCExpr lhs, TTCExpr rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }
  }

  public static class TTCAdd extends TTCBinOp {
    public TTCAdd(TTCExpr lhs, TTCExpr rhs) {
      super(lhs, rhs);
    }
  }

  public static class TTCSub extends TTCBinOp {
    public TTCSub(TTCExpr lhs, TTCExpr rhs) {
      super(lhs, rhs);
    }
  }

  public static class TTCMul extends TTCBinOp {
    public TTCMul(TTCExpr lhs, TTCExpr rhs) {
      super(lhs, rhs);
    }
  }

  public static class TTCDiv extends TTCBinOp {
    public TTCDiv(TTCExpr lhs, TTCExpr rhs) {
      super(lhs, rhs);
    }
  }

  public static class TTCPow extends TTCBinOp {
    public TTCPow(TTCExpr lhs, TTCExpr rhs) {
      super(lhs, rhs);
    }
  }

  public static class TTCNum extends TTCExpr {
    public final double value;

    public TTCNum(double value) {
      this.value = value;
    }
  }

  public static class TTCFunc extends TTCExpr {
    public final Distributions.Distribution dist;

    public TTCFunc(Distribution dist) {
      this.dist = dist;
    }
  }

  public abstract static class StepExpr {
    public final Asset subSrc;
    public final Asset src;
    public final Asset target;
    public final Asset subTarget;

    public StepExpr(Asset subSrc, Asset src, Asset target, Asset subTarget) {
      this.subSrc = subSrc;
      this.src = src;
      this.target = target;
      this.subTarget = subTarget;
    }
  }

  public abstract static class StepBinOp extends StepExpr {
    public final StepExpr lhs;
    public final StepExpr rhs;

    public StepBinOp(
        Asset subSrc, Asset src, Asset target, Asset subTarget, StepExpr lhs, StepExpr rhs) {
      super(subSrc, src, target, subTarget);
      this.lhs = lhs;
      this.rhs = rhs;
    }
  }

  public static class StepUnion extends StepBinOp {
    public StepUnion(
        Asset subSrc, Asset src, Asset target, Asset subTarget, StepExpr lhs, StepExpr rhs) {
      super(subSrc, src, target, subTarget, lhs, rhs);
    }
  }

  public static class StepIntersection extends StepBinOp {
    public StepIntersection(
        Asset subSrc, Asset src, Asset target, Asset subTarget, StepExpr lhs, StepExpr rhs) {
      super(subSrc, src, target, subTarget, lhs, rhs);
    }
  }

  public static class StepDifference extends StepBinOp {
    public StepDifference(
        Asset subSrc, Asset src, Asset target, Asset subTarget, StepExpr lhs, StepExpr rhs) {
      super(subSrc, src, target, subTarget, lhs, rhs);
    }
  }

  public static class StepCollect extends StepBinOp {
    public StepCollect(
        Asset subSrc, Asset src, Asset target, Asset subTarget, StepExpr lhs, StepExpr rhs) {
      super(subSrc, src, target, subTarget, lhs, rhs);
    }
  }

  public static class StepTransitive extends StepExpr {
    public final StepExpr e;

    public StepTransitive(Asset subSrc, Asset src, Asset target, Asset subTarget, StepExpr e) {
      super(subSrc, src, target, subTarget);
      this.e = e;
    }
  }

  public static class StepField extends StepExpr {
    public final Field field;

    public StepField(Asset subSrc, Asset src, Asset target, Asset subTarget, Field field) {
      super(subSrc, src, target, subTarget);
      this.field = field;
    }
  }

  public static class StepAttackStep extends StepExpr {
    public final AttackStep attackStep;

    public StepAttackStep(Asset subSrc, Asset src, AttackStep attackStep) {
      super(subSrc, src, null, null);
      this.attackStep = attackStep;
    }
  }
}

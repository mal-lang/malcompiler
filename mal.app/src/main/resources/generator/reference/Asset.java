package core;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Asset {

  public String name;
  public String assetClassName;
  public static List<Asset> allAssets = new ArrayList<>();

  public Asset() {
    this("Anonymous");
  }

  public Asset(String name) {
    this.name = name;
    allAssets.add(this);
  }

  public static Asset getAsset(String assetName) {
    for (Asset asset : allAssets) {
      if (asset.name.equals(assetName)) {
        return asset;
      }
    }
    return null;
  }

  public static Asset getRandomAsset() {
    int size = allAssets.size();
    int item = new Random().nextInt(size);
    int i = 0;
    for (Asset obj : allAssets) {
      if (i == item) return obj;
      i++;
    }
    return null;
  }

  public void prohibitAssociation(String sourceClassName, String roleName, Boolean enforce) {
    if (this.getClass().getName().equals("auto." + sourceClassName)) {
      if (getAssociatedAssetClassName(roleName) != null) {
        assertion(
            sourceClassName
                + "is not allowed to have an association to "
                + roleName
                + " but "
                + this.toString()
                + " seems to not have just that.",
            enforce);
      }
    }
  }

  public void counterPartTypeRequirement(
      String sourceClassName,
      String roleName,
      Set<String> requiredTargetClassName,
      Boolean enforce) {
    if (this.getClass().getName().equals("auto." + sourceClassName)) {
      if (getAssociatedAssetClassName(roleName) == null) {
        assertion(
            "A constraint assumed every "
                + sourceClassName
                + ", including "
                + this.toString()
                + ", would have a "
                + roleName
                + ", but this seem to not have been the case.",
            enforce);
      } else {
        String acutalTargetClassName = this.getAssociatedAssetClassName(roleName);
        if (acutalTargetClassName.equals("")) {
          assertion(
              "Constraint violation: The "
                  + this.getClass().getName()
                  + " required an association with "
                  + requiredTargetClassName
                  + " but found none.",
              enforce);
        } else {
          if (!requiredTargetClassName.contains(acutalTargetClassName)) {
            assertion(
                "Constraint violation: The "
                    + this.getClass().getName()
                    + " required an association with "
                    + requiredTargetClassName
                    + " but found "
                    + acutalTargetClassName
                    + ".",
                enforce);
          }
        }
      }
    }
  }

  // For each asset of type sourceAssetClassName that also has a neighbor named
  // conditionalNeighborRoleName,
  // there must be a cycle of secondROlename.thirdRoleName that contains the
  // source asset.
  public void cycleRequirement(
      String sourceAssetClassName,
      Set<String> conditionalNeighborRoleName,
      Set<String> secondRoleNames,
      Set<String> thirdRoleNames,
      Boolean enforce) {
    if (this.inheritedClassNames().contains(sourceAssetClassName)) {
      if (!this.getAssociatedAssets(conditionalNeighborRoleName).isEmpty()) {
        Set<Asset> targetAssets = new HashSet<>();
        for (Asset firstAsset : this.getAssociatedAssets(conditionalNeighborRoleName)) {
          for (Asset secondAsset : firstAsset.getAssociatedAssets(secondRoleNames)) {
            for (Asset thirdAsset : secondAsset.getAssociatedAssets(thirdRoleNames)) {
              targetAssets.add(thirdAsset);
            }
          }
        }
        try {
          if (!targetAssets.contains(this)) {
            assertion(
                "Constraint violation: "
                    + this.toString()
                    + "."
                    + conditionalNeighborRoleName.toString()
                    + "."
                    + secondRoleNames.toString()
                    + "."
                    + thirdRoleNames.toString()
                    + " should contain "
                    + this.toString()
                    + ", thus creating a cycle.",
                enforce);
          }
        } catch (NullPointerException e) {
          assertion("Model too incomplete to check for constraint.", enforce);
        }
      }
    }
  }

  public void cycleRequirement(
      String sourceAssetClassName,
      Set<String> conditionalNeighborRoleName,
      List<String> navigationPath,
      Boolean enforce) {
    if (this.inheritedClassNames().contains(sourceAssetClassName)) {
      if (!this.getAssociatedAssets(conditionalNeighborRoleName).isEmpty()) {
        Set<Asset> targetAssets = new HashSet<>();
        for (Asset conditionalNeighbor : this.getAssociatedAssets(conditionalNeighborRoleName)) {
          targetAssets.addAll(conditionalNeighbor.getCollection(navigationPath));
        }

        try {
          if (!targetAssets.contains(this)) {
            String errorMessage =
                "Constraint violation: "
                    + this.toString()
                    + "."
                    + conditionalNeighborRoleName.toString();
            for (String roleName : navigationPath) {
              errorMessage += "." + roleName.toString();
            }
            assertion(errorMessage, enforce);
          }
        } catch (NullPointerException e) {
          assertion("Model too incomplete to check for constraint.", enforce);
        }
      }
    }
  }

  public Set<Asset> getCollection(List<String> navigationPath) {
    Set<Asset> sourceAssets = new HashSet<>();
    sourceAssets.add(this);
    Set<Asset> targetAssets = new HashSet<>();
    for (String nextRoleName : navigationPath) {
      targetAssets.clear();
      for (Asset asset : sourceAssets) {
        for (Asset associatedAsset : asset.getAssociatedAssets(nextRoleName)) {
          targetAssets.add(associatedAsset);
        }
      }
      sourceAssets.clear();
      sourceAssets.addAll(targetAssets);
    }
    return targetAssets;
  }

  protected Set<Asset> getAssociatedAssets(Set<String> roleNames) {
    Set<Asset> assets = new HashSet<>();
    for (String firstRoleName : roleNames) {
      assets.addAll(this.getAssociatedAssets(firstRoleName));
    }
    return assets;
  }

  public List<String> inheritedClassNames() {
    List<String> inheritedClassNames = new ArrayList<>();
    inheritedClassNames.add(this.getClass().getName());
    Class<?> superClass = this.getClass().getSuperclass();
    while (!superClass.getName().equals("core.Asset")) {
      inheritedClassNames.add(superClass.getName());
      superClass = superClass.getSuperclass();
    }
    return inheritedClassNames;
  }

  public String getAssociatedAssetClassName(String roleName) {
    return "";
  }

  public Set<Asset> getAssociatedAssets(String roleName) {
    return null;
  }

  public Set<Asset> getAllAssociatedAssets() {
    return null;
  }

  public static List<Asset> getAssets(String className) {
    List<Asset> allAssetsOfClass = new ArrayList<>();
    try {
      Class<?> cls = Class.forName("auto." + className);
      for (Asset asset : Asset.allAssets) {
        if (cls.isAssignableFrom(asset.getClass())) {
          allAssetsOfClass.add(asset);
        }
      }
    } catch (Exception e) {
      fail("Can't find asset called " + className + ".");
    }
    return allAssetsOfClass;
  }

  public boolean isConnected(Asset target) {
    Set<Asset> closure = getClosure();
    return closure.contains(target);
  }

  public Set<Asset> getClosure() {
    Set<Asset> thisInASet = new HashSet<>();
    thisInASet.add(this);
    return getClosure(thisInASet);
  }

  public Set<Asset> getClosure(Set<Asset> sourceSet) {
    Set<Asset> baseTargetSet = new HashSet<>();
    baseTargetSet.addAll(sourceSet);
    Set<Asset> expandedTargetSet;
    boolean foundNewNeighbors = true;
    while (foundNewNeighbors) {
      expandedTargetSet = expandWithNeighbors(baseTargetSet);
      if (baseTargetSet.containsAll(expandedTargetSet)) {
        foundNewNeighbors = false;
      }
      baseTargetSet = expandedTargetSet;
    }
    return baseTargetSet;
  }

  public Set<Asset> expandWithNeighbors(Set<Asset> sourceSet) {
    Set<Asset> targetSet = new HashSet<>();
    targetSet.addAll(sourceSet);
    for (Asset sourceAsset : sourceSet) {
      targetSet.addAll(sourceAsset.getAllAssociatedAssets());
    }
    return targetSet;
  }

  protected void assertion(String noAssociationMessage, Boolean enforce) {
    if (enforce) {
      fail(noAssociationMessage);
    } else {
      System.out.println(noAssociationMessage);
    }
  }
}

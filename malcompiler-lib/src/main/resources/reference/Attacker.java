package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Attacker {
  private static final Pattern distributionPattern =
      Pattern.compile("^([a-zA-Z]+)(?:\\((?:([0-9.]+)(?:, ([0-9.]+))?)?\\))?$");

  protected Set<AttackStep> activeAttackSteps = new HashSet<>();
  public boolean verbose = false;
  private static final String defaultProfile = "attackerProfile.ttc";
  protected static Map<String, Double> ttcHashMap = new HashMap<>();

  public Attacker() {
    verbose = false;
  }

  public Attacker(boolean verbose) {
    this.verbose = verbose;
  }

  public void addAttackPoint(AttackStep attackPoint) {
    attackPoint.ttc = 0;
    activeAttackSteps.add(attackPoint);
  }

  public void addRandomAttackPoint(long randomSeed) {
    AttackStep attackPoint = AttackStep.randomAttackStep(randomSeed);
    System.out.println("Attack point: " + attackPoint.fullName());
    addAttackPoint(attackPoint);
  }

  private AttackStep getShortestActiveStep() {
    AttackStep shortestStep = null;
    double shortestTtc = Double.MAX_VALUE;
    for (AttackStep attackStep : activeAttackSteps) {
      if (attackStep.ttc < shortestTtc) {
        shortestTtc = attackStep.ttc;
        shortestStep = attackStep;
      }
    }
    return shortestStep;
  }

  public void reset() {
    for (AttackStep attackStep : AttackStep.allAttackSteps) {
      attackStep.ttc = Double.MAX_VALUE;
    }
  }

  private void debugPrint(String str) {
    if (verbose) {
      System.out.println(str);
    }
  }

  public void customizeTtc(String name, String distribution) {
    ttcHashMap.put(name, Attacker.parseDistribution(distribution, isDefense(name)));
  }

  public static double parseDistribution(String dist, boolean defense) {
    Matcher matcher = distributionPattern.matcher(dist);
    matcher.matches();
    double a = 0;
    double b = 0;
    try {
      a = Double.valueOf(matcher.group(2));
      b = Double.valueOf(matcher.group(3));
    } catch (Exception e) {
    }
    switch (matcher.group(1)) {
      case "Bernoulli":
        if (defense) {
          return a < 0.5 ? 0 : Double.MAX_VALUE;
        } else {
          return a < 0.5 ? Double.MAX_VALUE : 0;
        }
      case "Binomial":
        return a * b;
      case "Exponential":
        return 1 / a;
      case "Gamma":
        return a / b;
      case "Infinity":
        return Double.MAX_VALUE;
      case "LogNormal":
        return Math.exp(a + b / 2);
      case "Pareto":
        return a <= 1 ? Double.MAX_VALUE : a * b / (a - 1);
      case "TruncatedNormal":
        return a;
      case "Uniform":
        return (a + b) / 2;
      case "Zero":
        return 0;
      default:
        System.err.println(String.format("No matching distribution for: %s", dist));
        return 0;
    }
  }

  private boolean isDefense(String name) {
    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
    for (Defense defense : Defense.allDefenses) {
      if (defense.disable.fullName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  private Map<String, Double> readProfile(Properties profile) {
    Map<String, Double> profileMap = new HashMap<>();
    for (String name : profile.stringPropertyNames()) {
      // Local ttc overrides ttcfile
      if (ttcHashMap.containsKey(name)) {
        profileMap.put(name, ttcHashMap.get(name));
      } else {
        profileMap.put(name, parseDistribution(profile.getProperty(name), isDefense(name)));
      }
    }
    ttcHashMap.clear();
    return profileMap;
  }

  public void attack() {
    try {
      attack(new File(getClass().getClassLoader().getResource(defaultProfile).toURI()));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public void attack(String profilePath) {
    attack(new File(profilePath));
  }

  public void attack(File profileFile) {
    Properties profile = new Properties();
    try {
      profile.load(new FileInputStream(profileFile));
    } catch (IOException e) {
      System.err.println("Could not open profile: " + profileFile.getPath());
      System.exit(1);
    }
    attack(profile);
  }

  public void attack(Properties profile) {
    AttackStep.ttcHashMap = readProfile(profile);
    debugPrint("debug attacking");

    debugPrint(
        String.format(
            "The model contains %d assets and %d attack steps.",
            Asset.allAssets.size(), AttackStep.allAttackSteps.size()));
    AttackStep currentAttackStep = null;
    debugPrint(String.format("AttackStep.allAttackSteps = %s", AttackStep.allAttackSteps));

    for (AttackStep attackStep : AttackStep.allAttackSteps) {
      attackStep.setExpectedParents();
      debugPrint(
          String.format(
              "The expected parents of %s are %s",
              attackStep.fullName(), attackStep.expectedParents));
    }

    for (Defense defense : Defense.allDefenses) {
      if (!defense.isEnabled()) {
        addAttackPoint(defense.disable);
      }
    }

    while (!activeAttackSteps.isEmpty()) {
      debugPrint(String.format("activeAttackSteps = %s", activeAttackSteps));
      currentAttackStep = getShortestActiveStep();
      debugPrint(String.format("Updating children of %s", currentAttackStep.fullName()));
      currentAttackStep.updateChildren(activeAttackSteps);
      activeAttackSteps.remove(currentAttackStep);
    }
  }
}

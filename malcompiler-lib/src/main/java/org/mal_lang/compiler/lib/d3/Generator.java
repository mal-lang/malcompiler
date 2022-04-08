package org.mal_lang.compiler.lib.d3;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.lib.Lang;
import org.mal_lang.compiler.lib.Lang.StepAttackStep;
import org.mal_lang.compiler.lib.Lang.StepBinOp;
import org.mal_lang.compiler.lib.Lang.StepExpr;

public class Generator extends org.mal_lang.compiler.lib.Generator {
  public static void generate(Lang lang, Map<String, String> args)
      throws CompilerException, FileNotFoundException {
    new Generator(lang, args);
  }

  private Generator(Lang lang, Map<String, String> args)
      throws CompilerException, FileNotFoundException {
    super(false, false);
    Locale.setDefault(Locale.ROOT);
    if (!args.containsKey("path") || args.get("path").isBlank()) {
      throw error("D3 generator requires argument 'path'");
    }
    var outputDir = new File(args.get("path"));
    if (!outputDir.isDirectory()) {
      throw error("Argument 'path' must be a directory");
    }

    var json = Json.createObjectBuilder();
    var assets = Json.createArrayBuilder();
    for (var asset : lang.getAssets().values()) {
      var jsonAsset = Json.createObjectBuilder();
      jsonAsset.add("name", asset.getName());
      if (!asset.getAttackSteps().isEmpty()) {
        var attackSteps = Json.createArrayBuilder();
        for (var attackStep : asset.getAttackSteps().values()) {
          var jsonAttackStep = Json.createObjectBuilder();
          jsonAttackStep.add("name", attackStep.getName());
          switch (attackStep.getType()) {
            case ANY:
              jsonAttackStep.add("type", "or");
              break;
            case ALL:
              jsonAttackStep.add("type", "and");
              break;
            case DEFENSE:
            case EXIST:
            case NOTEXIST:
              jsonAttackStep.add("type", "defense");
              break;
            default:
              throw new RuntimeException("Invalid attack step type " + attackStep.getType());
          }

          var targets = Json.createArrayBuilder();

          if (asset.hasSuperAsset()) {
            // getAttackStep will traverse all parents
            var as = asset.getSuperAsset().getAttackStep(attackStep.getName());
            if (as != null) {
              JsonObjectBuilder jsonStep = Json.createObjectBuilder();
              jsonStep.add("name", as.getName());
              jsonStep.add("entity_name", as.getAsset().getName());
              jsonStep.add("size", 4000);
              targets.add(jsonStep);
            }
          }

          for (var expr : attackStep.getReaches()) {
            var as = getAttackStep(expr);
            JsonObjectBuilder jsonStep = Json.createObjectBuilder();
            jsonStep.add("name", as.attackStep.getName());
            jsonStep.add("entity_name", as.attackStep.getAsset().getName());
            jsonStep.add("size", 4000);
            targets.add(jsonStep);
          }
          jsonAttackStep.add("targets", targets);
          attackSteps.add(jsonAttackStep);
        }
        jsonAsset.add("children", attackSteps);
      }
      assets.add(jsonAsset);
    }
    json.add("children", assets);
    var jsonString = json.build().toString();

    var name = lang.getDefine("id");
    var output = new File(outputDir, "visualization." + name + ".html");
    var is = getClass().getResourceAsStream("/d3/visualization.html");
    var reader = new BufferedReader(new InputStreamReader(is));
    String content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
    content = content.replace("{{NAME}}", name).replace("{{JSON}}", jsonString);

    try (var pw = new PrintWriter(output)) {
      pw.write(content);
    }
  }

  private StepAttackStep getAttackStep(StepExpr expr) {
    if (expr instanceof StepAttackStep) {
      return (StepAttackStep) expr;
    } else if (expr instanceof StepBinOp) {
      return getAttackStep(((StepBinOp) expr).rhs);
    } else {
      throw new RuntimeException("Unexpected expression " + expr);
    }
  }
}

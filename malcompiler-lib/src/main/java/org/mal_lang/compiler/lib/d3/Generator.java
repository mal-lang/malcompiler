package org.mal_lang.compiler.lib.d3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.mal_lang.compiler.lib.AST;
import org.mal_lang.compiler.lib.AST.Asset;
import org.mal_lang.compiler.lib.AST.IDExpr;
import org.mal_lang.compiler.lib.Analyzer;
import org.mal_lang.compiler.lib.CompilerException;

public class Generator extends org.mal_lang.compiler.lib.Generator {
  public static void generate(Analyzer analyzer, Map<String, String> args)
      throws CompilerException, FileNotFoundException {
    new Generator(analyzer, args);
  }

  private Generator(Analyzer analyzer, Map<String, String> args)
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
    for (var category : analyzer.ast.getCategories()) {
      for (var asset : category.assets) {
        var jsonAsset = Json.createObjectBuilder();
        jsonAsset.add("name", asset.name.id);
        if (!asset.attackSteps.isEmpty()) {
          var attackSteps = Json.createArrayBuilder();
          for (var attackStep : asset.attackSteps) {
            var jsonAttackStep = Json.createObjectBuilder();
            jsonAttackStep.add("name", attackStep.name.id);
            switch (attackStep.type) {
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
                throw new RuntimeException("Invalid attack step type " + attackStep.type);
            }

            JsonArrayBuilder targets = Json.createArrayBuilder();

            Asset parent = asset;
            while (parent.parent.isPresent()) {
              var name = parent.parent.get();
              parent = analyzer.getAsset(name);
              if (analyzer.hasStep(asset, attackStep.name.id) != null) {
                JsonObjectBuilder jsonStep = Json.createObjectBuilder();
                jsonStep.add("name", attackStep.name.id);
                jsonStep.add("entity_name", parent.name.id);
                jsonStep.add("size", 4000);
                targets.add(jsonStep);
                break;
              }
            }

            if (attackStep.reaches.isPresent()) {
              for (var expr : attackStep.reaches.get().reaches) {
                String step = "";
                String target = "";
                if (expr instanceof IDExpr) {
                  step = ((IDExpr) expr).id.id;
                  target = asset.name.id;
                } else {
                  var stepExpr = (AST.StepExpr) expr;
                  step = ((IDExpr) stepExpr.rhs).id.id;
                  target = analyzer.checkToAsset(asset, stepExpr.lhs).name.id;
                }
                JsonObjectBuilder jsonStep = Json.createObjectBuilder();
                jsonStep.add("name", step);
                jsonStep.add("entity_name", target);
                jsonStep.add("size", 4000);
                targets.add(jsonStep);
              }
            }
            jsonAttackStep.add("targets", targets);
            attackSteps.add(jsonAttackStep);
          }
          jsonAsset.add("children", attackSteps);
        }
        assets.add(jsonAsset);
      }
    }
    json.add("children", assets);
    var jsonString = json.build().toString();

    String name = "";
    for (var define : analyzer.ast.getDefines()) {
      if (define.key.id.equals("id")) {
        name = define.value;
        break;
      }
    }

    var output = new File(outputDir, "visualization." + name + ".html");
    var is = getClass().getResourceAsStream("/d3/visualization.html");
    var reader = new BufferedReader(new InputStreamReader(is));
    String content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
    content = content.replace("{{NAME}}", name).replace("{{JSON}}", jsonString);

    try (var pw = new PrintWriter(output)) {
      pw.write(content);
    }
  }
}

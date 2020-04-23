package org.mal_lang.compiler.test.lib.d3;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mal_lang.compiler.test.lib.AssertLang.assertGetLangClassPath;

import java.io.FileNotFoundException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mal_lang.compiler.lib.CompilerException;
import org.mal_lang.compiler.test.MalTest;

public class TestD3Generator extends MalTest {

  public void generate(String resource) {
    var path = getNewTmpDir("d3");
    var lang = assertGetLangClassPath(resource);
    try {
      org.mal_lang.compiler.lib.d3.Generator.generate(lang, Map.of("path", path));
    } catch (FileNotFoundException | CompilerException e) {
      fail(e);
    }
  }

  @Test
  public void testBled() {
    generate("bled/bled.mal");
  }

  @Test
  public void testVehicleLang() {
    generate("vehiclelang/vehicleLang.mal");
  }

  @Test
  public void testComplex() {
    generate("analyzer/complex.mal");
  }
}

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
module org.mal_lang.compiler.lib {
  exports org.mal_lang.formatter;
  exports org.mal_lang.compiler.lib;
  exports org.mal_lang.compiler.lib.reference;
  exports org.mal_lang.compiler.lib.securicad;

  requires com.squareup.javapoet;
  requires java.compiler;
  requires java.logging;
  requires java.desktop;
  requires svgSalamander;
}

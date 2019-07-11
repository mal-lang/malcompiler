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
module mal.app {
  // copied from main module descriptor
  exports com.foreseeti.mal;
  exports com.foreseeti.mal.vehiclelang;

  requires info.picocli;
  requires java.logging;
  requires com.squareup.javapoet;
  requires java.compiler;

  opens com.foreseeti.mal to
      info.picocli;

  // additional test requirement
  requires transitive org.junit.jupiter.api;
}

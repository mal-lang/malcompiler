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
package org.mal_lang.mal.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public final class MalInfo {
  private static final String TITLE_KEY = Attributes.Name.IMPLEMENTATION_TITLE.toString();
  private static final String VERSION_KEY = Attributes.Name.IMPLEMENTATION_VERSION.toString();
  private static final String MAL_COMPILER = "MAL Compiler";

  private static String title = null;
  private static String version = null;

  private static List<Manifest> getManifests() throws IOException {
    var manifests = new ArrayList<Manifest>();
    var resources = MalInfo.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
    while (resources.hasMoreElements()) {
      try {
        manifests.add(new Manifest(resources.nextElement().openStream()));
      } catch (IOException e) {
        continue;
      }
    }
    return manifests;
  }

  private static Map<String, String> getManifestMap(Manifest man) {
    var manifest = new HashMap<String, String>();
    var attributes = man.getMainAttributes();
    for (var attribute : attributes.entrySet()) {
      var key = attribute.getKey();
      var value = attribute.getValue();
      if (key instanceof Attributes.Name && value instanceof String) {
        manifest.put(((Attributes.Name) key).toString(), (String) value);
      }
    }
    return manifest;
  }

  private static String getManifestTitle(Map<String, String> manifest) {
    return manifest.get(TITLE_KEY);
  }

  private static String getManifestVersion(Map<String, String> manifest) {
    return manifest.get(VERSION_KEY);
  }

  private static Map<String, String> getMalCompilerManifest() throws IOException {
    var manifests = getManifests();
    if (manifests.isEmpty()) {
      throw new IOException("Couldn't find any manifest files in the class path");
    }
    for (var man : manifests) {
      var manifest = getManifestMap(man);
      var title = getManifestTitle(manifest);
      if (title != null && title.equals(MAL_COMPILER)) {
        return manifest;
      }
    }
    throw new IOException(String.format("Couldn't find manifest file for %s", MAL_COMPILER));
  }

  private static void initManifest() throws IOException {
    var manifest = getMalCompilerManifest();
    var title = getManifestTitle(manifest);
    var version = getManifestVersion(manifest);
    if (version == null || version.isBlank()) {
      throw new IOException(
          String.format("Manifest file for %s contains no version", MAL_COMPILER));
    }
    MalInfo.title = title;
    MalInfo.version = version;
  }

  public static String getTitle() throws IOException {
    if (title == null) {
      initManifest();
    }
    return title;
  }

  public static String getVersion() throws IOException {
    if (version == null) {
      initManifest();
    }
    return version;
  }
}

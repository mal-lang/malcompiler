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

import java.util.HashMap;
import java.util.Map;

/**
 * Scope stores maps on a stack. Each map is a 'scope' of values uniquely identified by their map
 * keys. Values from previous scopes may be retrieved with the lookup() and lookdown() methods.
 */
public class Scope<T> {
  private Map<String, T> symbols;
  public final Scope<T> parent;

  public Scope() {
    this.symbols = new HashMap<>();
    this.parent = null;
  }

  public Scope(Scope<T> parent) {
    this.symbols = new HashMap<>();
    this.parent = parent;
  }

  /**
   * Iterates the scopes from the bottom up (back to front), and returns the value of the first map
   * containing the key.
   *
   * @param key Object key
   * @return Object associated with the first match of key, or null if not found
   */
  public T lookup(String key) {
    if (symbols.containsKey(key)) {
      return symbols.get(key);
    } else if (parent != null) {
      return parent.lookup(key);
    } else {
      return null;
    }
  }

  /**
   * Iterates the scope from the top down (front to back), and returns the value of the first map
   * containing the key.
   *
   * @param key Object key
   * @return Object associated with the first match of key, or null if not found
   */
  public T lookdown(String key) {
    if (parent != null) {
      var parentValue = parent.lookdown(key);
      if (parentValue != null) {
        return parentValue;
      }
    }
    return symbols.get(key);
  }

  /**
   * Looks only at the current scope and returns the value associated with the key.
   *
   * @param key Object key
   * @return Object associated with the match of key, or null if not found
   */
  public T look(String key) {
    return symbols.get(key);
  }

  public Scope<T> getScopeFor(String key) {
    if (symbols.containsKey(key)) {
      return this;
    } else if (parent != null) {
      return parent.getScopeFor(key);
    } else {
      return null;
    }
  }

  /**
   * Adds a value to the current scope (map).
   *
   * @param key Key associated with object
   * @param value Value associated with the key
   */
  public void add(String key, T value) {
    symbols.put(key, value);
  }

  @Override
  public String toString() {
    if (parent != null) {
      return String.format("{%s, %s}", parent.toString(), String.join(", ", symbols.keySet()));
    } else {
      return String.format("{%s}", String.join(", ", symbols.keySet()));
    }
  }
}

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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Scope stores maps on a stack. Each map is a 'scope' of values uniquely identified by their map
 * keys. Values from previous scopes may be retrieved with the lookup() and lookdown() methods.
 */
public class Scope<T> {
  public Stack<Map<String, T>> stack;

  public Scope() {
    stack = new Stack<>();
  }

  /** Creates a new scope (map) and puts it on the top of the stack. */
  public void enterScope() {
    stack.push(new HashMap<>());
  }

  /** Destroys the current scope. */
  public void exitScope() {
    stack.pop();
  }

  /**
   * Iterates the scopes from the bottom up (back to front), and returns the value of the first map
   * containing the key.
   *
   * @param key Object key
   * @return Object associated with the first match of key, or null if not found
   */
  public T lookup(String key) {
    for (int i = stack.size() - 1; i >= 0; i--) {
      if (stack.get(i).containsKey(key)) {
        return stack.get(i).get(key);
      }
    }
    return null;
  }

  /**
   * Iterates the scope from the top down (front to back), and returns the value of the first map
   * containing the key.
   *
   * @param key Object key
   * @return Object associated with the first match of key, or null if not found
   */
  public T lookdown(String key) {
    for (int i = 0; i < stack.size(); i++) {
      if (stack.get(i).containsKey(key)) {
        return stack.get(i).get(key);
      }
    }
    return null;
  }

  /**
   * Looks only at the current scope and returns the value associated with the key.
   *
   * @param key Object key
   * @return Object associated with the match of key, or null if not found
   */
  public T look(String key) {
    if (stack.peek().containsKey(key)) {
      return stack.peek().get(key);
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
    stack.peek().put(key, value);
  }
}

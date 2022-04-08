/*
 * Copyright 2020-2022 Foreseeti AB
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
package org.mal_lang.formatter.blocks;

public class TextBlock extends AbstractBlock {
  boolean free;

  public TextBlock(String value, boolean free) {
    output = value;
    this.free = free;
  }

  public TextBlock(String value) {
    this(value, false);
  }

  @Override
  public void update(int index, int margin) {
    this.index = index + this.getOutput().length();
    if (free) {
      this.cost = 0;
    } else if (this.index < margin) {
      this.cost = 0;
    } else {
      this.cost = COST_PER_CHARACTER_OVER_MARGIN * (this.index - margin);
    }
  }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MultiBlock extends AbstractBlock {
  protected List<Block> blocks;

  public MultiBlock(List<Block> blocks) {
    this.blocks = new ArrayList<>(blocks);
  }

  public MultiBlock(Block... blocks) {
    this(Arrays.asList(blocks));
  }

  public void add(Block block) {
    blocks.add(block);
  }

  public void add(Block... blocks) {
    for (var block : blocks) {
      add(block);
    }
  }

  public int getSize() {
    return blocks.size();
  }
}

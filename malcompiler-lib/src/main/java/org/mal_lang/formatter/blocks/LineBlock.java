/*
 * Copyright 2020 Foreseeti AB
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

import java.util.List;

public class LineBlock extends MultiBlock {
  public LineBlock(Block... blocks) {
    super(blocks);
  }

  public LineBlock(List<Block> blocks) {
    super(blocks);
  }

  @Override
  public void update(int index, int margin) {
    this.index = index;
    cost = 0;
    output = "";
    for (var block : blocks) {
      block.update(this.index, margin);
      this.index = block.getIndex();
      cost += block.getCost();
      output += block.getOutput();
    }
  }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WrapBlock extends ChoiceBlock {
  private List<Block> separators;
  private int indent;
  private int n;
  private List<Block> _blocks;
  private boolean alwaysSeparate;

  public WrapBlock(List<Block> separators, int indent, boolean alwaysSeparate, List<Block> blocks) {
    super();
    this.separators = new ArrayList<>(separators);
    this.alwaysSeparate = alwaysSeparate;
    this.indent = indent;
    _blocks = new ArrayList<>(blocks);
    n = _blocks.size();
    super.add(b(1));
  }

  public WrapBlock(List<Block> separators, int indent, List<Block> blocks) {
    this(separators, indent, true, blocks);
  }

  public WrapBlock(String separator, int indent, List<Block> blocks) {
    this(List.of(new TextBlock(separator)), indent, false, blocks);
  }

  public WrapBlock(String separator, int indent, Block... blocks) {
    this(separator, indent, Arrays.asList(blocks));
  }

  private Block breakBlock(int i, Block block) {
    if (alwaysSeparate) {
      block = new LineBlock(s(i), block);
    }
    return new IndentBlock(indent, block);
  }

  private Block l(int i) {
    return _blocks.get(i - 1);
  }

  private Block s(int i) {
    if (i > separators.size()) {
      return separators.get(0);
    } else {
      return separators.get(i - 1);
    }
  }

  private Block lb(int start, int end) {
    var block = new LineBlock(l(start));
    for (int i = start + 1; i <= end; i++) {
      block.add(s(i - 1), l(i));
    }
    return block;
  }

  private Map<Integer, Block> bCache = new ConcurrentHashMap<>();

  private Block b(int index) {
    return bCache.computeIfAbsent(
        index,
        i -> {
          if (i == n) {
            return l(i);
          } else {
            var block = new ChoiceBlock(new StackBlock(l(i), breakBlock(i, b(i + 1))));
            for (int j = i + 1; j < n; j++) {
              block.add(new StackBlock(lb(i, j), breakBlock(j, b(j + 1))));
            }
            block.add(lb(i, n));
            return block;
          }
        });
  }

  @Override
  public void add(Block block) {
    throw new RuntimeException("WrapBlock cannot be changed after instantiation");
  }
}

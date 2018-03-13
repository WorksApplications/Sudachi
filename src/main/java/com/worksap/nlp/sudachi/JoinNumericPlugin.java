/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.sudachi;

import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin for concatenation of the numerics.
 *
 * This plugin concatenate the sequence of numerics.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.JoinNumericPlugin",
 *     "enableNormalize" : true,
 *   }
 * }</pre>
 *
 * <p>If {@code enableNormalize} is {@code true}, the normalized form
 * of the sequence of digits and Kanji numerics is the numerical value
 * represented by the sequence.
 */
class JoinNumericPlugin extends PathRewritePlugin {

    boolean enableNormalize;

    @Override
    public void setUp(Grammar grammar) {
        enableNormalize = settings.getBoolean("enableNormalize", true);
    }

    @Override
    public void rewrite(InputText<?> text, List<LatticeNode> path, Lattice lattice) {
        int beginIndex = -1;
        NumericParser parser = new NumericParser();

        for (int i = 0; i < path.size(); i++) {
            LatticeNode node = path.get(i);
            Set<CategoryType> types = getCharCategoryTypes(text, node);
            String s = node.getWordInfo().getNormalizedForm();
            if (types.contains(CategoryType.NUMERIC) ||
                types.contains(CategoryType.KANJINUMERIC) ||
                s.equals(".") || s.equals(",")) {

                if (beginIndex < 0) {
                    parser.clear();
                    beginIndex = i;
                }

                for (int j = 0; j < s.length(); j++) {
                    char c = s.charAt(j);
                    if (!parser.append(c)) {
                        if (beginIndex >= 0) {
                            if ((s.equals(",") || s.equals(".")) && beginIndex != i) {
                                i = split(s, path, beginIndex, i, lattice) + 1;
                            }
                            beginIndex = -1;
                        }
                        break;
                    }
                }
            } else {
                if (beginIndex >= 0 && parser.done()) {
                    concat(path, beginIndex, i, lattice, parser);
                    i = beginIndex + 1;
                }
                beginIndex = -1;
            }
        }

        if (beginIndex >= 0) {
            if (parser.done()) {
                concat(path, beginIndex, path.size(), lattice, parser);
            }
        }
    }

    private void concat(List<LatticeNode> path, int begin, int end,
              Lattice lattice, NumericParser parser) {
        if (enableNormalize) {
            String normalizedForm = parser.getNormalized();
            if (end - begin > 1 || !normalizedForm.equals(path.get(begin).getWordInfo().getNormalizedForm())) {
                concatenate(path, begin, end, lattice, normalizedForm);
            }
        } else {
            if (end - begin > 1) {
                concatenate(path, begin, end, lattice, null);
            }
        }
    }

    private int split(String delim, List<LatticeNode> path, int begin, int end, Lattice lattice) {
        NumericParser parser = new NumericParser();

        int b = begin;
        for (int i = begin; i < end; i++) {
            LatticeNode node = path.get(i);
            String s = node.getWordInfo().getNormalizedForm();
            if (s.equals(delim)) {
                parser.done();
                concat(path, b, i, lattice, parser);
                end -= i - b - 1;
                i = b + 1;
                b = i + 1;
                parser.clear();
            } else {
                for (int j = 0; j < s.length(); j++) {
                    parser.append(s.charAt(j));
                }
            }
        }
        parser.done();
        concat(path, b, end, lattice, parser);

        return b;
    }
}

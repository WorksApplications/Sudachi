/*
 * Copyright (c) 2019 Works Applications Co., Ltd.
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin for concatenation of the numerics.
 *
 * This plugin concatenate the sequence of numerics.
 *
 * <p>
 * The following is an example of settings.
 * 
 * <pre>
 * {@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.JoinNumericPlugin",
 *     "enableNormalize" : true,
 *   }
 * }
 * </pre>
 *
 * <p>
 * If {@code enableNormalize} is {@code true}, the normalized form of the
 * sequence of digits and Kanji numerics is the numerical value represented by
 * the sequence.
 */
class JoinNumericPlugin extends PathRewritePlugin {

    static final List<String> NUMERIC_POS = Arrays.asList("名詞", "数詞", "*", "*", "*", "*");

    boolean enableNormalize;
    short numericPOSId;

    @Override
    public void setUp(Grammar grammar) {
        enableNormalize = settings.getBoolean("enableNormalize", true);
        numericPOSId = grammar.getPartOfSpeechId(NUMERIC_POS);
    }

    @Override
    public void rewrite(InputText text, List<LatticeNode> path, Lattice lattice) {
        int beginIndex = -1;
        boolean commaAsDigit = true;
        boolean periodAsDigit = true;
        NumericParser parser = new NumericParser();

        for (int i = 0; i < path.size(); i++) {
            LatticeNode node = path.get(i);
            Set<CategoryType> types = getCharCategoryTypes(text, node);
            String s = node.getWordInfo().getNormalizedForm();
            if (types.contains(CategoryType.NUMERIC) || types.contains(CategoryType.KANJINUMERIC)
                    || (periodAsDigit && s.equals(".")) || (commaAsDigit && s.equals(","))) {

                if (beginIndex < 0) {
                    parser.clear();
                    beginIndex = i;
                }

                for (int j = 0; j < s.length(); j++) {
                    char c = s.charAt(j);
                    if (!parser.append(c)) {
                        if (beginIndex >= 0) {
                            if (parser.errorState == NumericParser.Error.COMMA) {
                                commaAsDigit = false;
                                i = beginIndex - 1;
                            } else if (parser.errorState == NumericParser.Error.POINT) {
                                periodAsDigit = false;
                                i = beginIndex - 1;
                            }
                            beginIndex = -1;
                        }
                        break;
                    }
                }
            } else {
                if (beginIndex >= 0) {
                    if (parser.done()) {
                        concat(path, beginIndex, i, lattice, parser);
                        i = beginIndex + 1;
                    } else {
                        String ss = path.get(i - 1).getWordInfo().getNormalizedForm();
                        if ((parser.errorState == NumericParser.Error.COMMA && ss.equals(","))
                                || (parser.errorState == NumericParser.Error.POINT && ss.equals("."))) {
                            concat(path, beginIndex, i - 1, lattice, parser);
                            i = beginIndex + 2;
                        }
                    }
                }
                beginIndex = -1;
                if (!commaAsDigit && !s.equals(",")) {
                    commaAsDigit = true;
                }
                if (!periodAsDigit && !s.equals(".")) {
                    periodAsDigit = true;
                }
            }
        }

        if (beginIndex >= 0) {
            if (parser.done()) {
                concat(path, beginIndex, path.size(), lattice, parser);
            } else {
                String ss = path.get(path.size() - 1).getWordInfo().getNormalizedForm();
                if ((parser.errorState == NumericParser.Error.COMMA && ss.equals(","))
                        || (parser.errorState == NumericParser.Error.POINT && ss.equals("."))) {
                    concat(path, beginIndex, path.size() - 1, lattice, parser);
                }
            }
        }
    }

    private void concat(List<LatticeNode> path, int begin, int end, Lattice lattice, NumericParser parser) {
        if (path.get(begin).getWordInfo().getPOSId() != numericPOSId)
            return;
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
}

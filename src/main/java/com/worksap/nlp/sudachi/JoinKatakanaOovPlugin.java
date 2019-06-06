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

import java.util.List;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin for concatenation of Katakana OOVs.
 *
 * This plugin concatenate the Katakana OOV and the adjacent Katakana morphemes.
 *
 * <p>
 * The concatenated morpheme is OOV, and its part of speech must be specified in
 * the settings.
 *
 * <p>
 * The following is an example of settings.
 * 
 * <pre>
 * {@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.JoinKatakanaOovPlugin",
 *     "oovPOS" : [ "POS1", "POS2", ... ],
 *     "minLength" : 3
 *   }
 * }
 * </pre>
 */
class JoinKatakanaOovPlugin extends PathRewritePlugin {

    short oovPosId;
    int minLength;

    @Override
    public void setUp(Grammar grammar) {
        List<String> pos = settings.getStringList("oovPOS");
        if (pos.isEmpty()) {
            throw new IllegalArgumentException("oovPOS is undefined");
        }
        oovPosId = grammar.getPartOfSpeechId(pos);
        if (oovPosId < 0) {
            throw new IllegalArgumentException("oovPOS is invalid");
        }
        minLength = settings.getInt("minLength", 1);
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength is negative");
        }
    }

    @Override
    public void rewrite(InputText text, List<LatticeNode> path, Lattice lattice) {
        for (int i = 0; i < path.size(); i++) {
            LatticeNode node = path.get(i);
            if ((node.isOOV() || isShorter(minLength, text, node)) && isKatakanaNode(text, node)) {
                int begin = i - 1;
                for (; begin >= 0; begin--) {
                    if (!isKatakanaNode(text, path.get(begin))) {
                        begin++;
                        break;
                    }
                }
                if (begin < 0) {
                    begin = 0;
                }
                int end = i + 1;
                for (; end < path.size(); end++) {
                    if (!isKatakanaNode(text, path.get(end))) {
                        break;
                    }
                }
                while (begin != end && !canOovBowNode(text, path.get(begin))) {
                    begin++;
                }
                if (end - begin > 1) {
                    concatenateOov(path, begin, end, oovPosId, lattice);
                    i = begin + 1;
                }
            }
        }
    }

    boolean isKatakanaNode(InputText text, LatticeNode node) {
        return getCharCategoryTypes(text, node).contains(CategoryType.KATAKANA);
    }

    boolean isShorter(int length, InputText text, LatticeNode node) {
        return text.codePointCount(node.getBegin(), node.getEnd()) < length;
    }

    boolean canOovBowNode(InputText text, LatticeNode node) {
        return !text.getCharCategoryTypes(node.getBegin()).contains(CategoryType.NOOOVBOW);
    }
}

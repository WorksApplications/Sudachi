/*
 * Copyright (c) 2017-2024 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.dictionary.POS;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

/**
 * A morpheme as a part of the analysis result.
 * 
 * @see Morpheme
 * @see MorphemeList
 * @see SingleMorphemeImpl
 */
class MorphemeImpl extends MorphemeImplBase {
    private final MorphemeList list;
    private final int index;

    // cache
    private LatticeNodeImpl node;

    /* internal */ MorphemeImpl(MorphemeList list, int index) {
        this.list = list;
        this.index = index;
    }

    protected Grammar getGrammar() {
        return list.grammar;
    }

    protected WordInfo getWordInfo() {
        return node().getWordInfo();
    }

    protected StringsCache strings() {
        return node().getStrings();
    }

    private LatticeNodeImpl node() {
        LatticeNodeImpl n = node;
        if (n == null) {
            n = list.node(index);
            node = n;
        }
        return n;
    }

    @Override
    public int begin() {
        return list.getBegin(index);
    }

    @Override
    public int end() {
        return list.getEnd(index);
    }

    @Override
    public String surface() {
        return list.getSurface(index);
    }

    @Override
    public List<Morpheme> split(Tokenizer.SplitMode mode) {
        return list.split(mode, index);
    }

    @Override
    public int getWordId() {
        return node().getWordId();
    }
}

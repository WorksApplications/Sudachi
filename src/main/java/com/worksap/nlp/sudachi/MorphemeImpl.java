/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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
import com.worksap.nlp.sudachi.dictionary.WordInfo;

class MorphemeImpl implements Morpheme {
    private final MorphemeList list;
    private final int index;
    private LatticeNodeImpl node;

    /*internal*/ MorphemeImpl(MorphemeList list, int index) {
        this.list = list;
        this.index = index;
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
    public POS partOfSpeech() {
        WordInfo wi = getWordInfo();
        return list.grammar.getPartOfSpeechString(wi.getPOSId());
    }

    @Override
    public short partOfSpeechId() {
        WordInfo wi = getWordInfo();
        return wi.getPOSId();
    }

    @Override
    public String dictionaryForm() {
        return node().getDictionaryForm();
    }

    @Override
    public String normalizedForm() {
        return node().getNormalizedForm();
    }

    @Override
    public String readingForm() {
        return node().getReading();
    }

    @Override
    public List<Morpheme> split(Tokenizer.SplitMode mode) {
        return list.split(mode, index);
    }

    @Override
    public boolean isOOV() {
        return node().isOOV();
    }

    @Override
    public int getWordId() {
        return node().getWordId();
    }

    @Override
    public int getDictionaryId() {
        return node().getDictionaryId();
    }

    @Override
    public int[] getSynonymGroupIds() {
        WordInfo wi = getWordInfo();
        return wi.getSynonymGroupIds();
    }

    private LatticeNodeImpl node() {
        LatticeNodeImpl n = node;
        if (n == null) {
            n = list.node(index);
            node = n;
        }
        return n;
    }

    WordInfo getWordInfo() {
        return node().getWordInfo();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MorphemeImpl{");
        sb.append("begin=").append(begin());
        sb.append(", end=").append(end());
        sb.append(", surface=").append(surface());
        sb.append(", pos=").append(partOfSpeechId()).append('/').append(partOfSpeech());
        int wordId = getWordId();
        sb.append(", wid=(").append(WordId.dic(wordId)).append(',').append(WordId.word(wordId));
        sb.append(")}");
        return sb.toString();
    }

    /*internal*/ boolean isCompatible(JapaneseDictionary dictionary) {
        return dictionary.grammar == this.list.grammar;
    }
}

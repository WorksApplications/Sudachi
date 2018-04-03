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

import com.worksap.nlp.sudachi.dictionary.WordInfo;

class MorphemeImpl implements Morpheme {

    final MorphemeList list;
    final int index;
    WordInfo wordInfo;

    MorphemeImpl(MorphemeList list, int index) {
        this.list = list;
        this.index = index;
    }

    @Override
    public int begin() { return list.getBegin(index); }

    @Override
    public int end() { return list.getEnd(index); }

    @Override
    public String surface() {
        return list.getSurface(index);
    }

    @Override
    public List<String> partOfSpeech() {
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
        WordInfo wi = getWordInfo();
        return wi.getDictionaryForm();
    }

    @Override
    public String normalizedForm() {
        WordInfo wi = getWordInfo();
        return wi.getNormalizedForm();
    }

    @Override
    public String readingForm() {
        WordInfo wi = getWordInfo();
        return wi.getReadingForm();
    }

    @Override
    public List<Morpheme> split(Tokenizer.SplitMode mode) {
        WordInfo wi = getWordInfo();
        return list.split(mode, index, wi);
    }

    @Override
    public boolean isOOV() {
        return list.isOOV(index);
    }
    
    @Override
    public int getWordId() {
        return list.getWordId(index);
    }

    @Override
    public int getDictionaryId() {
        return list.getDictionaryId(index);
    }

    WordInfo getWordInfo() {
        if (wordInfo == null) {
            wordInfo = list.getWordInfo(index);
        }
        return wordInfo;
    }

}

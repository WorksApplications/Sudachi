/*
 * Copyright (c) 2024 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.POS;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

/**
 * base class for the morpheme classes.
 * 
 * @see MorphemeList
 * @see MorphemeListItem
 * @see SingleMorphemeImpl
 */
abstract class MorphemeImplBase implements Morpheme {

    protected abstract Grammar getGrammar();

    protected abstract WordInfo getWordInfo();

    protected abstract StringsCache strings();

    @Override
    public POS partOfSpeech() {
        WordInfo wi = getWordInfo();
        return getGrammar().getPartOfSpeechString(wi.getPOSId());
    }

    @Override
    public short partOfSpeechId() {
        WordInfo wi = getWordInfo();
        return wi.getPOSId();
    }

    @Override
    public String dictionaryForm() {
        return strings().getDictionaryForm();
    }

    @Override
    public String normalizedForm() {
        return strings().getNormalizedForm();
    }

    @Override
    public String readingForm() {
        return strings().getReading();
    }

    @Override
    public boolean isOOV() {
        return WordId.isOov(getWordId());
    }

    @Override
    public int getDictionaryId() {
        if (isOOV()) {
            return -1;
        }
        return WordId.dic(getWordId());
    }

    @Override
    public int[] getSynonymGroupIds() {
        WordInfo wi = getWordInfo();
        return wi.getSynonymGroupIds();
    }

    @Override
    public String getUserData() {
        WordInfo wi = getWordInfo();
        return wi.getUserData();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append("{");
        sb.append("begin=").append(begin());
        sb.append(", end=").append(end());
        sb.append(", surface=").append(surface());
        sb.append(", pos=").append(partOfSpeechId()).append('/').append(partOfSpeech());
        int wid = getWordId();
        sb.append(", wid=(").append(WordId.dic(wid)).append(',').append(WordId.word(wid));
        sb.append(")}");
        return sb.toString();
    }

    /* internal */ boolean isCompatible(JapaneseDictionary dictionary) {
        return dictionary.grammar == getGrammar();
    }
}

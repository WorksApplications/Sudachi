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

import java.util.ArrayList;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.Lexicon;
import com.worksap.nlp.sudachi.dictionary.POS;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

/**
 * A morpheme independent from the analysis.
 * 
 * @see Morpheme
 * @see MorphemeImpl
 */
class SingleMorphemeImpl implements Morpheme {
    private final int wordId;

    // dictionary data, maybe null for OOV/user morpheme.
    private final Grammar grammar;
    private final Lexicon lexicon;

    // cache
    private WordInfo wordInfo;
    private StringsCache strings;

    private int begin;
    private int end;

    SingleMorphemeImpl(Grammar grammar, Lexicon lexicon, int wordId) {
        this.grammar = grammar;
        this.lexicon = lexicon;
        this.wordId = wordId;

        begin = 0;
        end = surface().length();
    }

    /* internal */ SingleMorphemeImpl(JapaneseDictionary dictionary, int wordId) {
        this(dictionary.getGrammar(), dictionary.getLexicon(), wordId);
    }

    @Override
    public int begin() {
        return this.begin;
    }

    @Override
    public int end() {
        return this.end;
    }

    @Override
    public POS partOfSpeech() {
        WordInfo wi = getWordInfo();
        return grammar.getPartOfSpeechString(wi.getPOSId());
    }

    @Override
    public short partOfSpeechId() {
        WordInfo wi = getWordInfo();
        return wi.getPOSId();
    }

    @Override
    public String surface() {
        return strings().getSurface();
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
    public List<Morpheme> split(Tokenizer.SplitMode mode) {
        List<Morpheme> splits = new ArrayList<>();
        appendSplitsTo(splits, mode);
        return splits;
    }

    private void appendSplitsTo(List<Morpheme> result, Tokenizer.SplitMode mode) {
        if (mode == Tokenizer.SplitMode.A) {
            appendSplitsTo(result, getWordInfo().getAunitSplit());
        } else if (mode == Tokenizer.SplitMode.B) {
            appendSplitsTo(result, getWordInfo().getBunitSplit());
        } else if (mode == Tokenizer.SplitMode.C) {
            appendSplitsTo(result, getWordInfo().getCunitSplit());
        } else {
            result.add(this);
        }
    }

    private void appendSplitsTo(List<Morpheme> result, int[] splitIds) {
        if (splitIds.length == 0) {
            result.add(this);
            return;
        }
        if (splitIds.length == 1) {
            int wid = splitIds[0];
            if (wid == getWordId()) {
                result.add(this);
            } else {
                SingleMorphemeImpl m = new SingleMorphemeImpl(grammar, lexicon, wid);
                m.begin = begin;
                m.end = end;
                result.add(m);
            }
            return;
        }

        int offset = begin();
        for (int wid : splitIds) {
            SingleMorphemeImpl m = new SingleMorphemeImpl(grammar, lexicon, wid);
            m.begin = offset;
            offset += m.surface().length();
            m.end = offset;
            result.add(m);
        }
    }

    @Override
    public boolean isOOV() {
        return WordId.isOov(wordId);
    }

    @Override
    public int getWordId() {
        return wordId;
    }

    @Override
    public int getDictionaryId() {
        if (isOOV()) {
            return -1;
        }
        return WordId.dic(wordId);
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

    private WordInfo getWordInfo() {
        if (wordInfo != null) {
            return wordInfo;
        }
        wordInfo = lexicon.getWordInfo(wordId);
        return wordInfo;
    }

    private StringsCache strings() {
        StringsCache sc = strings;
        if (sc == null) {
            sc = new StringsCache(lexicon, wordId, getWordInfo());
            strings = sc;
        }
        return sc;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append("{");
        sb.append("begin=").append(begin());
        sb.append(", end=").append(end());
        sb.append(", surface=").append(surface());
        sb.append(", pos=").append(partOfSpeechId()).append('/').append(partOfSpeech());
        int wordId = getWordId();
        sb.append(", wid=(").append(WordId.dic(wordId)).append(',').append(WordId.word(wordId));
        sb.append(")}");
        return sb.toString();
    }

    /* internal */ boolean isCompatible(JapaneseDictionary dictionary) {
        return dictionary.grammar == this.grammar;
    }
}

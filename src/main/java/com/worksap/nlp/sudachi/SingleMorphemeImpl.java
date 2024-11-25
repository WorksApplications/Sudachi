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

import java.util.ArrayList;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.Lexicon;
import com.worksap.nlp.sudachi.dictionary.POS;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

/**
 * A morpheme which is independent from the analysis.
 * 
 * @see Morpheme
 * @see MorphemeImpl
 */
class SingleMorphemeImpl extends MorphemeImplBase {
    private final int wordId;

    // dictionary data, maybe null for OOV/user morpheme.
    private final Grammar grammar;
    private final Lexicon lexicon;

    // cache
    private WordInfo wordInfo;
    private StringsCache strings;

    // begin/end is always 0/surface().length() except splits, since this morpheme
    // class does not have the corresponding input text.
    private int begin;
    private int end;

    /** Create a morpheme based on the dictionary data and the word id */
    /* internal */ SingleMorphemeImpl(Grammar grammar, Lexicon lexicon, int wordId) {
        this.wordId = wordId;
        this.grammar = grammar;
        this.lexicon = lexicon;

        begin = 0;
        end = surface().length();
    }

    /** Create an oov morpheme with given data. */
    /* internal */ SingleMorphemeImpl(Grammar grammar, short posId, String surface, String reading,
            String normalizedForm, String dictionaryForm) {
        this.wordId = WordId.makeOov(posId);
        this.grammar = grammar;
        this.lexicon = null;

        this.wordInfo = new WordInfo((short) surface.length(), posId);
        this.strings = new StringsCache(surface, reading, normalizedForm, dictionaryForm);

        begin = 0;
        end = surface.length();
    }

    protected Grammar getGrammar() {
        return grammar;
    }

    protected WordInfo getWordInfo() {
        WordInfo wi = wordInfo;
        if (wi == null) {
            wi = lexicon.getWordInfo(wordId);
            wordInfo = wi;
        }
        return wi;
    }

    protected StringsCache strings() {
        StringsCache sc = strings;
        if (sc == null) {
            sc = new StringsCache(lexicon, wordId, getWordInfo());
            strings = sc;
        }
        return sc;
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
    public String surface() {
        return strings().getSurface();
    }

    @Override
    public List<Morpheme> split(Tokenizer.SplitMode mode) {
        List<Morpheme> splits = new ArrayList<>();
        appendSplitsTo(splits, mode);
        return splits;
    }

    /**
     * append sub-split Morphemes to the result list.
     * 
     * @see LatticeNodeImpl.appendSplitsTo
     */
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
    public int getWordId() {
        return wordId;
    }
}

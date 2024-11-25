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

import com.worksap.nlp.sudachi.dictionary.Lexicon;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

/**
 * Cache strings to reduce the access to the lexicon. Also used to mock the
 * lexicon for OOV.
 */
/* internal */ class StringsCache {
    private final Lexicon lexicon;
    private final int wordId;
    private WordInfo wordInfo;

    private String surface;
    private String reading;
    private String normalizedForm;
    private String dictionaryForm;

    /**
     * Create StringsCache for a word with the wordId in the lexicon.
     */
    public StringsCache(Lexicon lexicon, int wordId) {
        this.lexicon = lexicon;
        this.wordId = wordId;
    }

    /**
     * Create StringsCache for a word with the wordId in the lexicon.
     * 
     * Pass wordInfo to skip lexicon access.
     */
    public StringsCache(Lexicon lexicon, int wordId, WordInfo wordInfo) {
        this(lexicon, wordId);
        this.wordInfo = wordInfo;
    }

    /**
     * Create StringsCache for a word not in the dictionary with provided string
     * forms.
     * 
     * Each string forms must be non null.
     */
    public StringsCache(String surface, String readingForm, String normalizedForm, String dictionaryForm) {
        if (surface == null) {
            throw new IllegalArgumentException("arg surface must be non null.");
        }
        if (readingForm == null) {
            throw new IllegalArgumentException("arg readingForm must be non null.");
        }
        if (normalizedForm == null) {
            throw new IllegalArgumentException("arg normalizedForm must be non null.");
        }
        if (dictionaryForm == null) {
            throw new IllegalArgumentException("arg dictionaryForm must be non null.");
        }

        this.surface = surface;
        this.reading = readingForm;
        this.normalizedForm = normalizedForm;
        this.dictionaryForm = dictionaryForm;

        // won't be used. fill with temporary values
        this.lexicon = null;
        this.wordId = WordId.ID_OOV_NOPOS;
    }

    /**
     * Create StringsCache for a word not in the dictionary filling all form with a
     * same string.
     */
    public StringsCache(String surface) {
        this(surface, surface, surface, surface);
    }

    /**
     * Get internal lexicon for the case this class is also used as a lexicon.
     */
    public Lexicon getLexicon() {
        return lexicon;
    }

    private WordInfo getWordInfo() {
        WordInfo wi = wordInfo;
        if (wi == null) {
            wi = getLexicon().getWordInfo(wordId);
            wordInfo = wi;
        }
        return wi;
    }

    public String getSurface() {
        // benign data race pattern
        // https://shipilev.net/blog/2016/close-encounters-of-jmm-kind/#wishful-benign-is-resilient
        String s = surface;
        if (s == null) {
            WordInfo wi = getWordInfo();
            int headwordPtr = wi.getHeadword();
            int dic = WordId.dic(wordId);
            s = lexicon.string(dic, headwordPtr);
            surface = s;
        }
        return s;
    }

    public String getReading() {
        String s = reading;
        if (s == null) {
            WordInfo wi = getWordInfo();
            int readingPtr = wi.getReadingForm();
            int dic = WordId.dic(wordId);
            s = lexicon.string(dic, readingPtr);
            reading = s;
        }
        return s;
    }

    public String getNormalizedForm() {
        String s = normalizedForm;
        if (s == null) {
            WordInfo wi = getWordInfo();
            int wordref = wi.getNormalizedForm();
            int dic = WordId.refDic(wordref, WordId.dic(wordId));
            int headwordPtr = lexicon.wordInfos(dic).headwordPtr(WordId.word(wordref));
            s = lexicon.string(dic, headwordPtr);
            normalizedForm = s;
        }
        return s;
    }

    public String getDictionaryForm() {
        String s = dictionaryForm;
        if (s == null) {
            WordInfo wi = getWordInfo();
            int wordref = wi.getDictionaryForm();
            int dic = WordId.refDic(wordref, WordId.dic(wordId));
            int headwordPtr = lexicon.wordInfos(dic).headwordPtr(WordId.word(wordref));
            s = lexicon.string(dic, headwordPtr);
            dictionaryForm = s;
        }
        return s;
    }
}
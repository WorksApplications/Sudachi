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

package com.worksap.nlp.sudachi.dictionary.build;

import java.util.Objects;

import com.worksap.nlp.sudachi.dictionary.Lexicon;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

/**
 * WordInfo wrapper for EntryLookup.Entry interface.
 * 
 * Used to resolve wordref that references entry in the system dictionary
 * (during user dictinary build).
 */
public class CompiledWordEntry implements EntryLookup.Entry {
    private final Lexicon lexicon;
    private final int wordId;
    private final String referenceId;
    private WordInfo wordInfo = null;

    public CompiledWordEntry(Lexicon lexicon, int wordId) {
        this(lexicon, wordId, null);
    }

    public CompiledWordEntry(Lexicon lexicon, int wordId, String referenceId) {
        this.lexicon = lexicon;
        this.wordId = wordId;
        this.referenceId = referenceId;
    }

    private WordInfo wordInfo() {
        if (wordInfo != null) {
            return wordInfo;
        }
        wordInfo = lexicon.getWordInfo(wordId);
        return wordInfo;
    }

    @Override
    public int pointer() {
        return wordId;
    }

    @Override
    public boolean matches(short posId, String reading) {
        WordInfo wi = wordInfo();
        return (posId == wi.getPOSId()) && Objects.equals(reading, lexicon.string(0, wi.getReadingForm()));
    }

    @Override
    public String headword() {
        WordInfo wi = wordInfo();
        return lexicon.string(0, wi.getHeadword());
    }

    @Override
    public String referenceId() {
        return referenceId;
    }
}

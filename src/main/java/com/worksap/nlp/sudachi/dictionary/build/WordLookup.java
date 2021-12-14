/*
 * Copyright (c) 2021 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.WordId;
import com.worksap.nlp.sudachi.dictionary.LexiconSet;

import java.util.List;

public class WordLookup {
    public static class Noop implements WordIdResolver {
        @Override
        public int lookup(String headword, short posId, String reading) {
            return -1;
        }

        @Override
        public void validate(int wordId) {

        }

        @Override
        public boolean isUser() {
            return false;
        }
    }

    public static class Csv implements WordIdResolver {
        private final CsvLexicon lexicon;

        public Csv(CsvLexicon lexicon) {
            this.lexicon = lexicon;
        }

        @Override
        public int lookup(String headword, short posId, String reading) {
            List<CsvLexicon.WordEntry> entries = lexicon.getEntries();
            for (int i = 0; i < entries.size(); ++i) {
                CsvLexicon.WordEntry entry = entries.get(i);
                if (entry.headword.equals(headword) && entry.wordInfo.getPOSId() == posId
                        && entry.wordInfo.getReadingForm().equals(reading)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public void validate(int wordId) {
            if (wordId < 0) {
                throw new IllegalArgumentException("wordId can't be negative, was " + wordId);
            }
            List<CsvLexicon.WordEntry> entries = lexicon.getEntries();
            if (wordId >= entries.size()) {
                throw new IllegalArgumentException(String
                        .format("wordId %d was larger than number of dictionary entries (%d)", wordId, entries.size()));
            }
        }

        @Override
        public boolean isUser() {
            return false;
        }
    }

    public static class Prebuilt implements WordIdResolver {
        private final LexiconSet lexicon;
        private final int prebuiltSize;

        public Prebuilt(LexiconSet lexicon) {
            this.lexicon = lexicon;
            this.prebuiltSize = lexicon.size();
        }

        @Override
        public int lookup(String headword, short posId, String reading) {
            return lexicon.getWordId(headword, posId, reading);
        }

        @Override
        public void validate(int wordId) {
            int word = WordId.word(wordId);
            if (word > prebuiltSize) {
                throw new IllegalArgumentException("WordId was larger than the number of dictionary entries");
            }
        }

        @Override
        public boolean isUser() {
            return false;
        }
    }

    public static class Chain implements WordIdResolver {
        private final WordIdResolver system;
        private final WordIdResolver user;

        public Chain(WordIdResolver system, WordIdResolver user) {
            this.system = system;
            this.user = user;
        }

        @Override
        public int lookup(String headword, short posId, String reading) {
            int wid = user.lookup(headword, posId, reading);
            if (wid == -1) {
                return system.lookup(headword, posId, reading);
            }
            return wid;
        }

        @Override
        public void validate(int wordId) {
            int dic = WordId.dic(wordId);
            if (dic == 0) {
                system.validate(wordId);
            } else if (dic == 1) {
                user.validate(wordId);
            } else {
                throw new IllegalArgumentException("dictionary id can be only 0 or 1 at the build time");
            }
        }

        @Override
        public boolean isUser() {
            return true;
        }
    }
}

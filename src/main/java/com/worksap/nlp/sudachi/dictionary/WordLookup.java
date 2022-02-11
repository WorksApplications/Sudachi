/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi.dictionary;

import java.util.Arrays;
import java.util.List;

public final class WordLookup {
    private final DoubleArrayLookup lookup = new DoubleArrayLookup();
    private WordIdTable words;
    // initial size 16 - one cache line (64 bytes) on most modern CPUs
    private int[] wordIds = new int[16];
    private int numWords;
    private final List<DoubleArrayLexicon> lexicons;
    private int currentLexicon = -1;

    public WordLookup(List<DoubleArrayLexicon> lexicons) {
        this.lexicons = lexicons;
    }

    private void rebind(DoubleArrayLexicon lexicon) {
        lookup.setArray(lexicon.getTrieArray());
        words = lexicon.getWordIdTable();
    }

    public void reset(byte[] key, int offset, int limit) {
        currentLexicon = lexicons.size() - 1;
        rebind(lexicons.get(currentLexicon));
        lookup.reset(key, offset, limit);
    }

    public int[] prepare(int length) {
        if (wordIds.length < length) {
            wordIds = Arrays.copyOf(wordIds, Math.max(length, wordIds.length * 2));
        }
        return wordIds;
    }

    public boolean next() {
        while (!lookup.next()) {
            int nextLexicon = currentLexicon - 1;
            if (nextLexicon < 0) {
                return false;
            }
            rebind(lexicons.get(nextLexicon));
            currentLexicon = nextLexicon;
        }
        int wordGroupId = lookup.getValue();
        numWords = words.fillBuffer(wordGroupId, this);
        return true;
    }

    public int getEndOffset() {
        return lookup.getOffset();
    }

    public int getNumWords() {
        return numWords;
    }

    public int[] getWordsIds() {
        return wordIds;
    }
}

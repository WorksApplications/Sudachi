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

import com.worksap.nlp.sudachi.WordId;

import java.util.Arrays;
import java.util.List;

/**
 * This class is an abstraction for looking up words in a list of binary
 * dictionaries. It returns a list of WordIds for each matching key in the Trie
 * index. WordIds are stored in a plain int array to remove any possible boxing.
 * Memory for the lookup is kept for a single analysis step to decrease garbage
 * collection pressure.
 */
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

    /**
     * Start the search for new key
     * 
     * @param key
     *            utf-8 bytes corresponding to the trie key
     * @param offset
     *            offset of key start
     * @param limit
     *            offset of key end
     */
    public void reset(byte[] key, int offset, int limit) {
        currentLexicon = lexicons.size() - 1;
        rebind(lexicons.get(currentLexicon));
        lookup.reset(key, offset, limit);
    }

    /**
     * This is not public API. Returns the array for wordIds with the length at
     * least equal to the passed parameter
     * 
     * @param length
     *            minimum requested length
     * @return WordId array
     */
    public int[] prepare(int length) {
        if (wordIds.length < length) {
            wordIds = Arrays.copyOf(wordIds, Math.max(length, wordIds.length * 2));
        }
        return wordIds;
    }

    /**
     * Sets the wordIds, numWords, endOffset to the
     * 
     * @return true if there was an entry in any of binary dictionaries
     */
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

    /**
     * Returns trie key end offset
     * 
     * @return number of utf-8 bytes corresponding to the end of key
     */
    public int getEndOffset() {
        return lookup.getOffset();
    }

    /**
     *
     * @return number of currently correct entries in the wordIds
     */
    public int getNumWords() {
        return numWords;
    }

    /**
     * Returns array of word ids. Number of correct entries is specified by
     * {@link #getNumWords()}. WordIds have their dictionary part set.
     * 
     * @return array consisting word ids for the current index entry
     * @see WordId
     */
    public int[] getWordsIds() {
        return wordIds;
    }
}

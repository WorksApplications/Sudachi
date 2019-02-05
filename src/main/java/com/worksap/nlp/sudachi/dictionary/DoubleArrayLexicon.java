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

package com.worksap.nlp.sudachi.dictionary;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;

import com.worksap.nlp.dartsclone.DoubleArray;
import com.worksap.nlp.sudachi.MorphemeList;
import com.worksap.nlp.sudachi.Tokenizer;

public class DoubleArrayLexicon implements Lexicon {

    static final int USER_DICT_COST_PAR_MORPH = -20;

    private WordIdTable wordIdTable;
    private WordParameterList wordParams;
    private WordInfoList wordInfos;
    private DoubleArray trie;

    public DoubleArrayLexicon(ByteBuffer bytes, int offset) {
        trie = new DoubleArray();
        int size = bytes.getInt(offset);
        offset += 4;
        ((Buffer)bytes).position(offset);
        IntBuffer array = bytes.asIntBuffer();
        trie.setArray(array, size);
        offset += trie.totalSize();

        wordIdTable = new WordIdTable(bytes, offset);
        offset += wordIdTable.storageSize();

        wordParams = new WordParameterList(bytes, offset);
        offset += wordParams.storageSize();

        wordInfos = new WordInfoList(bytes, offset, wordParams.size());
    }

    /**
     * Returns the word IDs obtained by common prefix search.
     *
     * <p>The search begin with the position at the {@code offset}
     * of the {@code text}.
     *
     * <p>The return value is consist of the word ID and the length
     * of the matched part.
     * 
     * @param text the key
     * @param offset the offset of the key
     * @return the iterator of results
     */
    @Override
    public Iterator<int[]> lookup(byte[] text, int offset) {
        Iterator<int[]> iterator
            = trie.commonPrefixSearch(text, offset);
        if (!iterator.hasNext()) {
            return iterator;
        }
        return new Itr(iterator);
    }

    private class Itr implements Iterator<int[]> {
        private final Iterator<int[]> iterator;
        private Integer[] wordIds;
        private int length;
        private int index;

        Itr(Iterator<int[]> iterator) {
            this.iterator = iterator;
            index = -1;
        }

        @Override
        public boolean hasNext() {
            if (index < 0) {
                return iterator.hasNext();
            } else {
                return (index < wordIds.length) || iterator.hasNext();
            }
        }

        @Override
        public int[] next() {
            if (index < 0 || index >= wordIds.length) {
                int[] p = iterator.next();
                wordIds = wordIdTable.get(p[0]);
                length = p[1];
                index = 0;
            }
            return new int[] { wordIds[index++], length };
        }
    }


    @Override
    public short getLeftId(int wordId) {
        return wordParams.getLeftId(wordId);
    }

    @Override
    public short getRightId(int wordId) {
        return wordParams.getRightId(wordId);
    }

    @Override
    public short getCost(int wordId) {
        return wordParams.getCost(wordId);
    }

    @Override
    public WordInfo getWordInfo(int wordId) {
        return wordInfos.getWordInfo(wordId);
    }

    @Override
    public int getDictionaryId(int wordId) {
        return 0;
    }

    public void calculateCost(Tokenizer tokenizer) {
        for (int wordId = 0; wordId < wordParams.size(); wordId++) {
            if (getCost(wordId) != Short.MIN_VALUE) {
                continue;
            }
            String surface = getWordInfo(wordId).getSurface();
            MorphemeList ms = (MorphemeList)tokenizer.tokenize(surface);
            int cost = ms.getInternalCost()
                + USER_DICT_COST_PAR_MORPH * ms.size();
            if (cost > Short.MAX_VALUE) {
                cost = Short.MAX_VALUE;
            } else if (cost < Short.MIN_VALUE) {
                cost = Short.MIN_VALUE;
            }
            wordParams.setCost(wordId, (short)cost);
        }
    }
}

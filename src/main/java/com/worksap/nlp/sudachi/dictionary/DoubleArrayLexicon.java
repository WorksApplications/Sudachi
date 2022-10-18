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

package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;

import com.worksap.nlp.dartsclone.DoubleArray;
import com.worksap.nlp.sudachi.MorphemeList;
import com.worksap.nlp.sudachi.Tokenizer;

public class DoubleArrayLexicon implements Lexicon {
    static final int USER_DICT_COST_PAR_MORPH = -20;
    private final WordInfoList wordInfos;
    private final DoubleArray trie;
    private final WordParameters parameters;
    private final Description description;
    private final WordIdTable wordIdTable;
    private final CompactedStrings strings;


    public DoubleArrayLexicon(Description description, WordIdTable wordIdTable, WordParameters wordParams, WordInfoList wordInfos,
            DoubleArray trie, CompactedStrings strings) {
        this.description = description;
        this.wordIdTable = wordIdTable;
        this.parameters = wordParams;
        this.wordInfos = wordInfos;
        this.trie = trie;
        this.strings = strings;
    }

    public static DoubleArrayLexicon load(ByteBuffer bytes, Description header) {
        ByteBuffer trieBuf = header.slice(bytes, Blocks.TRIE_INDEX);
        DoubleArray da = new DoubleArray();
        IntBuffer array = trieBuf.asIntBuffer();
        da.setArray(array, array.limit());

        WordParameters parms;
        if (header.isRuntimeCosts()) {
            parms = WordParameters.readWrite(bytes, header);
        } else {
            parms = WordParameters.readOnly(bytes, header);
        }

        WordIdTable idTable = new WordIdTable(header.slice(bytes, Blocks.WORD_POINTERS));
        WordInfoList infos = new WordInfoList(header.slice(bytes, Blocks.ENTRIES));
        CompactedStrings strings = new CompactedStrings(header.slice(bytes, Blocks.STRINGS).asCharBuffer());

        return new DoubleArrayLexicon(header, idTable, parms, infos, da, strings);
    }

    /**
     * Returns the word IDs obtained by common prefix search.
     *
     * <p>
     * The search begin with the position at the {@code offset} of the {@code text}.
     *
     * <p>
     * The return value is consist of the word ID and the length of the matched
     * part.
     * 
     * @param text
     *            the key
     * @param offset
     *            the offset of the key
     * @return the iterator of results
     */
    @Override
    public Iterator<int[]> lookup(byte[] text, int offset) {
        Iterator<int[]> iterator = trie.commonPrefixSearch(text, offset);
        if (!iterator.hasNext()) {
            return iterator;
        }
        return new Itr(iterator);
    }

    public IntBuffer getTrieArray() {
        return trie.array();
    }

    public WordIdTable getWordIdTable() {
        return wordIdTable;
    }

    @Override
    public long parameters(int wordId) {
        return parameters.loadParams(wordId);
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
    public String string(int dic, int stringPtr) {
        return strings.string(stringPtr);
    }

    @Override
    public WordInfo getWordInfo(int wordId) {
        return wordInfos.getWordInfo(wordId);
    }

    @Override
    public int size() {
        return (int) description.getNumTotalEntries();
    }

    public Iterator<Ints> wordIds() {
        return wordIdTable.wordIds();
    }

    /**
     * Returns true if the cost value is a normal value which can be used as is.
     * Otherwise, it is a placeholder which needs to be recalculated
     * based on the content of the dictionary.
     * @param cost raw cost value
     * @return true a normal cost value
     */
    public static boolean isNormalCost(short cost) {
        return cost != Short.MIN_VALUE;
    }

    public void calculateDynamicCosts(Tokenizer tokenizer) {
        Iterator<Ints> outer = wordIdTable.wordIds();
        while (outer.hasNext()) {
            Ints values = outer.next();
            for (int i = 0; i < values.length(); ++i) {
                int wordId = values.get(i);
                if (isNormalCost(WordParameters.cost(parameters(wordId)))) {
                    continue;
                }
                int surfPtr = wordInfos.surfacePtr(wordId);
                String surface = strings.string(surfPtr);
                MorphemeList ms = tokenizer.tokenize(surface);
                int cost = ms.getInternalCost() + USER_DICT_COST_PAR_MORPH * ms.size();
                if (cost > Short.MAX_VALUE) {
                    cost = Short.MAX_VALUE;
                } else if (cost < Short.MIN_VALUE) {
                    cost = Short.MIN_VALUE;
                }
                parameters.setCost(wordId, (short) cost);
            }
        }
    }

    public void setDictionaryId(int id) {
        wordIdTable.setDictionaryId(id);
    }

    @Override
    public WordInfoList wordInfos(int dic) {
        return wordInfos;
    }
}

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

package com.worksap.nlp.sudachi.dictionary;

import com.worksap.nlp.sudachi.WordId;

import java.util.*;

public class LexiconSet implements Lexicon {
    static final int MAX_DICTIONARIES = 15;

    List<DoubleArrayLexicon> lexicons = new ArrayList<>();
    final short systemPartOfSpeechSize;
    List<Short> posOffsets = new ArrayList<>();

    public LexiconSet(Lexicon systemLexicon, short systemPartOfSpeechSize) {
        this.systemPartOfSpeechSize = systemPartOfSpeechSize;
        add(systemLexicon, (short) 0);
    }

    public void add(Lexicon lexicon, short posOffset) {
        DoubleArrayLexicon daLexicon = (DoubleArrayLexicon) lexicon;
        daLexicon.setDictionaryId(lexicons.size());
        lexicons.add(daLexicon);
        posOffsets.add(posOffset);
    }

    public boolean isFull() {
        return lexicons.size() >= MAX_DICTIONARIES;
    }

    @Override
    public Iterator<int[]> lookup(byte[] text, int offset) {
        if (lexicons.isEmpty()) {
            return Collections.emptyIterator();
        }
        if (lexicons.size() == 1) {
            return lexicons.get(0).lookup(text, offset);
        }
        return new Itr(text, offset, lexicons.size() - 1);
    }

    /**
     * Traverses Dictionaries in the reverse order.
     * <ul>
     * <li>First, user dictionaries, starting from the last one</li>
     * <li>Finally, the system dictionary</li>
     * </ul>
     *
     * Dictionaries have their word weights prioritized in the same manner
     */
    private class Itr implements Iterator<int[]> {
        byte[] text;
        int offset;
        int dictId;
        Iterator<int[]> iterator;

        Itr(byte[] text, int offset, int start) {
            this.text = text;
            this.offset = offset;
            dictId = start;
            iterator = lexicons.get(dictId).lookup(text, offset);
        }

        @Override
        public boolean hasNext() {
            while (!iterator.hasNext()) {
                int nextId = dictId - 1;
                if (nextId < 0) {
                    return false;
                }
                iterator = lexicons.get(nextId).lookup(text, offset);
                dictId = nextId;
            }
            return true;
        }

        @Override
        public int[] next() {
            if (hasNext()) {
                int[] r = iterator.next();
                r[0] = buildWordId(dictId, r[0]);
                return r;
            }
            throw new NoSuchElementException();
        }
    }

    @Override
    public WordInfo getWordInfo(int wordId) {
        int dictionaryId = WordId.dic(wordId);
        int internalId = WordId.word(wordId);
        WordInfo wordInfo = lexicons.get(dictionaryId).getWordInfo(internalId);
        short posId = wordInfo.getPOSId();
        if (dictionaryId > 0 && posId >= systemPartOfSpeechSize) { // user defined part-of-speech
            wordInfo.setPOSId((short) (wordInfo.getPOSId() - systemPartOfSpeechSize + posOffsets.get(dictionaryId)));
        }
        convertSplit(wordInfo.getAunitSplit(), dictionaryId);
        convertSplit(wordInfo.getBunitSplit(), dictionaryId);
        convertSplit(wordInfo.getWordStructure(), dictionaryId);
        return wordInfo;
    }

    @Override
    public int size() {
        return lexicons.stream().mapToInt(Lexicon::size).sum();
    }

    private static int getWordId(int wordId) {
        return WordId.word(wordId);
    }

    private int buildWordId(int dictId, int wordId) {
        if (dictId >= lexicons.size()) {
            throw new IndexOutOfBoundsException("dictionaryId is too large: " + dictId);
        }
        return WordId.make(dictId, wordId);
    }

    private void convertSplit(int[] split, int dictionaryId) {
        for (int i = 0; i < split.length; i++) {
            if (WordId.dic(split[i]) > 0) {
                split[i] = buildWordId(dictionaryId, getWordId(split[i]));
            }
        }
    }

    public WordLookup makeLookup() {
        return new WordLookup(this.lexicons);
    }

    @Override
    public long parameters(int wordId) {
        int dic = WordId.dic(wordId);
        return lexicons.get(dic).parameters(wordId);
    }

    @Override
    public String string(int dic, int stringPtr) {
        return lexicons.get(dic).string(dic, stringPtr);
    }

    @Override
    public WordInfoList wordInfos(int dic) {
        return lexicons.get(dic).wordInfos(dic);
    }
}

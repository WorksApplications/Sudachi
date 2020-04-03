/*
 * Copyright (c) 2020 Works Applications Co., Ltd.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class LexiconSet implements Lexicon {

    static final int MAX_DICTIONARIES = 16;

    List<Lexicon> lexicons = new ArrayList<>();
    List<Short> posOffsets = new ArrayList<>();

    public LexiconSet(Lexicon systemLexicon) {
        lexicons.add(systemLexicon);
        posOffsets.add((short) 0);
    }

    public void add(Lexicon lexicon, short posOffset) {
        if (!lexicons.contains(lexicon)) {
            lexicons.add(lexicon);
            posOffsets.add(posOffset);
        }
    }

    public boolean isFull() {
        return lexicons.size() >= MAX_DICTIONARIES;
    }

    @Override
    public Iterator<int[]> lookup(byte[] text, int offset) {
        if (lexicons.size() == 1) {
            return lexicons.get(0).lookup(text, offset);
        }
        return new Itr(text, offset);
    }

    private class Itr implements Iterator<int[]> {
        byte[] text;
        int offset;
        int dictId;
        Iterator<int[]> iterator;

        Itr(byte[] text, int offset) {
            this.text = text;
            this.offset = offset;
            dictId = 1;
            iterator = lexicons.get(dictId).lookup(text, offset);
        }

        @Override
        public boolean hasNext() {
            while (!iterator.hasNext()) {
                if (dictId == 0) {
                    return false;
                }
                dictId++;
                if (dictId >= lexicons.size()) {
                    dictId = 0;
                }
                iterator = lexicons.get(dictId).lookup(text, offset);
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
    public int getWordId(String headword, short posId, String readingForm) {
        for (int dictId = 1; dictId < lexicons.size(); dictId++) {
            int wid = lexicons.get(dictId).getWordId(headword, posId, readingForm);
            if (wid >= 0) {
                return buildWordId(dictId, wid);
            }
        }
        return lexicons.get(0).getWordId(headword, posId, readingForm);
    }

    @Override
    public short getLeftId(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getLeftId(getWordId(wordId));
    }

    @Override
    public short getRightId(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getRightId(getWordId(wordId));
    }

    @Override
    public short getCost(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getCost(getWordId(wordId));
    }

    @Override
    public WordInfo getWordInfo(int wordId) {
        int dictionaryId = getDictionaryId(wordId);
        WordInfo wordInfo = lexicons.get(dictionaryId).getWordInfo(getWordId(wordId));
        short posId = wordInfo.getPOSId();
        if (dictionaryId > 0 && posId >= posOffsets.get(1)) { // user defined part-of-speech
            wordInfo.setPOSId((short) (wordInfo.getPOSId() - posOffsets.get(1) + posOffsets.get(dictionaryId)));
        }
        convertSplit(wordInfo.getAunitSplit(), dictionaryId);
        convertSplit(wordInfo.getBunitSplit(), dictionaryId);
        convertSplit(wordInfo.getWordStructure(), dictionaryId);
        return wordInfo;
    }

    @Override
    public int getDictionaryId(int wordId) {
        return wordId >>> 28;
    }

    @Override
    public int size() {
        return lexicons.stream().mapToInt(Lexicon::size).sum();
    }

    private int getWordId(int wordId) {
        return 0x0fffffff & wordId;
    }

    private int buildWordId(int dictId, int wordId) {
        if (wordId > 0x0fffffff) {
            throw new IndexOutOfBoundsException("wordId is too large: " + wordId);
        }
        if (dictId >= lexicons.size()) {
            throw new IndexOutOfBoundsException("dictionaryId is too large: " + dictId);
        }
        return (dictId << 28) | wordId;
    }

    private void convertSplit(int[] split, int dictionaryId) {
        for (int i = 0; i < split.length; i++) {
            if (getDictionaryId(split[i]) > 0) {
                split[i] = buildWordId(dictionaryId, getWordId(split[i]));
            }
        }
    }
}

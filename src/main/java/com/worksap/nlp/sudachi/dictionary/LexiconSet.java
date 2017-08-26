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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class LexiconSet implements Lexicon {

    List<Lexicon> lexicons = new ArrayList<>();;

    public LexiconSet(Lexicon systemLexicon) {
        lexicons.add(systemLexicon);
    }

    public void add(Lexicon lexicon) {
        if (!lexicons.contains(lexicon)) {
            lexicons.add(lexicon);
        }
    }

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

    public short getLeftId(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getLeftId(getWordId(wordId));
    }

    public short getRightId(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getRightId(getWordId(wordId));
    }

    public short getCost(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getCost(getWordId(wordId));
    }

    public WordInfo getWordInfo(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getWordInfo(getWordId(wordId));
    }

    private int getDictionaryId(int wordId) {
        return wordId >>> 28;
    }

    private int getWordId(int wordId) {
        return 0x0fffffff & wordId;
    }

    private int buildWordId(int dictId, int wordId) {
        if (wordId > 0x0fffffff) {
            throw new RuntimeException("wordId is too large: " + wordId);
        }
        if (dictId > 0xf) {
            throw new RuntimeException("dictionaryId is too large: " + dictId);
        }
        return (dictId << 28) | wordId;
    }
}

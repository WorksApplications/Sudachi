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

import java.util.Iterator;

/**
 * The lexicon of morphemes.
 */
public interface Lexicon {

    Iterator<int[]> lookup(byte[] text, int offset);

    int getWordId(String headword, short posId, String readingForm);

    /**
     * Returns the left-ID of the morpheme specified by the word ID.
     *
     * <p>
     * when the word ID is out of range, the behavior is undefined.
     *
     * @param wordId
     *            the word ID of the morpheme
     * @return the left-ID of the morpheme
     */
    short getLeftId(int wordId);

    /**
     * Returns the right-ID of the morpheme specified by the word ID.
     *
     * <p>
     * when the word ID is out of range, the behavior is undefined.
     *
     * @param wordId
     *            the word ID of the morpheme
     * @return the right-ID of the morpheme.
     */
    short getRightId(int wordId);

    /**
     * Returns the word occurrence cost of the morpheme specified by the word ID.
     *
     * <p>
     * when the word ID is out of range, the behavior is undefined.
     *
     * @param wordId
     *            the word ID of the morpheme
     * @return the word occurrence cost
     */
    short getCost(int wordId);

    /**
     * Returns the informations of the morpheme specified by the word ID.
     *
     * <p>
     * when the word ID is out of range, the behavior is undefined.
     *
     * @param wordId
     *            the word ID of the morpheme
     * @return the informations of the morpheme
     * @see WordInfo
     */
    WordInfo getWordInfo(int wordId);

    /**
     * Returns the ID of the dictionary containing the morpheme specified by the
     * word ID.
     *
     * If the morpheme is in the system dictionary, it returns {@code 0}.
     *
     * @param wordId
     *            the word ID of the morpheme
     * @return the dictionary ID
     */
    int getDictionaryId(int wordId);

    /**
     * Returns the number of morphemes in the dictionary.
     *
     * @return the number of morphemes
     */
    int size();
}

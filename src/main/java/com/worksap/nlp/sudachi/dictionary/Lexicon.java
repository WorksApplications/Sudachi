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

import com.worksap.nlp.sudachi.WordId;

import java.util.Iterator;

/**
 * The lexicon of morphemes.
 */
public interface Lexicon {

    Iterator<int[]> lookup(byte[] text, int offset);

    /**
     * Return packed parameters for the morpheme with the given id.
     * Parameters are leftId, rightId, cost packed in a single long value.
     * @param wordId id of word to extract parameters
     * @return long value of packed parameters
     */
    long parameters(int wordId);

    /**
     * Returns the on-disk information of the morpheme specified by the word ID.
     *
     * <p>
     * when the word ID is out of range, the behavior is undefined.
     *
     * @param wordId
     *            the word ID of the morpheme
     * @return on-disk information for the morpheme with the given id
     * @see WordInfo
     */
    WordInfo getWordInfo(int wordId);

    /**
     * Returns the number of morphemes in the dictionary.
     *
     * @return the number of morphemes
     */
    int size();

    /**
     * Get the string with the given packed string pointer from the dictionary
     * @param dic dictionary id
     * @param stringPtr packed string pointer
     * @return String object value, copy of the in-memory representation
     * @see WordId#dic(int)
     */
    String string(int dic, int stringPtr);

    WordInfoList wordInfos(int dic);
}

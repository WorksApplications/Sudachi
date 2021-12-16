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

package com.worksap.nlp.sudachi;

public class WordId {
    private WordId() {
    }

    /**
     * Internal word ids can't be larger than this number
     */
    public static final int MAX_WORD_ID = 0x0fffffff;

    /**
     * Dictionary ids can't be larger than this number
     */
    public static final int MAX_DIC_ID = 0xe;

    public static int makeUnchecked(int dic, int word) {
        int dicPart = dic << 28;
        return dicPart | word;
    }

    /**
     * Make combined WordId from dictionary and internal parts. This method does
     * bound checking.
     *
     * @param dic
     *            dictionary id. 0 is system, 1 and above are user.
     * @param word
     *            word id inside the dictionary.
     * @return combined word id.
     */
    public static int make(int dic, int word) {
        if (word > MAX_WORD_ID) {
            throw new IndexOutOfBoundsException("wordId is too large: " + word);
        }
        if (dic > MAX_DIC_ID) {
            throw new IndexOutOfBoundsException("dictionaryId is too large: " + dic);
        }
        return makeUnchecked(dic, word);
    }

    /**
     * Extract dictionary number from the combined word id
     * 
     * @param wordId
     *            combined word id
     * @return dictionary number
     */
    public static int dic(int wordId) {
        return wordId >>> 28;
    }

    /**
     * Extract internal word id from the combined word id
     * 
     * @param wordId
     *            combined word id
     * @return internal word id
     */
    public static int word(int wordId) {
        return wordId & MAX_WORD_ID;
    }
}

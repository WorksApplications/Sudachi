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

/**
 * Utility to handle combined word id.
 * 
 * Combined word id (32 bits) consists of two parts, dictionary id (top 4 bit)
 * and dictionary-internal word id (rest bits).
 */
public class WordId {
    private WordId() {
    }

    /**
     * Internal word ids can't be larger than this number.
     */
    public static final int MAX_WORD_ID = 0x0fff_ffff;

    /**
     * Dictionary ids can't be larger than this number.
     * 
     * Dictionary id 0x0 is reserved for the system dictionary and 0xf is reserved
     * for oov and special words.
     */
    public static final int MAX_DIC_ID = 0xe;

    // ids for special tokens.
    public static final int ID_BOS = 0xffff_fff0;
    public static final int ID_EOS = 0xffff_fff1;
    public static final int ID_OOV_NOPOS = 0xf000_ffff;

    /**
     * Make combined WordId from dictionary and internal parts, without checking
     * bound.
     *
     * @param dic
     *            dictionary id. 0 is system, 1 and above are user.
     * @param word
     *            word id inside the dictionary.
     * @return combined word id.
     */
    public static int makeUnchecked(int dic, int word) {
        int dicPart = dicIdMask(dic);
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

    /** Make OOV WordId from provided pos id. */
    public static int makeOov(short posId) {
        return 0xf000_0000 | posId;
    }

    /**
     * Extract dictionary id from the combined word id
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

    /**
     * Encode dictionary id as a part of combined word id.
     */
    public static int dicIdMask(int dicId) {
        return dicId << 28;
    }

    /**
     * Override dictionary part of the word id using given dicIdMask.
     */
    public static int applyMask(int wordId, int dicIdMask) {
        return (wordId & MAX_WORD_ID) | dicIdMask;
    }

    /** Override dictionary part of the word id with given dic id. */
    public static int overrideDic(int wordId, int dicId) {
        return applyMask(wordId, dicIdMask(dicId));
    }

    /**
     * Resolve dic id to refer.
     * 
     * @param wordRef
     *            word ref taken from word entry.
     * @param actualDicId
     *            dic id of the dict which the word entry comes from.
     * @return dic id which the wordid referring to.
     */
    public static int refDic(int wordRef, int actualDicId) {
        // 1 if wordref refers to the entry inside same dict, 0 otherwise (i.e. refers
        // to system dict entry)
        boolean isReferringUser = dic(wordRef) == 1;
        if (isReferringUser) {
            return actualDicId;
        }
        return 0; // system dict id
    }

    /**
     * Fill flag part of word ref with actual dic id.
     * 
     * @param wordRef
     *            word ref taken from word entry.
     * @param actualDicId
     *            dic id of the dict which the word entry comes from.
     * @return dic id which the wordid referring to.
     */
    public static int resolveRef(int wordRef, int actualDicId) {
        boolean isReferringUser = dic(wordRef) == 1;
        if (isReferringUser) {
            return overrideDic(wordRef, actualDicId);
        }
        return wordRef; // dict part is 0 and thus no need to change.
    }

    /** @return if given word id represents OOV. */
    public static boolean isOov(int wordId) {
        // low 16 bits are OOV POS, top 4 are 1s
        return (wordId & 0xffff_0000) == 0xf000_0000;
    }

    /** @return if given word id represents special words. */
    public static boolean isSpecial(int wordId) {
        // top 5 bits should be filled
        return (wordId & 0xf800_0000) == 0xf800_0000;
    }
}

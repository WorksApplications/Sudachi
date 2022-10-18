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

import com.worksap.nlp.sudachi.StringUtil;

import java.nio.ByteBuffer;

/**
 * Internal morpheme information. This class does not contain any strings.
 *
 * <p>
 * This class holds morpheme data which is not used in the viterbi search.
 */
public class WordInfo {
    private final short headwordLength;
    private short posId;
    private final int surface;
    private final int reading;
    private final int normalizedForm;
    private final int dictionaryForm;
    private final int[] aUnitSplit;
    private final int[] bUnitSplit;
    private final int[] cUnitSplit;
    private final int[] wordStructure;
    private final int[] synonymGids;
    private final String userData;

    public WordInfo(short headwordLength, short posId, int surface, int reading, int normalizedForm, int dictionaryForm,
            int[] aUnitSplit, int[] bUnitSplit, int[] cUnitSplit, int[] wordStructure, int[] synonymGids,
            String userData) {
        this.headwordLength = headwordLength;
        this.posId = posId;
        this.surface = surface;
        this.reading = reading;
        this.normalizedForm = normalizedForm;
        this.dictionaryForm = dictionaryForm;
        this.aUnitSplit = aUnitSplit;
        this.bUnitSplit = bUnitSplit;
        this.cUnitSplit = cUnitSplit;
        this.wordStructure = wordStructure;
        this.synonymGids = synonymGids;
        this.userData = userData;
    }

    /**
     * Allocates morpheme information for ones not in the lexicon.
     * For example, OOVs.
     *
     * @param headwordLength
     *            the length of the morpheme
     * @param posId
     *            the ID of the part-of-speech of the morpheme
     */
    public WordInfo(short headwordLength, short posId) {
        this.headwordLength = headwordLength;
        this.posId = posId;
        this.surface = 0;
        this.normalizedForm = 0;
        this.dictionaryForm = 0;
        this.reading = 0;
        this.aUnitSplit = Ints.EMPTY_ARRAY;
        this.bUnitSplit = Ints.EMPTY_ARRAY;
        this.cUnitSplit = Ints.EMPTY_ARRAY;
        this.wordStructure = Ints.EMPTY_ARRAY;
        this.synonymGids = Ints.EMPTY_ARRAY;
        this.userData = "";
    }

    /**
     * Returns the text of the morpheme.
     *
     * @return the text of the morpheme
     */
    public int getSurface() {
        return surface;
    }

    /**
     * Returns the length of the text in internal use unit.
     *
     * <p>
     * This length is used to place a node in the
     * {@link com.worksap.nlp.sudachi.Lattice}, does not equals
     * {@code getSurface().length()}.
     *
     * @return the length of the text
     */
    public short getLength() {
        return headwordLength;
    }

    /**
     * Returns the part-of-speech ID of the morpheme.
     * The strings of part-of-speech name can be gotten with
     * {@link Grammar#getPartOfSpeechString}.
     * 
     * @return the POS ID
     */
    public short getPOSId() {
        return posId;
    }

    /**
     * Sets the part-of-speech ID of the morpheme.
     *
     * @param posId
     *            the POS ID
     */
    public void setPOSId(short posId) {
        this.posId = posId;
    }

    /**
     * Returns the entry id of the normalized form of the morpheme.
     *
     * @return the normalized form of the morpheme
     */
    public int getNormalizedForm() {
        return normalizedForm;
    }

    /**
     * Returns the word ID of the dictionary form of the morpheme.
     * The information of the dictionary form can be gotten with
     * {@link Lexicon#getWordInfo}
     *
     * @return the word ID of the dictionary form of the morpheme
     */
    public int getDictionaryForm() {
        return dictionaryForm;
    }

    /**
     * Returns the raw string pointer to the reading form of the morpheme.
     *
     * @return raw string pointer of the reading form
     * @see StringPtr
     */
    public int getReadingForm() {
        return reading;
    }

    /**
     * Returns the array of word IDs which the morpheme is compounded of in A mode.
     *
     * @return the word IDs of A units
     */
    public int[] getAunitSplit() {
        return aUnitSplit;
    }

    /**
     * Returns the array of word IDs which the morpheme is compounded of in B mode.
     *
     * @return the word IDs of B units
     */
    public int[] getBunitSplit() {
        return bUnitSplit;
    }

    /**
     * Returns the array of the morphemes which the morpheme is compounded of.
     *
     * @return the word IDs of the constituents of the morpheme
     */
    public int[] getWordStructure() {
        return wordStructure;
    }

    /**
     * Returns the array of the synonym groups.
     *
     * @return the synonym group IDs of the morpheme
     * @deprecated use {@link #getSynonymGroupIds()}, this method has a typo in its
     *             name
     */
    @Deprecated
    public int[] getSynonymGoupIds() {
        return getSynonymGroupIds();
    }

    /**
     * Returns the array of the synonym groups.
     *
     * @return the synonym group IDs of the morpheme
     */
    public int[] getSynonymGroupIds() {
        return synonymGids;
    }

    public String getUserData() {
        return userData;
    }

    public static int surfaceForm(ByteBuffer buffer, int pos) {
        return buffer.getInt(pos + 8);
    }

    public static int readingForm(ByteBuffer buffer, int pos) {
        return buffer.getInt(pos + 12);
    }


    private WordInfo(ByteBuffer buffer, int pos) {
        // short leftId = buffer.getShort(pos);
        // short rightId = buffer.getShort(pos + 2);
        // short cost = buffer.getShort(pos + 4);
        // do not modify buffer metadata for better performance
        posId = buffer.getShort(pos + 6);
        surface = surfaceForm(buffer, pos); // +8
        reading = readingForm(buffer, pos); // +12
        normalizedForm = buffer.getInt(pos + 16);
        dictionaryForm = buffer.getInt(pos + 20);
        long rest = buffer.getLong(pos + 24);
        headwordLength = (short) (rest & 0xffff);
        rest >>>= 16;
        if (rest == 0) {
            cUnitSplit = Ints.EMPTY_ARRAY;
            bUnitSplit = Ints.EMPTY_ARRAY;
            aUnitSplit = Ints.EMPTY_ARRAY;
            wordStructure = Ints.EMPTY_ARRAY;
            synonymGids = Ints.EMPTY_ARRAY;
            userData = "";
            return;
        }
        int cSplitLen = (int) ((rest) & 0xff);
        int bSplitLen = (int) ((rest >>> 8) & 0xff);
        int aSplitLen = (int) ((rest >>> 16) & 0xff);
        int wordStructureLen = (int) ((rest >>> 24) & 0xff);
        int synonymLen = (int) ((rest >>> 32) & 0xff);
        int userDataFlag = (int) ((rest >>> 40) & 0xff);
        int offset = pos + 32;
        cUnitSplit = Ints.readArray(buffer, offset, cSplitLen);
        offset += cSplitLen * 4;
        if (bSplitLen == 0xff) {
            bUnitSplit = cUnitSplit;
        } else {
            bUnitSplit = Ints.readArray(buffer, offset, bSplitLen);
            offset += bSplitLen * 4;
        }
        if (aSplitLen == 0xff) {
            aUnitSplit = bUnitSplit;
        } else {
            aUnitSplit = Ints.readArray(buffer, offset, aSplitLen);
            offset += aSplitLen * 4;
        }
        if (wordStructureLen == 0xff) {
            wordStructure = aUnitSplit;
            offset += wordStructureLen * 4;
        } else {
            wordStructure = Ints.readArray(buffer, offset, wordStructureLen);
        }
        synonymGids = Ints.readArray(buffer, offset, synonymLen);

        if (userDataFlag != 0) {
            userData = StringUtil.readLengthPrefixed(buffer);
        } else {
            userData = "";
        }
    }

    public static WordInfo read(ByteBuffer buffer, int pos) {
        return new WordInfo(buffer, pos);
    }
}

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

/**
 * Informations of the morpheme.
 *
 * <p>
 * This class has the informations which are not used in the graph calculation.
 */
public class WordInfo {

    private final String surface;
    private final short headwordLength;
    private short posId;
    private final String normalizedForm;
    private final int dictionaryFormWordId;
    private final String dictionaryForm;
    private final String readingForm;
    private final int[] aUnitSplit;
    private final int[] bUnitSplit;
    private final int[] wordStructure;
    private final int[] synonymGids;

    WordInfo(String surface, short headwordLength, short posId, String normalizedForm, int dictionaryFormWordId,
            String dictionaryForm, String readingForm, int[] aUnitSplit, int[] bUnitSplit, int[] wordStructure,
            int[] synonymGids) {
        this.surface = surface;
        this.headwordLength = headwordLength;
        this.posId = posId;
        this.normalizedForm = normalizedForm;
        this.dictionaryFormWordId = dictionaryFormWordId;
        this.dictionaryForm = dictionaryForm;
        this.readingForm = readingForm;
        this.aUnitSplit = aUnitSplit;
        this.bUnitSplit = bUnitSplit;
        this.wordStructure = wordStructure;
        this.synonymGids = synonymGids;
    }

    /**
     * Allocates informations of morpheme not in the lexicons.
     *
     * @param surface
     *            the text of the morpheme
     * @param headwordLength
     *            the length of the morpheme
     * @param posId
     *            the ID of the part-of-speech of the morpheme
     * @param normalizedForm
     *            the normalized form of the morpheme
     * @param dictionaryForm
     *            the dictionary form of the morpheme
     * @param readingForm
     *            the reading form of the morpheme
     */
    public WordInfo(String surface, short headwordLength, short posId, String normalizedForm, String dictionaryForm,
            String readingForm) {
        this.surface = surface;
        this.headwordLength = headwordLength;
        this.posId = posId;
        this.normalizedForm = normalizedForm;
        this.dictionaryFormWordId = -1;
        this.dictionaryForm = dictionaryForm;
        this.readingForm = readingForm;
        this.aUnitSplit = new int[0];
        this.bUnitSplit = new int[0];
        this.wordStructure = new int[0];
        this.synonymGids = new int[0];
    }

    /**
     * Returns the text of the morpheme.
     *
     * @return the text of the morpheme
     */
    public String getSurface() {
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
     *
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
     * Returns the normalized form of the morpheme.
     *
     * @return the normalized form of the morpheme
     */
    public String getNormalizedForm() {
        return normalizedForm;
    }

    /**
     * Returns the word ID of the dictionary form of the morpheme.
     *
     * The information of the dictionary form can be gotten with
     * {@link Lexicon#getWordInfo}
     *
     * @return the word ID of the dictionary form of the morpheme
     */
    public int getDictionaryFormWordId() {
        return dictionaryFormWordId;
    }

    /**
     * Returns the dictionary form of the morpheme.
     *
     * @return the dictionary form of the morpheme
     */
    public String getDictionaryForm() {
        return dictionaryForm;
    }

    /**
     * Returns the reading form of the morpheme.
     *
     * @return the reading form of the morpheme
     */
    public String getReadingForm() {
        return readingForm;
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
     */
    public int[] getSynonymGoupIds() {
        return synonymGids;
    }
}

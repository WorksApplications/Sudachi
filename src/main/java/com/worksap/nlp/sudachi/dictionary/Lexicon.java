package com.worksap.nlp.sudachi.dictionary;

import java.util.Iterator;

/**
 * The lexicon of morphemes.
 */
public interface Lexicon {

    Iterator<int[]> lookup(byte[] text, int offset);

    /**
     * Returns the left-ID of the morpheme specified by the word ID.
     *
     * <p>when the word ID is out of range, the behavior is undefined.
     *
     * @param wordId the word ID of the morpheme
     * @return the left-ID of the morpheme
     */
    short getLeftId(int wordId);

    /**
     * Returns the right-ID of the morpheme specified by the word ID.
     *
     * <p>when the word ID is out of range, the behavior is undefined.
     *
     * @param wordId the word ID of the morpheme
     * @return the right-ID of the morpheme.
     */
    short getRightId(int wordId);

    /**
     * Returns the word occurrence cost of the morpheme specified
     * by the word ID.
     *
     * <p>when the word ID is out of range, the behavior is undefined.
     *
     * @param wordId the word ID of the morpheme
     * @return the word occurrence cost
     */
    short getCost(int wordId);

    /**
     * Returns the informations of the morpheme specified by the word ID.
     *
     * <p>when the word ID is out of range, the behavior is undefined.
     *
     * @param wordId the word ID of the morpheme
     * @return the informations of the morpheme
     * @see WordInfo
     */
    WordInfo getWordInfo(int wordId);
}

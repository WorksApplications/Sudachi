package com.worksap.nlp.sudachi;

import java.util.List;

/**
 * A morpheme.
 */
public interface Morpheme {

    /**
     * Returns the start index of the morpheme.
     *
     * When the input text is normalized, some morphemes have
     * the same start index.
     *
     * @return the index of first character of the morpheme
     */
    public int begin();

    /**
     * Returns the offset after the last character of the morpheme.
     *
     * When the input text is normalized, some morphemes have
     * the same end index.
     *
     * @return the offset after the last character of the morpheme
     */
    public int end();

    /**
     * Returns the text of morpheme.
     *
     * When the input text is normalized, some morphemes have
     * the same surface.
     *
     * @return the text of morpheme
     */
    public String surface();

    /**
     * Returns the part of speech of the morpheme.
     *
     * @return the part of speech of the morpheme
     */
    public List<String> partOfSpeech();

    /**
     * Returns the dictionary form of morpheme.
     *
     * 'Dictionary form' means a word's lemma and '終止形' in Japanese.
     *
     * @return the dictionary form of morpheme
     */
    public String dictionaryForm();

    /**
     * Returns the normalized form of morpheme.
     *
     * This method returns the form normalizing inconsistent spellings and
     * inflected forms.
     *
     * @return the normalized form of morpheme
     */
    public String normalizedForm();

    /**
     * Returns the reading form of morpheme.
     *
     * This method returns Japanese syllabaries 'フリガナ' in katakana.
     *
     * If the morpheme is OOV, it returns a empty string.
     *
     * @return the reading form of morpheme.
     */
    public String readingForm();

    /**
     * Split the morpheme in another splitting mode.
     *
     * If {@code mode} is the same with using in
     * {@link Tokenizer#tokenize(Tokenizer.SplitMode,String)} or no more
     * splitting, this method returns {@code this}.
     *
     * @param mode a mode of splitting
     * @return the list of splitted morphemes
     * @see Tokenizer#tokenize(Tokenizer.SplitMode,String)
     */
    public List<Morpheme> split(Tokenizer.SplitMode mode);

    /**
     * Returns whether the morpheme is out-of-vocabulary (OOV) or not.
     *
     * @return {@code true} if, and only if the morpheme is OOV
     */
    public boolean isOOV();
}

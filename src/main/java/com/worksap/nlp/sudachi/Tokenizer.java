package com.worksap.nlp.sudachi;

import java.io.PrintStream;
import java.util.List;

/**
 * A tokenizer of morphological analysis.
 */
public interface Tokenizer {

    /**
     * Tokenize a text.
     *
     * This method tokenizes a input text as a sentence.
     * When the text is long, it required a lot of memory.
     *
     * @param mode a mode of splitting
     * @param text input text
     * @return  a result of tokenizing
     */
    public List<Morpheme> tokenize(SplitMode mode, String text);

    /**
     * Tokenize a text.
     *
     * Tokenize a text with {@link SplitMode}.C.
     *
     * @param text input text
     * @return  a result of tokenizing
     * @see #tokenize(SplitMode,String)
     */
    public default List<Morpheme> tokenize(String text) {
        return tokenize(SplitMode.C, text);
    }

    /**
     * Prints a lattice structure of analyzing.
     *
     * @param output an output of printing
     */
    public void setDumpOutput(PrintStream output);

    /**
     * A mode of splitting
     */
    public static enum SplitMode {
        /** short mode */
        A,

        /** middle mode */
        B,

        /** long mode */
        C,
    }
}

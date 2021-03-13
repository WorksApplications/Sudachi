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

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.List;

/**
 * A tokenizer of morphological analysis.
 */
public interface Tokenizer {

    /**
     * Tokenize a text.
     *
     * This method tokenizes a input text as a sentence. When the text is long, it
     * required a lot of memory.
     *
     * @param mode
     *            a mode of splitting
     * @param text
     *            input text
     * @return a result of tokenizing
     */
    public List<Morpheme> tokenize(SplitMode mode, String text);

    /**
     * Tokenize a text.
     *
     * Tokenize a text with {@link SplitMode}.C.
     *
     * @param text
     *            input text
     * @return a result of tokenizing
     * @see #tokenize(SplitMode,String)
     */
    public default List<Morpheme> tokenize(final String text) {
        return tokenize(SplitMode.C, text);
    }

    /**
     * Tokenize sentences.
     *
     * This method divide a input text into sentences and tokenizes them.
     *
     * @param mode
     *            a mode of splitting
     * @param text
     *            input text
     * @return a result of tokenizing
     */
    public Iterable<List<Morpheme>> tokenizeSentences(SplitMode mode, String text);

    /**
     * Tokenize sentences.
     *
     * This method divide a input text into sentences and tokenizes them with
     * {@link SplitMode}.C.
     *
     * @param text
     *            input text
     * @return a result of tokenizing
     * @see #tokenizeSentences(SplitMode,String)
     */
    public default Iterable<List<Morpheme>> tokenizeSentences(String text) {
        return tokenizeSentences(SplitMode.C, text);
    }

    /**
     * Tokenize sentences.
     *
     * This method reads a input text from {@code input} and divides it into
     * sentences and tokenizes them.
     *
     * @param mode
     *            a mode of splitting
     * @param input
     *            a reader of input text
     * @return a result of tokenizing
     * @throws IOException
     *             if reading a stream is failed
     */
    public Iterable<List<Morpheme>> tokenizeSentences(SplitMode mode, Reader input) throws IOException;

    /**
     * Tokenize sentences.
     *
     * This method reads a input text from {@code input} and divides it into
     * sentences and tokenizes them with {@link SplitMode}.C.
     *
     * @param input
     *            a reader of input text
     * @return a result of tokenizing
     * @throws IOException
     *             if reading a stream is failed
     * @see #tokenizeSentences(SplitMode,Reader)
     */
    public default Iterable<List<Morpheme>> tokenizeSentences(Reader input) throws IOException {
        return tokenizeSentences(SplitMode.C, input);
    }

    /**
     * Prints a lattice structure of analyzing.
     *
     * @param output
     *            an output of printing
     */
    public void setDumpOutput(PrintStream output);

    /**
     * A mode of splitting
     */
    public enum SplitMode {
        /** short mode */
        A,

        /** middle mode */
        B,

        /** long mode */
        C,
    }
}

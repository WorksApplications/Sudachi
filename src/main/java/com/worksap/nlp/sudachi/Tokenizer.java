/*
 * Copyright (c) 2021-2024 Works Applications Co., Ltd.
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
import java.util.Iterator;
import java.util.List;

/**
 * A tokenizer of morphological analysis.
 */
public interface Tokenizer {

    /**
     * Tokenize a text. This method tokenizes an input text as a sentence. When the
     * text is long, it uses a lot of memory.
     *
     * @param mode
     *            a mode of splitting
     * @param text
     *            input text
     * @return a result of tokenizing
     */
    MorphemeList tokenize(SplitMode mode, String text);

    /**
     *
     * Tokenize a text with {@link SplitMode}.C.
     *
     * @param text
     *            input text
     * @return a result of tokenizing
     * @see #tokenize(SplitMode,String)
     */
    default MorphemeList tokenize(final String text) {
        return tokenize(SplitMode.C, text);
    }

    /**
     * Tokenize sentences. This method divide an input text into sentences and
     * tokenizes them. When the text is long, it uses a lot of memory.
     *
     * @param mode
     *            a mode of splitting
     * @param text
     *            input text
     * @return a result of tokenizing
     */
    Iterable<MorphemeList> tokenizeSentences(SplitMode mode, String text);

    /**
     * Tokenize sentences. Divide an input text into sentences and tokenize them
     * with {@link SplitMode}.C. When the text is long, it uses a lot of memory.
     *
     * @param text
     *            input text
     * @return a result of tokenizing
     * @see #tokenizeSentences(SplitMode,String)
     */
    default Iterable<MorphemeList> tokenizeSentences(String text) {
        return tokenizeSentences(SplitMode.C, text);
    }

    /**
     * Read an input text from {@code input}, divide it into sentences and tokenize
     * them. It reads the input lazily.
     *
     * @param mode
     *            a mode of splitting
     * @param input
     *            a readable input text
     * @return an iterator of tokenized sentences
     */
    Iterator<List<Morpheme>> tokenizeSentences(SplitMode mode, Readable input);

    /**
     * Read an input text from {@code input}, divide it into sentences and tokenize
     * them with {@link SplitMode}.C. It reads the input lazily.
     *
     * @param input
     *            a readable input text
     * @return an iterator of tokenized sentences
     * @see #tokenizeSentences(SplitMode,Readable)
     */
    default Iterator<List<Morpheme>> tokenizeSentences(Readable input) {
        return tokenizeSentences(SplitMode.C, input);
    }

    /**
     * Read an input text from {@code input}, divide it into sentences and tokenize
     * them. It reads the input lazily.
     *
     * @param mode
     *            a mode of splitting
     * @param input
     *            a readable input text
     * @return a result of tokenizing
     * @deprecated renamed to {@link #tokenizeSentences(SplitMode, Readable)}
     * 
     */
    @Deprecated
    Iterator<List<Morpheme>> lazyTokenizeSentences(SplitMode mode, Readable input);

    /**
     * Read an input text from {@code input}, divide it into sentences and tokenize
     * them with {@link SplitMode}.C. It reads the input lazily.
     *
     * @param input
     *            a readable input text
     * @return a result of tokenizing
     * @see #lazyTokenizeSentences(SplitMode,Readable)
     * @deprecated renamed to {@link #tokenizeSentences(Readable)}
     */
    @Deprecated
    default Iterator<List<Morpheme>> lazyTokenizeSentences(Readable input) {
        return lazyTokenizeSentences(SplitMode.C, input);
    }

    /**
     * Prints lattice structure of the analysis into the passed {@link PrintStream}.
     *
     * @param output
     *            an output of printing
     */
    void setDumpOutput(PrintStream output);

    /**
     * Tokenize a text and dump the internal structures into a JSON string. This
     * method tokenizes an input text as a single sentence.
     *
     * @param text
     *            input text
     * @return a JSON string
     */
    String dumpInternalStructures(String text);

    /**
     * A mode of splitting
     */
    enum SplitMode {
        /** short mode */
        A,

        /** middle mode */
        B,

        /** long mode */
        C,
    }
}

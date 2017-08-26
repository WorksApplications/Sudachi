/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

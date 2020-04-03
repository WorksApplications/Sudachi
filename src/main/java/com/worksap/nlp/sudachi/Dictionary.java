/*
 * Copyright (c) 2020 Works Applications Co., Ltd.
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
import java.util.List;

/**
 * A lexicon and a grammar for morphological analysis.
 *
 * This class requires a lot of memory. When using multiple analyzers, it is
 * recommended to generate only one instance of this class, and generate
 * multiple tokenizers.
 *
 * @see DictionaryFactory
 * @see Tokenizer
 * @see AutoCloseable
 */
public interface Dictionary extends AutoCloseable {

    /**
     * Creates a tokenizer instance.
     *
     * @return a tokenizer
     */
    public Tokenizer create();

    @Override
    public void close() throws IOException;

    /**
     * Returns the number of types of part-of-speech.
     *
     * The IDs of part-of-speech are within the range of 0 to
     * {@code getPartOfSpeechSize() - 1}.
     *
     * @return the number of types of part-of-speech
     */
    public int getPartOfSpeechSize();

    /**
     * Returns the array of strings of part-of-speech name.
     *
     * The name is divided into layers.
     *
     * @param posId
     *            the ID of the part-of-speech
     * @return the list of strings of part-of-speech name
     * @throws IndexOutOfBoundsException
     *             if {@code posId} is out of the range
     */
    public List<String> getPartOfSpeechString(short posId);
}

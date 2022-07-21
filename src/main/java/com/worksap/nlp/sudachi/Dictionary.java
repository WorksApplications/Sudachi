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

import com.worksap.nlp.sudachi.dictionary.POS;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

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

    /**
     * Create a POS matcher that will match any of POS for which the passed
     * predicate returns true. PosMatcher will be much faster than doing string
     * comparison on POS objects.
     *
     * @param predicate
     *            returns true if the POS is needed
     * @return PosMatcher object that mirrors behavior of the predicate
     */
    PosMatcher posMatcher(Predicate<POS> predicate);

    /**
     * Create a POS matcher that will mirror matching behavior of passed list of
     * partially-defined POS.
     * 
     * @param posList
     *            list of partially defined part-of-speech objects
     * @return mirroring PosMatcher object
     * @see PartialPOS
     */
    default PosMatcher posMatcher(Iterable<PartialPOS> posList) {
        return posMatcher(posRepr -> {
            for (PartialPOS p : posList) {
                if (p.matches(posRepr)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Create a POS matcher that will mirror matching behavior of passed list of
     * partially-defined POS.
     * 
     * @param posList
     *            list of partially defined part-of-speech objects
     * @return mirroring PosMatcher object
     * @see PartialPOS
     */
    default PosMatcher posMatcher(PartialPOS... posList) {
        return posMatcher(Arrays.asList(posList));
    }
}

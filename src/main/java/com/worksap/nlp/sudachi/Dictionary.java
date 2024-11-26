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
    public Tokenizer tokenizer();

    /**
     * Creates a tokenizer instance.
     *
     * @return a tokenizer
     * 
     * @deprecated renamed to {@link tokenizer()}
     */
    @Deprecated
    public Tokenizer create();

    @Override
    public void close() throws IOException;

    /**
     * Lookup entries in the dictionary without performing an analysis.
     * 
     * Specified surface will be normalized. This will work like performing analysis
     * on the given headword and find paths with a single morpheme, but returns all
     * paths instead of the lowest cost one.
     * 
     * @param surface
     *            to lookup. Will be normalized beforehand.
     * @return a list of morphemes that match the surface. Their begin/end will be
     *         0/length of their headword.
     */
    public List<Morpheme> lookup(CharSequence surface);

    /**
     * Create an out-of-vocabulary morpheme from the pos id and string forms.
     * 
     * Begin/end will be set based on the surface.
     * 
     * @param posId
     *            part-of-speech id of the morpheme
     * @param surface
     *            surface of the morpheme
     * @param reading
     *            reading form of the morpheme
     * @param normalizedForm
     *            normalized form of the morpheme
     * @param dictionaryForm
     *            dictionary form of the morpheme
     * @return an oov morpheme with given information
     */
    public Morpheme oovMorpheme(short posId, String surface, String reading, String normalizedForm,
            String dictionaryForm);

    /**
     * Create an out-of-vocabulary morpheme from the pos id and the surface.
     * 
     * Use the surface to for other string forms. Begin/end will be set based on the
     * surface.
     * 
     * @param posId
     *            part-of-speech id of the morpheme
     * @param surface
     *            surface of the morpheme
     * @return an oov morpheme with given information
     */
    public default Morpheme oovMorpheme(short posId, String surface) {
        return oovMorpheme(posId, surface, surface, surface, surface);
    }

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

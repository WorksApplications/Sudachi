/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * API for checking if a morpheme belongs to a set of part of speech. Use
 * factory methods of {@link Dictionary} object to create instances.
 * 
 * @see Dictionary#posMatcher(Predicate)
 * @see Dictionary#posMatcher(Iterable)
 */
public class PosMatcher implements Predicate<Morpheme>, Iterable<POS> {
    private final BitSet matching;
    private final JapaneseDictionary dictionary;

    /**
     * Creates a PosMatcher for a given Dictionary and list of POS id. This is a
     * low-level API, use factory method on {@link Dictionary} instead
     * 
     * @param ids
     *            list of POS ids
     * @param dictionary
     *            related dictionary
     */
    public PosMatcher(int[] ids, JapaneseDictionary dictionary) {
        BitSet bits = new BitSet();
        for (int id : ids) {
            bits.set(id);
        }
        matching = bits;
        this.dictionary = dictionary;
    }

    private PosMatcher(BitSet data, JapaneseDictionary dictionary) {
        this.matching = data;
        this.dictionary = dictionary;
    }

    /**
     * Returns a PosMatcher which matches POS present in any of matchers
     * 
     * @param other
     *            second matcher
     * @return PosMatcher which matches union of POS tags
     */
    public PosMatcher union(PosMatcher other) {
        checkCompatibility(other);
        BitSet merged = new BitSet();
        merged.or(matching);
        merged.or(other.matching);
        return new PosMatcher(merged, dictionary);
    }

    /**
     * Returns a PosMatcher which matches POS present in both of matchers
     * 
     * @param other
     *            second matcher
     * @return PosMatcher which matches intersection of POS tags
     */
    public PosMatcher intersection(PosMatcher other) {
        checkCompatibility(other);
        BitSet merged = new BitSet();
        merged.or(matching);
        merged.and(other.matching);
        return new PosMatcher(merged, dictionary);
    }

    /**
     * Returns a PosMatcher for POS not present in current PosMatcher
     * 
     * @return PosMatcher which is inverse of this
     */
    public PosMatcher invert() {
        // bitset can be shorter than number of ids, so we create the full id range and
        // filter matching items
        int[] indices = IntStream.range(0, dictionary.getPartOfSpeechSize()).filter(idx -> !matching.get(idx))
                .toArray();
        return new PosMatcher(indices, dictionary);
    }

    private void checkCompatibility(PosMatcher other) {
        if (dictionary != other.dictionary) {
            throw new IllegalArgumentException("PosMatchers are using different dictionaries");
        }
    }

    /**
     * Checks that {@link Morpheme} matches the POS configuration. It is incorrect
     * to pass the Morpheme produced from other {@link Dictionary} than the one
     * which was used to create the current instance of {@link PosMatcher}.
     *
     * When assertions are enabled, this method checks if Morpheme was produced by
     * the same dictionary.
     * 
     * @param morpheme
     *            the input argument
     * @return true if morpheme matches the current configuration, false otherwise
     */
    @Override
    public boolean test(Morpheme morpheme) {
        assert ((MorphemeImpl) morpheme).list.grammar == dictionary.grammar;
        return matching.get(morpheme.partOfSpeechId());
    }

    /**
     * Iterates POS tags which are matched by this matcher
     * 
     * @return Iterator for POS tags
     */
    @Override
    public Iterator<POS> iterator() {
        return new Iterator<POS>() {
            private int index = matching.nextSetBit(0);

            @Override
            public boolean hasNext() {
                return index >= 0;
            }

            @Override
            public POS next() {
                if (index < 0) {
                    throw new NoSuchElementException();
                }
                short posId = (short) index;
                POS result = dictionary.getGrammar().getPartOfSpeechString(posId);
                index = matching.nextSetBit(index + 1);
                return result;
            }
        };
    }
}

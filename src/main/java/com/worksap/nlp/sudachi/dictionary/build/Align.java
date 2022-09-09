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

package com.worksap.nlp.sudachi.dictionary.build;

public class Align {

    private Align() {
    }

    public static boolean isPowerOf2(long value) {
        return (value & value - 1) == 0;
    }

    /**
     * Aligns value to the alignment
     * 
     * @param value
     *            value to be aligned
     * @param alignment
     *            required alignment as a power of two
     * @return aligned value, it should be greater or equal than the passed value
     */
    public static int align(int value, int alignment) {
        return (int) align((long) value, alignment);
    }

    /**
     * Aligns value to the alignment
     *
     * @param value
     *            value to be aligned
     * @param alignment
     *            required alignment as a power of two
     * @return aligned value, it should be greater or equal than the passed value
     */
    public static long align(long value, long alignment) {
        assert isPowerOf2(alignment);
        assert value >= 0;

        // Compute alignment mask, it is the inverse of the mask for the bits that must
        // be 0 for alignment to be correct
        // Checking mask is computed as alignment - 1. E.g. 7 for alignment of 8, or 15
        // for alignment of 16.
        // The second one is its inverse.
        long bits = alignment - 1;
        long mask = ~bits;
        return (value + bits) & mask;
    }
}

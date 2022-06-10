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

public class WordMask {
    public static final int MAX_LENGTH = 63;

    // instance creation is forbidden
    private WordMask() {

    }

    /**
     * Add n-th element to wordMask
     * 
     * @param positions
     *            current mask of word positions
     * @param position
     *            new position to add
     * @return position mask with the new element added
     */
    public static long addNth(long positions, int position) {
        return positions | nth(position);
    }

    /**
     * Create a word mask with nth position set
     * 
     * @param position
     *            number of set position
     * @return a word mask bitset
     */
    public static long nth(int position) {
        assert position > 0;
        int fixedPosition = Math.min(position - 1, MAX_LENGTH);
        return 1L << fixedPosition;
    }

    /**
     * Checks that a word mask has nth position set
     * 
     * @param positions
     *            word mask of positions
     * @param position
     *            position to check
     * @return whether the checked position was included in the set
     */
    public static boolean hasNth(long positions, int position) {
        return (positions & nth(position)) != 0;
    }
}

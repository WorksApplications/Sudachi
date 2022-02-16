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

package com.worksap.nlp.sudachi.dictionary;

import java.nio.IntBuffer;

/**
 * This class implements common prefix lookup in the double array with a
 * different API. It uses fields to return current values of end offset and a
 * value stored in trie to reduce GC pressure. It also modifies the hot loop to
 * reduce the number of non-elidable field writes.
 */
public final class DoubleArrayLookup {
    private IntBuffer array;
    private byte[] key;
    private int limit;
    private int startOffset;
    private int offset;
    private int nodePos;
    private int nodeValue;

    public DoubleArrayLookup() {
        this(null);
    }

    public DoubleArrayLookup(IntBuffer array) {
        this.array = array;
    }

    public DoubleArrayLookup(IntBuffer array, byte[] key, int offset, int limit) {
        this(array);
        reset(key, offset, limit);
    }

    private static boolean hasLeaf(int unit) {
        return ((unit >>> 8) & 1) == 1;
    }

    private static int value(int unit) {
        return unit & ((1 << 31) - 1);
    }

    private static int label(int unit) {
        return unit & ((1 << 31) | 0xFF);
    }

    private static int offset(int unit) {
        return ((unit >>> 10) << ((unit & (1 << 9)) >>> 6));
    }

    public void setArray(IntBuffer array) {
        this.array = array;
        reset(this.key, this.startOffset, this.limit);
    }

    public void reset(byte[] key, int offset, int limit) {
        this.key = key;
        this.offset = offset;
        this.startOffset = offset;
        this.limit = limit;
        nodePos = 0;
        int unit = array.get(nodePos);
        nodePos ^= offset(unit);
    }

    public boolean next() {
        IntBuffer array = this.array;
        byte[] key = this.key;
        int nodePos = this.nodePos;
        int limit = this.limit;

        for (int offset = this.offset; offset < limit; ++offset) {
            int k = Byte.toUnsignedInt(key[offset]);
            nodePos ^= k;
            int unit = array.get(nodePos);
            if (label(unit) != k) {
                this.offset = limit; // no more loop
                this.nodePos = nodePos;
                return false;
            }

            nodePos ^= offset(unit);
            if (hasLeaf(unit)) {
                nodeValue = value(array.get(nodePos));
                this.offset = offset + 1;
                this.nodePos = nodePos;
                return true;
            }
        }
        return false;
    }

    public int getValue() {
        return nodeValue;
    }

    public int getOffset() {
        return offset;
    }
}

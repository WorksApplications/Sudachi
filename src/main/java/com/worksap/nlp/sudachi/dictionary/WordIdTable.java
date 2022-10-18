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

package com.worksap.nlp.sudachi.dictionary;

import com.worksap.nlp.sudachi.WordId;

import java.nio.ByteBuffer;
import java.util.Iterator;

class WordIdTable {
    private final ByteBuffer bytes;
    private int dicIdMask = 0;

    WordIdTable(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    Integer[] get(int index) {
        ByteBuffer dup = bytes.duplicate();
        dup.position(index);
        BufReader reader = new BufReader(dup);
        int length = reader.readVarint32();
        Integer[] result = new Integer[length];
        int mask = dicIdMask;
        int sum = 0;
        for (int i = 0; i < length; i++) {
            int v = reader.readVarint32();
            result[i] = WordId.applyMask(v + sum, mask);
            sum += v;
        }
        return result;
    }

    /**
     * Reads the word IDs to the passed WordLookup object
     *
     * @param index
     *            index in the word array
     * @param lookup
     *            object to read word IDs into
     * @return number of read IDs
     */
    int readWordIds(int index, WordLookup lookup) {
        ByteBuffer dup = bytes.duplicate();
        dup.position(index);
        BufReader reader = new BufReader(dup);
        int length = reader.readVarint32();
        int[] result = lookup.outputBuffer(length);
        readDeltaCompressed(result, length, this.dicIdMask, reader);
        return length;
    }

    private static void readDeltaCompressed(int[] result, int count, int mask, BufReader reader) {
        int sum = 0;
        for (int i = 0; i < count; ++i) {
            int v = reader.readVarint32();
            result[i] = WordId.applyMask(v + sum, mask);
            sum += v;
        }
    }

    void setDictionaryId(int id) {
        dicIdMask = WordId.dicIdMask(id);
    }

    /**
     * Iterates over all valid word ids in the dictionary.
     * Iteration order is not the same as the original dictionary order, but dictionary ids, when sorted, form the correct order.
     * <br>
     * The returned Ints object will be the same for each invocation of {@code next()}.
     * @return iterator object
     */
    public Iterator<Ints> wordIds() {
        return new Iterator<Ints>() {
            private final BufReader buf = new BufReader(bytes.duplicate());
            private final Ints ints = new Ints(16);
            @Override
            public boolean hasNext() {
                return buf.remaining() > 0;
            }

            @Override
            public Ints next() {
                BufReader r = buf;
                int size = r.readVarint32();
                int[] data = ints.prepare(size);
                readDeltaCompressed(data, size, dicIdMask, r);
                return ints;
            }
        };
    }
}

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

class WordIdTable {
    private final ByteBuffer bytes;
    private final int size;
    private final int offset;
    private int dicIdMask = 0;

    WordIdTable(ByteBuffer bytes, int offset) {
        this.bytes = bytes;
        size = bytes.getInt(offset);
        this.offset = offset + 4;
    }

    int storageSize() {
        return 4 + size;
    }

    Integer[] get(int index) {
        int length = Byte.toUnsignedInt(bytes.get(offset + index++));
        Integer[] result = new Integer[length];
        for (int i = 0; i < length; i++) {
            result[i] = bytes.getInt(offset + index);
            index += 4;
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
        int offset = this.offset + index;
        ByteBuffer bytes = this.bytes;
        int length = Byte.toUnsignedInt(bytes.get(offset));
        offset += 1;
        int[] result = lookup.outputBuffer(length);
        int dicIdMask = this.dicIdMask;
        for (int i = 0; i < length; i++) {
            int wordId = bytes.getInt(offset);
            result[i] = WordId.applyMask(wordId, dicIdMask);
            offset += 4;
        }
        return length;
    }

    void setDictionaryId(int id) {
        dicIdMask = WordId.dicIdMask(id);
    }
}

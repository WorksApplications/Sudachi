package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;

class WordIdTable {

    private final ByteBuffer bytes;
    private final int size;
    private final int offset;

    WordIdTable(ByteBuffer bytes, int offset) {
        this.bytes = bytes;
        size = bytes.getInt(offset);
        this.offset = offset + 4;
    }

    int storageSize() { return 4 + size; }

    Integer[] get(int index) {
        int length = bytes.getShort(offset + index);
        index += 2;
        Integer[] result = new Integer[length];
        for (int i = 0; i < length; i++) {
            result[i] = bytes.getInt(offset + index);
            index += 4;
        }
        return result;
    }
}

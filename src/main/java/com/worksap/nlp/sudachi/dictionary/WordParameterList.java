package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;

class WordParameterList {

    private final int ELEMENT_SIZE = 2 * 3;

    private final ByteBuffer bytes;
    private final int size;
    private final int offset;

    WordParameterList(ByteBuffer bytes, int offset) {
        this.bytes = bytes;
        size = bytes.getInt(offset);
        this.offset = offset + 4;
    }

    int storageSize() { return 4 + ELEMENT_SIZE * size; }

    int size() { return size; }

    short getLeftId(int wordId) {
        return bytes.getShort(offset + ELEMENT_SIZE * wordId);
    }

    short getRightId(int wordId) {
        return bytes.getShort(offset + ELEMENT_SIZE * wordId + 2);
    }

    short getCost(int wordId) {
        return bytes.getShort(offset + ELEMENT_SIZE * wordId + 4);
    }

    void setCost(int wordId, short cost) {
        bytes.putShort(offset + ELEMENT_SIZE * wordId + 4, cost);
    }

    int endOffset() {
        return offset + 4 + ELEMENT_SIZE * size;
    }
}

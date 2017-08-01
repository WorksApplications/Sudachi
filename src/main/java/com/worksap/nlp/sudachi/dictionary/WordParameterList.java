package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class WordParameterList {

    private final int ELEMENT_SIZE = 2 * 3;

    private ByteBuffer bytes;
    private final int size;
    private int offset;
    private boolean isCopied;

    WordParameterList(ByteBuffer bytes, int offset) {
        this.bytes = bytes;
        size = bytes.getInt(offset);
        this.offset = offset + 4;
        isCopied = false;
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
        if (!isCopied) {
            copyBuffer();
        }
        bytes.putShort(offset + ELEMENT_SIZE * wordId + 4, cost);
    }

    int endOffset() {
        return offset + 4 + ELEMENT_SIZE * size;
    }

    synchronized void copyBuffer() {
        ByteBuffer newBuffer = ByteBuffer.allocate(ELEMENT_SIZE * size);
        newBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer srcBuffer = bytes.duplicate();
        srcBuffer.position(offset);
        srcBuffer.limit(offset + ELEMENT_SIZE * size);
        newBuffer.put(srcBuffer);
        bytes = newBuffer;
        offset = 0;
        isCopied = true;
    }
}

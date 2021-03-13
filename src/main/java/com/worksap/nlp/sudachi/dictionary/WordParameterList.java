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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class WordParameterList {

    private static final int ELEMENT_SIZE = 2 * 3;

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

    int storageSize() {
        return 4 + ELEMENT_SIZE * size;
    }

    int size() {
        return size;
    }

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
        Buffer buffer = srcBuffer; // a kludge for Java 9
        buffer.position(offset);
        buffer.limit(offset + ELEMENT_SIZE * size);
        newBuffer.put(srcBuffer);
        bytes = newBuffer;
        offset = 0;
        isCopied = true;
    }
}

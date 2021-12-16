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

package com.worksap.nlp.sudachi.dictionary.build;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Buffers dictionary data for writing into channels. Wrapper over a ByteBuffer.
 *
 * @see java.nio.ByteBuffer
 */
public class DicBuffer {
    public static final int MAX_STRING = Short.MAX_VALUE;
    private final ByteBuffer buffer;

    public DicBuffer(int length, int number) {
        this(length * number * 2 + number * 2);
    }

    public DicBuffer(int size) {
        buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public static boolean isValidLength(String text) {
        return text.length() <= MAX_STRING;
    }

    /**
     * Tries to put the string s into the buffer
     *
     * @param s
     *            the string to put into buffer
     * @return true if successful, false when the buffer does not have enough size.
     *         The buffer is not modified in that case.
     */
    public boolean put(String s) {
        int length = s.length();
        if (!putLength(length)) {
            return false;
        }
        s.chars().forEach(c -> buffer.putChar((char) c));
        return true;
    }

    /**
     * Tries to put the length of a string into the buffer
     *
     * @param length
     *            the length of the string to put into buffer
     * @return true if successful, false when the buffer does not have enough size.
     *         The buffer is not modified in that case.
     */
    public boolean putLength(int length) {
        if (length >= MAX_STRING) {
            throw new IllegalArgumentException("can't handle string with length >= " + MAX_STRING);
        }
        int addLen = (length > Byte.MAX_VALUE) ? 2 : 1;
        if (wontFit(length + addLen)) {
            return false;
        }
        if (length <= Byte.MAX_VALUE) {
            buffer.put((byte) length);
        } else {
            buffer.put((byte) ((length >> 8) | 0x80));
            buffer.put((byte) (length & 0xFF));
        }
        return true;
    }

    public <T> T consume(IOConsumer<T> consumer) throws IOException {
        buffer.flip();
        T result = consumer.accept(buffer);
        buffer.clear();
        return result;
    }

    public void putShort(short val) {
        buffer.putShort(val);
    }

    public void putInt(int val) {
        buffer.putInt(val);
    }

    public boolean wontFit(int space) {
        return buffer.remaining() < space;
    }

    public int position() {
        return buffer.position();
    }

    public void putEmptyIfEqual(String field, String surface) {
        if (field.equals(surface)) {
            put("");
        } else {
            put(field);
        }
    }

    public void putInts(int[] data) {
        buffer.put((byte) data.length);
        for (int v : data) {
            buffer.putInt(v);
        }
    }

    @FunctionalInterface
    public interface IOConsumer<T> {
        T accept(ByteBuffer arg) throws IOException;
    }
}

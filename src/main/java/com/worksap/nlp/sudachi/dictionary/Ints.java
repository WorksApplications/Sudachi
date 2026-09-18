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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Internal class for dealing with resizable integer arrays without boxing or
 * double indirection. This class is not a part of Sudachi API and can be
 * changed.
 */
public class Ints {
    private int[] data;
    private int length;

    private Ints(int[] data, int size) {
        this.data = data;
        this.length = size;
    }

    public Ints(int capacity) {
        data = new int[capacity];
        length = 0;
    }

    public int get(int index) {
        if (index >= length) {
            throw new IndexOutOfBoundsException(String.format("index %d is larger than Ints length %d", index, length));
        }
        return data[index];
    }

    public int set(int index, int value) {
        int old = data[index];
        data[index] = value;
        return old;
    }

    public int length() {
        return length;
    }

    public int capacity() {
        return data.length;
    }

    public void append(int value) {
        maybeResize(1);
        int idx = this.length;
        data[idx] = value;
        length = idx + 1;
    }

    public void clear() {
        length = 0;
    }

    private int[] maybeResize(int additional) {
        int newSize = length + additional;
        int[] d = data;
        if (newSize > capacity()) {
            d = Arrays.copyOf(data, Math.max(newSize, length * 2));
            data = d;
        }
        return d;
    }

    public static Ints wrap(int[] array, int size) {
        return new Ints(array, size);
    }

    public static Ints wrap(int[] array) {
        return new Ints(array, array.length);
    }

    public static final int[] EMPTY_ARRAY = new int[0];

    public static int[] readArray(ByteBuffer buffer, int len) {
        if (len == 0) {
            return EMPTY_ARRAY;
        }
        int position = buffer.position();
        buffer.position(position + len * 4);
        return readArray(buffer, position, len);
    }

    public static int[] readArray(ByteBuffer buffer, int offset, int len) {
        if (len == 0) {
            return EMPTY_ARRAY;
        }
        int[] result = new int[len];
        for (int i = 0; i < len; ++i) {
            result[i] = buffer.getInt(offset + i * 4);
        }
        return result;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "I[", "]");
        for (int i = 0; i < length; ++i) {
            joiner.add(String.valueOf(data[i]));
        }
        return joiner.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        Ints o = (Ints) other;
        if (this.length != o.length) {
            return false;
        }
        for (int i = 0; i < this.length; i++) {
            if (this.get(i) != o.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.length, Arrays.copyOfRange(this.data, 0, this.length));
    }

    /**
     * Make sure the internal buffer has enough capacity for the specified size.
     * This also increases length and they should be filled using {@code set} or
     * returned array.
     * 
     * @return internal int array
     */
    public int[] prepare(int size) {
        int[] d = maybeResize(size);
        this.length += size;
        return d;
    }

    public void appendAll(Ints other) {
        int addedLength = other.length;
        int[] write = maybeResize(addedLength);
        int start = length;
        if (addedLength >= 0) {
            System.arraycopy(other.data, 0, write, start, addedLength);
        }
        length += addedLength;
    }

    public void sort() {
        Arrays.sort(data, 0, length);
    }
}

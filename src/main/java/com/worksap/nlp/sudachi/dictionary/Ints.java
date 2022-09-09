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
import java.util.StringJoiner;

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
        assert index < length;
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

    public void append(int value) {
        maybeResize(1);
        int idx = this.length;
        data[idx] = value;
        length = idx + 1;
    }

    public void clear() {
        length = 0;
    }

    public void maybeResize(int additional) {
        int newSize = length + additional;
        if (newSize > data.length) {
            data = Arrays.copyOf(data, Math.max(newSize, length * 2));
        }
    }

    public static Ints wrap(int[] array, int size) {
        return new Ints(array, size);
    }

    public static Ints wrap(int[] array) {
        return new Ints(array, array.length);
    }

    private static final int[] EMPTY_ARRAY = new int[0];

    public static int[] readArray(ByteBuffer buffer, int len) {
        if (len == 0) {
            return EMPTY_ARRAY;
        }
        int[] result = new int[len];
        for (int i = 0; i < len; ++i) {
            result[i] = buffer.getInt();
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
}

package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;
import java.util.Arrays;

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

    public static int[] readArray(ByteBuffer buffer, int len) {
        int[] result = new int[len];
        for (int i = 0; i < len; ++i) {
            result[i] = buffer.getInt();
        }
        return result;
    }

}

package com.worksap.nlp.sudachi.dictionary;

import java.util.Arrays;

public class Ints {
    private int[] data;
    private int length;

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

}

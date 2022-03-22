package com.worksap.nlp.sudachi.dictionary.build;

import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.TreeMap;

public class WordLayout {
    private final ArrayList<ArrayList<UniqueStrings.Item>> items = new ArrayList<>();
    private final TreeMap<Integer, FreeSpace> free = new TreeMap<>();

    public void add(UniqueStrings.Item item) {

    }

    public void write(SeekableByteChannel channel) {

    }

    public static int requiredAlignment(UniqueStrings.Item item) {
        int length = item.getLength();
        return length;
    }

    public static boolean isPowerOfTwo(int val) {
        return (val & (val - 1)) == 0;
    }

    public static int nextPowerOfTwo(int val) {
        if (isPowerOfTwo(val)) {
            return val;
        }
        int nlz = Integer.numberOfLeadingZeros(val);
        return 1 << (32 - nlz);
    }

    public static class FreeSpace {

    }
}

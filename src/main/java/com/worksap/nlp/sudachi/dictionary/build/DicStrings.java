package com.worksap.nlp.sudachi.dictionary.build;

import java.util.HashMap;

public class DicStrings {
    private HashMap<String, Integer> counts;

    static int length(int pointer) {
        int b0 = pointer >>> 24;
        int additional = Math.max(0, b0 - (255 - 8));
        int b1 = (pointer & 0x00ffffff) >>> (24 - additional);
        return b0 + b1 * 8 - additional;
    }

    static int offset(int pointer) {
        int b0 = pointer >>> 24;
        int additional = Math.max(0, b0 - (255 - 8));
        int mask = 0x00ffffff >>> additional;
        int rawOffset = pointer & mask;
        return rawOffset << (additional + 1);
    }

}

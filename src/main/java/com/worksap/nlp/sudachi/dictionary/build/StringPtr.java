package com.worksap.nlp.sudachi.dictionary.build;

public class StringPtr {
    public static final int MAX_LENGTH_BITS = 11;
    public static final int BASE_OFFSET = 32 - 5;
    public static final int MAX_SIMPLE_LENGTH = 31 - MAX_LENGTH_BITS - 1;
    public static final int MAX_LENGTH = 4095 + MAX_SIMPLE_LENGTH;

    private final int length;
    private final int offset;

    private StringPtr(int length, int offset) {
        this.length = length;
        this.offset = offset;
    }

    public static StringPtr unsafe(int length, int offset) {
        return new StringPtr(length, offset);
    }

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return offset;
    }

    public static StringPtr decode(int pointer) {
        // first 5 bits are length and marker values for additional
        int base = pointer >>> BASE_OFFSET; // max value = 31
        int shift = Math.max(0, base - MAX_SIMPLE_LENGTH); // max value = 12
        // 16 - lower ignored bits, followed by max 11 additional bits of length
        int nonFixedLength = (pointer & 0x07ff_0000) >>> (16 + 12 - shift);
        // compute implicit bit, because first additional bit is not stored
        int implicitBit = 0x8000_0000 >>> 32 - shift;
        int finalLength = (base - shift) + (nonFixedLength | implicitBit);
        int fixedShift = shift - 1;
        int offset = (pointer & (0x07ff_ffff >>> fixedShift)) << fixedShift;
        return new StringPtr(finalLength, offset);
    }

    public int additionalBits() {
        if (length <= MAX_SIMPLE_LENGTH) {
            return 0;
        }
        int remaining = length - MAX_SIMPLE_LENGTH;
        int firstOne = 32 - Integer.numberOfLeadingZeros(remaining);
        return firstOne - 1;
    }

    public int encode() {
        int addBits = additionalBits();
        int baseLength = Math.min(length, MAX_SIMPLE_LENGTH);
        int remainingLength = length - baseLength;
        int basePart = (addBits + baseLength) << BASE_OFFSET;
        return basePart;
    }
}

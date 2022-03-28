package com.worksap.nlp.sudachi.dictionary.build;

import java.util.Objects;
import java.util.StringJoiner;

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
        // compute the non-stored first bit which is implicitly one
        int implicitBit = (1 << 12) >>> 13 - shift;
        int finalLength = (base - shift) + (nonFixedLength | implicitBit);
        int fixedShift = Math.max(shift - 1, 0);
        int offset = (pointer & (0x07ff_ffff >>> fixedShift)) << fixedShift;
        return unsafe(finalLength, offset);
    }

    public int additionalBits() {
        if (length <= MAX_SIMPLE_LENGTH) {
            return 0;
        }
        int remaining = length - MAX_SIMPLE_LENGTH;
        return 32 - Integer.numberOfLeadingZeros(remaining);
    }

    public int encode() {
        int addBits = additionalBits();
        int baseLength = Math.min(length, MAX_SIMPLE_LENGTH);
        int basePart = (addBits + baseLength) << BASE_OFFSET;

        int remainingLength = length - baseLength;
        int implicitBit = (1 << 12) >>> (13 - addBits);
        int nonFixedLength = remainingLength ^ implicitBit;
        int lengthPart = nonFixedLength << 16 + 12 - addBits;

        int offsetPart = offset >>> Math.max(addBits - 1, 0);
        assert (basePart & lengthPart) == 0;
        assert (basePart & offsetPart) == 0;
        assert (lengthPart & offsetPart) == 0;
        return basePart | lengthPart | offsetPart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringPtr stringPtr = (StringPtr) o;
        return length == stringPtr.length && offset == stringPtr.offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(length, offset);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StringPtr.class.getSimpleName() + "[", "]")
                .add("length=" + length)
                .add("offset=" + offset)
                .toString();
    }
}

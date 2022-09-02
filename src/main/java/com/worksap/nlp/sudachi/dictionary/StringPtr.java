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

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Pointer to a string in the string storage. Consists of offset and length
 * compressed in a single int value. <br>
 * Length can be stored from 5 (max value 19) bits to 19 (max value 4095 + 19).
 * Remaining bits are offset which are aligned with a difficult.
 */
public class StringPtr {
    public static final int MAX_LENGTH_BITS = 12;
    public static final int BASE_OFFSET = 32 - 5;
    public static final int MAX_SIMPLE_LENGTH = 31 - MAX_LENGTH_BITS;
    public static final int MAX_LENGTH = 4095 + MAX_SIMPLE_LENGTH;

    private final int length;
    private final int offset;

    private StringPtr(int length, int offset) {
        this.length = length;
        this.offset = offset;
    }

    /**
     * Create a new {@link StringPtr} without any runtime checks. Use
     * {@link #isValid(int, int)} to check validity.
     * 
     * @param length
     *            length of string
     * @param offset
     *            offset of string
     * @return StringPtr object, possibly invalid
     */
    public static StringPtr unsafe(int length, int offset) {
        return new StringPtr(length, offset);
    }

    public static StringPtr checked(int length, int offset) {
        if (length > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Maximum possible length is %d, was requested %d", MAX_LENGTH, length));
        }
        if (!isValid(offset, length)) {
            throw new IllegalArgumentException(String.format("StringPtr is invalid offset=%08x length=%d alignment=%d",
                    offset, length, requiredAlignment(length)));
        }
        return unsafe(length, offset);
    }

    public static StringPtr decode(int pointer) {
        // first 5 bits are length and marker values for additional length bits
        int base = pointer >>> BASE_OFFSET; // max value = 31
        int addBits = Math.max(0, base - MAX_SIMPLE_LENGTH); // max value = 12
        // 16 - lower ignored bits, followed by max 11 additional bits of length
        int nonFixedLength = (pointer & 0x07ff_0000) >>> (16 + MAX_LENGTH_BITS - addBits);
        // compute the non-stored first bit which is implicitly one
        int implicitBit = (1 << MAX_LENGTH_BITS) >>> 13 - addBits;
        int finalLength = (base - addBits) + (nonFixedLength | implicitBit);
        int fixedShift = Math.max(addBits - 1, 0);
        int offset = (pointer & (0x07ff_ffff >>> fixedShift)) << fixedShift;
        return unsafe(finalLength, offset);
    }

    public static int requiredAlignment(int length) {
        if (length <= MAX_SIMPLE_LENGTH) {
            return 0;
        }
        int remaining = length - MAX_SIMPLE_LENGTH;
        return 32 - Integer.numberOfLeadingZeros(remaining);
    }

    static boolean isValid(int offset, int length) {
        int alignment = requiredAlignment(length);
        if (alignment == 0) {
            return true;
        }
        int alignmentStep = 1 << alignment - 1;
        int alignmentMask = alignmentStep - 1;
        return (offset & alignmentMask) == 0;
    }

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return offset;
    }

    public int additionalBits() {
        return requiredAlignment(length);
    }

    public int encode() {
        int addBits = additionalBits();
        int baseLength = Math.min(length, MAX_SIMPLE_LENGTH);
        int basePart = (addBits + baseLength) << BASE_OFFSET;

        int remainingLength = length - baseLength;
        int implicitBit = (1 << MAX_LENGTH_BITS) >>> (13 - addBits);
        int nonFixedLength = remainingLength ^ implicitBit;
        int lengthPart = nonFixedLength << 16 + MAX_LENGTH_BITS - addBits;

        int offsetPart = offset >>> Math.max(addBits - 1, 0);
        assert (basePart & lengthPart) == 0;
        assert (basePart & offsetPart) == 0;
        assert (lengthPart & offsetPart) == 0;
        return basePart | lengthPart | offsetPart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StringPtr stringPtr = (StringPtr) o;
        return length == stringPtr.length && offset == stringPtr.offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(length, offset);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StringPtr.class.getSimpleName() + "[", "]").add("length=" + length)
                .add("offset=" + offset).add(String.format("encoded=%08x", encode())).toString();
    }

    public boolean isSubseqValid(int start, int end) {
        int realStart = offset + start;
        int length = end - start;
        return isValid(realStart, length);
    }

    public StringPtr subPtr(int start, int end) {
        return StringPtr.checked(end - start, offset + start);
    }
}

package com.worksap.nlp.sudachi.dictionary.build;

public class Align {

    private Align() {}

    public static boolean isPowerOf2(int value) {
        return (value & value - 1) == 0;
    }

    /**
     * Aligns value to the alignment
     * @param value value to be aligned
     * @param alignment required alignment as a power of two
     * @return aligned value, it should be greater or equal than the passed value
     */
    public static int align(int value, int alignment) {
        assert isPowerOf2(alignment);
        // Compute alignment mask, it is the inverse of the mask for the bits that must be 0 for alignment to be correct
        // Checking mask is computed as alignment - 1. E.g. 7 for alignment of 8, or 15 for alignment of 16.
        // The second one is its inverse.
        int mask = -alignment; // same as ~(alignment - 1)
        int masked = value & mask;
        if (masked == value) {
            return value;
        } else {
            return masked + alignment;
        }
    }
}

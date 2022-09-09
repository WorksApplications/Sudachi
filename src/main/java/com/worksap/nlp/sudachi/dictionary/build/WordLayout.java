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

package com.worksap.nlp.sudachi.dictionary.build;

import com.worksap.nlp.sudachi.dictionary.StringPtr;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringJoiner;

/**
 * <p>
 * Lays out dictionary words so that they will form correct {@link StringPtr}
 * instances. That means taking into account the required alignment for strings
 * with larger sizes. Aligning strings produces wasted space in form of padding,
 * which is kept track with free list approach.
 *
 * <p>
 * The main API is {@link #add(String)} method which should be called for all
 * strings. The method should be called for strings sorted in descending order
 * by length, otherwise padding between aligned strings would not be utilized
 * correctly. The returned {@link StringPtr}s will be correct in any case.
 *
 * <p>
 * The current implementation is relatively fast, but can be made even faster by
 * using sorted multiset collection. JVM standard library does not have one, so
 * the current implementation sorts free list while keeping track if the sort is
 * needed and guarding against relatively expensive checking free lists with
 * additional conditions.
 */
public class WordLayout {
    private final UnicodeBufferResizeable buffer = new UnicodeBufferResizeable();
    private final ArrayList<FreeSpace> free = new ArrayList<>();
    private boolean freeDirty = false;
    private int pointer;
    private int maxLength = -1;

    public StringPtr add(String string) {
        return add(string, 0, string.length());
    }

    public StringPtr add(String string, int start, int end) {
        int length = string.length();
        int alignment = StringPtr.requiredAlignment(length);
        int offset = allocate(length, alignment);
        buffer.put(offset, string, start, end);
        return StringPtr.checked(length, offset);
    }

    /**
     * Allocates a segment of utf-16 code units in a specified block, taking in
     * account requested alignment.
     *
     * Alignment can skip some space in the beginning of the block because of
     * padding. That space will be placed into free lists. Nothing will be placed in
     * the free lists if the allocation is not possible.
     *
     * @param length
     *            requested length of segment
     * @param alignment
     *            requested alignment of segment
     * @param start
     *            start of the block of memory to use
     * @param end
     *            end of the block of memory to use
     * @return offset of the aligned data or -1 if allocation is impossible
     */
    private int allocateInBlock(int length, int alignment, int start, int end) {
        int requiredAlignment = Math.max(0, alignment - 1);
        int alignmentStep = 1 << requiredAlignment;
        int alignmentMask = alignmentStep - 1;
        int alignedStart = start & ~alignmentMask;
        boolean isAligned = alignedStart == start;
        if (!isAligned) {
            alignedStart += alignmentStep;
        }
        int available = end - alignedStart;
        if (available < length) {
            return -1;
        }
        if (!isAligned) {
            int padding = alignedStart - start;
            assert padding > 0;
            free.add(new FreeSpace(start, padding));
            freeDirty = true;
            int estimated = availableMaxLength(padding);
            maxLength = Math.max(maxLength, estimated);
        }
        return alignedStart;
    }

    /**
     * Allocates a slot of {@code length} bytes, alignment with {@code alignment}.
     * It first considers free slots created by previous allocations, if none is
     * valid.
     * <p>
     * Current implementation is prone to creating "holes" of 1-length, which are
     * almost impossible to fill from the usual dictionaries. Most emoji take 2 code
     * units and words which are not substrings of another word are usually longer.
     * The current implementation wastes ~32k holes in ~42M dictionary, which is
     * ~0.1% of total space.
     *
     * @param length
     *            number of byte
     * @param alignment
     *            requested alignment
     * @return offset in utf-16 code units to the location of the requested block
     */
    private int allocate(int length, int alignment) {
        if (length <= maxLength) {
            if (freeDirty) {
                freeDirty = false;
                Collections.sort(free);
            }
            int startIdx = Collections.binarySearch(free, new FreeSpace(0, length));
            if (startIdx < 0) {
                startIdx = -startIdx - 1;
            }

            int numFree = free.size();
            for (int i = startIdx; i < numFree; ++i) {
                FreeSpace fs = free.get(i);
                if (fs.length < length) {
                    continue;
                }
                int end = fs.start + fs.length;
                int start = allocateInBlock(length, alignment, fs.start, end);
                if (start != -1) {
                    int remaining = end - start - length;
                    if (remaining > 0) {
                        fs.start = start + length;
                        fs.length = remaining;
                        freeDirty = true;
                        // we need to recompute maxLength only if modifying the last (maximum) element
                        // in free lists
                        if (i == numFree - 1) {
                            maxLength = computeNewMaxLength(i);
                        }
                    } else {
                        free.remove(i);
                        maxLength = computeNewMaxLength(numFree - 2);
                    }
                    return start;
                }
            }
            maxLength = Math.max(0, maxLength - 1);
        }

        int alignedStart = allocateInBlock(length, alignment, pointer, Integer.MAX_VALUE);
        assert alignedStart != -1;
        pointer = alignedStart + length;
        return alignedStart;
    }

    /**
     * Returns available max length for a hole
     * 
     * @param length
     *            hole length
     * @return length of an element which can be allocated using any alignment
     */
    private static int availableMaxLength(int length) {
        int simple = StringPtr.MAX_SIMPLE_LENGTH + 1;
        if (length <= simple) {
            return length;
        }
        int clz = Integer.numberOfLeadingZeros(length - simple);
        int candidateLength = 1 << (31 - clz);
        return Math.max(simple, candidateLength + simple);
    }

    /**
     * Compute new maximum length which can be handled by free lists. Should be
     * called if the last element of free lists was updated.
     * 
     * @param index
     *            index in free lists of the element which needs to be considered
     * @return new maximum length that can be handled by free lists
     */
    private int computeNewMaxLength(int index) {
        // assumption: free is sorted ascending my length (except i-th item)
        int freeLength = free.size();
        // if new size of free array is 0, then it's -1
        if (freeLength == 0) {
            return -1;
        }

        int newLength = availableMaxLength(free.get(index).length);
        // more than 1 element: maximum of newly computed length and the previous value
        if (freeLength > 1) {
            FreeSpace prevSpace = free.get(index - 1);
            int fixedLength = availableMaxLength(prevSpace.length);
            return Math.max(fixedLength, newLength);
        } else {
            // otherwise, simply new value
            return newLength;
        }
    }

    public void write(WritableByteChannel channel) throws IOException {
        buffer.write(channel, 0, pointer * 2);
    }

    public static class FreeSpace implements Comparable<FreeSpace> {
        int start;
        int length;

        public FreeSpace(int start, int length) {
            this.start = start;
            this.length = length;
        }

        @Override
        public int compareTo(FreeSpace o) {
            int comparison = Integer.compare(length, o.length);
            if (comparison != 0)
                return comparison;
            return Integer.compare(start, o.start);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", "FreeSpace[", "]").add("start=" + start).add("length=" + length).toString();
        }
    }

    int wastedBytes() {
        return free.stream().mapToInt(f -> f.length).sum();
    }

    int numSlots() {
        return free.size();
    }
}

package com.worksap.nlp.sudachi.dictionary.build;

import com.worksap.nlp.sudachi.dictionary.StringPtr;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;

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

    private int allocateAligned(int length, int alignment, int start, int end) {
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
            maxLength = Math.max(maxLength, padding - (start & alignmentMask));
        }
        return alignedStart;
    }

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
                int start = allocateAligned(length, alignment, fs.start, end);
                if (start != -1) {
                    int remaining = end - start - length;
                    if (remaining > 0) {
                        fs.start = start + length;
                        fs.length = remaining;
                        freeDirty = true;
                        maxLength = computeNewMaxLength(i);
                    } else {
                        free.remove(i);
                        maxLength = computeNewMaxLength(numFree - 2);
                    }
                    return start;
                }
            }
            maxLength = Math.max(0, maxLength - 1);
        }


        int alignedStart = allocateAligned(length, alignment, pointer, Integer.MAX_VALUE);
        assert alignedStart != -1;
        pointer = alignedStart + length;
        return alignedStart;
    }

    private int estimatedLength(int length) {
        if (length <= 20) {
            return length;
        }
        int clz = Integer.numberOfLeadingZeros(length);
        return 1 << (31 - clz);
    }

    private int computeNewMaxLength(int i) {
        // assumption: free is sorted ascending my length (except i-th item)
        int freeLength = free.size();
        // if new size of free array is 0, then it's -1
        if (freeLength == 0) {
            return -1;
        }

        int newLength = estimatedLength(free.get(i).length);
        // more than 1 element: maximum of newly computed length and the previous value
        if (freeLength > 1) {
            FreeSpace prevSpace = free.get(i - 1);
            int fixedLength = estimatedLength(prevSpace.length);
            return Math.max(fixedLength, newLength);
        } else {
            // otherwise, simply new value
            return newLength;
        }
    }

    public void write(WritableByteChannel channel) throws IOException {
        buffer.write(channel, pointer * 2);
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
            int cval = Integer.compare(length, o.length);
            if (cval != 0) return cval;
            return Integer.compare(start, o.start);
        }
    }

    int wastedBytes() {
        return free.stream().mapToInt(f -> f.length).sum();
    }

    int numSlots() {
        return free.size();
    }
}

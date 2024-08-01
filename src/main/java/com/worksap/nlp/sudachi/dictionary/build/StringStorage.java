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
import java.util.*;

/**
 * Dictionary parts: storage of strings used in the lexicons.
 */
public class StringStorage implements StringIndex {
    // strings required by lexicons
    private final HashMap<String, Item> strings = new HashMap<>();
    private final HashMap<String, Item> candidates = new HashMap<>();
    // compacted strings layout
    private final WordLayout layout = new WordLayout();

    /**
     * Add string to the storage.
     * 
     * @param data
     */
    void add(String data) {
        strings.put(data, null);
    }

    /**
     * Compile added strings. should only call once after all strings are added and
     * before use.
     * 
     * @param progress
     */
    void compile(Progress progress) {
        candidates.clear();
        candidates.put("", new Item("", 0, 0));
        List<String> collect = new ArrayList<>(strings.keySet());
        // sort strings so that processing works correctly
        collect.sort(Comparator.comparingInt(String::length).reversed().thenComparing(String::compareTo));
        int size = collect.size();
        for (int i = 0; i < size; ++i) {
            String str = collect.get(i);
            strings.put(str, process(str));
            if (progress != null) {
                progress.progress(i, size);
            }
        }
        candidates.clear();
    }

    // layout string and returns Item
    private Item process(String str) {
        Item present = candidates.get(str);
        if (present != null) { // this str is a substring of previous one.
            return present;
        }

        int length = str.length();
        int[] offsets = new int[length + 1];
        int numOffsets = computeOffsets(str, offsets);

        StringPtr ptr = layout.add(str);
        Item full = new Item(str, 0, length);
        full.root = full;
        full.ptr = ptr;
        candidates.put(str, full);

        // handle substrings
        for (int i = 0; i < numOffsets; ++i) {
            int start = offsets[i];
            for (int j = i + 1; j <= numOffsets; ++j) {
                int end = offsets[j];
                String sub = str.substring(start, end);
                // Create a possible substring only if
                // 1. It does not exist yet
                // 2. Can form a valid pointer to it (string pointer requires aligned offset
                // based on str length)
                if (!candidates.containsKey(sub) && ptr.isSubseqValid(start, end)) {
                    Item item = new Item(str, start, end);
                    item.root = full;
                    candidates.put(sub, item);
                }
            }
        }

        return full;
    }

    // compute char offset for each codepoint in the str.
    // @return number of code points.
    private int computeOffsets(String str, int[] offsets) {
        int count = 0;
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (Character.isLowSurrogate(ch)) {
                if (i + 1 < len && Character.isHighSurrogate(str.charAt(i + 1))) {
                    i += 1;
                }
            }
            offsets[count] = i;
            count += 1;
        }
        offsets[count] = len;
        return count;
    }

    /** @return StringPtr for the string */
    public StringPtr resolve(String data) {
        Item item = strings.get(data);
        return item.root.ptr.subPtr(item.start, item.end);
    }

    /** @return string hash map */
    public HashMap<String, Item> getStrings() {
        return strings;
    }

    /**
     * Write compacted string storage to the provided channel
     * 
     * @param channel
     * @throws IOException
     */
    public void writeCompact(WritableByteChannel channel) throws IOException {
        layout.write(channel);
    }

    /**
     * Data class of string and its pointer.
     */
    public static class Item {
        // super-string that contains this string
        private final String data;
        // substring range of this string in data
        private final int start;
        private final int end;
        // root to get the pointer from
        private Item root;
        // pointer to data in the storage
        private StringPtr ptr;

        public Item(String data, int start, int end) {
            this.data = data;
            this.start = start;
            this.end = end;
        }

        public String getData() {
            return data;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getLength() {
            return end - start;
        }
    }

}

/*
 * Copyright (c) 2021 Works Applications Co., Ltd.
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

import com.worksap.nlp.dartsclone.DoubleArray;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Dictionary Parts: Trie index and entry offsets
 */
public class Index implements WriteDictionary {
    private final SortedMap<byte[], List<Integer>> elements = new TreeMap<>((byte[] l, byte[] r) -> {
        int llen = l.length;
        int rlen = r.length;
        for (int i = 0; i < Math.min(llen, rlen); i++) {
            if (l[i] != r[i]) {
                return (l[i] & 0xff) - (r[i] & 0xff);
            }
        }
        return l.length - r.length;
    });

    private int count = 0;

    public int add(String key, int wordId) {
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        List<Integer> entries = elements.computeIfAbsent(bytes, k -> new ArrayList<>());
        if (entries.size() >= 255) {
            throw new IllegalArgumentException(String.format("key %s has >= 255 entries in the dictionary", key));
        }
        entries.add(wordId);
        count += 1;
        return bytes.length;
    }

    public void writeTo(ModelOutput output) throws IOException {
        DoubleArray trie = new DoubleArray();

        int size = this.elements.size();

        byte[][] keys = new byte[size][];
        int[] values = new int[size];
        ByteBuffer wordIdTable = ByteBuffer.allocate(count * (4 + 2));
        wordIdTable.order(ByteOrder.LITTLE_ENDIAN);

        output.withSizedPart("WordId table", () -> {
            int i = 0;
            int numEntries = this.elements.entrySet().size();
            for (Map.Entry<byte[], List<Integer>> entry : this.elements.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = wordIdTable.position();
                i++;
                List<Integer> wordIds = entry.getValue();
                wordIdTable.put((byte) wordIds.size());
                for (int wid : wordIds) {
                    wordIdTable.putInt(wid);
                }
                output.progress(i, numEntries);
            }
            return wordIdTable.position() + 4;
        });

        DicBuffer buffer = new DicBuffer(4);
        output.withPart("double array Trie", () -> {
            trie.build(keys, values, output::progress);
            buffer.putInt(trie.size());
            buffer.consume(output::write);
            output.write(trie.byteArray());
        });

        buffer.putInt(wordIdTable.position());
        buffer.consume(output::write);

        wordIdTable.flip();
        output.write(wordIdTable);
    }
}

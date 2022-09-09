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
import com.worksap.nlp.sudachi.dictionary.Blocks;
import com.worksap.nlp.sudachi.dictionary.Ints;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Dictionary Parts: Trie index and entry offsets
 */
public class Index implements WriteDictionary {
    private final SortedMap<byte[], Ints> elements = new TreeMap<>((byte[] l, byte[] r) -> {
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
        Ints entries = elements.computeIfAbsent(bytes, k -> new Ints(4));
        entries.append(wordId);
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
            for (Map.Entry<byte[], Ints> entry : this.elements.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = wordIdTable.position();
                i++;
                Ints wordIds = entry.getValue();
                int length = wordIds.length();
                wordIdTable.put((byte) length);
                for (int word = 0; word < length; ++word) {
                    int wid = wordIds.get(word);
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

    public void compile(BlockLayout layout) throws IOException {
        TrieData data = layout.block(Blocks.WORD_ID_TABLE, this::writeWordTable);
        layout.block(Blocks.TRIE_INDEX, data::writeTrie);
    }

    private TrieData writeWordTable(BlockOutput out) throws IOException {
        int size = this.elements.size();
        byte[][] keys = new byte[size][];
        int[] values = new int[size];
        ChanneledBuffer buffer = new ChanneledBuffer(out.getChannel());

        out.measured("Word Id table", (p) -> {
            int i = 0;
            for (Map.Entry<byte[], Ints> entry : this.elements.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = buffer.offset();
                i++;
                Ints wordIds = entry.getValue();
                int length = wordIds.length();
                BufWriter buf = buffer.writer((length + 1) * 5);

                buf.putVarint32(length);
                int prevWid = 0;
                for (int word = 0; word < length; ++word) {
                    int wid = wordIds.get(word);
                    buf.putVarint32(wid - prevWid);
                    prevWid = wid;
                }
                p.progress(i, size);
            }
            return null;
        });

        buffer.flush();

        return new TrieData(keys, values);
    }

    private static class TrieData {
        private final byte[][] keys;
        private final int[] values;

        public TrieData(byte[][] keys, int[] values) {
            this.keys = keys;
            this.values = values;
        }

        public Void writeTrie(BlockOutput block) throws IOException {
            return block.measured("Trie Index", (p) -> {
                DoubleArray trie = new DoubleArray();
                trie.build(keys, values, p::progress);
                ByteBuffer buf = trie.byteArray().duplicate();
                block.getChannel().write(buf);
                return null;
            });
        }
    }
}

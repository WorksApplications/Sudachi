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
import com.worksap.nlp.sudachi.dictionary.Block;
import com.worksap.nlp.sudachi.dictionary.Ints;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Dictionary Parts: Trie index and corresponding word id table.
 * 
 * TRIE maps headwords to offset for WordIdTable. WordIdTable contains the list
 * of word-ids of words which have the target headword. WordId here means offset
 * in WordEntryTable (with last n bits dropped).
 * 
 * WordIdTable also contins word-ids that are not indexed in TRIE, so that we
 * can iterate over all word entries.
 */
public class Index {
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

    /**
     * Add a (headword, wordid) pair to the index
     * 
     * @param key
     * @param wordId
     * @return
     */
    public int add(String key, int wordId) {
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        Ints entries = elements.computeIfAbsent(bytes, k -> new Ints(4));
        entries.append(wordId);
        return bytes.length;
    }

    /**
     * Write word id table and trie to the provided block layout.
     * 
     * @param layout
     * @param notIndexed
     * @throws IOException
     */
    public void compile(BlockLayout layout, List<RawWordEntry> notIndexed) throws IOException {
        TrieData data = layout.block(Block.WORD_POINTERS, o -> writeWordTable(o, notIndexed));
        layout.block(Block.TRIE_INDEX, data::writeTrie);
    }

    private TrieData writeWordTable(BlockOutput out, List<? extends EntryLookup.Entry> notIndexed) throws IOException {
        int size = this.elements.size();
        byte[][] keys = new byte[size][];
        int[] values = new int[size];
        BufferedChannel buffer = new BufferedChannel(out.getChannel(),
                Math.max((notIndexed.size() + 16) * 5, 64 * 1024));

        int nis = notIndexed.size();
        int fullsize = size + nis;

        out.measured("Word Id table", p -> {
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
                p.progress(i, fullsize);
            }

            // write non-indexed entries
            BufWriter buf = buffer.writer((nis + 1) * 5);
            buf.putVarint32(nis);
            int prevId = 0;
            for (EntryLookup.Entry e : notIndexed) {
                int wid = e.pointer();
                buf.putVarint32(wid - prevId);
                prevId = wid;
                p.progress(++i, fullsize);
            }
            buffer.flush();
            return null;
        });

        return new TrieData(keys, values);
    }

    /**
     * Subclass for trie construction.
     */
    private static class TrieData {
        // headwords added to this index
        private final byte[][] keys;
        // offsets to WordIdTable
        private final int[] values;

        public TrieData(byte[][] keys, int[] values) {
            this.keys = keys;
            this.values = values;
        }

        /**
         * Write trie to the provided block output.
         * 
         * @param block
         * @return
         * @throws IOException
         */
        public Void writeTrie(BlockOutput block) throws IOException {
            return block.measured("Trie Index", p -> {
                DoubleArray trie = new DoubleArray();
                trie.build(keys, values, p::progress);
                ByteBuffer buf = trie.byteArray().duplicate();
                block.getChannel().write(buf);
                return null;
            });
        }
    }
}

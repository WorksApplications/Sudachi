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
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * Dictionary Parts: Trie index and entry offsets
 */
public class Index {
    private final static Logger logger = Logger.getLogger(Index.class.getName());

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

    public void write(SeekableByteChannel output) throws IOException {
        DoubleArray trie = new DoubleArray();

        int size = this.elements.size();

        byte[][] keys = new byte[size][];
        int[] values = new int[size];
        ByteBuffer wordIdTable = ByteBuffer.allocate(count * (4 + 2));
        wordIdTable.order(ByteOrder.LITTLE_ENDIAN);

        int i = 0;
        for (Map.Entry<byte[], List<Integer>> entry : this.elements.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = wordIdTable.position();
            i++;
            List<Integer> wordIds = entry.getValue();
            wordIdTable.put((byte) wordIds.size());
            for (int wid : wordIds) {
                wordIdTable.putInt(wid);
            }
        }

        logger.info("building the trie");
        trie.build(keys, values, (n, s) -> {
            if (n % ((s / 10) + 1) == 0) {
                logger.info(".");
            }
        });
        logger.info("done\n");

        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        logger.info("writing the trie...");
        buffer.clear();
        buffer.putInt(trie.size());
        buffer.flip();
        output.write(buffer);
        buffer.clear();

        output.write(trie.byteArray());
        logger.info(() -> String.format("trie size.. %d", trie.size() * 4L + 4L));

        logger.info("writing the word-ID table...");
        buffer.putInt(wordIdTable.position());
        buffer.flip();
        output.write(buffer);
        buffer.clear();

        wordIdTable.flip();
        output.write(wordIdTable);
        logger.info(() -> String.format("wordid table.. %d", wordIdTable.position() + 4L));
    }
}

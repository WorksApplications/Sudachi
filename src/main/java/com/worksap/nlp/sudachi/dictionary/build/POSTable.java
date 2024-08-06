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

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.POS;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Dictionary parts: List of part-of-speeches.
 */
public class POSTable {
    static final int MAX_POS_NUMBER = Short.MAX_VALUE;

    private final List<POS> table = new ArrayList<>();
    private final HashMap<POS, Short> lookup = new HashMap<>();
    public boolean allowNewPos = true;
    // number of pos loaded from the system dictionary.
    private int builtin = 0;

    /**
     * Returns the id of given POS, updating table if it's not in the list.
     * 
     * @param s
     * @return
     */
    short getId(POS s) {
        return lookup.computeIfAbsent(s, p -> {
            if (!allowNewPos) {
                throw new IllegalArgumentException(
                        String.format("POS %s is not present in the table and new POS is not allowed", s));
            }

            int next = table.size();
            if (next >= MAX_POS_NUMBER) {
                throw new IllegalArgumentException("maximum POS number exceeded by " + s);
            }
            table.add(s);
            return (short) next;
        });
    }

    /** @return full POS list that contains builtin and newly added POSs */
    List<POS> getList() {
        return table;
    }

    /** @return number of all POSs in the table. */
    int size() {
        return table.size();
    }

    /**
     * @return number of non-builtin POSs.
     */
    public int ownedLength() {
        return table.size() - builtin;
    }

    /**
     * Load pos table from the grammar (of the system dictionary). They are
     * considered as built-in pos.
     * 
     * @param grammar
     * @return number read.
     */
    public int preloadFrom(Grammar grammar) {
        int partOfSpeechSize = grammar.getPartOfSpeechSize();
        for (short i = 0; i < partOfSpeechSize; ++i) {
            POS pos = grammar.getPartOfSpeechString(i);
            table.add(pos);
            lookup.put(pos, i);
        }
        builtin += partOfSpeechSize;
        return partOfSpeechSize;
    }

    /**
     * Load pos table from the text. Assume 6-column csv without header.
     * 
     * @param data
     * @return number read.
     */
    public int readEntries(InputStream data) throws IOException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(data, StandardCharsets.UTF_8));

        int baseSize = table.size();
        int numLines = 0;
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            String[] cols = line.split(",");
            if (cols.length != 6) {
                throw new InputFileException(numLines,
                        new IllegalArgumentException(String.format("each POS must have 6 columns: %s", line)));
            }

            int posid = getId(new POS(cols));
            if (posid != baseSize + numLines) {
                throw new InputFileException(numLines,
                        new IllegalArgumentException(String.format("POS already exists (%s): %s", posid, line)));
            }
            numLines += 1;
        }
        return numLines;
    }

    /**
     * Write pos table to the provided block output.
     * 
     * @param out
     * @return
     * @throws IOException
     */
    public Void compile(BlockOutput out) throws IOException {
        return out.measured("POS Table", p -> {
            BufferedChannel cbuf = new BufferedChannel(out.getChannel());
            cbuf.byteBuffer(2).putShort((short) ownedLength());
            for (int i = 0; i < ownedLength(); ++i) {
                BufWriter writer = cbuf.writer(POS.MAX_BINARY_LENGTH);
                POS pos = table.get(builtin + i);
                for (String s : pos) {
                    // strings are always shorter than POS.MAX
                    writer.putShortString(s);
                }
                p.progress(i, ownedLength());
            }
            cbuf.flush();
            return null;
        });

    }
}

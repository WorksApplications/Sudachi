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
import java.util.Arrays;
import java.util.Collections;
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
    private short builtin = 0;

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
     * Add pos at the index `id` of the table. This may creates null entry in the
     * table.
     * 
     * @param pos
     *            POS to add
     * @param id
     *            pos-id (index) to add POS at
     * @return pos-id
     */
    private short addPosAt(POS pos, short id) {
        if (!allowNewPos) {
            throw new IllegalArgumentException(String.format("new POS is not allowed", pos));
        }
        if (id >= MAX_POS_NUMBER) {
            throw new IllegalArgumentException("id " + id + " exceeds the maximum POS number");
        }

        if (table.size() <= id) {
            table.addAll(Collections.nCopies(id - table.size() + 1, null));
        }
        POS current = table.get(id);
        if (current != null) {
            throw new IllegalArgumentException(String.format("POS already exists (%s): %s", id, current));
        }
        table.set(id, pos);
        lookup.put(pos, id);
        return id;
    }

    /**
     * Assure that the table has no null entry.
     * 
     * Should be called after addPosAt is used.
     */
    private void assureNoEmptyEntry() {
        List<Integer> nullIndices = new ArrayList<>();
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i) == null) {
                nullIndices.add(i);
            }
        }
        if (!nullIndices.isEmpty()) {
            throw new IllegalStateException(String.format("Missing POS-Ids found: %s", nullIndices));
        }
    }

    /**
     * Load pos table from the grammar (of the system dictionary). They are
     * considered as built-in pos.
     * 
     * @param grammar
     * @return number read.
     */
    public int preloadFrom(Grammar grammar) {
        if (!table.isEmpty()) {
            throw new IllegalStateException("POSTable.preloadFrom must be called before any other POS is added.");
        }

        short partOfSpeechSize = (short) grammar.getPartOfSpeechSize();
        for (short i = 0; i < partOfSpeechSize; ++i) {
            POS pos = grammar.getPartOfSpeechString(i);
            table.add(pos);
            lookup.put(pos, i);
        }

        builtin = partOfSpeechSize;
        return partOfSpeechSize;
    }

    /**
     * Load pos table from the text.
     * 
     * Assume csv format, with POS_Id (not required) and 6 POS parts columns.
     * 
     * @param data
     * @return number read.
     */
    public int readEntries(InputStream data) throws IOException {
        POSCSVReader reader = new POSCSVReader(data);

        int baseSize = table.size();
        int numAdded = 0;
        POSWithId posWithId;
        while ((posWithId = reader.nextPos()) != null) {
            if (!reader.hasIdColumn) {
                int posId = getId(posWithId.pos);
                if (posId != baseSize + numAdded) {
                    throw new InputFileException(numAdded, new IllegalArgumentException(
                            String.format("POS already exists (%s): %s", posId, table.get(posId).toString())));
                }
            } else {
                addPosAt(posWithId.pos, posWithId.id);
            }
            numAdded += 1;
        }
        assureNoEmptyEntry();
        return numAdded;
    }

    /**
     * Data class for pos read from csv.
     */
    static class POSWithId {
        public POS pos;
        public short id = -1;
        public int sourceLine;

        POSWithId(POS pos, short id) {
            this.pos = pos;
            this.id = id;
        }

        POSWithId(POS pos) {
            this.pos = pos;
        }
    }

    /**
     * POS CSV reader.
     * 
     * Each row must have 6 pos parts. Pos id can be missing (filled with -1).
     */
    public static class POSCSVReader {
        private CSVParser parser;
        private int[] columnMapping;
        private List<String> cachedRow;
        public boolean hasIdColumn = true;

        public Column[] PART_COLUMNS = { Column.POS1, Column.POS2, Column.POS3, Column.POS4, Column.POS5, Column.POS6 };

        public enum Column {
            POS_ID(false), POS1(true), POS2(true), POS3(true), POS4(true), POS5(true), POS6(true);

            private final boolean required;

            Column(boolean required) {
                this.required = required;
            }

            /**
             * Parse string as Column, ignoring "_" and cases.
             */
            public static Column fromString(String str) {
                String processed = str.replace("_", "");
                for (Column col : Column.values()) {
                    if (col.name().replace("_", "").equalsIgnoreCase(processed)) {
                        return col;
                    }
                }
                return null;
            }
        }

        POSCSVReader(InputStream data) throws IOException {
            this.parser = new CSVParser(new InputStreamReader(data, StandardCharsets.UTF_8));
            parser.setName("POS csv");
            resolveColumnLayout();
        }

        /**
         * Resolve header line.
         * 
         * POS id column can be missing even if there are no header.
         */
        private void resolveColumnLayout() throws IOException {
            List<String> row = parser.getNextRow();
            Column parsed = Column.fromString(row.get(0));
            if (parsed == null) { // no header line
                this.cachedRow = row;
                this.columnMapping = null;
                if (row.size() == 6) {
                    this.columnMapping = new int[] { -1, 0, 1, 2, 3, 4, 5 };
                    this.hasIdColumn = false;
                }
                return;
            }

            List<Column> remaining = new ArrayList<>(Arrays.asList(Column.values()));
            columnMapping = new int[Column.values().length];
            Arrays.fill(columnMapping, -1);
            for (int colIdx = 0; colIdx < row.size(); colIdx++) {
                String elem = row.get(colIdx);
                parsed = Column.fromString(elem);
                if (!remaining.contains(parsed)) {
                    throw new InputFileException(parser.getName(), 0, elem,
                            new IllegalArgumentException("Invalid column name"));
                }
                columnMapping[parsed.ordinal()] = colIdx;
                remaining.remove(remaining.indexOf(parsed));
            }
            for (Column col : remaining) {
                if (col.required) {
                    throw new InputFileException(parser.getName(), 0, col.name(),
                            new IllegalArgumentException("Required column is missing"));
                }
            }
            if (remaining.contains(Column.POS_ID)) {
                hasIdColumn = false;
            }
        }

        private int getIdx(List<String> data, Column column) {
            int idx = column.ordinal();
            if (columnMapping != null) {
                idx = columnMapping[idx];
            }
            if ((idx < 0 && column.required) || idx >= data.size()) {
                throw new InputFileException(parser.getName(), parser.getRowCount(), column.name(),
                        new IllegalArgumentException(String.format("column [%s] was not present", column.name())));
            }
            return idx;
        }

        private POS getPos(List<String> data) {
            String[] parts = new String[6];
            for (int idx = 0; idx < PART_COLUMNS.length; idx++) {
                parts[idx] = data.get(getIdx(data, PART_COLUMNS[idx]));
            }
            return new POS(parts);
        }

        private POSWithId convertRow(List<String> data) {
            POS pos = getPos(data);
            int idIdx = getIdx(data, Column.POS_ID);
            if (idIdx < 0) {
                return new POSWithId(pos);
            }
            return new POSWithId(pos, Short.parseShort(data.get(idIdx)));
        }

        /**
         * Parse next line as a set of POS and id.
         * 
         * returned pos-id is -1 when pos-id column is missing.
         */
        POSWithId nextPos() throws IOException {
            List<String> row = cachedRow;
            if (row == null) {
                row = parser.getNextRow();
            } else {
                cachedRow = null;
            }
            if (row == null) {
                return null;
            }
            POSWithId pos = convertRow(row);
            pos.sourceLine = parser.getRowCount();
            return pos;
        }
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

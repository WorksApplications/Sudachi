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

import com.worksap.nlp.sudachi.dictionary.Ints;
import com.worksap.nlp.sudachi.dictionary.POS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Reader for the lexicon csv file.
 */
public class RawLexiconReader {
    /**
     * Enum order is in legacy csv order. If a header is present, fields will be
     * reordered with respect to the header.
     */
    public enum Column {
        SURFACE(true), LEFT_ID(true), RIGHT_ID(true), COST(true), WRITING(false), POS1(false), POS2(false), POS3(
                false), POS4(false), POS5(false), POS6(false), READING_FORM(true), NORMALIZED_FORM(
                        true), DICTIONARY_FORM(true), MODE(false), SPLIT_A(true), SPLIT_B(true), WORD_STRUCTURE(
                                true), SYNONYM_GROUPS(false), SPLIT_C(false), USER_DATA(false), POS_ID(false);

        private static final List<Column> POS_PARTS = Arrays.asList(POS1, POS2, POS3, POS4, POS5, POS6);
        private final boolean required;

        Column(boolean required) {
            this.required = required;
        }

        /**
         * Parse string as Column, ignoring "_" and cases.
         */
        private static Column fromString(String str) {
            String processed = str.replace("_", "");
            for (Column col : Column.values()) {
                if (col.name().replace("_", "").equalsIgnoreCase(processed)) {
                    return col;
                }
            }
            return null;
        }
    }

    private static final Pattern INTEGER_REGEX = Pattern.compile("^-?\\d+$");
    public static final char LIST_DELIMITER = '/';

    private List<String> cachedRow;
    private int[] mapping;
    private final CSVParser parser;
    private final POSTable posTable;
    private final WordRef.Parser normRefParser; // for normalized form
    private final WordRef.Parser dictRefParser; // for dictionary form
    private final WordRef.Parser splitParser; // for splits

    public RawLexiconReader(CSVParser parser, POSTable pos) throws IOException {
        this.parser = parser;
        this.posTable = pos;
        resolveColumnLayout();
        if (isLegacyColumnLayout()) {
            normRefParser = WordRef.parser(pos, false, true, false);
            dictRefParser = WordRef.parser(pos, true, false, true);
            splitParser = WordRef.parser(pos, true, false, false);
        } else {
            normRefParser = WordRef.parser(pos, false, true, false);
            dictRefParser = WordRef.parser(pos, false, false, false);
            splitParser = WordRef.parser(pos, false, false, false);
        }
    }

    /** assume legacy column layout if header line is not present */
    private boolean isLegacyColumnLayout() {
        return mapping == null;
    }

    /** resolve header line and set to mapping if it exists. */
    private void resolveColumnLayout() throws IOException {
        List<String> row = parser.getNextRow();

        String leftId = row.get(Column.LEFT_ID.ordinal());
        if (INTEGER_REGEX.matcher(leftId).matches()) {
            this.cachedRow = row;
            this.mapping = null;
            return;
        }

        List<Column> remaining = new ArrayList<>(Arrays.asList(Column.values()));
        mapping = new int[remaining.size()];
        Arrays.fill(mapping, -1);

        for (int fieldId = 0; fieldId < row.size(); ++fieldId) {
            String field = row.get(fieldId);
            Column parsed = Column.fromString(field);
            if (parsed == null || !remaining.contains(parsed)) {
                throw new InputFileException(parser.getName(), 0, field,
                        new IllegalArgumentException("Invalid column name"));
            }
            mapping[parsed.ordinal()] = fieldId;
            remaining.remove(remaining.indexOf(parsed));
        }

        List<Column> missings = remaining.stream().filter(c -> c.required).collect(Collectors.toList());
        if (!missings.isEmpty()) {
            StringJoiner joiner = new StringJoiner(", ", "required columns [", "] were not present in the header");
            missings.stream().forEach(c -> joiner.add(c.name()));
            throw new InputFileException(parser.getName(), 0, "", new IllegalArgumentException(joiner.toString()));
        }

        verifyPosColumns();
    }

    private void verifyPosColumns() {
        if (mapping == null) {
            return;
        }

        long numPosColumnsFound = Column.POS_PARTS.stream().filter(c -> mapping[c.ordinal()] >= 0).count();
        if (numPosColumnsFound != 0 && numPosColumnsFound != POS.DEPTH) {
            throw new InputFileException(parser.getName(), 0, "POS_PARTS",
                    new IllegalArgumentException("Pos1 ~ Pos6 columns must appear as a set."));
        }

        boolean posIdExists = mapping[Column.POS_ID.ordinal()] >= 0;
        boolean posPartsExists = numPosColumnsFound == POS.DEPTH;
        if (!posIdExists && !posPartsExists) {
            throw new InputFileException(parser.getName(), 0, "POS",
                    new IllegalArgumentException("Both or either PosId column or Pos1~Pos6 columns are required."));
        }
    }

    /** parse specified column as string */
    private String get(List<String> data, Column column, boolean unescape) {
        int index = column.ordinal();
        if (mapping != null) {
            index = mapping[index];
        }
        if (index < 0 || index >= data.size()) {
            if (column.required) {
                throw new InputFileException(parser.getName(), parser.getRowCount(), column.name(),
                        new IllegalArgumentException(String.format("column [%s] was not present", column.name())));
            } else {
                return "";
            }
        }
        String s = data.get(index);
        if (unescape) {
            return Unescape.unescape(s);
        } else {
            return s;
        }
    }

    private String getNonEmpty(List<String> data, Column column, boolean unescape) {
        String value = get(data, column, unescape);
        if (value.isEmpty()) {
            throw new InputFileException(parser.getName(), parser.getRowCount(), column.name(),
                    new IllegalArgumentException(String.format("Column %s cannot be empty", column.name())));
        }
        return value;
    }

    /** parse specified column as short */
    private short getShort(List<String> data, Column column) {
        String value = get(data, column, false);
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new InputFileException(parser.getName(), parser.getRowCount(), column.name(),
                    new IllegalArgumentException(String.format("failed to parse '%s' as a short value", value)));
        }
    }

    /** parse specified column as Ints. */
    private Ints getInts(List<String> data, Column column) {
        String value = get(data, column, false);
        if (value == null || value.isEmpty() || "*".equals(value)) {
            return Ints.wrap(Ints.EMPTY_ARRAY);
        }
        String[] parts = value.split(String.valueOf(LIST_DELIMITER));
        if (parts.length > Byte.MAX_VALUE) {
            throw new InputFileException(parser.getName(), parser.getRowCount(), column.name(),
                    new IllegalArgumentException("int list contained more than 127 entries: " + value));
        }
        Ints result = new Ints(parts.length);
        for (String part : parts) {
            result.append(Integer.parseInt(part));
        }
        return result;
    }

    /** parse specified column as WordRef list. */
    private List<WordRef> getWordRefs(List<String> data, Column column, WordRef.Parser refParser) {
        String value = get(data, column, false);
        if (value == null || value.isEmpty() || "*".equals(value)) {
            return new ArrayList<>();
        }
        String[] parts = value.split(String.valueOf(LIST_DELIMITER));
        if (parts.length > Byte.MAX_VALUE) {
            throw new InputFileException(parser.getName(), parser.getRowCount(), column.name(),
                    new IllegalArgumentException("reference list contained more than 127 entries: " + value));
        }
        List<WordRef> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            try {
                result.add(refParser.parse(part));
            } catch (IllegalArgumentException e) {
                throw new InputFileException(parser.getName(), parser.getRowCount(), column.name(), e);
            }
        }
        return result;
    }

    /** parse specified column as WordRef, also checks self-reference. */
    private WordRef getWordRef(List<String> data, Column column, WordRef.Parser refParser, RawWordEntry entry) {
        String value = get(data, column, false);
        WordRef ref;
        try {
            ref = refParser.parse(value);
        } catch (IllegalArgumentException e) {
            throw new InputFileException(parser.getName(), parser.getRowCount(), column.name(), e);
        }

        // if parsed ref seems to refering current entry, return self-reference (null),
        // because headword/triple ref may resolved to other entry.
        if (ref instanceof WordRef.Headword) {
            WordRef.Headword headword = (WordRef.Headword) ref;
            if (headword.getHeadword().equals(entry.headword())) {
                return null;
            }
        } else if (ref instanceof WordRef.Triple) {
            WordRef.Triple triple = (WordRef.Triple) ref;
            if (triple.getHeadword().equals(entry.headword()) && triple.getPosId() == entry.posId
                    && triple.getReading().equals(entry.reading)) {
                return null;
            }
        }
        return ref;
    }

    /** parse POS columns. */
    private short getPos(List<String> data) {
        short posStrId = -1;
        if (!get(data, Column.POS1, false).isEmpty()) {
            POS pos = new POS(
                    // comment for line break
                    get(data, Column.POS1, true), get(data, Column.POS2, true), get(data, Column.POS3, true),
                    get(data, Column.POS4, true), get(data, Column.POS5, true), get(data, Column.POS6, true));
            posStrId = posTable.getId(pos);
        }

        short posId = -1;
        if (!get(data, Column.POS_ID, false).isEmpty()) {
            posId = getShort(data, Column.POS_ID);

            if (posId >= posTable.size()) {
                throw new InputFileException(parser.getName(), parser.getRowCount(), "POS_ID",
                        new IllegalArgumentException(
                                String.format("POS for id %d is not present in the table.", posId)));
            }
        }

        if (posId < 0 && posStrId < 0) {
            throw new InputFileException(parser.getName(), parser.getRowCount(), "POS",
                    new IllegalArgumentException("Both PosId and Pos1-6 are missing."));
        }
        if (posId >= 0 && posStrId >= 0 && posId != posStrId) {
            throw new InputFileException(parser.getName(), parser.getRowCount(), "POS", new IllegalArgumentException(
                    String.format("PosId (%d) and id from Pos1-6 (%d) does not match.", posId, posStrId)));
        }

        return posId >= 0 ? posId : posStrId;
    }

    /** convert csv row to RawWordEntry */
    private RawWordEntry convertEntry(List<String> data) {
        RawWordEntry entry = new RawWordEntry();
        entry.surface = getNonEmpty(data, Column.SURFACE, true);
        String writing = get(data, Column.WRITING, true);
        entry.writing = writing.isEmpty() ? entry.surface : writing;

        entry.leftId = getShort(data, Column.LEFT_ID);
        entry.rightId = getShort(data, Column.RIGHT_ID);
        entry.cost = getShort(data, Column.COST);

        entry.reading = get(data, Column.READING_FORM, true);
        entry.posId = getPos(data);

        // writing, pos, reading must be parsed before these to resolve wordref.
        entry.normalizedForm = getWordRef(data, Column.NORMALIZED_FORM, normRefParser, entry);
        entry.dictionaryForm = getWordRef(data, Column.DICTIONARY_FORM, dictRefParser, entry);

        entry.mode = get(data, Column.MODE, false);
        entry.aUnitSplit = getWordRefs(data, Column.SPLIT_A, splitParser);
        entry.bUnitSplit = getWordRefs(data, Column.SPLIT_B, splitParser);
        entry.cUnitSplit = getWordRefs(data, Column.SPLIT_C, splitParser);
        entry.wordStructure = getWordRefs(data, Column.WORD_STRUCTURE, splitParser);
        entry.synonymGroups = getInts(data, Column.SYNONYM_GROUPS);
        entry.userData = get(data, Column.USER_DATA, true);

        try {
            entry.validate();
        } catch (IllegalArgumentException e) {
            throw new InputFileException(parser.getName(), parser.getRowCount(), "", e);
        }
        return entry;
    }

    /** @return next entry parsed */
    public RawWordEntry nextEntry() throws IOException {
        List<String> row = cachedRow;
        if (row != null) {
            cachedRow = null;
        } else {
            row = parser.getNextRow();
        }
        if (row == null) {
            return null;
        }
        RawWordEntry entry = convertEntry(row);
        entry.sourceLine = parser.getRowCount();
        entry.sourceName = parser.getName();
        return entry;
    }
}

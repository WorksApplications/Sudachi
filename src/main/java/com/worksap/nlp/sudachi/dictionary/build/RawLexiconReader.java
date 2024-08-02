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

/**
 * Reader for the lexicon csv file.
 */
public class RawLexiconReader {
    /**
     * Enum order is in legacy csv order. If a header is present, fields will be
     * reordered with respect to the header.
     */
    public enum Column {
        Surface(true), LeftId(true), RightId(true), Cost(true), Writing(false), Pos1(false), Pos2(false), Pos3(
                false), Pos4(false), Pos5(false), Pos6(false), ReadingForm(true), NormalizedForm(true), DictionaryForm(
                        true), Mode(false), SplitA(true), SplitB(true), WordStructure(
                                true), SynonymGroups(false), SplitC(false), UserData(false), PosId(false);

        private final boolean required;

        Column(boolean required) {
            this.required = required;
        }
    }

    private List<String> cachedRecord;
    private int[] mapping;
    private final CSVParser parser;
    private final POSTable posTable;
    private final WordRef.Parser normRefParser; // for normalized form
    private final WordRef.Parser dictRefParser; // for dictionary form
    private final WordRef.Parser splitParser; // for splits
    private boolean posIdExists = false;
    private boolean posStrExists = true;

    public RawLexiconReader(CSVParser parser, POSTable pos, boolean user) throws IOException {
        this.parser = parser;
        this.posTable = pos;
        resolveColumnLayout();
        if (isLegacyColumnLayout()) {
            normRefParser = WordRef.parser(pos, false, true, false);
            dictRefParser = WordRef.parser(pos, true, false, true);
            splitParser = WordRef.parser(pos, true, false, false);
        } else {
            normRefParser = WordRef.parser(pos, false, true, false);
            dictRefParser = WordRef.parser(pos, !user, false, false);
            splitParser = WordRef.parser(pos, !user, false, false);
        }
    }

    private static final Pattern INTEGER_REGEX = Pattern.compile("^-?\\d+$");

    /** assume legacy column layout if header line is not present */
    private boolean isLegacyColumnLayout() {
        return mapping == null;
    }

    /** resolve header line and set to mapping if it exists. */
    private void resolveColumnLayout() throws IOException {
        List<String> record = parser.getNextRecord();

        String leftId = record.get(Column.LeftId.ordinal());
        if (INTEGER_REGEX.matcher(leftId).matches()) {
            this.cachedRecord = record;
            return;
        }

        List<Column> remaining = new ArrayList<>(Arrays.asList(Column.values()));
        int[] mapping = new int[remaining.size()];
        Arrays.fill(mapping, -1);

        outer: for (int fieldId = 0; fieldId < record.size(); ++fieldId) {
            String field = record.get(fieldId).replaceAll("_", "");
            for (int colId = 0; colId < remaining.size(); ++colId) {
                Column col = remaining.get(colId);
                if (col.name().equalsIgnoreCase(field)) {
                    mapping[col.ordinal()] = fieldId;
                    remaining.remove(colId);
                    continue outer;
                }
            }
            throw new CsvFieldException(parser.getName(), 0, field,
                    new IllegalArgumentException("Invalid column name"));
        }

        for (Column column : remaining) {
            if (column.required) {
                StringJoiner joiner = new StringJoiner(", ", "required columns [", "] were not present in the header");
                remaining.stream().filter(c -> c.required).forEach(c -> joiner.add(c.name()));
                throw new CsvFieldException(parser.getName(), 0, "", new IllegalArgumentException(joiner.toString()));
            }
        }

        this.posIdExists = mapping[Column.PosId.ordinal()] >= 0;
        long numPosColumnsFound = Arrays
                .asList(Column.Pos1, Column.Pos2, Column.Pos3, Column.Pos4, Column.Pos5, Column.Pos6).stream()
                .filter(c -> mapping[c.ordinal()] >= 0).count();
        if (numPosColumnsFound != 0 && numPosColumnsFound != POS.DEPTH) {
            throw new CsvFieldException(parser.getName(), 0, "POS",
                    new IllegalArgumentException("Pos1 ~ Pos6 columns must appear as a set."));
        }
        this.posStrExists = numPosColumnsFound == POS.DEPTH;
        if (!posIdExists && !posStrExists) {
            throw new CsvFieldException(parser.getName(), 0, "POS",
                    new IllegalArgumentException("Both or either PosId column or Pos1~Pos6 columns are required."));
        }

        this.mapping = mapping;
    }

    /** parse specified column as string */
    private String get(List<String> data, Column column, boolean unescape) {
        int index = column.ordinal();
        if (mapping != null) {
            index = mapping[index];
        }
        if (index < 0 || index >= data.size()) {
            if (column.required) {
                throw new CsvFieldException(parser.getName(), parser.getRow(), column.name(),
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
            throw new CsvFieldException(parser.getName(), parser.getRow(), column.name(),
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
            throw new CsvFieldException(parser.getName(), parser.getRow(), column.name(),
                    new IllegalArgumentException(String.format("failed to parse '%s' as a short value", value)));
        }
    }

    /** parse specified column as Ints. */
    private Ints getInts(List<String> data, Column column) {
        String value = get(data, column, false);
        if (value == null || value.isEmpty() || "*".equals(value)) {
            return Ints.wrap(Ints.EMPTY_ARRAY);
        }
        String[] parts = value.split("/");
        if (parts.length > Byte.MAX_VALUE) {
            throw new CsvFieldException(parser.getName(), parser.getRow(), column.name(),
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
        String[] parts = value.split("/");
        if (parts.length > Byte.MAX_VALUE) {
            throw new CsvFieldException(parser.getName(), parser.getRow(), column.name(),
                    new IllegalArgumentException("reference list contained more than 127 entries: " + value));
        }
        List<WordRef> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            try {
                result.add(refParser.parse(part));
            } catch (IllegalArgumentException e) {
                throw new CsvFieldException(parser.getName(), parser.getRow(), column.name(), e);
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
            throw new CsvFieldException(parser.getName(), parser.getRow(), column.name(), e);
        }

        // if parsed ref seems to refering current entry, return self-reference (null),
        // because headword/triple ref may resolved to other entry.
        if (ref instanceof WordRef.Headword) {
            WordRef.Headword headword = (WordRef.Headword) ref;
            if (headword.getHeadword().equals(entry.headword)) {
                return null;
            }
        } else if (ref instanceof WordRef.Triple) {
            WordRef.Triple triple = (WordRef.Triple) ref;
            if (triple.getHeadword().equals(entry.headword) && triple.getPosId() == entry.posId
                    && triple.getReading().equals(entry.reading)) {
                return null;
            }
        }
        return ref;
    }

    /** parse POS columns. */
    private short getPos(List<String> data) {
        short posId = -1;
        short posStrId = -1;

        if (this.posIdExists) {
            posId = getShort(data, Column.PosId);
        }
        if (this.posStrExists) {
            POS pos = new POS(
                    // comment for line break
                    get(data, Column.Pos1, true), get(data, Column.Pos2, true), get(data, Column.Pos3, true),
                    get(data, Column.Pos4, true), get(data, Column.Pos5, true), get(data, Column.Pos6, true));
            posStrId = posTable.getId(pos);
        }
        if (this.posIdExists && this.posStrExists && posId != posStrId) {
            throw new CsvFieldException(parser.getName(), parser.getRow(), "POS", new IllegalArgumentException(
                    String.format("PosId (%d) and id from Pos1-6 (%d) does not match.", posId, posStrId)));
        }

        return this.posIdExists ? posId : posStrId;
    }

    /** convert csv row to RawWordEntry */
    private RawWordEntry convertEntry(List<String> data) {
        RawWordEntry entry = new RawWordEntry();
        entry.headword = getNonEmpty(data, Column.Surface, true);

        entry.leftId = getShort(data, Column.LeftId);
        entry.rightId = getShort(data, Column.RightId);
        entry.cost = getShort(data, Column.Cost);

        entry.reading = get(data, Column.ReadingForm, true);
        entry.posId = getPos(data);

        // headword, pos, reading must be parsed before these.
        entry.normalizedForm = getWordRef(data, Column.NormalizedForm, normRefParser, entry);
        entry.dictionaryForm = getWordRef(data, Column.DictionaryForm, dictRefParser, entry);

        entry.mode = get(data, Column.Mode, false);
        entry.aUnitSplit = getWordRefs(data, Column.SplitA, splitParser);
        entry.bUnitSplit = getWordRefs(data, Column.SplitB, splitParser);
        entry.cUnitSplit = getWordRefs(data, Column.SplitC, splitParser);
        entry.wordStructure = getWordRefs(data, Column.WordStructure, splitParser);
        entry.synonymGroups = getInts(data, Column.SynonymGroups);
        entry.userData = get(data, Column.UserData, true);

        try {
            entry.validate();
        } catch (IllegalArgumentException e) {
            throw new CsvFieldException(parser.getName(), parser.getRow(), "", e);
        }
        return entry;
    }

    /** @return next entry parsed */
    public RawWordEntry nextEntry() throws IOException {
        List<String> record = cachedRecord;
        if (record != null) {
            cachedRecord = null;
        } else {
            record = parser.getNextRecord();
        }
        if (record == null) {
            return null;
        }
        RawWordEntry entry = convertEntry(record);
        entry.sourceLine = parser.getRow();
        entry.sourceName = parser.getName();
        return entry;
    }
}

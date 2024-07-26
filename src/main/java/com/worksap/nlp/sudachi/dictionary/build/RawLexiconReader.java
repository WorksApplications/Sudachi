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

import com.worksap.nlp.sudachi.dictionary.CSVParser;
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
        Surface(true), LeftId(true), RightId(true), Cost(true), Writing(false), Pos1(true), Pos2(true), Pos3(
                true), Pos4(true), Pos5(true), Pos6(true), ReadingForm(true), NormalizedForm(true), DictionaryForm(
                        true), Mode(true), SplitA(true), SplitB(
                                true), WordStructure(true), SynonymGroups(false), SplitC(false), UserData(false);

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

    public RawLexiconReader(CSVParser parser, POSTable pos, boolean user) throws IOException {
        this.parser = parser;
        this.posTable = pos;
        resolveColumnLayout();
        if (isLegacyColumnLayout()) {
            normRefParser = WordRef.parser(pos, false, true, false);
            dictRefParser = WordRef.parser(pos, true, true, true);
            splitParser = WordRef.parser(pos, true, false, false);
        } else {
            normRefParser = WordRef.parser(pos, false, false, false);
            dictRefParser = WordRef.parser(pos, !user, false, false);
            splitParser = WordRef.parser(pos, false, false, false);
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
            for (int colId = 0; colId < record.size(); ++colId) {
                Column col = remaining.get(colId);
                if (col.name().equalsIgnoreCase(field)) {
                    mapping[col.ordinal()] = fieldId;
                    remaining.remove(colId);
                    continue outer;
                }
            }
            throw new IllegalArgumentException(String.format("column [%s] is not recognized", field));
        }

        for (Column column : remaining) {
            if (column.required) {
                StringJoiner joiner = new StringJoiner(", ", "required columns [", "] were not present in the header");
                remaining.stream().filter(c -> c.required).forEach(c -> joiner.add(c.name()));
                throw new IllegalArgumentException(joiner.toString());
            }
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
                throw new CsvFieldException(
                        String.format("column [%s] (index=%d) was not present", column.name(), index));
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

    /** parse specified column as short */
    private short getShort(List<String> data, Column column) {
        String value = get(data, column, false);
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new CsvFieldException(
                    String.format("failed to parse '%s' as a short value in column: %s", value, column.name()));
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
            throw new IllegalArgumentException("int list contained more than 127 entries: " + value);
        }
        Ints result = new Ints(parts.length);
        for (String part : parts) {
            result.append(Integer.parseInt(part));
        }
        return result;
    }

    /** parse specified column as WordRef list. */
    private List<WordRef> getWordRefs(List<String> data, Column column, WordRef.Parser parser) {
        String value = get(data, column, false);
        if (value == null || value.isEmpty() || "*".equals(value)) {
            return new ArrayList<>();
        }
        String[] parts = value.split("/");
        if (parts.length > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("reference list contained more than 127 entries: " + value);
        }
        List<WordRef> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            result.add(parser.parse(part));
        }
        return result;
    }

    /** convert csv row to RawWordEntry */
    private RawWordEntry convertEntry(List<String> data) {
        RawWordEntry entry = new RawWordEntry();
        entry.headword = get(data, Column.Surface, true);
        entry.leftId = getShort(data, Column.LeftId);
        entry.rightId = getShort(data, Column.RightId);
        entry.cost = getShort(data, Column.Cost);

        entry.reading = get(data, Column.ReadingForm, true);
        entry.normalizedForm = normRefParser.parse(get(data, Column.NormalizedForm, false));
        entry.dictionaryForm = dictRefParser.parse(get(data, Column.DictionaryForm, false));

        POS pos = new POS(
                // comment for line break
                get(data, Column.Pos1, true), get(data, Column.Pos2, true), get(data, Column.Pos3, true),
                get(data, Column.Pos4, true), get(data, Column.Pos5, true), get(data, Column.Pos6, true));

        entry.posId = posTable.getId(pos);

        entry.mode = get(data, Column.Mode, false);
        entry.aUnitSplit = getWordRefs(data, Column.SplitA, splitParser);
        entry.bUnitSplit = getWordRefs(data, Column.SplitB, splitParser);
        entry.cUnitSplit = getWordRefs(data, Column.SplitC, splitParser);
        entry.wordStructure = getWordRefs(data, Column.WordStructure, splitParser);
        entry.synonymGroups = getInts(data, Column.SynonymGroups);
        entry.userData = get(data, Column.UserData, true);

        entry.validate();

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

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

import com.worksap.nlp.sudachi.StringUtil;
import com.worksap.nlp.sudachi.WordId;
import com.worksap.nlp.sudachi.dictionary.POS;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CsvLexicon implements WriteDictionary {
    static final int ARRAY_MAX_LENGTH = Byte.MAX_VALUE;
    static final int MIN_REQUIRED_NUMBER_OF_COLUMNS = 18;
    static final Pattern unicodeLiteral = Pattern.compile("\\\\u([0-9a-fA-F]{4}|\\{[0-9a-fA-F]+})");
    private static final Pattern PATTERN_ID = Pattern.compile("U?\\d+");
    private final Parameters parameters = new Parameters();
    private final POSTable posTable;
    private final List<RawWordEntry> entries = new ArrayList<>();
    private WordIdResolver widResolver = null;

    public CsvLexicon(POSTable pos) {
        posTable = pos;
    }

    public void setResolver(WordIdResolver widResolver) {
        this.widResolver = widResolver;
    }

    public List<RawWordEntry> getEntries() {
        return entries;
    }

    RawWordEntry parseLine(List<String> cols) {
        if (cols.size() < MIN_REQUIRED_NUMBER_OF_COLUMNS) {
            throw new IllegalArgumentException("invalid format");
        }
        for (int i = 0; i < 15; i++) {
            cols.set(i, Unescape.unescape(cols.get(i)));
        }

        if (cols.get(0).getBytes(StandardCharsets.UTF_8).length > DicBuffer.MAX_STRING
                || !DicBuffer.isValidLength(cols.get(4)) || !DicBuffer.isValidLength(cols.get(11))
                || !DicBuffer.isValidLength(cols.get(12))) {
            throw new IllegalArgumentException("string is too long");
        }

        if (cols.get(0).isEmpty()) {
            throw new IllegalArgumentException("headword is empty");
        }

        RawWordEntry entry = new RawWordEntry();

        // headword for trie
        if (!cols.get(1).equals("-1")) {
            entry.headword = cols.get(0);
        }

        // left-id, right-id, cost
        short leftId = Short.parseShort(cols.get(1));
        short rightId = Short.parseShort(cols.get(2));
        short cost = Short.parseShort(cols.get(3));
        parameters.add(leftId, rightId, cost);
        entry.leftId = leftId;
        entry.rightId = rightId;
        entry.cost = cost;

        // part of speech
        POS pos = new POS(cols.get(5), cols.get(6), cols.get(7), cols.get(8), cols.get(9), cols.get(10));
        short posId = posTable.getId(pos);

        entry.aUnitSplitString = cols.get(15);
        entry.bUnitSplitString = cols.get(16);
        entry.wordStructureString = cols.get(17);
        checkSplitInfoFormat(entry.aUnitSplitString);
        checkSplitInfoFormat(entry.bUnitSplitString);
        checkSplitInfoFormat(entry.wordStructureString);
        if (cols.get(14).equals("A") && (!entry.aUnitSplitString.equals("*") || !entry.bUnitSplitString.equals("*"))) {
            throw new IllegalArgumentException("invalid splitting");
        }
        return entry;
    }

    int[] parseSynonymGids(String str) {
        if (str.equals("*")) {
            return new int[0];
        }
        String[] ids = str.split("/");
        if (ids.length > ARRAY_MAX_LENGTH) {
            throw new IllegalArgumentException("too many units");
        }
        int[] ret = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            ret[i] = Integer.parseInt(ids[i]);
        }
        return ret;
    }

    int wordToId(String text) {
        String[] cols = text.split(",", 8);
        if (cols.length < 8) {
            throw new IllegalArgumentException("too few columns");
        }
        String headword = Unescape.unescape(cols[0]);
        POS pos = new POS(Arrays.copyOfRange(cols, 1, 7));
        short posId = posTable.getId(pos);
        String reading = Unescape.unescape(cols[7]);
        return widResolver.lookup(headword, posId, reading);
    }

    void checkSplitInfoFormat(String info) {
        if (StringUtil.count(info, '/') + 1 > ARRAY_MAX_LENGTH) {
            throw new IllegalArgumentException("too many units");
        }
    }

    boolean isId(String text) {
        return PATTERN_ID.matcher(text).matches();
    }

    int[] parseSplitInfo(String info) {
        if (info.equals("*")) {
            return new int[0];
        }
        String[] words = info.split("/");
        if (words.length > ARRAY_MAX_LENGTH) {
            throw new IllegalArgumentException("too many units");
        }
        int[] ret = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            String ref = words[i];
            if (isId(ref)) {
                ret[i] = parseId(ref);
            } else {
                ret[i] = wordToId(ref);
                if (ret[i] < 0) {
                    throw new IllegalArgumentException("couldn't find " + ref + " in the dictionaries");
                }
            }
        }
        return ret;
    }

    int parseId(String text) {
        int id = 0;
        if (text.startsWith("U")) {
            id = Integer.parseInt(text.substring(1));
            if (widResolver.isUser()) {
                id = WordId.make(1, id);
            }
        } else {
            id = Integer.parseInt(text);
        }
        widResolver.validate(id);
        return id;
    }

    @Override
    public void writeTo(ModelOutput output) throws IOException {

    }

    public int addEntry(RawWordEntry e) {
        int id = entries.size();
        entries.add(e);
        return id;
    }

    public void setLimits(int left, int right) {
        parameters.setLimits(left, right);
    }

}

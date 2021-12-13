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

import com.worksap.nlp.sudachi.dictionary.POS;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvLexicon {
    static class WordEntry {
        String headword;
        WordInfo wordInfo;
        String aUnitSplitString;
        String bUnitSplitString;
        String wordStructureString;
    }

    static final int ARRAY_MAX_LENGTH = Byte.MAX_VALUE;
    static final int MIN_REQUIRED_NUMBER_OF_COLUMNS = 18;

    static final Pattern unicodeLiteral = Pattern.compile("\\\\u([0-9a-fA-F]{4}|\\{[0-9a-fA-F]+})");

    /**
     * Resolve unicode escape sequences in the string
     *
     * Sequences are defined to be \\u0000-\\uFFFF: exactly four hexadecimal
     * characters preceeded by \\u \\u{...}: a correct unicode character inside
     * brackets
     * 
     * @param text
     *            to to resolve sequences
     * @return string with unicode escapes resolved
     */
    public static String unescape(String text) {
        Matcher m = unicodeLiteral.matcher(text);
        if (!m.find()) {
            return text;
        }

        StringBuffer sb = new StringBuffer();
        m.reset();
        while (m.find()) {
            String u = m.group(1);
            if (u.startsWith("{")) {
                u = u.substring(1, u.length() - 1);
            }
            m.appendReplacement(sb, new String(Character.toChars(Integer.parseInt(u, 16))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private final Parameters parameters = new Parameters();

    WordEntry parseLine(String[] cols) {
        if (cols.length < MIN_REQUIRED_NUMBER_OF_COLUMNS) {
            throw new IllegalArgumentException("invalid format");
        }
        for (int i = 0; i < 15; i++) {
            cols[i] = unescape(cols[i]);
        }

        if (cols[0].getBytes(StandardCharsets.UTF_8).length > Strings.MAX_LENGTH || !Strings.isValidLength(cols[4])
                || !Strings.isValidLength(cols[11]) || !Strings.isValidLength(cols[12])) {
            throw new IllegalArgumentException("string is too long");
        }

        if (cols[0].isEmpty()) {
            throw new IllegalArgumentException("headword is empty");
        }

        WordEntry entry = new WordEntry();

        // headword for trie
        if (!cols[1].equals("-1")) {
            entry.headword = cols[0];
        }

        // left-id, right-id, cost
        parameters.add(Short.parseShort(cols[1]), Short.parseShort(cols[2]), Short.parseShort(cols[3]));

        // part of speech
        POS pos = new POS(Arrays.copyOfRange(cols, 5, 11));
        short posId = getPosId(pos);
        if (posId < 0) {
            throw new IllegalArgumentException("invalid part of speech");
        }

        entry.aUnitSplitString = cols[15];
        entry.bUnitSplitString = cols[16];
        entry.wordStructureString = cols[17];
        checkSplitInfoFormat(entry.aUnitSplitString);
        checkSplitInfoFormat(entry.bUnitSplitString);
        checkSplitInfoFormat(entry.wordStructureString);
        if (cols[14].equals("A") && (!entry.aUnitSplitString.equals("*") || !entry.bUnitSplitString.equals("*"))) {
            throw new IllegalArgumentException("invalid splitting");
        }

        int[] synonymGids = new int[0];
        if (cols.length > 18) {
            synonymGids = parseSynonymGids(cols[18]);
        }

        entry.wordInfo = new WordInfo(cols[4], // headword
                (short) cols[0].getBytes(StandardCharsets.UTF_8).length, posId, cols[12], // normalizedForm
                (cols[13].equals("*") ? -1 : Integer.parseInt(cols[13])), // dictionaryFormWordId
                "", // dummy
                cols[11], // readingForm
                null, null, null, synonymGids);

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

    void checkSplitInfoFormat(String info) {
        if (info.chars().filter(i -> i == '/').count() + 1 > ARRAY_MAX_LENGTH) {
            throw new IllegalArgumentException("too many units");
        }
    }

    private final POSTable posTable = new POSTable();

    public short getPosId(POS pos) {
        return posTable.getId(pos);
    }

}

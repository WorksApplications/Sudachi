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

import com.worksap.nlp.sudachi.StringUtil;
import com.worksap.nlp.sudachi.dictionary.POS;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Reference to a word in the CSV dictionary.
 */
public abstract class WordRef {
    public abstract int resolve(Lookup2 resolver);

    public static final class LineNo extends WordRef {
        private final int line;

        public LineNo(int line) {
            this.line = line;
        }

        public int getLine() {
            return line;
        }

        @Override
        public int resolve(Lookup2 resolver) {
            return resolver.byIndex(line).pointer();
        }

        @Override
        public String toString() {
            return String.format("WordRef/Line: %d", line);
        }
    }

    public static final class Headword extends WordRef {
        private final String headword;

        public Headword(String headword) {
            this.headword = headword;
        }

        public String getHeadword() {
            return headword;
        }

        @Override
        public int resolve(Lookup2 resolver) {
            List<Lookup2.Entry> entries = resolver.byHeadword(headword);
            return entries.get(0).pointer();
        }

        @Override
        public String toString() {
            return String.format("WordRef/Headword: %s", headword);
        }
    }

    public static final class Triple extends WordRef {
        private final String headword;
        private final short posId;
        private final String reading;

        public Triple(String headword, short posId, String reading) {
            this.headword = headword;
            this.posId = posId;
            this.reading = reading;
        }

        public String getHeadword() {
            return headword;
        }

        public short getPosId() {
            return posId;
        }

        public String getReading() {
            return reading;
        }

        @Override
        public int resolve(Lookup2 resolver) {
            List<Lookup2.Entry> entries = resolver.byHeadword(headword);
            for (Lookup2.Entry entry : entries) {
                if (entry.matches(posId, reading)) {
                    return entry.pointer();
                }
            }
            return -1;
        }

        @Override
        public String toString() {
            return String.format("WordRef: %s/%d/%s", headword, posId, reading);
        }
    }

    private static final Pattern NUMERIC_RE = Pattern.compile("^U?\\d+$");

    public static Parser parser(POSTable posTable, boolean allowNumeric, boolean allowHeadword) {
        return new Parser(posTable, allowNumeric, allowHeadword);
    }

    public static class Parser {
        private final POSTable posTable;
        private final boolean allowNumeric;
        private final boolean allowHeadword;

        public Parser(POSTable posTable, boolean allowNumeric, boolean allowHeadword) {
            this.posTable = posTable;
            this.allowNumeric = allowNumeric;
            this.allowHeadword = allowHeadword;
        }

        public WordRef parse(String text) {
            if ("*".equals(text) || text == null || text.isEmpty()) {
                return null;
            }

            if (NUMERIC_RE.matcher(text).matches()) {
                if (!allowNumeric) {
                    throw new CsvFieldException(
                            String.format("invalid word reference: %s, numeric references are not supported", text));
                }
                int offset = text.charAt(0) == 'U' ? 1 : 0;
                int lineNum = Integer.parseInt(text.substring(offset));
                return new LineNo(lineNum);
            }

            if (StringUtil.count(text, ',') == 7) {
                String[] cols = text.split(",", 8);
                String headword = Unescape.unescape(cols[0]);
                String[] posElems = Arrays.copyOfRange(cols, 1, 7);
                for (int i = 0; i < POS.DEPTH; ++i) {
                    posElems[i] = Unescape.unescape(posElems[i]);
                }
                POS pos = new POS(posElems);
                short posId = posTable.getId(pos);
                String reading = Unescape.unescape(cols[7]);
                return new Triple(headword, posId, reading);
            }

            if (allowHeadword) {
                return new Headword(text);
            } else {
                throw new CsvFieldException(
                        String.format("invalid word reference: %s, it must contain POS tag and reading", text));
            }
        }

    }
}

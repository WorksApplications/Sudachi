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
import com.worksap.nlp.sudachi.WordId;
import com.worksap.nlp.sudachi.dictionary.POS;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reference to a word in the lexicon csv.
 */
public abstract class WordRef {
    /** resolve word ref into pointer (word id) using resolver. */
    public abstract int resolve(Lookup2 resolver);

    /**
     * Encode the target entry as wordref.
     * 
     * wordref (32 bits) has similar structure as combined word id, but its dict
     * part contains a flag that indicates if the referencing entry is in the same
     * dict or referencing system dict.
     * 
     * @param entry
     *            to encode
     * @return encoded wordref
     */
    public int intoWordRef(Lookup2.EntryWithFlag entry) {
        return WordId.make(entry.isUser ? 1 : 0, entry.pointer());
    }

    /**
     * Reference written by line number of the lexicon csv file.
     */
    public static final class LineNo extends WordRef {
        private final int line;
        private final boolean isUser;

        public LineNo(int line, boolean isUser) {
            this.line = line;
            this.isUser = isUser;
        }

        public int getLine() {
            return line;
        }

        @Override
        public int resolve(Lookup2 resolver) {
            return intoWordRef(resolver.byIndex(line, isUser));
        }

        @Override
        public String toString() {
            return String.format("WordRef/Line: %s%d", isUser ? "U" : "S", line);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null || getClass() != other.getClass())
                return false;
            LineNo o = (LineNo) other;
            return (line == o.line) && (isUser == o.isUser);
        }
    }

    /**
     * Reference written by surface.
     */
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
            List<Lookup2.EntryWithFlag> entries = resolver.byHeadword(headword);
            return intoWordRef(entries.get(0));
        }

        @Override
        public String toString() {
            return String.format("WordRef/Headword: %s", headword);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null || getClass() != other.getClass())
                return false;
            Headword o = (Headword) other;
            return headword.equals(o.headword);
        }
    }

    /**
     * Reference written by surface-pos-reading tuple.
     */
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
            List<Lookup2.EntryWithFlag> entries = resolver.byHeadword(headword);
            for (Lookup2.EntryWithFlag entry : entries) {
                if (entry.matches(posId, reading)) {
                    return intoWordRef(entry);
                }
            }
            return -1;
        }

        @Override
        public String toString() {
            return String.format("WordRef: %s/%d/%s", headword, posId, reading);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null || getClass() != other.getClass())
                return false;
            Triple o = (Triple) other;
            return (headword.equals(o.headword)) && (posId == o.posId) && (reading.equals(o.reading));
        }
    }

    private static final Pattern NUMERIC_RE = Pattern.compile("^U?\\d+$");

    /** Alias of WordRef.Parser constructor. */
    public static Parser parser(POSTable posTable, boolean allowNumeric, boolean allowHeadword,
            boolean allowNullAsterisk) {
        return new Parser(posTable, allowNumeric, allowHeadword, allowNullAsterisk);
    }

    /** Parser to parse wordref from a string in the lexicon field. */
    public static class Parser {
        private final POSTable posTable;
        private final boolean allowNumeric;
        private final boolean allowHeadword;
        private final boolean allowNullAsterisk;

        public Parser(POSTable posTable, boolean allowNumeric, boolean allowHeadword, boolean allowNullAsterisk) {
            this.posTable = posTable;
            this.allowNumeric = allowNumeric;
            this.allowHeadword = allowHeadword;
            this.allowNullAsterisk = allowNullAsterisk;
        }

        /** @return WordRef parsed from the text. */
        public WordRef parse(String text) {
            if (text == null || text.isEmpty() || (allowNullAsterisk && "*".equals(text))) {
                return null;
            }

            if (allowNumeric && NUMERIC_RE.matcher(text).matches()) {
                boolean isUser = text.charAt(0) == 'U';
                int offset = isUser ? 1 : 0;
                int lineNum = Integer.parseInt(text.substring(offset));
                return new LineNo(lineNum, isUser);
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

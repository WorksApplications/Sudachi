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
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Reference to a word in the lexicon csv.
 */
public abstract class WordRef {
    /** resolve word ref into pointer (word id) using resolver. */
    public abstract int resolve(EntryLookup resolver);

    /**
     * @return true if this ref refers to the provided entry identity.
     */
    public boolean matches(String headword, short posId, String reading, String referenceId) {
        return false;
    }

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
    public int intoWordRef(EntryLookup.EntryWithFlag entry) {
        return WordId.make(entry.isUser ? 1 : 0, entry.pointer());
    }

    /**
     * Reference written by line number of the lexicon csv file.
     */
    public static final class RefByLineNo extends WordRef {
        private final int line;
        private final boolean isUser;

        public RefByLineNo(int line, boolean isUser) {
            this.line = line;
            this.isUser = isUser;
        }

        public int getLine() {
            return line;
        }

        @Override
        public int resolve(EntryLookup resolver) {
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
            RefByLineNo o = (RefByLineNo) other;
            return (line == o.line) && (isUser == o.isUser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, isUser);
        }

    }

    /**
     * Reference written by headword.
     */
    public static final class RefByHeadword extends WordRef {
        private final String headword;

        public RefByHeadword(String headword) {
            this.headword = headword;
        }

        public String getHeadword() {
            return headword;
        }

        @Override
        public boolean matches(String headword, short posId, String reading, String referenceId) {
            return this.headword.equals(headword);
        }

        @Override
        public int resolve(EntryLookup resolver) {
            List<EntryLookup.EntryWithFlag> entries = resolver.byHeadword(headword);
            // Use the first entry. This is ok since RefByHeadword is only allowed for the
            // normalized form and only the headword of the referred entry will be used.
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
            RefByHeadword o = (RefByHeadword) other;
            return headword.equals(o.headword);
        }

        @Override
        public int hashCode() {
            return Objects.hash(headword);
        }
    }

    /**
     * Reference written by headword-pos-reading and optional reference-id key.
     */
    public static final class RefByEntryKey extends WordRef {
        private final String headword;
        private final short posId;
        private final String reading;
        private final String referenceId;

        public RefByEntryKey(String headword, short posId, String reading) {
            this(headword, posId, reading, null);
        }

        public RefByEntryKey(String headword, short posId, String reading, String referenceId) {
            this.headword = headword;
            this.posId = posId;
            this.reading = reading;
            this.referenceId = normalizeReferenceId(referenceId);
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

        public String getReferenceId() {
            return referenceId;
        }

        @Override
        public boolean matches(String headword, short posId, String reading, String referenceId) {
            return this.headword.equals(headword) && this.posId == posId && this.reading.equals(reading)
                    && Objects.equals(this.referenceId, normalizeReferenceId(referenceId));
        }

        @Override
        public int resolve(EntryLookup resolver) {
            if (referenceId != null) {
                EntryLookup.EntryWithFlag entry = resolver.byReferenceId(referenceId);
                if (entry == null) {
                    throw new IllegalArgumentException("matching entry not found for the " + this.toString());
                }
                if (entry.matches(posId, reading) && headword.equals(entry.headword())) {
                    return intoWordRef(entry);
                }
                throw new IllegalArgumentException("matching entry not found for the " + this.toString());
            }

            List<EntryLookup.EntryWithFlag> entries = resolver.byHeadword(headword);
            if (entries == null) {
                throw new IllegalArgumentException("matching entry not found for the " + this.toString());
            }
            for (EntryLookup.EntryWithFlag entry : entries) {
                if (entry.matches(posId, reading)) {
                    return intoWordRef(entry);
                }
            }
            throw new IllegalArgumentException("matching entry not found for the " + this.toString());
        }

        @Override
        public String toString() {
            return String.format("WordRef/EntryKey: %s/%d/%s/%s", headword, posId, reading, referenceId);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null || getClass() != other.getClass())
                return false;
            RefByEntryKey o = (RefByEntryKey) other;
            return (headword.equals(o.headword)) && (posId == o.posId) && (reading.equals(o.reading))
                    && Objects.equals(referenceId, o.referenceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(headword, posId, reading, referenceId);
        }

        private static String normalizeReferenceId(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            return value;
        }
    }

    /** Alias of WordRef.Parser constructor. */
    public static Parser parser(POSTable posTable, boolean allowLineNo, boolean allowHeadword,
            boolean allowNullAsterisk) {
        return new Parser(posTable, allowLineNo, allowHeadword, allowNullAsterisk);
    }

    /** Parser to parse wordref from a string in the lexicon field. */
    public static class Parser {
        public static final char WORDREF_DELIMITER = ',';
        private static final Pattern NUMERIC_RE = Pattern.compile("^U?\\d+$");

        private final POSTable posTable;
        private final boolean allowLineNo;
        private final boolean allowHeadword;
        private final boolean allowNullAsterisk;

        public Parser(POSTable posTable, boolean allowLineNo, boolean allowHeadword, boolean allowNullAsterisk) {
            this.posTable = posTable;
            this.allowLineNo = allowLineNo;
            this.allowHeadword = allowHeadword;
            this.allowNullAsterisk = allowNullAsterisk;
        }

        /** @return WordRef parsed from the text. */
        public WordRef parse(String text) {
            if (text == null || text.isEmpty() || (allowNullAsterisk && "*".equals(text))) {
                return null;
            }

            if (allowLineNo && NUMERIC_RE.matcher(text).matches()) {
                boolean isUser = text.charAt(0) == 'U';
                int offset = isUser ? 1 : 0;
                int lineNum = Integer.parseInt(text.substring(offset));
                return new RefByLineNo(lineNum, isUser);
            }

            // entry key, pos is written as 6-parts
            if (StringUtil.count(text, WORDREF_DELIMITER) == 7) {
                String[] cols = text.split(String.valueOf(WORDREF_DELIMITER), 8);
                String headword = Unescape.unescape(cols[0]);
                String[] posElems = Arrays.copyOfRange(cols, 1, 7);
                for (int i = 0; i < POS.DEPTH; ++i) {
                    posElems[i] = Unescape.unescape(posElems[i]);
                }
                POS pos = new POS(posElems);
                short posId = posTable.getId(pos);
                String reading = Unescape.unescape(cols[7]);
                return new RefByEntryKey(headword, posId, reading);
            }

            // entry key, pos is written as 6-parts with reference-id
            if (StringUtil.count(text, WORDREF_DELIMITER) == 8) {
                String[] cols = text.split(String.valueOf(WORDREF_DELIMITER), 9);
                String headword = Unescape.unescape(cols[0]);
                String[] posElems = Arrays.copyOfRange(cols, 1, 7);
                for (int i = 0; i < POS.DEPTH; ++i) {
                    posElems[i] = Unescape.unescape(posElems[i]);
                }
                POS pos = new POS(posElems);
                short posId = posTable.getId(pos);
                String reading = Unescape.unescape(cols[7]);
                String referenceId = Unescape.unescape(cols[8]);
                return new RefByEntryKey(headword, posId, reading, referenceId);
            }

            // entry key, pos is written as pos-id
            if (StringUtil.count(text, WORDREF_DELIMITER) == 2) {
                String[] cols = text.split(String.valueOf(WORDREF_DELIMITER), 3);
                String headword = Unescape.unescape(cols[0]);
                short posId = Short.parseShort(cols[1]);
                String reading = Unescape.unescape(cols[2]);
                return new RefByEntryKey(headword, posId, reading);
            }

            // entry key, pos is written as pos-id with reference-id
            if (StringUtil.count(text, WORDREF_DELIMITER) == 3) {
                String[] cols = text.split(String.valueOf(WORDREF_DELIMITER), 4);
                String headword = Unescape.unescape(cols[0]);
                short posId = Short.parseShort(cols[1]);
                String reading = Unescape.unescape(cols[2]);
                String referenceId = Unescape.unescape(cols[3]);
                return new RefByEntryKey(headword, posId, reading, referenceId);
            }

            if (allowHeadword) {
                return new RefByHeadword(Unescape.unescape(text));
            }

            throw new IllegalArgumentException(String.format("invalid word reference: %s", text));
        }
    }
}

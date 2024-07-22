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
import com.worksap.nlp.sudachi.dictionary.Ints;
import com.worksap.nlp.sudachi.dictionary.WordInfoList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Output channel wrapper to write word entries.
 */
public class WordEntryLayout {
    private final StringIndex index;
    private final WordRef.Parser wordRefParser;
    private final Lookup2 lookup;
    private final BufferedChannel buffer;
    private final Ints aSplits = new Ints(16);
    private final Ints bSplits = new Ints(16);
    private final Ints cSplits = new Ints(16);
    private final Ints wordStructure = new Ints(16);
    private final Ints synonymGroups = new Ints(16);

    public static final int MAX_LENGTH = 32 // minimum size
            + Byte.MAX_VALUE * Integer.BYTES * 5 // splits and synonyms
            + (Short.MAX_VALUE + 1) * Character.BYTES; // user data

    public WordEntryLayout(Lookup2 resolver, StringIndex index, WordRef.Parser parser, BufferedChannel buffer) {
        this.lookup = resolver;
        this.index = index;
        this.wordRefParser = parser;
        this.buffer = buffer;
    }

    /**
     * Write word entry into output and returns next offset.
     * 
     * @param entry
     * @return
     * @throws IOException
     */
    public int put(RawWordEntry entry) throws IOException {
        BufWriter buf = this.buffer.writer(MAX_LENGTH);

        buf.putShort(entry.leftId);
        buf.putShort(entry.rightId);
        buf.putShort(entry.cost);
        buf.putShort(entry.posId);
        // 2*4 = 8 bytes

        buf.putInt(index.resolve(entry.headword).encode()); // surfacePtr
        buf.putInt(index.resolve(entry.reading).encode()); // readingPtr
        int normFormPtr = entry.pointer;
        if (entry.normalizedForm != null) {
            normFormPtr = entry.normalizedForm.resolve(lookup);
        }
        int dicFormPtr = entry.pointer;
        if (entry.dictionaryForm != null) {
            dicFormPtr = entry.dictionaryForm.resolve(lookup);
        }
        buf.putInt(normFormPtr); // normalized entry
        buf.putInt(dicFormPtr); // dictionary form
        // 8 + 4*4 = 24 bytes

        // length can't be more than ~4k utf-16 code units so the cast is safe
        short utf8Len = (short) StringUtil.countUtf8Bytes(entry.headword);
        byte cSplitLen = parseWordRefList(entry.cUnitSplitString, "", cSplits);
        byte bSplitLen = parseWordRefList(entry.bUnitSplitString, entry.cUnitSplitString, bSplits);
        byte aSplitLen = parseWordRefList(entry.aUnitSplitString, entry.bUnitSplitString, aSplits);
        byte wordStructureLen = parseWordRefList(entry.wordStructureString, entry.aUnitSplitString, wordStructure);
        byte synonymLen = parseIntList(entry.synonymGroups, synonymGroups);
        int userDataLength = entry.userData.length();
        buf.putShort(utf8Len);
        buf.putByte(cSplitLen);
        buf.putByte(bSplitLen);
        buf.putByte(aSplitLen);
        buf.putByte(wordStructureLen);
        buf.putByte(synonymLen);
        buf.putByte(userDataLength == 0 ? (byte) 0 : (byte) 1);
        // 24 + 8 = 32 bytes

        // putInts is no-op if length <= 0
        buf.putInts(cSplits, cSplitLen);
        buf.putInts(bSplits, bSplitLen);
        buf.putInts(aSplits, aSplitLen);
        buf.putInts(wordStructure, wordStructureLen);
        buf.putInts(synonymGroups, synonymLen);
        if (userDataLength != 0) {
            buf.putShort((short) userDataLength);
            String userData = entry.userData;
            for (int i = 0; i < userDataLength; ++i) {
                buf.putShort((short) userData.charAt(i));
            }
        }

        int position = this.buffer.alignTo(WordInfoList.OFFSET_ALIGNMENT);
        return RawLexicon.pointer(position);
    }

    /** parse int list, i.e. synonym group ids */
    private byte parseIntList(String data, Ints result) {
        if (data == null || data.isEmpty() || "*".equals(data)) {
            result.clear();
            return 0;
        }
        String[] parts = data.split("/");
        if (parts.length > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("reference list contained more than 127 entries: " + data);
        }
        result.clear();
        for (String part : parts) {
            result.append(Integer.parseInt(part));
        }
        return (byte) parts.length;
    }

    /**
     * Parse word ref list, i.e. A/B/C split and word structure.
     * 
     * If it is equivalent to the reference, return -1 without parsing.
     * 
     * @param data
     * @param reference
     * @param result
     * @return
     */
    byte parseWordRefList(String data, String reference, Ints result) {
        if (data == null || data.isEmpty() || "*".equals(data)) {
            result.clear();
            return 0;
        }
        if (data.equals(reference)) {
            result.clear();
            return -1;
        }

        String[] parts = data.split("/");
        if (parts.length > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("reference list contained more than 127 entries: " + data);
        }
        result.clear();
        for (String part : parts) {
            WordRef ref = wordRefParser.parse(part);
            result.append(ref.resolve(lookup));
        }
        return (byte) parts.length;
    }
}

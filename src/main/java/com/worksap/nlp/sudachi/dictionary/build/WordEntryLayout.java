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
import com.worksap.nlp.sudachi.dictionary.Ints;
import com.worksap.nlp.sudachi.dictionary.WordInfoList;

import java.io.IOException;
import java.util.List;

/**
 * Output channel wrapper to write word entries.
 */
public class WordEntryLayout {
    private final StringIndex index;
    private final Lookup2 lookup;
    private final BufferedChannel buffer;
    private final boolean isUser;

    // caches
    private final Ints aSplits = new Ints(16);
    private final Ints bSplits = new Ints(16);
    private final Ints cSplits = new Ints(16);
    private final Ints wordStructure = new Ints(16);

    public static final int MAX_LENGTH = 32 // minimum size
            + Byte.MAX_VALUE * Integer.BYTES * 5 // splits and synonyms
            + (Short.MAX_VALUE + 1) * Character.BYTES; // user data

    public WordEntryLayout(Lookup2 resolver, StringIndex index, BufferedChannel buffer, boolean isUser) {
        this.lookup = resolver;
        this.index = index;
        this.buffer = buffer;
        this.isUser = isUser;
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
        int selfWordRef = isUser ? WordId.make(1, entry.pointer) : entry.pointer;
        int normFormPtr = selfWordRef;
        if (entry.normalizedForm != null) {
            normFormPtr = entry.normalizedForm.resolve(lookup);
        }
        int dicFormPtr = selfWordRef;
        if (entry.dictionaryForm != null) {
            dicFormPtr = entry.dictionaryForm.resolve(lookup);
        }
        buf.putInt(normFormPtr); // normalized entry
        buf.putInt(dicFormPtr); // dictionary form
        // 8 + 4*4 = 24 bytes

        // length can't be more than ~4k utf-16 code units so the cast is safe
        short utf8Len = (short) StringUtil.countUtf8Bytes(entry.headword);
        byte cSplitLen = resolveWordRefList(entry.cUnitSplit, null, cSplits);
        byte bSplitLen = resolveWordRefList(entry.bUnitSplit, entry.cUnitSplit, bSplits);
        byte aSplitLen = resolveWordRefList(entry.aUnitSplit, entry.bUnitSplit, aSplits);
        byte wordStructureLen = resolveWordRefList(entry.wordStructure, entry.aUnitSplit, wordStructure);
        byte synonymLen = (byte) entry.synonymGroups.length();
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
        buf.putInts(entry.synonymGroups, synonymLen);
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

    /**
     * Resolve wordref list (A/B/C split and word structure) using the Lookup and
     * returns its length.
     * 
     * If it is equivalent to the reference, return -1 without parsing.
     * 
     * @param refs
     *            wordref list to resolve.
     * @param reference
     *            wordref list of higher split unit.
     * @param result
     *            Ints to save resolved wordrefs.
     * @return -1 if equals to reference, otherwise length.
     */
    private byte resolveWordRefList(List<WordRef> refs, List<WordRef> reference, Ints result) {
        result.clear();
        if (refs.isEmpty()) {
            return 0;
        }
        if (refs.equals(reference)) {
            // this cannot capture the case different WordRef subclass refers to the same
            // entry.
            // allow this behaviour for the compatibility to
            // {@link RawWordEntry.computeExpectedSize}.
            return -1;
        }
        for (WordRef ref : refs) {
            result.append(ref.resolve(lookup));
        }
        return (byte) refs.size();
    }
}

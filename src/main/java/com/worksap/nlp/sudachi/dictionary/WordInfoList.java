/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;

class WordInfoList {

    private final ByteBuffer bytes;
    private final int offset;
    private final int wordSize;

    WordInfoList(ByteBuffer bytes, int offset, int wordSize) {
        this.bytes = bytes;
        this.offset = offset;
        this.wordSize = wordSize;
    }

    WordInfo getWordInfo(int wordId) {
        ByteBuffer buf = bytes.asReadOnlyBuffer();
        buf.order(bytes.order());
        buf.position(wordIdToOffset(wordId));

        String surface = bufferToString(buf);
        short headwordLength = (short)bufferToStringLength(buf);
        short posId = buf.getShort();
        String normalizedForm = bufferToString(buf);
        if (normalizedForm.isEmpty()) {
            normalizedForm = surface;
        }
        int dictionaryFormWordId = buf.getInt();
        String readingForm = bufferToString(buf);
        if (readingForm.isEmpty()) {
            readingForm = surface;
        }
        int[] aUnitSplit = bufferToIntArray(buf);
        int[] bUnitSplit = bufferToIntArray(buf);
        int[] wordStructure = bufferToIntArray(buf);

        String dictionaryForm = surface;
        if (dictionaryFormWordId >= 0 && dictionaryFormWordId != wordId) {
            WordInfo wi = getWordInfo(dictionaryFormWordId);
            dictionaryForm = wi.getSurface();
        }

        return new WordInfo(surface, headwordLength, posId, normalizedForm,
                            dictionaryFormWordId, dictionaryForm, readingForm,
                            aUnitSplit, bUnitSplit, wordStructure);
    }

    private int wordIdToOffset(int wordId) {
        return bytes.getInt(offset + 4 * wordId);
    }

    private int bufferToStringLength(ByteBuffer buffer) {
        byte length = buffer.get();
        if (length < 0) {
            int high = Byte.toUnsignedInt(length);
            int low = Byte.toUnsignedInt(buffer.get());
            return ((high & 0x7F) << 8) | low;
        }
        return length;
    }

    private String bufferToString(ByteBuffer buffer) {
        int length = bufferToStringLength(buffer);
        char[] str = new char[length];
        for (int i = 0; i < length; i++) {
            str[i] = buffer.getChar();
        }
        return new String(str);
    }

    private int[] bufferToIntArray(ByteBuffer buffer) {
        int length = Byte.toUnsignedInt(buffer.get());
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = buffer.getInt();
        }
        return array;
    }
}

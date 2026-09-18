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

package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;

public class WordInfoList {
    public static final int ALIGNMENT_BITS = 3;
    public static final int OFFSET_ALIGNMENT = 1 << ALIGNMENT_BITS;

    public static int wordId2offset(int wordId) {
        return wordId << ALIGNMENT_BITS;
    }

    public static int offset2wordId(long offset) {
        return (int) (offset >>> ALIGNMENT_BITS);
    }

    private final ByteBuffer bytes;

    WordInfoList(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    public WordInfo getWordInfo(int wordId) {
        return WordInfo.read(bytes, wordId2offset(wordId));
    }

    public int headwordPtr(int wordId) {
        return WordInfo.headwordForm(bytes, wordId2offset(wordId));
    }

    public int readingPtr(int wordId) {
        return WordInfo.readingForm(bytes, wordId2offset(wordId));
    }
}

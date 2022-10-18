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
    private final ByteBuffer bytes;

    WordInfoList(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    public WordInfo getWordInfo(int wordId) {
        int position = wordId * 8;
        return WordInfo.read(bytes, position);
    }

    public int surfacePtr(int wordId) {
        return WordInfo.surfaceForm(bytes, wordId * 8);
    }

    public int readingPtr(int wordId) {
        return WordInfo.readingForm(bytes, wordId * 8);
    }
}

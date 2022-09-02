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
import com.worksap.nlp.sudachi.dictionary.WordInfo;

import java.util.Objects;

@SuppressWarnings("jol")
public class RawWordEntry implements Lookup2.Entry {
    int pointer;
    String headword;
    String reading;
    String normalizedFormRef;
    String dictionaryFormRef;
    WordInfo wordInfo;
    String aUnitSplitString;
    String bUnitSplitString;
    String cUnitSplitString;
    String wordStructureString;
    String synonymGroups;
    String userData;
    String mode;
    short leftId;
    short rightId;
    short cost;
    short surfaceUtf8Length;
    int expectedSize = 0;
    short posId;

    private int countSplits(String data) {
        return StringUtil.count(data, '/');
    }

    public int computeExpectedSize() {
        if (expectedSize != 0) {
            return expectedSize;
        }

        int size = 32;

        size += countSplits(aUnitSplitString) * 4;
        size += countSplits(bUnitSplitString) * 4;
        size += countSplits(cUnitSplitString) * 4;
        size += countSplits(wordStructureString) * 4;
        size += wordInfo.getSynonymGroupIds().length * 4;
        if (userData.length() != 0) {
            size += 2 + userData.length() * 2;
        }

        size = Align.align(size, 8);

        expectedSize = size;
        return size;
    }

    /**
     * Entries with negative leftId are not indexed
     * 
     * @return true if the word should be present in the trie index
     */
    public boolean shouldBeIndexed() {
        return leftId >= 0;
    }

    @Override
    public int pointer() {
        return pointer;
    }

    @Override
    public boolean matches(short posId, String reading) {
        return wordInfo.getPOSId() == posId && Objects.equals(wordInfo.getReadingForm(), reading);
    }

    @Override
    public String headword() {
        return wordInfo.getSurface();
    }
}

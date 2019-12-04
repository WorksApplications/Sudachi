/*
 * Copyright (c) 2019 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;

class UTF8InputText implements InputText {

    private final String originalText;
    private final String modifiedText;
    private final byte[] bytes;
    private final int[] offsets;
    private final int[] byteIndexes;
    private final List<EnumSet<CategoryType>> charCategories;
    private final List<Integer> charCategoryContinuities;
    private final List<Boolean> canBowList;

    UTF8InputText(Grammar grammar, String originalText, String modifiedText, byte[] bytes, int[] offsets,
            int[] byteIndexes, List<EnumSet<CategoryType>> charCategories, List<Integer> charCategoryContinuities,
            List<Boolean> canBowList) {

        this.originalText = originalText;
        this.modifiedText = modifiedText;
        this.bytes = bytes;
        this.offsets = offsets;
        this.byteIndexes = byteIndexes;
        this.charCategories = charCategories;
        this.charCategoryContinuities = charCategoryContinuities;
        this.canBowList = canBowList;
    }

    @Override
    public String getOriginalText() {
        return originalText;
    }

    @Override
    public String getText() {
        return modifiedText;
    }

    byte[] getByteText() {
        return bytes;
    }

    @Override
    public String getSubstring(int begin, int end) {
        if (begin < 0) {
            throw new StringIndexOutOfBoundsException(begin);
        }
        if (end > bytes.length) {
            throw new StringIndexOutOfBoundsException(end);
        }
        if (begin > end) {
            throw new StringIndexOutOfBoundsException(end - begin);
        }

        return modifiedText.substring(byteIndexes[begin], byteIndexes[end]);
    }

    int getOffsetTextLength(int index) {
        return byteIndexes[index];
    }

    @Override
    public int getOriginalIndex(int index) {
        return offsets[index];
    }

    @Override
    public Set<CategoryType> getCharCategoryTypes(int index) {
        return charCategories.get(byteIndexes[index]);
    }

    @Override
    public Set<CategoryType> getCharCategoryTypes(int begin, int end) {
        if (begin + getCharCategoryContinuousLength(begin) < end) {
            return Collections.emptySet();
        }
        int b = byteIndexes[begin];
        int e = byteIndexes[end];
        Set<CategoryType> continuousCategory = charCategories.get(b).clone();
        for (int i = b + 1; i < e; i++) {
            continuousCategory.retainAll(charCategories.get(i));
        }
        return continuousCategory;
    }

    @Override
    public int getCharCategoryContinuousLength(int index) {
        return charCategoryContinuities.get(index);
    }

    @Override
    public int getCodePointsOffsetLength(int index, int codePointOffset) {
        int length = 0;
        int target = byteIndexes[index] + codePointOffset;
        for (int i = index; i < bytes.length; i++) {
            if (byteIndexes[i] >= target) {
                return length;
            }
            length++;
        }
        return length;
    }

    @Override
    public int codePointCount(int begin, int end) {
        return byteIndexes[end] - byteIndexes[begin];
    }

    @Override
    public boolean canBow(int index) {
        return isCharAlignment(index) && canBowList.get(byteIndexes[index]);
    }

    @Override
    public int getWordCandidateLength(int index) {
        for (int i = index + 1; i < bytes.length; i++) {
            if (canBow(i)) {
                return i - index;
            }
        }
        return bytes.length - index;
    }

    private boolean isCharAlignment(int index) {
        return (bytes[index] & 0xC0) != 0x80;
    }
}

/*
 * Copyright (c) 2020 Works Applications Co., Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
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
    private final int[] byteToOriginal;
    private final int[] byteToModified;
    private final List<Integer> modifiedToOriginal;
    private final List<EnumSet<CategoryType>> charCategories;
    private final List<Integer> charCategoryContinuities;
    private final List<Boolean> canBowList;

    UTF8InputText(Grammar grammar, String originalText, String modifiedText, byte[] bytes, int[] byteToOriginal,
            int[] byteToModified, List<Integer> modifiedToOriginal, List<EnumSet<CategoryType>> charCategories,
            List<Integer> charCategoryContinuities, List<Boolean> canBowList) {

        this.originalText = originalText;
        this.modifiedText = modifiedText;
        this.bytes = bytes;
        this.byteToOriginal = byteToOriginal;
        this.byteToModified = byteToModified;
        this.modifiedToOriginal = modifiedToOriginal;
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

        return modifiedText.substring(byteToModified[begin], byteToModified[end]);
    }

    @Override
    public UTF8InputText slice(int begin, int end) {
        if (begin < 0) {
            throw new StringIndexOutOfBoundsException(begin);
        }
        if (end > modifiedText.length()) {
            throw new StringIndexOutOfBoundsException(end);
        }
        if (begin > end) {
            throw new StringIndexOutOfBoundsException(end - begin);
        }

        int byteBegin = getCodePointsOffsetLength(0, begin);
        int length = getCodePointsOffsetLength(byteBegin, end - begin);
        int byteEnd = byteBegin + length;

        String originalText = this.originalText.substring(byteToOriginal[byteBegin], byteToOriginal[byteEnd]);
        String modifiedText = this.modifiedText.substring(begin, end);
        byte[] bytes = Arrays.copyOfRange(this.bytes, byteBegin, byteEnd);

        int[] byteToOriginal = new int[length + 1];
        for (int i = 0; i < length + 1; i++) {
            byteToOriginal[i] = this.byteToOriginal[byteBegin + i] - this.byteToOriginal[byteBegin];
        }
        int[] byteToModified = new int[length + 1];
        for (int i = 0; i < length + 1; i++) {
            byteToModified[i] = this.byteToModified[byteBegin + i] - begin;
        }
        List<Integer> modifiedToOriginal = new ArrayList<>();
        for (int i = 0; i < end + 1; i++) {
            modifiedToOriginal.add(this.modifiedToOriginal.get(i) - this.modifiedToOriginal.get(begin));
        }

        List<EnumSet<CategoryType>> charCategories = this.charCategories.subList(begin, end);

        List<Integer> charCategoryContinuities = this.charCategoryContinuities.subList(byteBegin, byteEnd);
        if (charCategoryContinuities.get(length - 1) != 1) {
            int i = length - 1;
            int len = 1;
            while (i >= 0 && charCategoryContinuities.get(i) != 1) {
                charCategoryContinuities.set(i--, len++);
            }
        }

        List<Boolean> canBowList = this.canBowList.subList(begin, end);

        return new UTF8InputText(null, originalText, modifiedText, bytes, byteToOriginal, byteToModified,
                modifiedToOriginal, charCategories, charCategoryContinuities, canBowList);
    }

    int getOffsetTextLength(int index) {
        return byteToModified[index];
    }

    @Override
    public int getOriginalIndex(int index) {
        return byteToOriginal[index];
    }

    @Override
    public Set<CategoryType> getCharCategoryTypes(int index) {
        return charCategories.get(byteToModified[index]);
    }

    @Override
    public Set<CategoryType> getCharCategoryTypes(int begin, int end) {
        if (begin + getCharCategoryContinuousLength(begin) < end) {
            return Collections.emptySet();
        }
        int b = byteToModified[begin];
        int e = byteToModified[end];
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
        int target = byteToModified[index] + codePointOffset;
        for (int i = index; i < bytes.length; i++) {
            if (byteToModified[i] >= target) {
                return length;
            }
            length++;
        }
        return length;
    }

    @Override
    public int codePointCount(int begin, int end) {
        return byteToModified[end] - byteToModified[begin];
    }

    @Override
    public boolean canBow(int index) {
        return isCharAlignment(index) && canBowList.get(byteToModified[index]);
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

    @Override
    public int getNextInOriginal(int index) {
        int o = modifiedToOriginal.get(index + 1);
        while (index + 1 < modifiedText.length() + 1 && modifiedToOriginal.get(index + 1) == o) {
            index++;
        }
        return index;
    }

    int textIndexToOriginalTextIndex(int index) {
        return modifiedToOriginal.get(index);
    }
}

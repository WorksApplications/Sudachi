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

package com.worksap.nlp.sudachi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.CategoryTypeSet;
import com.worksap.nlp.sudachi.dictionary.Grammar;

class UTF8InputTextBuilder implements InputTextBuilder<byte[]> {

    private final String originalText;
    private StringBuilder modifiedText;
    private List<Integer> textOffsets;
    
    private final Grammar grammar;
    
    public UTF8InputTextBuilder(String text, Grammar grammar) {
        this.grammar = grammar;
        
        originalText = text;
        modifiedText = new StringBuilder(text);
        textOffsets = new ArrayList<>(modifiedText.length() + 1);
        for (int i = 0, j = 0; i < originalText.length(); i++) {
            if (!Character.isLowSurrogate(originalText.charAt(i))) {
                j = i;
            }
            textOffsets.add(j);
        }
        textOffsets.add(originalText.length());
    }
    
    @Override
    public void replace(int begin, int end, String str) {
        if (begin < 0)
            throw new StringIndexOutOfBoundsException(begin);
        if (begin > modifiedText.length())
            throw new StringIndexOutOfBoundsException("begin > length()");
        if (begin > end)
            throw new StringIndexOutOfBoundsException("begin > end");
        if (begin == end)
            throw new IllegalArgumentException("begin == end");
        
        if (end > modifiedText.length()) {
            end = modifiedText.length();
        }
        
        modifiedText.replace(begin, end, str);

        int offset = textOffsets.get(begin);
        int length = str.length();
        if (end - begin > length) {
            textOffsets.subList(begin + length, end).clear();
        }
        for (int i = 0; i < length; i++) {
            if (begin + i < end) {
                textOffsets.set(begin + i, offset);
            } else {
                textOffsets.add(begin + i, offset);
            }
        }
    }
    
    @Override
    public String getOriginalText() {
        return originalText;
    }
    
    @Override
    public String getText() {
        return modifiedText.toString();
    }
    
    @Override
    public UTF8InputText build() {
        String modifiedStringText = getText();
        byte[] byteText = modifiedStringText.getBytes(StandardCharsets.UTF_8);

        int length = byteText.length;
        int[] byteIndexes = new int[length + 1];
        int[] offsets = new int[length + 1];
        for (int i = 0, j = 0; i < modifiedText.length(); i++) {
            if (Character.isLowSurrogate(modifiedText.charAt(i))) {
                continue;
            }
            for (int k = 0; k < utf8ByteLength(modifiedText.codePointAt(i)); k++) {
                byteIndexes[j] = i;
                offsets[j] = textOffsets.get(i);
                j++;
            }
        }
        byteIndexes[length] = modifiedStringText.length();
        offsets[length] = textOffsets.get(textOffsets.size() - 1);

        List<Set<CategoryType>> charCategories
            = getCharCategoryTypes(modifiedStringText);
        List<Integer> charCategoryContinuities
            = getCharCategoryContinuities(modifiedStringText, length, charCategories);
        List<Boolean> canBowList = buildCanBowList(modifiedStringText, charCategories);

        return new UTF8InputText(grammar, originalText, modifiedStringText, byteText,
                                 offsets,
                                 byteIndexes,
            Collections.unmodifiableList(charCategories),
            Collections.unmodifiableList(charCategoryContinuities),
            Collections.unmodifiableList(canBowList));
    }

    private List<Set<CategoryType>> getCharCategoryTypes(String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        List<Set<CategoryType>> charCategoryTypes = new ArrayList<>(text.length());
        Set<CategoryType> types = null;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLowSurrogate(text.charAt(i)) && types != null) {
                charCategoryTypes.add(types);
                continue;
            }
            types = grammar.getCharacterCategory()
                .getCategoryTypes(text.codePointAt(i));
            charCategoryTypes.add(types);
        }
        return charCategoryTypes;
    }
    
    private List<Integer> getCharCategoryContinuities(String text,
                                                      int byteLength,
                                                      List<Set<CategoryType>> charCategories) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> charCategoryContinuities = new ArrayList<>(byteLength);
        for (int i = 0; i < charCategories.size(); ) {
            int next = i + getCharCategoryContinuousLength(charCategories, i);
            int length = 0;
            for (int j = i; j < next; j = text.offsetByCodePoints(j, 1)) {
                length += utf8ByteLength(text.codePointAt(j));
            }
            for (int k = length; k > 0; k--) {
                charCategoryContinuities.add(k);
            }
            i = next;
        }
        return charCategoryContinuities;
    }
    
    private int getCharCategoryContinuousLength(List<Set<CategoryType>> charCategories, int offset) {
        int length;
        Set<CategoryType> continuousCategory
            = ((CategoryTypeSet)charCategories.get(offset)).clone();
        for (length = 1; length < charCategories.size() - offset; length++) {
            continuousCategory.retainAll(charCategories.get(offset + length));
            if (continuousCategory.isEmpty()) {
                return length;
            }
        }
        return length;
    }
    
    private List<Boolean> buildCanBowList(String text,
                                          List<Set<CategoryType>> charCategories) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        List<Boolean> canBowList = new ArrayList<>(text.length());
        for (int i = 0; i < charCategories.size(); i++) {
            if (i == 0) {
                canBowList.add(true);
                continue;
            }

            if (Character.isLowSurrogate(text.charAt(i))) {
                canBowList.add(false);
                continue;
            }

            Set<CategoryType> types = ((CategoryTypeSet)charCategories.get(i)).clone();
            if (types.contains(CategoryType.ALPHA) ||
                types.contains(CategoryType.GREEK) ||
                types.contains(CategoryType.CYRILLIC)) {
                types.retainAll(charCategories.get(i - 1));
                canBowList.add(types.isEmpty());
                continue;
            }

            canBowList.add(true);
        }

        return canBowList;
    }

    private int utf8ByteLength(int cp) {
        if (cp < 0) {
            return 0;
        } else if (cp <= 0x7F) {
            return 1;
        } else if (cp <= 0x7FF) {
            return 2;
        } else if (cp <= 0xFFFF) {
            return 3;
        } else if (cp <= 0x10FFFF) {
            return 4;
        } else {
            return 0;
        }
    }
}

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

import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.CategoryTypeSet;

class MockInputText implements InputText<String> {

    String text;
    CategoryTypeSet[] types;

    MockInputText(String text) {
        this.text = text;
        types = new CategoryTypeSet[text.length()];
        for (int i = 0; i < text.length(); i++) {
            types[i] = new CategoryTypeSet();
        }
    }

    void setCategoryType(int begin, int end, CategoryType... types) {
        for (int i = begin; i < end; i++) {
            for (CategoryType type : types) {
                this.types[i].add(type);
            }
        }
    }

    @Override
    public String getText() { return text; }

    @Override
    public String getOriginalText() { return text; }
    
    @Override
    public String getSubstring(int begin, int end) { return text.substring(begin, end); }
    
    @Override
    public int getOriginalIndex(int index) { return index; }
    
    @Override
    public Set<CategoryType> getCharCategoryTypes(int index) {
        return types[index];
    }

    @Override
    public Set<CategoryType> getCharCategoryTypes(int begin, int end) {
        Set<CategoryType> continuousCategory = types[begin].clone();
        for (int i = text.offsetByCodePoints(begin, 1); i < end;
             i = text.offsetByCodePoints(i, 1)) {
            continuousCategory.retainAll(types[i]);
        }
        return continuousCategory;
    }
    
    @Override
    public int getCharCategoryContinuousLength(int index) {
        Set<CategoryType> continuousCategory = types[index].clone();
        for (int i = text.offsetByCodePoints(index, 1); i < text.length();
             i = text.offsetByCodePoints(i, 1)) {
            continuousCategory.retainAll(types[i]);
            if (continuousCategory.isEmpty()) {
                return i - index;
            }
        }
        return text.length() - index;
    }
    
    @Override
    public int getCodePointsOffsetLength(int index, int codePointOffset) {
        return text.offsetByCodePoints(index, codePointOffset) - index;
    }

    @Override
    public boolean canBow(int index) { return true; }
}

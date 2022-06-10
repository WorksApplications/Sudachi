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

package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.worksap.nlp.sudachi.dictionary.CharacterCategory;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.POS;

public class MockGrammar implements Grammar {

    Map<Short, Map<Short, Short>> matrix = new HashMap<>();
    private final CharacterCategory category = defaultCharCategory();

    @Override
    public int getPartOfSpeechSize() {
        return 0;
    }

    @Override
    public POS getPartOfSpeechString(short posId) {
        return null;
    }

    @Override
    public short getPartOfSpeechId(List<String> pos) {
        return 0;
    }

    @Override
    public short getConnectCost(short left, short right) {
        return matrix.getOrDefault(left, Collections.emptyMap()).getOrDefault(right, (short) 0);
    }

    @Override
    public void setConnectCost(short left, short right, short cost) {
        matrix.computeIfAbsent(left, k -> new HashMap<>()).put(right, cost);
    }

    @Override
    public short[] getBOSParameter() {
        return null;
    }

    @Override
    public short[] getEOSParameter() {
        return null;
    }

    @Override
    public CharacterCategory getCharacterCategory() {
        return category;
    }

    public static CharacterCategory defaultCharCategory() {
        try {
            return CharacterCategory.load(PathAnchor.classpath().resource("char.def"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setCharacterCategory(CharacterCategory charCategory) {
    }
}

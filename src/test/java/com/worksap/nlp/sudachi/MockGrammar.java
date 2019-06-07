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

import java.io.IOException;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.CharacterCategory;
import com.worksap.nlp.sudachi.dictionary.Grammar;

public class MockGrammar implements Grammar {
    @Override
    public int getPartOfSpeechSize() {
        return 0;
    }

    @Override
    public List<String> getPartOfSpeechString(short posId) {
        return null;
    }

    @Override
    public short getPartOfSpeechId(List<String> pos) {
        return 0;
    }

    @Override
    public short getConnectCost(short leftId, short rightId) {
        return 0;
    }

    @Override
    public void setConnectCost(short leftId, short rightId, short cost) {
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
        CharacterCategory charCategory = new CharacterCategory();
        try {
            charCategory.readCharacterDefinition(MockGrammar.class.getResource("/char.def").getPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return charCategory;
    }

    @Override
    public void setCharacterCategory(CharacterCategory charCategory) {
    }
}

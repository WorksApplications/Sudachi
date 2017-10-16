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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.CharacterCategory;
import com.worksap.nlp.sudachi.dictionary.Grammar;

public class UTF8InputTextTest {
    
    // mixed full-width, half-width, accented
    // U+2123D '𡈽' uses surrogate pair
    static final String TEXT = "âｂC1あ234漢字𡈽アｺﾞ";
    byte[] bytes = {
        (byte)0xC3, (byte)0xA2, (byte)0xEF, (byte)0xBD, (byte)0x82, (byte)0x43,
        (byte)0x31, (byte)0xE3, (byte)0x81, (byte)0x82, (byte)0x32, (byte)0x33,
        (byte)0x34, (byte)0xE6, (byte)0xBC, (byte)0xA2, (byte)0xE5, (byte)0xAD,
        (byte)0x97, (byte)0xF0, (byte)0xA1, (byte)0x88, (byte)0xBD, (byte)0xE3,
        (byte)0x82, (byte)0xA2, (byte)0xEF, (byte)0xBD, (byte)0xBA, (byte)0xEF,
        (byte)0xBE, (byte)0x9E
    };
    UTF8InputText input;
    UTF8InputTextBuilder builder;
    MockGrammar grammar;
    
    @Before
    public void setUp() {
        grammar = new MockGrammar();
        CharacterCategory charCategory = new CharacterCategory();
        try {
            charCategory.readCharacterDefinition(UTF8InputTextTest.class.getClassLoader()
                .getResource("char.def").getPath());
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        grammar.setCharacterCategory(charCategory);
        
        builder = new UTF8InputTextBuilder(TEXT, grammar);
    }
    
    @Test
    public void getOriginalText() {
        assertThat(builder.getOriginalText(), is(TEXT));
        assertThat(builder.getText(), is(TEXT));
        input = builder.build();
        assertThat(input.getOriginalText(), is(TEXT));
        assertThat(input.getText(), is(TEXT));
    }
    
    @Test
    public void getByteText() {
        input = builder.build();
        assertThat(input.getByteText().length, is(32));
        assertArrayEquals(bytes, input.getByteText());
    }
    
    @Test
    public void getOriginalIndex() {
        input = builder.build();
        assertThat(input.getOriginalIndex(0), is(0));
        assertThat(input.getOriginalIndex(1), is(0));
        assertThat(input.getOriginalIndex(2), is(1));
        assertThat(input.getOriginalIndex(4), is(1));
        assertThat(input.getOriginalIndex(6), is(3));
        assertThat(input.getOriginalIndex(7), is(4));
        assertThat(input.getOriginalIndex(10), is(5));
        assertThat(input.getOriginalIndex(18), is(9));
        assertThat(input.getOriginalIndex(19), is(10));
        assertThat(input.getOriginalIndex(22), is(10));
        assertThat(input.getOriginalIndex(23), is(12));
        assertThat(input.getOriginalIndex(28), is(13));
        assertThat(input.getOriginalIndex(31), is(14));
    }
    
    @Test
    public void getCharCategoryTypes() {
        input = builder.build();
        assertThat(input.getCharCategoryTypes(0), hasItem(CategoryType.ALPHA));
        assertThat(input.getCharCategoryTypes(2), hasItem(CategoryType.ALPHA));
        assertThat(input.getCharCategoryTypes(5), hasItem(CategoryType.ALPHA));
        assertThat(input.getCharCategoryTypes(6), hasItem(CategoryType.NUMERIC));
        assertThat(input.getCharCategoryTypes(7), hasItem(CategoryType.HIRAGANA));
        assertThat(input.getCharCategoryTypes(9), hasItem(CategoryType.HIRAGANA));
        assertThat(input.getCharCategoryTypes(10), hasItem(CategoryType.NUMERIC));
        assertThat(input.getCharCategoryTypes(13), hasItem(CategoryType.KANJI));
        assertThat(input.getCharCategoryTypes(18), hasItem(CategoryType.KANJI));
        assertThat(input.getCharCategoryTypes(19), hasItem(CategoryType.DEFAULT));
        assertThat(input.getCharCategoryTypes(22), hasItem(CategoryType.DEFAULT));
        assertThat(input.getCharCategoryTypes(23), hasItem(CategoryType.KATAKANA));
        assertThat(input.getCharCategoryTypes(26), hasItem(CategoryType.KATAKANA));
        assertThat(input.getCharCategoryTypes(31), hasItem(CategoryType.KATAKANA));
    }
    
    @Test
    public void getCharCategoryContinuousLength() {
        input = builder.build();
        assertThat(input.getCharCategoryContinuousLength(0), is(6));
        assertThat(input.getCharCategoryContinuousLength(1), is(5));
        assertThat(input.getCharCategoryContinuousLength(2), is(4));
        assertThat(input.getCharCategoryContinuousLength(5), is(1));
        assertThat(input.getCharCategoryContinuousLength(6), is(1));
        assertThat(input.getCharCategoryContinuousLength(7), is(3));
        assertThat(input.getCharCategoryContinuousLength(10), is(3));
        assertThat(input.getCharCategoryContinuousLength(11), is(2));
        assertThat(input.getCharCategoryContinuousLength(12), is(1));
        assertThat(input.getCharCategoryContinuousLength(19), is(4));
        assertThat(input.getCharCategoryContinuousLength(22), is(1));
        assertThat(input.getCharCategoryContinuousLength(23), is(9));
        assertThat(input.getCharCategoryContinuousLength(26), is(6));
        assertThat(input.getCharCategoryContinuousLength(31), is(1));
    }
    
    @Test
    public void replaceWithSameLength() {
        builder.replace(8, 10, "ああ");
        assertThat(builder.getOriginalText(), is(TEXT));
        assertThat(builder.getText(), is("âｂC1あ234ああ𡈽アｺﾞ"));
        input = builder.build();
        assertThat(input.getOriginalText(), is(TEXT));
        assertThat(input.getText(), is("âｂC1あ234ああ𡈽アｺﾞ"));
        assertThat(input.getByteText().length, is(32));
        assertThat(input.getOriginalIndex(0), is(0));
        assertThat(input.getOriginalIndex(12), is(7));
        assertThat(input.getOriginalIndex(13), is(8));
        assertThat(input.getOriginalIndex(15), is(8));
        assertThat(input.getOriginalIndex(16), is(8));
        assertThat(input.getOriginalIndex(18), is(8));
        assertThat(input.getOriginalIndex(19), is(10));
        assertThat(input.getOriginalIndex(22), is(10));
        assertThat(input.getOriginalIndex(31), is(14));
    }

    @Test
    public void replaceWithDeletion() {
        builder.replace(8, 10, "あ");
        assertThat(builder.getOriginalText(), is(TEXT));
        assertThat(builder.getText(), is("âｂC1あ234あ𡈽アｺﾞ"));
        input = builder.build();
        assertThat(input.getOriginalText(), is(TEXT));
        assertThat(input.getText(), is("âｂC1あ234あ𡈽アｺﾞ"));
        assertThat(input.getByteText().length, is(29));
        assertThat(input.getOriginalIndex(0), is(0));
        assertThat(input.getOriginalIndex(12), is(7));
        assertThat(input.getOriginalIndex(13), is(8));
        assertThat(input.getOriginalIndex(15), is(8));
        assertThat(input.getOriginalIndex(16), is(10));
        assertThat(input.getOriginalIndex(19), is(10));
        assertThat(input.getOriginalIndex(28), is(14));
    }

    @Test
    public void replaceWithInsertion() {
        builder.replace(8, 10, "あああ");
        assertThat(builder.getOriginalText(), is(TEXT));
        assertThat(builder.getText(), is("âｂC1あ234あああ𡈽アｺﾞ"));
        input = builder.build();
        assertThat(input.getOriginalText(), is(TEXT));
        assertThat(input.getText(), is("âｂC1あ234あああ𡈽アｺﾞ"));
        assertThat(input.getByteText().length, is(35));
        assertThat(input.getOriginalIndex(0), is(0));
        assertThat(input.getOriginalIndex(12), is(7));
        assertThat(input.getOriginalIndex(13), is(8));
        assertThat(input.getOriginalIndex(21), is(8));
        assertThat(input.getOriginalIndex(22), is(10));
        assertThat(input.getOriginalIndex(25), is(10));
        assertThat(input.getOriginalIndex(34), is(14));
    }
    
    @Test
    public void replaceMultiTimes() {
        builder.replace(0, 1, "a");
        builder.replace(1, 2, "b");
        builder.replace(2, 3, "c");
        builder.replace(10, 12, "土");
        builder.replace(12, 14, "ゴ");
        input = builder.build();
        assertThat(input.getOriginalText(), is(TEXT));
        assertThat(input.getText(), is("abc1あ234漢字土アゴ"));
        assertThat(input.getByteText().length, is(25));
        assertThat(input.getOriginalIndex(0), is(0));
        assertThat(input.getOriginalIndex(1), is(1));
        assertThat(input.getOriginalIndex(2), is(2));
        assertThat(input.getOriginalIndex(7), is(5));
        assertThat(input.getOriginalIndex(8), is(6));
        assertThat(input.getOriginalIndex(9), is(7));
        assertThat(input.getOriginalIndex(15), is(9));
        assertThat(input.getOriginalIndex(16), is(10));
        assertThat(input.getOriginalIndex(18), is(10));
        assertThat(input.getOriginalIndex(19), is(12));
        assertThat(input.getOriginalIndex(21), is(12));
        assertThat(input.getOriginalIndex(22), is(13));
        assertThat(input.getOriginalIndex(24), is(13));
    }
    
    @Test
    public void getByteLengthByCodePoints() {
        input = builder.build();
        assertThat(input.getCodePointsOffsetLength(0, 1), is(2));
        assertThat(input.getCodePointsOffsetLength(0, 4), is(7));
        assertThat(input.getCodePointsOffsetLength(10, 1), is(1));
        assertThat(input.getCodePointsOffsetLength(11, 1), is(1));
        assertThat(input.getCodePointsOffsetLength(12, 1), is(1));
        assertThat(input.getCodePointsOffsetLength(13, 2), is(6));
        assertThat(input.getCodePointsOffsetLength(19, 1), is(4));
        assertThat(input.getCodePointsOffsetLength(23, 3), is(9));
    }
    
    @Test
    public void canBow() {
        input = builder.build();
        assertTrue(input.canBow(0)); // â
        assertFalse(input.canBow(1));
        assertFalse(input.canBow(2)); // ｂ
        assertFalse(input.canBow(3));
        assertFalse(input.canBow(4));
        assertFalse(input.canBow(5)); // C
        assertTrue(input.canBow(6));  // 1
        assertTrue(input.canBow(7));  // あ

        assertTrue(input.canBow(19)); // 𡈽
        assertFalse(input.canBow(20));
        assertFalse(input.canBow(21));
        assertFalse(input.canBow(22));
        assertTrue(input.canBow(23)); // ア
    }

    class MockGrammar implements Grammar {
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
        public void setConnectCost(short leftId, short rightId, short cost) {}
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
                charCategory.readCharacterDefinition(UTF8InputTextTest.class.getClassLoader()
                    .getResource("char.def").getPath());
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            return charCategory;
        }
        @Override
        public void setCharacterCategory(CharacterCategory charCategory) {
        }
    }
}

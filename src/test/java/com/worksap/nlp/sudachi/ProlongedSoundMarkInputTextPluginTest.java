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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.worksap.nlp.sudachi.dictionary.CharacterCategory;
import com.worksap.nlp.sudachi.dictionary.Grammar;

public class ProlongedSoundMarkInputTextPluginTest {
    
    static final String ORIGINAL_TEXT = "ゴーーーーール";
    static final String NORMALIZED_TEXT = "ゴール";
    UTF8InputTextBuilder builder;
    UTF8InputText text;
    ProlongedSoundMarkTextPlugin plugin;
    
    @Before
    public void setUp() {
        builder = new UTF8InputTextBuilder(ORIGINAL_TEXT, new MockGrammar());
        plugin = new ProlongedSoundMarkTextPlugin();
        try {
            plugin.setUp();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void beforeRewrite() {
        assertThat(builder.getOriginalText(), is(ORIGINAL_TEXT));
        assertThat(builder.getText(), is(ORIGINAL_TEXT));
        text = builder.build();
        assertThat(text.getOriginalText(), is(ORIGINAL_TEXT));
        assertThat(text.getText(), is(ORIGINAL_TEXT));
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(21));
        assertArrayEquals(
            new byte[] {
                    (byte)0xE3, (byte)0x82, (byte)0xB4, (byte)0xE3,
                    (byte)0x83, (byte)0xBC, (byte)0xE3, (byte)0x83,
                    (byte)0xBC, (byte)0xE3, (byte)0x83, (byte)0xBC,
                    (byte)0xE3, (byte)0x83, (byte)0xBC, (byte)0xE3,
                    (byte)0x83, (byte)0xBC, (byte)0xE3, (byte)0x83,
                    (byte)0xAB
            }, bytes
        );
        assertThat(text.getOriginalIndex(0), is(0));
        assertThat(text.getOriginalIndex(3), is(1));
        assertThat(text.getOriginalIndex(6), is(2));
        assertThat(text.getOriginalIndex(9), is(3));
        assertThat(text.getOriginalIndex(12), is(4));
        assertThat(text.getOriginalIndex(15), is(5));
        assertThat(text.getOriginalIndex(18), is(6));
        assertThat(text.getOriginalIndex(21), is(7));
    }

    @Test
    public void afterRewrite() {
        assertThat(builder.getOriginalText(), is(ORIGINAL_TEXT));
        assertThat(builder.getText(), is(ORIGINAL_TEXT));
        plugin.rewrite(builder);
        text = builder.build();
        assertThat(text.getOriginalText(), is(ORIGINAL_TEXT));
        assertThat(text.getText(), is(NORMALIZED_TEXT));
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(9));
        assertArrayEquals(
                new byte[] {
                        (byte)0xE3, (byte)0x82, (byte)0xB4, (byte)0xE3,
                        (byte)0x83, (byte)0xBC, (byte)0xE3, (byte)0x83,
                        (byte)0xAB
                }, bytes
        );
        assertThat(text.getOriginalIndex(0), is(0));
        assertThat(text.getOriginalIndex(3), is(1));
        assertThat(text.getOriginalIndex(6), is(6));
        assertThat(text.getOriginalIndex(9), is(7));
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
                charCategory.readCharacterDefinition(DefaultInputTextPluginTest.class.getClassLoader()
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

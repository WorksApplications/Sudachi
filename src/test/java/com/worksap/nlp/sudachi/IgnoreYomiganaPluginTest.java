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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.List;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.worksap.nlp.sudachi.dictionary.CharacterCategory;
import com.worksap.nlp.sudachi.dictionary.Grammar;

public class IgnoreYomiganaPluginTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    UTF8InputTextBuilder builder;
    UTF8InputText text;
    IgnoreYomiganaPlugin plugin;

    @Before
    public void setUp() throws IOException {
        Utils.copyResource(temporaryFolder.getRoot().toPath(), "/system.dic", "/user.dic", "/char.def", "/unk.def");
        String path = temporaryFolder.getRoot().getPath();
        String jsonString = Utils.readAllResource("/sudachi.json");
        Dictionary dict = new DictionaryFactory().create(path, jsonString);
        plugin = new IgnoreYomiganaPlugin();

        Settings settings = Settings.parseSettings(null, jsonString);
        List<JsonObject> list = settings.getList("inputTextPlugin", JsonObject.class);
        for (JsonObject p : list) {
            if (p.getString("class").equals("com.worksap.nlp.sudachi.IgnoreYomiganaPlugin")) {
                plugin.setSettings(new Settings(p, null));
                break;
            }
        }

        try {
            plugin.setUp(((JapaneseDictionary) dict).grammar);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void ignoreYomiganaAtMiddle() {
        final String ORIGINAL_TEXT = "徳島（とくしま）に行く";
        final String NORMALIZED_TEXT = "徳島に行く";
        builder = new UTF8InputTextBuilder(ORIGINAL_TEXT, new MockGrammar());
        plugin.rewrite(builder);
        text = builder.build();

        assertThat(text.getOriginalText(), is(ORIGINAL_TEXT));
        assertThat(text.getText(), is(NORMALIZED_TEXT));
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(15));
        assertArrayEquals(new byte[] { (byte) 0xE5, (byte) 0xBE, (byte) 0xB3, (byte) 0xE5, (byte) 0xB3, (byte) 0xB6,
                (byte) 0xE3, (byte) 0x81, (byte) 0xAB, (byte) 0xE8, (byte) 0xA1, (byte) 0x8C, (byte) 0xE3, (byte) 0x81,
                (byte) 0x8F }, bytes);
        assertThat(text.getOriginalIndex(0), is(0));
        assertThat(text.getOriginalIndex(3), is(1));
        assertThat(text.getOriginalIndex(6), is(8));
        assertThat(text.getOriginalIndex(9), is(9));
        assertThat(text.getOriginalIndex(12), is(10));
    }

    @Test
    public void ignoreYomiganaAtMiddleAtEnd() {
        final String ORIGINAL_TEXT = "徳島（とくしま）";
        final String NORMALIZED_TEXT = "徳島";
        builder = new UTF8InputTextBuilder(ORIGINAL_TEXT, new MockGrammar());
        plugin.rewrite(builder);
        text = builder.build();

        assertThat(text.getOriginalText(), is(ORIGINAL_TEXT));
        assertThat(text.getText(), is(NORMALIZED_TEXT));
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(6));
        assertArrayEquals(new byte[] { (byte) 0xE5, (byte) 0xBE, (byte) 0xB3, (byte) 0xE5, (byte) 0xB3, (byte) 0xB6 },
                bytes);
        assertThat(text.getOriginalIndex(0), is(0));
        assertThat(text.getOriginalIndex(3), is(1));
    }

    @Test
    public void ignoreYomiganaMultipleTimes() {
        final String ORIGINAL_TEXT = "徳島（とくしま）に行（い）く";
        final String NORMALIZED_TEXT = "徳島に行く";
        builder = new UTF8InputTextBuilder(ORIGINAL_TEXT, new MockGrammar());
        plugin.rewrite(builder);
        text = builder.build();

        assertThat(text.getOriginalText(), is(ORIGINAL_TEXT));
        assertThat(text.getText(), is(NORMALIZED_TEXT));
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(15));
        assertArrayEquals(new byte[] { (byte) 0xE5, (byte) 0xBE, (byte) 0xB3, (byte) 0xE5, (byte) 0xB3, (byte) 0xB6,
                (byte) 0xE3, (byte) 0x81, (byte) 0xAB, (byte) 0xE8, (byte) 0xA1, (byte) 0x8C, (byte) 0xE3, (byte) 0x81,
                (byte) 0x8F }, bytes);
        assertThat(text.getOriginalIndex(0), is(0));
        assertThat(text.getOriginalIndex(3), is(1));
        assertThat(text.getOriginalIndex(6), is(8));
        assertThat(text.getOriginalIndex(9), is(9));
        assertThat(text.getOriginalIndex(12), is(13));
    }

    @Test
    public void ignoreYomiganaMultipleBracketTypes() {
        final String ORIGINAL_TEXT = "徳島(とくしま)に行（い）く";
        final String NORMALIZED_TEXT = "徳島に行く";
        builder = new UTF8InputTextBuilder(ORIGINAL_TEXT, new MockGrammar());
        plugin.rewrite(builder);
        text = builder.build();

        assertThat(text.getOriginalText(), is(ORIGINAL_TEXT));
        assertThat(text.getText(), is(NORMALIZED_TEXT));
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(15));
        assertArrayEquals(new byte[] { (byte) 0xE5, (byte) 0xBE, (byte) 0xB3, (byte) 0xE5, (byte) 0xB3, (byte) 0xB6,
                (byte) 0xE3, (byte) 0x81, (byte) 0xAB, (byte) 0xE8, (byte) 0xA1, (byte) 0x8C, (byte) 0xE3, (byte) 0x81,
                (byte) 0x8F }, bytes);
        assertThat(text.getOriginalIndex(0), is(0));
        assertThat(text.getOriginalIndex(3), is(1));
        assertThat(text.getOriginalIndex(6), is(8));
        assertThat(text.getOriginalIndex(9), is(9));
        assertThat(text.getOriginalIndex(12), is(13));
    }

    @Test
    public void doNotIgnoreIfNotYomigana() {
        final String ORIGINAL_TEXT = "徳島に（よく）行く";
        final String NORMALIZED_TEXT = "徳島に（よく）行く";
        builder = new UTF8InputTextBuilder(ORIGINAL_TEXT, new MockGrammar());
        plugin.rewrite(builder);
        text = builder.build();

        assertThat(text.getOriginalText(), is(ORIGINAL_TEXT));
        assertThat(text.getText(), is(NORMALIZED_TEXT));
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(27));
        assertArrayEquals(new byte[] { (byte) 0xE5, (byte) 0xBE, (byte) 0xB3, (byte) 0xE5, (byte) 0xB3, (byte) 0xB6,
                (byte) 0xE3, (byte) 0x81, (byte) 0xAB, (byte) 0xEF, (byte) 0xBC, (byte) 0x88, (byte) 0xE3, (byte) 0x82,
                (byte) 0x88, (byte) 0xE3, (byte) 0x81, (byte) 0x8F, (byte) 0xEF, (byte) 0xBC, (byte) 0x89, (byte) 0xE8,
                (byte) 0xA1, (byte) 0x8C, (byte) 0xE3, (byte) 0x81, (byte) 0x8F }, bytes);
        assertThat(text.getOriginalIndex(0), is(0));
        assertThat(text.getOriginalIndex(3), is(1));
        assertThat(text.getOriginalIndex(6), is(2));
        assertThat(text.getOriginalIndex(9), is(3));
        assertThat(text.getOriginalIndex(12), is(4));
        assertThat(text.getOriginalIndex(15), is(5));
        assertThat(text.getOriginalIndex(18), is(6));
        assertThat(text.getOriginalIndex(21), is(7));
        assertThat(text.getOriginalIndex(24), is(8));
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
                charCategory.readCharacterDefinition(
                        DefaultInputTextPluginTest.class.getClassLoader().getResource("char.def").getPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return charCategory;
        }

        @Override
        public void setCharacterCategory(CharacterCategory charCategory) {
        }
    }
}

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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import javax.json.Json;

import org.junit.Before;
import org.junit.Test;

public class DefaultInputTextPluginTest {

    // U+2F3C '⼼' should not be normalized to U+5FC3 '心'
    // 'Ⅲ' should not be normalized to 'III' but should be lower case 'ⅲ'
    static final String ORIGINAL_TEXT = "ÂＢΓД㈱ｶﾞウ゛⼼Ⅲ";
    static final String NORMALIZED_TEXT = "âbγд(株)ガヴ⼼ⅲ";
    UTF8InputTextBuilder builder;
    UTF8InputText text;
    DefaultInputTextPlugin plugin;

    @Before
    public void setUp() {
        builder = new UTF8InputTextBuilder(ORIGINAL_TEXT, new MockGrammar());
        plugin = new DefaultInputTextPlugin();
        try {
            plugin.rewriteDef = DefaultInputTextPluginTest.class.getClassLoader().getResource("rewrite.def").getPath();
            plugin.setUp(new MockGrammar());
        } catch (IOException ex) {
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
        assertThat(bytes.length, is(30));
        assertArrayEquals(new byte[] { (byte) 0xC3, (byte) 0x82, (byte) 0xEF, (byte) 0xBC, (byte) 0xA2, (byte) 0xCE,
                (byte) 0x93, (byte) 0xD0, (byte) 0x94, (byte) 0xE3, (byte) 0x88, (byte) 0xB1, (byte) 0xEF, (byte) 0xBD,
                (byte) 0xB6, (byte) 0xEF, (byte) 0xBE, (byte) 0x9E, (byte) 0xE3, (byte) 0x82, (byte) 0xA6, (byte) 0xE3,
                (byte) 0x82, (byte) 0x9B, (byte) 0xE2, (byte) 0xBC, (byte) 0xBC, (byte) 0xE2, (byte) 0x85,
                (byte) 0xA2 }, bytes);
        assertThat(text.getOriginalIndex(0), is(0));
        assertThat(text.getOriginalIndex(1), is(0));
        assertThat(text.getOriginalIndex(2), is(1));
        assertThat(text.getOriginalIndex(4), is(1));
        assertThat(text.getOriginalIndex(8), is(3));
        assertThat(text.getOriginalIndex(12), is(5));
        assertThat(text.getOriginalIndex(24), is(9));
        assertThat(text.getOriginalIndex(26), is(9));
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
        assertThat(bytes.length, is(24));
        assertArrayEquals(new byte[] { (byte) 0xC3, (byte) 0xA2, (byte) 0x62, (byte) 0xCE, (byte) 0xB3, (byte) 0xD0,
                (byte) 0xB4, (byte) 0x28, (byte) 0xE6, (byte) 0xA0, (byte) 0xAA, (byte) 0x29, (byte) 0xE3, (byte) 0x82,
                (byte) 0xAC, (byte) 0xE3, (byte) 0x83, (byte) 0xB4, (byte) 0xE2, (byte) 0xBC, (byte) 0xBC, (byte) 0xE2,
                (byte) 0x85, (byte) 0xB2 }, bytes);
        assertThat(text.getOriginalIndex(0), is(0));
        assertThat(text.getOriginalIndex(1), is(0));
        assertThat(text.getOriginalIndex(2), is(1));
        assertThat(text.getOriginalIndex(3), is(2));
        assertThat(text.getOriginalIndex(7), is(4));
        assertThat(text.getOriginalIndex(8), is(5));
        assertThat(text.getOriginalIndex(11), is(5));
        assertThat(text.getOriginalIndex(15), is(7));
        assertThat(text.getOriginalIndex(17), is(7));
    }

    @Test
    public void setUpWithNull() throws IOException {
        plugin = new DefaultInputTextPlugin();
        plugin.setSettings(new Settings(Json.createObjectBuilder().build(), null));
        plugin.setUp(new MockGrammar());
        assertThat(plugin.rewriteDef, is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidFormatOfIgnoreList() throws IOException {
        plugin = new DefaultInputTextPlugin();
        plugin.rewriteDef = DefaultInputTextPluginTest.class.getClassLoader()
                .getResource("rewrite_error_ignorelist.def").getPath();
        plugin.setUp(new MockGrammar());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidFormatOfReplaceList() throws IOException {
        plugin = new DefaultInputTextPlugin();
        plugin.rewriteDef = DefaultInputTextPluginTest.class.getClassLoader()
                .getResource("rewrite_error_replacelist.def").getPath();
        plugin.setUp(new MockGrammar());
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicatedLinesInReplaceList() throws IOException {
        plugin = new DefaultInputTextPlugin();
        plugin.rewriteDef = DefaultInputTextPluginTest.class.getClassLoader().getResource("rewrite_error_dup.def")
                .getPath();
        plugin.setUp(new MockGrammar());
    }
}

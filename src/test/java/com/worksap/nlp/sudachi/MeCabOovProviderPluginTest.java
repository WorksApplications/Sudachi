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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.worksap.nlp.sudachi.MeCabOovProviderPlugin.CategoryInfo;
import com.worksap.nlp.sudachi.dictionary.CategoryType;

public class MeCabOovProviderPluginTest {

    static class TestPlugin extends MeCabOovProviderPlugin {

        @SuppressWarnings("unchecked")
        public List<LatticeNode> provideOOV(InputText inputText, int offset, boolean otherWords) {
            List<? extends LatticeNode> nodes = new ArrayList<>();
            provideOOV(inputText, offset, otherWords ? 1 : 0, (List<LatticeNodeImpl>) nodes);
            return (List<LatticeNode>) nodes;
        }
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    TestPlugin plugin;
    MockInputText inputText;

    @Before
    public void setUp() throws IOException {
        plugin = new TestPlugin();

        MeCabOovProviderPlugin.OOV oov1 = new MeCabOovProviderPlugin.OOV();
        oov1.posId = 1;
        MeCabOovProviderPlugin.OOV oov2 = new MeCabOovProviderPlugin.OOV();
        oov2.posId = 2;
        plugin.oovList.put(CategoryType.KANJI, Collections.singletonList(oov1));
        plugin.oovList.put(CategoryType.KANJINUMERIC, Arrays.asList(oov1, oov2));

        inputText = new MockInputText("あいうえお");
    }

    @Test
    public void provideOOV000() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.KANJI;
        cinfo.isInvoke = false;
        cinfo.isGroup = false;
        cinfo.length = 0;
        plugin.categories.put(CategoryType.KANJI, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(0));

        nodes = plugin.provideOOV(inputText, 0, true);
        assertThat(nodes.size(), is(0));
    }

    @Test
    public void provideOOV100() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.KANJI;
        cinfo.isInvoke = true;
        cinfo.isGroup = false;
        cinfo.length = 0;
        plugin.categories.put(CategoryType.KANJI, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(0));

        nodes = plugin.provideOOV(inputText, 0, true);
        assertThat(nodes.size(), is(0));
    }

    @Test
    public void provideOOV010() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.KANJI;
        cinfo.isInvoke = false;
        cinfo.isGroup = true;
        cinfo.length = 0;
        plugin.categories.put(CategoryType.KANJI, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(1));

        LatticeNode n = nodes.get(0);
        assertThat(n.getWordInfo().getSurface(), is("あいう"));
        assertThat(n.getWordInfo().getLength(), is((short) 3));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        nodes = plugin.provideOOV(inputText, 0, true);
        assertThat(nodes.size(), is(0));
    }

    @Test
    public void provideOOV110() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.KANJI;
        cinfo.isInvoke = true;
        cinfo.isGroup = true;
        cinfo.length = 0;
        plugin.categories.put(CategoryType.KANJI, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(1));

        LatticeNode n = nodes.get(0);
        assertThat(n.getWordInfo().getSurface(), is("あいう"));
        assertThat(n.getWordInfo().getLength(), is((short) 3));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        nodes = plugin.provideOOV(inputText, 0, true);
        assertThat(nodes.size(), is(1));
    }

    @Test
    public void provideOOV002() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.KANJI;
        cinfo.isInvoke = false;
        cinfo.isGroup = false;
        cinfo.length = 2;
        plugin.categories.put(CategoryType.KANJI, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(2));

        LatticeNode n = nodes.get(0);
        assertThat(n.getWordInfo().getSurface(), is("あ"));
        assertThat(n.getWordInfo().getLength(), is((short) 1));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        n = nodes.get(1);
        assertThat(n.getWordInfo().getSurface(), is("あい"));
        assertThat(n.getWordInfo().getLength(), is((short) 2));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        nodes = plugin.provideOOV(inputText, 0, true);
        assertThat(nodes.size(), is(0));
    }

    public void provideOOV102() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.KANJI;
        cinfo.isInvoke = false;
        cinfo.isGroup = false;
        cinfo.length = 2;
        plugin.categories.put(CategoryType.KANJI, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(2));

        LatticeNode n = nodes.get(0);
        assertThat(n.getWordInfo().getSurface(), is("あ"));
        assertThat(n.getWordInfo().getLength(), is((short) 1));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        n = nodes.get(1);
        assertThat(n.getWordInfo().getSurface(), is("あい"));
        assertThat(n.getWordInfo().getLength(), is((short) 2));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        nodes = plugin.provideOOV(inputText, 0, true);
        assertThat(nodes.size(), is(2));
    }

    @Test
    public void provideOOV012() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.KANJI;
        cinfo.isInvoke = false;
        cinfo.isGroup = true;
        cinfo.length = 2;
        plugin.categories.put(CategoryType.KANJI, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(3));

        LatticeNode n = nodes.get(0);
        assertThat(n.getWordInfo().getSurface(), is("あいう"));
        assertThat(n.getWordInfo().getLength(), is((short) 3));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        n = nodes.get(1);
        assertThat(n.getWordInfo().getSurface(), is("あ"));
        assertThat(n.getWordInfo().getLength(), is((short) 1));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        n = nodes.get(2);
        assertThat(n.getWordInfo().getSurface(), is("あい"));
        assertThat(n.getWordInfo().getLength(), is((short) 2));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        nodes = plugin.provideOOV(inputText, 0, true);
        assertThat(nodes.size(), is(0));
    }

    @Test
    public void provideOOV112() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.KANJI;
        cinfo.isInvoke = true;
        cinfo.isGroup = true;
        cinfo.length = 2;
        plugin.categories.put(CategoryType.KANJI, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(3));

        LatticeNode n = nodes.get(0);
        assertThat(n.getWordInfo().getSurface(), is("あいう"));
        assertThat(n.getWordInfo().getLength(), is((short) 3));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        n = nodes.get(1);
        assertThat(n.getWordInfo().getSurface(), is("あ"));
        assertThat(n.getWordInfo().getLength(), is((short) 1));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        n = nodes.get(2);
        assertThat(n.getWordInfo().getSurface(), is("あい"));
        assertThat(n.getWordInfo().getLength(), is((short) 2));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        nodes = plugin.provideOOV(inputText, 0, true);
        assertThat(nodes.size(), is(3));
    }

    @Test
    public void provideOOV006() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.KANJI;
        cinfo.isInvoke = false;
        cinfo.isGroup = false;
        cinfo.length = 6;
        plugin.categories.put(CategoryType.KANJI, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(3));

        LatticeNode n = nodes.get(0);
        assertThat(n.getWordInfo().getSurface(), is("あ"));
        assertThat(n.getWordInfo().getLength(), is((short) 1));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        n = nodes.get(1);
        assertThat(n.getWordInfo().getSurface(), is("あい"));
        assertThat(n.getWordInfo().getLength(), is((short) 2));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        n = nodes.get(2);
        assertThat(n.getWordInfo().getSurface(), is("あいう"));
        assertThat(n.getWordInfo().getLength(), is((short) 3));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        nodes = plugin.provideOOV(inputText, 0, true);
        assertThat(nodes.size(), is(0));
    }

    @Test
    public void provideOOVMultiOOV() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.KANJINUMERIC;
        cinfo.isInvoke = false;
        cinfo.isGroup = true;
        cinfo.length = 0;
        plugin.categories.put(CategoryType.KANJINUMERIC, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.KANJINUMERIC);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(2));

        LatticeNode n = nodes.get(0);
        assertThat(n.getWordInfo().getSurface(), is("あいう"));
        assertThat(n.getWordInfo().getLength(), is((short) 3));
        assertThat(n.getWordInfo().getPOSId(), is((short) 1));

        n = nodes.get(1);
        assertThat(n.getWordInfo().getSurface(), is("あいう"));
        assertThat(n.getWordInfo().getLength(), is((short) 3));
        assertThat(n.getWordInfo().getPOSId(), is((short) 2));
    }

    @Test
    public void provideOOVWithoutCInfo() {
        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(0));
    }

    @Test
    public void provideOOVWithoutOOVList() {
        MeCabOovProviderPlugin.CategoryInfo cinfo = new MeCabOovProviderPlugin.CategoryInfo();
        cinfo.type = CategoryType.HIRAGANA;
        cinfo.isInvoke = false;
        cinfo.isGroup = true;
        cinfo.length = 0;
        plugin.categories.put(CategoryType.HIRAGANA, cinfo);

        inputText.setCategoryType(0, 3, CategoryType.HIRAGANA);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertThat(nodes.size(), is(0));
    }

    public static class Lines extends Config.Resource<byte[]> {
        private final byte[] data;

        public Lines(String... data) {
            String joined = String.join("\n", data);
            this.data = joined.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public InputStream asInputStream() {
            return new ByteArrayInputStream(data);
        }

        @Override
        public ByteBuffer asByteBuffer() {
            return ByteBuffer.wrap(data);
        }
    }

    @Test
    public void readCharacterProperty() throws IOException {
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.readCharacterProperty(new Lines("#", "DEFAULT 0 1 2", "ALPHA 1 0 0", "0x0000...0x0002 ALPHA"));
        assertFalse(plugin.categories.get(CategoryType.DEFAULT).isInvoke);
        assertTrue(plugin.categories.get(CategoryType.DEFAULT).isGroup);
        assertThat(plugin.categories.get(CategoryType.DEFAULT).length, is(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readCharacterPropertyWithTooFewColumns() throws IOException {
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.readCharacterProperty(new Lines("DEFAULT 0 1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readCharacterPropertyWithUndefinedType() throws IOException {
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.readCharacterProperty(new Lines("FOO 0 1 2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readCharacterPropertyDuplicatedDefinitions() throws IOException {
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.readCharacterProperty(new Lines("DEFAULT 0 1 2", "DEFAULT 1 1 2"));
    }

    @Test
    public void readOOV() throws IOException {
        Lines oovConfig = new Lines("DEFAULT,1,2,3,補助記号,一般,*,*,*,*", "DEFAULT,3,4,5,補助記号,一般,*,*,*,*");
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.categories.put(CategoryType.DEFAULT, new CategoryInfo());
        plugin.readOOV(oovConfig, new MockGrammar());
        assertThat(plugin.oovList.size(), is(1));
        assertThat(plugin.oovList.get(CategoryType.DEFAULT).size(), is(2));
        assertThat(plugin.oovList.get(CategoryType.DEFAULT).get(0).leftId, is((short) 1));
        assertThat(plugin.oovList.get(CategoryType.DEFAULT).get(0).rightId, is((short) 2));
        assertThat(plugin.oovList.get(CategoryType.DEFAULT).get(0).cost, is((short) 3));
        assertThat(plugin.oovList.get(CategoryType.DEFAULT).get(0).posId, is((short) 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readOOVWithTooFewColumns() throws IOException {
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.categories.put(CategoryType.DEFAULT, new CategoryInfo());
        plugin.readOOV(new Lines("DEFAULT,1,2,3"), new MockGrammar());
    }

    @Test(expected = IllegalArgumentException.class)
    public void readOOVWithUndefinedType() throws IOException {
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.categories.put(CategoryType.DEFAULT, new CategoryInfo());
        plugin.readOOV(new Lines("FOO,1,2,3,補助記号,一般,*,*,*,*"), new MockGrammar());
    }

    @Test(expected = IllegalArgumentException.class)
    public void readOOVWithCategoryNotInCharacterProperty() throws IOException {
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.categories.put(CategoryType.DEFAULT, new CategoryInfo());
        plugin.readOOV(new Lines("FOO,1,2,3,補助記号,一般,*,*,*,*"), new MockGrammar());
    }
}

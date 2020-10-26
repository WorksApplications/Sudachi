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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.worksap.nlp.sudachi.MeCabOovProviderPlugin.CategoryInfo;
import com.worksap.nlp.sudachi.dictionary.CategoryType;

public class MeCabOovProviderPluginTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    MeCabOovProviderPlugin plugin;
    MockInputText inputText;

    @Before
    public void setUp() throws IOException {
        plugin = new MeCabOovProviderPlugin();

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

    @Test
    public void readCharacterProperty() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("#\n  \nDEFAULT 0 1 2\nALPHA 1 0 0\n0x0000...0x0002 ALPHA");
        }
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.readCharacterProperty(inputFile.getPath());
        assertFalse(plugin.categories.get(CategoryType.DEFAULT).isInvoke);
        assertTrue(plugin.categories.get(CategoryType.DEFAULT).isGroup);
        assertThat(plugin.categories.get(CategoryType.DEFAULT).length, is(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readCharacterPropertyWithTooFewColumns() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("DEFAULT 0 1\n");
        }
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.readCharacterProperty(inputFile.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void readCharacterPropertyWithUndefinedType() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("FOO 0 1 2\n");
        }
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.readCharacterProperty(inputFile.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void readCharacterPropertyDuplicatedDefinitions() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("DEFAULT 0 1 2\nDEFAULT 1 1 2");
        }
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.readCharacterProperty(inputFile.getPath());
    }

    @Test
    public void readOOV() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("DEFAULT,1,2,3,補助記号,一般,*,*,*,*\n");
            writer.write("DEFAULT,3,4,5,補助記号,一般,*,*,*,*\n");
        }
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.categories.put(CategoryType.DEFAULT, new CategoryInfo());
        plugin.readOOV(inputFile.getPath(), new MockGrammar());
        assertThat(plugin.oovList.size(), is(1));
        assertThat(plugin.oovList.get(CategoryType.DEFAULT).size(), is(2));
        assertThat(plugin.oovList.get(CategoryType.DEFAULT).get(0).leftId, is((short) 1));
        assertThat(plugin.oovList.get(CategoryType.DEFAULT).get(0).rightId, is((short) 2));
        assertThat(plugin.oovList.get(CategoryType.DEFAULT).get(0).cost, is((short) 3));
        assertThat(plugin.oovList.get(CategoryType.DEFAULT).get(0).posId, is((short) 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readOOVWithTooFewColumns() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("DEFAULT,1,2,3\n");
        }
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.categories.put(CategoryType.DEFAULT, new CategoryInfo());
        plugin.readOOV(inputFile.getPath(), new MockGrammar());
    }

    @Test(expected = IllegalArgumentException.class)
    public void readOOVWithUndefinedType() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("FOO,1,2,3,補助記号,一般,*,*,*,*\n");
        }
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.categories.put(CategoryType.DEFAULT, new CategoryInfo());
        plugin.readOOV(inputFile.getPath(), new MockGrammar());
    }

    @Test(expected = IllegalArgumentException.class)
    public void readOOVWithCategoryNotInCharacterProperty() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("ALPHA,1,2,3,補助記号,一般,*,*,*,*\n");
        }
        MeCabOovProviderPlugin plugin = new MeCabOovProviderPlugin();
        plugin.categories.put(CategoryType.DEFAULT, new CategoryInfo());
        plugin.readOOV(inputFile.getPath(), new MockGrammar());
    }
}

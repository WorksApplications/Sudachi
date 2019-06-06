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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.worksap.nlp.sudachi.dictionary.CategoryType;

public class MeCabOovProviderPluginTest {

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
        assertEquals(0, nodes.size());

        nodes = plugin.provideOOV(inputText, 0, true);
        assertEquals(0, nodes.size());
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
        assertEquals(0, nodes.size());

        nodes = plugin.provideOOV(inputText, 0, true);
        assertEquals(0, nodes.size());
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
        assertEquals(1, nodes.size());

        LatticeNode n = nodes.get(0);
        assertEquals("あいう", n.getWordInfo().getSurface());
        assertEquals(3, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        nodes = plugin.provideOOV(inputText, 0, true);
        assertEquals(0, nodes.size());
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
        assertEquals(1, nodes.size());

        LatticeNode n = nodes.get(0);
        assertEquals("あいう", n.getWordInfo().getSurface());
        assertEquals(3, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        nodes = plugin.provideOOV(inputText, 0, true);
        assertEquals(1, nodes.size());
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
        assertEquals(2, nodes.size());

        LatticeNode n = nodes.get(0);
        assertEquals("あ", n.getWordInfo().getSurface());
        assertEquals(1, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        n = nodes.get(1);
        assertEquals("あい", n.getWordInfo().getSurface());
        assertEquals(2, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        nodes = plugin.provideOOV(inputText, 0, true);
        assertEquals(0, nodes.size());
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
        assertEquals(2, nodes.size());

        LatticeNode n = nodes.get(0);
        assertEquals("あ", n.getWordInfo().getSurface());
        assertEquals(1, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        n = nodes.get(1);
        assertEquals("あい", n.getWordInfo().getSurface());
        assertEquals(2, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        nodes = plugin.provideOOV(inputText, 0, true);
        assertEquals(2, nodes.size());
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
        assertEquals(3, nodes.size());

        LatticeNode n = nodes.get(0);
        assertEquals("あいう", n.getWordInfo().getSurface());
        assertEquals(3, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        n = nodes.get(1);
        assertEquals("あ", n.getWordInfo().getSurface());
        assertEquals(1, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        n = nodes.get(2);
        assertEquals("あい", n.getWordInfo().getSurface());
        assertEquals(2, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        nodes = plugin.provideOOV(inputText, 0, true);
        assertEquals(0, nodes.size());
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
        assertEquals(3, nodes.size());

        LatticeNode n = nodes.get(0);
        assertEquals("あいう", n.getWordInfo().getSurface());
        assertEquals(3, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        n = nodes.get(1);
        assertEquals("あ", n.getWordInfo().getSurface());
        assertEquals(1, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        n = nodes.get(2);
        assertEquals("あい", n.getWordInfo().getSurface());
        assertEquals(2, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        nodes = plugin.provideOOV(inputText, 0, true);
        assertEquals(3, nodes.size());
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
        assertEquals(3, nodes.size());

        LatticeNode n = nodes.get(0);
        assertEquals("あ", n.getWordInfo().getSurface());
        assertEquals(1, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        n = nodes.get(1);
        assertEquals("あい", n.getWordInfo().getSurface());
        assertEquals(2, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        n = nodes.get(2);
        assertEquals("あいう", n.getWordInfo().getSurface());
        assertEquals(3, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        nodes = plugin.provideOOV(inputText, 0, true);
        assertEquals(0, nodes.size());
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
        assertEquals(2, nodes.size());

        LatticeNode n = nodes.get(0);
        assertEquals("あいう", n.getWordInfo().getSurface());
        assertEquals(3, n.getWordInfo().getLength());
        assertEquals(1, n.getWordInfo().getPOSId());

        n = nodes.get(1);
        assertEquals("あいう", n.getWordInfo().getSurface());
        assertEquals(3, n.getWordInfo().getLength());
        assertEquals(2, n.getWordInfo().getPOSId());
    }

    @Test
    public void provideOOVWithoutCInfo() {
        inputText.setCategoryType(0, 3, CategoryType.KANJI);

        List<LatticeNode> nodes = plugin.provideOOV(inputText, 0, false);
        assertEquals(0, nodes.size());
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
        assertEquals(0, nodes.size());
    }
}

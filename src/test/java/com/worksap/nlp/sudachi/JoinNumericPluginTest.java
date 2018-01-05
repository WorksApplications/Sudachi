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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import org.junit.rules.TemporaryFolder;

public class JoinNumericPluginTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    JapaneseTokenizer tokenizer;
    JoinNumericPlugin plugin;

    @Before
    public void setUp() throws IOException {
        Utils.copyResource(temporaryFolder.getRoot().toPath(),
                "/system.dic", "/user.dic", "/joinnumeric/char.def", "/unk.def");

        String path = temporaryFolder.getRoot().getPath();
        String settings = Utils.readAllResource("/sudachi.json");
        Dictionary dict = new DictionaryFactory().create(path, settings);
        tokenizer = (JapaneseTokenizer)dict.create();

        plugin = new JoinNumericPlugin();
    }

    @Test
    public void testKanjiNumericEnabled() {
        plugin.joinKanjiNumeric = true;

        UTF8InputText input = getInputText("123一二三123");
        LatticeImpl lattice = tokenizer.buildLattice(input);
        List<LatticeNode> path = lattice.getBestPath();
        plugin.rewrite(input, path, lattice);

        assertEquals(3, path.size());
        assertEquals("123", path.get(0).getWordInfo().getSurface());
        assertEquals("一二三", path.get(1).getWordInfo().getSurface());
        assertEquals("123", path.get(2).getWordInfo().getSurface());
    }

    @Test
    public void testAllNumericEnabledNumericKanjiNumeric() {
        plugin.joinAllNumeric = true;

        UTF8InputText input = getInputText("123一二三123");
        LatticeImpl lattice = tokenizer.buildLattice(input);
        List<LatticeNode> path = lattice.getBestPath();
        plugin.rewrite(input, path, lattice);

        assertEquals(1, path.size());
        assertEquals("123一二三123", path.get(0).getWordInfo().getSurface());
    }

    @Test
    public void testAllNumericEnabledKanjiNumeric() {
        plugin.joinAllNumeric = true;

        UTF8InputText input = getInputText("一二三");
        LatticeImpl lattice = tokenizer.buildLattice(input);
        List<LatticeNode> path = lattice.getBestPath();
        plugin.rewrite(input, path, lattice);

        assertEquals(1, path.size());
        assertEquals("一二三", path.get(0).getWordInfo().getSurface());
    }

    @Test
    public void testAllNumericEnabledNonNumericKanjiNumeric() {
        plugin.joinAllNumeric = true;

        UTF8InputText input = getInputText(".一二三");
        LatticeImpl lattice = tokenizer.buildLattice(input);
        List<LatticeNode> path = lattice.getBestPath();
        plugin.rewrite(input, path, lattice);

        assertEquals(2, path.size());
        assertEquals(".", path.get(0).getWordInfo().getSurface());
        assertEquals("一二三", path.get(1).getWordInfo().getSurface());
    }

    private UTF8InputText getInputText(String text) {
        UTF8InputTextBuilder builder = new UTF8InputTextBuilder(text, tokenizer.grammar);
        return builder.build();
    }

}

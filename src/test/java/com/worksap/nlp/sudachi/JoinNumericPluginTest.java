/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class JoinNumericPluginTest {
    JapaneseTokenizer tokenizer;
    JoinNumericPlugin plugin;

    @Before
    public void setUp() throws IOException {
        Config config = TestDictionary.INSTANCE.user0Cfg()
                .characterDefinition(getClass().getClassLoader().getResource("joinnumeric/char.def"));
        Dictionary dict = new DictionaryFactory().create(config);
        tokenizer = (JapaneseTokenizer) dict.create();

        plugin = new JoinNumericPlugin();
        plugin.setSettings(Settings.parse("{}", PathAnchor.none()));
        plugin.setUp(((JapaneseDictionary) dict).grammar);
    }

    @Test
    public void testDigit() {
        List<LatticeNodeImpl> path = getPath("123円20銭");
        assertEquals(4, path.size());
        assertEquals("123", path.get(0).getBaseSurface());
        assertEquals("20", path.get(2).getBaseSurface());

        path = getPath("080-121");
        assertEquals(3, path.size());
        assertEquals("080", path.get(0).getBaseSurface());
        assertEquals("121", path.get(2).getBaseSurface());
    }

    @Test
    public void testKanjiNumeric() {
        List<LatticeNodeImpl> path = getPath("一二三万二千円");
        assertEquals(2, path.size());
        assertEquals("一二三万二千", path.get(0).getBaseSurface());

        path = getPath("二百百");
        assertEquals(3, path.size());
    }

    @Test
    public void testNormalize() {
        plugin.enableNormalize = true;
        List<LatticeNodeImpl> path = getPath("一二三万二千円");
        assertEquals(2, path.size());
        assertEquals("1232000", path.get(0).getBaseSurface());
    }

    @Test
    public void testNormalizeWithNotNumeric() {
        plugin.enableNormalize = true;
        List<LatticeNodeImpl> path = getPath("六三四");
        assertEquals(1, path.size());
        assertEquals("六三四", path.get(0).getNormalizedForm());
    }

    @Test
    public void testPoint() {
        plugin.enableNormalize = true;

        List<LatticeNodeImpl> path = getPath("1.002");
        assertEquals(1, path.size());
        assertEquals("1.002", path.get(0).getNormalizedForm());

        path = getPath(".002");
        assertEquals(2, path.size());
        assertEquals(".", path.get(0).getNormalizedForm());
        assertEquals("002", path.get(1).getNormalizedForm());

        path = getPath("22.");
        assertEquals(2, path.size());
        assertEquals("22", path.get(0).getNormalizedForm());
        assertEquals(".", path.get(1).getNormalizedForm());

        path = getPath("22.節");
        assertEquals(3, path.size());
        assertEquals("22", path.get(0).getNormalizedForm());
        assertEquals(".", path.get(1).getNormalizedForm());

        path = getPath(".c");
        assertEquals(2, path.size());
        assertEquals(".", path.get(0).getNormalizedForm());

        path = getPath("1.20.3");
        assertEquals(5, path.size());
        assertEquals("20", path.get(2).getNormalizedForm());

        path = getPath("652...");
        assertEquals(4, path.size());
        assertEquals("652", path.get(0).getNormalizedForm());
    }

    @Test
    public void testComma() {
        plugin.enableNormalize = true;

        List<LatticeNodeImpl> path = getPath("2,000,000");
        assertEquals(1, path.size());
        assertEquals("2000000", path.get(0).getNormalizedForm());

        path = getPath("2,00,000,000円");
        assertEquals(8, path.size());
        assertEquals("2", path.get(0).getNormalizedForm());
        assertEquals(",", path.get(1).getNormalizedForm());
        assertEquals("00", path.get(2).getNormalizedForm());
        assertEquals(",", path.get(3).getNormalizedForm());
        assertEquals("000", path.get(4).getNormalizedForm());
        assertEquals(",", path.get(5).getNormalizedForm());
        assertEquals("000", path.get(6).getNormalizedForm());

        path = getPath(",");
        assertEquals(1, path.size());

        path = getPath("652,,,");
        assertEquals(4, path.size());
        assertEquals("652", path.get(0).getNormalizedForm());

        path = getPath("256,5.50389");
        assertEquals(3, path.size());
        assertEquals("256", path.get(0).getNormalizedForm());
        assertEquals("5.50389", path.get(2).getNormalizedForm());

        path = getPath("256,550.389");
        assertEquals(1, path.size());
        assertEquals("256550.389", path.get(0).getNormalizedForm());
    }

    @Test
    public void testSingleNode() {
        plugin.enableNormalize = false;
        List<LatticeNodeImpl> path = getPath("猫三匹");
        assertEquals(3, path.size());
        assertEquals("三", path.get(1).getNormalizedForm());

        plugin.enableNormalize = true;
        path = getPath("猫三匹");
        assertEquals(3, path.size());
        assertEquals("3", path.get(1).getNormalizedForm());
    }

    private List<LatticeNodeImpl> getPath(String text) {
        UTF8InputText input = new UTF8InputTextBuilder(text, tokenizer.grammar).build();
        LatticeImpl lattice = tokenizer.buildLattice(input);
        List<LatticeNodeImpl> path = lattice.getBestPath();
        plugin.rewrite(input, path, lattice);
        lattice.clear();
        return path;
    }

}

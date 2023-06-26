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
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class JoinKatakanaOovPluginTest {
    JapaneseTokenizer tokenizer;
    JoinKatakanaOovPlugin plugin;

    @Before
    public void setUp() throws IOException {
        Dictionary dict = TestDictionary.INSTANCE.user1();
        tokenizer = (JapaneseTokenizer) dict.create();
        plugin = new JoinKatakanaOovPlugin();
    }

    @Test
    public void testKatakanaLength() {
        // アイ, アイウ in the dictionary

        plugin.minLength = 0;
        List<? extends LatticeNode> path = getPath("アイアイウ");
        assertEquals(2, path.size());

        plugin.minLength = 1;
        path = getPath("アイアイウ");
        assertEquals(2, path.size());

        plugin.minLength = 2;
        path = getPath("アイアイウ");
        assertEquals(2, path.size());

        plugin.minLength = 3;
        path = getPath("アイアイウ");
        assertEquals(1, path.size());
    }

    @Test
    public void testPOS() {
        // アイアイウ is 名詞-固有名詞-地名-一般 in the dictionary
        plugin.minLength = 3;
        List<? extends LatticeNode> path = getPath("アイアイウ");
        assertEquals(1, path.size());
        assertFalse(path.get(0).isOOV()); // use the word in dictionary
    }

    @Test
    public void testStartWithMiddle() {
        plugin.minLength = 3;
        List<? extends LatticeNode> path = getPath("アイウアイアイウ");
        assertEquals(1, path.size());
    }

    @Test
    public void testStartWithTail() {
        plugin.minLength = 3;
        List<? extends LatticeNode> path = getPath("アイウアイウアイ");
        assertEquals(1, path.size());
    }

    @Test
    public void testWithNOOOVBOW() {
        plugin.minLength = 3;
        List<LatticeNodeImpl> path = getPath("ァアイアイウ");
        assertEquals(2, path.size());
        assertEquals("ァ", path.get(0).getBaseSurface());

        path = getPath("アイウァアイウ");
        assertEquals(1, path.size());
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

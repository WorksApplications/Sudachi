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

package com.worksap.nlp.sudachi.dictionary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.*;

public class DoubleArrayLexiconTest {

    static final int GRAMMAR_SIZE = 410;

    DoubleArrayLexicon lexicon;

    @Before
    public void setUp() throws IOException {
        ByteBuffer bytes = DictionaryReader.read("/system.dic");
        DictionaryHeader header = new DictionaryHeader(bytes, 0);
        lexicon = new DoubleArrayLexicon(bytes, header.storageSize() + GRAMMAR_SIZE);
    }

    @Test
    public void lookup() {
        List<int[]> results
            = iteratorToList(lexicon.lookup("東京都".getBytes(StandardCharsets.UTF_8), 0));

        assertEquals(3, results.size());
        assertArrayEquals(new int[] { 4, 3 }, results.get(0)); // 東
        assertArrayEquals(new int[] { 5, 6 }, results.get(1)); // 東京
        assertArrayEquals(new int[] { 6, 9 }, results.get(2)); // 東京都

        results
            = iteratorToList(lexicon.lookup("東京都に".getBytes(StandardCharsets.UTF_8), 9));
        assertEquals(2, results.size());
        assertArrayEquals(new int[] { 1, 12 }, results.get(0)); // に(接続助詞)
        assertArrayEquals(new int[] { 2, 12 }, results.get(1)); // に(格助詞)

        results
            = iteratorToList(lexicon.lookup("あれ".getBytes(StandardCharsets.UTF_8), 0));
        assertEquals(0, results.size());
    }

    @Test
    public void parameters() {
        // た
        assertEquals(1, lexicon.getLeftId(0)); 
        assertEquals(1, lexicon.getRightId(0));
        assertEquals(8729, lexicon.getCost(0));

        // 東京都
        assertEquals(6, lexicon.getLeftId(6));
        assertEquals(8, lexicon.getRightId(6));
        assertEquals(5320, lexicon.getCost(6));

        // 都
        assertEquals(8, lexicon.getLeftId(9));
        assertEquals(8, lexicon.getRightId(9));
        assertEquals(2914, lexicon.getCost(9));
    }

    @Test
    public void wordInfo() {
        // た
        WordInfo wi = lexicon.getWordInfo(0);
        assertEquals("た", wi.getSurface());
        assertEquals(3, wi.getLength());
        assertEquals(0, wi.getPOSId());
        assertEquals("た", wi.getNormalizedForm());
        assertEquals(-1, wi.getDictionaryFormWordId());
        assertEquals("た", wi.getDictionaryForm());
        assertEquals("タ", wi.getReadingForm());
        assertArrayEquals(new int[0], wi.getAunitSplit());
        assertArrayEquals(new int[0], wi.getBunitSplit());
        assertArrayEquals(new int[0], wi.getWordStructure());

        // 行っ
        wi = lexicon.getWordInfo(8);
        assertEquals("行っ", wi.getSurface());
        assertEquals("行く", wi.getNormalizedForm());
        assertEquals(7, wi.getDictionaryFormWordId());
        assertEquals("行く", wi.getDictionaryForm());

        // 東京都
        wi = lexicon.getWordInfo(6);
        assertEquals("東京都", wi.getSurface());
        assertArrayEquals(new int[] { 5, 9 }, wi.getAunitSplit());
        assertArrayEquals(new int[0], wi.getBunitSplit());
        assertArrayEquals(new int[] { 5, 9 }, wi.getWordStructure());
    }

    static <E> List<E> iteratorToList(Iterator<E> iterator) {
        List<E> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }
}

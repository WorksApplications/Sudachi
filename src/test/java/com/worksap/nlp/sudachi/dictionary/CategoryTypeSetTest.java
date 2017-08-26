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

import static org.junit.Assert.*;
import org.junit.*;

import java.util.Iterator;

public class CategoryTypeSetTest {

    CategoryTypeSet set;

    @Before
    public void setUp() {
        set = new CategoryTypeSet();
        set.add(CategoryType.ALPHA);
        set.add(CategoryType.HIRAGANA);
        set.add(CategoryType.USER4);
    }

    @Test
    public void size() {
        assertEquals(3, set.size());
    }

    @Test
    public void contains() {
        assertTrue(set.contains(CategoryType.HIRAGANA));
        assertFalse(set.contains(CategoryType.USER1));
    }

    @Test
    public void remove() {
        set.remove(CategoryType.HIRAGANA);
        assertEquals(2, set.size());
        assertFalse(set.contains(CategoryType.HIRAGANA));
        assertTrue(set.contains(CategoryType.ALPHA));
    }

    @Test
    public void iterator() {
        Iterator<CategoryType> iterator = set.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(CategoryType.ALPHA, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(CategoryType.HIRAGANA, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(CategoryType.USER4, iterator.next());
        assertFalse(iterator.hasNext());
        assertNull(iterator.next());
    }

    @Test
    public void iteratorWithRemove() {
        Iterator<CategoryType> iterator = set.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(CategoryType.ALPHA, iterator.next());
        iterator.remove();
        assertEquals(2, set.size());
        assertFalse(set.contains(CategoryType.ALPHA));

        assertEquals(CategoryType.HIRAGANA, iterator.next());
        iterator.remove();
        assertEquals(1, set.size());
        assertFalse(set.contains(CategoryType.HIRAGANA));

        assertEquals(CategoryType.USER4, iterator.next());
        iterator.remove();
        assertEquals(0, set.size());
        assertFalse(set.contains(CategoryType.USER4));
    }

    @Test
    public void retainAll() {
        CategoryTypeSet other = new CategoryTypeSet();
        other.add(CategoryType.HIRAGANA);
        other.add(CategoryType.KATAKANA);

        set.retainAll(other);
        assertEquals(1, set.size());
        assertTrue(set.contains(CategoryType.HIRAGANA));
    }
}

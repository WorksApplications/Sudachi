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

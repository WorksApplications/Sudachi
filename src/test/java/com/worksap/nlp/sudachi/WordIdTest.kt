package com.worksap.nlp.sudachi

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals

class WordIdTest {
    @Test
    fun valid() {
        assertEquals(WordId.make(0, 0), 0)
        assertEquals(WordId.make(0, 5), 5)
        assertNotEquals(WordId.make(1, 5), 5)
    }

    @Test
    fun deconstruct() {
        val wid = WordId.make(12, 51612312)
        assertEquals(12, WordId.dic(wid))
        assertEquals(51612312, WordId.word(wid))
    }

    @Test
    fun invalid() {
        assertFails { WordId.make(0, WordId.MAX_WORD_ID + 1) }
        assertFails { WordId.make(WordId.MAX_DIC_ID + 1, 0) }
    }
}
package com.worksap.nlp.sudachi.dictionary.build

import kotlin.test.Test
import kotlin.test.assertEquals

class StringStorageTest {

    @Test
    fun simple() {
        val strs = StringStorage()
        strs.add("test")
        strs.add("es")
        strs.compile()
        val data = strs.strings;
        assertEquals(2, data.size)
        assertEquals(1, data["es"]?.start)
        assertEquals(3, data["es"]?.end)
    }

    @Test
    fun oneChar() {
        val strs = StringStorage()
        strs.add("x")
        strs.add("y")
        strs.compile()
        val data = strs.strings
        assertEquals(2, data.size)
        assertEquals(0, data["x"]?.start)
        assertEquals(1, data["x"]?.end)
        assertEquals(0, data["y"]?.start)
        assertEquals(1, data["y"]?.end)
    }
}
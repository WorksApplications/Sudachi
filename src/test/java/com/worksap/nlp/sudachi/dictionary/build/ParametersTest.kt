package com.worksap.nlp.sudachi.dictionary.build

import kotlin.test.Test
import kotlin.test.assertEquals

class ParametersTest {
    @Test
    fun resizeWorks() {
        val params = Parameters(4)
        params.add(1, 1, 1)
        params.add(2, 2, 2)
        val ch = BytesChannel()
        val out = ModelOutput(ch)
        params.writeTo(out)
        assertEquals(ch.size, 12)
        val b = ch.buffer()
        assertEquals(b.short, 1)
        assertEquals(b.short, 1)
        assertEquals(b.short, 1)
        assertEquals(b.short, 2)
        assertEquals(b.short, 2)
        assertEquals(b.short, 2)
        assertEquals(b.remaining(), 0)
    }
}

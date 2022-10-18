package com.worksap.nlp.sudachi.dictionary

import com.worksap.nlp.sudachi.dictionary.build.InMemoryChannel
import kotlin.test.Test
import kotlin.test.assertEquals

class DescriptionTest {
    @Test
    fun serialization() {
        val d = Description()
        d.blocks = listOf(Description.Block("test", 5, 15), Description.Block("test2", 30, 25))
        d.reference = "testref"
        d.comment = "コメント"
        val chan = InMemoryChannel(4096)
        d.save(chan)
        chan.position(0)
        val d2 = Description.load(chan)
        assertEquals(d.comment, d2.comment)
        assertEquals(d.reference, d2.reference)
        assertEquals(d.signature, d2.signature)
        assertEquals(d.blocks.size, d2.blocks.size)
        assertEquals(d.blocks[0].name, d2.blocks[0].name)
        assertEquals(d.blocks[0].start, d2.blocks[0].start)
        assertEquals(d.blocks[0].size, d2.blocks[0].size)
        assertEquals(d.blocks[1].name, d2.blocks[1].name)
        assertEquals(d.blocks[1].start, d2.blocks[1].start)
        assertEquals(d.blocks[1].size, d2.blocks[1].size)
    }
}
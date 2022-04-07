package com.worksap.nlp.sudachi.dictionary.build

import com.worksap.nlp.sudachi.dictionary.StringPtr
import java.nio.CharBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class WordLayoutTest {
    companion object {
        fun CharBuffer.read(ptr: StringPtr): String {
            return substring(ptr.offset, ptr.offset + ptr.length)
        }
    }


    @Test
    fun alignmentBasedPlacement() {
        val layout = WordLayout()
        val p1 = layout.add("0".repeat(25))
        val p2 = layout.add("1".repeat(23))
        val p3 = layout.add("2".repeat(15))
        val p4 = layout.add("3".repeat(3))
        val p5 = layout.add("4".repeat(1))
        val p6 = layout.add("5".repeat(2))
        val chan = InMemoryChannel()
        layout.write(chan)
        val chars = chan.buffer().asCharBuffer()
        assertEquals("0".repeat(25), chars.read(p1))
        assertEquals("1".repeat(23), chars.read(p2))
        assertEquals("2".repeat(15), chars.read(p3))
        assertEquals("3".repeat(3), chars.read(p4))
        assertEquals("4".repeat(1), chars.read(p5))
        assertEquals("5".repeat(2), chars.read(p6))
        assert(p5.offset < p2.offset)
        assert(p6.offset < p2.offset)
    }
}
package com.worksap.nlp.sudachi.dictionary.build

import java.nio.ByteOrder
import kotlin.test.*

class DicBufferTest {
    @Test
    fun writeEmptyIntArray() {
        val s = DicBuffer(1024)
        s.putInts(intArrayOf())
        val bb = s.consume { it.duplicate() }
        assertEquals(bb.remaining(), 1)
        assertEquals(bb.get(), 0)
        assertEquals(bb.remaining(), 0)
    }

    @Test
    fun writeIntArray() {
        val s = DicBuffer(1024)
        s.putInts(intArrayOf(1, 2, 3))
        val bb = s.consume { it.duplicate() }
        bb.order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(bb.remaining(), 4 * 3 + 1)
        assertEquals(bb.get(), 3)
        assertEquals(bb.getInt(), 1)
        assertEquals(bb.getInt(), 2)
        assertEquals(bb.getInt(), 3)
        assertEquals(bb.remaining(), 0)
    }

    @Test
    fun writeEmptyString() {
        val s = DicBuffer(1024)
        s.put("")
        val bb = s.consume { it.duplicate() }
        assertEquals(bb.remaining(), 1)
        assertEquals(bb.get(), 0)
        assertEquals(bb.remaining(), 0)
    }

    @Test
    fun writeSmallString() {
        val s = DicBuffer(1024)
        s.put("あ𠮟")
        val bb = s.consume { it.duplicate() }
        bb.order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(bb.remaining(), 1 + 2 * 3)
        assertEquals(bb.get(), 3)
        assertEquals(bb.getChar(), 'あ')
        assertEquals(bb.getChar(), '\uD842')
        assertEquals(bb.getChar(), '\uDF9F')
        assertEquals(bb.remaining(), 0)
    }

    @Test
    fun writeLargeString() {
        val s = DicBuffer(1024)
        val str = "0123456789".repeat(20)
        s.put(str)
        val bb = s.consume { it.duplicate() }
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val length = str.length
        assertEquals(bb.remaining(), 2 + length * 2)
        assertEquals(bb.get(), (length shr 8 or 0x80).toByte())
        assertEquals(bb.get(), (length and 0xff).toByte())
    }

    @Test
    fun failWriteHugeString() {
        val s = DicBuffer(1024)
        val str = "0123456789".repeat(DicBuffer.MAX_STRING / 10 + 1)
        assertFails { s.put(str) }
    }

    @Test
    fun checkedPut() {
        val s = DicBuffer(10)
        assertTrue { s.put("asdf") }
        assertFalse { s.put("asdf") }
    }
}
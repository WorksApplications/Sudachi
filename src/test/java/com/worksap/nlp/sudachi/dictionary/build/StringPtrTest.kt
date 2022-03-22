package com.worksap.nlp.sudachi.dictionary.build

import org.junit.Test
import kotlin.test.assertEquals


class StringPtrTest {

    @Test
    fun additionalBits() {
        assertEquals(0, StringPtr.unsafe(0, 0).additionalBits())
        assertEquals(1, StringPtr.unsafe(22, 0).additionalBits())
    }

    @Test
    fun lengthEncode() {
        assertEquals(0, StringPtr.unsafe(0, 0).encode())
        assertEquals(0b00001000_00000000_00000000_00000000, StringPtr.unsafe(1, 0).encode())
    }

    @Test
    fun decodeMaxLength() {
        val encoded = 0b11111111_11111111_00000000_00000000
        val decoded = StringPtr.decode(encoded.toInt())
        assertEquals(StringPtr.MAX_LENGTH, decoded.length)
    }

    @Test
    fun encodeMaxLength() {
        val decoded = StringPtr.unsafe(StringPtr.MAX_LENGTH, 0)
        val encoded = 0b11111111_11111111_00000000_00000000
        assertEquals(encoded.toInt(), decoded.encode())
    }
}
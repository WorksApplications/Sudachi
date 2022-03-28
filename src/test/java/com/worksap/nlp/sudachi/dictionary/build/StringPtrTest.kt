package com.worksap.nlp.sudachi.dictionary.build

import org.junit.Test
import kotlin.test.assertEquals


class StringPtrTest {

    @Test
    fun additionalBits() {
        assertEquals(0, StringPtr.unsafe(0, 0).additionalBits())
        assertEquals(2, StringPtr.unsafe(22, 0).additionalBits())
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

    private fun checkConversion(length: Int, offset: Int) {
        val original = StringPtr.unsafe(length, offset)
        val encoded = original.encode()
        val decoded = StringPtr.decode(encoded)
        assertEquals(original, decoded, "conversion failed, encoded value = %08x".format(encoded))
    }

    @Test
    fun decodeEncodeMaxSimple() {
        checkConversion(19, 0x07ff_ffff)
    }

    @Test
    fun decodeEncodeSimple() {
        checkConversion(5, 10)
        checkConversion(1, 10)
        checkConversion(19, 10)
    }

    @Test
    fun decodeEncodeAddLength() {
        // low offset bits must be aligned for large lengths
        checkConversion(19 + 0b00000000_000000001, 0x07ff_ffff xor ((1 shl 0) - 1))
        checkConversion(19 + 0b00000000_000000011, 0x07ff_ffff xor ((1 shl 1) - 1))
        checkConversion(19 + 0b00000000_000000111, 0x07ff_ffff xor ((1 shl 2) - 1))
        checkConversion(19 + 0b00000000_000001111, 0x07ff_ffff xor ((1 shl 3) - 1))
        checkConversion(19 + 0b00000000_000011111, 0x07ff_ffff xor ((1 shl 4) - 1))
        checkConversion(19 + 0b00000000_000111111, 0x07ff_ffff xor ((1 shl 5) - 1))
        checkConversion(19 + 0b00000000_001111111, 0x07ff_ffff xor ((1 shl 6) - 1))
        checkConversion(19 + 0b00000000_011111111, 0x07ff_ffff xor ((1 shl 7) - 1))
        checkConversion(19 + 0b00000000_111111111, 0x07ff_ffff xor ((1 shl 8) - 1))
        checkConversion(19 + 0b00000001_111111111, 0x07ff_ffff xor ((1 shl 9) - 1))
        checkConversion(19 + 0b00000011_111111111, 0x07ff_ffff xor ((1 shl 10) - 1))
        checkConversion(19 + 0b00000111_111111111, 0x07ff_ffff xor ((1 shl 11) - 1))
    }
}
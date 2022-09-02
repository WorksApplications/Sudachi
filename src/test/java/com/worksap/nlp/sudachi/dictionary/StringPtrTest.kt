/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi.dictionary

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

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

  @Test
  fun isValid() {
    assertTrue { StringPtr.isValid(0, 0) }
    assertTrue { StringPtr.isValid(1, 0) }
    assertTrue { StringPtr.isValid(0, 1) }
    assertTrue { StringPtr.isValid(1, 1) }
    assertTrue { StringPtr.isValid(0, 19) }
    assertTrue { StringPtr.isValid(1, 19) }
    assertTrue { StringPtr.isValid(0, 20) }
    assertTrue { StringPtr.isValid(1, 20) }
    assertTrue { StringPtr.isValid(0, 21) }
    assertFalse { StringPtr.isValid(1, 21) }
    assertTrue { StringPtr.isValid(2, 21) }
    assertTrue { StringPtr.isValid(0, 23) }
    assertFalse { StringPtr.isValid(1, 23) }
    assertFalse { StringPtr.isValid(2, 23) }
    assertTrue { StringPtr.isValid(4, 23) }
  }
}

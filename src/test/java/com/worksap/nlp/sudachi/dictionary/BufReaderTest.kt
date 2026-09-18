/*
 * Copyright (c) 2022-2025 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.dictionary.build.BufWriter
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

inline fun <reified T> check(
    crossinline fin: (BufWriter, T) -> Unit,
    crossinline fout: (BufReader) -> T
): (T) -> Unit = {
  val bb = ByteBuffer.allocate(32)
  val w = BufWriter(bb)
  fin(w, it)
  bb.flip()
  val r = BufReader(bb)
  val y = fout(r)
  assertEquals(it, y)
}

class BufReaderTest {
  @Test
  fun varint64() {
    val checkLong = check({ w, x -> w.putVarint64(x) }, { it.readVarint64() })
    // single bits
    checkLong(0x0)
    checkLong(0x1)
    checkLong(0x2)
    checkLong(0x4)
    checkLong(0x8)
    checkLong(0x10)
    checkLong(0x20)
    checkLong(0x40)
    checkLong(0x80)
    checkLong(0x100)
    checkLong(0x200)
    checkLong(0x400)
    checkLong(0x800)
    checkLong(0x1000)
    checkLong(0x2000)
    checkLong(0x4000)
    checkLong(0x8000)
    checkLong(0x1_0000)
    checkLong(0x2_0000)
    checkLong(0x4_0000)
    checkLong(0x8_0000)
    checkLong(0x10_0000)
    checkLong(0x20_0000)
    checkLong(0x40_0000)
    checkLong(0x80_0000)
    checkLong(0x100_0000)
    checkLong(0x200_0000)
    checkLong(0x400_0000)
    checkLong(0x800_0000)
    checkLong(0x1000_0000)
    checkLong(0x2000_0000)
    checkLong(0x4000_0000)
    checkLong(0x8000_0000)
    checkLong(0x1_0000_0000)
    checkLong(0x2_0000_0000)
    checkLong(0x4_0000_0000)
    checkLong(0x8_0000_0000)
    checkLong(0x10_0000_0000)
    checkLong(0x20_0000_0000)
    checkLong(0x40_0000_0000)
    checkLong(0x80_0000_0000)
    checkLong(0x100_0000_0000)
    checkLong(0x200_0000_0000)
    checkLong(0x400_0000_0000)
    checkLong(0x800_0000_0000)
    checkLong(0x1000_0000_0000)
    checkLong(0x2000_0000_0000)
    checkLong(0x4000_0000_0000)
    checkLong(0x8000_0000_0000)
    checkLong(0x1_0000_0000_0000)
    checkLong(0x2_0000_0000_0000)
    checkLong(0x4_0000_0000_0000)
    checkLong(0x8_0000_0000_0000)
    checkLong(0x10_0000_0000_0000)
    checkLong(0x20_0000_0000_0000)
    checkLong(0x40_0000_0000_0000)
    checkLong(0x80_0000_0000_0000)
    checkLong(0x100_0000_0000_0000)
    checkLong(0x200_0000_0000_0000)
    checkLong(0x400_0000_0000_0000)
    checkLong(0x800_0000_0000_0000)
    checkLong(0x1000_0000_0000_0000)
    checkLong(0x2000_0000_0000_0000)
    checkLong(0x4000_0000_0000_0000)
    checkLong(0x7fff_ffff_ffff_ffff.inv())

    // long-max
    checkLong(0x7fff_ffff_ffff_ffff)
    // full bit
    checkLong(0L.inv())
  }

  @Test
  fun varint32() {
    val checkInt = check({ w, x -> w.putVarint32(x) }, { it.readVarint32() })
    // single bits
    checkInt(0x0)
    checkInt(0x1)
    checkInt(0x2)
    checkInt(0x4)
    checkInt(0x8)
    checkInt(0x10)
    checkInt(0x20)
    checkInt(0x40)
    checkInt(0x80)
    checkInt(0x100)
    checkInt(0x200)
    checkInt(0x400)
    checkInt(0x800)
    checkInt(0x1000)
    checkInt(0x2000)
    checkInt(0x4000)
    checkInt(0x8000)
    checkInt(0x1_0000)
    checkInt(0x2_0000)
    checkInt(0x4_0000)
    checkInt(0x8_0000)
    checkInt(0x10_0000)
    checkInt(0x20_0000)
    checkInt(0x40_0000)
    checkInt(0x80_0000)
    checkInt(0x100_0000)
    checkInt(0x200_0000)
    checkInt(0x400_0000)
    checkInt(0x800_0000)
    checkInt(0x1000_0000)
    checkInt(0x2000_0000)
    checkInt(0x4000_0000)
    checkInt(0x7fff_ffff.inv())

    // int-max
    checkInt(0x7fff_ffff)
    // full bit
    checkInt(0.inv())
  }

  @Test
  fun invalid_varint64() {
    val bb = ByteBuffer.allocate(32)
    val w = BufWriter(bb)

    // 64 bits are encoded as 7 bits * 9 + 1 bit.
    for (i in 1..9) {
      w.putByte(Byte.MIN_VALUE) // 0x80, 1 bit continue flag + 7 bits
    }
    // using other than the lowest 1 bit is invalid here
    w.putByte(0x02)

    bb.flip()
    val r = BufReader(bb)
    assertFails { r.readVarint64() }
  }

  @Test
  fun invalid_varint32() {
    // put/read func for varint64/32 shares implementation
    val checkLong2Int =
        check({ w, x -> w.putVarint64(x) }, { it.readVarint32().toLong() and 0xffff_ffff })

    // less than or equal to 32 bit should be ok
    checkLong2Int(0x0)
    checkLong2Int(0x1)
    checkLong2Int(0x8)
    checkLong2Int(0x8000_0000)
    checkLong2Int(0xffff_ffff)

    // more than 32 bit should fail
    assertFails { checkLong2Int(0x1_0000_0000) }
  }

  @Test
  fun utf8String() {
    val checkUtf8String = check({ w, x -> w.putUtf8String(x) }, { it.readUtf8String() })
    checkUtf8String("")
    checkUtf8String("test")
    checkUtf8String("привет")
    checkUtf8String("こんにちは")
    checkUtf8String("東京都")
    checkUtf8String("""👨‍👩‍👧‍👦""")
    checkUtf8String("""t東e京s💞t都""")
  }

  @Test
  fun checkInts() {
    val checkInt = check({ w, x -> w.putVarint32(x) }, { it.readVarint32() })
    for (i in 0..10000) {
      checkInt(i)
    }
  }
}

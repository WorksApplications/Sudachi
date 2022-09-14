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

import com.worksap.nlp.sudachi.dictionary.build.BufWriter
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

fun checkLong(x: Long) {
  val bb = ByteBuffer.allocate(32)
  val w = BufWriter(bb)
  w.putVarint64(x)
  bb.flip()
  val r = BufReader(bb)
  val y = r.readVarint64()
  assertEquals(x, y)
}

fun checkInt(x: Int) {
  val bb = ByteBuffer.allocate(32)
  val w = BufWriter(bb)
  w.putVarint32(x)
  bb.flip()
  val r = BufReader(bb)
  val y = r.readVarint32()
  assertEquals(x, y)
}

fun checkUtf8String(s: String) {
  val bb = ByteBuffer.allocate(32)
  val w = BufWriter(bb)
  w.putStringUtf8(s)
  bb.flip()
  val r = BufReader(bb)
  val y = r.readUtf8String()
  assertEquals(s, y)
}

class BufReaderTest {
  @Test
  fun varint64() {
    checkLong(0L.inv())
    checkLong(0x0)
    checkLong(0x1)
    checkLong(0x80)
    checkLong(0xff)
    checkLong(0x4ff)
    checkLong(0xfff)
    checkLong(0x4fff)
    checkLong(0xffff)
    checkLong(0x4_ffff)
    checkLong(0xf_ffff)
    checkLong(0x4f_ffff)
    checkLong(0xff_ffff)
    checkLong(0x4ff_ffff)
    checkLong(0xfff_ffff)
    checkLong(0x4fff_ffff)
    checkLong(0xffff_ffff)
    checkLong(0x4_ffff_ffff)
    checkLong(0xf_ffff_ffff)
    checkLong(0x4f_ffff_ffff)
    checkLong(0xff_ffff_ffff)
    checkLong(0x4ff_ffff_ffff)
    checkLong(0xfff_ffff_ffff)
    checkLong(0x4fff_ffff_ffff)
    checkLong(0xffff_ffff_ffff)
    checkLong(0x4_ffff_ffff_ffff)
    checkLong(0xf_ffff_ffff_ffff)
    checkLong(0x4f_ffff_ffff_ffff)
    checkLong(0xff_ffff_ffff_ffff)
    checkLong(0x4ff_ffff_ffff_ffff)
    checkLong(0xfff_ffff_ffff_ffff)
    checkLong(0x4fff_ffff_ffff_ffff)
    checkLong(0x5fff_ffff_ffff_ffff)
    checkLong(0x6fff_ffff_ffff_ffff)
    checkLong(0x7fff_ffff_ffff_ffff)
    checkLong(0x1111_1111_1111_1111)
    checkLong(0x2222_2222_2222_2222)
    checkLong(0x3333_3333_3333_3333)
    checkLong(0x5555_5555_5555_5555)
  }

  @Test
  fun varint32() {
    checkInt(0.inv())
    checkInt(0x0)
    checkInt(0x1)
    checkInt(0x80)
    checkInt(0xff)
    checkInt(0x4ff)
    checkInt(0xfff)
    checkInt(0x4fff)
    checkInt(0xffff)
    checkInt(0x4_ffff)
    checkInt(0xf_ffff)
    checkInt(0x4f_ffff)
    checkInt(0xff_ffff)
    checkInt(0x4ff_ffff)
    checkInt(0xfff_ffff)
    checkInt(0x4fff_ffff)
  }

  @Test
  fun utf8String() {
    checkUtf8String("")
    checkUtf8String("test")
    checkUtf8String("привет")
    checkUtf8String("こんにちは")
    checkUtf8String("東京都")
    checkUtf8String("""👨‍👩‍👧‍👦""")
    checkUtf8String("""t東e京s💞t都""")
  }
}

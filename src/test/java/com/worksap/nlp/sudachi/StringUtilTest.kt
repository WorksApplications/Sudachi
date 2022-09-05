/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StringUtilTest {
  @Test
  fun readAllBytes() {
    val resource = javaClass.getResource("/char.def")
    val buf = StringUtil.readAllBytes(resource)
    val str = StringUtil.readFully(resource)
    val bytes = str.encodeToByteArray()
    assertEquals(bytes.size, buf.remaining())
    val arr2 = ByteArray(bytes.size)
    buf.get(arr2)
    assertContentEquals(bytes, arr2)
  }

  @Test
  fun countUtf8Bytes() {
    assertEquals(0, StringUtil.countUtf8Bytes(""))
    assertEquals(4, StringUtil.countUtf8Bytes("test"))
    assertEquals(12, StringUtil.countUtf8Bytes("привет"))
    assertEquals(9, StringUtil.countUtf8Bytes("東京都"))
    assertEquals(4, StringUtil.countUtf8Bytes("💞"))
    assertEquals(13, StringUtil.countUtf8Bytes("東京💞都"))
    assertEquals(17, StringUtil.countUtf8Bytes("t東e京s💞t都"))
    // https://emojipedia.org/family-man-woman-girl-boy/
    assertEquals(25, StringUtil.countUtf8Bytes("""👨‍👩‍👧‍👦"""))
  }
  @Test
  fun countUtf8BytesRandomInput() {
    for (iter in 1..1000) {
      val r = Random(5)
      val len = r.nextInt(iter)
      val str =
          generateSequence { r.nextInt(0x15000) }
              .filterNot { Character.isBmpCodePoint(it) && Character.isSurrogate(it.toChar()) }
              .take(len)
              .fold(StringBuilder()) { s, i -> s.appendCodePoint(i) }
              .toString()
      val expected = str.toByteArray().size
      assertEquals(
          expected,
          StringUtil.countUtf8Bytes(str, 0, str.length),
          "failed to count utf8 bytes for iter=$iter, [$str]")
    }
  }

  @Test
  fun invalidParamters() {
    assertFailsWith<IllegalArgumentException> { StringUtil.countUtf8Bytes("", -1, 0) }
    assertFailsWith<IllegalArgumentException> { StringUtil.countUtf8Bytes("", 0, 1) }
    assertFailsWith<IllegalArgumentException> { StringUtil.countUtf8Bytes("test", 0, 6) }
    assertFailsWith<IllegalArgumentException> { StringUtil.countUtf8Bytes("test", 6, 0) }
  }
}

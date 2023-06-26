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

package com.worksap.nlp.sudachi.dictionary.build

import com.worksap.nlp.sudachi.dictionary.StringPtr
import java.nio.CharBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
    val p4 = layout.add("3".repeat(4))
    val p5 = layout.add("4".repeat(1))
    val p6 = layout.add("5".repeat(2))
    val chan = InMemoryChannel()
    layout.write(chan)
    val chars = chan.buffer().asCharBuffer()
    assertEquals("0".repeat(25), chars.read(p1))
    assertEquals("1".repeat(23), chars.read(p2))
    assertEquals("2".repeat(15), chars.read(p3))
    assertEquals("3".repeat(4), chars.read(p4))
    assertEquals("4".repeat(1), chars.read(p5))
    assertEquals("5".repeat(2), chars.read(p6))
    // last two should be placed between 0-s and 1-s
    assertTrue(p5.offset < p2.offset)
    assertTrue(p6.offset < p2.offset)
  }

  @Test
  fun alignmentPlacedPlacementLarge() {
    val layout = WordLayout()
    val ptrs = ArrayList<StringPtr>()
    for (i in 0..499) {
      val char = 500 - i
      val str = char.toChar().toString().repeat(char)
      ptrs.add(layout.add(str))
    }
    val chan = InMemoryChannel()
    layout.write(chan)
    val chars = chan.buffer().asCharBuffer()
    for (i in 0..499) {
      val char = 500 - i
      val expected = char.toChar().toString().repeat(char)
      val actual = chars.read(ptrs[i])
      assertEquals(expected, actual)
    }
  }

  @Test
  fun alignmentPlacedPlacementHoles() {
    val layout = WordLayout()
    val ptrs = ArrayList<StringPtr>()
    for (i in 0..3) {
      val count = 200 - 5 * i
      val str = i.toChar().toString().repeat(count)
      ptrs.add(layout.add(str))
    }
    for (i in 0..20) {
      val count = 21 - i
      val str = (20 + i).toChar().toString().repeat(count)
      ptrs.add(layout.add(str))
    }
    val chan = InMemoryChannel()
    layout.write(chan)
    val chars = chan.buffer().asCharBuffer()
    for (i in 0..3) {
      val count = 200 - 5 * i
      val char = i.toChar()
      val expected = char.toString().repeat(count)
      val actual = chars.read(ptrs[i])
      assertEquals(expected, actual)
    }
    for (i in 0..20) {
      val count = 21 - i
      val char = (20 + i).toChar()
      val expected = char.toString().repeat(count)
      val actual = chars.read(ptrs[4 + i])
      assertEquals(expected, actual)
    }
  }

  @Test
  fun coverage() {
    val layout = WordLayout()
    assertEquals(0, layout.wastedBytes())
    assertEquals(0, layout.numSlots())
    assertNotNull(layout.toString())
  }
}

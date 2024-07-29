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

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CsvLexiconTest {
  @Test
  fun failEntryIsSmall() {
    val clex = CsvLexicon(POSTable())
    (0..18).forEach {
      val data = generateSequence { "a" }.take(it).toList()
      assertFails { clex.parseLine(data) }
    }
  }

  @Test
  fun failEntryHasTooLongString() {
    val clex = CsvLexicon(POSTable())
    val data = "東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*".split(",")
    assertFails {
      val copy = data.toList().toMutableList()
      copy[0] = "a".repeat(DicBuffer.MAX_STRING + 1)
      clex.parseLine(copy)
    }
    assertFails {
      val copy = data.toList().toMutableList()
      copy[4] = "a".repeat(DicBuffer.MAX_STRING + 1)
      clex.parseLine(copy)
    }
    assertFails {
      val copy = data.toList().toMutableList()
      copy[11] = "a".repeat(DicBuffer.MAX_STRING + 1)
      clex.parseLine(copy)
    }
    assertFails {
      val copy = data.toList().toMutableList()
      copy[12] = "a".repeat(DicBuffer.MAX_STRING + 1)
      clex.parseLine(copy)
    }
  }

  @Test
  fun failEmptyHeadword() {
    val clex = CsvLexicon(POSTable())
    val data = ",1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*".split(",")
    assertFails { clex.parseLine(data) }
  }

  @Test
  fun failInvalidSplitting() {
    val clex = CsvLexicon(POSTable())
    assertFails {
      clex.parseLine("a,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,1,*,*,*".split(","))
    }
    assertFails {
      clex.parseLine("a,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,1,*,*".split(","))
    }
  }

  @Test
  @Ignore
  fun failTooManyUnits() {
    val clex = CsvLexicon(POSTable())
    val data = "東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,C,*,*,*,*".split(",")
    assertFails {
      val copy = data.toList().toMutableList()
      copy[15] = (0..256).joinToString("/") { it.toString() }
      clex.parseLine(copy)
    }
    assertFails {
      val copy = data.toList().toMutableList()
      copy[16] = (0..256).joinToString("/") { it.toString() }
      clex.parseLine(copy)
    }
    assertFails {
      val copy = data.toList().toMutableList()
      copy[17] = (0..256).joinToString("/") { it.toString() }
      clex.parseLine(copy)
    }
    assertFails {
      val copy = data.toList().toMutableList()
      copy[18] = (0..256).joinToString("/") { it.toString() }
      clex.parseLine(copy)
    }
  }

  @Test
  fun unescape() {
    assertEquals("test", Unescape.unescape("""test"""))
    assertEquals("\u0000", Unescape.unescape("""\u0000"""))
    assertEquals("a\u0000a", Unescape.unescape("""a\u0000a"""))
    assertEquals("あ", Unescape.unescape("""\u3042"""))
    assertEquals("あ5", Unescape.unescape("""\u30425"""))
    assertEquals("💕", Unescape.unescape("""\u{1f495}"""))
    assertEquals("a💕x", Unescape.unescape("""a\u{1f495}x"""))
    assertEquals("\udbff\udfff", Unescape.unescape("""\u{10ffff}"""))
  }

  @Test
  fun unescapeFails() {
    assertFails { Unescape.unescape("""\u{FFFFFF}""") }
    assertFails { Unescape.unescape("""\u{110000}""") } // 0x10ffff is the largest codepoint
  }
}

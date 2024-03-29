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

package com.worksap.nlp.sudachi.dictionary.build

import com.worksap.nlp.sudachi.dictionary.GrammarImpl
import com.worksap.nlp.sudachi.dictionary.POS
import kotlin.test.assertEquals
import kotlin.test.assertFails
import org.junit.Test

class GrammarTest {
  @Test
  fun singlePos() {
    val cm = ConnectionMatrix()
    Res("test.matrix") { cm.readEntries(it) }
    val pos = POSTable()
    assertEquals(0, pos.getId(POS("a", "b", "c", "d", "e", "f")))
    val outbuf = MemChannel()
    val out = ModelOutput(outbuf)
    pos.writeTo(out)
    cm.writeTo(out)
    val gram = GrammarImpl(outbuf.buffer(), 0)
    assertEquals(gram.getPartOfSpeechString(0), POS("a", "b", "c", "d", "e", "f"))
  }

  @Test
  fun failPosData() {
    val posTable = POSTable()
    repeat(Short.MAX_VALUE.toInt()) {
      val pos = POS("a", "b", "c", "d", "e", it.toString())
      assertEquals(posTable.getId(pos), it.toShort())
    }
    assertFails { posTable.getId(POS("a", "a", "a", "a", "a", "a")) }
  }

  @Test
  fun invalidPos() {
    assertFails { POS() }
    assertFails { POS("1") }
    assertFails { POS("1", "2") }
    assertFails { POS("1", "2", "3") }
    assertFails { POS("1", "2", "3", "4") }
    assertFails { POS("1", "2", "3", "4", "5") }
    assertFails { POS("1", "2", "3", "4", "5", null) }
    assertFails { POS("1", "2", "3", "4", "5", "6", "7") }
    assertFails { POS("1", "2", "3", "4", "5", "6".repeat(POS.MAX_COMPONENT_LENGTH + 1)) }
  }

  @Test
  fun worksWithEnormousPos() {
    val posTable = POSTable()
    val e = "あ".repeat(127)
    repeat(1024) {
      val pos = POS(e, e, e, e, e, it.toString())
      assertEquals(posTable.getId(pos), it.toShort())
    }
    val cm = ConnectionMatrix()
    Res("test.matrix") { cm.readEntries(it) }
    val outbuf = MemChannel()
    val out = ModelOutput(outbuf)
    posTable.writeTo(out)
    cm.writeTo(out)
    val gram = GrammarImpl(outbuf.buffer(), 0)
    assertEquals(gram.partOfSpeechSize, 1024)
    repeat(1024) {
      val pos = POS(e, e, e, e, e, it.toString())
      assertEquals(pos, gram.getPartOfSpeechString(it.toShort()))
    }
  }
}

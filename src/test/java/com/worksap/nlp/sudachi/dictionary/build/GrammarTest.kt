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

import com.worksap.nlp.sudachi.dictionary.Block
import com.worksap.nlp.sudachi.dictionary.Description
import com.worksap.nlp.sudachi.dictionary.GrammarImpl
import com.worksap.nlp.sudachi.dictionary.POS
import kotlin.test.assertEquals
import org.junit.Test

// Grammar (ConnectionMatrix + POSTable) build test
class GrammarTest {
  @Test
  fun singlePos() {
    val cm = ConnectionMatrix()
    Res("test.matrix") { cm.readEntries(it) }

    val pos = POSTable()
    assertEquals(0, pos.getId(POS("a", "b", "c", "d", "e", "f")))

    val outbuf = MemChannel()
    val layout = BlockLayout(outbuf)
    layout.block(Block.POS_TABLE, pos::compile)
    layout.block(Block.CONNECTION_MATRIX, cm::compile)
    val description = Description()
    description.setBlocks(layout.blocks())

    val grammar = GrammarImpl.load(outbuf.buffer(), description)
    assertEquals(grammar.getPartOfSpeechString(0), POS("a", "b", "c", "d", "e", "f"))
  }

  @Test
  fun worksWithEnormousPos() {
    val cm = ConnectionMatrix()
    Res("test.matrix") { cm.readEntries(it) }

    val posTable = POSTable()
    val e = "あ".repeat(127)
    repeat(1024) {
      val pos = POS(e, e, e, e, e, it.toString())
      assertEquals(posTable.getId(pos), it.toShort())
    }

    val outbuf = MemChannel()
    val layout = BlockLayout(outbuf)
    layout.block(Block.POS_TABLE, posTable::compile)
    layout.block(Block.CONNECTION_MATRIX, cm::compile)
    val description = Description()
    description.setBlocks(layout.blocks())

    val grammar = GrammarImpl.load(outbuf.buffer(), description)
    assertEquals(grammar.partOfSpeechSize, 1024)
    repeat(1024) {
      val pos = POS(e, e, e, e, e, it.toString())
      assertEquals(pos, grammar.getPartOfSpeechString(it.toShort()))
    }
  }
}

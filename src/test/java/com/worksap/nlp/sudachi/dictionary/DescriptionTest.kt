/*
 * Copyright (c) 2024 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.TestDictionary
import com.worksap.nlp.sudachi.dictionary.build.MemChannel
import kotlin.test.Test
import kotlin.test.assertEquals

class DescriptionTest {
  @Test
  fun serialization() {
    val d = Description()
    d.blocks = listOf(Description.BlockInfo("test", 5, 15), Description.BlockInfo("test2", 30, 25))
    d.reference = "testref"
    d.comment = "コメント"
    val chan = MemChannel(4096)
    d.save(chan)
    chan.position(0)
    val d2 = Description.load(chan)
    assertEquals(d.comment, d2.comment)
    assertEquals(d.reference, d2.reference)
    assertEquals(d.signature, d2.signature)
    assertEquals(d.blocks.size, d2.blocks.size)
    assertEquals(d.blocks[0].name, d2.blocks[0].name)
    assertEquals(d.blocks[0].start, d2.blocks[0].start)
    assertEquals(d.blocks[0].size, d2.blocks[0].size)
    assertEquals(d.blocks[1].name, d2.blocks[1].name)
    assertEquals(d.blocks[1].start, d2.blocks[1].start)
    assertEquals(d.blocks[1].size, d2.blocks[1].size)
  }

  @Test
  fun getComment() {
    val desc: Description = TestDictionary.systemDict.getDictionaryHeader()
    assertEquals(desc.getComment(), "the system dictionary for the unit tests")
  }
}

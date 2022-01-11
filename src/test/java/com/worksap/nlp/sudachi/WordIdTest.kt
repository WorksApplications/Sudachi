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

package com.worksap.nlp.sudachi

import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals
import org.junit.Test

class WordIdTest {
  @Test
  fun valid() {
    assertEquals(WordId.make(0, 0), 0)
    assertEquals(WordId.make(0, 5), 5)
    assertNotEquals(WordId.make(1, 5), 5)
  }

  @Test
  fun deconstruct() {
    val wid = WordId.make(12, 51612312)
    assertEquals(12, WordId.dic(wid))
    assertEquals(51612312, WordId.word(wid))
  }

  @Test
  fun invalid() {
    assertFails { WordId.make(0, WordId.MAX_WORD_ID + 1) }
    assertFails { WordId.make(WordId.MAX_DIC_ID + 1, 0) }
  }
}

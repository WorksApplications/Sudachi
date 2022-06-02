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

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WordMaskTest {

  @Test
  fun works() {
    (1..65).forEach { i ->
      val mask = WordMask.nth(i)
      assertTrue(WordMask.hasNth(mask, i))
    }
  }

  @Test
  fun addNth() {
    val mask1 = WordMask.addNth(0, 1)
    val mask2 = WordMask.addNth(mask1, 3)
    val mask3 = WordMask.addNth(mask2, 64)
    assertTrue(WordMask.hasNth(mask3, 1))
    assertFalse(WordMask.hasNth(mask3, 2))
    assertTrue(WordMask.hasNth(mask3, 3))
    assertFalse(WordMask.hasNth(mask3, 4))
    assertFalse(WordMask.hasNth(mask3, 63))
    assertTrue(WordMask.hasNth(mask3, 64))
    assertTrue(WordMask.hasNth(mask3, 65))
  }
}

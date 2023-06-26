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

import kotlin.test.Test
import kotlin.test.assertEquals

class AlignTest {
  @Test
  fun works8() {
    assertEquals(0, Align.align(0, 8))
    assertEquals(8, Align.align(1, 8))
    assertEquals(8, Align.align(2, 8))
    assertEquals(8, Align.align(3, 8))
    assertEquals(8, Align.align(4, 8))
    assertEquals(8, Align.align(5, 8))
    assertEquals(8, Align.align(6, 8))
    assertEquals(8, Align.align(7, 8))
    assertEquals(8, Align.align(8, 8))
    assertEquals(16, Align.align(9, 8))
  }

  @Test
  fun works16() {
    assertEquals(0, Align.align(0, 16))
    assertEquals(16, Align.align(1, 16))
    assertEquals(16, Align.align(2, 16))
    assertEquals(16, Align.align(3, 16))
    assertEquals(16, Align.align(4, 16))
    assertEquals(16, Align.align(5, 16))
    assertEquals(16, Align.align(6, 16))
    assertEquals(16, Align.align(7, 16))
    assertEquals(16, Align.align(8, 16))
    assertEquals(16, Align.align(9, 16))
    assertEquals(16, Align.align(16, 16))
    assertEquals(32, Align.align(17, 16))
  }
}

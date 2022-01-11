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

import kotlin.test.Test
import kotlin.test.assertEquals

class ParametersTest {
  @Test
  fun resizeWorks() {
    val params = Parameters(4)
    params.add(1, 1, 1)
    params.add(2, 2, 2)
    val ch = MemChannel()
    val out = ModelOutput(ch)
    params.writeTo(out)
    assertEquals(ch.position(), 12)
    val b = ch.buffer()
    assertEquals(b.short, 1)
    assertEquals(b.short, 1)
    assertEquals(b.short, 1)
    assertEquals(b.short, 2)
    assertEquals(b.short, 2)
    assertEquals(b.short, 2)
    assertEquals(b.remaining(), 0)
  }
}

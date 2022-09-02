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

class StringStorageTest {

  @Test
  fun simple() {
    val strs = StringStorage()
    strs.add("test")
    strs.add("es")
    strs.compile()
    val data = strs.strings
    assertEquals(2, data.size)
    assertEquals(1, data["es"]?.start)
    assertEquals(3, data["es"]?.end)
  }

  @Test
  fun oneChar() {
    val strs = StringStorage()
    strs.add("x")
    strs.add("y")
    strs.compile()
    val data = strs.strings
    assertEquals(2, data.size)
    assertEquals(0, data["x"]?.start)
    assertEquals(1, data["x"]?.end)
    assertEquals(0, data["y"]?.start)
    assertEquals(1, data["y"]?.end)
  }
}

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

package com.worksap.nlp.sudachi

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class StringUtilTest {
  @Test
  fun readAllBytes() {
    val resource = javaClass.getResource("/char.def")
    val buf = StringUtil.readAllBytes(resource)
    val str = StringUtil.readFully(resource)
    val bytes = str.encodeToByteArray()
    assertEquals(bytes.size, buf.remaining())
    val arr2 = ByteArray(bytes.size)
    buf.get(arr2)
    assertContentEquals(bytes, arr2)
  }
}

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

import com.worksap.nlp.sudachi.dictionary.Connection
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.junit.Test

object Res {
  operator fun <R> invoke(name: String, fn: (InputStream) -> R): R {
    Res.javaClass.getResourceAsStream(name).use {
      assertNotNull(it, "resource '$name' did not exist")
      return fn(it)
    }
  }
}

class ConnectionMatrixTest {
  @Test
  fun parse3x3() {
    val cm = ConnectionMatrix()
    assertEquals(9, Res("test.matrix") { cm.readEntries(it) })
    val conn = Connection(cm.compiledNoHeader.asShortBuffer(), 3, 3)
    assertEquals(conn.cost(0, 0), 0)
    assertEquals(conn.cost(1, 1), 4)
    assertEquals(conn.cost(2, 1), 7)
  }

  @Test
  fun invalidHeader() {
    val cm = ConnectionMatrix()
    assertFailsWith<IllegalArgumentException> { cm.readEntries("1".byteInputStream()) }
  }

  @Test
  fun emptyHeader() {
    val cm = ConnectionMatrix()
    assertFailsWith<IllegalArgumentException> { cm.readEntries("".byteInputStream()) }
  }

  @Test
  fun badHeader() {
    val cm = ConnectionMatrix()
    assertFailsWith<IllegalArgumentException> { cm.readEntries("5 a".byteInputStream()) }
  }
}

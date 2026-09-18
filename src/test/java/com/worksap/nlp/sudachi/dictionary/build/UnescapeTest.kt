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
import kotlin.test.assertFails

class UnescapeTest {

  @Test
  fun unescape() {
    assertEquals("test", Unescape.unescape("""test"""))
    assertEquals("\u0000", Unescape.unescape("""\u0000"""))
    assertEquals("a\u0000a", Unescape.unescape("""a\u0000a"""))
    assertEquals("あ", Unescape.unescape("""\u3042"""))
    assertEquals("あ5", Unescape.unescape("""\u30425"""))
    assertEquals("💕", Unescape.unescape("""\u{1f495}"""))
    assertEquals("a💕x", Unescape.unescape("""a\u{1f495}x"""))
    assertEquals("\udbff\udfff", Unescape.unescape("""\u{10ffff}"""))
  }

  @Test
  fun unescapeFails() {
    assertFails { Unescape.unescape("""\u{FFFFFF}""") }
    assertFails { Unescape.unescape("""\u{110000}""") } // 0x10ffff is the largest codepoint
  }
}

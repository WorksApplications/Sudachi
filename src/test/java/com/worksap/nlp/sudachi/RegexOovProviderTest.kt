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
import kotlin.test.assertEquals

class RegexOovProviderTest {
  private val analyzer =
      kotlin.run {
        val cfg = Config.empty()
        cfg.addOovProviderPlugin(RegexOovProvider::class.java)
            .add("regex", """[0-9a-z-]+""")
            .add("cost", 3500)
            .add("leftId", 5)
            .add("rightId", 5)
            .addList("pos", "名詞", "普通名詞", "一般", "*", "*", "*")
        // prepend our OOV configuration to the main configuration
        DictionaryFactory()
            .create(cfg.merge(TestDictionary.user0Cfg(), Config.MergeMode.APPEND))
            .create()
      }

  @Test
  fun noOtherWords() {
    val tokens = analyzer.tokenize("XAG-2F")
    assertEquals(1, tokens.size)
    assertEquals("XAG-2F", tokens[0].surface())
  }

  @Test
  fun hasOtherWords() {
    val tokens = analyzer.tokenize("京都XAG-2F東京")
    assertEquals(3, tokens.size)
    assertEquals("XAG-2F", tokens[1].surface())
  }

  @Test
  fun hasOtherConflictingWords() {
    val tokens = analyzer.tokenize("２つXＡＧ-2F")
    assertEquals(3, tokens.size)
    assertEquals("XＡＧ-2F", tokens[2].surface())
    assertEquals("xag-2f", tokens[2].normalizedForm())
    assertEquals("", tokens[2].readingForm())
  }
}

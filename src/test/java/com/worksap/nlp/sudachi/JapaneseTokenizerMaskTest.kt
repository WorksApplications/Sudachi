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
import kotlin.test.assertIs

class JapaneseTokenizerMaskTest {
  private class CaptureOtherWords : OovProviderPlugin() {
    val otherWords = ArrayList<Pair<Int, Long>>()
    override fun provideOOV(
        inputText: InputText?,
        offset: Int,
        otherWords: Long,
        result: MutableList<LatticeNodeImpl>?
    ): Int {
      this.otherWords.add(offset to otherWords)
      return 0
    }
  }

  @Test
  fun correctMasksWithFirstProvider() {
    val cfg0 = Config.empty()
    cfg0.addOovProviderPlugin(CaptureOtherWords::class.java)
    val cfg = cfg0.merge(TestDictionary.user0Cfg(), Config.MergeMode.APPEND)
    val dic = DictionaryFactory().create(cfg) as JapaneseDictionary
    val tokenizer = dic.create()

    assertIs<CaptureOtherWords>(dic.oovProviderPlugins[0])
    assertIs<SimpleOovProviderPlugin>(dic.oovProviderPlugins[1])

    tokenizer.tokenize("かaiueoか")
    val provider = dic.oovProviderPlugins.first { it is CaptureOtherWords } as CaptureOtherWords
    val otherWords = provider.otherWords
    assertEquals(3, otherWords.size)
    // in this order word masks are empty
    assertEquals(0 to 0L, otherWords[0])
    assertEquals(3 to 0L, otherWords[1])
    assertEquals(8 to 0L, otherWords[2])
  }

  @Test
  fun correctMasksWithSecondProvider() {
    val cfg = TestDictionary.user0Cfg()
    cfg.addOovProviderPlugin(CaptureOtherWords::class.java)
    val dic = DictionaryFactory().create(cfg) as JapaneseDictionary
    val tokenizer = dic.create()

    assertIs<SimpleOovProviderPlugin>(dic.oovProviderPlugins[0])
    assertIs<CaptureOtherWords>(dic.oovProviderPlugins[1])

    tokenizer.tokenize("かaiueoか")
    val provider = dic.oovProviderPlugins.first { it is CaptureOtherWords } as CaptureOtherWords
    val otherWords = provider.otherWords
    assertEquals(3, otherWords.size)
    // in this order word masks are not empty
    assertEquals(0 to WordMask.nth(3), otherWords[0])
    assertEquals(3 to WordMask.nth(5), otherWords[1])
    assertEquals(8 to WordMask.nth(3), otherWords[2])
  }
}

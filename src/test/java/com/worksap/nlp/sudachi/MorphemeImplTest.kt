/*
 * Copyright (c) 2022-2026 Works Applications Co., Ltd.
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
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MorphemeListItemTest {
  @Test
  fun useToString() {
    val dic = TestDictionary.user0()
    // should be split into す/だ/ち, all of them are OOV
    val sudachi = dic.tokenizer().tokenize("すだち")
    // wid of OOV is (0xf, posId)
    assertEquals(
        "MorphemeListItem{begin=0, end=1, surface=す, pos=4/名詞,普通名詞,一般,*,*,*, wid=(15,4)}",
        sudachi[0].toString())
  }

  @Test
  fun userdata() {
    // system
    val sdic = TestDictionary.user0()
    val tokyo = sdic.tokenizer().tokenize("東京")
    assertTrue(tokyo[0].getUserData().isEmpty())

    // oov
    val oovs = sdic.tokenizer().tokenize("すだち")
    assertTrue(oovs[0].getUserData().isEmpty())

    // user with data
    val udic = TestDictionary.user1()
    val sudachi = udic.tokenizer().tokenize("すだち")
    assertEquals("徳島県産", sudachi[0].getUserData())

    // user without data
    val piraru = udic.tokenizer().tokenize("ぴらる")
    assertTrue(piraru[0].getUserData().isEmpty())
  }

  @Test
  fun referencedMorphemes() {
    val dic = TestDictionary.user0()
    val morphemes = dic.tokenizer().tokenize("いっ")
    val m = morphemes[0]

    val normalized = m.normalizedFormMorpheme()
    val dictionary = m.dictionaryFormMorpheme()

    assertEquals("行く", normalized.surface())
    assertEquals("いく", dictionary.surface())
    assertEquals(m.normalizedForm(), normalized.surface())
    assertEquals(m.dictionaryForm(), dictionary.surface())
    assertEquals(0, normalized.begin())
    assertEquals(2, normalized.end())
    assertEquals(0, dictionary.begin())
    assertEquals(2, dictionary.end())

    val kyoto = dic.tokenizer().tokenize("京都")[0]
    assertSame(kyoto, kyoto.normalizedFormMorpheme())
    assertSame(kyoto, kyoto.dictionaryFormMorpheme())
  }
}

/*
 * Copyright (c) 2024 Works Applications Co., Ltd.
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SingleMorphemeImplTest {
  @Test
  fun allMethodsWorks() {
    val dic = TestDictionary.user0()
    val m = dic.lookup("京都")[0]

    assertEquals(0, m.begin())
    assertEquals(2, m.end())
    assertEquals(listOf("名詞", "固有名詞", "地名", "一般", "*", "*"), m.partOfSpeech())
    assertEquals(3, m.partOfSpeechId())
    assertEquals("京都", m.surface())
    assertEquals("キョウト", m.readingForm())
    assertEquals("京都", m.normalizedForm())
    assertEquals("京都", m.dictionaryForm())
    // assertEquals(, m.split(Tokenizer.SplitMode.A)) // in other test
    assertFalse(m.isOOV())
    assertEquals(16, m.getWordId())
    assertEquals(0, m.getDictionaryId())
    assertContentEquals(intArrayOf(1, 5), m.getSynonymGroupIds())
    assertEquals("", m.getUserData())
  }

  @Test
  fun allMethodsWorksOOV() {
    val dic = TestDictionary.user0()
    val m = dic.oovMorpheme(3, "大阪")

    assertEquals(0, m.begin())
    assertEquals(2, m.end())
    assertEquals(listOf("名詞", "固有名詞", "地名", "一般", "*", "*"), m.partOfSpeech())
    assertEquals(3, m.partOfSpeechId())
    assertEquals("大阪", m.surface())
    assertEquals("大阪", m.readingForm())
    assertEquals("大阪", m.normalizedForm())
    assertEquals("大阪", m.dictionaryForm())
    // assertEquals(, m.split(Tokenizer.SplitMode.A)) // in other test
    assertTrue(m.isOOV())
    assertEquals(WordId.makeOov(3), m.getWordId())
    assertEquals(-1, m.getDictionaryId())
    assertEquals(0, m.getSynonymGroupIds().size)
    assertEquals("", m.getUserData())
  }

  @Test
  fun useToString() {
    val dic = TestDictionary.user0()
    val ms = dic.lookup("京都")
    assertEquals(
        "SingleMorphemeImpl{begin=0, end=2, surface=京都, pos=3/名詞,固有名詞,地名,一般,*,*, wid=(0,16)}",
        ms[0].toString())
  }

  @Test
  fun split() {
    val dic = TestDictionary.user0()
    val ms = dic.lookup("東京都")
    assertEquals(0, ms[0].begin())
    assertEquals(3, ms[0].end())
    assertEquals(29, ms[0].getWordId())

    val spl = ms[0].split(Tokenizer.SplitMode.A)
    assertEquals(2, spl.size)
    assertEquals("東京", spl[0].surface())
    assertEquals(0, spl[0].begin())
    assertEquals(2, spl[0].end())
    assertEquals(25, spl[0].getWordId())
    assertEquals("都", spl[1].surface())
    assertEquals(2, spl[1].begin())
    assertEquals(3, spl[1].end())
    assertEquals(46, spl[1].getWordId())
  }

  @Test
  fun splitOOV() {
    val dic = TestDictionary.user0()
    val m = dic.oovMorpheme(0, "東京都")
    assertEquals(0, m.begin())
    assertEquals(3, m.end())
    assertEquals(WordId.makeOov(0), m.getWordId())

    val spl = m.split(Tokenizer.SplitMode.A)
    assertEquals(1, spl.size)
    assertEquals(m, spl[0])
  }
}

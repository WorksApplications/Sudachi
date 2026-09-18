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

package com.worksap.nlp.sudachi.dictionary

import com.worksap.nlp.sudachi.TestDictionary
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DoubleArrayLexiconTest {
  lateinit var lexicon: DoubleArrayLexicon
  lateinit var systemWordIds: Ints

  @BeforeTest
  fun setup() {
    val bytes = TestDictionary.systemDictData.buffer()
    val desc = Description.load(bytes)
    lexicon = DoubleArrayLexicon.load(bytes, desc)

    val wids = Ints(lexicon.size())
    for (ints: Ints in lexicon.getWordIdTable().wordIds()) {
      wids.appendAll(ints)
    }
    wids.sort()
    systemWordIds = wids
  }

  fun getWordId(idx: Int): Int {
    return systemWordIds.get(idx)
  }

  @Test
  fun iterWordIds() {
    assertEquals(44, systemWordIds.length())
    for (i in 0..(systemWordIds.length() - 1)) {
      lexicon.getWordInfo(systemWordIds.get(i))
    }
  }

  @Test
  fun lookup() {
    var iter = lexicon.lookup("東京都".toByteArray(), 0)
    assertEquals(listOf(getWordId(4), 3), iter.next().toList())
    assertEquals(listOf(getWordId(5), 6), iter.next().toList())
    assertEquals(listOf(getWordId(6), 9), iter.next().toList())
    assertFalse(iter.hasNext())

    iter = lexicon.lookup("東京都に".toByteArray(), 9)
    assertEquals(listOf(getWordId(1), 12), iter.next().toList()) // に(接続助詞)
    assertEquals(listOf(getWordId(2), 12), iter.next().toList()) // に(格助詞)
    assertFalse(iter.hasNext())

    iter = lexicon.lookup("あれ".toByteArray(), 0)
    assertFalse(iter.hasNext())
  }

  @Test
  fun parameters() {
    // た
    var param = lexicon.parameters(getWordId(0))
    assertEquals(1, WordParameters.leftId(param))
    assertEquals(1, WordParameters.rightId(param))
    assertEquals(8729, WordParameters.cost(param))

    // 東京都
    param = lexicon.parameters(getWordId(6))
    assertEquals(6, WordParameters.leftId(param))
    assertEquals(8, WordParameters.rightId(param))
    assertEquals(5320, WordParameters.cost(param))

    // 都
    param = lexicon.parameters(getWordId(9))
    assertEquals(8, WordParameters.leftId(param))
    assertEquals(8, WordParameters.rightId(param))
    assertEquals(2914, WordParameters.cost(param))
  }

  @Test
  fun wordInfo() {
    // た
    var wi = lexicon.getWordInfo(getWordId(0))
    assertEquals("た", lexicon.string(0, wi.getHeadword()))
    assertEquals(3, wi.getLength())
    assertEquals(0, wi.getPOSId())
    assertEquals("た", lexicon.string(0, lexicon.getWordInfo(wi.getNormalizedForm()).getHeadword()))
    assertEquals("た", lexicon.string(0, lexicon.getWordInfo(wi.getDictionaryForm()).getHeadword()))
    assertEquals("タ", lexicon.string(0, wi.getReadingForm()))
    assertEquals(listOf(), wi.getAunitSplit().toList())
    assertEquals(listOf(), wi.getBunitSplit().toList())
    assertEquals(listOf(), wi.getWordStructure().toList())

    // 行っ
    wi = lexicon.getWordInfo(getWordId(8))
    assertEquals("行っ", lexicon.string(0, wi.getHeadword()))
    assertEquals("行く", lexicon.string(0, lexicon.getWordInfo(wi.getNormalizedForm()).getHeadword()))
    assertEquals("行く", lexicon.string(0, lexicon.getWordInfo(wi.getDictionaryForm()).getHeadword()))

    // な。な  (phantom normalized form)
    wi = lexicon.getWordInfo(getWordId(42))
    assertEquals("な。な", lexicon.string(0, wi.getHeadword()))
    assertEquals("ナナ", lexicon.string(0, wi.getReadingForm()))
    assertEquals("なな", lexicon.string(0, lexicon.getWordInfo(wi.getNormalizedForm()).getHeadword()))

    // 東京都
    wi = lexicon.getWordInfo(getWordId(6))
    assertEquals("東京都", lexicon.string(0, wi.getHeadword()))
    assertEquals(listOf(getWordId(5), getWordId(10)), wi.getAunitSplit().toList())
    assertEquals(listOf(), wi.getBunitSplit().toList())
    assertEquals(listOf(getWordId(5), getWordId(10)), wi.getWordStructure().toList())
    assertEquals(listOf(), wi.getSynonymGroupIds().toList())
  }

  @Test
  fun userWordInfo() {
    val bytes = TestDictionary.userDict1Data.buffer()
    val desc = Description.load(bytes)
    val userlex = DoubleArrayLexicon.load(bytes, desc)

    // すだち
    val wi = userlex.getWordInfo(34)
    assertEquals("すだち", userlex.string(0, wi.getHeadword()))
    assertEquals(8, wi.getPOSId())
    assertEquals("徳島県産", wi.getUserData())
  }

  @Test
  fun wordInfoLong() {
    // 0123456789 * 30
    val wi = lexicon.getWordInfo(getWordId(39))
    val surface = lexicon.string(0, wi.getHeadword())
    assertEquals(300, surface.length)
    assertEquals(300, wi.getLength())
    val normalizedform =
        lexicon.string(0, lexicon.getWordInfo(wi.getNormalizedForm()).getHeadword())
    assertEquals(300, normalizedform.length)
    val dictionaryform =
        lexicon.string(0, lexicon.getWordInfo(wi.getDictionaryForm()).getHeadword())
    assertEquals(300, dictionaryform.length)
    val readingform = lexicon.string(0, wi.getReadingForm())
    assertEquals(570, readingform.length)
  }

  @Test
  fun size() {
    assertEquals(44, lexicon.size())
  }

  @Test fun string() {}
}

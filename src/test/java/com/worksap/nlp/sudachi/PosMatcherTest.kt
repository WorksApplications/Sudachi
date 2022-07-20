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

import com.worksap.nlp.sudachi.dictionary.POS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PosMatcherTest {

  private val dic = DictionaryFactory().create(TestDictionary.user2Cfg()) as JapaneseDictionary

  @Test
  fun basic() {
    val nouns = dic.posMatcher(PartialPOS("名詞"))
    val morphs = dic.create().tokenize("京都に行った")
    assertEquals(4, morphs.size)
    assertTrue(nouns.test(morphs[0]))
    assertFalse(nouns.test(morphs[1]))
    assertFalse(nouns.test(morphs[2]))
    assertFalse(nouns.test(morphs[3]))
  }

  @Test
  fun userDic() {
    val filter = dic.posMatcher { it[3] == "ミカン科" }
    val morphs = dic.create().tokenize("すだちにかぼす")
    assertEquals(3, morphs.size)
    assertTrue(filter.test(morphs[0]))
    assertFalse(filter.test(morphs[1]))
    assertTrue(filter.test(morphs[2]))
  }

  @Test
  fun union() {
    val f1 = dic.posMatcher { it[5] == "スダチ" }
    val f2 = dic.posMatcher { it[5] == "カボス" }
    val filter = f1.union(f2)
    val morphs = dic.create().tokenize("すだちにかぼす")
    assertEquals(3, morphs.size)
    assertTrue(filter.test(morphs[0]))
    assertFalse(filter.test(morphs[1]))
    assertTrue(filter.test(morphs[2]))
  }

  @Test
  fun intersection() {
    val f1 = dic.posMatcher { it[5] == "終止形-一般" }
    val f2 = dic.posMatcher { it[0] == "動詞" }
    val filter = f1.intersection(f2)
    val morphs = dic.create().tokenize("いった東京行く")
    assertEquals(4, morphs.size)
    assertFalse(filter.test(morphs[0]))
    assertFalse(filter.test(morphs[1]))
    assertFalse(filter.test(morphs[2]))
    assertTrue(filter.test(morphs[3]))
  }

  @Test
  fun invert() {
    val filter = dic.posMatcher { it[3] == "ミカン科" }.invert()
    val morphs = dic.create().tokenize("すだちにかぼす")
    assertEquals(3, morphs.size)
    assertFalse(filter.test(morphs[0]))
    assertTrue(filter.test(morphs[1]))
    assertFalse(filter.test(morphs[2]))
  }

  @Test
  fun iterator() {
    val filter = dic.posMatcher { it[3] == "ミカン科" }
    val posList = filter.toList()
    assertEquals(
        listOf(
            POS(*"被子植物門,双子葉植物綱,ムクロジ目,ミカン科,ミカン属,スダチ".split(",").toTypedArray()),
            POS(*"被子植物門,双子葉植物綱,ムクロジ目,ミカン科,ミカン属,カボス".split(",").toTypedArray())),
        posList)
  }
}

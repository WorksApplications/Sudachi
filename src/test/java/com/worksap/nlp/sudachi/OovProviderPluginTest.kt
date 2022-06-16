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

import com.worksap.nlp.sudachi.OovProviderPlugin.*
import com.worksap.nlp.sudachi.dictionary.Grammar
import com.worksap.nlp.sudachi.dictionary.POS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

class OovProviderPluginTest {
  class FakeOovProvider : OovProviderPlugin() {
    override fun provideOOV(
        inputText: InputText?,
        offset: Int,
        otherWords: Long,
        result: MutableList<LatticeNodeImpl>?
    ): Int {
      return 0
    }

    override fun setUp(grammar: Grammar?) {
      val kind = settings.getString(USER_POS, USER_POS_FORBID)
      val pos = POS(settings.getStringList("pos"))
      posId = posIdOf(grammar, pos, kind)
    }

    var posId: Short = -1
  }

  @Test
  fun posIdOfWorks() {
    val cfg = TestDictionary.user0Cfg()
    cfg.addOovProviderPlugin(FakeOovProvider::class.java)
        .addList("pos", "名詞", "普通名詞", "一般", "*", "*", "*")
    val inst = DictionaryFactory().create(cfg) as JapaneseDictionary
    val plugin = assertIs<FakeOovProvider>(inst.oovProviderPlugins.last())
    assertEquals(4, plugin.posId)
  }

  @Test
  fun posIdOfWorksNewPos() {
    val cfg = TestDictionary.user0Cfg()
    cfg.addOovProviderPlugin(FakeOovProvider::class.java)
        .addList("pos", "名詞", "普通名詞", "一般", "*", "*", "new")
        .add(USER_POS, USER_POS_ALLOW)
    val inst = DictionaryFactory().create(cfg) as JapaneseDictionary
    val plugin = assertIs<FakeOovProvider>(inst.oovProviderPlugins.last())
    assertEquals(8, plugin.posId)
  }

  @Test
  fun failInvalidName() {
    val cfg = TestDictionary.user0Cfg()
    cfg.addOovProviderPlugin(FakeOovProvider::class.java)
        .addList("pos", "名詞", "普通名詞", "一般", "*", "*", "*")
        .add(USER_POS, "test")
    assertFails { DictionaryFactory().create(cfg) }
  }

  @Test
  fun failInvalidPos() {
    val cfg = TestDictionary.user0Cfg()
    cfg.addOovProviderPlugin(FakeOovProvider::class.java)
        .addList("pos", "名詞", "普通名詞", "一般", "*", "*", "test")
    assertFails { DictionaryFactory().create(cfg) }
  }

  @Test
  fun doubleRegisterReturnsSamePosId() {
    val cfg = TestDictionary.user0Cfg()
    cfg.addOovProviderPlugin(FakeOovProvider::class.java)
        .addList("pos", "名詞", "普通名詞", "一般", "*", "*", "new")
        .add(USER_POS, USER_POS_ALLOW)
    cfg.addOovProviderPlugin(FakeOovProvider::class.java)
        .addList("pos", "名詞", "普通名詞", "一般", "*", "*", "new")
        .add(USER_POS, USER_POS_ALLOW)
    val inst = DictionaryFactory().create(cfg) as JapaneseDictionary
    val oovPlugins = inst.oovProviderPlugins
    val p1 = assertIs<FakeOovProvider>(oovPlugins[oovPlugins.size - 2])
    assertEquals(8, p1.posId)
    val p2 = assertIs<FakeOovProvider>(oovPlugins[oovPlugins.size - 1])
    assertEquals(8, p2.posId)
  }

  @Test
  fun posIdOfWorksNewPosWithUserDict() {
    val cfg = TestDictionary.user2Cfg()
    cfg.addOovProviderPlugin(FakeOovProvider::class.java)
        .addList("pos", "名詞", "普通名詞", "一般", "*", "*", "new")
        .add(USER_POS, USER_POS_ALLOW)
    val dict = DictionaryFactory().create(cfg) as JapaneseDictionary
    val plugin = assertIs<FakeOovProvider>(dict.oovProviderPlugins.last())
    assertEquals(8, plugin.posId)
    val tokinzer = dict.create()
    val tokens = tokinzer.tokenize("すだちかぼす")
    assertEquals("スダチ", tokens[0].partOfSpeech()[5])
    assertEquals("カボス", tokens[1].partOfSpeech()[5])
  }
}

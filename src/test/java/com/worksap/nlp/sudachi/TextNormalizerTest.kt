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

import com.worksap.nlp.sudachi.dictionary.CharacterCategory
import com.worksap.nlp.sudachi.dictionary.GrammarImpl
import kotlin.test.*

class TextNormalizerTest {

  private val dic =
      DictionaryFactory()
          .create(TestDictionary.user2Cfg().characterDefinition(CharacterCategory.loadDefault()))
          as JapaneseDictionary

  @Test
  fun instantiation() {
    TextNormalizer.fromDictionary(dic)
    TextNormalizer(dic.getGrammar())
    TextNormalizer(dic.getGrammar(), dic.inputTextPlugins)
    TextNormalizer.defaultTextNormalizer()
  }

  @Test
  fun failToInstantiateWithoutCharCategory() {
    val grammar = GrammarImpl()
    assertFails { TextNormalizer(grammar) }
  }

  @Test
  fun normalizeText() {
    val tn = TextNormalizer.defaultTextNormalizer()

    // from DefaultInputTextPlugin test
    assertEquals("âbγд(株)ガヴ⼼ⅲ", tn.normalize("ÂＢΓД㈱ｶﾞウ゛⼼Ⅲ"))
  }

  @Test
  fun normalizeTextWithDefaultConfig() {
    // will use default config, which has InputTextPlugins of
    // [Default, ProlongedSoundMark, IgnoreYomigana]
    val tn = TextNormalizer.fromDictionary(dic)
    print(dic.inputTextPlugins)

    assertEquals("âbγд(株)ガヴ⼼ⅲ", tn.normalize("ÂＢΓД㈱ｶﾞウ゛⼼Ⅲ")) // default
    assertEquals("うわーい", tn.normalize("うわーーーい")) // prolonged sound mark
    assertEquals("小鳥遊", tn.normalize("小鳥遊（タカナシ）")) // ignore yomigana
  }
}

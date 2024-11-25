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

import com.worksap.nlp.sudachi.dictionary.BinaryDictionary
import com.worksap.nlp.sudachi.dictionary.build.DicBuilder
import com.worksap.nlp.sudachi.dictionary.build.MemChannel
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JapaneseDictionaryTest {
  lateinit var tempDir: Path
  lateinit var dict: Dictionary

  @BeforeTest
  fun setup() {
    dict = TestDictionary.user0()
    tempDir = createTempDirectory()
  }

  @AfterTest
  fun tearDown() {
    dict.close()
  }

  fun setupMinimumConfig(): Config {
    val config = Config.fromClasspath("sudachi_minimum.json")
    config.systemDictionary(TestDictionary.systemDict)
    return config
  }

  @Test
  @Deprecated("testing deprecated Dictionary.create")
  fun create() {
    assertIs<Tokenizer>(dict.create())
  }

  @Test
  fun createTokenizer() {
    assertIs<Tokenizer>(dict.tokenizer())
  }

  @Test
  fun getPartOfSpeechSize() {
    assertEquals(8, dict.getPartOfSpeechSize())
  }

  @Test
  fun getPartOfSpeechString() {
    val pos = dict.getPartOfSpeechString(0)
    assertNotNull(pos)
    assertEquals("助動詞", pos.get(0))
  }

  @Test
  fun instantiateConfigWithoutCharDef() {
    val config = setupMinimumConfig()
    val jdict = DictionaryFactory().create(config)

    assertNotNull(jdict)
    assertNotNull(jdict.tokenizer())
    jdict.close()
  }

  @Test
  fun throwExceptionOnDictionaryUsageAfterClose() {
    val config = setupMinimumConfig()
    val jdict = DictionaryFactory().create(config)
    jdict.close()

    assertFailsWith(IllegalStateException::class) { jdict.tokenizer() }
  }

  @Test
  fun throwExceptionOnTokenizerUsageAfterClose() {
    val config = setupMinimumConfig()
    val jdict = DictionaryFactory().create(config)
    val tok = jdict.tokenizer()
    jdict.close()

    assertFailsWith(IllegalStateException::class) { tok.tokenize("a") }
  }

  @Test
  fun lookupEntries() {
    // nothing
    val nothing = dict.lookup("存在しない語")
    assertTrue(nothing.isEmpty())

    // system
    val tokyo = dict.lookup("東京都")
    assertEquals(1, tokyo.size)
    assertEquals("トウキョウト", tokyo[0].readingForm())

    // user
    val sudachi = TestDictionary.user1().lookup("すだち")
    assertEquals(1, sudachi.size)
    assertEquals("徳島県産", sudachi[0].getUserData())

    // will be normalized
    val norm = dict.lookup("特A")
    assertEquals(1, norm.size)
    assertEquals("特A", norm[0].normalizedForm())

    // inputTextPlugin
    val yomi = dict.lookup("京都（キョウト）")
    assertEquals(1, yomi.size)
    assertEquals("京都", yomi[0].normalizedForm())
  }

  @Test
  fun lookupMultipleEntries() {
    val lex_system = tempDir.resolve("lex_system.csv")
    lex_system
        .toFile()
        .writeText(
            """IndexForm,LeftId,RightId,Cost,headword,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
abc,1,1,4675,ABC,名詞,普通名詞,一般,*,*,*,エービーシー,,,,,
abc,1,1,4675,abc,名詞,普通名詞,一般,*,*,*,エービーシー,,,,,""")
    val mc_sys = MemChannel()
    DicBuilder.system()
        .matrix(javaClass.getResource("/dict/matrix.def"))
        .lexicon(lex_system)
        .build(mc_sys)
    val sdict = BinaryDictionary.loadSystem(mc_sys.buffer())

    val lex_user = tempDir.resolve("lex_user.csv")
    lex_user
        .toFile()
        .writeText(
            """IndexForm,LeftId,RightId,Cost,headword,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
abc,1,1,4675,aBc,名詞,普通名詞,一般,*,*,*,エービーシー,,,,,
abc,1,1,4675,AbC,名詞,普通名詞,一般,*,*,*,エービーシー,,,,,""")
    val mc_user = MemChannel()
    DicBuilder.user().system(sdict).lexicon(lex_user).build(mc_user)
    val udict = BinaryDictionary.loadUser(mc_user.buffer())

    val cfg =
        Config.defaultConfig()
            .clearUserDictionaries()
            .systemDictionary(sdict)
            .addUserDictionary(udict)
    val mdict = DictionaryFactory().create(cfg)

    val found = mdict.lookup("ABC")
    assertEquals(4, found.size)

    // should be in the order in the dict
    assertEquals("aBc", found.get(0).surface())
    assertEquals("AbC", found.get(1).surface())
    assertEquals("ABC", found.get(2).surface())
    assertEquals("abc", found.get(3).surface())
  }

  @Test
  fun oovMorpheme() {
    val m1 = dict.oovMorpheme(1, "OOV")
    assertEquals(0, m1.begin())
    assertEquals(3, m1.end())
    assertEquals(1, m1.partOfSpeechId())
    assertEquals("OOV", m1.surface())
    assertEquals("OOV", m1.readingForm())
    assertEquals("OOV", m1.normalizedForm())
    assertEquals("OOV", m1.dictionaryForm())
    assertTrue(m1.isOOV())
    assertEquals(WordId.makeOov(1), m1.getWordId())
    assertEquals(-1, m1.getDictionaryId())
    assertEquals(0, m1.getSynonymGroupIds().size)
    assertEquals("", m1.getUserData())

    val m2 = dict.oovMorpheme(2, "OOVs", "OOVr", "OOVn", "OOVd")
    assertEquals(0, m2.begin())
    assertEquals(4, m2.end())
    assertEquals(2, m2.partOfSpeechId())
    assertEquals("OOVs", m2.surface())
    assertEquals("OOVr", m2.readingForm())
    assertEquals("OOVn", m2.normalizedForm())
    assertEquals("OOVd", m2.dictionaryForm())
  }
}

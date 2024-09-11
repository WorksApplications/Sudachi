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

import com.worksap.nlp.sudachi.WordId
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DictionaryBuilderTest {
  lateinit var tempDir: Path

  @BeforeTest
  fun setup() {
    tempDir = createTempDirectory()
  }

  @Test
  fun buildSystemDictCommandline() {
    // build and load
    val outputFile = tempDir.resolve("test.dic")
    val matrixFile = tempDir.resolve("matrix.def")
    val inputFile = tempDir.resolve("lex.csv")

    matrixFile.toFile().writeText("1 1\n0 0 200\n")
    inputFile
        .toFile()
        .writeText(
            """東京都,0,0,100,東京都,名詞,固有名詞,地名,一般,*,*,ヒガシキョウト,東京都,,B,"東,名詞,普通名詞,一般,*,*,*,ヒガシ/京都,名詞,固有名詞,地名,一般,*,*,キョウト",,"東,名詞,普通名詞,一般,*,*,*,ヒガシ/京都,名詞,固有名詞,地名,一般,*,*,キョウト",1/2
東,-1,-1,200,東,名詞,普通名詞,一般,*,*,*,ヒガシ,ひがし,,A,,,,
京都,0,0,300,京都,名詞,固有名詞,地名,一般,*,*,キョウト,京都,,A,,,,""")
    val wordIds = listOf(4, 10, 14, 18) // 3 + phantom entry (ひがし)

    DictionaryBuilder.main(
        arrayOf(
            "-o",
            outputFile.toString(),
            "-m",
            matrixFile.toString(),
            "-d",
            "test",
            inputFile.toString()))

    val dictionary = BinaryDictionary(outputFile.toString())

    // header
    val header = dictionary.getDictionaryHeader()
    assertTrue(header.isSystemDictionary())
    assertEquals("test", header.getComment())

    // grammar
    val grammar = dictionary.getGrammar()
    assertEquals(2, grammar.getPartOfSpeechSize())
    assertEquals(POS("名詞", "固有名詞", "地名", "一般", "*", "*"), grammar.getPartOfSpeechString(0))
    assertEquals(POS("名詞", "普通名詞", "一般", "*", "*", "*"), grammar.getPartOfSpeechString(1))
    assertEquals(200, grammar.getConnectCost(0, 0))

    // lexicon
    val lexicon = dictionary.getLexicon()
    assertEquals(3, lexicon.size())

    // first entry
    var wordId = wordIds[0]
    var params = lexicon.parameters(wordId)
    assertEquals(0, WordParameters.leftId(params))
    assertEquals(100, WordParameters.cost(params))
    var wi = lexicon.getWordInfo(wordId)
    assertEquals("東京都", lexicon.string(0, wi.getSurface()))
    assertEquals("ヒガシキョウト", lexicon.string(0, wi.getReadingForm()))
    assertEquals(WordId.make(0, wordId), wi.getNormalizedForm())
    assertEquals(WordId.make(0, wordId), wi.getDictionaryForm())
    assertEquals(0, wi.getPOSId())
    assertEquals(listOf(wordIds[1], wordIds[2]), wi.getAunitSplit().toList())
    assertEquals(0, wi.getBunitSplit().size)
    assertEquals(listOf(1, 2), wi.getSynonymGroupIds().toList())
    var bs = "東京都".toByteArray()
    var itr = lexicon.lookup(bs, 0)
    assertTrue(itr.hasNext())
    assertEquals(listOf(wordId, bs.size), itr.next().toList())
    assertFalse(itr.hasNext())

    // second entry
    wordId = wordIds[1]
    params = lexicon.parameters(wordId)
    assertEquals(-1, WordParameters.leftId(params))
    assertEquals(200, WordParameters.cost(params))
    wi = lexicon.getWordInfo(wordId)
    assertEquals("東", lexicon.string(0, wi.getSurface()))
    assertEquals("ヒガシ", lexicon.string(0, wi.getReadingForm()))
    assertEquals(WordId.make(0, wordIds[3]), wi.getNormalizedForm())
    assertEquals(WordId.make(0, wordId), wi.getDictionaryForm())
    assertEquals(1, wi.getPOSId())
    assertEquals(0, wi.getAunitSplit().size)
    assertEquals(0, wi.getBunitSplit().size)
    assertEquals(0, wi.getSynonymGroupIds().size)
    itr = lexicon.lookup("東".toByteArray(), 0)
    assertFalse(itr.hasNext())
  }

  @Test
  fun buildSystemDictCommandlineWithPos() {
    // build and load
    val outputFile = tempDir.resolve("test.dic")
    val matrixFile = tempDir.resolve("matrix.def")
    val posFile = tempDir.resolve("pos.csv")
    val inputFile = tempDir.resolve("lex.csv")

    matrixFile.toFile().writeText("1 1\n0 0 200\n")
    posFile
        .toFile()
        .writeText("pos1,pos2,pos3,pos4,pos5,pos6\n名詞,普通名詞,一般,*,*,*\n名詞,固有名詞,地名,一般,*,*\n")
    inputFile
        .toFile()
        .writeText(
            """Surface,leftId,rightId,cost,writing,posId,readingform,normalizedform,dictionaryform,mode,splitA,splitB,wordstructure,synonymgroups
東京都,0,0,100,東京都,1,ヒガシキョウト,東京都,,B,"東,名詞,普通名詞,一般,*,*,*,ヒガシ/京都,1,キョウト",,"東,名詞,普通名詞,一般,*,*,*,ヒガシ/京都,1,キョウト",1/2
東,-1,-1,200,東,0,ヒガシ,ひがし,,A,,,,
京都,0,0,300,京都,1,キョウト,京都,,A,,,,""")
    val wordIds = listOf(4, 10, 14, 18) // 3 + phantom entry (ひがし)

    DictionaryBuilder.main(
        arrayOf(
            "-o",
            outputFile.toString(),
            "-m",
            matrixFile.toString(),
            "-p",
            posFile.toString(),
            "-d",
            "test",
            inputFile.toString()))

    val dictionary = BinaryDictionary(outputFile.toString())

    // grammar
    val grammar = dictionary.getGrammar()
    assertEquals(2, grammar.getPartOfSpeechSize())
    assertEquals(POS("名詞", "普通名詞", "一般", "*", "*", "*"), grammar.getPartOfSpeechString(0))
    assertEquals(POS("名詞", "固有名詞", "地名", "一般", "*", "*"), grammar.getPartOfSpeechString(1))
    assertEquals(200, grammar.getConnectCost(0, 0))

    // lexicon
    val lexicon = dictionary.getLexicon()
    assertEquals(3, lexicon.size())

    // first entry
    var wordId = wordIds[0]
    var params = lexicon.parameters(wordId)
    assertEquals(0, WordParameters.leftId(params))
    assertEquals(100, WordParameters.cost(params))
    var wi = lexicon.getWordInfo(wordId)
    assertEquals("東京都", lexicon.string(0, wi.getSurface()))
    assertEquals("ヒガシキョウト", lexicon.string(0, wi.getReadingForm()))
    assertEquals(WordId.make(0, wordId), wi.getNormalizedForm())
    assertEquals(WordId.make(0, wordId), wi.getDictionaryForm())
    assertEquals(1, wi.getPOSId())
    assertEquals(listOf(wordIds[1], wordIds[2]), wi.getAunitSplit().toList())
    assertEquals(0, wi.getBunitSplit().size)
    assertEquals(listOf(1, 2), wi.getSynonymGroupIds().toList())
    var bs = "東京都".toByteArray()
    var itr = lexicon.lookup(bs, 0)
    assertTrue(itr.hasNext())
    assertEquals(listOf(wordId, bs.size), itr.next().toList())
    assertFalse(itr.hasNext())
  }
}

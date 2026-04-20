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

package com.worksap.nlp.sudachi.dictionary

import com.worksap.nlp.sudachi.TestDictionary
import com.worksap.nlp.sudachi.Utils
import com.worksap.nlp.sudachi.dictionary.build.DicBuilder
import com.worksap.nlp.sudachi.dictionary.build.MemChannel
import com.worksap.nlp.sudachi.dictionary.build.Progress
import com.worksap.nlp.sudachi.res
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Path
import java.util.Arrays
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DictionaryPrinterTest {
  lateinit var tempDir: Path

  @BeforeTest
  fun setup() {
    tempDir = createTempDirectory()
    TestDictionary.systemDictData.writeData(tempDir.resolve("system.dic"))
    TestDictionary.userDict1Data.writeData(tempDir.resolve("user.dic"))
    Utils.copyResource(tempDir, "/unk.def")
  }

  fun printDictionary(
      output: OutputStream,
      filename: String,
      system: BinaryDictionary? = null,
      posMode: DictionaryPrinter.POSMode = DictionaryPrinter.POSMode.DEFAULT,
      wordRefMode: DictionaryPrinter.WordRefMode = DictionaryPrinter.WordRefMode.DEFAULT
  ) {
    val ps = PrintStream(output)
    val filepath = tempDir.resolve(filename).toString()
    val dict = BinaryDictionary(filepath)
    val printer = DictionaryPrinter(ps, dict, system, posMode, wordRefMode)
    printer.setProgress(Progress.NOOP) // suppress progress
    printer.printDictionary()
    dict.close()
  }

  fun wordInfoString(lex: DoubleArrayLexicon, wordId: Int): String {
    val wi = lex.getWordInfo(wordId)
    return "${wordId}, ${lex.string(0, wi.getHeadword())}, ${wi.getLength()}, ${wi.getPOSId()}, ${wi.getNormalizedForm()}, ${wi.getDictionaryForm()}, ${lex.string(0, wi.getReadingForm())}, ${Arrays.toString(wi.getAunitSplit())}, ${Arrays.toString(wi.getBunitSplit())}, ${Arrays.toString(wi.getCunitSplit())}, ${Arrays.toString(wi.getWordStructure())}, ${Arrays.toString(wi.getSynonymGroupIds())}, ${wi.getUserData()}"
  }

  @Test
  fun printSystemDict() {
    val output = ByteArrayOutputStream()
    printDictionary(output, "system.dic")
    val lines = output.toString().split(System.lineSeparator())

    assertEquals(46, lines.size) // header + entries + trailing new line
    assertEquals(
        "INDEX_FORM,LEFT_ID,RIGHT_ID,COST,HEADWORD,POS1,POS2,POS3,POS4,POS5,POS6,READING_FORM,NORMALIZED_FORM,DICTIONARY_FORM,SPLIT_A,SPLIT_B,SPLIT_C,WORD_STRUCTURE,SYNONYM_GROUPS,USER_DATA,REFERENCE_ID",
        lines[0])
    assertEquals("た,1,1,8729,,助動詞,*,*,*,助動詞-タ,終止形-一般,タ,,,,,,,,,", lines[1])
    assertEquals("に,2,2,11406,,助詞,接続助詞,*,*,*,*,ニ,,,,,,,,,", lines[2])
    assertEquals(
        "東京都,6,8,5320,,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,\"東京,名詞,固有名詞,地名,一般,*,*,トウキョウ/都,名詞,普通名詞,一般,*,*,*,ト,to-2\",,,\"東京,名詞,固有名詞,地名,一般,*,*,トウキョウ/都,名詞,普通名詞,一般,*,*,*,ト,to-2\",,,",
        lines[7])
    assertEquals("特a,8,8,2914,特A,名詞,普通名詞,一般,*,*,*,トクエー,,,,,,,,,", lines[41])
  }

  @Test
  fun printSystemDictPosIdColumn() {
    val output = ByteArrayOutputStream()
    printDictionary(output, "system.dic", posMode = DictionaryPrinter.POSMode.ID)
    val lines = output.toString().split(System.lineSeparator())

    assertEquals(46, lines.size) // header + entries + trailing new line
    assertEquals(
        "INDEX_FORM,LEFT_ID,RIGHT_ID,COST,HEADWORD,POS_ID,READING_FORM,NORMALIZED_FORM,DICTIONARY_FORM,SPLIT_A,SPLIT_B,SPLIT_C,WORD_STRUCTURE,SYNONYM_GROUPS,USER_DATA,REFERENCE_ID",
        lines[0])
    assertEquals("た,1,1,8729,,0,タ,,,,,,,,,", lines[1])
    assertEquals("に,2,2,11406,,1,ニ,,,,,,,,,", lines[2])
  }

  @Test
  fun printSystemDictBothPosColumn() {
    val output = ByteArrayOutputStream()
    printDictionary(output, "system.dic", posMode = DictionaryPrinter.POSMode.BOTH)
    val lines = output.toString().split(System.lineSeparator())

    assertEquals(46, lines.size) // header + entries + trailing new line
    assertEquals(
        "INDEX_FORM,LEFT_ID,RIGHT_ID,COST,HEADWORD,POS_ID,POS1,POS2,POS3,POS4,POS5,POS6,READING_FORM,NORMALIZED_FORM,DICTIONARY_FORM,SPLIT_A,SPLIT_B,SPLIT_C,WORD_STRUCTURE,SYNONYM_GROUPS,USER_DATA,REFERENCE_ID",
        lines[0])
    assertEquals("た,1,1,8729,,0,助動詞,*,*,*,助動詞-タ,終止形-一般,タ,,,,,,,,,", lines[1])
    assertEquals("に,2,2,11406,,1,助詞,接続助詞,*,*,*,*,ニ,,,,,,,,,", lines[2])
  }

  @Test
  fun printSystemDictPosIdRef() {
    val output = ByteArrayOutputStream()
    printDictionary(output, "system.dic", wordRefMode = DictionaryPrinter.WordRefMode.TRIPLE_ID)
    val lines = output.toString().split(System.lineSeparator())

    assertEquals(46, lines.size) // header + entries + trailing new line
    assertEquals(
        "INDEX_FORM,LEFT_ID,RIGHT_ID,COST,HEADWORD,POS1,POS2,POS3,POS4,POS5,POS6,READING_FORM,NORMALIZED_FORM,DICTIONARY_FORM,SPLIT_A,SPLIT_B,SPLIT_C,WORD_STRUCTURE,SYNONYM_GROUPS,USER_DATA,REFERENCE_ID",
        lines[0])
    assertEquals(
        "東京都,6,8,5320,,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,\"東京,3,トウキョウ/都,4,ト,to-2\",,,\"東京,3,トウキョウ/都,4,ト,to-2\",,,",
        lines[7])
  }

  @Test
  fun printUserDict() {
    val output = ByteArrayOutputStream()
    printDictionary(output, "user.dic", TestDictionary.systemDict)
    val lines = output.toString().split(System.lineSeparator())

    assertEquals(9, lines.size) // header + entries + trailing new line
    assertEquals(
        "INDEX_FORM,LEFT_ID,RIGHT_ID,COST,HEADWORD,POS1,POS2,POS3,POS4,POS5,POS6,READING_FORM,NORMALIZED_FORM,DICTIONARY_FORM,SPLIT_A,SPLIT_B,SPLIT_C,WORD_STRUCTURE,SYNONYM_GROUPS,USER_DATA,REFERENCE_ID",
        lines[0])
    assertEquals(
        "東京府,6,6,2816,,名詞,固有名詞,地名,一般,*,*,トウキョウフ,,,\"東京,名詞,固有名詞,地名,一般,*,*,トウキョウ/府,名詞,普通名詞,一般,*,*,*,フ,fu-2\",,,\"東京,名詞,固有名詞,地名,一般,*,*,トウキョウ/府,名詞,普通名詞,一般,*,*,*,フ,fu-2\",1/3,,",
        lines[5])
    assertEquals("すだち,6,6,2816,,被子植物門,双子葉植物綱,ムクロジ目,ミカン科,ミカン属,スダチ,スダチ,,,,,,,,徳島県産,", lines[7])
  }

  @Test
  fun printUserDictPosIdRef() {
    val output = ByteArrayOutputStream()
    printDictionary(
        output,
        "user.dic",
        TestDictionary.systemDict,
        wordRefMode = DictionaryPrinter.WordRefMode.TRIPLE_ID)
    val lines = output.toString().split(System.lineSeparator())

    assertEquals(9, lines.size) // header + entries + trailing new line
    assertEquals(
        "INDEX_FORM,LEFT_ID,RIGHT_ID,COST,HEADWORD,POS1,POS2,POS3,POS4,POS5,POS6,READING_FORM,NORMALIZED_FORM,DICTIONARY_FORM,SPLIT_A,SPLIT_B,SPLIT_C,WORD_STRUCTURE,SYNONYM_GROUPS,USER_DATA,REFERENCE_ID",
        lines[0])
    assertEquals(
        "東京府,6,6,2816,,名詞,固有名詞,地名,一般,*,*,トウキョウフ,,,\"東京,3,トウキョウ/府,4,フ,fu-2\",,,\"東京,3,トウキョウ/府,4,フ,fu-2\",1/3,,",
        lines[5])
  }

  @Test
  fun printUserDictWithoutSystem() {
    val output = ByteArrayOutputStream()
    assertFails { printDictionary(output, "user.dic", null) }
  }

  @Test
  fun failToPrintInvalidFile() {
    val output = ByteArrayOutputStream()
    assertFails { printDictionary(output, "unk.def", TestDictionary.systemDict) }
  }

  @Test
  fun rebuildSystem() {
    val lexfile = tempDir.resolve("system_lex.csv")
    val output1 = FileOutputStream(lexfile.toFile())
    printDictionary(output1, "system.dic")
    output1.close()

    val dicfile2 = tempDir.resolve("system.dic2")
    val reload = MemChannel()
    DicBuilder.system().matrix(res("/dict/matrix.def")).lexicon(lexfile).build(reload)
    reload.writeData(dicfile2)

    val original = BinaryDictionary(tempDir.resolve("system.dic").toString())
    val rebuilt = BinaryDictionary(tempDir.resolve("system.dic2").toString())

    val headerO = original.getDictionaryHeader()
    val headerR = rebuilt.getDictionaryHeader()
    assertEquals(headerO.getReference(), headerR.getReference())
    assertEquals(headerO.isRuntimeCosts(), headerR.isRuntimeCosts())
    assertEquals(headerO.getNumTotalEntries(), headerR.getNumTotalEntries())
    assertEquals(headerO.getNumIndexedEntries(), headerR.getNumIndexedEntries())

    val grammarO = original.getGrammar()
    val grammarR = rebuilt.getGrammar()
    val posSize = grammarO.getPartOfSpeechSize()
    assertEquals(posSize, grammarR.getPartOfSpeechSize())
    for (i in 0..(posSize - 1)) {
      assertEquals(
          grammarO.getPartOfSpeechString(i.toShort()), grammarR.getPartOfSpeechString(i.toShort()))
    }

    val lexO = original.getLexicon()
    val lexR = rebuilt.getLexicon()
    val wiIterO = lexO.getWordIdTable().wordIds()
    val wiIterR = lexR.getWordIdTable().wordIds()

    while (wiIterO.hasNext()) {
      assertTrue(wiIterR.hasNext())
      val wisO = wiIterO.next()
      val wisR = wiIterR.next()

      assertEquals(wisO, wisR)
      for (i in 0..(wisO.length() - 1)) {
        assertEquals(wordInfoString(lexO, wisO.get(i)), wordInfoString(lexR, wisR.get(i)))
      }
    }
    assertFalse(wiIterR.hasNext())
  }

  @Test
  fun rebuildUser() {
    val lexfile = tempDir.resolve("user_lex.csv")
    val output1 = FileOutputStream(lexfile.toFile())
    printDictionary(output1, "user.dic", TestDictionary.systemDict)
    output1.close()

    val dicfile2 = tempDir.resolve("user.dic2")
    val reload = MemChannel()
    DicBuilder.user().system(TestDictionary.systemDict).lexicon(lexfile).build(reload)
    reload.writeData(dicfile2)

    val original = BinaryDictionary(tempDir.resolve("user.dic").toString())
    val rebuilt = BinaryDictionary(tempDir.resolve("user.dic2").toString())

    val headerO = original.getDictionaryHeader()
    val headerR = rebuilt.getDictionaryHeader()
    assertEquals(headerO.getReference(), headerR.getReference())
    assertEquals(headerO.isRuntimeCosts(), headerR.isRuntimeCosts())
    assertEquals(headerO.getNumTotalEntries(), headerR.getNumTotalEntries())
    assertEquals(headerO.getNumIndexedEntries(), headerR.getNumIndexedEntries())

    val grammarO = original.getGrammar()
    val grammarR = rebuilt.getGrammar()
    val posSize = grammarO.getPartOfSpeechSize()
    assertEquals(posSize, grammarR.getPartOfSpeechSize())
    for (i in 0..(posSize - 1)) {
      assertEquals(
          grammarO.getPartOfSpeechString(i.toShort()), grammarR.getPartOfSpeechString(i.toShort()))
    }

    val lexO = original.getLexicon()
    val lexR = rebuilt.getLexicon()
    val wiIterO = lexO.getWordIdTable().wordIds()
    val wiIterR = lexR.getWordIdTable().wordIds()

    while (wiIterO.hasNext()) {
      assertTrue(wiIterR.hasNext())
      val wisO = wiIterO.next()
      val wisR = wiIterR.next()

      assertEquals(wisO, wisR)
      for (i in 0..(wisO.length() - 1)) {
        assertEquals(wordInfoString(lexO, wisO.get(i)), wordInfoString(lexR, wisR.get(i)))
      }
    }
    assertFalse(wiIterR.hasNext())
  }
}

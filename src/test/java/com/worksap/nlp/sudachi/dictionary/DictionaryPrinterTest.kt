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
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class DictionaryPrinterTest {
  lateinit var tempDir: Path

  @BeforeTest
  fun setup() {
    tempDir = createTempDirectory()
    TestDictionary.systemDictData.writeData(tempDir.resolve("system.dic"))
    TestDictionary.userDict1Data.writeData(tempDir.resolve("user.dic"))
    Utils.copyResource(tempDir, "/unk.def")
  }

  @Test
  fun printSystemDict() {
    val filename = tempDir.resolve("system.dic").toString()
    val output = ByteArrayOutputStream()
    val ps = PrintStream(output)
    DictionaryPrinter.printDictionary(filename, null, ps)
    val lines = output.toString().split(System.lineSeparator())

    assertEquals(41, lines.size) // header + entries + trailing new line
    assertEquals(
        "Surface,LeftId,RightId,Cost,Pos1,Pos2,Pos3,Pos4,Pos5,Pos6,ReadingForm,NormalizedForm,DictionaryForm,SplitA,SplitB,SplitC,WordStructure,SynonymGroups,UserData",
        lines[0])
    assertEquals("た,1,1,8729,助動詞,*,*,*,助動詞-タ,終止形-一般,タ,,,,,,,,", lines[1])
    assertEquals("に,2,2,11406,助詞,接続助詞,*,*,*,*,ニ,,,,,,,,", lines[2])
  }

  @Test
  fun printUserDict() {
    val filename = tempDir.resolve("user.dic").toString()
    val output = ByteArrayOutputStream()
    val ps = PrintStream(output)
    DictionaryPrinter.printDictionary(filename, TestDictionary.systemDict, ps)
    val lines = output.toString().split(System.lineSeparator())

    assertEquals(6, lines.size) // header + entries + trailing new line
    assertEquals(
        "Surface,LeftId,RightId,Cost,Pos1,Pos2,Pos3,Pos4,Pos5,Pos6,ReadingForm,NormalizedForm,DictionaryForm,SplitA,SplitB,SplitC,WordStructure,SynonymGroups,UserData",
        lines[0])
    assertEquals(
        "東京府,6,6,2816,名詞,固有名詞,地名,一般,*,*,トウキョウフ,,,\"東京,名詞,固有名詞,地名,一般,*,*,トウキョウ/府,名詞,普通名詞,一般,*,*,*,フ\",,,\"東京,名詞,固有名詞,地名,一般,*,*,トウキョウ/府,名詞,普通名詞,一般,*,*,*,フ\",1/3,",
        lines[3])
    assertEquals("すだち,6,6,2816,被子植物門,双子葉植物綱,ムクロジ目,ミカン科,ミカン属,スダチ,スダチ,,,,,,,,", lines[4])
  }

  @Test
  fun printUserDictWithoutSystem() {
    val filename = tempDir.resolve("user.dic").toString()
    val output = ByteArrayOutputStream()
    val ps = PrintStream(output)

    assertFails { DictionaryPrinter.printDictionary(filename, null, ps) }
  }

  @Test
  fun failToPrintInvalidFile() {
    val filename = tempDir.resolve("unk.def").toString()
    val output = ByteArrayOutputStream()
    val ps = PrintStream(output)

    assertFails { DictionaryPrinter.printDictionary(filename, TestDictionary.systemDict, ps) }
  }
}

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
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

class DictionaryGrammarPrinterTest {
  @Test
  fun printSystemPOSs() {
    val grammar = TestDictionary.systemDict.getGrammar()
    val output = ByteArrayOutputStream()
    val ps = PrintStream(output)
    DictionaryGrammarPrinter.printPos(grammar, ps)
    val lines = output.toString().split(System.lineSeparator())

    assertEquals(8 + 1, lines.size) // system 8 + last newline

    val cols = lines.get(0).split(",")
    assertEquals(7, cols.size) // id + 6 parts
  }

  @Test
  fun printUserPOSs() {
    val grammar = TestDictionary.userDict1.getGrammar()
    val output = ByteArrayOutputStream()
    val ps = PrintStream(output)
    DictionaryGrammarPrinter.printPos(grammar, ps)
    val lines = output.toString().split(System.lineSeparator())

    assertEquals(1 + 1, lines.size) // user 1 + last newline
  }
}

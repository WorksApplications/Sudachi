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
import java.lang.IllegalArgumentException
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DictionaryHeaderPrinterTest {
  lateinit var tempDir: Path

  @BeforeTest
  fun setup() {
    tempDir = createTempDirectory()
    TestDictionary.systemDictData.writeData(tempDir.resolve("system.dic"))
    TestDictionary.userDict1Data.writeData(tempDir.resolve("user.dic"))
    Utils.copyResource(tempDir, "/unk.def")
  }

  @Test
  fun printSystemHeader() {
    val filename = tempDir.resolve("system.dic").toString()
    val output = ByteArrayOutputStream()
    val ps = PrintStream(output)
    DictionaryHeaderPrinter.printDescription(filename, ps)
    val lines = output.toString().split(System.lineSeparator())

    assertTrue(lines[0].contains(filename))
    assertTrue(lines[1].contains("system"))
    assertTrue(lines[2].startsWith("Creation time: "))
    assertTrue(lines[3].equals("Comment: the system dictionary for the unit tests"))
    assertTrue(lines[4].startsWith("Signature: "))
    assertTrue(lines[5].equals("Reference: "))
  }

  @Test
  fun printUserHeader() {
    val filename = tempDir.resolve("user.dic").toString()
    val output = ByteArrayOutputStream()
    val ps = PrintStream(output)
    DictionaryHeaderPrinter.printDescription(filename, ps)
    val lines = output.toString().split(System.lineSeparator())

    assertTrue(lines[0].contains(filename))
    assertTrue(lines[1].contains("user"))
    assertTrue(lines[2].startsWith("Creation time: "))
    assertTrue(lines[3].equals("Comment: "))
    assertTrue(lines[4].startsWith("Signature: "))
    assertTrue(lines[5].startsWith("Reference: "))
  }

  @Test
  fun failToPrintInvalidHeader() {
    val filename = tempDir.resolve("unk.def").toString()
    val output = ByteArrayOutputStream()
    val ps = PrintStream(output)

    var exceptionThrown = false
    try {
      DictionaryHeaderPrinter.printDescription(filename, ps)
    } catch (e: IllegalArgumentException) {
      exceptionThrown = true
    }
    val lines = output.toString().split(System.lineSeparator())

    assertTrue(lines[0].contains(filename))
    assertTrue(exceptionThrown)
  }
}

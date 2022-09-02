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

package com.worksap.nlp.sudachi.dictionary.build

import com.worksap.nlp.sudachi.cps
import com.worksap.nlp.sudachi.dictionary.CSVParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RawLexiconReaderTest {
  companion object {
    fun csv(name: String): CSVParser {
      val stream = cps(name)
      return CSVParser(stream.reader())
    }
  }

  @Test
  fun legacyCsvWithMinimumFields() {
    val reader = RawLexiconReader(csv("legacy-minimum.csv"), POSTable())
    assertNotNull(reader.nextEntry()).let { e ->
      assertEquals("東京都", e.headword)
      assertEquals("トウキョウト", e.reading)
      assertEquals("5/9", e.wordStructureString)
      assertEquals("", e.cUnitSplitString)
      assertEquals("", e.userData)
    }
    assertNull(reader.nextEntry())
  }

  @Test
  fun legacyCsvWithAllFields() {
    val reader = RawLexiconReader(csv("legacy-full.csv"), POSTable())
    assertNotNull(reader.nextEntry()).let { e ->
      assertEquals("東京都", e.headword)
      assertEquals("トウキョウト", e.reading)
      assertEquals("5/9", e.wordStructureString)
      assertEquals("8/9", e.cUnitSplitString)
      assertEquals("10", e.userData)
    }
    assertNull(reader.nextEntry())
  }

  @Test fun headerCsvMinimumFields() {}

  @Test
  fun headerCsvAllFields() {
    val reader = RawLexiconReader(csv("headers-all.csv"), POSTable())
    assertNotNull(reader.nextEntry()).let { e ->
      assertEquals("東京都", e.headword)
      assertEquals("トウキョウト", e.reading)
      assertEquals("5/9", e.aUnitSplitString)
      assertEquals("5/10", e.bUnitSplitString)
      assertEquals("5/11", e.cUnitSplitString)
      assertEquals("6/7", e.wordStructureString)
      assertEquals("10", e.userData)
    }
    assertNull(reader.nextEntry())
  }
}

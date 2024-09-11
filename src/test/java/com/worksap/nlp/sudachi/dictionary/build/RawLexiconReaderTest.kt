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

import com.worksap.nlp.sudachi.dictionary.Ints
import com.worksap.nlp.sudachi.dictionary.POS
import com.worksap.nlp.sudachi.dictionary.StringPtr
import com.worksap.nlp.sudachi.resStream
import java.io.StringReader
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RawLexiconReaderTest {
  companion object {
    fun csvfile(name: String): CSVParser {
      val stream = resStream(name)
      return CSVParser(stream.reader())
    }

    fun csvtext(content: String): CSVParser {
      return CSVParser(StringReader(content))
    }
  }

  @Test
  fun legacyCsvWithMinimumFields() {
    val reader = RawLexiconReader(csvfile("legacy-minimum.csv"), POSTable())
    assertNotNull(reader.nextEntry()).let { e ->
      assertEquals("東京都", e.headword)
      assertEquals("トウキョウト", e.reading)
      assertEquals(listOf(WordRef.LineNo(5, false), WordRef.LineNo(9, false)), e.wordStructure)
      assertEquals(0, e.synonymGroups.length())
      assertTrue(e.cUnitSplit.isEmpty())
      assertEquals("", e.userData)
    }
    assertNull(reader.nextEntry())
  }

  @Test
  fun legacyCsvWithAllFields() {
    val reader = RawLexiconReader(csvfile("legacy-full.csv"), POSTable())
    assertNotNull(reader.nextEntry()).let { e ->
      assertEquals("東京都", e.headword)
      assertEquals("トウキョウト", e.reading)
      assertEquals(listOf(WordRef.LineNo(5, false), WordRef.LineNo(9, false)), e.wordStructure)
      assertEquals(Ints.wrap(intArrayOf(6, 7)), e.synonymGroups)
      assertEquals(listOf(WordRef.LineNo(8, false), WordRef.LineNo(9, false)), e.cUnitSplit)
      assertEquals("10", e.userData)
    }
    assertNull(reader.nextEntry())
  }

  @Test
  fun headerCsvMinimumFields() {
    val reader = RawLexiconReader(csvfile("headers-minimum.csv"), POSTable())
    assertNotNull(reader.nextEntry()).let { e ->
      assertEquals("東京都", e.headword)
      assertEquals("トウキョウト", e.reading)
      assertEquals(
          listOf(WordRef.Triple("東京", 0, "トウキョウ"), WordRef.Triple("都", 1, "ト")), e.aUnitSplit)
      assertEquals(
          listOf(WordRef.Triple("東京", 0, "トウキョウ"), WordRef.Triple("都", 2, "ト")), e.bUnitSplit)
      assertEquals(
          listOf(WordRef.Triple("東京", 0, "トウキョウ"), WordRef.Triple("都", 3, "ト")), e.wordStructure)
    }
    assertNotNull(reader.nextEntry())
    assertNull(reader.nextEntry())
  }

  @Test
  fun headerCsvAllFields() {
    val reader = RawLexiconReader(csvfile("headers-all.csv"), POSTable())
    assertNotNull(reader.nextEntry()).let { e ->
      assertEquals("東京都", e.headword)
      assertEquals("トウキョウト", e.reading)
      assertEquals(
          listOf(WordRef.Triple("東京", 0, "トウキョウ"), WordRef.Triple("都", 1, "ト")), e.aUnitSplit)
      assertEquals(
          listOf(WordRef.Triple("東京", 0, "トウキョウ"), WordRef.Triple("都", 2, "ト")), e.bUnitSplit)
      assertEquals(
          listOf(WordRef.Triple("東京", 0, "トウキョウ"), WordRef.Triple("都", 3, "ト")), e.cUnitSplit)
      assertEquals(
          listOf(WordRef.Triple("東京", 0, "トウキョウ"), WordRef.Triple("都", 4, "ト")), e.wordStructure)
      assertEquals(Ints.wrap(intArrayOf(8, 9)), e.synonymGroups)
      assertEquals("10", e.userData)
    }
    assertNotNull(reader.nextEntry())
    assertNull(reader.nextEntry())
  }

  @Test
  fun failMissingRequiredEntry() {
    // pos1-6 are not required (because of posId), but must be used as a set
    val columns =
        "Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure".split(
            ",")
    val values = "東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,,,".split(",")

    for (i in columns.indices) {
      val skipCols = columns.toMutableList() // copy
      skipCols.removeAt(i)
      val skipVals = values.toMutableList() // copy
      skipVals.removeAt(i)

      val text = skipCols.joinToString(",") + "\n" + skipVals.joinToString(",")
      assertFails { RawLexiconReader(csvtext(text), POSTable()) }
    }
  }

  @Test
  fun posIdColumn() {
    val text =
        """Surface,LeftId,RightId,Cost,pos_id,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
東京都,6,8,5320,0,トウキョウト,,,,,"""
    val posTable = POSTable()
    posTable.getId(POS("a", "a", "a", "a", "a", "0"))

    val reader = RawLexiconReader(csvtext(text), posTable)
    assertNotNull(reader.nextEntry()).let { e -> assertEquals(0, e.posId) }
    assertNull(reader.nextEntry())
  }

  @Test
  fun failNonExistingPosId() {
    val text =
        """Surface,LeftId,RightId,Cost,pos_id,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
東京都,6,8,5320,1,トウキョウト,,,,,"""
    val posTable = POSTable()
    posTable.getId(POS("a", "a", "a", "a", "a", "0"))

    assertFails {
      val reader = RawLexiconReader(csvtext(text), posTable)
      reader.nextEntry()
    }
  }

  @Test
  fun posIdAndParts() {
    val text =
        """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,pos_id,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,0,トウキョウト,,,,,"""
    val posTable = POSTable()

    val reader = RawLexiconReader(csvtext(text), posTable)
    assertNotNull(reader.nextEntry()).let { e -> assertEquals(0, e.posId) }
    assertNull(reader.nextEntry())
  }

  @Test
  fun posIdAndEmptyParts() {
    val text =
        """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,pos_id,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
東京都,6,8,5320,,,,,,,0,トウキョウト,,,,,"""
    val posTable = POSTable()
    posTable.getId(POS("a", "a", "a", "a", "a", "0"))

    val reader = RawLexiconReader(csvtext(text), posTable)
    assertNotNull(reader.nextEntry()).let { e -> assertEquals(0, e.posId) }
    assertNull(reader.nextEntry())
  }

  @Test
  fun posPartsAndEmptyPosId() {
    val text =
        """Surface,LeftId,RightId,Cost,pos_id,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
東京都,6,8,5320,0,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,,,"""
    val posTable = POSTable()

    val reader = RawLexiconReader(csvtext(text), posTable)
    assertNotNull(reader.nextEntry()).let { e -> assertEquals(0, e.posId) }
    assertNull(reader.nextEntry())
  }

  @Test
  fun failPosIdAndPartsNotMatch() {
    val text =
        """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,pos_id,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,0,トウキョウト,,,,,"""
    val posTable = POSTable()
    posTable.getId(POS("a", "a", "a", "a", "a", "0"))

    assertFails {
      val reader = RawLexiconReader(csvtext(text), posTable)
      reader.nextEntry()
    }
  }

  @Test
  fun failPosColumnMissing() {
    val text =
        """Surface,LeftId,RightId,Cost,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
東京都,6,8,5320,トウキョウト,,,,,"""
    val posTable = POSTable()
    posTable.getId(POS("a", "a", "a", "a", "a", "0"))

    assertFails {
      val reader = RawLexiconReader(csvtext(text), posTable)
      reader.nextEntry()
    }
  }

  @Test
  fun failPosColumnEmpty() {
    val text =
        """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,pos_id,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
東京都,6,8,5320,,,,,,,,トウキョウト,,,,,"""
    val posTable = POSTable()
    posTable.getId(POS("a", "a", "a", "a", "a", "0"))

    assertFails {
      val reader = RawLexiconReader(csvtext(text), posTable)
      reader.nextEntry()
    }
  }

  @Test
  fun failTooLongValue() {
    val oversizeWord = "a".repeat(StringPtr.MAX_LENGTH + 1)
    run {
      val text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
${oversizeWord},6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,1,,,"""
      val reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
    run {
      val text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,${oversizeWord},,,1,,,"""
      val reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
    run {
      val text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,${oversizeWord},,1,,,"""
      val reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
  }

  @Test
  fun failEmptyHeadword() {
    val text =
        """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,wordstructure
,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,,,"""
    val reader = RawLexiconReader(csvtext(text), POSTable())
    assertFails { reader.nextEntry() }
  }

  @Test
  @Ignore // Currently single split list is allowed.
  fun failSingleSplit() {
    run {
      val text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,1,,,"""
      val reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
    run {
      val text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,,1,,"""
      val reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
    run {
      val text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,,,1,"""
      val reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
    run {
      val text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,,,,1"""
      val reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
  }

  @Test
  fun failTooManySplit() {
    val oversizeSplit: String =
        generateSequence { "1" }.take(Byte.MAX_VALUE.toInt() + 1).joinToString("/")

    run {
      var text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,${oversizeSplit},,,"""
      var reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
    run {
      val text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,,${oversizeSplit},,"""
      val reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
    run {
      val text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,,,${oversizeSplit},"""
      val reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
    run {
      val text =
          """Surface,LeftId,RightId,Cost,pos1,pos2,pos3,pos4,pos5,pos6,reading_form,normalized_form,DictionaryForm,splita,splitb,splitC,wordstructure
東京都,6,8,5320,名詞,固有名詞,地名,一般,*,*,トウキョウト,,,,,,${oversizeSplit}"""
      val reader = RawLexiconReader(csvtext(text), POSTable())
      assertFails { reader.nextEntry() }
    }
  }
}

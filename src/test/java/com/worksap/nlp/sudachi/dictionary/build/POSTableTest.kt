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

import com.worksap.nlp.sudachi.dictionary.POS
import com.worksap.nlp.sudachi.resStream
import kotlin.test.assertEquals
import kotlin.test.assertFails
import org.junit.Test

class POSTableTest {
  @Test
  fun loadFromCSV() {
    val posTable = POSTable()
    val nRead = posTable.readEntries(resStream("/dict/pos.csv"))

    assertEquals(8, nRead)
    assertEquals(nRead, posTable.ownedLength())

    val pos = POS("名詞", "固有名詞", "地名", "一般", "*", "*")
    assertEquals(5, posTable.getId(pos))
  }

  @Test
  fun allowNoHeaderWithPosId() {
    val poss = """0,名詞,普通名詞,一般,*,*,*
1,助詞,接続助詞,*,*,*,*"""
    val posTable = POSTable()
    posTable.readEntries(poss.byteInputStream())

    val pos = POS("名詞", "普通名詞", "一般", "*", "*", "*")
    assertEquals(0, posTable.getId(pos))
  }

  @Test
  fun allowNoHeaderWithoutPosId() {
    val poss = """名詞,普通名詞,一般,*,*,*
助詞,接続助詞,*,*,*,*"""
    val posTable = POSTable()
    posTable.readEntries(poss.byteInputStream())

    val pos = POS("名詞", "普通名詞", "一般", "*", "*", "*")
    assertEquals(0, posTable.getId(pos))
  }

  @Test
  fun allowNotOrderedColumnsWithPosid() {
    val poss = """pos5,pos6,posId,pos1,pos2,pos3,pos4
*,*,0,名詞,普通名詞,一般,*
*,*,1,助詞,接続助詞,*,*"""
    val posTable = POSTable()
    posTable.readEntries(poss.byteInputStream())

    val pos = POS("名詞", "普通名詞", "一般", "*", "*", "*")
    assertEquals(0, posTable.getId(pos))
  }

  @Test
  fun allowNotOrderedColumnsWithoutPosid() {
    val poss = """pos5,pos6,pos1,pos2,pos3,pos4
*,*,名詞,普通名詞,一般,*
*,*,助詞,接続助詞,*,*"""
    val posTable = POSTable()
    posTable.readEntries(poss.byteInputStream())

    val pos = POS("名詞", "普通名詞", "一般", "*", "*", "*")
    assertEquals(0, posTable.getId(pos))
  }

  @Test
  fun allowNotOrderedPosIds() {
    val poss = """posId,pos1,pos2,pos3,pos4,pos5,pos6
1,名詞,普通名詞,一般,*,*,*
0,助詞,接続助詞,*,*,*,*"""
    val posTable = POSTable()
    posTable.readEntries(poss.byteInputStream())

    val pos = POS("名詞", "普通名詞", "一般", "*", "*", "*")
    assertEquals(1, posTable.getId(pos))
  }

  @Test
  fun inhibitMissingPosId() {
    val poss = """posId,pos1,pos2,pos3,pos4,pos5,pos6
1,名詞,普通名詞,一般,*,*,*"""
    val posTable = POSTable()
    assertFails { posTable.readEntries(poss.byteInputStream()) }
  }

  @Test
  fun inhibitReadingDuplicatePos() {
    val dupPoss = """名詞,普通名詞,一般,*,*,*
名詞,普通名詞,一般,*,*,*"""
    val posTable = POSTable()
    assertFails { posTable.readEntries(dupPoss.byteInputStream()) }
  }

  @Test
  fun inhibitNewPos() {
    val posTable = POSTable()
    posTable.setAllowNewPos(false)

    val newPos = POS("a", "a", "a", "a", "a", "a")
    assertFails { posTable.getId(newPos) }
  }

  @Test
  fun failTooManyPoss() {
    val posTable = POSTable()
    repeat(Short.MAX_VALUE.toInt()) {
      val pos = POS("a", "b", "c", "d", "e", it.toString())
      assertEquals(posTable.getId(pos), it.toShort())
    }
    assertFails { posTable.getId(POS("a", "a", "a", "a", "a", "a")) }
  }
}

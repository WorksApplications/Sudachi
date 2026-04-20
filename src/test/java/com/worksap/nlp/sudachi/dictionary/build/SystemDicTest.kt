/*
 * Copyright (c) 2017-2024 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.WordId
import com.worksap.nlp.sudachi.dictionary.BinaryDictionary
import com.worksap.nlp.sudachi.dictionary.Block
import com.worksap.nlp.sudachi.dictionary.DictionaryAccess
import com.worksap.nlp.sudachi.dictionary.POS
import com.worksap.nlp.sudachi.morpheme
import com.worksap.nlp.sudachi.res
import com.worksap.nlp.sudachi.setCharacterCategory
import kotlin.test.*

fun DicBuilder.System.lexicon(s: String): DicBuilder.System {
  return this.lexicon("test", { s.byteInputStream() }, s.length.toLong())
}

fun DicBuilder.User.lexicon(s: String): DicBuilder.User {
  return this.lexicon("test", { s.byteInputStream() }, s.length.toLong())
}

class SystemDicTest {
  @Test
  fun simple() {
    val data = MemChannel()
    DicBuilder.system()
        .matrix(javaClass.getResource("test.matrix"))
        .lexicon(javaClass.getResource("one.csv"))
        .build(data)
    val dic = BinaryDictionary(data.buffer())
    assertEquals(1, dic.grammar.partOfSpeechSize)
    assertEquals(1, dic.lexicon.size())
  }

  @Test
  fun fields() {
    val bldr = DicBuilder.system().matrix(javaClass.getResource("test.matrix"))
    val data = MemChannel()
    repeat(10) { bldr.lexicon(javaClass.getResource("one.csv")) }
    bldr.lexicon("南,1,1,4675,南,名詞,普通名詞,一般,*,*,*,ミナミ,西,5,C,0/1,2/3,4/5,6/7").build(data)
    val dic = BinaryDictionary(data.buffer())
    (dic as DictionaryAccess).setCharacterCategory(javaClass.getResource("char.def"))
    assertEquals(11, dic.lexicon.size()) // 10 + 南
    assertEquals(POS("名詞", "普通名詞", "一般", "*", "*", "*"), dic.grammar.getPartOfSpeechString(0))
    val m = dic.morpheme(44) // 11th word (i.e. 南)
    val wi = dic.lexicon.getWordInfo(m.getWordId())
    assertEquals("南", m.surface())
    assertEquals(3, wi.length)
    assertEquals(0, wi.posId)
    assertEquals("東", m.dictionaryForm())
    assertEquals("西", m.normalizedForm())
    assertEquals("ミナミ", m.readingForm())
    assertContentEquals(intArrayOf(4, 8), wi.aunitSplit)
    assertContentEquals(intArrayOf(12, 16), wi.bunitSplit)
    assertContentEquals(intArrayOf(20, 24), wi.wordStructure)
    assertContentEquals(intArrayOf(6, 7), m.synonymGroupIds)
  }

  @Test
  fun fieldsCompressed() {
    val bldr = DicBuilder.system().matrix(javaClass.getResource("test.matrix"))
    val data = MemChannel()
    bldr.lexicon("南,1,1,4675,南,名詞,普通名詞,一般,*,*,*,南,南,*,C,*,*,*,*").build(data)
    val dic = BinaryDictionary(data.buffer())
    (dic as DictionaryAccess).setCharacterCategory(javaClass.getResource("char.def"))
    val wordIds = intArrayOf(4)
    assertEquals(1, dic.lexicon.size())
    assertEquals(POS("名詞", "普通名詞", "一般", "*", "*", "*"), dic.grammar.getPartOfSpeechString(0))
    val m = dic.morpheme(wordIds[0])
    assertEquals("南", m.surface())
    assertEquals("南", m.dictionaryForm())
    assertEquals("南", m.normalizedForm())
    assertEquals("南", m.readingForm())
  }

  @Test
  fun failMatrixSizeValidation() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    assertFails { bldr.lexicon("東,4,1,4675,東,名詞,普通名詞,一般,*,*,*,ヒガシ,東,*,A,*,*,*,*") }
    assertFails { bldr.lexicon("東,1,4,4675,東,名詞,普通名詞,一般,*,*,*,ヒガシ,東,*,A,*,*,*,*") }
  }

  @Test
  fun aSplits() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    val data = MemChannel()
    bldr
        .lexicon(
            """東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,0/2,*,0/2,*
都,2,2,2914,都,名詞,普通名詞,一般,*,*,*,ト,都,*,A,*,*,*,*""".trimIndent())
        .build(data)
    val wordIds = intArrayOf(4, 8, 13)
    val dic = BinaryDictionary(data.buffer())
    assertEquals(3, dic.lexicon.size())
    val wi = dic.lexicon.getWordInfo(wordIds[1])
    assertContentEquals(intArrayOf(wordIds[0], wordIds[2]), wi.aunitSplit)
    assertContentEquals(intArrayOf(wordIds[0], wordIds[2]), wi.wordStructure)
  }

  @Test
  fun aSplitsInline() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    val data = MemChannel()
    bldr
        .lexicon(
            """東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,"東京,名詞,固有名詞,地名,一般,*,*,トウキョウ/2",*,0/2,*
都,2,2,2914,都,名詞,普通名詞,一般,*,*,*,ト,都,*,A,*,*,*,*""".trimIndent())
        .build(data)
    val wordIds = intArrayOf(4, 8, 14)
    val dic = BinaryDictionary(data.buffer())
    assertEquals(3, dic.lexicon.size())
    val wi = dic.lexicon.getWordInfo(wordIds[1])
    assertContentEquals(intArrayOf(wordIds[0], wordIds[2]), wi.aunitSplit)
    assertContentEquals(intArrayOf(wordIds[0], wordIds[2]), wi.wordStructure)
  }

  @Test
  fun bSplits() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    val data = MemChannel()
    bldr
        .lexicon(
            """東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,*,0/2,0/2,*
都,2,2,2914,都,名詞,普通名詞,一般,*,*,*,ト,都,*,A,*,*,*,*""".trimIndent())
        .build(data)
    val wordIds = intArrayOf(4, 8, 14)
    val dic = BinaryDictionary(data.buffer())
    assertEquals(3, dic.lexicon.size())
    val wi = dic.lexicon.getWordInfo(wordIds[1])
    assertContentEquals(intArrayOf(wordIds[0], wordIds[2]), wi.bunitSplit)
    assertContentEquals(intArrayOf(wordIds[0], wordIds[2]), wi.wordStructure)
  }

  @Test
  fun systemSplitU() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    val data = MemChannel()
    bldr
        .lexicon(
            """東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,*,0/2,U0/U2,*
都,2,2,2914,都,名詞,普通名詞,一般,*,*,*,ト,都,*,A,*,*,*,*""".trimIndent())
        .build(data)
    val wordIds = intArrayOf(4, 8, 14)
    val dic = BinaryDictionary(data.buffer())
    assertEquals(3, dic.lexicon.size())
    val wi = dic.lexicon.getWordInfo(wordIds[1])
    assertContentEquals(intArrayOf(wordIds[0], wordIds[2]), wi.bunitSplit)
    assertContentEquals(intArrayOf(wordIds[0], wordIds[2]), wi.wordStructure)
  }

  @Test
  fun variousWordReferences() {
    val dictData = MemChannel()
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    bldr.lexicon(javaClass.getResource("wordref.csv")).build(dictData)

    val wordIds = intArrayOf(4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 45)
    val dic = BinaryDictionary(dictData.buffer())
    assertEquals(wordIds.size, dic.lexicon.size())
  }

  @Test
  fun referenceIdResolvesAmbiguousEntries() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    val data = MemChannel()
    bldr
        .lexicon(
            """
IndexForm,LeftId,RightId,Cost,Headword,POS1,POS2,POS3,POS4,POS5,POS6,Reading_Form,Normalized_Form,Dictionary_Form,Mode,Split_A,Split_B,WordStructure,reference_id
東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京A,,,,,,tokyo-a
東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京B,,,,,,tokyo-b
区,2,2,2914,区,名詞,普通名詞,一般,*,*,*,ク,,,,,,,
東京区,2,2,5320,東京区,名詞,固有名詞,地名,一般,*,*,トウキョウク,,,B,"東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,tokyo-b/区,名詞,普通名詞,一般,*,*,*,ク",,,
                """.trimIndent())
        .build(data)

    val dic = BinaryDictionary(data.buffer())
    val wi = dic.lexicon.getWordInfo(WordId.make(0, 16))
    assertContentEquals(intArrayOf(8, 12), wi.aunitSplit)
    assertEquals(2, dic.getReferenceIdMap().size)
    assertEquals("tokyo-b", dic.getReferenceIdMap()[8])
    assertNotNull(dic.dictionaryHeader.sliceOrNull(data.buffer(), Block.REFERENCE_ID_TABLE))
  }

  @Test
  fun duplicateReferenceIdFails() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    assertFails {
      bldr.lexicon(
          """
IndexForm,LeftId,RightId,Cost,Headword,POS1,POS2,POS3,POS4,POS5,POS6,Reading_Form,Normalized_Form,Dictionary_Form,Mode,Split_A,Split_B,WordStructure,reference_id
東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京A,,,,,,dup
東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京B,,,,,,dup
          """.trimIndent())
    }
  }

  @Test
  fun failSplitBoundsCheck() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    bldr.lexicon("""東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,*,1,*,*""")
    assertFails { bldr.build(MemChannel()) }
  }

  @Test
  fun failInvalidNumberOfInlineRefFields() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    assertFails {
      bldr.lexicon("""東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,*,"a,b,c,d,e",*,*""")
    }
  }

  @Test
  fun failInlineRefInvalid() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    bldr.lexicon(
        """東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,*,"東京,名詞,固有名詞,地名,一般,*,*,a",*,*""".trimMargin())
    assertFails { bldr.build(MemChannel()) }
  }

  @Test
  fun enormousEntriesWork() {
    val bldr = DicBuilder.system().matrix(res("test.matrix"))
    (0..100).forEach { i ->
      val istr = String.format("%04x", i)
      val surf = "a".repeat(1024) + istr
      val read = "b".repeat(1024) + istr
      val norm = "c".repeat(1024) + istr
      bldr.lexicon("$surf,1,1,2816,$surf,名詞,固有名詞,地名,一般,*,*,$read,$norm,*,A,*,*,*,*")
    }
    val ch = MemChannel()
    bldr.build(ch)
    val dic = BinaryDictionary(ch.buffer())
    (dic as DictionaryAccess).setCharacterCategory(javaClass.getResource("char.def"))
    assertEquals(dic.lexicon.size(), 101)

    (0..100).forEach { i ->
      val wordId = i * 4 + 4
      val istr = String.format("%04x", i)
      val surf = "a".repeat(1024) + istr
      val read = "b".repeat(1024) + istr
      val norm = "c".repeat(1024) + istr

      val surfArray = surf.encodeToByteArray()
      val iter = dic.lexicon.lookup(surfArray, 0)
      assertTrue { iter.hasNext() }
      assertContentEquals(intArrayOf(wordId, surfArray.size), iter.next())
      assertFalse { iter.hasNext() }

      val m = dic.morpheme(wordId)
      assertEquals(surf, m.surface())
      assertEquals(read, m.readingForm())
      assertEquals(norm, m.normalizedForm())
    }
  }
}

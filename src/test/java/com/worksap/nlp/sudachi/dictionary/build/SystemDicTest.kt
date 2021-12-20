package com.worksap.nlp.sudachi.dictionary.build

import com.worksap.nlp.sudachi.dictionary.BinaryDictionary
import com.worksap.nlp.sudachi.dictionary.POS
import kotlin.test.*

class SystemDicTest {
    @Test
    fun simple() {
        val data = BytesChannel()
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
        val data = BytesChannel()
        repeat(10) { bldr.lexicon(javaClass.getResource("one.csv")) }
        bldr.lexicon("南,1,1,4675,南,名詞,普通名詞,一般,*,*,*,ミナミ,西,5,C,0/1,2/3,4/5,6/7".byteInputStream())
            .build(data)
        val dic = BinaryDictionary(data.buffer())
        assertEquals(11, dic.lexicon.size())
        assertEquals(POS("名詞", "普通名詞", "一般", "*", "*", "*"), dic.grammar.getPartOfSpeechString(0))
        val wi = dic.lexicon.getWordInfo(10)
        assertEquals(wi.surface, "南")
        assertEquals(wi.length, 3)
        assertEquals(wi.posId, 0)
        assertEquals(wi.dictionaryFormWordId, 5)
        assertEquals(wi.dictionaryForm, "東")
        assertEquals(wi.normalizedForm, "西")
        assertEquals(wi.readingForm, "ミナミ")
        assertContentEquals(wi.aunitSplit, intArrayOf(0, 1))
        assertContentEquals(wi.bunitSplit, intArrayOf(2, 3))
        assertContentEquals(wi.wordStructure, intArrayOf(4, 5))
        assertContentEquals(wi.synonymGoupIds, intArrayOf(6, 7))
    }

    @Test
    fun fieldsCompressed() {
        val bldr = DicBuilder.system().matrix(javaClass.getResource("test.matrix"))
        val data = BytesChannel()
        bldr.lexicon("南,1,1,4675,南,名詞,普通名詞,一般,*,*,*,南,南,*,C,*,*,*,*".byteInputStream())
            .build(data)
        val dic = BinaryDictionary(data.buffer())
        assertEquals(1, dic.lexicon.size())
        assertEquals(POS("名詞", "普通名詞", "一般", "*", "*", "*"), dic.grammar.getPartOfSpeechString(0))
        val wi = dic.lexicon.getWordInfo(0)
        assertEquals(wi.surface, "南")
        assertEquals(wi.dictionaryFormWordId, -1)
        assertEquals(wi.dictionaryForm, "南")
        assertEquals(wi.normalizedForm, "南")
        assertEquals(wi.readingForm, "南")
    }

    @Test
    fun failMatrixSizeValidation() {
        val bldr = DicBuilder.system().matrix(res("test.matrix"))
        assertFails { bldr.lexicon("東,4,1,4675,東,名詞,普通名詞,一般,*,*,*,ヒガシ,東,*,A,*,*,*,*".byteInputStream()) }
        assertFails { bldr.lexicon("東,1,4,4675,東,名詞,普通名詞,一般,*,*,*,ヒガシ,東,*,A,*,*,*,*".byteInputStream()) }
    }

    @Test
    fun aSplits() {
        val bldr = DicBuilder.system().matrix(res("test.matrix"))
        val data = BytesChannel()
        bldr.lexicon("""東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
                        東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,0/2,*,0/2,*
                        都,2,2,2914,都,名詞,普通名詞,一般,*,*,*,ト,都,*,A,*,*,*,*""".trimIndent().byteInputStream())
            .build(data)
        val dic = BinaryDictionary(data.buffer())
        assertEquals(3, dic.lexicon.size())
        val wi = dic.lexicon.getWordInfo(1)
        assertContentEquals(wi.aunitSplit, intArrayOf(0, 2))
        assertContentEquals(wi.wordStructure, intArrayOf(0, 2))
    }

    @Test
    fun aSplitsInline() {
        val bldr = DicBuilder.system().matrix(res("test.matrix"))
        val data = BytesChannel()
        bldr.lexicon("""東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
                        東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,"東京,名詞,固有名詞,地名,一般,*,*,トウキョウ/2",*,0/2,*
                        都,2,2,2914,都,名詞,普通名詞,一般,*,*,*,ト,都,*,A,*,*,*,*""".trimIndent().byteInputStream())
            .build(data)
        val dic = BinaryDictionary(data.buffer())
        assertEquals(3, dic.lexicon.size())
        val wi = dic.lexicon.getWordInfo(1)
        assertContentEquals(wi.aunitSplit, intArrayOf(0, 2))
        assertContentEquals(wi.wordStructure, intArrayOf(0, 2))
    }

    @Test
    fun bSplits() {
        val bldr = DicBuilder.system().matrix(res("test.matrix"))
        val data = BytesChannel()
        bldr.lexicon("""東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
                        東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,*,0/2,0/2,*
                        都,2,2,2914,都,名詞,普通名詞,一般,*,*,*,ト,都,*,A,*,*,*,*""".trimIndent().byteInputStream())
            .build(data)
        val dic = BinaryDictionary(data.buffer())
        assertEquals(3, dic.lexicon.size())
        val wi = dic.lexicon.getWordInfo(1)
        assertContentEquals(wi.bunitSplit, intArrayOf(0, 2))
        assertContentEquals(wi.wordStructure, intArrayOf(0, 2))
    }

    @Test
    fun systemSplitU() {
        val bldr = DicBuilder.system().matrix(res("test.matrix"))
        val data = BytesChannel()
        bldr.lexicon("""東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
                        東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,*,0/2,U0/U2,*
                        都,2,2,2914,都,名詞,普通名詞,一般,*,*,*,ト,都,*,A,*,*,*,*""".trimIndent().byteInputStream())
            .build(data)
        val dic = BinaryDictionary(data.buffer())
        assertEquals(3, dic.lexicon.size())
        val wi = dic.lexicon.getWordInfo(1)
        assertContentEquals(wi.bunitSplit, intArrayOf(0, 2))
        assertContentEquals(wi.wordStructure, intArrayOf(0, 2))
    }

    @Test
    fun failSplitBoundsCheck() {
        val bldr = DicBuilder.system().matrix(res("test.matrix"))
        bldr.lexicon(
            """東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,*,1,*,*""".byteInputStream()
        )
        assertFails { bldr.build(BytesChannel()) }
    }

    @Test
    fun failInvalidNumberOfInlineRefFields() {
        val bldr = DicBuilder.system().matrix(res("test.matrix"))
        bldr.lexicon(
            """東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,*,"a,b,c,d,e",*,*""".byteInputStream()
        )
        assertFails { bldr.build(BytesChannel()) }
    }

    @Test
    fun failInlineRefInvalid() {
        val bldr = DicBuilder.system().matrix(res("test.matrix"))
        bldr.lexicon(
            """東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
               東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,*,"東京,名詞,固有名詞,地名,一般,*,*,a",*,*""".trimMargin().byteInputStream()
        )
        assertFails { bldr.build(BytesChannel()) }
    }

    @Test
    fun enormousEntriesWork() {
        val bldr = DicBuilder.system().matrix(res("test.matrix"))
        (0..100).forEach { i ->
            val istr = String.format("%04x", i)
            val surf = "a".repeat(1024) + istr
            val read = "b".repeat(1024) + istr
            val norm = "c".repeat(1024) + istr
            bldr.lexicon("$surf,1,1,2816,$surf,名詞,固有名詞,地名,一般,*,*,$read,$norm,*,A,*,*,*,*".byteInputStream())
        }
        val ch = BytesChannel()
        bldr.build(ch)
        val dic = BinaryDictionary(ch.buffer())
        assertEquals(dic.lexicon.size(), 101)
        (0..100).forEach { i ->
            val istr = String.format("%04x", i)
            val surf = "a".repeat(1024) + istr
            val read = "b".repeat(1024) + istr
            val norm = "c".repeat(1024) + istr

            val surfArray = surf.encodeToByteArray()
            val iter = dic.lexicon.lookup(surfArray, 0)
            assertTrue { iter.hasNext() }
            assertContentEquals(intArrayOf(i, surfArray.size), iter.next())
            assertFalse { iter.hasNext() }

            val wi = dic.lexicon.getWordInfo(i)
            assertEquals(wi.surface, surf)
            assertEquals(wi.readingForm, read)
            assertEquals(wi.normalizedForm, norm)
        }
    }
}
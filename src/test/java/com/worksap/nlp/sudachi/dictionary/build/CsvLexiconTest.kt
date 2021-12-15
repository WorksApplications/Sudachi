package com.worksap.nlp.sudachi.dictionary.build

import kotlin.test.Test
import kotlin.test.assertFails

class CsvLexiconTest {
    @Test
    fun failEntryIsSmall() {
        val clex = CsvLexicon(POSTable())
        (0..18).forEach {
            val data = generateSequence { "a" }.take(it).toList()
            assertFails { clex.parseLine(data) }
        }
    }

    @Test
    fun failEntryHasTooLongString() {
        val clex = CsvLexicon(POSTable())
        val data = "東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*".split(",")
        assertFails {
            val copy = data.toList().toMutableList()
            copy[0] = "a".repeat(DicBuffer.MAX_STRING + 1)
            clex.parseLine(copy)
        }
        assertFails {
            val copy = data.toList().toMutableList()
            copy[4] = "a".repeat(DicBuffer.MAX_STRING + 1)
            clex.parseLine(copy)
        }
        assertFails {
            val copy = data.toList().toMutableList()
            copy[11] = "a".repeat(DicBuffer.MAX_STRING + 1)
            clex.parseLine(copy)
        }
        assertFails {
            val copy = data.toList().toMutableList()
            copy[12] = "a".repeat(DicBuffer.MAX_STRING + 1)
            clex.parseLine(copy)
        }
    }

    @Test
    fun failEmptyHeadword() {
        val clex = CsvLexicon(POSTable())
        val data = ",1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*".split(",")
        assertFails { clex.parseLine(data) }
    }

    @Test
    fun failInvalidSplitting() {
        val clex = CsvLexicon(POSTable())
        assertFails { clex.parseLine("a,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,1,*,*,*".split(",")) }
        assertFails { clex.parseLine("a,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,1,*,*".split(",")) }
    }

    @Test
    fun failTooManyUnits() {
        val clex = CsvLexicon(POSTable())
        val data = "東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,C,*,*,*,*".split(",")
        assertFails {
            val copy = data.toList().toMutableList()
            copy[15] = (0..256).joinToString("/") { it.toString() }
            clex.parseLine(copy)
        }
        assertFails {
            val copy = data.toList().toMutableList()
            copy[16] = (0..256).joinToString("/") { it.toString() }
            clex.parseLine(copy)
        }
        assertFails {
            val copy = data.toList().toMutableList()
            copy[17] = (0..256).joinToString("/") { it.toString() }
            clex.parseLine(copy)
        }
        assertFails {
            val copy = data.toList().toMutableList()
            copy[18] = (0..256).joinToString("/") { it.toString() }
            clex.parseLine(copy)
        }
    }
}
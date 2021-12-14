package com.worksap.nlp.sudachi.dictionary.build

import com.worksap.nlp.sudachi.dictionary.BinaryDictionary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

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
    fun failMatrixSizeValidation() {
        val bldr = DicBuilder.system().matrix(javaClass.getResource("test.matrix"))
        assertFails { bldr.lexicon("東,4,1,4675,東,名詞,普通名詞,一般,*,*,*,ヒガシ,東,*,A,*,*,*,*".byteInputStream()) }
        assertFails { bldr.lexicon("東,1,4,4675,東,名詞,普通名詞,一般,*,*,*,ヒガシ,東,*,A,*,*,*,*".byteInputStream()) }
    }
}
package com.worksap.nlp.sudachi.dictionary.build

import com.worksap.nlp.sudachi.dictionary.BinaryDictionary
import kotlin.test.Test
import kotlin.test.assertEquals

class SystemDicTest {
    @Test
    fun simple() {
        val data = BytesChannel()
        val stats =
            DicBuilder.system()
                .matrix(javaClass.getResource("test.matrix"))
                .lexicon(javaClass.getResource("one.csv"))
                .build(data)
        val dic = BinaryDictionary(data.buffer())
        assertEquals(1, dic.grammar.partOfSpeechSize)
        assertEquals(1, dic.lexicon.size())
    }
}
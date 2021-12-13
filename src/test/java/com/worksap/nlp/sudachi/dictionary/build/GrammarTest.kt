package com.worksap.nlp.sudachi.dictionary.build

import com.worksap.nlp.sudachi.dictionary.GrammarImpl
import com.worksap.nlp.sudachi.dictionary.POS
import org.junit.Test
import kotlin.test.assertEquals

class GrammarTest {
    @Test
    fun singlePos() {
        val cm = ConnectionMatrix()
        Res("test.matrix") { cm.readEntries(it) }
        val pos = POSTable()
        assertEquals(0, pos.getId(POS("a", "b", "c", "d", "e", "f")))
        val outbuf = BytesChannel()
        val out = ModelOutput(outbuf)
        pos.writeTo(out)
        cm.writeTo(out)
        val gram = GrammarImpl(outbuf.buffer(), 0)
        assertEquals(gram.getPartOfSpeechString(0), POS("a", "b", "c", "d", "e", "f"))
    }
}
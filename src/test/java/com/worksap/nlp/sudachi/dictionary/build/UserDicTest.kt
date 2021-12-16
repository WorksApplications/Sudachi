package com.worksap.nlp.sudachi.dictionary.build

import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.WordId
import com.worksap.nlp.sudachi.dictionary.BinaryDictionary
import com.worksap.nlp.sudachi.dictionary.DictionaryAccess
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals

fun <T : Any> T.res(name: String): URL {
    return javaClass.getResource(name) ?: throw IllegalArgumentException("$name was not found")
}

class TestDic {
    private var matrixUrl: URL = res("test.matrix")
    private lateinit var systemDic: BinaryDictionary
    private val userDics: MutableList<BinaryDictionary> = mutableListOf()
    private val config = res("sudachi_dic_build.json")

    fun matrix(url: URL): TestDic {
        this.matrixUrl = url
        return this
    }

    fun system(data: String): TestDic {
        val bldr = DicBuilder.system().matrix(matrixUrl).lexicon(data.byteInputStream())
        val ch = BytesChannel()
        bldr.build(ch)
        this.systemDic = BinaryDictionary(ch.buffer())
        return this
    }

    fun user(data: String): TestDic {
        val bldr = DicBuilder.user(systemDic).lexicon(data.byteInputStream())
        val ch = BytesChannel()
        bldr.build(ch)
        this.userDics.add(BinaryDictionary(ch.buffer()))
        return this
    }

    fun load(): Dictionary {
        val loader = DictionaryFactory.loader().config(config).system(systemDic)
        userDics.forEach { loader.user(it) }
        return loader.load()
    }
}


class UserDicTest {
    @Test
    fun simple() {
        val dic = TestDic()
            .system(
                """東京,1,1,2816,東京,名詞,固有名詞,地名,一般,*,*,トウキョウ,東京,*,A,*,*,*,*
                   都,2,2,2914,都,名詞,普通名詞,一般,*,*,*,ト,都,*,A,*,*,*,*""".trimIndent()
            )
            .user(
                """東京都,2,2,5320,東京都,名詞,固有名詞,地名,一般,*,*,トウキョウト,東京都,*,B,0/1,*,0/1,*""".trimIndent()
            )
            .load()

        val da = dic as DictionaryAccess;
        val wi = da.lexicon.getWordInfo(WordId.make(1, 0))
        assertEquals(wi.surface, "東京都")
        assertEquals(wi.readingForm, "トウキョウト")
    }
}
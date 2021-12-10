package com.worksap.nlp.sudachi.dictionary.build

import com.worksap.nlp.sudachi.dictionary.Connection
import org.junit.Test
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

object Res {
    operator fun <R> invoke(name: String, fn: (InputStream) -> R): R {
        Res.javaClass.getResourceAsStream(name).use {
            assertNotNull(it, "resource '$name' did not exist")
            return fn(it)
        }
    }
}

class ConnectionMatrixTest {
    @Test
    fun parse3x3() {
        val cm = ConnectionMatrix()
        assertEquals(9, Res("test.matrix") { cm.readEntries(it) })
        val conn = Connection(cm.compiledNoHeader.asShortBuffer(), 3, 3)
        assertEquals(conn.cost(0, 0), 0)
        assertEquals(conn.cost(1, 1), 4)
        assertEquals(conn.cost(2, 1), 7)
    }

    @Test
    fun invalidHeader() {
        val cm = ConnectionMatrix()
        assertFailsWith<IllegalArgumentException> { cm.readEntries("1".byteInputStream()) }
    }

    @Test
    fun emptyHeader() {
        val cm = ConnectionMatrix()
        assertFailsWith<IllegalArgumentException> { cm.readEntries("".byteInputStream()) }
    }

    @Test
    fun badHeader() {
        val cm = ConnectionMatrix()
        assertFailsWith<IllegalArgumentException> { cm.readEntries("5 a".byteInputStream()) }
    }
}
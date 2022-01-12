package com.worksap.nlp.sudachi

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class StringUtilTest {
    @Test
    fun readAllBytes() {
        val resource = javaClass.getResource("/char.def")
        val buf = StringUtil.readAllBytes(resource)
        val str = StringUtil.readFully(resource)
        val bytes = str.encodeToByteArray()
        assertEquals(bytes.size, buf.remaining())
        val arr2 = ByteArray(bytes.size)
        buf.get(arr2)
        assertContentEquals(bytes, arr2)
    }
}
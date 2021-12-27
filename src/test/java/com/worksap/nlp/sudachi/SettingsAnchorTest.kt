package com.worksap.nlp.sudachi

import com.worksap.nlp.sudachi.dictionary.build.DicBuilder
import kotlin.test.*

class SettingsAnchorTest {
    @Test
    fun classpath() {
        assertNotNull(SettingsAnchor.classpath())
    }

    @Test
    fun classpathOfClass() {
        val resolver = SettingsAnchor.classpath(DicBuilder::class.java)
        val path = resolver.resolve("one.csv")
        assertTrue(resolver.exists(path))
        val path2 = resolver.resolve("doesnotexist.file")
        assertFalse(resolver.exists(path2))
    }

    @Test
    fun chainNone() {
        assertEquals(SettingsAnchor.none(), SettingsAnchor.none().andThen(SettingsAnchor.none()))
        assertEquals(SettingsAnchor.classpath(), SettingsAnchor.classpath().andThen(SettingsAnchor.classpath()))
    }

    @Test fun chain() {
        val chain = SettingsAnchor.classpath().andThen(SettingsAnchor.none())
        assertIs<SettingsAnchor.Chain>(chain)
        assertEquals(chain.count(), 2)
        val chain2 = chain.andThen(chain)
        assertSame(chain, chain2)
    }

    @Test fun chainChains() {
        val chain1 = SettingsAnchor.classpath().andThen(SettingsAnchor.none())
        val chain2 = SettingsAnchor.classpath().andThen(SettingsAnchor.none())
        val chain3 = chain1.andThen(chain2)
        assertIs<SettingsAnchor.Chain>(chain3)
        assertEquals(chain3.count(), 2)
    }
}
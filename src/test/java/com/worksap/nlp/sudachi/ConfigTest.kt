package com.worksap.nlp.sudachi

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConfigTest {

    @Test
    fun empty() {
        assertNotNull(Config.empty())
    }

    @Test
    fun fromString() {
        assertNotNull(Config.fromJsonString("{}", Settings.PathResolver.classPath()))
    }

    @Test
    fun fromClasspath() {
        assertNotNull(Config.fromClasspath("sudachi_test_empty.json"))
    }

    @Test
    fun addEditConnectionCostPlugin() {
        val cfg = Config.empty()
        cfg.addEditConnectionCostPlugin(InhibitConnectionPlugin::class.java)
        val plugins = cfg.editConnectionCostPlugins
        assertEquals(plugins.size, 1)
    }

    @Test
    fun resolveFilesystemPath() {
        val cfg =
            Config.fromJsonString("""{"systemDict": "test"}""", Settings.PathResolver.fileSystem(Paths.get("/usr")))
        assertEquals(cfg.systemDictionary.repr(), Paths.get("/usr/test"))
    }

    @Test
    fun resolveClasspathDefault() {
        val cfg = Config.fromClasspath()
        assert((cfg.systemDictionary.repr() as URL).path.endsWith("system.dic"))
        val user = cfg.userDictionaries
        assertEquals(user.size, 1)
        assert((user[0].repr() as URL).path.endsWith("user.dic"))
        assert((cfg.characterDefinition.repr() as URL).path.endsWith("char.def"))
        assertEquals(cfg.inputTextPlugins.size, 3)
        assertEquals(cfg.oovProviderPlugins.size, 1)
    }

    @Test
    fun mergeReplace() {
        val base = Config.fromClasspath()
        val top = Config.fromJsonString("""{
            "systemDict": "test1.dic",
            "userDict": ["test2.dic", "test3.dic"],
            "oovProviderPlugin": [{
              "class": "com.worksap.nlp.sudachi.SimpleOovProviderPlugin",
              "cost": 12000
            }]
        }""", Settings.PathResolver.fileSystem(Paths.get("")))
        val merged = base.merge(top, Config.MergeMode.REPLACE)
        assert((merged.systemDictionary.repr() as Path).endsWith("test1.dic"))
        assertEquals(merged.userDictionaries.size, 2)
        assert((merged.userDictionaries[0].repr() as Path).endsWith("test2.dic"))
        assert((merged.userDictionaries[1].repr() as Path).endsWith("test3.dic"))
        assertEquals(merged.oovProviderPlugins.size, 1)
        assertEquals(merged.oovProviderPlugins[0].clazzName, "com.worksap.nlp.sudachi.SimpleOovProviderPlugin")
        assertEquals(merged.oovProviderPlugins[0].internal.getInt("cost"), 12000)
    }

    @Test
    fun mergeAppend() {
        val base = Config.fromClasspath()
        val top = Config.fromJsonString("""{
            "systemDict": "test1.dic",
            "userDict": ["test2.dic", "test3.dic"],
            "oovProviderPlugin": [{
              "class": "com.worksap.nlp.sudachi.SimpleOovProviderPlugin",
              "cost": 12000
            }]
        }""", Settings.PathResolver.fileSystem(Paths.get("")))
        val merged = base.merge(top, Config.MergeMode.REPLACE)
        assert((merged.systemDictionary.repr() as Path).endsWith("test1.dic"))
        assertEquals(merged.userDictionaries.size, 2)
        assert((merged.userDictionaries[0].repr() as Path).endsWith("test2.dic"))
        assert((merged.userDictionaries[1].repr() as Path).endsWith("test3.dic"))
        assertEquals(merged.oovProviderPlugins.size, 1)
        assertEquals(merged.oovProviderPlugins[0].clazzName, "com.worksap.nlp.sudachi.SimpleOovProviderPlugin")
        assertEquals(merged.oovProviderPlugins[0].internal.getInt("cost"), 12000)
    }

    @Test
    fun fromClasspathMerged() {
        val config = Config.fromClasspathMerged("sudachi.json", Config.MergeMode.APPEND)
        assertEquals(config.oovProviderPlugins.size, 2)
    }

}
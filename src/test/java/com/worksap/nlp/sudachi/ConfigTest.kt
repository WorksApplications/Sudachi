/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.sudachi

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

class ConfigTest {

  @Test
  fun empty() {
    assertNotNull(Config.empty())
  }

  @Test
  fun fromString() {
    assertNotNull(Config.fromJsonString("{}", PathAnchor.classpath()))
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
        Config.fromJsonString("""{"systemDict": "main"}""", PathAnchor.filesystem(Paths.get("src")))
    assertEquals(cfg.systemDictionary.repr(), Paths.get("src/main"))
  }

  @Test
  fun resolveClasspathDefault() {
    val cfg = Config.defaultConfig()
    assert((cfg.characterDefinition.repr() as URL).path.endsWith("char.def"))
    assertEquals(cfg.inputTextPlugins.size, 3)
    assertEquals(cfg.oovProviderPlugins.size, 1)
  }

  @Test
  fun merge() {
    val base = Config.defaultConfig()
    val top =
        Config.fromJsonString(
            """{
            "systemDict": "test1.dic",
            "userDict": ["test2.dic", "test3.dic"],
            "oovProviderPlugin": [{
              "class": "com.worksap.nlp.sudachi.SimpleOovProviderPlugin",
              "cost": 12000
            }]
        }""",
            PathAnchor.filesystem(Paths.get("")))
    val merged = top.withFallback(base)
    assert((merged.systemDictionary.repr() as Path).endsWith("test1.dic"))
    assertEquals(merged.userDictionaries.size, 2)
    assert((merged.userDictionaries[0].repr() as Path).endsWith("test2.dic"))
    assert((merged.userDictionaries[1].repr() as Path).endsWith("test3.dic"))
    assertEquals(merged.oovProviderPlugins.size, 1)
    assertEquals(
        merged.oovProviderPlugins[0].clazzName, "com.worksap.nlp.sudachi.SimpleOovProviderPlugin")
    assertEquals(merged.oovProviderPlugins[0].internal.getInt("cost"), 12000)
    assertEquals(merged.oovProviderPlugins[0].internal.getInt("leftId"), 8)
    assertEquals(merged.oovProviderPlugins[0].internal.getInt("rightId"), 8)
  }

  @Test
  fun pluginInvalidClass() {
    val cfg =
        Config.fromJsonString(
            """{            
            "oovProviderPlugin": [{
              "class": "java.lang.String"              
            }]
        }""",
            PathAnchor.none())
    assertFails { cfg.oovProviderPlugins[0].instantiate() }
  }

  @Test
  fun pluginInvalidClassName() {
    val cfg =
        Config.fromJsonString(
            """{            
            "oovProviderPlugin": [{
              "class": "java.lang.SSSSSString"              
            }]
        }""",
            PathAnchor.none())
    assertFails { cfg.oovProviderPlugins[0].instantiate() }
  }
}

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

import com.worksap.nlp.sudachi.dictionary.build.DicBuilder
import java.nio.file.Paths
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
    assertEquals(
        SettingsAnchor.classpath(), SettingsAnchor.classpath().andThen(SettingsAnchor.classpath()))
  }

  @Test
  fun chain() {
    val chain = SettingsAnchor.classpath().andThen(SettingsAnchor.none())
    assertIs<SettingsAnchor.Chain>(chain)
    assertEquals(chain.count(), 2)
    val chain2 = chain.andThen(chain)
    assertSame(chain, chain2)
  }

  @Test
  fun chainChains() {
    val chain1 = SettingsAnchor.classpath().andThen(SettingsAnchor.none())
    val chain2 = SettingsAnchor.classpath().andThen(SettingsAnchor.none())
    val chain3 = chain1.andThen(chain2)
    assertIs<SettingsAnchor.Chain>(chain3)
    assertEquals(chain3.count(), 2)
  }

  @Test
  fun chainChains2() {
    val chain1 = SettingsAnchor.classpath().andThen(SettingsAnchor.filesystem(Paths.get("")))
    val chain3 = SettingsAnchor.none().andThen(chain1)
    assertIs<SettingsAnchor.Chain>(chain3)
    assertEquals(chain3.count(), 3)
  }

  @Test
  fun chainExists() {
    val a1 = SettingsAnchor.classpath(DicBuilder::class.java)
    assertTrue(a1.exists(a1.resolve("one.csv")))
    val a2 = SettingsAnchor.none()
    assertFalse(a2.exists(a2.resolve("one.csv")))
    val a3 = a2.andThen(a1)
    assertTrue(a3.exists(a3.resolve("one.csv")))
  }

  @Test
  fun hashCodeWorks() {
    val a =
        SettingsAnchor.classpath()
            .andThen(SettingsAnchor.filesystem(Paths.get("")))
            .andThen(SettingsAnchor.none())
    assertNotEquals(a.hashCode(), SettingsAnchor.none().hashCode())
    assertNotEquals(a, SettingsAnchor.none())
  }
}
